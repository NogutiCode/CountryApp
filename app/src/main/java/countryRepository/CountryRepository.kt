package countryRepository

import app.Country
import app.CountryInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.*
import retrofit2.http.GET
import javax.inject.Inject

interface CountryApiService {
    @GET("v3.1/all?fields=name,capital,flags")
    suspend fun fetchCountryList(): List<Country>

    @GET("/v3.1/all")
    suspend fun fetchCountryInfo(): List<CountryInfo>
}
class CountryRepository @Inject constructor(
    private val apiService: CountryApiService
) {
    suspend fun fetchCountryList(): Flow<List<Country>> = flow {
        emit(apiService.fetchCountryList())
    }

    suspend fun fetchCountryInfo(): Flow<List<CountryInfo>> = flow {
        emit(apiService.fetchCountryInfo())
    }
}
