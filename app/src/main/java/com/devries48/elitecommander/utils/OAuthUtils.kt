package com.devries48.elitecommander.utils


import android.content.Context
import android.util.Log
import com.devries48.elitecommander.BuildConfig
import com.devries48.elitecommander.R
import com.devries48.elitecommander.models.FrontierAccessTokenRequestBody
import com.devries48.elitecommander.models.FrontierAccessTokenResponse
import com.devries48.elitecommander.network.retrofit.RetrofitSingleton
import okhttp3.Interceptor
import okhttp3.Request
import retrofit2.Call
import java.io.IOException


object OAuthUtils {
    fun storeUpdatedTokens(context: Context, accessToken: String, refreshToken: String) {
        if (BuildConfig.DEBUG) {
            Log.d("Tokens", "Access token: $accessToken")
            Log.d("Tokens", "Refresh token: $refreshToken")
        }
        SettingsUtils.setString(
            context, context.getString(R.string.access_token_key),
            accessToken
        )
        SettingsUtils.setString(
            context, context.getString(R.string.refresh_token_key),
            refreshToken
        )
    }

    private fun getAccessToken(context: Context): String {
        return SettingsUtils.getString(context, context.getString(R.string.access_token_key))!!
    }

    private fun getRefreshToken(context: Context): String {
        return SettingsUtils.getString(context, context.getString(R.string.refresh_token_key))!!
    }

    private fun getRefreshTokenRequestBody(ctx: Context): FrontierAccessTokenRequestBody {
        val requestBody = FrontierAccessTokenRequestBody()
        requestBody.grantType = "refresh_token"
        requestBody.clientId = BuildConfig.FRONTIER_AUTH_CLIENT_ID
        requestBody.refreshToken = getRefreshToken(ctx)
        return requestBody
    }

    fun getAuthorizationCodeRequestBody(
        codeVerifier: String?,
        authCode: String?
    ): FrontierAccessTokenRequestBody {
        val requestBody = FrontierAccessTokenRequestBody()
        requestBody.codeVerifier = codeVerifier
        requestBody.grantType = "authorization_code"
        requestBody.clientId = BuildConfig.FRONTIER_AUTH_CLIENT_ID
        requestBody.code = authCode
        requestBody.redirectUri = "elitecommander://oauth"
        return requestBody
    }

    @Throws(IOException::class)
    fun makeRefreshRequest(ctx: Context): FrontierAccessTokenResponse? {
        val requestBody = getRefreshTokenRequestBody(ctx)
        val retrofit = RetrofitSingleton.getInstance()
        val frontierRetrofit = retrofit?.getFrontierAuthRetrofit(ctx)

        if (frontierRetrofit != null) {
            val authResponse: Call<FrontierAccessTokenResponse> =
                frontierRetrofit.getAccessToken(requestBody)
            return authResponse.execute().body()!!
        }
        return null
    }

    fun getRequestWithFrontierAuthorization(ctx: Context, chain: Interceptor.Chain): Request {
        // Add access token header to request
        val originalRequest: Request = chain.request()
        val requestBuilder = originalRequest.newBuilder()
            .header(
                "Authorization", "Bearer " +
                        getAccessToken(ctx)
            )
        return requestBuilder.build()
    }
}
