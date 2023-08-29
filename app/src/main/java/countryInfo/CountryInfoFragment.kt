package countryInfo

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.countryapp.R
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.*


@Suppress("DEPRECATION")
@AndroidEntryPoint
class CountryInfoFragment : Fragment() {

    private val vm: InfoViewModel by viewModels() // init vm
    private var countryKey: String = ""
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
        countryKey = arguments?.getString("countryName").toString()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val layout = view.findViewById<LinearLayout>(R.id.listOfCountries)
        val setCountry = view.findViewById<TextView>(R.id.setCountry)

        val countryKey = arguments?.getString("countryName").toString()

        lifecycleScope.launchWhenStarted { // show info  + enable disable progress bar & layout
            vm.loadingStateFlow.collect { isLoading ->
                progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                layout.visibility = if (isLoading) View.GONE else View.VISIBLE
            }
        }

        lifecycleScope.launchWhenStarted { // show info abt country
            vm.processedCountryInfoStateFlow.collect { processedInfo: InfoViewModel.ProcessedCountryInfo? ->
                processedInfo?.let {
                    buildDesign(it)
                    setCountry.visibility = View.VISIBLE
                }
            }
        }

        vm.fetchCountryInfo(countryKey)
        initButtonsAndValues()
    }

    private fun initButtonsAndValues() {
        val buttonBack = view?.findViewById<ImageButton>(R.id.toChoose)
        buttonBack?.setOnClickListener {
            if (::call.isInitialized) {
                call.cancel()
            }
            stopFunction = true
            navController.navigateUp()
        }
    }

    private fun checkCountryInfo( // simple function to set text if the country does not have something
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

    private fun buildDesign(processedInfo: InfoViewModel.ProcessedCountryInfo) {
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

        countryNameText?.text = processedInfo.name
        countryNameText1?.text = processedInfo.name
        capitalTexts?.text = processedInfo.formattedCapital
        currencyText?.text = processedInfo.currency
        populationText?.text = processedInfo.formattedPopulation
        if (stopFunction) {
            return
        }
        checkCountryInfo("", processedInfo.neighbours, neighborsText!!, noNeighborsText)
        checkCountryInfo(" ", processedInfo.currency, currencyText!!, noCurrency)
        checkCountryInfo("", processedInfo.formattedCapital, capitalTexts!!, noCapital)

        if (countryPhoto != null) {
            vm.loadCountryImage(requireContext(), processedInfo.flag, countryPhoto)
        }
    }
}
