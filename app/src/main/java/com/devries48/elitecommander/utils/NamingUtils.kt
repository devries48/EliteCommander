package com.devries48.elitecommander.utils

import com.devries48.elitecommander.R
import java.util.*

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
            "Federation_Dropship_MkII" -> "Federal Assault FrontierShip"
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

    fun getCombatRankDrawableId(rankValue: Int): Int {
        return when (rankValue + 1) {
            2 -> R.drawable.rank_combat_2
            3 -> R.drawable.rank_combat_3
            4 -> R.drawable.rank_combat_4
            5 -> R.drawable.rank_combat_5
            6 -> R.drawable.rank_combat_6
            7 -> R.drawable.rank_combat_7
            8 -> R.drawable.rank_combat_8
            9 -> R.drawable.rank_combat_9
            else -> R.drawable.rank_combat_1
        }
    }

    fun getTradeRankDrawableId(rankValue: Int): Int {
        return when (rankValue + 1) {
            2 -> R.drawable.rank_trading_2
            3 -> R.drawable.rank_trading_3
            4 -> R.drawable.rank_trading_4
            5 -> R.drawable.rank_trading_5
            6 -> R.drawable.rank_trading_6
            7 -> R.drawable.rank_trading_7
            8 -> R.drawable.rank_trading_8
            9 -> R.drawable.rank_trading_9
            else -> R.drawable.rank_trading_1
        }
    }

    fun getExplorationRankDrawableId(rankValue: Int): Int {
        return when (rankValue + 1) {
            2 -> R.drawable.rank_exploration_2
            3 -> R.drawable.rank_exploration_3
            4 -> R.drawable.rank_exploration_4
            5 -> R.drawable.rank_exploration_5
            6 -> R.drawable.rank_exploration_6
            7 -> R.drawable.rank_exploration_7
            8 -> R.drawable.rank_exploration_8
            9 -> R.drawable.rank_exploration_9
            else -> R.drawable.rank_exploration_1
        }
    }

    fun getCqcRankDrawableId(rankValue: Int): Int {
        return when (rankValue + 1) {
            2 -> R.drawable.rank_cqc_2
            3 -> R.drawable.rank_cqc_3
            4 -> R.drawable.rank_cqc_4
            5 -> R.drawable.rank_cqc_5
            6 -> R.drawable.rank_cqc_6
            7 -> R.drawable.rank_cqc_7
            8 -> R.drawable.rank_cqc_8
            9 -> R.drawable.rank_cqc_9
            else -> R.drawable.rank_cqc_1
        }
    }

    fun getFederationRankDrawableId(rankValue: Int): Int {
        return when (rankValue + 1) {
            2 -> R.drawable.rank_fed_2
            3 -> R.drawable.rank_fed_3
            4 -> R.drawable.rank_fed_4
            5 -> R.drawable.rank_fed_5
            6 -> R.drawable.rank_fed_6
            7 -> R.drawable.rank_fed_7
            8 -> R.drawable.rank_fed_8
            9 -> R.drawable.rank_fed_9
            10 -> R.drawable.rank_fed_10
            11 -> R.drawable.rank_fed_11
            12 -> R.drawable.rank_fed_12
            13 -> R.drawable.rank_fed_13
            14 -> R.drawable.rank_fed_14
            else -> R.drawable.rank_fed_1
        }
    }

    fun getEmpireRankDrawableId(rankValue: Int): Int {
        return when (rankValue + 1) {
            2 -> R.drawable.rank_emp_2
            3 -> R.drawable.rank_emp_3
            4 -> R.drawable.rank_emp_4
            5 -> R.drawable.rank_emp_5
            6 -> R.drawable.rank_emp_6
            7 -> R.drawable.rank_emp_7
            8 -> R.drawable.rank_emp_8
            9 -> R.drawable.rank_emp_9
            10 -> R.drawable.rank_emp_10
            11 -> R.drawable.rank_emp_11
            12 -> R.drawable.rank_emp_12
            13 -> R.drawable.rank_emp_13
            14 -> R.drawable.rank_emp_14
            else -> R.drawable.rank_emp_1
        }
    }

    fun getDiscoveryBodyResources(bodyName: String, starType: String): Pair<Int, Int> {
        var stringResId = 0
        val drawableResId: Int

        if (starType.isNotEmpty()) {
            when (starType.toLowerCase(Locale.ROOT)) {
                "o" -> {
                    drawableResId = R.drawable.body_star_o
                    stringResId = R.string.body_star_o
                }
                "b" -> {
                    drawableResId = R.drawable.body_star_b
                    stringResId = R.string.body_star_b
                }
                "a" -> {
                    drawableResId = R.drawable.body_star_a
                    stringResId = R.string.body_star_a
                }
                "f" -> {
                    drawableResId = R.drawable.body_star_f
                    stringResId = R.string.body_star_f
                }
                "g" -> {
                    drawableResId = R.drawable.body_star_g
                    stringResId = R.string.body_star_g
                }
                "k" -> {
                    drawableResId = R.drawable.body_star_k
                    stringResId = R.string.body_star_k
                }
                "m" -> {
                    drawableResId = R.drawable.body_star_m
                    stringResId = R.string.body_star_m
                }
                "tts" -> {
                    drawableResId = R.drawable.body_star_tts
                    stringResId = R.string.body_star_tts
                }
                "supermassiveblackhole" -> {
                    drawableResId = R.drawable.body_black_hole_super_massive
                    stringResId = R.string.body_black_hole_super_massive
                }
                "l" -> {
                    drawableResId = R.drawable.body_star_l
                    stringResId = R.string.body_star_l
                }
                "t" -> {
                    drawableResId = R.drawable.body_star_t
                    stringResId = R.string.body_star_t
                }
                "y" -> {
                    drawableResId = R.drawable.body_star_y
                    stringResId = R.string.body_star_y
                }
                "d" -> {
                    drawableResId = R.drawable.body_star_d
                    stringResId = R.string.body_star_d
                }

                else -> {
                    drawableResId = R.drawable.body_unknown
                    println(starType)
                }
            }

        } else {
            when (bodyName.toLowerCase(Locale.ROOT)) {
                "water world" -> {
                    drawableResId = R.drawable.body_water_world
                    stringResId = R.string.body_water_world
                }
                "high metal content body" -> {
                    drawableResId = R.drawable.body_high_metal_content
                    stringResId = R.string.body_high_metal_content
                }
                "icy body" -> {
                    drawableResId = R.drawable.body_icy
                    stringResId = R.string.body_icy
                }
                "metal rich body" -> {
                    drawableResId = R.drawable.body_metal_rich
                    stringResId = R.string.body_metal_rich
                }
                "rocky body" -> {
                    drawableResId = R.drawable.body_rocky
                    stringResId = R.string.body_rocky
                }
                "rocky ice body" -> {
                    drawableResId = R.drawable.body_rocky_ice
                    stringResId = R.string.body_rocky_ice
                }
                "earthlike body" -> {
                    drawableResId = R.drawable.body_earthlike
                    stringResId = R.string.body_earthlike
                }
                "ammonia world" -> {
                    drawableResId = R.drawable.body_ammonia_world
                    stringResId = R.string.body_ammonia_world
                }
                "gas giant with water based life" -> {
                    drawableResId = R.drawable.body_giant_water_based
                    stringResId = R.string.body_giant_water_based
                }
                "gas giant with ammonia based life" -> {
                    drawableResId = R.drawable.body_giant_ammonia_based
                    stringResId = R.string.body_giant_ammonia_based
                }

                "sudarsky class i gas giant" -> {
                    drawableResId = R.drawable.body_sudarsky_class1
                    stringResId = R.string.body_sudarsky_class1
                }
                "sudarsky class ii gas giant" -> {
                    drawableResId = R.drawable.body_sudarsky_class2
                    stringResId = R.string.body_sudarsky_class2
                }
                "sudarsky class iii gas giant" -> {
                    drawableResId = R.drawable.body_sudarsky_class3
                    stringResId = R.string.body_sudarsky_class3
                }
                "sudarsky class iv gas giant" -> {
                    drawableResId = R.drawable.body_sudarsky_class4
                    stringResId = R.string.body_sudarsky_class4
                }
                "sudarsky class v gas giant" -> {
                    drawableResId = R.drawable.body_sudarsky_class5
                    stringResId = R.string.body_sudarsky_class5
                }

                else -> {
                    println(bodyName)
                    drawableResId = R.drawable.body_unknown
                }
            }
        }

        return stringResId to drawableResId
    }

}