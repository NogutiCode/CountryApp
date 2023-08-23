package countryList

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.countryapp.R
import dagger.hilt.android.AndroidEntryPoint
import kotlin.system.exitProcess

@Suppress("DEPRECATION")
@AndroidEntryPoint
class ChooseCountryFragment : Fragment() {
    private val vm: ListViewModel by viewModels()
    private lateinit var navController: NavController
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView
    private lateinit var countryAdapter: CountryAdapter
    private lateinit var scrollBtn: ImageButton
    private lateinit var searchView: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_choose_country, container, false)
        progressBar = view.findViewById(R.id.progressBar)
        scrollBtn = view.findViewById(R.id.InvisibleBtn)
        //searchView = view.findViewById(R.id.SearchView)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        searchView = view.findViewById(R.id.searchview)
        recyclerView = view.findViewById(R.id.recyclerViewCountries)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        countryAdapter = CountryAdapter { position -> onItemClick(position) }
        recyclerView.adapter = countryAdapter


        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            vm.filteredCountryListFlow.collect { filteredCountryList ->
                countryAdapter.updateData(filteredCountryList)
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            vm.countryListFlow.collect { filteredCountryList ->
                countryAdapter.updateData(filteredCountryList)
            }
        }
        countryAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                if (countryAdapter.itemCount > 0) {
                    progressBar.visibility = View.GONE
                }
            }
        })
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
        val clearButton = view?.findViewById<ImageButton>(R.id.removeText)
        clearButton?.setOnClickListener {
            searchView.text.clear()
        }

        searchView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()) {
                    clearButton?.visibility = View.GONE
                } else {
                    clearButton?.visibility = View.VISIBLE
                }
                vm.performSearch(s.toString())
                if (s.isNullOrEmpty()) {
                    countryAdapter.updateData(countryAdapter.getFullList())
                }
            }

            override fun afterTextChanged(s: Editable?) {
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
                val posForScroll = (totalItemCount * 0.1).toInt()
                if (isAtBottom) {
                    scrollBtn.rotation = 270f
                    scrollBtn.setOnClickListener {
                        if(lastVisibleItem > posForScroll)
                            recyclerView.scrollToPosition(posForScroll)
                        recyclerView.smoothScrollToPosition(0)
                    }
                } else {
                    scrollBtn.rotation = 90f
                    scrollBtn.setOnClickListener {
                        if(lastVisibleItem < posForScroll)
                            recyclerView.scrollToPosition(totalItemCount - posForScroll)
                        recyclerView.smoothScrollToPosition(totalItemCount)
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
