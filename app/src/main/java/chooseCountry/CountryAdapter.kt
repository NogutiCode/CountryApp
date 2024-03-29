package chooseCountry

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import app.Country
import com.example.countryapp.R
import jp.wasabeef.glide.transformations.CropCircleWithBorderTransformation
import jp.wasabeef.glide.transformations.RoundedCornersTransformation

class CountryAdapter(private val onItemClick: (Int) -> Unit) :
    RecyclerView.Adapter<CountryAdapter.CountryViewHolder>() {

    private var countryList: List<Country> = emptyList()
    private var fullCountryList: List<Country> = emptyList()


    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newCountryList: List<Country>) {
        countryList = newCountryList
        fullCountryList = newCountryList
        notifyDataSetChanged()
    }
    fun getItem(position: Int): Country {
        return countryList[position]
    }

    fun getFullList(): List<Country> {
        return fullCountryList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_country, parent, false)
        return CountryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CountryViewHolder, position: Int) {
        holder.bind(countryList[position])
    }

    override fun getItemCount(): Int {
        return countryList.size
    }

    inner class CountryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val button: Button = itemView.findViewById(R.id.countryButton)
        private val imageView: ImageView = itemView.findViewById(R.id.countryImageView)
        private val countryText: TextView = itemView.findViewById(R.id.countryText)
        private val capitalText: TextView = itemView.findViewById(R.id.capitalText)
        init {
            button.setOnClickListener { onItemClick(adapterPosition) }
        }

        @SuppressLint("SetTextI18n")
        fun bind(country: Country) {
            val name = country.name?.common
            val capital = country.capital?.toString()?.filter { it.isLetter() }
            val flag = country.flags?.png
            val formattedCapital = capital ?: ""
            countryText.text = name
            capitalText.text = formattedCapital
            val borderColor = ContextCompat.getColor(itemView.context, R.color.countryDesign)

            Glide.with(itemView)
                .load(flag)
                .apply(
                    RequestOptions()
                        .transform(
                            CropCircleWithBorderTransformation(4, borderColor),
                            RoundedCornersTransformation(16, 0)
                        )
                        .override(200, 200)
                )
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imageView)
        }
    }
}
