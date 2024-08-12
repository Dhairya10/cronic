package com.renalize.android.di

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.renalize.android.data.api.ApiService
import com.renalize.android.data.repository.PatientRepository
import com.renalize.android.util.Constants.BASE_URL
import com.renalize.android.util.PreferenceManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {

    @Provides
    @Singleton
    fun providePatientRepository(
        auth: FirebaseAuth,
        firebaseStorage: FirebaseStorage,
        chatService: ApiService,
        preferenceManager: PreferenceManager
    ): PatientRepository {
        return PatientRepository(
            auth = auth,
            firebaseStorage = firebaseStorage,
            apiService = chatService,
            preferenceManager = preferenceManager
        )
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        preferenceManager: PreferenceManager,
        firebaseAuth: FirebaseAuth,
        @ApplicationContext context: Context
    ): Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(
            OkHttpClient
                .Builder()
                .readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)
                .addInterceptor { chain ->
                    val token: String
                    runBlocking {
                        token = firebaseAuth.currentUser!!.getIdToken(true).await().token!!
                    }
                    val request =
                        chain.request().newBuilder()
                        .addHeader(
                            "Authorization",
                            "Bearer $token"
                        ).build()
                    chain.proceed(request)
                }.addInterceptor(ChuckerInterceptor(context))
                .build()
        )
        .build()

    @Singleton
    @Provides
    fun providesChatService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}