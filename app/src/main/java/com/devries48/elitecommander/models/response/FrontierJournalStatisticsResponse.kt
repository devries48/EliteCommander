package com.devries48.elitecommander.models.response

import com.google.gson.annotations.SerializedName

class FrontierJournalStatisticsResponse : FrontierJournalResponseBase() {
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