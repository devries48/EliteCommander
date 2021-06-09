package com.devries48.elitecommander.models

import androidx.annotation.StringRes

class RowModel {

    var type: String? = null

    @StringRes
    var leftTitleResId: Int = 0
    var leftValue: String? = null
    var leftDelta: String? = null
    var leftColor: RowBuilder.RowColor = RowBuilder.RowColor.DEFAULT

    @StringRes
    var middleTitleResId: Int = 0
    var middleValue: String? = null
    var middleDelta: String? = null
    var middleColor: RowBuilder.RowColor = RowBuilder.RowColor.DEFAULT

    @StringRes
    var rightTitleResId: Int = 0
    var rightValue: String? = null
    var rightDelta: String? = null
    var rightColor: RowBuilder.RowColor = RowBuilder.RowColor.DEFAULT
}