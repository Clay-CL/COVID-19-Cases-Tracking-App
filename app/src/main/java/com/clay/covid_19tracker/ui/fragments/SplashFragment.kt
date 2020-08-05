package com.clay.covid_19tracker.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController

import com.clay.covid_19tracker.R
import com.clay.covid_19tracker.ui.MainActivity
import com.clay.covid_19tracker.ui.MainViewModel
import com.clay.covid_19tracker.util.Resource
import kotlinx.android.synthetic.main.fragment_splash.*
import timber.log.Timber

const val DIALOG_TAG = "DIALOG_TAG"

class SplashFragment : Fragment(R.layout.fragment_splash) {

    lateinit var viewModel: MainViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as MainActivity).viewModel

        if (savedInstanceState != null) {
            //Timber.d("List of fragments size ${parentFragmentManager.fragments.size}")
            val errorDialog = parentFragmentManager.findFragmentByTag(DIALOG_TAG) as ErrorDialog?
            errorDialog?.dismiss()
            parentFragmentManager.fragments.clear()
            errorDialog?.apply {
                title = "Something Went Wrong"
                viewModel.stateCovidData.value?.message?.let { msg ->
                    this.message = msg
                }
                setYesListener {
                    this.dismiss()
                    this.dialog?.cancel()
                    parentFragmentManager.fragments.clear()
                    viewModel.getCovidData()
                    viewModel.getStateCovidData()
                }
                setNoListener {
                    this.dismiss()
                    (activity as MainActivity).onBackPressed()
                }
            }

        }

        viewModel.stateCovidData.observe(viewLifecycleOwner, Observer { resourceData ->
            when (resourceData) {
                is Resource.Success -> {
                    findNavController().navigate(R.id.action_splashFragment_to_covidFragment)
                }
                is Resource.Error -> {
                    // create a dialog box with an error
                    ErrorDialog().apply {
                        title = "Something Went Wrong"
                        resourceData.message?.let { msg ->
                            this.message = msg
                        }
                        setYesListener {
                            this.dismiss()
                            parentFragmentManager.fragments.clear()
                            viewModel.getCovidData()
                            viewModel.getStateCovidData()
                        }
                        setNoListener {
                            this.dismiss()
                            (activity as MainActivity).onBackPressed()
                        }
                    }.show(parentFragmentManager, DIALOG_TAG)
                }
                is Resource.Loading -> {
                    statusText.text = "Fetching Data..."
                }
            }
        })

    }

}
