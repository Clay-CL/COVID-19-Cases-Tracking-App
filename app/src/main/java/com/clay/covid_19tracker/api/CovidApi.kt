package com.clay.covid_19tracker.api

import com.clay.covid_19tracker.models.CovidData
import retrofit2.Response
import retrofit2.http.GET

interface CovidApi {

    @GET("us/daily.json")
    suspend fun getNationalData(): Response<MutableList<CovidData>>

    @GET("states/daily.json")
    suspend fun getStatesData(): Response<MutableList<CovidData>>
}