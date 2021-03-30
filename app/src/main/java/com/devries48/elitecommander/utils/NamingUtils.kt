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

    fun getAllianceRankDrawableId(): Int {
        return R.drawable.rank_alliance_1
    }


    /* List of number aliases for star types received from Frontier.
    * Returns the corresponding value of the requested star type. */
    fun getStarTypeAlias(starType: String): Int {
        return when (starType.toLowerCase(Locale.ROOT)) {
            "o" -> 1
            "b" -> 2
            "b_bluewhitesupergiant" -> 201
            "a" -> 3
            "a_bluewhitesupergiant" -> 301
            "f" -> 4
            "f_whitesupergiant" -> 401
            "g" -> 5
            "g_whitesupergiant" -> 5001
            "k" -> 6
            "k_orangegiant" -> 601
            "m" -> 7
            "m_redgiant" -> 701
            "m_redsupergiant" -> 702

            "l" -> 8
            "t" -> 9
            "y" -> 10

            // Proto stars
            "tts" -> 11
            "aebe" -> 12

            // Wolf-Rayet
            "w" -> 21
            "wn" -> 22
            "wnc" -> 23
            "wc" -> 24
            "wo" -> 25

            // carbon stars
            "cs" -> 31
            "c" -> 32
            "cn" -> 33
            "cj" -> 34
            "ch" -> 35
            "chd" -> 36
            "ms" -> 41
            "s" -> 42

            // white dwarfs
            "d" -> 51
            "da" -> 501
            "dab" -> 502
            "dao" -> 503
            "daz" -> 504
            "dav" -> 505
            "db" -> 506
            "dbz" -> 507
            "dbv" -> 508
            "do" -> 509
            "dov" -> 510
            "dq" -> 511
            "dc" -> 512
            "dcv" -> 513
            "dx" -> 514

            // Others
            "n" -> 91
            "h" -> 92
            "supermassiveblackhole" -> 93
            "x" -> 94
            "rogueplanet" -> 111
            "nebula" -> 112
            "stellarremnantnebula" -> 113
            else -> {
                println("Unknown star type: $starType")
                0
            }
        }
    }

    /* List of number aliases for planet types received from Frontier.
    * Returns the corresponding value of the requested star type. */
    fun getPlanetBodyAlias(planetBodyType: String): Int {
        return when (planetBodyType.toLowerCase(Locale.ROOT)) {
            "metal-rich body", "metal rich body" -> 1
            "high metal content body", "high metal content world" -> 2
            "rocky body" -> 11
            "rocky ice body" -> 12
            "rocky ice world" -> 12
            "icy body" -> 21
            "earthlike body", "earthlike", "earth-like world" -> 31
            "water world" -> 41
            "water giant" -> 42
            "water giant with life" -> 43
            "ammonia world" -> 51
            "gas giant with water based life", "gas giant with water-based life" -> 61
            "gas giant with ammonia based life", "gas giant with ammonia-based life" -> 62
            "sudarsky class i gas giant", "class i gas giant" -> 71
            "sudarsky class ii gas giant", "class ii gas giant" -> 72
            "sudarsky class iii gas giant", "class iii gas giant" -> 73
            "sudarsky class iv gas giant", "class iv gas giant" -> 74
            "sudarsky class v gas giant", "class v gas giant" -> 75
            "helium rich gas giant", "helium-rich gas giant" -> 81
            "helium gas giant" -> 82
            else -> {
                println("Unknown planet: $planetBodyType")
                0
            }
        }
    }

    // returns <drawable, string> Pair
    fun getDiscoveryBodyResources(bodyName: String, starType: String): Pair<Int, Int> {
        return if (starType.isNotEmpty() && starType.isNotBlank())
            getStarResources(starType)
        else getPlanetResources(bodyName)
    }

    private fun getStarResources(starType: String): Pair<Int, Int> {
        when (val id = getStarTypeAlias(starType)) {
            1 -> return R.drawable.body_star_o to R.string.body_star_o
            2, 201 -> return R.drawable.body_star_b to if (id == 2) R.string.body_star_b else R.string.body_star_b_blue_white_supergiant
            3, 301 -> return R.drawable.body_star_a to if (id == 3) R.string.body_star_a else R.string.body_star_a_blue_white_supergiant
            4, 401 -> return R.drawable.body_star_f to if (id == 4) R.string.body_star_f else R.string.body_star_f_whitesupergiant
            5, 5001 -> return R.drawable.body_star_g to if (id == 5) R.string.body_star_g else R.string.body_star_g_white_yellow_supergiant
            6, 601 -> return R.drawable.body_star_k to if (id == 5) R.string.body_star_k else R.string.body_star_k_yellow_orange_giant
            7, 701, 702 ->
                return R.drawable.body_star_m to
                        when (id) {
                            7 -> R.string.body_star_m
                            701 -> R.string.body_star_m_red_giant
                            else -> R.string.body_star_m_red_super_giant
                        }
            8 -> return R.drawable.body_star_l to R.string.body_star_l
            9 -> return R.drawable.body_star_t to R.string.body_star_t
            10 -> return R.drawable.body_star_y to R.string.body_star_y
            11 -> return R.drawable.body_star_tts to R.string.body_star_tts
            12 -> return R.drawable.body_star_aebe to R.string.body_star_aebe
            21, 22, 23, 24, 25 ->
                return R.drawable.body_star_wolf to
                        when (id) {
                            21 -> R.string.body_star_wolf
                            22 -> R.string.body_star_wolf_n
                            23 -> R.string.body_star_wolf_nc
                            24 -> R.string.body_star_wolf_c
                            else -> R.string.body_star_wolf_o
                        }
            31, 32, 33, 34, 35, 36, 41, 42 ->
                return R.drawable.body_star_c to
                        when (id) {
                            31 -> R.string.body_star_cs
                            32 -> R.string.body_star_c
                            33 -> R.string.body_star_cn
                            34 -> R.string.body_star_cj
                            35 -> R.string.body_star_ch
                            36 -> R.string.body_star_chd
                            41 -> R.string.body_star_ms
                            else -> R.string.body_star_s
                        }
            51, 501, 502, 503, 504, 505, 506, 507, 508, 509, 510, 511, 512, 513, 514 ->
                return R.drawable.body_star_d to
                        when (id) {
                            51 -> R.string.body_star_d
                            501 -> R.string.body_star_da
                            502 -> R.string.body_star_dab
                            503 -> R.string.body_star_dao
                            504 -> R.string.body_star_daz
                            505 -> R.string.body_star_dav
                            506 -> R.string.body_star_db
                            507 -> R.string.body_star_dbz
                            508 -> R.string.body_star_dbv
                            509 -> R.string.body_star_do
                            510 -> R.string.body_star_dov
                            511 -> R.string.body_star_dq
                            512 -> R.string.body_star_dc
                            513 -> R.string.body_star_dcw
                            else -> R.string.body_star_dx
                        }
            91 -> return R.drawable.body_star_neutron to R.string.body_star_neutron
            92 -> return R.drawable.body_black_hole to R.string.body_black_hole
            93 -> return R.drawable.body_black_hole_super_massive to R.string.body_black_hole_super_massive
            else -> {
                println(starType)
                return R.drawable.body_unknown to 0
            }
        }
    }

    private fun getPlanetResources(bodyName: String): Pair<Int, Int> {
        when (getPlanetBodyAlias(bodyName)) {
            1 -> return R.drawable.body_metal_rich to R.string.body_metal_rich
            2 -> return R.drawable.body_high_metal_content to R.string.body_high_metal_content
            11 -> return R.drawable.body_rocky to R.string.body_rocky
            12 -> return R.drawable.body_rocky_ice to R.string.body_rocky_ice
            21 -> return R.drawable.body_icy to R.string.body_icy
            31 -> return R.drawable.body_earthlike to R.string.body_earthlike
            41 -> return R.drawable.body_water_world to R.string.body_water_world
            42 -> return R.drawable.body_water_giant to R.string.body_water_giant
            51 -> return R.drawable.body_ammonia_world to R.string.body_ammonia_world
            61 -> return R.drawable.body_giant_water_based to R.string.body_giant_water_based
            62 -> return R.drawable.body_giant_ammonia_based to R.string.body_giant_ammonia_based
            71 -> return R.drawable.body_sudarski_class1 to R.string.body_sudarsky_class1
            72 -> return R.drawable.body_sudarski_class2 to R.string.body_sudarsky_class2
            73 -> return R.drawable.body_sudarski_class3 to R.string.body_sudarsky_class3
            74 -> return R.drawable.body_sudarski_class4 to R.string.body_sudarsky_class4
            75 -> return R.drawable.body_sudarski_class5 to R.string.body_sudarsky_class5
            81, 82 -> return R.drawable.body_helium_giant to R.string.body_helium_giant
            else -> {
                println(bodyName)
                return R.drawable.body_unknown to 0
            }
        }
    }


}