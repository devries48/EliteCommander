package com.devries48.elitecommander.declarations

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

fun <T> Call<T>.enqueueWrap(callback: CallBackKt<T>.() -> Unit) {
    val callBackKt = CallBackKt<T>()
    callback.invoke(callBackKt)
    this.enqueue(callBackKt)
}

class CallBackKt<T> : Callback<T> {
    var onResponse: ((Response<T>) -> Unit)? = null
    var onFailure: ((t: Throwable?) -> Unit)? = null

    override fun onFailure(call: Call<T>, t: Throwable) {
        onFailure?.invoke(t)
    }

    override fun onResponse(call: Call<T>, response: Response<T>) {
        onResponse?.invoke(response)
    }
}

@Suppress("UNCHECKED_CAST")
suspend fun <T> Call<T>.getResult(): Pair<Int, T> = suspendCoroutine { cont ->
    enqueue(object : Callback<T> {
        override fun onFailure(call: Call<T>, t: Throwable) {
            cont.resumeWithException(t)
        }

        override fun onResponse(call: Call<T>, response: Response<T>) {
            if (response.isSuccessful) {
                when {
                    response.code() == 204 -> cont.resume(Pair(204, null as T))
                    response.code() == 206 -> cont.resume(Pair(206, null as T))
                    else -> cont.resume(Pair(response.code(), response.body()!! as T))
                }
            } else {
                cont.resumeWithException(ErrorResponse(response.message(), response.code()))
            }
        }

    })
}

class ErrorResponse(message: String, code: Int) : Throwable(message)
