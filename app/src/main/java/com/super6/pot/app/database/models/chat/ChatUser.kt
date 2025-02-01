package com.super6.pot.app.database.models.chat



import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.super6.pot.api.models.service.FeedUserProfileInfo
import com.super6.pot.app.database.models.service.converters.Converters
import kotlinx.parcelize.Parcelize

@Parcelize
@TypeConverters(Converters::class)
@Entity(tableName = "chat_users", indices = [Index(value = ["recipient_id"], unique = true)])
data class ChatUser(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "chat_id")
    val chatId: Int = 0,

    @ColumnInfo(name = "user_id")
    val userId: Long,

    @ColumnInfo(name = "recipient_id")
    val recipientId: Long,

    @ColumnInfo(name = "user_profile_info")
    val userProfile: FeedUserProfileInfo,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long,

    @ColumnInfo(name = "public_key") val publicKeyBase64: String?=null,

    @ColumnInfo(name = "key_version") val keyVersion: Long?=-1) : Parcelable
