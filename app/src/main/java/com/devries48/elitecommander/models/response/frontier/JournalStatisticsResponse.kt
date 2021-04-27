package com.devries48.elitecommander.models.response.frontier

import com.google.gson.annotations.SerializedName

data class JournalStatisticsResponse(
    @SerializedName("Bank_Account")
    val bankAccount: BankAccount,
    @SerializedName("Combat")
    val combat: Combat,
    @SerializedName("Crafting")
    val crafting: Crafting,
    @SerializedName("Crew")
    val crew: Crew,
    @SerializedName("Crime")
    val crime: Crime,
    @SerializedName("Exploration")
    val exploration: Exploration,
    @SerializedName("Material_Trader_Stats")
    val materialTraderStats: MaterialTraderStats,
    @SerializedName("Mining")
    val mining: Mining,
    @SerializedName("Multicrew")
    val multicrew: Multicrew,
    @SerializedName("Passengers")
    val passengers: Passengers,
    @SerializedName("Search_And_Rescue")
    val searchAndRescue: SearchAndRescue,
    @SerializedName("Smuggling")
    val smuggling: Smuggling,
    @SerializedName("TG_ENCOUNTERS")
    val tGENCOUNTERS: TGENCOUNTERS,
    @SerializedName("Trading")
    val trading: Trading
) : JournalResponseBase() {

    data class BankAccount(
        @SerializedName("Current_Wealth")
        val currentWealth: Long,
        @SerializedName("Insurance_Claims")
        val insuranceClaims: Int,
        @SerializedName("Owned_Ship_Count")
        val ownedShipCount: Int,
        @SerializedName("Spent_On_Ammo_Consumables")
        val spentOnAmmoConsumables: Long,
        @SerializedName("Spent_On_Fuel")
        val spentOnFuel: Long,
        @SerializedName("Spent_On_Insurance")
        val spentOnInsurance: Long,
        @SerializedName("Spent_On_Outfitting")
        val spentOnOutfitting: Long,
        @SerializedName("Spent_On_Repairs")
        val spentOnRepairs: Long,
        @SerializedName("Spent_On_Ships")
        val spentOnShips: Long
    )

    data class Combat(
        @SerializedName("Assassination_Profits")
        val assassinationProfits: Long,
        @SerializedName("Assassinations")
        val assassinations: Int,
        @SerializedName("Bounties_Claimed")
        val bountiesClaimed: Int,
        @SerializedName("Bounty_Hunting_Profit")
        val bountyHuntingProfit: Long,
        @SerializedName("Combat_Bond_Profits")
        val combatBondProfits: Long,
        @SerializedName("Combat_Bonds")
        val combatBonds: Int,
        @SerializedName("Highest_Single_Reward")
        val highestSingleReward: Int,
        @SerializedName("Skimmers_Killed")
        val skimmersKilled: Int
    )

    data class Crafting(
        @SerializedName("Count_Of_Used_Engineers")
        val countOfUsedEngineers: Int, // 8
        @SerializedName("Recipes_Generated")
        val recipesGenerated: Int, // 423
        @SerializedName("Recipes_Generated_Rank_1")
        val recipesGeneratedRank1: Int, // 136
        @SerializedName("Recipes_Generated_Rank_2")
        val recipesGeneratedRank2: Int, // 101
        @SerializedName("Recipes_Generated_Rank_3")
        val recipesGeneratedRank3: Int, // 92
        @SerializedName("Recipes_Generated_Rank_4")
        val recipesGeneratedRank4: Int, // 65
        @SerializedName("Recipes_Generated_Rank_5")
        val recipesGeneratedRank5: Int // 29
    )

    data class Crew(
        @SerializedName("NpcCrew_Fired")
        val npcCrewFired: Int, // 6
        @SerializedName("NpcCrew_Hired")
        val npcCrewHired: Int, // 6
        @SerializedName("NpcCrew_TotalWages")
        val npcCrewTotalWages: Long // 51906960
    )

    data class Crime(
        @SerializedName("Bounties_Received")
        val bountiesReceived: Int,
        @SerializedName("Fines")
        val fines: Int,
        @SerializedName("Highest_Bounty")
        val highestBounty: Int,
        @SerializedName("Notoriety")
        val notoriety: Int,
        @SerializedName("Total_Bounties")
        val totalBounties: Long,
        @SerializedName("Total_Fines")
        val totalFines: Long
    )

    data class Exploration(
        @SerializedName("Efficient_Scans")
        val efficientScans: Int,
        @SerializedName("Exploration_Profits")
        val explorationProfits: Long,
        @SerializedName("Greatest_Distance_From_Start")
        val greatestDistanceFromStart: Double,
        @SerializedName("Highest_Payout")
        val highestPayout: Int,
        @SerializedName("Planets_Scanned_To_Level_2")
        val planetsScannedToLevel2: Int,
        @SerializedName("Planets_Scanned_To_Level_3")
        val planetsScannedToLevel3: Int,
        @SerializedName("Systems_Visited")
        val systemsVisited: Int,
        @SerializedName("Time_Played")
        val timePlayed: Int,
        @SerializedName("Total_Hyperspace_Distance")
        val totalHyperspaceDistance: Int,
        @SerializedName("Total_Hyperspace_Jumps")
        val totalHyperspaceJumps: Int
    )

    data class MaterialTraderStats(
        @SerializedName("Encoded_Materials_Traded")
        val encodedMaterialsTraded: Int, // 98
        @SerializedName("Grade_1_Materials_Traded")
        val grade1MaterialsTraded: Int, // 87
        @SerializedName("Grade_2_Materials_Traded")
        val grade2MaterialsTraded: Int, // 317
        @SerializedName("Grade_3_Materials_Traded")
        val grade3MaterialsTraded: Int, // 154
        @SerializedName("Grade_4_Materials_Traded")
        val grade4MaterialsTraded: Int, // 87
        @SerializedName("Materials_Traded")
        val materialsTraded: Int, // 645
        @SerializedName("Raw_Materials_Traded")
        val rawMaterialsTraded: Int, // 522
        @SerializedName("Trades_Completed")
        val tradesCompleted: Int // 10
    )

    data class Mining(
        @SerializedName("Materials_Collected")
        val materialsCollected: Int, // 7317
        @SerializedName("Mining_Profits")
        val miningProfits: Long, // 14267710
        @SerializedName("Quantity_Mined")
        val quantityMined: Int // 326
    )

    data class Multicrew(
        @SerializedName("Multicrew_Credits_Total")
        val multicrewCreditsTotal: Long,
        @SerializedName("Multicrew_Fighter_Time_Total")
        val multicrewFighterTimeTotal: Int,
        @SerializedName("Multicrew_Fines_Total")
        val multicrewFinesTotal: Int,
        @SerializedName("Multicrew_Gunner_Time_Total")
        val multicrewGunnerTimeTotal: Int,
        @SerializedName("Multicrew_Time_Total")
        val multicrewTimeTotal: Int
    )

    data class Passengers(
        @SerializedName("Passengers_Missions_Accepted")
        val passengersMissionsAccepted: Int, // 56
        @SerializedName("Passengers_Missions_Bulk")
        val passengersMissionsBulk: Int, // 222
        @SerializedName("Passengers_Missions_Delivered")
        val passengersMissionsDelivered: Int, // 367
        @SerializedName("Passengers_Missions_Disgruntled")
        val passengersMissionsDisgruntled: Int, // 3
        @SerializedName("Passengers_Missions_Ejected")
        val passengersMissionsEjected: Int, // 17
        @SerializedName("Passengers_Missions_VIP")
        val passengersMissionsVIP: Int // 145
    )

    data class SearchAndRescue(
        @SerializedName("SearchRescue_Count")
        val searchRescueCount: Int,
        @SerializedName("SearchRescue_Profit")
        val searchRescueProfit: Long,
        @SerializedName("SearchRescue_Traded")
        val searchRescueTraded: Int
    )

    data class Smuggling(
        @SerializedName("Average_Profit")
        val averageProfit: Int, // 16920
        @SerializedName("Black_Markets_Profits")
        val blackMarketsProfits: Long, // 33840
        @SerializedName("Black_Markets_Traded_With")
        val blackMarketsTradedWith: Int, // 2
        @SerializedName("Highest_Single_Transaction")
        val highestSingleTransaction: Long, // 33354
        @SerializedName("Resources_Smuggled")
        val resourcesSmuggled: Int // 4
    )

    data class TGENCOUNTERS(
        @SerializedName("TG_ENCOUNTER_TOTAL")
        val tGENCOUNTERTOTAL: Int, // 2
        @SerializedName("TG_ENCOUNTER_TOTAL_LAST_SHIP")
        val tGENCOUNTERTOTALLASTSHIP: String, // Dolphin
        @SerializedName("TG_ENCOUNTER_TOTAL_LAST_SYSTEM")
        val tGENCOUNTERTOTALLASTSYSTEM: String, // Witch Head Sector MN-T c3-1
        @SerializedName("TG_ENCOUNTER_TOTAL_LAST_TIMESTAMP")
        val tGENCOUNTERTOTALLASTTIMESTAMP: String, // 3306-11-23 16:58
        @SerializedName("TG_SCOUT_COUNT")
        val tGSCOUTCOUNT: Int // 2
    )

    data class Trading(
        @SerializedName("Average_Profit")
        val averageProfit: Double, // 1122471.1491366
        @SerializedName("Highest_Single_Transaction")
        val highestSingleTransaction: Int, // 8364224
        @SerializedName("Market_Profits")
        val marketProfits: Long, // 715014122
        @SerializedName("Markets_Traded_With")
        val marketsTradedWith: Int, // 114
        @SerializedName("Resources_Traded")
        val resourcesTraded: Int // 171149
    )
}