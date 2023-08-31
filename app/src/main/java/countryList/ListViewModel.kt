package countryList

import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import countryRepository.CountryRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.Country
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(
    private val repository: CountryRepository
) : ViewModel() {

    private val countryListFlow = MutableStateFlow<List<Country>>(emptyList())
    private val filteredCountryListFlow = MutableStateFlow<List<Country>>(emptyList())
    val combinedCountryListFlow: Flow<List<Country>> = combine(
        countryListFlow,
        filteredCountryListFlow
    ) { originalList, filteredList ->
        filteredList.ifEmpty {
            originalList
        }
    }

    private val _recyclerViewVisibility = MutableLiveData(View.VISIBLE)
    val recyclerViewVisibility: LiveData<Int> = _recyclerViewVisibility

    private val countries: MutableList<Array<String>> = mutableListOf()
    fun fetchCountryList() {
        viewModelScope.launch {
            repository.fetchCountryList()
                .flowOn(Dispatchers.IO)
                .collect { countryList ->
                    countryListFlow.value = countryList
                    countries.clear()
                    for (country in countryListFlow.value) {
                        val cca3 = country.cca3.toString()
                        val name = country.name?.common ?: ""
                        countries.add(arrayOf(cca3, name))
                    }
                    for (row in countries) {
                        Log.d("Array", "${row[0]} - ${row[1]}")
                    }
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
