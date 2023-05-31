package com.example.countryapp

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import jp.wasabeef.glide.transformations.CropCircleWithBorderTransformation
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import okhttp3.*
import java.io.IOException
import java.text.NumberFormat
import java.util.*


class CountryInfo : Fragment() {

    private var selectedButtonId: Int = 0
    private lateinit var progressBar: ProgressBar
    private lateinit var layout: LinearLayout
    private lateinit var setCountry: TextView
    private lateinit var navController: NavController
    private val client = OkHttpClient()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_country_info, container, false)
        layout = view.findViewById(R.id.listOfCountries)
        setCountry = view.findViewById(R.id.setCountry)
        progressBar = view.findViewById(R.id.progressBar)
        return view

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        initButtonsAndValues()
        makeApiRequest()
    }

    private fun initButtonsAndValues() {

        val buttonBack = view?.findViewById<ImageButton>(R.id.toChoose)
        buttonBack?.setOnClickListener {
            navController.navigate(R.id.action_countryInfo_to_chooseCountry)
        }

        arguments?.let { bundle ->
            selectedButtonId = bundle.getInt("buttonId", 0)
        }
    }

    private fun formatNumberWithCommas(number: Int): String {
        val numberFormat = NumberFormat.getNumberInstance(Locale.US)
        return numberFormat.format(number)
    }

    private fun makeApiRequest() {

        progressBar.visibility = View.VISIBLE
        layout.visibility = View.GONE
        setCountry.visibility = View.GONE

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

                                val nameCountry = country.name?.common.toString()
                                val arrayNames = list.map { it.name?.common }.toTypedArray()
                                val capital = country.capital?.toString()?.replace("[", "")?.replace("]", "")
                                val formattedCapital = capital ?: ""
                                val flag = country.flags?.png.toString()

                                val currencyFindFullName = country.currencies?.values?.firstOrNull()
                                val currencyFullName: String? = currencyFindFullName?.name
                                val currencyFindSmallName = country.currencies?.toList()?.firstOrNull()
                                val currencySmallName: String? = currencyFindSmallName?.first
                                val formattedCurrencySmallName = currencySmallName ?: ""
                                val formattedCurrencyFullName = currencyFullName ?: ""

                                val population = country.population.toString()
                                val formattedPopulation = formatNumberWithCommas(population.toInt())

                                val borders = country.borders?.toString()?.replace("[", "")?.replace("]", "")
                                val withoutBracketsBorders = borders ?: ""
                                val withoutComa = withoutBracketsBorders.split(", ")
                                val arrayBorders = withoutComa.toTypedArray()

                                val arrayFifa = list.map { it.cca3 }.toTypedArray()

                                val builder = StringBuilder()
                                if (index == selectedButtonId) {
                                    for (i in arrayFifa.indices) {
                                        for (element in arrayBorders) {
                                            if (element == arrayFifa[i]) {
                                                if (builder.isNotEmpty()) {
                                                    builder.append(", ")
                                                }
                                                builder.append(arrayNames[i])
                                            }
                                        }
                                    }
                                    val listNeighbors = builder.toString()
                                    println("Name:; $listNeighbors $nameCountry ::: $formattedCapital ::: $flag ::: $currencySmallName $currencyFullName  ::: $formattedPopulation ::: ")
                                    activity?.runOnUiThread {

                                        buildDesign(
                                            nameCountry,
                                            flag,
                                            formattedCapital,
                                            "$formattedCurrencySmallName $formattedCurrencyFullName",
                                            listNeighbors,
                                            formattedPopulation
                                        )
                                        Handler(Looper.getMainLooper()).postDelayed({
                                            progressBar.visibility = View.GONE
                                            layout.visibility = View.VISIBLE
                                            setCountry.visibility = View.VISIBLE

                                        }, 50)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        })
    }

    private fun checkCountryInfo(CheckType: String, TextCheck: String, TextVar: TextView, IfNoSomething: String)
    {
        if (TextCheck == CheckType) {
            TextVar.text = IfNoSomething
        } else {
            TextVar.text = TextCheck
        }
    }

    private fun buildDesign(Country: String, CountryPhoto: String, capitalText: String, Currency: String, Neighbours: String, Population: String)
    {
        val countryNameText = view?.findViewById<TextView>(R.id.setCountry)
        val countryNameText1 = view?.findViewById<TextView>(R.id.setCountryText)
        val countryPhoto = view?.findViewById<ImageView>(R.id.countryImage)
        val capitalTexts = view?.findViewById<TextView>(R.id.setCapitalText)
        val currencyText = view?.findViewById<TextView>(R.id.setCurrency)
        val neighborsText = view?.findViewById<TextView>(R.id.setNeighbours)
        val populationText = view?.findViewById<TextView>(R.id.setPopulation)

        val noNeighborsText = "No have neighbours"
        val noCurrency = "No have own currency"
        val noCapital = "No have own capital"

        countryNameText?.text = Country
        countryNameText1?.text = Country
        capitalTexts?.text = capitalText
        currencyText?.text = Currency
        populationText?.text = Population

        checkCountryInfo("", Neighbours, neighborsText!!, noNeighborsText)
        checkCountryInfo(" ", Currency, currencyText!!, noCurrency)
        checkCountryInfo("", capitalText, capitalTexts!!, noCapital)

        if (countryPhoto != null) {
            Glide.with(requireContext())
                .load(CountryPhoto)
                .apply(
                    RequestOptions()
                        .transform(
                            CropCircleWithBorderTransformation(
                                4,
                                Color.GRAY
                            ), // Установите желаемую ширину и цвет обводки
                            RoundedCornersTransformation(16, 0)
                        )
                        .override(500, 500)
                )
                .into(countryPhoto)
        }
    }

}
