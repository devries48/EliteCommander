package com.devries48.elitecommander.models

import androidx.annotation.StringRes

class StatisticModel {

    var type: StatisticsBuilder.StatisticType? = null

    @StringRes
    var leftTitleResId: Int = 0
    var leftValue: String? = null
    var leftShowDelta:Boolean=false

    @StringRes
    var middleTitleResId: Int = 0
    var middleValue: String? = null
    var middleValueAlignLeft: Boolean = false
    var middleShowDelta:Boolean=false
    var middleColor: StatisticsBuilder.StatisticColor = StatisticsBuilder.StatisticColor.DEFAULT

    @StringRes
    var rightTitleResId: Int = 0
    var rightValue: String? = null
    var rightShowDelta:Boolean=false
    var rightColor: StatisticsBuilder.StatisticColor = StatisticsBuilder.StatisticColor.DEFAULT

}