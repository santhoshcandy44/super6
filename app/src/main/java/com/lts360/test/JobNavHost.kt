package com.lts360.test

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable


@Serializable
sealed class JobRoutes {
    @Serializable
    data object JobPostings : JobRoutes()
    @Serializable
    data object JobDetails : JobRoutes()
}



@Composable
fun JobNavHost() {
    val navController = rememberNavController()
    val viewModel : JobPostingsViewModel = hiltViewModel()

    NavHost(navController = navController, startDestination = JobRoutes.JobPostings) {
        composable<JobRoutes.JobPostings> {
            JobPostings( viewModel){
                navController.navigate(JobRoutes.JobDetails)
            }
        }

        composable<JobRoutes.JobDetails> {
            viewModel.selectedJobPosting.value?.let {
                DetailedJobPosting(it){
                    navController.popBackStack()
                }
            }
        }
    }
}