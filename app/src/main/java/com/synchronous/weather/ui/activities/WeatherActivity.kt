package com.synchronous.weather.ui.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.synchronous.weather.R
import com.synchronous.weather.databinding.ActivityWeatherBinding
import com.synchronous.weather.ui.viewmodel.WeatherViewModel
import com.synchronous.weather.ui.viewmodelfactory.WeatherViewModelFactory
import com.synchronous.weather.util.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance

class WeatherActivity : AppCompatActivity(), KodeinAware {

    override val kodein by closestKodein()
    private lateinit var dataBind: ActivityWeatherBinding
    private val factory: WeatherViewModelFactory by instance()
    private val viewModel: WeatherViewModel by lazy {
        ViewModelProvider(this, factory).get(WeatherViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataBind = DataBindingUtil.setContentView(this, R.layout.activity_weather)
        setupUI()
        observeAPICall()
    }

    private fun setupUI() {
        dataBind.inputFindCityWeather.setOnEditorActionListener { view, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.fetchWeatherDetailFromDb((view as EditText).text.toString())
            }
            false
        }
    }


    @SuppressLint("SetTextI18n")
    private fun observeAPICall() {
        viewModel.weatherLiveData.observe(this, EventObserver { state ->
            when (state) {
                is State.Loading -> {
                }
                is State.Success -> {
                    dataBind.textLabelSearchForCity.hide()
                    dataBind.constraintLayoutShowingTemp.show()
                    dataBind.inputFindCityWeather.text?.clear()
                    state.data.let { weatherDetail ->
                        val iconCode = weatherDetail.icon?.replace("n", "d")
                        AppUtils.setGlideImage(
                            dataBind.imageWeatherSymbol,
                            AppConstants.WEATHER_API_IMAGE_ENDPOINT + "${iconCode}@4x.png"
                        )
                        dataBind.textTodaysDate.text =
                            AppUtils.getCurrentDateTime(AppConstants.DATE_FORMAT)
                        dataBind.textTemperature.text = weatherDetail.temp.toString()
                        dataBind.textCityName.text =
                            "${weatherDetail.cityName?.capitalize()}, ${weatherDetail.countryName}"
                    }

                }
                is State.Error -> {
                    showToast(state.message)
                }
            }
        })
    }



}