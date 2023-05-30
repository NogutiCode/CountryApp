package com.example.countryapp

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.*
import java.io.IOException

data class Country(
    val name: Name?,
    val capital: Any?,
    val flags: Flags?
)

data class Flags(
    val png: String?,
    val svg: String?
)

data class Name(
    val common: String?,
    val official: String?,
    val nativeName: Map<String, Any>?
)

class ChooseCountry : Fragment() {

    private val client = OkHttpClient()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        makeApiRequest()

        return inflater.inflate(R.layout.fragment_choose_country, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = Navigation.findNavController(view)

        val btn: ImageButton = view.findViewById(R.id.toCountryInfo)
        btn.setOnClickListener {
            navController.navigate(R.id.action_chooseCountry_to_countryInfo)
        }
    }

    private fun makeApiRequest() {
        val request = Request.Builder()
            .url("https://restcountries.com/v3.1/all")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
                    val responseBody = response.body?.string()
                    if (responseBody != null) {
                        val moshi = Moshi.Builder()
                            .addLast(KotlinJsonAdapterFactory())
                            .build()
                        val listType = Types.newParameterizedType(List::class.java, Country::class.java)
                        val adapter: JsonAdapter<List<Country>> = moshi.adapter(listType)
                        val countryList: List<Country>? = adapter.fromJson(responseBody)
                        countryList?.let { list ->
                            for (country in list) {
                                val name = country.name?.common
                                val capital = country.capital?.toString()?.replace("[", "")?.replace("]", "")
                                val flag = country.flags?.svg
                                val formattedCapital = capital ?: ""
                                println("Name: $name ::: $formattedCapital ::: $flag")
                            }
                        }
                    }
                }
            }
        })
    }
    fun buildDesign(){

    }
}

