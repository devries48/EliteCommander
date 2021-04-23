package com.devries48.elitecommander.models

import androidx.annotation.StringRes

class StatisticModel {

    var type: StatisticsBuilder.Companion.StatisticType? = null

    @StringRes
    var leftTitleResId: Int = 0
    var leftValue: String? = null
    var leftDelta: String? = null
    var leftColor: StatisticsBuilder.Companion.StatisticColor = StatisticsBuilder.Companion.StatisticColor.DEFAULT

    @StringRes
    var middleTitleResId: Int = 0
    var middleValue: String? = null
    var middleDelta: String? = null
    var middleColor: StatisticsBuilder.Companion.StatisticColor = StatisticsBuilder.Companion.StatisticColor.DEFAULT

    @StringRes
    var rightTitleResId: Int = 0
    var rightValue: String? = null
    var rightDelta: String? = null
    var rightColor: StatisticsBuilder.Companion.StatisticColor = StatisticsBuilder.Companion.StatisticColor.DEFAULT

}