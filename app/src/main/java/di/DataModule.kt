package di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.example.countryapp.CountryRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideSharedPreferences(application: Application): SharedPreferences {
        return application.getSharedPreferences("entrance", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideCountryRepository(): CountryRepository {
        return CountryRepository()
    }

}
//1) Retrofit DONE /////////
//2) прочитать про dependecies injection
//3) постараться перенести на viewmodel
//4) use recyclerview
//5) раскидать все по пакетам к каждому viewmodel
//6) прочитать repository pattern
//7) посмотреть все про lifecycle
//8) посмотреть что такое flow с coroutines
//9) попробовать сделать offline mode (cash - Room database)
//10) https://medium.com/@BerkOzyurt/android-clean-architecture-mvvm-usecase-ae1647f0aea3 почитать