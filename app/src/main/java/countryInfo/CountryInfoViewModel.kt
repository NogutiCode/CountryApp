package countryInfo


import android.content.Context
import android.util.Log
import android.widget.ImageView
import androidx.core.content.ContextCompat
import countryRepository.CountryRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import app.CountryInfo
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.countryapp.R
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.wasabeef.glide.transformations.CropCircleWithBorderTransformation
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject


@HiltViewModel
class CountryInfoViewModel @Inject constructor(
    private val repository: CountryRepository
) : ViewModel() {

    data class ProcessedCountryInfo(
        val name: String,
        val formattedCapital: String,
        val flag: String,
        val currency: String,
        val formattedPopulation: String,
    )


    private val _currencyText = MutableStateFlow("")
    val currencyText: StateFlow<String> = _currencyText

    private val _capitalTexts = MutableStateFlow("")
    val capitalTexts: StateFlow<String> = _capitalTexts

    private val _borderCountriesStringFlow = MutableStateFlow("")
    val borderCountriesStringFlow: StateFlow<String> = _borderCountriesStringFlow

    private val _loadingStateFlow = MutableStateFlow(false)
    val loadingStateFlow: StateFlow<Boolean> = _loadingStateFlow

    private val _processedCountryInfoStateFlow = MutableStateFlow<ProcessedCountryInfo?>(null)
    val processedCountryInfoStateFlow: StateFlow<ProcessedCountryInfo?> = _processedCountryInfoStateFlow

    fun fetchCountryInfo(countryName: String) {
        viewModelScope.launch {
            _loadingStateFlow.value = true
            repository.fetchCountryInfo(countryName)
                .map { countryList ->
                    val selectedCountry = countryList.find { it.name?.common == countryName }
                    selectedCountry?.let { country ->
                        processCountry(country)
                    }
                }
                .flowOn(Dispatchers.Default)
                .collect { processedInfo ->
                    _processedCountryInfoStateFlow.value = processedInfo
                }
        }
    }

    private fun fetchBordersInfo(borders: String) {
        viewModelScope.launch {
            repository.fetchBordersInfo(borders)
                .map { borderCountries ->
                    val neededBorders = borderCountries.mapNotNull { it.name?.common }
                    val borderCountriesString = neededBorders.joinToString(", ")
                    Log.e("bob", neededBorders.toString())
                    _borderCountriesStringFlow.value = borderCountriesString
                }
                .flowOn(Dispatchers.Default)
                .collect { }
            _loadingStateFlow.value = false
        }
    }


    private fun formatNumberWithCommas(number: Int): String {
        val numberFormat = NumberFormat.getNumberInstance(Locale.US)
        return numberFormat.format(number)
    }

    private fun processCountryInfo(country: CountryInfo): ProcessedCountryInfo {
        val nameCountry = country.name?.common.toString()
        val capital = country.capital?.toString()?.replace("[", "")?.replace("]", "")
        val formattedCapital = capital ?: ""
        val flag = country.flags?.png.toString()

        val currencyFindFullName = country.currencies?.values?.firstOrNull()
        val currencyFullName: String? = currencyFindFullName?.name
        val currencyFindSmallName = country.currencies?.toList()?.firstOrNull()
        val currencySmallName: String? = currencyFindSmallName?.first
        val formattedCurrencySmallName = currencySmallName ?: ""
        val formattedCurrencyFullName = currencyFullName ?: ""

        val population = country.population.toString()
        val formattedPopulation = formatNumberWithCommas(population.toInt())

        return ProcessedCountryInfo(
            nameCountry,
            formattedCapital,
            flag,
            "$formattedCurrencySmallName $formattedCurrencyFullName",
            formattedPopulation
        )
    }


    private fun processCountry(
        country: CountryInfo
    ): ProcessedCountryInfo {
        val processedInfo = processCountryInfo(country)
        val borders = country.borders?.toString()?.filter { it.isLetterOrDigit() || it == ',' }

        if (processedInfo.currency.isBlank()) {
            _currencyText.value = "No own currency"
        } else {
            _currencyText.value = processedInfo.currency
        }

        if (processedInfo.formattedCapital.isBlank()) {
            _capitalTexts.value = "No own capital"
        } else {
            _capitalTexts.value = processedInfo.formattedCapital
        }

        if(borders != null){
            viewModelScope.launch {
                fetchBordersInfo(borders.toString())
            }
        }
        else{
            _borderCountriesStringFlow.value = "No have neighbours"
            _loadingStateFlow.value = false
        }

        return processedInfo
    }

    fun loadCountryImage(context: Context, imageUrl: String, imageView: ImageView) {
        val borderColor = ContextCompat.getColor(imageView.context, R.color.countryDesign)
        Glide.with(context)
            .load(imageUrl)
            .apply(
                RequestOptions()
                    .transform(
                        CropCircleWithBorderTransformation(
                            4,
                            borderColor
                        ),
                        RoundedCornersTransformation(16, 0)
                    )
                    .override(500, 500)
            )
            .into(imageView)
    }
}