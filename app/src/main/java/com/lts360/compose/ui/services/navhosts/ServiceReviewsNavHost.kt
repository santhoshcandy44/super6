package com.lts360.compose.ui.services.navhosts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.lts360.api.models.service.Service
import com.lts360.compose.ui.services.CommentsList
import com.lts360.compose.ui.services.ReplyScreen
import com.lts360.compose.ui.viewmodels.ServicesViewModel

/*
sealed class ServiceReviewsRoutes{

    data object Reviews: ServiceReviewsRoutes()

    data class ReviewsReplies(val commentId:Int): ServiceReviewsRoutes()
}

@Composable
fun ServiceReviewsNavHost(userId:Long, selectedItem: Service?, viewModel:ServicesViewModel){

    val selectedReviews by viewModel.selectedReviews.collectAsState()

    val selectedReviewReplies by viewModel.selectedReviewReplies.collectAsState()
    val isReviewsLoading by viewModel.isReviewsLoading.collectAsState()
    val isReviewsReplyLoading by viewModel.isReviewsReplyLoading.collectAsState()
    val isReviewPosting by viewModel.isReviewPosting.collectAsState()
    val isReplyPosting by viewModel.isReplyPosting.collectAsState()

    val selectedCommentId by viewModel.selectedCommentId.collectAsState()

    // Create a NavController for navigation inside the BottomSheet
    val commentsNavController = rememberNavController()
    // Navigation Host for Bottom Sheet
    NavHost(commentsNavController, startDestination = "comments") {
        composable<ServiceReviewsRoutes.Reviews> {
            CommentsList(
                navController = commentsNavController,
                isReviewsLoading,
                isReviewPosting,
                selectedReviews,
                { commentText ->
                    selectedItem?.let {
                        viewModel.insertReview(
                            it.serviceId,
                            userId,
                            commentText
                        )
                    }

                },
                { comment ->

                    if (selectedCommentId != comment.id) {
                        viewModel.loadReViewRepliesSelectedItem(comment)
                    }
                    // Navigate to the replies screen
                    commentsNavController.navigate(ServiceReviewsRoutes.ReviewsReplies(comment.id))

                }
            )
        }
        composable<ServiceReviewsRoutes.ReviewsReplies> { backStackEntry ->

            val args = backStackEntry.toRoute<ServiceReviewsRoutes.ReviewsReplies>()
            val commentId = args.commentId


            val selectedComment = selectedReviews.find { it.id == commentId }

            selectedComment?.let {
                ReplyScreen(
                    selectedComment,
                    isReplyPosting,
                    isReviewsReplyLoading,
                    selectedReviewReplies,
                    {
                        commentsNavController.popBackStack() // Go back to the comments screen
                    },
                    { commentText, selectedReply ->

                        selectedItem?.let {
                            viewModel.insertReply(
                                selectedComment.id,
                                userId,
                                commentText,
                                selectedReply?.user?.userId
                            )
                        }

                    })
            }
        }
    }
}*/
