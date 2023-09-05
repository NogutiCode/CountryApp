package chooseCountry

import android.view.View
import countryRepository.CountryRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.Country
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ChooseCountryViewModel @Inject constructor(
    private val repository: CountryRepository

) : ViewModel() {

    private val countryListFlow = MutableStateFlow<List<Country>>(emptyList())
    private val filteredCountryListFlow = MutableStateFlow<List<Country>>(emptyList())

    private val _loadingStateFlow = MutableStateFlow(false)
    val loadingStateFlow: StateFlow<Boolean> = _loadingStateFlow

    private val _recyclerViewVisibility = MutableStateFlow(View.VISIBLE)
    val recyclerViewVisibility: StateFlow<Int> = _recyclerViewVisibility

    private var isFirstLoad = true

    val combinedCountryListFlow: Flow<List<Country>> = combine(
        countryListFlow,
        filteredCountryListFlow
    ) { originalList, filteredList ->
        filteredList.ifEmpty {
            originalList
        }
    }


    fun fetchCountryList() {
        if (!isFirstLoad) {
            return
        }
        viewModelScope.launch {
            _loadingStateFlow.value = true
            repository.fetchCountryList()
                .flowOn(Dispatchers.IO)
                .collect { countryList ->
                    countryListFlow.value = countryList
                    _loadingStateFlow.value = false
                    isFirstLoad = false
                }
            }
        }

    fun performSearch(query: String) {
        val lowercaseQuery = query.lowercase(Locale.getDefault())
        val queryWords = lowercaseQuery.split(" ")

        viewModelScope.launch {
            val filteredList = if (lowercaseQuery.isBlank()) {
                countryListFlow.value
            } else {
                countryListFlow.value.filter { country ->
                    val name = country.name?.common?.lowercase(Locale.getDefault())
                    val capital = country.capital?.toString()?.filter { it.isLetter() }?.lowercase(
                        Locale.getDefault()
                    )

                    queryWords.all { word ->
                        name?.contains(word) == true || capital?.contains(word) == true
                    }
                }
            }

            filteredCountryListFlow.value = filteredList
            _recyclerViewVisibility.value = if (filteredList.isEmpty()) View.GONE else View.VISIBLE
        }
    }
}
