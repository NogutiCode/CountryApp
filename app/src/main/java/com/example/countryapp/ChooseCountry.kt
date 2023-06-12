package com.example.countryapp

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.size
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import okhttp3.*
import jp.wasabeef.glide.transformations.CropCircleWithBorderTransformation
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import kotlin.system.exitProcess


class ChooseCountryFragment : Fragment() {
    private lateinit var scrollView: ScrollView
    private lateinit var progressBar: ProgressBar
    private lateinit var navController: NavController
    private lateinit var layout: LinearLayout
    private lateinit var call: Call
    private var stopFunction = false
    private var totalCountryCount = 0
    private lateinit var vm: MainViewModel
    private val buttons = mutableListOf<Button>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_choose_country, container, false)
        progressBar = view.findViewById(R.id.progressBar)
        layout = view.findViewById(R.id.listOfCountries)
        scrollView = view.findViewById(R.id.CountryScroll)
        vm = ViewModelProvider(this)[MainViewModel::class.java]
        return view

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)
        initValues(view)
        setupCountryListObserver()
        fetchCountryList()


    }

    private fun initValues(view: View) {
        val btnBack: ImageButton = view.findViewById(R.id.toEntrance)
        btnBack.setOnClickListener {
            if (::call.isInitialized) {
                call.cancel()
            }
            stopFunction = true
            activity?.finish()
            exitProcess(0)

        }
    }

    override fun onPause() {
        super.onPause()
        saveScrollPosition()
    }

    private fun setupCountryListObserver() {
        progressBar.visibility = View.VISIBLE
        layout.visibility = View.GONE

        vm.countryListLiveData.observe(viewLifecycleOwner) { countryList ->
            totalCountryCount = countryList.size
            if (buttons.isEmpty()) {
                for ((index, country) in countryList.withIndex()) {
                    if (stopFunction) {
                        return@observe
                    }

                    val name = country.name?.common
                    val capital = country.capital?.toString()?.replace("[", "")?.replace("]", "")
                    val flag = country.flags?.png
                    val formattedCapital = capital ?: ""

                    buildButton("$name \n$formattedCapital", flag, index)
                }
            }

        }
    }
    private fun fetchCountryList() {
        progressBar.visibility = View.VISIBLE
        layout.visibility = View.GONE
        buttons.clear()
        vm.fetchCountryList()
    }


    private fun setButtonWithImage(button: Button, text: String, image: Drawable?) {
        button.text = text
        button.setCompoundDrawablesWithIntrinsicBounds(image, null, null, null)
    }

    private fun buildButton(countryText: String, imageLink: String?, buttonId: Int) {
        val button = Button(requireContext())
        val imageView = ImageView(requireContext())
        val imagePadding = 75

        button.id = buttonId
        button.text = countryText
        button.isAllCaps = false
        button.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
        button.compoundDrawablePadding = imagePadding

        if (stopFunction) {
            return
        }

        Glide.with(this)
            .load(imageLink)
            .apply(
                RequestOptions()
                    .transform(
                        CropCircleWithBorderTransformation(4, Color.GRAY),
                        RoundedCornersTransformation(16, 0)
                    )
                    .override(200, 200)
            )
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(object : CustomTarget<Drawable>() {
                override fun onResourceReady(
                    resource: Drawable,
                    transition: Transition<in Drawable>?
                ) {
                    if (stopFunction) {
                        return
                    }

                    imageView.setImageDrawable(resource)
                    imageView.layoutParams = ViewGroup.LayoutParams(300, 300)

                    button.layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 300
                    )

                    requireView().findViewById<LinearLayout>(R.id.listOfCountries)
                    setButtonWithImage(button, countryText, imageView.drawable)
                    button.background = ColorDrawable(Color.TRANSPARENT)

                    buttons.add(button)
                    layout.addView(button)

                    button.setOnClickListener {
                        val bundle = Bundle().apply { putInt("buttonId", buttonId) }
                        navController.navigate(R.id.action_chooseCountry_to_countryInfo, bundle)
                    }

                    if(totalCountryCount == layout.size){
                        activity?.runOnUiThread(){
                            restoreScrollPosition()//scroll который запоминает где ты был в прошлый раз
                            progressBar.visibility = View.GONE
                            layout.visibility = View.VISIBLE
                        }
                    }

                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    }


    private fun saveScrollPosition() {
        vm.scrollPosition = scrollView.scrollY
    }

    private fun restoreScrollPosition() {
        scrollView.post {
            scrollView.scrollTo(0, vm.scrollPosition)
            progressBar.visibility = View.GONE
            layout.visibility = View.VISIBLE
        }
    }
}

