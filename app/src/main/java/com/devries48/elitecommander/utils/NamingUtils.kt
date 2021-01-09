package com.devries48.elitecommander.utils

import com.devries48.elitecommander.R

object NamingUtils {
    fun getShipName(internalName: String): String {
        return when (internalName) {
            "SideWinder" -> "Sidewinder"
            "Viper" -> "Viper MkIII"
            "CobraMkIII" -> "Cobra MkIII"
            "Type6" -> "Type-6 Transporter"
            "Type7" -> "Type-7 Transporter"
            "Asp" -> "Asp Explorer"
            "Empire_Trader" -> "Imperial Clipper"
            "Federation_Dropship" -> "Federal Dropship"
            "Type9" -> "Type-9 Heavy"
            "BelugaLiner" -> "Beluga Liner"
            "FerDeLance" -> "Fer-de-Lance"
            "Federation_Corvette" -> "Federal Corvette"
            "Cutter" -> "Imperial Cutter"
            "DiamondBack" -> "Diamondback Scout"
            "Empire_Courier" -> "Imperial Courier"
            "DiamondBackXL" -> "Diamondback Explorer"
            "Empire_Eagle" -> "Imperial Eagle"
            "Federation_Dropship_MkII" -> "Federal Assault Ship"
            "Federation_Gunship" -> "Federal Gunship"
            "Viper_MkIV" -> "Viper MkIV"
            "CobraMkIV" -> "Cobra MkIV"
            "Independant_Trader" -> "Keelback"
            "Asp_Scout" -> "Asp Scout"
            "Type9_Military" -> "Type-10 Defender"
            "Krait_MkII" -> "Krait MkII"
            "TypeX" -> "Alliance Chieftain"
            "TypeX_2" -> "Alliance Crusader"
            "TypeX_3" -> "Alliance Challenger"
            "Krait_Light" -> "Krait Phantom"
            else -> internalName
        }
    }

    //TODO: Refactor rank names alphabetically

    fun getCombatRankDrawableId(rankValue: Int): Int {
        return when (rankValue + 1) {
            2 -> R.drawable.rank_2_combat
            3 -> R.drawable.rank_3_combat
            4 -> R.drawable.rank_4_combat
            5 -> R.drawable.rank_5_combat
            6 -> R.drawable.rank_6_combat
            7 -> R.drawable.rank_7_combat
            8 -> R.drawable.rank_8_combat
            9 -> R.drawable.rank_9_combat
            else -> R.drawable.rank_1_combat
        }
    }

    fun getTradeRankDrawableId(rankValue: Int): Int {
        return when (rankValue + 1) {
            2 -> R.drawable.rank_2_trading
            3 -> R.drawable.rank_3_trading
            4 -> R.drawable.rank_4_trading
            5 -> R.drawable.rank_5_trading
            6 -> R.drawable.rank_6_trading
            7 -> R.drawable.rank_7_trading
            8 -> R.drawable.rank_8_trading
            9 -> R.drawable.rank_9_trading
            else -> R.drawable.rank_1_trading
        }
    }

    fun getExplorationRankDrawableId(rankValue: Int): Int {
        return when (rankValue + 1) {
            2 -> R.drawable.rank_2_exploration
            3 -> R.drawable.rank_3_exploration
            4 -> R.drawable.rank_4_exploration
            5 -> R.drawable.rank_5_exploration
            6 -> R.drawable.rank_6_exploration
            7 -> R.drawable.rank_7_exploration
            8 -> R.drawable.rank_8_exploration
            9 -> R.drawable.rank_9_exploration
            else -> R.drawable.rank_1_exploration
        }
    }

    fun getCqcRankDrawableId(rankValue: Int): Int {
        return when (rankValue + 1) {
            2 -> R.drawable.rank_2_cqc
            3 -> R.drawable.rank_3_cqc
            4 -> R.drawable.rank_4_cqc
            5 -> R.drawable.rank_5_cqc
            6 -> R.drawable.rank_6_cqc
            7 -> R.drawable.rank_7_cqc
            8 -> R.drawable.rank_8_cqc
            9 -> R.drawable.rank_9_cqc
            else -> R.drawable.rank_1_cqc
        }
    }

    fun getFederationRankDrawableId(rankValue: Int): Int {
        return when (rankValue + 1) {
            2 -> R.drawable.rank_2_fed
            3 -> R.drawable.rank_3_fed
            4 -> R.drawable.rank_4_fed
            5 -> R.drawable.rank_5_fed
            6 -> R.drawable.rank_6_fed
            7 -> R.drawable.rank_7_fed
            8 -> R.drawable.rank_8_fed
            9 -> R.drawable.rank_9_fed
            10 -> R.drawable.rank_10_fed
            11-> R.drawable.rank_11_fed
            12 -> R.drawable.rank_12_fed
            13-> R.drawable.rank_13_fed
            14 -> R.drawable.rank_14_fed
            else -> R.drawable.rank_1_fed
        }
    }

    fun getEmpireRankDrawableId(rankValue: Int): Int {
        return when (rankValue + 1) {
            2 -> R.drawable.rank_2_emp
            3 -> R.drawable.rank_3_emp
            4 -> R.drawable.rank_4_emp
            5 -> R.drawable.rank_5_emp
            6 -> R.drawable.rank_6_emp
            7 -> R.drawable.rank_7_emp
            8 -> R.drawable.rank_8_emp
            9 -> R.drawable.rank_9_emp
            10 -> R.drawable.rank_10_emp
            11-> R.drawable.rank_11_emp
            12 -> R.drawable.rank_12_emp
            13-> R.drawable.rank_13_emp
            14 -> R.drawable.rank_14_emp
            else -> R.drawable.rank_1_emp
        }
    }
}