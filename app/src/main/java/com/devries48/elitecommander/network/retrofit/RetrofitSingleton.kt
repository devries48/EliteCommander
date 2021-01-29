package com.devries48.elitecommander.network.retrofit

import android.content.Context
import com.devries48.elitecommander.R
import com.devries48.elitecommander.events.FrontierAuthNeededEvent
import com.devries48.elitecommander.models.FrontierAccessTokenResponse
import com.devries48.elitecommander.utils.OAuthUtils
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import org.greenrobot.eventbus.EventBus
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.Serializable
import java.util.concurrent.TimeUnit

// Singleton safe from serialization/reflection...
// From https://medium.com/exploring-code/how-to-make-the-perfect-singleton-de6b951dfdb0

open class RetrofitSingleton private constructor() : Serializable {
    private var edApiRetrofit: EDApiRetrofit? = null
    private var frontierAuthRetrofit: FrontierAuthRetrofit? = null
    private var frontierRetrofit: FrontierRetrofit? = null
    private var retrofitBuilder: Retrofit.Builder? = null

    //Make singleton from serialize and deserialize operation.
    protected fun readResolve(): RetrofitSingleton? {
        return getInstance()
    }

    fun getEdApiRetrofit(ctx: Context): EDApiRetrofit? {
        if (edApiRetrofit != null) {
            return edApiRetrofit
        }
        edApiRetrofit = retrofitInstance
            ?.baseUrl(ctx.getString(R.string.edapi_base))
            ?.build()
            ?.create(EDApiRetrofit::class.java)
        return edApiRetrofit
    }

    fun getFrontierAuthRetrofit(ctx: Context): FrontierAuthRetrofit? {
        if (frontierAuthRetrofit != null) {
            return frontierAuthRetrofit
        }
        frontierAuthRetrofit = retrofitInstance
            ?.baseUrl(ctx.getString(R.string.frontier_auth_base))
            ?.build()
            ?.create(FrontierAuthRetrofit::class.java)
        return frontierAuthRetrofit
    }

    fun getFrontierRetrofit(ctx: Context): FrontierRetrofit? {
        if (frontierRetrofit != null) {
            return frontierRetrofit
        }
        val httpClient = commonOkHttpClientBuilder

        // Add interceptor for tokens in response
        httpClient.addInterceptor { chain: Interceptor.Chain ->
            var request: Request =
                OAuthUtils.getRequestWithFrontierAuthorization(ctx, chain)
            var response = chain.proceed(request)

            // Check if access token expired and renew it if needed
            if (response.code() == 403 || response.code() == 422 || response.code() == 401) {
                try{
                    val responseBody: FrontierAccessTokenResponse? = OAuthUtils.makeRefreshRequest(ctx)
                    if (responseBody == null) {
                        // Send event to renew login
                        EventBus.getDefault().post(FrontierAuthNeededEvent(true))
                        return@addInterceptor response
                    }

                    // Retry request
                    responseBody.accessToken?.let {
                        responseBody.refreshToken?.let { it1 ->
                            OAuthUtils.storeUpdatedTokens(
                                ctx, it,
                                it1
                            )
                        }
                    }
                    response.close()
                    request = OAuthUtils.getRequestWithFrontierAuthorization(ctx, chain)
                    response = chain.proceed(request)

                } catch (e: IllegalArgumentException) {
                    e.printStackTrace()
                }
            }

            // If still not ok, need login
            if (!response.isSuccessful && (response.code() == 403 || response.code() == 422 || response.code() == 401)) {
                EventBus.getDefault().post(FrontierAuthNeededEvent(true))
            }
            response
        }
        val customRetrofitBuilder = Retrofit.Builder()
            .client(httpClient.build())
            .addConverterFactory(commonGsonConverterFactory)
        frontierRetrofit = customRetrofitBuilder
            .baseUrl(ctx.getString(R.string.frontier_api_base))
            .build()
            .create(FrontierRetrofit::class.java)
        return frontierRetrofit
    }

    private val retrofitInstance: Retrofit.Builder?
        get() {
            if (retrofitBuilder != null) {
                return retrofitBuilder
            }
            retrofitBuilder = Retrofit.Builder()
                .client(commonOkHttpClientBuilder.build())
                .addConverterFactory(commonGsonConverterFactory)
            return retrofitBuilder
        }
    private val commonGsonConverterFactory: GsonConverterFactory
        get() {
            val gson = GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                //.registerTypeAdapter(EDSMSystemInformationResponse::class.java, EDSMDeserializer())
                .create()
            return GsonConverterFactory.create(gson)
        }
    private val commonOkHttpClientBuilder: OkHttpClient.Builder
         get() = OkHttpClient().newBuilder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)

    companion object {
        @Volatile
        private var instance: RetrofitSingleton? = null
        fun getInstance(): RetrofitSingleton? {
            if (instance == null) {
                synchronized(RetrofitSingleton::class.java) {
                    if (instance == null) instance =
                        RetrofitSingleton()
                }
            }
            return instance
        }
    }

    // Private constructor.
    init {

        // Prevent form the reflection api.
        if (instance != null) {
            throw RuntimeException("Use getInstance() method to get an instance of this class.")
        }
    }
}