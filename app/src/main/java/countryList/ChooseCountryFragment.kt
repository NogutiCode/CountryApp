package countryList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.countryapp.R
import dagger.hilt.android.AndroidEntryPoint
import kotlin.system.exitProcess


@AndroidEntryPoint
class ChooseCountryFragment : Fragment() {
    private val vm: ListViewModel by viewModels()
    private lateinit var navController: NavController
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView
    private lateinit var countryAdapter: CountryAdapter
    private lateinit var scrollBtn: ImageButton
    private lateinit var searchView: SearchView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_choose_country, container, false)
        progressBar = view.findViewById(R.id.progressBar)
        scrollBtn = view.findViewById(R.id.InvisibleBtn)
        searchView = view.findViewById(R.id.SearchView)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        recyclerView = view.findViewById(R.id.recyclerViewCountries)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        countryAdapter = CountryAdapter { position -> onItemClick(position) }
        recyclerView.adapter = countryAdapter

        vm.filteredCountryListLiveData.observe(viewLifecycleOwner) { filteredCountryList ->
            countryAdapter.updateData(filteredCountryList)
        }

        vm.countryListLiveData.observe(viewLifecycleOwner) { filteredCountryList ->
            countryAdapter.updateData(filteredCountryList)
            progressBar.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }

        vm.fetchCountryList()
        searchCountry()
        setupScrollListener()
        initValues(view)
    }

    private fun initValues(view: View) {
        val btnBack: ImageButton = view.findViewById(R.id.toEntrance)
        btnBack.setOnClickListener {
            activity?.finish()
            exitProcess(0)
        }
    }
    private fun searchCountry() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                vm.performSearch(query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                vm.performSearch(newText)
                if (newText.isEmpty()) {
                    countryAdapter.updateData(countryAdapter.getFullList())
                }
                return false
            }
        })
    }

    private fun setupScrollListener() {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastCompletelyVisibleItemPosition()
                val isAtBottom = lastVisibleItem == totalItemCount - 1

                if (isAtBottom) {
                    scrollBtn.rotation = 90f
                    scrollBtn.setOnClickListener {
                        recyclerView.scrollToPosition(0)
                    }
                } else {
                    scrollBtn.rotation = 270f
                    scrollBtn.setOnClickListener {
                        recyclerView.scrollToPosition(totalItemCount - 1)
                    }
                }
            }
        })
    }

    private fun onItemClick(position: Int) {
        val country = countryAdapter.getItem(position)
        country.name?.common?.let { countryName ->
            val bundle = Bundle().apply {
                putString("countryName", countryName)
            }
            navController.navigate(R.id.action_chooseCountry_to_countryInfo, bundle)
        }
    }
}