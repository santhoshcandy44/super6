package com.lts360.compose.ui.main.models

import com.google.gson.annotations.SerializedName


data class SearchTerm(
    @SerializedName("search_term")
    val searchTerm: String,
)

