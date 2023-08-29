package countryRepository

import app.Country
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.*
import retrofit2.http.GET
import javax.inject.Inject

interface CountryApiService {
    @GET("v3.1/all")
    suspend fun fetchCountryList(): List<Country>
}
class CountryRepository @Inject constructor(
    private val apiService: CountryApiService
) {
    suspend fun fetchCountryList(): Flow<List<Country>> = flow {// function that suspends it + the country list returns as a Flow.
        emit(apiService.fetchCountryList())
    }
}
