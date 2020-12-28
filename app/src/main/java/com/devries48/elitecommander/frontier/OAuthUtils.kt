package com.devries48.elitecommander.frontier


import android.content.Context
import android.util.Log
import com.devries48.elitecommander.BuildConfig
import com.devries48.elitecommander.R
import com.devries48.elitecommander.utils.SettingsUtils
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

    fun getAccessToken(context: Context): String {
        return SettingsUtils.getString(context, context.getString(R.string.access_token_key))!!
    }

    fun getRefreshToken(context: Context): String {
        return SettingsUtils.getString(context, context.getString(R.string.refresh_token_key))!!
    }

    private fun getRefreshTokenRequestBody(ctx: Context): FrontierAccessTokenRequestBody {
        val requestBody = FrontierAccessTokenRequestBody()
        requestBody.GrantType = "refresh_token"
        requestBody.ClientId = BuildConfig.FRONTIER_AUTH_CLIENT_ID
        requestBody.RefreshToken = getRefreshToken(ctx)
        return requestBody
    }

    fun getAuthorizationCodeRequestBody(
        codeVerifier: String?,
        authCode: String?
    ): FrontierAccessTokenRequestBody {
        val requestBody = FrontierAccessTokenRequestBody()
        requestBody.CodeVerifier = codeVerifier
        requestBody.GrantType = "authorization_code"
        requestBody.ClientId = BuildConfig.FRONTIER_AUTH_CLIENT_ID
        requestBody.Code = authCode
        requestBody.RedirectUri = "elitecommander://oauth"
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
