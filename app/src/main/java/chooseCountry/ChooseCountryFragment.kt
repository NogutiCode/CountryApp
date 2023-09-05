package chooseCountry

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.Country
import com.example.countryapp.R
import dagger.hilt.android.AndroidEntryPoint
import entrace.MainActivity
import kotlinx.coroutines.launch
import kotlin.system.exitProcess


@AndroidEntryPoint
class ChooseCountryFragment : Fragment() {
    private val chooseCountryViewModel: ChooseCountryViewModel by viewModels()
    private lateinit var navController: NavController
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView
    private lateinit var countryAdapter: CountryAdapter
    private lateinit var scrollBtn: ImageButton
    private lateinit var searchView: EditText
    private var isFirstLoad = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_choose_country, container, false)
        progressBar = view.findViewById(R.id.progressBar)
        scrollBtn = view.findViewById(R.id.InvisibleBtn)
        recyclerView = view.findViewById(R.id.recyclerViewCountries)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        searchView = view.findViewById(R.id.searchview)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        countryAdapter = CountryAdapter { position -> onItemClick(position) }
        recyclerView.adapter = countryAdapter


        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            val currentDestination = navController.currentDestination

            if (currentDestination?.id == R.id.chooseCountry) {
                activity?.finish()
            } else {
                val intent = Intent(requireContext(), MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            chooseCountryViewModel.loadingStateFlow.collect { isLoading ->
                progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }


        viewLifecycleOwner.lifecycleScope.launch {
            chooseCountryViewModel.combinedCountryListFlow.collect { combinedList ->
                updateAdapterData(combinedList)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            chooseCountryViewModel.recyclerViewVisibility.collect { visibility ->
                recyclerView.visibility = visibility
            }
        }
        if (isFirstLoad) {
            chooseCountryViewModel.fetchCountryList()
            isFirstLoad = false
        }
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


    private fun updateAdapterData(data: List<Country>) {
        countryAdapter.updateData(data)
        if (data.isNotEmpty()) {
            progressBar.visibility = View.GONE
        }
    }

    private fun searchCountry() {
        val clearButton = view?.findViewById<ImageButton>(R.id.removeText)
        clearButton?.setOnClickListener {
            searchView.text.clear()
        }

        searchView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()) {
                    clearButton?.visibility = View.GONE
                } else {
                    clearButton?.visibility = View.VISIBLE
                }
                chooseCountryViewModel.performSearch(s.toString())
                if (s.isNullOrEmpty()) {
                    updateAdapterData(countryAdapter.getFullList())
                }
            }

            override fun afterTextChanged(s: Editable?) {}
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
                        if (lastVisibleItem > posForScroll)
                            recyclerView.scrollToPosition(posForScroll)
                        recyclerView.smoothScrollToPosition(0)
                    }
                } else {
                    scrollBtn.rotation = 90f
                    scrollBtn.setOnClickListener {
                        if (lastVisibleItem < posForScroll)
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

