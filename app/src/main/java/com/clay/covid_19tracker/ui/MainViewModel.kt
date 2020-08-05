package com.clay.covid_19tracker.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.clay.covid_19tracker.BaseApplication
import com.clay.covid_19tracker.models.CovidData
import com.clay.covid_19tracker.repository.CovidRepository
import com.clay.covid_19tracker.util.Metric
import com.clay.covid_19tracker.util.Resource
import com.clay.covid_19tracker.util.TimeScale
import kotlinx.coroutines.launch
import retrofit2.Response
import timber.log.Timber
import java.io.IOException

class MainViewModel(app: Application, val covidRepository: CovidRepository) :
    AndroidViewModel(app) {

    val covidData: MutableLiveData<Resource<MutableList<CovidData>>> = MutableLiveData()

    val stateCovidData: MutableLiveData<Resource<Map<String, List<CovidData>>>> = MutableLiveData()

    val metricData: MutableLiveData<Metric> = MutableLiveData()

    val timeScale: MutableLiveData<TimeScale> = MutableLiveData()

    val selectedState : MutableLiveData<Int> = MutableLiveData()

    init {
        getCovidData()
        getStateCovidData()
        postInitValues()
    }

    private fun postInitValues() {
        metricData.postValue(Metric.POSITIVE)
        timeScale.postValue(TimeScale.MAX)
        selectedState.postValue(0)
    }

    fun getCovidData() {
        viewModelScope.launch {
            safeHandleCovidResponse()
        }
    }

    fun getStateCovidData() {
        viewModelScope.launch {
            safeHandleStateCovidResponse()
        }
    }

    private suspend fun safeHandleCovidResponse() {
        covidData.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = covidRepository.getNationalData()
                covidData.postValue(handleCovidResponse(response))
            } else {
                covidData.postValue(Resource.Error("No Internet Connection"))
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException -> covidData.postValue(Resource.Error("Network Failure"))
                else -> covidData.postValue(Resource.Error(t.message))
            }
        }

    }


    private fun handleCovidResponse(response: Response<MutableList<CovidData>>): Resource<MutableList<CovidData>>? {
        if (response.isSuccessful) {
            response.body()?.let {
                Timber.d(it.size.toString())
                it.reverse()
                return Resource.Success(it)
            }
        }
        Timber.d(response.message())
        return Resource.Error(response.message())
    }

    private suspend fun safeHandleStateCovidResponse() {
        stateCovidData.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = covidRepository.getStatesData()
                stateCovidData.postValue(handleStateCovidResponse(response))
            } else {
                stateCovidData.postValue(Resource.Error("No Internet Connection"))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> covidData.postValue(Resource.Error("Network Failure"))
                else -> covidData.postValue(Resource.Error(t.message))
            }
        }
    }

    private fun handleStateCovidResponse(response: Response<MutableList<CovidData>>): Resource<Map<String, List<CovidData>>>? {
        if (response.isSuccessful) {
            response.body()?.let {
                it.reverse()
                val stateDataMap = it.groupBy { it.state }
                return Resource.Success(stateDataMap)
            }
        }
        Timber.d(response.message())
        return Resource.Error(response.message())
    }


    // internet Utils

    private fun hasInternetConnection(): Boolean {
        //we need the connectivity manager
        // we don't necessarily need activity context
        // we can even use application context here as
        // as long as the app lives
        // the applicationContext is not null
        val connectivityManager = getApplication<BaseApplication>()
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val networkCapabilities =
                connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false

            return when {
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.activeNetworkInfo?.run {
                return when (this.type) {
                    ConnectivityManager.TYPE_WIFI -> true
                    ConnectivityManager.TYPE_MOBILE -> true
                    ConnectivityManager.TYPE_ETHERNET -> true
                    else -> false
                }
            }
        }

        return false

    }


}