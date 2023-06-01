package com.example.countryapp
import kotlinx.coroutines.*
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.*
import java.io.IOException
import jp.wasabeef.glide.transformations.CropCircleWithBorderTransformation
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import kotlin.system.exitProcess


class ChooseCountry : Fragment() {
    private lateinit var scrollView: ScrollView
    private val scrollPositionKey = "scroll_position"
    private var isApiKeyLoaded = false

    private var countryList: List<Country>? = null
    private lateinit var progressBar: ProgressBar
    private lateinit var navController: NavController
    private lateinit var layout: LinearLayout
    private lateinit var call: Call
    private var stopFunction = false
    private val client = OkHttpClient()

    private var createdButtonCount = 0
    private var totalCountryCount = 0



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_choose_country, container, false)
        progressBar = view.findViewById(R.id.progressBar)
        layout = view.findViewById(R.id.listOfCountries)
        scrollView = view.findViewById(R.id.CountryScroll)
        return view

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)
        initValues(view)

    }

    private fun initValues(view: View){
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

    override fun onResume() {
        super.onResume()
        if (isApiKeyLoaded) {
            restoreScrollPosition()
        } else {
            makeApiRequest()
        }
    }

    override fun onPause() {
        super.onPause()
        saveScrollPosition()
    }

    private fun makeApiRequest() {
        progressBar.visibility = View.VISIBLE
        layout.visibility = View.GONE
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
                        countryList = adapter.fromJson(responseBody)
                        countryList?.let { list ->
                            //totalCountryCount = list.size
                            for ((index, country) in list.withIndex()) {
                                val name = country.name?.common
                                val capital = country.capital?.toString()?.replace("[", "")?.replace("]", "")
                                val flag = country.flags?.png
                                val formattedCapital = capital ?: ""

                                activity?.runOnUiThread {
                                    buildDesign("$name \n$formattedCapital" ,"$flag", "$index")
                                }

                            }
                        }
                    }
                }
            }
        })

    }

    private fun setButtonWithImage(button: Button, text: String, image: Drawable?) {
        button.text = text
        button.setCompoundDrawablesWithIntrinsicBounds(image, null, null, null)
    }

    private fun buildDesign(textCountry: String, ImageLink: String, ButtonId: String) {
        val button = Button(requireContext())
        val imageView = ImageView(requireContext())
        val imagePadding = 75
        val btnId = ButtonId.toInt()
        //loadedCountryCount++
        button.id = btnId
        button.text = textCountry
        button.isAllCaps = false
        button.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
        button.compoundDrawablePadding = imagePadding
        if (stopFunction) {
            return
        }
        Glide.with(requireContext())
            .load(ImageLink)
            .apply(RequestOptions()
                .transform(
                    CropCircleWithBorderTransformation(4, Color.GRAY), // Установите желаемую ширину и цвет обводки
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
                    imageView.setImageDrawable(resource)
                    imageView.layoutParams = ViewGroup.LayoutParams(300, 300)
                    if (stopFunction) {
                        return
                    }
                    button.layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 300
                    )
                    val linearLayout = view?.findViewById<LinearLayout>(R.id.listOfCountries)
                    setButtonWithImage(button, textCountry, imageView.drawable)
                    button.background = ColorDrawable(Color.TRANSPARENT)

                    linearLayout?.addView(button)
                    val bundle = Bundle()
                    button.setOnClickListener {
                        bundle.putInt("buttonId", btnId)
                        navController.navigate(R.id.action_chooseCountry_to_countryInfo, bundle)
                    }
                    //apiKeyLoaded() -- Возвращение скролла на место где оно было
                    if (btnId == totalCountryCount) {
                            progressBar.visibility = View.GONE
                            layout.visibility = View.VISIBLE
                    }


                }
                override fun onLoadCleared(placeholder: Drawable?) {}
            })
        }

    private fun apiKeyLoaded() {
        isApiKeyLoaded = true
        restoreScrollPosition()
    }

    private fun saveScrollPosition() {
        val scrollPosition = scrollView.scrollY
        val sharedPreferences = requireContext().getSharedPreferences("scroll_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putInt(scrollPositionKey, scrollPosition).apply()
    }

    private fun restoreScrollPosition() {
        val sharedPreferences = requireContext().getSharedPreferences("scroll_prefs", Context.MODE_PRIVATE)
        val scrollPosition = sharedPreferences.getInt(scrollPositionKey, 0)
        scrollView.post {
            scrollView.scrollTo(0, scrollPosition)
        }
    }
}

