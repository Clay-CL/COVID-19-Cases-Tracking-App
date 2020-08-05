package com.clay.covid_19tracker.models

import java.util.*

data class CovidData(
    val date: Int?,
    val dateChecked: Date?,
    val death: Int?,
    val deathIncrease: Int?,
    val hash: String?,
    val hospitalized: Int?,
    val hospitalizedCumulative: Int?,
    val hospitalizedCurrently: Int?,
    val hospitalizedIncrease: Int?,
    val inIcuCumulative: Int?,
    val inIcuCurrently: Int?,
    val lastModified: String?,
    val negative: Int?,
    val negativeIncrease: Int?,
    val onVentilatorCumulative: Int?,
    val onVentilatorCurrently: Int?,
    val pending: Int?,
    val posNeg: Int?,
    val positive: Int?,
    val positiveIncrease: Int?,
    val recovered: Int?,
    val state: String,
    val total: Int?,
    val totalTestResults: Int?,
    val totalTestResultsIncrease: Int?
)