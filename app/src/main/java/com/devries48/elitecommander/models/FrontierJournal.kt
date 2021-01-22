package com.devries48.elitecommander.models

import android.content.Context
import com.devries48.elitecommander.R
import com.devries48.elitecommander.events.FrontierRanksEvent
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName

class FrontierJournal {

    fun parseResponse(response: String) {

        val response1 = """{
  "timestamp": "2021-01-17T17:25:36Z",
  "event": "Statistics",
  "Bank_Account": {
    "Current_Wealth": 1859926914,
    "Spent_On_Ships": 176881313,
    "Spent_On_Outfitting": 299547117,
    "Spent_On_Repairs": 3600668,
    "Spent_On_Fuel": 616088,
    "Spent_On_Ammo_Consumables": 661191,
    "Insurance_Claims": 24,
    "Spent_On_Insurance": 30282726,
    "Owned_Ship_Count": 9
  },
  "Combat": {
    "Bounties_Claimed": 180,
    "Bounty_Hunting_Profit": 3938978,
    "Combat_Bonds": 276,
    "Combat_Bond_Profits": 7138468,
    "Assassinations": 9,
    "Assassination_Profits": 1477104,
    "Highest_Single_Reward": 106400,
    "Skimmers_Killed": 14
  },
  "Crime": {
    "Notoriety": 0,
    "Fines": 35,
    "Total_Fines": 665079,
    "Bounties_Received": 62,
    "Total_Bounties": 49500,
    "Highest_Bounty": 10000
  },
  "Smuggling": {
    "Black_Markets_Traded_With": 2,
    "Black_Markets_Profits": 33840,
    "Resources_Smuggled": 4,
    "Average_Profit": 16920,
    "Highest_Single_Transaction": 33354
  },
  "Trading": {
    "Markets_Traded_With": 114,
    "Market_Profits": 715014122,
    "Resources_Traded": 171149,
    "Average_Profit": 1122471.1491366,
    "Highest_Single_Transaction": 8364224
  },
  "Mining": {
    "Mining_Profits": 14267710,
    "Quantity_Mined": 326,
    "Materials_Collected": 7353
  },
  "Exploration": {
    "Systems_Visited": 5423,
    "Exploration_Profits": 897847545,
    "Planets_Scanned_To_Level_2": 51953,
    "Planets_Scanned_To_Level_3": 51953,
    "Efficient_Scans": 731,
    "Highest_Payout": 25271392,
    "Total_Hyperspace_Distance": 163694,
    "Total_Hyperspace_Jumps": 9674,
    "Greatest_Distance_From_Start": 25868.701225907,
    "Time_Played": 3787560
  },
  "Passengers": {
    "Passengers_Missions_Accepted": 56,
    "Passengers_Missions_Disgruntled": 3,
    "Passengers_Missions_Bulk": 222,
    "Passengers_Missions_VIP": 145,
    "Passengers_Missions_Delivered": 367,
    "Passengers_Missions_Ejected": 17
  },
  "Search_And_Rescue": {
    "SearchRescue_Traded": 10,
    "SearchRescue_Profit": 230502,
    "SearchRescue_Count": 9
  },
  "TG_ENCOUNTERS": {
    "TG_ENCOUNTER_TOTAL": 2,
    "TG_ENCOUNTER_TOTAL_LAST_SYSTEM": "Witch Head Sector MN-T c3-1",
    "TG_ENCOUNTER_TOTAL_LAST_TIMESTAMP": "3306-11-23 16:58",
    "TG_ENCOUNTER_TOTAL_LAST_SHIP": "Dolphin",
    "TG_SCOUT_COUNT": 2
  },
  "Crafting": {
    "Count_Of_Used_Engineers": 8,
    "Recipes_Generated": 423,
    "Recipes_Generated_Rank_1": 136,
    "Recipes_Generated_Rank_2": 101,
    "Recipes_Generated_Rank_3": 92,
    "Recipes_Generated_Rank_4": 65,
    "Recipes_Generated_Rank_5": 29
  },
  "Crew": {
    "NpcCrew_TotalWages": 51906960,
    "NpcCrew_Hired": 6,
    "NpcCrew_Fired": 6
  },
  "Multicrew": {
    "Multicrew_Time_Total": 0,
    "Multicrew_Gunner_Time_Total": 0,
    "Multicrew_Fighter_Time_Total": 0,
    "Multicrew_Credits_Total": 0,
    "Multicrew_Fines_Total": 0
  },
  "Material_Trader_Stats": {
    "Trades_Completed": 10,
    "Materials_Traded": 645,
    "Encoded_Materials_Traded": 98,
    "Raw_Materials_Traded": 522,
    "Grade_1_Materials_Traded": 87,
    "Grade_2_Materials_Traded": 317,
    "Grade_3_Materials_Traded": 154,
    "Grade_4_Materials_Traded": 87
  }
}{
  "timestamp": "2021-01-17T20:29:49Z",
  "event": "MaterialCollected",
  "Category": "Raw",
  "Name": "germanium",
  "Count": 3
}{
  "timestamp": "2021-01-17T20:30:13Z",
  "event": "MaterialCollected",
  "Category": "Raw",
  "Name": "germanium",
  "Count": 3
}{
  "timestamp": "2021-01-17T20:30:28Z",
  "event": "CodexEntry",
  "EntryID": 1400158,
  "Name": "${"$"}Codex_Ent_IceFumarole_WaterGeysers_Name;",
  "Name_Localised": "Water Ice Fumarole",
  "SubCategory": "${"$"}Codex_SubCategory_Geology_and_Anomalies;",
  "SubCategory_Localised": "Geology and anomalies",
  "Category": "${"$"}Codex_Category_Biology;",
  "Category_Localised": "Biological and Geological",
  "Region": "${"$"}Codex_RegionName_2;",
  "Region_Localised": "Empyrean Straits",
  "System": "Eorld Pri OO-B c16-2017",
  "SystemAddress": 554512389415562,
  "NearestDestination": "${"$"}SAA_Unknown_Signal:#type=${"$"}SAA_SignalType_Geological;:#index=33;",
  "NearestDestination_Localised": "Geological Signal (33)"
}{
  "timestamp": "2021-01-17T20:31:02Z",
  "event": "MaterialCollected",
  "Category": "Raw",
  "Name": "carbon",
  "Count": 3
}{
  "timestamp": "2021-01-17T20:32:16Z",
  "event": "DockSRV",
  "ID": 14}{
  "timestamp": "2021-01-17T17:25:38Z",
  "event": "Rank",
  "Combat": 4,
  "Trade": 7,
  "Explore": 8,
  "Empire": 2,
  "Federation": 8,
  "CQC": 0
}{
  "timestamp": "2021-01-17T17:25:38Z",
  "event": "Progress",
  "Combat": 13,
  "Trade": 83,
  "Explore": 100,
  "Empire": 100,
  "Federation": 35,
  "CQC": 0
}
"""

        // Couldn't get RegEx working here (worked on Kotlin Playground ...
        // ... So fix the response by removing the first and last curly brace
        response1.trim().drop(1).dropLast(1).split("}{").map {
            mRawEvents.add(RawEvent(it.trim()))
        }
        println("Journal events present: " + mRawEvents.size)
    }

    fun getRanks(context: Context): FrontierRanksEvent {
        try {
            val rawRank = mRawEvents.firstOrNull { it.event == JOURNAL_EVENT_RANK }
            val rawProgress = mRawEvents.firstOrNull { it.event == JOURNAL_EVENT_PROGRESS }

            if (rawRank == null || rawProgress == null) {
                throw error("Error parsing rank events from journal")
            }

            val rank = Gson().fromJson(rawRank.json, FrontierJournalRank::class.java)
            val progress =
                Gson().fromJson(rawProgress.json, FrontierJournalRankProgress::class.java)

            val combatRank = FrontierRanksEvent.FrontierRank(
                context.resources.getStringArray(R.array.ranks_combat)[rank.combat],
                rank.combat,
                progress.combat
            )
            val tradeRank = FrontierRanksEvent.FrontierRank(
                context.resources.getStringArray(R.array.ranks_trade)[rank.trade],
                rank.trade,
                progress.trade
            )
            val exploreRank = FrontierRanksEvent.FrontierRank(
                context.resources.getStringArray(R.array.ranks_explorer)[rank.explore],
                rank.explore,
                progress.explore
            )
            val cqcRank = FrontierRanksEvent.FrontierRank(
                context.resources.getStringArray(R.array.ranks_cqc)[rank.cqc],
                rank.cqc,
                progress.cqc
            )
            val federationRank = FrontierRanksEvent.FrontierRank(
                context.resources.getStringArray(R.array.ranks_federation)[rank.federation],
                rank.federation,
                progress.federation
            )
            val empireRank = FrontierRanksEvent.FrontierRank(
                context.resources.getStringArray(R.array.ranks_empire)[rank.empire],
                rank.empire,
                progress.empire
            )

            return FrontierRanksEvent(
                true, combatRank, tradeRank, exploreRank,
                cqcRank, federationRank, empireRank
            )
        } catch (e: Exception) {
            println("LOG: Error parsing ranking events from journal." + e.message)
            return FrontierRanksEvent(
                false, null, null,
                null, null, null, null
            )

        }
    }

    fun getStatistics(): FrontierJournalStatistics? {
        val event = mRawEvents.firstOrNull { it.event == JOURNAL_EVENT_STATISTICS }

        if (event != null) {
            return Gson().fromJson(
                event.json,
                FrontierJournalStatistics::class.java
            )
        }
        return null
    }

    private class RawEvent(private var value: String) {
        var event: String
        val json: JsonObject = JsonParser.parseString("{$value}").asJsonObject

        init {
            event = json.get("event").asString
            println("Journal event: $event")
        }
    }

    companion object {
        const val JOURNAL_EVENT_STATISTICS = "Statistics"
        private const val JOURNAL_EVENT_RANK = "Rank"
        private const val JOURNAL_EVENT_PROGRESS = "Progress"

        private val mRawEvents: MutableList<RawEvent> = ArrayList()


    }
}

abstract class FrontierJournalBase

class FrontierJournalCommander : FrontierJournalBase()

class FrontierJournalStatistics : FrontierJournalBase() {
    @SerializedName("Bank_Account")
    var bankAccount: BankAccount? = null

    @SerializedName("Combat")
    var combat: Combat? = null

    @SerializedName("Smuggling")
    var smuggling: Smuggling? = null

    inner class BankAccount {
        @SerializedName("Current_Wealth")
        var currentWealth: Long = 0

        @SerializedName("Spent_On_Ships")
        var spentOnShips: Long = 0

        @SerializedName("Spent_On_Outfitting")
        var spentOnOutfitting: Long = 0

        @SerializedName("Spent_On_Repairs")
        var spentOnRepairs: Long = 0

        @SerializedName("Spent_On_Fuel")
        var spentOnFuel: Long = 0

        @SerializedName("spentOnAmmoConsumables")
        var spentOnAmmoConsumables: Long = 0

        @SerializedName("Insurance_Claims")
        var insuranceClaims: Int = 0

        @SerializedName("Spent_On_Insurance")
        var spentOnInsurance: Long = 0

        @SerializedName("Owned_Ship_Count")
        var ownedShipCount: Int = 0
    }

    inner class Combat {
        @SerializedName("Bounties_Claimed")
        var bountiesClaimed: Int = 0

        @SerializedName("Bounty_Hunting_Profit")
        var bountyHuntingProfit: Long = 0

        @SerializedName("Combat_Bonds")
        var combatBonds: Int = 0

        @SerializedName("Combat_Bond_Profits")
        var combatBondProfits: Long = 0

        @SerializedName("Assassinations")
        var assassinations: Int = 0

        @SerializedName("Assassination_Profits")
        var assassinationProfits: Long = 0

        @SerializedName("Highest_Single_Reward")
        var highestSingleReward: Long = 0

        @SerializedName("Skimmers_Killed")
        var skimmersKilled: Int = 0
    }

    inner class Smuggling {
        @SerializedName("Black_Markets_Traded_With")
        var blackMarketsTradedWith: Int = 0

        @SerializedName("Black_Markets_Profits")
        var blackMarketsProfits: Long = 0

        @SerializedName("Resources_Smuggled")
        var resourcesSmuggled: Int = 0

        @SerializedName("Average_Profit")
        var averageProfit: Long = 0

        @SerializedName("Highest_Single_Transaction")
        var highestSingleTransaction: Long = 0
    }
}

class FrontierJournalRank : FrontierJournalBase() {
    @SerializedName("Combat")
    var combat: Int = 0

    @SerializedName("Trade")
    var trade: Int = 0

    @SerializedName("Explore")
    var explore: Int = 0

    @SerializedName("Empire")
    var empire: Int = 0

    @SerializedName("Federation")
    var federation: Int = 0

    @SerializedName("CQC")
    var cqc: Int = 0
}

class FrontierJournalRankProgress : FrontierJournalBase() {
    @SerializedName("Combat")
    var combat: Int = 0

    @SerializedName("Trade")
    var trade: Int = 0

    @SerializedName("Explore")
    var explore: Int = 0

    @SerializedName("Empire")
    var empire: Int = 0

    @SerializedName("Federation")
    var federation: Int = 0

    @SerializedName("CQC")
    var cqc: Int = 0
}

