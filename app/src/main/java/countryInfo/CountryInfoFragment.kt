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
import kotlinx.coroutines.launch
import okhttp3.*



@AndroidEntryPoint
class CountryInfoFragment : Fragment() {

    private val countryInfoViewModel: CountryInfoViewModel by viewModels()
    private var countryKey: String = ""
    private lateinit var progressBar: ProgressBar
    private lateinit var layout: LinearLayout
    private lateinit var setCountry: TextView
    private lateinit var navController: NavController
    private lateinit var call: Call

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

        viewLifecycleOwner.lifecycleScope.launch {
            countryInfoViewModel.loadingStateFlow.collect { isLoading ->
                progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                layout.visibility = if (isLoading) View.GONE else View.VISIBLE
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            countryInfoViewModel.processedCountryInfoStateFlow.collect { processedInfo: CountryInfoViewModel.ProcessedCountryInfo? ->
                processedInfo?.let {
                    buildDesign(it)
                    setCountry.visibility = View.VISIBLE
                }
            }
        }
        countryInfoViewModel.fetchCountryInfo(countryKey)
        initButtonsAndValues()
    }

    private fun initButtonsAndValues() {
        val buttonBack = view?.findViewById<ImageButton>(R.id.toChoose)
        buttonBack?.setOnClickListener {
            if (::call.isInitialized) {
                call.cancel()
            }
            navController.navigateUp()
        }
    }

    private fun buildDesign(processedInfo: CountryInfoViewModel.ProcessedCountryInfo) {
        val countryNameText = view?.findViewById<TextView>(R.id.setCountry)
        val countryNameText1 = view?.findViewById<TextView>(R.id.setCountryText)
        val countryPhoto = view?.findViewById<ImageView>(R.id.countryImage)
        val capitalTexts = view?.findViewById<TextView>(R.id.setCapitalText)
        val currencyText = view?.findViewById<TextView>(R.id.setCurrency)
        val neighborsText = view?.findViewById<TextView>(R.id.setNeighbours)
        val populationText = view?.findViewById<TextView>(R.id.setPopulation)

        countryNameText?.text = processedInfo.name
        countryNameText1?.text = processedInfo.name
        capitalTexts?.text = processedInfo.formattedCapital
        currencyText?.text = processedInfo.currency
        populationText?.text = processedInfo.formattedPopulation
        neighborsText?.text = processedInfo.neighbours


        val noNeighborsText = "No have neighbours"
        val noCurrency = "No own currency"
        val noCapital = "No own capital"

        if (processedInfo.neighbours.isEmpty()) {
            neighborsText?.text = noNeighborsText
        }
        if (processedInfo.currency.trim().isEmpty()) {
            currencyText?.text = noCurrency
        }
        if (processedInfo.formattedCapital.isEmpty()) {
            capitalTexts?.text = noCapital
        }

        if (countryPhoto != null) {
            countryInfoViewModel.loadCountryImage(requireContext(), processedInfo.flag, countryPhoto)
        }
    }
}
