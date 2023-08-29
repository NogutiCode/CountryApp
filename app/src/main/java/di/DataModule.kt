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
    @Singleton // it makes 1 time
    fun provideSharedPreferences(application: Application): SharedPreferences { // to check if program started first time or not
        return application.getSharedPreferences("entrance", Context.MODE_PRIVATE)
    }


    @Provides
    @Singleton // it makes 1 time
    fun provideGlide(@ApplicationContext context: Context): RequestManager { // for glide package  to make rounded pictures
        return Glide.with(context)
    }

    @Provides
    fun provideMoshi(): Moshi { // parse info to json
        return Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    @Provides
    fun provideRetrofit(moshi: Moshi): Retrofit { // make api request
        return Retrofit.Builder()
            .baseUrl("https://restcountries.com/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    fun provideCountryApiService(retrofit: Retrofit): CountryApiService { //to use my api service that gets other part of link
        return retrofit.create(CountryApiService::class.java)
    }

}

//###################################
//1) try to do offline mode (cash - Room database) ========== last
//###################################