package countryInfo


import CountryRepository.CountryRepository
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.Country
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class InfoViewModel @Inject constructor() : ViewModel() {
    private val repository = CountryRepository()


    val countryListLiveData: MutableLiveData<List<Country>> by lazy {
        MutableLiveData<List<Country>>()
    }

    fun fetchCountryList() {
        viewModelScope.launch {
            val countryList = withContext(Dispatchers.IO) {
                repository.fetchCountryList()
            }
            countryListLiveData.value = countryList
        }
    }
}
