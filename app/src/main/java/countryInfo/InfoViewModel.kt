package countryInfo


import android.content.Context
import android.graphics.Color
import android.widget.ImageView
import android.widget.TextView
import countryRepository.CountryRepository
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.Country
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.wasabeef.glide.transformations.CropCircleWithBorderTransformation
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class InfoViewModel @Inject constructor() : ViewModel() {
    private val repository = CountryRepository()


    val countryListLiveData: MutableLiveData<List<Country>> by lazy {
        MutableLiveData<List<Country>>()
    }

    fun fetchCountryList() {
        viewModelScope.launch {
            val countryList = withContext(Dispatchers.IO) {
                repository.fetchCountryList()
            }
            countryListLiveData.value = countryList
        }
    }



    fun checkCountryInfo(checkType: String, textVar: TextView, ifNoSomething: String) {
        textVar.text = if (checkType == "") ifNoSomething else checkType
    }

    fun loadCountryPhoto(context: Context, countryPhoto: String, imageView: ImageView) {
        Glide.with(context)
            .load(countryPhoto)
            .apply(
                RequestOptions()
                    .transform(
                        CropCircleWithBorderTransformation(4, Color.parseColor("#4942E4")),
                        RoundedCornersTransformation(16, 0)
                    )
                    .override(500, 500)
            )
            .into(imageView)
    }
}
