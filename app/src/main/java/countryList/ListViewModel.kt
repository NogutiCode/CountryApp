package countryList

import countryRepository.CountryRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.Country
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(
    private val repository: CountryRepository
) : ViewModel() {

    private val _countryListFlow = MutableStateFlow<List<Country>>(emptyList()) // Convert Mutable data to livedata (flow)
    val countryListFlow: StateFlow<List<Country>>  // Help users to read ur code + foolproof (flow)
        get() = _countryListFlow

    private val _filteredCountryListFlow = MutableStateFlow<List<Country>>(emptyList()) // Convert Mutable data to livedata (flow)
    val filteredCountryListFlow: StateFlow<List<Country>>  // Help users to read ur code + foolproof (flow)
        get() = _filteredCountryListFlow

    init {
        fetchCountryList()
    }

    fun fetchCountryList() { // fetch the list of countries (api)
        viewModelScope.launch {
            repository.fetchCountryList()
                .flowOn(Dispatchers.IO)
                .collect { countryList ->
                    _countryListFlow.value = countryList
                }
        }
    }

    fun performSearch(query: String) { // search parse function
        val lowercaseQuery = query.lowercase(Locale.getDefault())
        val queryWords = lowercaseQuery.split(" ")

        viewModelScope.launch {
            val filteredList = if (lowercaseQuery.isBlank()) {
                _countryListFlow.value
            } else {
                _countryListFlow.value.filter { country ->
                    val name = country.name?.common?.lowercase(Locale.getDefault())
                    val capital = country.capital?.toString()?.replace("[", "")?.replace("]", "")?.lowercase(
                        Locale.getDefault()
                    )

                    queryWords.all { word ->
                        name?.contains(word) == true || capital?.contains(word) == true
                    }
                }
            }

            _filteredCountryListFlow.value = filteredList
        }
    }
}
