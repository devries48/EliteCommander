package com.devries48.elitecommander.utils


import android.content.Context
import android.util.Log
import com.devries48.elitecommander.BuildConfig
import com.devries48.elitecommander.interfaces.FrontierAuthInterface
import com.devries48.elitecommander.models.httpRequest.FrontierAccessTokenRequestBody
import com.devries48.elitecommander.models.response.FrontierAccessTokenResponse
import com.devries48.elitecommander.network.RetrofitClient
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
            context, SettingsUtils.Key.ACCESS_TOKEN,
            accessToken
        )
        SettingsUtils.setString(
            context, SettingsUtils.Key.REFRESH_TOKEN,
            refreshToken
        )
    }

    fun getAccessToken(context: Context): String? {
        return SettingsUtils.getString(context, SettingsUtils.Key.ACCESS_TOKEN)
    }

    private fun getRefreshToken(context: Context): String? {
        return SettingsUtils.getString(context, SettingsUtils.Key.REFRESH_TOKEN)
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
        val auth= getAuthRetrofit(ctx)
        if (auth != null) {
            val authResponse: Call<FrontierAccessTokenResponse> =
                auth.getAccessToken(requestBody)
            return authResponse.execute().body()!!
        }
        return null
    }

    private fun getAuthRetrofit(ctx: Context): FrontierAuthInterface?
    {
        val retrofit = RetrofitClient.getInstance()
        return retrofit?.getFrontierAuthRetrofit(ctx)
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
