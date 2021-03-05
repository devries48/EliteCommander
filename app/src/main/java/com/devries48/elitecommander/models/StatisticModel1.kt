package com.devries48.elitecommander.models

import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import org.jetbrains.annotations.NotNull

//TODO: Setup as row and column class
data class StatisticModel1(
    @StringRes val stringRes: Int = 0,
    var value: String? = null,

    @StringRes var middleStringRes: Int = 0,
    var middleValue: String? = null,
    @StyleRes @NotNull  var middleValueStyleRes: Int = 0,

    @StringRes var rightStringRes: Int = 0,
    var rightValue: String? = null,
    @StyleRes @NotNull  var rightValueStyleRes: Int = 0
)