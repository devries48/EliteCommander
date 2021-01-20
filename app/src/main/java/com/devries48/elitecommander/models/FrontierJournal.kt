package com.devries48.elitecommander.models

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

    /**
     *  Capture FrontierRanksEvent for the result.
     */
    fun GetRanks()
    {
        val rankEvent = mRawEvents.firstOrNull { it.event == JOURNAL_EVENT_RANK }
        val progressEvent = mRawEvents.firstOrNull { it.event == JOURNAL_EVENT_PROGRESS }
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
        const val JOURNAL_EVENT_RANK = "Rank"
        const val JOURNAL_EVENT_PROGRESS = "Progress"

        private val mRawEvents: MutableList<RawEvent> = ArrayList()
    }
}

abstract class FrontierJournalBase

class FrontierJournalStatistics : FrontierJournalBase() {
    @SerializedName("Bank_Account")
     var bankAccount: BankAccount?=null

    @SerializedName("Combat")
     var combat: Combat?=null

    @SerializedName("Smuggling")
     var smuggling: Smuggling?=null

    inner class BankAccount {
        @SerializedName("Current_Wealth")
        var currentWealth: Long = 0

        @SerializedName("Spent_On_Ships")
        val spentOnShips: Long = 0

        @SerializedName("Spent_On_Outfitting")
        val spentOnOutfitting: Long = 0

        @SerializedName("Spent_On_Repairs")
        val spentOnRepairs: Long = 0

        @SerializedName("Spent_On_Fuel")
        val spentOnFuel: Long = 0

        @SerializedName("spentOnAmmoConsumables")
        val spentOnAmmoConsumables: Long = 0

        @SerializedName("Insurance_Claims")
        val insuranceClaims: Int = 0

        @SerializedName("Spent_On_Insurance")
        val spentOnInsurance: Long = 0

        @SerializedName("Owned_Ship_Count")
        val ownedShipCount: Int = 0
    }

     inner class Combat {
         @SerializedName("Bounties_Claimed")
         val bountiesClaimed: Int=0
         @SerializedName("Bounty_Hunting_Profit")
         val bountyHuntingProfit: Long=0
         @SerializedName("Combat_Bonds")
         val combatBonds: Int=0
         @SerializedName("Combat_Bond_Profits")
         val combatBondProfits: Long=0
         @SerializedName("Assassinations")
         val assassinations: Int=0
         @SerializedName("Assassination_Profits")
         val assassinationProfits: Long=0
         @SerializedName("Highest_Single_Reward")
         val highestSingleReward: Long=0
         @SerializedName("Skimmers_Killed")
         val skimmersKilled: Int=0
     }

    inner class Smuggling(
        @SerializedName("blackMarketsTradedWith") val blackMarketsTradedWith: Int,
        @SerializedName("blackMarketsProfits") val blackMarketsProfits: Long,
        @SerializedName("resourcesSmuggled") val resourcesSmuggled: Int,
        @SerializedName("averageProfit") val averageProfit: Long,
        @SerializedName("highestSingleTransaction") val highestSingleTransaction: Long
    )



}

class FrontierJournalRank : FrontierJournalBase() {
    @SerializedName("Combat")
    val combat: Int = 0
    @SerializedName("Trade")
    val trade: Int = 0
    @SerializedName("Explore")
    val explore: Int = 0
    @SerializedName("Empire")
    val empire: Int = 0
    @SerializedName("Federation")
    val federation: Int = 0
    @SerializedName("CQC")
    val cqc: Int = 0
}

class FrontierJournalRankProgress : FrontierJournalBase() {
    @SerializedName("Combat")
    val combat: Int = 0
    @SerializedName("Trade")
    val trade: Int = 0
    @SerializedName("Explore")
    val explore: Int = 0
    @SerializedName("Empire")
    val empire: Int = 0
    @SerializedName("Federation")
    val federation: Int = 0
    @SerializedName("CQC")
    val cqc: Int = 0
}




class FrontierJournalCommander : FrontierJournalBase()
