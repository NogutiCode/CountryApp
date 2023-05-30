package com.example.countryapp

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
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



class ChooseCountry : Fragment() {
    private lateinit var progressBar: ProgressBar
    private lateinit var navController: NavController
    private val client = OkHttpClient()
    private var btnId: Int = 0;
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_choose_country, container, false)
        progressBar = view.findViewById(R.id.progressBar)
        return view

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)

        makeApiRequest()

        val btn: ImageButton = view.findViewById(R.id.toCountryInfo)
        btn.setOnClickListener {
            navController.navigate(R.id.action_chooseCountry_to_countryInfo)
        }
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
                                val name = country.name?.common
                                val capital = country.capital?.toString()?.replace("[", "")?.replace("]", "")
                                val flag = country.flags?.png
                                val formattedCapital = capital ?: ""
                                //println("Element Index: $index")
                                //println("Name: $name ::: $formattedCapital ::: $flag")

                                activity?.runOnUiThread {
                                    buildDesign("$name \n$formattedCapital", "$flag", "$index")
                                    progressBar.visibility = View.GONE
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

    private fun buildDesign(ButtonText: String, ImageLink: String, ButtonId: String) {
        val button = Button(requireContext())
        val imageView = ImageView(requireContext())
        val imageUrl = ImageLink
        val imagePadding = 75
        val btnId = ButtonId.toInt()
        button.id = btnId
        //println(button.id)
        button.text = ButtonText
        button.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
        button.compoundDrawablePadding = imagePadding

        Glide.with(requireContext())
            .load(imageUrl)
            .apply(RequestOptions().circleCrop().override(200, 200))
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(object : CustomTarget<Drawable>() {
                override fun onResourceReady(
                    resource: Drawable,
                    transition: Transition<in Drawable>?
                ) {
                    imageView.setImageDrawable(resource)
                    imageView.layoutParams = ViewGroup.LayoutParams(300, 300)

                    button.layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 300
                    )

                    val linearLayout = view?.findViewById<LinearLayout>(R.id.listOfCountries)
                    setButtonWithImage(button, ButtonText, imageView.drawable)
                    button.background = ColorDrawable(Color.TRANSPARENT)

                    linearLayout?.addView(button)
                    val bundle = Bundle()
                    button.setOnClickListener {
                        bundle.putInt("buttonId", btnId)
                        navController.navigate(R.id.action_chooseCountry_to_countryInfo, bundle)
                        //println(btnId)

                    }
                }
                override fun onLoadCleared(placeholder: Drawable?) {}
            })
        }
    }

