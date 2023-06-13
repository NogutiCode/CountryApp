package com.example.countryapp

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {
    var scrollPosition: Int = 0
    private val repository = CountryRepository()
    val countryListLiveData: MutableLiveData<List<Country>> by lazy {
        MutableLiveData<List<Country>>()
    }
    init {
        Log.e("AAA", "VM Created")
    }
    override fun onCleared() {
        Log.e("AAA", "VM cleared")
        super.onCleared()
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
