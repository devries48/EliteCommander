package com.devries48.elitecommander.declarations

import androidx.lifecycle.MutableLiveData

fun Any?.toStringOrEmpty() = this?.toString() ?: ""

fun <T : Any?> MutableLiveData<T>.default(initialValue: T) = apply { postValue(initialValue) }
