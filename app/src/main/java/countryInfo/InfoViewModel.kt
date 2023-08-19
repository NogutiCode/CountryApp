package countryInfo


import android.content.Context
import android.graphics.Color
import android.widget.ImageView
import androidx.lifecycle.LiveData
import countryRepository.CountryRepository
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.Country
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
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
class InfoViewModel @Inject constructor(
    private val repository: CountryRepository
) : ViewModel() {

    data class ProcessedCountryInfo(
        val name: String,
        val formattedCapital: String,
        val flag: String,
        val currency: String,
        val neighbours: String,
        val formattedPopulation: String
    )

    private val _loadingStateFlow = MutableStateFlow<Boolean>(false)
    val loadingStateFlow: StateFlow<Boolean> = _loadingStateFlow

    private val _processedCountryInfoStateFlow = MutableStateFlow<ProcessedCountryInfo?>(null)
    val processedCountryInfoStateFlow: StateFlow<ProcessedCountryInfo?> = _processedCountryInfoStateFlow

    fun fetchCountryInfo(countryName: String) {
        viewModelScope.launch {
            _loadingStateFlow.value = true
            repository.fetchCountryList()
                .map { countryList ->
                    val selectedCountry = countryList.find { it.name?.common == countryName }
                    selectedCountry?.let { country ->
                        processCountry(country, countryList, countryName)
                    }
                }
                .flowOn(Dispatchers.Default)
                .collect { processedInfo ->
                    _processedCountryInfoStateFlow.value = processedInfo
                    _loadingStateFlow.value = false
                }
        }
    }

    private fun formatNumberWithCommas(number: Int): String {
        val numberFormat = NumberFormat.getNumberInstance(Locale.US)
        return numberFormat.format(number)
    }

    private fun processCountry(
        country: Country,
        countryList: List<Country>,
        countryKey: String
    ): ProcessedCountryInfo {
        val nameCountry = country.name?.common.toString()
        val arrayNames = countryList.map { it.name?.common }.toTypedArray()
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

        val borders = country.borders?.toString()?.replace("[", "")?.replace("]", "")
        val withoutBracketsBorders = borders ?: ""
        val withoutComma = withoutBracketsBorders.split(", ")
        val arrayBorders = withoutComma.toTypedArray()

        val arrayFifa = countryList.map { it.cca3 }.toTypedArray()

        val builder = StringBuilder()
        if (nameCountry == countryKey) {
            for (i in arrayFifa.indices) {
                for (element in arrayBorders) {
                    if (element == arrayFifa[i]) {
                        if (builder.isNotEmpty()) {
                            builder.append(", ")
                        }
                        builder.append(arrayNames[i])
                    }
                }
            }
        }
        val listNeighbors = builder.toString()
        return ProcessedCountryInfo(
            nameCountry,
            formattedCapital,
            flag,
            "$formattedCurrencySmallName $formattedCurrencyFullName",
            listNeighbors,
            formattedPopulation
        )
    }
    fun loadCountryImage(context: Context, imageUrl: String, imageView: ImageView) {
        Glide.with(context)
            .load(imageUrl)
            .apply(
                RequestOptions()
                    .transform(
                        CropCircleWithBorderTransformation(
                            4,
                            Color.parseColor("#4942E4")
                        ),
                        RoundedCornersTransformation(16, 0)
                    )
                    .override(500, 500)
            )
            .into(imageView)
    }
}