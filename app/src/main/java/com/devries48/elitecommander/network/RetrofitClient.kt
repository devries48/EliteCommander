package com.devries48.elitecommander.network

import android.content.Context
import com.devries48.elitecommander.R
import com.devries48.elitecommander.events.FrontierAuthNeededEvent
import com.devries48.elitecommander.interfaces.EdsmInterface
import com.devries48.elitecommander.interfaces.FrontierAuthInterface
import com.devries48.elitecommander.interfaces.FrontierInterface
import com.devries48.elitecommander.models.response.frontier.AccessTokenResponse
import com.devries48.elitecommander.utils.OAuthUtils
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.greenrobot.eventbus.EventBus
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.Serializable
import java.util.concurrent.TimeUnit

// Singleton safe from serialization/reflection...
// From https://medium.com/exploring-code/how-to-make-the-perfect-singleton-de6b951dfdb0

open class RetrofitClient private constructor() : Serializable {
    private var frontierAuthInterface: FrontierAuthInterface? = null
    private var frontierInterface: FrontierInterface? = null
    private var edsmInterface: EdsmInterface? = null
    private var retrofitBuilder: Retrofit.Builder? = null

    fun getEdsmRetrofit(ctx: Context): EdsmInterface? {
        if (edsmInterface != null) return edsmInterface

        edsmInterface = retrofitInstance
            ?.baseUrl(ctx.getString(R.string.edsm_base))
            ?.build()
            ?.create(EdsmInterface::class.java)
        return edsmInterface
    }

    fun getFrontierAuthRetrofit(ctx: Context): FrontierAuthInterface? {
        if (frontierAuthInterface != null) return frontierAuthInterface

        frontierAuthInterface = retrofitInstance
            ?.baseUrl(ctx.getString(R.string.frontier_auth_base))
            ?.build()
            ?.create(FrontierAuthInterface::class.java)
        return frontierAuthInterface
    }

    fun getFrontierRetrofit(ctx: Context): FrontierInterface? {
        if (frontierInterface != null) return frontierInterface

        val httpClient = commonOkHttpClientBuilder

        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.NONE)
        httpClient.addInterceptor(logging)

        // Add interceptor for tokens in response
        httpClient.addInterceptor { chain: Interceptor.Chain ->
            var request: Request = OAuthUtils.getRequestWithFrontierAuthorization(ctx, chain)
            var response = chain.proceed(request)

            // Check if access token expired and renew it if needed
            if (isExpired(response.code)) {
                try {
                    val body: AccessTokenResponse? = OAuthUtils.makeRefreshRequest(ctx)
                    if (body == null) {
                        EventBus.getDefault().post(FrontierAuthNeededEvent(true)) // Send event to renew login
                        return@addInterceptor response
                    }

                    // Retry request
                    body.accessToken?.let {
                        body.refreshToken?.let { it1 ->
                            OAuthUtils.storeUpdatedTokens(ctx, it, it1)
                        }
                    }
                    response.close()
                    request = OAuthUtils.getRequestWithFrontierAuthorization(ctx, chain)
                    response = chain.proceed(request)

                } catch (e: IllegalArgumentException) {
                    e.printStackTrace()
                }

                // If still not ok, need login
                if (isFailed(response)) EventBus.getDefault().post(FrontierAuthNeededEvent(true))
            }

            response
        }

        val customRetrofitBuilder = Retrofit.Builder()
            .client(httpClient.build())
            .addConverterFactory(commonGsonConverterFactory)
        frontierInterface = customRetrofitBuilder
            .baseUrl(ctx.getString(R.string.frontier_api_base))
            .build()
            .create(FrontierInterface::class.java)
        return frontierInterface
    }

    private fun isExpired(code: Int): Boolean {
        return code == 403 || code == 422 || code == 401
    }

    private fun isFailed(response: Response): Boolean {
        return !response.isSuccessful || response.code == 403 || response.code == 422 || response.code == 401
    }

    private val retrofitInstance: Retrofit.Builder?
        get() {
            if (retrofitBuilder != null) return retrofitBuilder

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
        private var instance: RetrofitClient? = null

        fun getInstance(): RetrofitClient? {
            if (instance == null) {
                synchronized(RetrofitClient::class.java) {
                    if (instance == null) instance = RetrofitClient()
                }
            }
            return instance
        }
    }

    init {
        // Prevent form the reflection api.
        if (instance != null) {
            throw RuntimeException("Use getInstance() method to get an instance of this class.")
        }
    }
}