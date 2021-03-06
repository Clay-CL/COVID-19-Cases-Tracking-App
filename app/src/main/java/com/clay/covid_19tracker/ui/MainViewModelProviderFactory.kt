package com.clay.covid_19tracker.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.clay.covid_19tracker.repository.CovidRepository

class MainViewModelProviderFactory(val app: Application, val covidRepository: CovidRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MainViewModel(app, covidRepository) as T
    }
}