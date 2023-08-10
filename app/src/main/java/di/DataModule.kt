package di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import countryRepository.CountryRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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

    @Provides
    @Singleton
    fun provideGlide(@ApplicationContext context: Context): RequestManager {
        return Glide.with(context)
    }

}
//1) Retrofit ///// DONE
//###################################
//2) Прочитать про dependecies injection ======== HILT
//###################################
//3) постараться перенести на viewmodel ///// DONE
//4) use recyclerview ///// DONE
//5) раскидать все по пакетам к каждому viewmodel ///// DONE
//6) прочитать repository pattern //// DONE
//7) посмотреть все про lifecycle //// DONE
//8) посмотреть что такое flow с coroutines //// DONE (Насчет использование пока незнаю)
//###################################
//9) Попробовать сделать offline mode (cash - Room database) ==========
//###################################
//10) https://medium.com/@BerkOzyurt/android-clean-architecture-mvvm-usecase-ae1647f0aea3 почитать //// DONE