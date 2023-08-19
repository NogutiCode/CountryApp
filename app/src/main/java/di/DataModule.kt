package di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import countryRepository.CountryApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
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
    fun provideGlide(@ApplicationContext context: Context): RequestManager {
        return Glide.with(context)
    }

    @Provides
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    @Provides
    fun provideRetrofit(moshi: Moshi): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://restcountries.com/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    fun provideCountryApiService(retrofit: Retrofit): CountryApiService {
        return retrofit.create(CountryApiService::class.java)
    }

}
//1) Retrofit ///// DONE
//2) Прочитать про dependecies injection //// DONE
//3) постараться перенести на viewmodel ///// DONE
//4) use recyclerview ///// DONE
//5) раскидать все по пакетам к каждому viewmodel ///// DONE
//6) прочитать repository pattern //// DONE
//7) посмотреть все про lifecycle //// DONE
//8) посмотреть что такое flow с coroutines ///// DONE
//###################################
//9) Попробовать сделать offline mode (cash - Room database) ==========
//###################################
//10) https://medium.com/@BerkOzyurt/android-clean-architecture-mvvm-usecase-ae1647f0aea3 почитать //// DONE