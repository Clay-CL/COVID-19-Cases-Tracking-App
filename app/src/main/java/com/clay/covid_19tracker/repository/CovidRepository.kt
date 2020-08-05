package com.clay.covid_19tracker.repository

import com.clay.covid_19tracker.api.RetrofitInstance

class CovidRepository {

    suspend fun getNationalData() = RetrofitInstance.api.getNationalData()

    suspend fun getStatesData() = RetrofitInstance.api.getStatesData()

}