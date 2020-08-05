package com.clay.covid_19tracker.adapters

import android.graphics.RectF
import com.clay.covid_19tracker.models.CovidData
import com.clay.covid_19tracker.util.Metric
import com.clay.covid_19tracker.util.TimeScale
import com.robinhood.spark.SparkAdapter

class CovidSparkAdapter : SparkAdapter() {

    var dailyData = listOf<CovidData>()

    var metric = Metric.POSITIVE

    var daysAgo = TimeScale.MAX

    override fun getY(index: Int): Float {
        val chosenDayData = dailyData[index]
        return when (metric) {
            Metric.NEGATIVE -> chosenDayData.negativeIncrease?.toFloat()!!
            Metric.POSITIVE -> chosenDayData.positiveIncrease?.toFloat()!!
            Metric.DEATH -> chosenDayData.deathIncrease?.toFloat()!!
        }
    }


    override fun getItem(index: Int) = dailyData[index]

    override fun getCount() = dailyData.size

    override fun getDataBounds(): RectF {
        val bounds =  super.getDataBounds()
        if(daysAgo != TimeScale.MAX) {
            bounds.apply {
                left = count - daysAgo.numDays.toFloat()
            }
        }
        return bounds
    }
}