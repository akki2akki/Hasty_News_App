package com.hasty.newsapp.repositories

import com.hasty.newsapp.api.RetrofitInstance

class NewsRepository {


    suspend fun getAllBreakingNews(
        countryCode: String,
        pageNumber: Int
    ) = RetrofitInstance.api.getBreakingNews(
        countryCode, pageNumber)


    suspend fun searchNews(
        searchQuery: String,
        pageNumber: Int
    ) = RetrofitInstance.api.searchForNews(
        searchQuery, pageNumber
    )


}