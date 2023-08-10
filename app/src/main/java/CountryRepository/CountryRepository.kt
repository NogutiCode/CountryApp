package CountryRepository

import app.Country
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.*
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET

interface CountryApiService {
    @GET("v3.1/all")
    suspend fun fetchCountryList(): List<Country>
}
class CountryRepository {
    private val apiService: CountryApiService

    init {
        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://restcountries.com/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        apiService = retrofit.create(CountryApiService::class.java)
    }

    suspend fun fetchCountryList(): List<Country>? = withContext(Dispatchers.IO) {
        var retryCount = 0
        val maxRetries = 5
        var countryList: List<Country>? = null

        while (retryCount < maxRetries) {
            try {
                countryList = apiService.fetchCountryList()
                break
            } catch (e: Exception) {
                e.printStackTrace()
            }

            retryCount++
            delay(1000)
        }

        countryList
    }
}
