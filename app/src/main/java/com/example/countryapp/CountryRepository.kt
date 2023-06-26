package com.example.countryapp

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.*

class CountryRepository {
    private val client = OkHttpClient()
    private val maxRetries = 3

    fun fetchCountryList(): List<Country>? {
        var retryCount = 0
        var countryList: List<Country>? = null

        while (retryCount < maxRetries) {
            val request = Request.Builder()
                .url("https://restcountries.com/v3.1/all")
                .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val responseBody = response.body.string()
                val moshi = Moshi.Builder()
                    .addLast(KotlinJsonAdapterFactory())
                    .build()
                val listType = Types.newParameterizedType(List::class.java, Country::class.java)
                val adapter: JsonAdapter<List<Country>> = moshi.adapter(listType)
                countryList = adapter.fromJson(responseBody)
                break
            } else {
                retryCount++
                Thread.sleep(1000)
            }
        }

        return countryList
    }
}
