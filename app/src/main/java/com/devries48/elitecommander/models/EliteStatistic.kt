package com.devries48.elitecommander.models

import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import org.jetbrains.annotations.NotNull

data class EliteStatistic(
    @StringRes val stringRes: Int = 0,
    var value: String? = null,
    @StringRes var rightStringRes: Int = 0,
    var rightValue: String? = null,
    @StyleRes @NotNull  var rightValueStyleRes: Int = 0
)