package com.example.countryapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.navigation.Navigation
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.*
import java.io.IOException
import java.text.NumberFormat
import java.util.*


class CountryInfo : Fragment() {
    private var selectedButtonId: Int = 0
    private lateinit var progressBar: ProgressBar
    private val client = OkHttpClient()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_country_info, container, false)
        progressBar = view.findViewById(R.id.progressBar)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = Navigation.findNavController(view)
        val buttonBack = view.findViewById<Button>(R.id.button)
        buttonBack.setOnClickListener {
            navController.navigate(R.id.action_countryInfo_to_chooseCountry)
        }

        arguments?.let { bundle ->
            selectedButtonId = bundle.getInt("buttonId", 0)
        }
        makeApiRequest()

        //println(selectedButtonId)

    }
    fun formatNumberWithCommas(number: Int): String {
        val numberFormat = NumberFormat.getNumberInstance(Locale.US)
        return numberFormat.format(number)
    }
    private fun makeApiRequest() {
        progressBar.visibility = View.VISIBLE
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
                        val listType =
                            Types.newParameterizedType(List::class.java, Country::class.java)
                        val adapter: JsonAdapter<List<Country>> = moshi.adapter(listType)
                        val countryList: List<Country>? = adapter.fromJson(responseBody)
                        countryList?.let { list ->
                            for ((index, country) in list.withIndex()) {
                                if (index == selectedButtonId) {
                                    val name = country.name?.common
                                    val capital = country.capital?.toString()?.replace("[", "")?.replace("]", "")
                                    val flag = country.flags?.png

                                    val currencyFindFullName = country.currencies?.values?.firstOrNull()
                                    val currencyFullName: String? = currencyFindFullName?.name
                                    val currencyFindSmallName = country.currencies?.toList()?.firstOrNull()
                                    val currencySmallName: String? = currencyFindSmallName?.first

                                    val population = country.population.toString()
                                    val formattedPopulation = formatNumberWithCommas(population.toInt())
                                    //val gini = country.gini

                                    val formattedCapital = capital ?: ""

                                    //println("Element Index: $index")
                                    println("Name: $name ::: $formattedCapital ::: $flag ::: $currencySmallName $currencyFullName  ::: $formattedPopulation ::: ")
                                }
                                activity?.runOnUiThread {
                                   progressBar.visibility = View.GONE
                                }

                            }
                        }
                    }
                }
            }
        })
    }
}