package com.example.countryapp

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.*

class CountryRepository {
    private val client = OkHttpClient()
    fun fetchCountryList(): List<Country>? {
        val request = Request.Builder()
            .url("https://restcountries.com/v3.1/all")
            .build()

        val response = client.newCall(request).execute()

        return if (response.isSuccessful) {
            val responseBody = response.body.string()
            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()
            val listType = Types.newParameterizedType(List::class.java, Country::class.java)
            val adapter: JsonAdapter<List<Country>> = moshi.adapter(listType)
            adapter.fromJson(responseBody)
        } else {
            null
        }
    }
}