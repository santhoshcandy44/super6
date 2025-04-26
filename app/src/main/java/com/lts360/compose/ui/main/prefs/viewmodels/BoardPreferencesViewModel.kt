package com.lts360.compose.ui.main.prefs.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.lts360.api.utils.Result
import com.lts360.api.utils.ResultError
import com.lts360.api.utils.mapExceptionToError
import com.lts360.api.app.AppClient
import com.lts360.api.common.errors.ErrorResponse
import com.lts360.api.common.responses.ResponseReply
import com.lts360.api.prefs.BoardPreferenceApiService
import com.lts360.app.database.daos.prefs.BoardDao
import com.lts360.app.database.models.app.Board
import com.lts360.app.database.models.app.toBoardPref
import com.lts360.components.utils.LogUtils.TAG
import com.lts360.components.utils.errorLogger
import com.lts360.compose.ui.managers.NetworkConnectivityManager
import com.lts360.compose.ui.managers.UserSharedPreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


data class BoardPref(
    @SerializedName("board_id")
    val boardId: Int,
    @SerializedName("board_name")
    val boardName: String,
    @SerializedName("board_label")
    val boardLabel: String,
    @SerializedName("is_selected")
    val isSelected: Boolean,
    @SerializedName("display_order")
    val displayOrder: Int
)


@HiltViewModel
class BoardPreferencesViewModel @Inject constructor(
    private val boardDao: BoardDao,
    val repository: BoardsPreferencesRepository,
    networkConnectivityManager: NetworkConnectivityManager
) : ViewModel() {

    val userId = UserSharedPreferencesManager.userId

    val connectivityManager = networkConnectivityManager

    private val _allBoards = MutableStateFlow<List<BoardPref>>(emptyList())
    val allBoards = _allBoards.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isUpdating = MutableStateFlow(false)
    val isUpdating = _isUpdating.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    var errorMessage: String = ""

    private val _error = MutableStateFlow<ResultError?>(null)
    val error = _error.asStateFlow()


    init {
        onGetBoards(userId)
    }


    fun updateBoardSelectionStatus(boardId: Int) {
        _allBoards.update {
            val updatedBoards = _allBoards.value.map {
                if (it.boardId == boardId) {
                    it.copy(isSelected = !it.isSelected)
                } else {
                    it
                }
            }.toMutableList()

            updatedBoards.filter { it.isSelected }
                .forEachIndexed { index, board ->

                    val boardIndex = updatedBoards.indexOfFirst { it.boardId == board.boardId }
                    if (boardIndex != -1) {
                        updatedBoards[boardIndex] = board.copy(displayOrder = index)
                    }

                }

            updatedBoards
                .filterNot { it.isSelected }
                .forEach { board ->
                    val boardIndex = updatedBoards.indexOfFirst { it.boardId == board.boardId }
                    if (boardIndex != -1) {
                        updatedBoards[boardIndex] = board.copy(displayOrder = -1)
                    }
                }

            updatedBoards
        }

    }

    fun deselectBoard(boardId: Int) {
        _allBoards.update {
            _allBoards.value.map {
                if (it.boardId == boardId) {
                    it.copy(isSelected = false)
                } else {
                    it
                }
            }
        }
    }

    fun boardOrderChange(movedBoardId: Int, newPosition: Int) {
        _allBoards.update { currentBoards ->
            // Create a mutable copy
            val updatedBoards = currentBoards.toMutableList()

            // Get all selected boards sorted by current display order
            val selectedBoards = updatedBoards
                .filter { it.isSelected && it.displayOrder >= 0 }
                .sortedBy { it.displayOrder }


            // Find the moved board (throw exception if not found)
            val movedBoard = selectedBoards.firstOrNull { it.boardId == movedBoardId }
                ?: return@update currentBoards

            // Create new ordered list without the moved board
            val remainingBoards = selectedBoards.toMutableList().apply {
                remove(movedBoard)
            }

            // Ensure newPosition is within valid bounds
            val safeNewPosition = newPosition.coerceIn(0, remainingBoards.size)

            // Insert at new position
            remainingBoards.add(safeNewPosition, movedBoard)

            // Update displayOrder for all reordered boards
            remainingBoards.forEachIndexed { index, board ->
                val boardIndex = updatedBoards.indexOfFirst { it.boardId == board.boardId }
                if (boardIndex != -1) {
                    updatedBoards[boardIndex] = board.copy(displayOrder = index)
                }
            }

            // Also update non-selected boards to ensure their displayOrder stays consistent
            updatedBoards
                .filterNot { it.isSelected }
                .forEach { board ->
                    val boardIndex = updatedBoards.indexOfFirst { it.boardId == board.boardId }
                    if (boardIndex != -1) {
                        // Maintain their original displayOrder or adjust as needed
                        updatedBoards[boardIndex] = board.copy(displayOrder = -1)
                    }
                }

            updatedBoards
        }

    }

    fun validateSelectedBoards() = _allBoards.value.any { it.isSelected }

    fun setRefreshing(isRefreshing: Boolean) {
        _isRefreshing.value = isRefreshing
    }

    private fun updateError(exception: Throwable?) {
        _error.value = exception?.let {
            mapExceptionToError(it)
        }
    }


    fun onGetBoards(
        userId: Long,
        isLoading: Boolean = true,
        isRefreshing: Boolean = false,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {


            try {
                if (isLoading) {
                    _isLoading.value = true

                }
                if (isRefreshing) {
                    _isRefreshing.value = true

                }

                when (val result = repository.getBoards(userId)) {
                    is Result.Success -> {

                        _allBoards.value = Gson().fromJson(
                            result.data.data,
                            object : TypeToken<List<BoardPref>>() {}.type
                        )

                        boardDao.clearAndInsertSelectedBoards(_allBoards.value)

                        onSuccess()
                        updateError(null)
                    }

                    is Result.Error -> {
                        if (result.error is CancellationException) {
                            return@launch
                        }
                        val error = mapExceptionToError(result.error)
                        errorMessage = error.errorMessage
                        onError(errorMessage)
                        updateError(result.error)
                        // Handle the error and update the UI accordingly
                    }

                }


            } catch (t: Throwable) {
                errorMessage = "Something went wrong"
                onError(errorMessage)
                t.printStackTrace()
            } finally {

                if (isLoading) {
                    _isLoading.value = false
                }
                if (isRefreshing) {
                    _isRefreshing.value = false
                }
            }
        }
    }


    fun onUpdateBoards(
        userId: Long,
        selectedItems: List<BoardPref>,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {
            try {
                _isUpdating.value = true
                when (val result = repository.updateBoards(
                    userId,
                    selectedItems
                )) {
                    is Result.Success -> {
                        _allBoards.value = emptyList()
                        _allBoards.value = Gson().fromJson(
                            result.data.data,
                            object : TypeToken<List<BoardPref>>() {}.type
                        )

                        boardDao.clearAndInsertSelectedBoards(_allBoards.value)
                        onSuccess(result.data.message)

                    }

                    is Result.Error -> {
                        if (result.error is CancellationException) {
                            return@launch
                        }
                        updateError(result.error)
                        errorMessage = mapExceptionToError(result.error).errorMessage
                        onError(errorMessage)

                    }

                }
            } catch (t: Throwable) {
                errorMessage = "Something went wrong"
                onError(errorMessage)
                t.printStackTrace()
            } finally {
                _isUpdating.value = false
            }

        }

    }


}


@HiltViewModel
class GuestBoardPreferencesViewModel @Inject constructor(
    private val boardDao: BoardDao,
    val repository: BoardsPreferencesRepository,
    networkConnectivityManager: NetworkConnectivityManager
) : ViewModel() {

    val userId = UserSharedPreferencesManager.userId

    val connectivityManager = networkConnectivityManager

    private val _allBoards = MutableStateFlow<List<BoardPref>>(emptyList())
    val allBoards = _allBoards.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isUpdating = MutableStateFlow(false)
    val isUpdating = _isUpdating.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    var errorMessage: String = ""

    private val _error = MutableStateFlow<ResultError?>(null)
    val error = _error.asStateFlow()


    init {
        onGetBoards(userId)
    }


    fun updateBoardSelectionStatus(boardId: Int) {
        _allBoards.update {

            val updatedBoards = it.map {
                if (it.boardId == boardId) {
                    it.copy(isSelected = !it.isSelected)
                } else {
                    it
                }
            }.toMutableList()

            updatedBoards.filter { it.isSelected }
                .forEachIndexed { index, board ->

                    val boardIndex = updatedBoards.indexOfFirst { it.boardId == board.boardId }
                    if (boardIndex != -1) {
                        updatedBoards[boardIndex] = board.copy(displayOrder = index)
                    }

                }

            updatedBoards
                .filterNot { it.isSelected }
                .forEach { board ->
                    val boardIndex = updatedBoards.indexOfFirst { it.boardId == board.boardId }
                    if (boardIndex != -1) {
                        updatedBoards[boardIndex] = board.copy(displayOrder = -1)
                    }
                }


            updatedBoards
        }

    }

    fun deselectBoard(boardId: Int) {
        _allBoards.update {
            _allBoards.value.map {
                if (it.boardId == boardId) {
                    it.copy(isSelected = false)
                } else {
                    it
                }
            }
        }
    }

    fun boardOrderChange(movedBoardId: Int, newPosition: Int) {
        _allBoards.update { currentBoards ->
            // Create a mutable copy
            val updatedBoards = currentBoards.toMutableList()

            // Get all selected boards sorted by current display order
            val selectedBoards = updatedBoards
                .filter { it.isSelected && it.displayOrder >= 0 }
                .sortedBy { it.displayOrder }

            // Find the moved board (throw exception if not found)
            val movedBoard = selectedBoards.firstOrNull { it.boardId == movedBoardId }
                ?: return@update currentBoards

            // Create new ordered list without the moved board
            val remainingBoards = selectedBoards.toMutableList().apply {
                remove(movedBoard)
            }

            // Ensure newPosition is within valid bounds
            val safeNewPosition = newPosition.coerceIn(0, remainingBoards.size)

            // Insert at new position
            remainingBoards.add(safeNewPosition, movedBoard)

            // Update displayOrder for all reordered boards
            remainingBoards.forEachIndexed { index, board ->
                val boardIndex = updatedBoards.indexOfFirst { it.boardId == board.boardId }
                if (boardIndex != -1) {
                    updatedBoards[boardIndex] = board.copy(displayOrder = index)
                }
            }

            // Also update non-selected boards to ensure their displayOrder stays consistent
            updatedBoards
                .filterNot { it.isSelected }
                .forEach { board ->
                    val boardIndex = updatedBoards.indexOfFirst { it.boardId == board.boardId }
                    if (boardIndex != -1) {
                        // Maintain their original displayOrder or adjust as needed
                        updatedBoards[boardIndex] = board.copy(displayOrder = -1)
                    }
                }


            updatedBoards
        }

    }

    fun setRefreshing(isRefreshing: Boolean) {
        _isRefreshing.value = isRefreshing
    }

    private fun updateError(exception: Throwable?) {
        _error.value = exception?.let {
            mapExceptionToError(it)
        }
    }

    fun validateSelectedBoards() = _allBoards.value.any { it.isSelected }


    fun onGetBoards(
        userId: Long,
        isLoading: Boolean = true,
        isRefreshing: Boolean = false,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {},
    ) {
        viewModelScope.launch {


            try {
                if (isLoading) {
                    _isLoading.value = true

                }
                if (isRefreshing) {
                    _isRefreshing.value = true
                }

                when (val result = repository.guestGetBoards(userId)) {
                    is Result.Success -> {


                        withContext(Dispatchers.IO) {
                            val allBoards = boardDao.getAllBoards()

                            val boardPrefList = Gson().fromJson(
                                result.data.data,
                                object : TypeToken<List<BoardPref>>() {}.type
                            ) as List<BoardPref>

                            val allBoardsMap = allBoards.associateBy { it.boardId }

                            _allBoards.update { boards ->
                                boardPrefList.map { boardItem ->
                                    allBoardsMap[boardItem.boardId]?.toBoardPref(
                                        true
                                    ) ?: run {
                                        boardItem.copy(isSelected = false)
                                    }
                                }
                            }

                            boardDao.clearAndInsertSelectedBoards(_allBoards.value)
                        }


                        onSuccess()
                        updateError(null)
                    }

                    is Result.Error -> {
                        if (result.error is CancellationException) {
                            return@launch
                        }
                        val error = mapExceptionToError(result.error)
                        errorMessage = error.errorMessage
                        onError(errorMessage)
                        updateError(result.error)
                    }

                }


            } catch (t: Throwable) {
                errorMessage = "Something went wrong"
                onError(errorMessage)
                t.printStackTrace()
            } finally {

                if (isLoading) {
                    _isLoading.value = false
                }
                if (isRefreshing) {
                    _isRefreshing.value = false
                }
            }
        }
    }


    fun onUpdateBoards(
        items: List<BoardPref>,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {
            try {
                _isUpdating.value = true

                val allBoardsFromDb = withContext(Dispatchers.IO) {
                    boardDao.clearAndInsertSelectedBoards(items)
                    boardDao.getAllBoards()
                }

                val allBoardsMap = allBoardsFromDb.associateBy { it.boardId }

                _allBoards.update { boards ->
                    boards.map { board ->
                        val boardFromDb = allBoardsMap[board.boardId]

                        if (boardFromDb == null) {
                            board
                        } else {
                            board.copy(isSelected = true)
                        }
                    }
                }

                onSuccess("Boards Updated")

            } catch (t: Throwable) {
                errorMessage = "Something went wrong"
                onError(errorMessage)
                t.printStackTrace()
            } finally {
                _isUpdating.value = false
            }

        }

    }

}


class BoardsPreferencesRepository @Inject constructor() {


    suspend fun getBoards(userId: Long): Result<ResponseReply> {
        return try {
            val response =
                AppClient.instance.create(BoardPreferenceApiService::class.java).getBoards(userId)
            if (response.isSuccessful) {

                val responseBody = response.body()

                if (responseBody != null && responseBody.isSuccessful) {
                    Result.Success(responseBody)
                } else {
                    val errorMessage = "Failed, try again later..."
                    Result.Error(Exception(errorMessage))
                }

            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = try {
                    Gson().fromJson(errorBody, ErrorResponse::class.java).message
                } catch (e: Exception) {
                    "An unknown error occurred"
                }
                Result.Error(Exception(errorMessage))
            }
        } catch (t: Throwable) {
            Result.Error(t)
        }

    }

    suspend fun updateBoards(
        userId: Long,
        selectedItems: List<BoardPref>,
    ): Result<ResponseReply> {
        return try {


            val response = AppClient.instance.create(BoardPreferenceApiService::class.java)
                .updateBoards(userId, Gson().toJson(selectedItems))
            if (response.isSuccessful) {

                val responseBody = response.body()

                if (responseBody != null && responseBody.isSuccessful) {
                    Result.Success(responseBody)

                } else {
                    val errorMessage = "Failed, try again later..."
                    Result.Error(Exception(errorMessage))
                }


            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = try {
                    Gson().fromJson(errorBody, ErrorResponse::class.java).message
                } catch (e: Exception) {
                    "An unknown error occurred"
                }
                Result.Error(Exception(errorMessage))
            }
        } catch (t: Throwable) {
            Result.Error(t)
        }

    }


    suspend fun guestGetBoards(userId: Long): Result<ResponseReply> {
        return try {
            val response =
                AppClient.instance.create(BoardPreferenceApiService::class.java)
                    .getGuestBoards(userId)
            if (response.isSuccessful) {

                val responseBody = response.body()

                if (responseBody != null && responseBody.isSuccessful) {
                    Result.Success(responseBody)
                } else {
                    val errorMessage = "Failed, try again later..."
                    Result.Error(Exception(errorMessage))
                }

            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = try {
                    Gson().fromJson(errorBody, ErrorResponse::class.java).message
                } catch (e: Exception) {
                    "An unknown error occurred"
                }
                Result.Error(Exception(errorMessage))
            }
        } catch (t: Throwable) {
            Result.Error(t)
        }

    }

}