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

    private val _countryListFlow = MutableStateFlow<List<Country>>(emptyList())
    val countryListFlow: StateFlow<List<Country>>
        get() = _countryListFlow

    private val _filteredCountryListFlow = MutableStateFlow<List<Country>>(emptyList())
    val filteredCountryListFlow: StateFlow<List<Country>>
        get() = _filteredCountryListFlow

    init {
        fetchCountryList()
    }

    fun fetchCountryList() {
        viewModelScope.launch {
            repository.fetchCountryList()
                .flowOn(Dispatchers.IO)
                .collect { countryList ->
                    _countryListFlow.value = countryList
                }
        }
    }

    fun performSearch(query: String) {
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
