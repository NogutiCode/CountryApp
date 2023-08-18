package countryList

import countryRepository.CountryRepository
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.Country
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(
        private val repository: CountryRepository
    ) : ViewModel() {

    private val _countryListLiveData = MutableLiveData<List<Country>>()
    val countryListLiveData: LiveData<List<Country>>
        get() = _countryListLiveData

    private val _filteredCountryListLiveData = MutableLiveData<List<Country>>()
    val filteredCountryListLiveData: LiveData<List<Country>>
        get() = _filteredCountryListLiveData


    fun fetchCountryList() {
        viewModelScope.launch {
            repository.fetchCountryList()
                .flowOn(Dispatchers.IO)
                .collect { countryList ->
                    _countryListLiveData.value = countryList
                }
        }
    }

    fun performSearch(query: String) {
        val lowercaseQuery = query.lowercase(Locale.getDefault())
        val queryWords = lowercaseQuery.split(" ")

        val filteredList = if (lowercaseQuery.isBlank()) {
            _countryListLiveData.value
        } else {
            _countryListLiveData.value?.filter { country ->
                val name = country.name?.common?.lowercase(Locale.getDefault())
                val capital = country.capital?.toString()?.replace("[", "")?.replace("]", "")?.lowercase(
                    Locale.getDefault()
                )

                queryWords.all { word ->
                    name?.contains(word) == true || capital?.contains(word) == true
                }
            }
        }

        _filteredCountryListLiveData.value = filteredList!!
    }
}

