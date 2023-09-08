package countryRepository



import app.Country
import app.CountryInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import javax.inject.Inject

interface CountryApiService {
    @GET("v3.1/all?fields=name,capital,flags")
    suspend fun fetchCountryList(): List<Country>

    @GET("/v3.1/name/{countryName}")
    suspend fun fetchCountryInfo(@Path("countryName") countryName: String): List<CountryInfo>

    @GET("v3.1/alpha?fields=name")
    suspend fun fetchBordersInfo(@Query("codes") countryBorder: String): List<CountryInfo>
}

class CountryRepository @Inject constructor(
    private val apiService: CountryApiService
) {
    suspend fun fetchCountryList(): Flow<List<Country>> = flow {
        emit(apiService.fetchCountryList())
    }

    suspend fun fetchCountryInfo(countryName: String): Flow<List<CountryInfo>> = flow {
        emit(apiService.fetchCountryInfo(countryName))
    }

    suspend fun fetchBordersInfo(name: String): Flow<List<CountryInfo>> = flow {
        emit(apiService.fetchBordersInfo(name))
    }
}