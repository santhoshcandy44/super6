package com.lts360.test

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import androidx.compose.runtime.collectAsState


sealed class JobRoutes : NavKey {
    @Serializable
    data object JobPostings : JobRoutes()

    @Serializable
    data object JobDetails : JobRoutes()
}

@Composable
fun JobNavHost() {
    val backStacks = rememberNavBackStack(JobRoutes.JobPostings)
    val viewModel: JobPostingsViewModel = koinViewModel()

    NavDisplay(
        backStack = backStacks,
        entryDecorators = listOf(
            rememberSceneSetupNavEntryDecorator(),
            rememberSavedStateNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {
                entry<JobRoutes.JobPostings> {
                    JobPostings(viewModel) {
                        backStacks.add(JobRoutes.JobDetails)
                    }
                }

                entry<JobRoutes.JobDetails> {
                    val selectedJob = viewModel.selectedJobPosting.collectAsState().value
                    selectedJob?.let {
                        DetailedJobPosting(selectedJob) {
                            backStacks.removeLastOrNull()
                        }
                    } ?: run {
                        LaunchedEffect(Unit) {
                            backStacks.removeLastOrNull()
                        }
                    }
                }
            }
    )
}