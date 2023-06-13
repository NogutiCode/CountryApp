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
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import dagger.hilt.android.AndroidEntryPoint
import jp.wasabeef.glide.transformations.CropCircleWithBorderTransformation
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import okhttp3.*
import java.text.NumberFormat
import java.util.*



@AndroidEntryPoint
class CountryInfoFragment: Fragment() {

    private val vm: MainViewModel by viewModels()
    private var selectedButtonId: Int = 0
    private lateinit var progressBar: ProgressBar
    private lateinit var layout: LinearLayout
    private lateinit var setCountry: TextView
    private lateinit var navController: NavController
    private lateinit var call: Call
    private var stopFunction = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_country_info, container, false)
        layout = view.findViewById(R.id.listOfCountries)
        setCountry = view.findViewById(R.id.setCountry)
        progressBar = view.findViewById(R.id.progressBar)
        ViewModelProvider(this)[MainViewModel::class.java]
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        initButtonsAndValues()
        setupCountryListObserver()
        fetchCountryList()


    }

    private fun initButtonsAndValues() {
        val buttonBack = view?.findViewById<ImageButton>(R.id.toChoose)
        buttonBack?.setOnClickListener {
            if (::call.isInitialized) {
                call.cancel()
            }
            stopFunction = true
            //navController.popBackStack(R.id.chooseCountry, false)
            navController.navigateUp()
            //navController.navigate(R.id.action_countryInfo_to_chooseCountry)
        }
        arguments?.let { bundle ->
            selectedButtonId = bundle.getInt("buttonId", 0)
        }

    }


    private fun formatNumberWithCommas(number: Int): String {
        val numberFormat = NumberFormat.getNumberInstance(Locale.US)
        return numberFormat.format(number)
    }

    private fun setupCountryListObserver() {
        vm.countryListLiveData.observe(viewLifecycleOwner) { countryList ->
            processCountryList(countryList)
            progressBar.visibility = View.GONE
            layout.visibility = View.VISIBLE
        }
    }

    private fun fetchCountryList() {
        progressBar.visibility = View.VISIBLE
        layout.visibility = View.GONE
        vm.fetchCountryList()
    }


    private fun processCountryList(countryList: List<Country>) {
        for ((index, country) in countryList.withIndex()) {
            val nameCountry = country.name?.common.toString()
            val arrayNames = countryList.map { it.name?.common }.toTypedArray()
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
            val withoutComma = withoutBracketsBorders.split(", ")
            val arrayBorders = withoutComma.toTypedArray()

            val arrayFifa = countryList.map { it.cca3 }.toTypedArray()

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


    private fun checkCountryInfo(
        CheckType: String,
        TextCheck: String,
        TextVar: TextView,
        IfNoSomething: String
    ) {
        if (TextCheck == CheckType) {
            TextVar.text = IfNoSomething
        } else {
            TextVar.text = TextCheck
        }
    }

    private fun buildDesign(
        Country: String,
        CountryPhoto: String,
        capitalText: String,
        Currency: String,
        Neighbours: String,
        Population: String
    ) {
        if (stopFunction) {
            return
        }
        val countryNameText = view?.findViewById<TextView>(R.id.setCountry)
        val countryNameText1 = view?.findViewById<TextView>(R.id.setCountryText)
        val countryPhoto = view?.findViewById<ImageView>(R.id.countryImage)
        val capitalTexts = view?.findViewById<TextView>(R.id.setCapitalText)
        val currencyText = view?.findViewById<TextView>(R.id.setCurrency)
        val neighborsText = view?.findViewById<TextView>(R.id.setNeighbours)
        val populationText = view?.findViewById<TextView>(R.id.setPopulation)

        val noNeighborsText = "No have neighbours"
        val noCurrency = "No own currency"
        val noCapital = "No own capital"

        countryNameText?.text = Country
        countryNameText1?.text = Country
        capitalTexts?.text = capitalText
        currencyText?.text = Currency
        populationText?.text = Population
        if (stopFunction) {
            return
        }
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
                            ),
                            RoundedCornersTransformation(16, 0)
                        )
                        .override(500, 500)
                )
                .into(countryPhoto)
        }
    }

}
