package com.super6.pot.ui.main.models

import com.google.gson.annotations.SerializedName


data class SearchTerm(
    @SerializedName("search_term")
    val searchTerm: String,
)

