package com.clay.covid_19tracker.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.clay.covid_19tracker.R
import com.clay.covid_19tracker.adapters.CovidSparkAdapter
import com.clay.covid_19tracker.models.CovidData
import com.clay.covid_19tracker.ui.MainActivity
import com.clay.covid_19tracker.ui.MainViewModel
import com.clay.covid_19tracker.util.Metric
import com.clay.covid_19tracker.util.Resource
import com.clay.covid_19tracker.util.TimeScale
import com.robinhood.ticker.TickerUtils
import kotlinx.android.synthetic.main.fragment_covid.*
import timber.log.Timber
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

private const val ALL_STATES: String = "All States"

class CovidFragment : Fragment(R.layout.fragment_covid) {

    lateinit var viewModel: MainViewModel
    lateinit var sparkAdapter: CovidSparkAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = (activity as MainActivity).viewModel
        setUpAdapter()
        subscribeToObservers()
    }

    private fun subscribeToObservers() {
        viewModel.covidData.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Success -> {
                    setUpEventListeners()
                    it.data?.let { covidData ->
                        // covidResponse contains the list of days of the COVID-19 data
                        updateGraph(covidData)
                    }
                }
                is Resource.Error -> {
                    Timber.d("Error Occured : ${it.message}")
                }
                is Resource.Loading -> {
                    Timber.d("Loading ....")
                }
            }
        })


        viewModel.stateCovidData.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Success -> {
                    setUpEventListeners()
                    it.data?.let { stateCovidData ->
                        // covidResponse contains the list of days of the COVID-19 data
                        val covidStateDataMap = stateCovidData
                        updateSpinnerWithStateData(covidStateDataMap.keys)
                    }
                }
                is Resource.Error -> {
                    Timber.d("Error Occured : ${it.message}")
                }
                is Resource.Loading -> {
                    Timber.d("Loading ....")
                }
            }
        })

        viewModel.metricData.observe(viewLifecycleOwner, Observer {
            it?.let {
                updateRadioGroupMetrics(it)
                updateColorSparkChart(it)
            }
            sparkAdapter.metric = it

            viewModel.stateCovidData.value?.let {
                when (it) {
                    is Resource.Success -> {
                        viewModel.selectedState.value?.let { position ->

                            viewModel.stateCovidData.value?.data?.keys?.let {
                                updateSpinnerWithStateData(it)
                            }
                            stateSpinner.selectedIndex = position
                            val selectedState = stateSpinner.getItemAtPosition(position) as String
                            val selectedData =
                                viewModel.stateCovidData.value?.data?.get(selectedState)
                                    ?.toMutableList()
                                    ?: viewModel.covidData.value?.data
                            selectedData?.let { updateGraph(it) }
                        }
                    }
                    is Resource.Loading -> {

                    }
                    is Resource.Error -> {

                    }
                }
            }

//            viewModel.covidData.value?.data?.let {
//                updateInfoForDate(it.last())
//            }
        })

        viewModel.timeScale.observe(viewLifecycleOwner, Observer {
            it?.let {
                updateRadioGroupTimeScale(it)
            }
            sparkAdapter.daysAgo = it

            viewModel.stateCovidData.value?.let {
                when (it) {
                    is Resource.Success -> {
                        viewModel.selectedState.value?.let { position ->

                            viewModel.stateCovidData.value?.data?.keys?.let {
                                updateSpinnerWithStateData(it)
                            }
                            stateSpinner.selectedIndex = position
                            val selectedState = stateSpinner.getItemAtPosition(position) as String
                            val selectedData =
                                viewModel.stateCovidData.value?.data?.get(selectedState)
                                    ?.toMutableList()
                                    ?: viewModel.covidData.value?.data
                            selectedData?.let { updateGraph(it) }
                        }
                    }
                    is Resource.Loading -> {

                    }
                    is Resource.Error -> {

                    }
                }
            }
        })

        viewModel.selectedState.observe(viewLifecycleOwner, Observer { position ->
            viewModel.stateCovidData.value?.let {
                when (it) {
                    is Resource.Success -> {
                        viewModel.stateCovidData.value?.data?.keys?.let {
                            updateSpinnerWithStateData(it)
                        }
                        stateSpinner.selectedIndex = position
                        val selectedState = stateSpinner.getItemAtPosition(position) as String
                        val selectedData =
                            viewModel.stateCovidData.value?.data?.get(selectedState)
                                ?.toMutableList()
                                ?: viewModel.covidData.value?.data
                        selectedData?.let { updateGraph(it) }
                    }
                    is Resource.Loading -> {

                    }
                    is Resource.Error -> {

                    }
                }
            }


        })

    }

    private fun updateSpinnerWithStateData(stateNames: Set<String>) {
        val stateAbbreviationList = stateNames.toMutableList()
        stateAbbreviationList.sort()
        stateAbbreviationList.add(0, ALL_STATES)

        stateSpinner.attachDataSource(stateAbbreviationList)
    }

    private fun updateColorSparkChart(metric: Metric) {
        @ColorInt val colorMetric = when (metric) {
            Metric.NEGATIVE -> ContextCompat.getColor(
                requireContext(),
                R.color.colorNegativeIncrease
            )
            Metric.POSITIVE -> ContextCompat.getColor(
                requireContext(),
                R.color.colorPositiveIncrease
            )
            Metric.DEATH -> ContextCompat.getColor(requireContext(), R.color.colorDeathIncrease)
        }

        sparkChart.lineColor = colorMetric
        casesTicker.setTextColor(colorMetric)

    }

    private fun updateRadioGroupTimeScale(timeScale: TimeScale) {
        when (timeScale) {
            TimeScale.MONTH -> radioButtonMonth
            TimeScale.WEEK -> radioButtonWeek
            TimeScale.MAX -> radioButtonMax
        }.isChecked = true
    }

    private fun updateRadioGroupMetrics(metric: Metric) {
        when (metric) {
            Metric.POSITIVE -> radioButtonPositive
            Metric.NEGATIVE -> radioButtonNegative
            Metric.DEATH -> radioButtonDeaths
        }.isChecked = true
    }


    private fun setUpEventListeners() {

        casesTicker.setCharacterLists(TickerUtils.provideNumberList())

        sparkChart.isScrubEnabled = true
        sparkChart.setScrubListener { itemData ->
            if (itemData is CovidData) {
                updateInfoForDate(itemData)
            }
        }

        radioGrpDuration.setOnCheckedChangeListener { _, checkedId ->
            sparkAdapter.daysAgo = when (checkedId) {
                R.id.radioButtonMonth -> TimeScale.MONTH
                R.id.radioButtonWeek -> TimeScale.WEEK
                else -> TimeScale.MAX
            }
            viewModel.timeScale.postValue(sparkAdapter.daysAgo)
        }

        radioGrpCasesType.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioButtonNegative -> updateDisplayMetric(Metric.NEGATIVE)
                R.id.radioButtonPositive -> updateDisplayMetric(Metric.POSITIVE)
                R.id.radioButtonDeaths -> updateDisplayMetric(Metric.DEATH)

            }
        }

        stateSpinner.setOnSpinnerItemSelectedListener { parent, _, position, _ ->
            viewModel.selectedState.postValue(position)
        }

    }

    private fun updateDisplayMetric(metric: Metric) {
        viewModel.metricData.postValue(metric)

    }

    private fun updateGraph(covidData: MutableList<CovidData>) {
        updateInfoForDate(covidData.last())
        sparkAdapter.dailyData = covidData.toList()
        sparkAdapter.notifyDataSetChanged()
    }

    private fun updateInfoForDate(covidData: CovidData) {

        Timber.d(covidData.positiveIncrease.toString())

        val numCases = when (sparkAdapter.metric) {
            Metric.NEGATIVE -> covidData.negativeIncrease!!
            Metric.POSITIVE -> covidData.positiveIncrease!!
            Metric.DEATH -> covidData.deathIncrease!!
        }

        casesTicker.text = NumberFormat.getInstance().format(numCases)
        val outputDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
        dateLabel.text = outputDateFormat.format(covidData.dateChecked!!)
    }

    private fun setUpAdapter() {
        sparkAdapter = CovidSparkAdapter()
        sparkChart.adapter = sparkAdapter
    }



}
