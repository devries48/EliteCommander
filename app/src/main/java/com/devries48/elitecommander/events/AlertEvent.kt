package com.devries48.elitecommander.events

import androidx.annotation.StringRes

data class AlertEvent(@StringRes val title: Int, @StringRes val message: Int)
