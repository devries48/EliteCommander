package com.devries48.elitecommander.fragments

import android.annotation.SuppressLint
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.devries48.elitecommander.R
import com.devries48.elitecommander.declarations.default
import com.devries48.elitecommander.events.*
import com.devries48.elitecommander.models.*
import com.devries48.elitecommander.models.ProfitModel.ProfitType.*
import com.devries48.elitecommander.models.StatisticsBuilder.Companion.StatisticColor.*
import com.devries48.elitecommander.models.StatisticsBuilder.Companion.StatisticFormat.*
import com.devries48.elitecommander.models.StatisticsBuilder.Companion.StatisticPosition.*
import com.devries48.elitecommander.models.StatisticsBuilder.Companion.StatisticType.*
import com.devries48.elitecommander.network.CommanderClient
import com.devries48.elitecommander.utils.SettingsUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.math.round

class CommanderViewModel(client: CommanderClient?) : ViewModel() {

    //<editor-fold desc="Private definitions">

    private val mCommanderApi = client
    private val mStatisticSettings: StatisticSettingsModel
    private var mCurrentSettings = StatisticSettingsModel()

    private val mName = MutableLiveData("")
    private val mNotoriety = MutableLiveData(0)
    private val mIsRanksBusy = MutableLiveData<Boolean>().default(true)
    private val mIsCmdrBusy = MutableLiveData<Boolean>().default(true)

    private val mCombatRank = MutableLiveData(RankModel(RankModel.RankType.COMBAT, FrontierRanksEvent.FrontierRank()))
    private val mTradeRank = MutableLiveData(RankModel(RankModel.RankType.TRADING, FrontierRanksEvent.FrontierRank()))
    private val mExploreRank =
        MutableLiveData(RankModel(RankModel.RankType.EXPLORATION, FrontierRanksEvent.FrontierRank()))
    private val mCqcRank = MutableLiveData(RankModel(RankModel.RankType.CQC, FrontierRanksEvent.FrontierRank()))
    private val mFederationRank =
        MutableLiveData(RankModel(RankModel.RankType.FEDERATION, FrontierRanksEvent.FrontierRank()))
    private val mEmpireRank =
        MutableLiveData(RankModel(RankModel.RankType.EMPIRE, FrontierRanksEvent.FrontierRank()))
    private val mAllianceRank =
        MutableLiveData(RankModel(RankModel.RankType.ALLIANCE, FrontierRanksEvent.FrontierRank()))

    private var mCurrentDiscoveries = MutableLiveData<List<FrontierDiscovery>>()
    private var mCurrentDiscoverySummary = MutableLiveData(FrontierDiscoverySummary(0, 0, 0, 0, 0, 0, 0, 0, 0, 0))

    private var mProfitChart = MutableLiveData<List<ProfitModel>>()

    private val mBuilderProfit: StatisticsBuilder = StatisticsBuilder()
    private val mBuilderMain: StatisticsBuilder = StatisticsBuilder()
    private val mBuilderCombat: StatisticsBuilder = StatisticsBuilder()
    private val mBuilderExploration: StatisticsBuilder = StatisticsBuilder()
    private val mBuilderPassenger: StatisticsBuilder = StatisticsBuilder()

    //</editor-fold>

    val name: LiveData<String> = mName
    val notoriety: LiveData<Int> = mNotoriety
    val combatRank: LiveData<RankModel> = mCombatRank
    val tradeRank: LiveData<RankModel> = mTradeRank
    val exploreRank: LiveData<RankModel> = mExploreRank
    val cqcRank: LiveData<RankModel> = mCqcRank
    val federationRank: LiveData<RankModel> = mFederationRank
    val empireRank: LiveData<RankModel> = mEmpireRank
    val allianceRank: LiveData<RankModel> = mAllianceRank
    val currentDiscoverySummary: LiveData<FrontierDiscoverySummary> = mCurrentDiscoverySummary
    var isRanksBusy: MutableLiveData<Boolean> = mIsRanksBusy
    var isCmdrBusy: MutableLiveData<Boolean> = mIsCmdrBusy

    init {
        EventBus.getDefault().register(this)
        mStatisticSettings = SettingsUtils.getStatisticSettings()
        mCurrentSettings.timestamp = mStatisticSettings.timestamp
    }

    override fun onCleared() {
        super.onCleared()

        EventBus.getDefault().unregister(this)
    }

    fun load() {
        mCommanderApi?.loadProfile()
        mCommanderApi?.loadCurrentJournal()
    }

    internal fun getMainStatistics(): LiveData<List<StatisticModel>> {
        return mBuilderMain.statistics
    }

    internal fun getCurrentDiscoveries(): LiveData<List<FrontierDiscovery>> {
        return mCurrentDiscoveries
    }

    internal fun getProfitStatistics(): LiveData<List<StatisticModel>> {
        return mBuilderProfit.statistics
    }

    internal fun getProfitChart(): LiveData<List<ProfitModel>> {
        return mProfitChart
    }

    internal fun getCombatStatistics(): LiveData<List<StatisticModel>> {
        return mBuilderCombat.statistics
    }

    internal fun getExplorationStatistics(): LiveData<List<StatisticModel>> {
        return mBuilderExploration.statistics
    }

    internal fun getPassengerStatistics(): LiveData<List<StatisticModel>> {
        return mBuilderPassenger.statistics
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onFrontierProfileEvent(profile: FrontierProfileEvent) {
        GlobalScope.launch {
            if (!profile.success) {
                sendAlert(R.string.frontier_profile)
                return@launch
            }
            launchProfile(profile)
            saveStatisticsSettings()
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onFrontierRanksEvent(ranks: FrontierRanksEvent) {
        GlobalScope.launch {
            if (!ranks.success) {
                sendAlert(R.string.frontier_journal_ranks)
                mIsRanksBusy.postValue(false)
                return@launch
            }
            launchRanks(ranks)
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onFrontierFleetEvent(fleet: FrontierFleetEvent) {
        GlobalScope.launch {
            if (!fleet.success) {
                sendAlert(R.string.frontier_fleet)
                return@launch
            }
            launchFleet(fleet)
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onDistanceSearch(distanceSearch: DistanceSearchEvent) {
        GlobalScope.launch {
            if (!distanceSearch.success) {
                sendAlert(R.string.edsm_distance)
                return@launch
            }
            launchDistanceSearch(distanceSearch)
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onCurrentDiscoveries(discoveries: FrontierDiscoveriesEvent) {
        GlobalScope.launch {
            if (!discoveries.success) {
                sendAlert(R.string.Frontier_journal_discoveries)
                return@launch
            }
            launchCurrentDiscoveries(discoveries)
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onStatistics(statistics: FrontierStatisticsEvent) {
        GlobalScope.launch {
            if (!statistics.success) {
                sendAlert(R.string.Frontier_journal_statistics)
                return@launch
            }

            launchPlayerStats(statistics)
            launchProfitChart(statistics)
            launchProfitStats(statistics)
            launchCombatStats(statistics)
            launchExplorationStats(statistics)
            launchPassengerStats(statistics)

            saveStatisticsSettings()
        }
    }

    private fun saveStatisticsSettings() {
        if (SettingsUtils.canSaveSettings(mCurrentSettings)) {
            SettingsUtils.setStatisticsSettings(mCurrentSettings)
            mIsCmdrBusy.postValue(false)
        }
    }

    private fun sendAlert(@StringRes message: Int) {
        EventBus.getDefault().post(AlertEvent(R.string.download_error, message))
    }

    private fun launchPlayerStats(statistics: FrontierStatisticsEvent) {
        mBuilderMain.addStatistic(
            CMDR_TIME_PLAYED,
            LEFT,
            R.string.time_played,
            statistics.exploration!!.timePlayed,
            mStatisticSettings.timePlayed,
            TIME
        )

        mBuilderMain.addStatistic(
            CMDR_TIME_PLAYED,
            RIGHT,
            R.string.last_journal,
            statistics.lastJournalDate!!,
            null,
            DATETIME,
            DIMMED
        )

        mBuilderMain.post()
        mCurrentSettings.timePlayed = statistics.exploration.timePlayed
    }

    private fun launchProfile(profile: FrontierProfileEvent) {
        mName.postValue(profile.name)

        val amount = profile.balance
        var credits = if (profile.loan != 0L) {
            val loan = StatisticsBuilder.formatCurrency(profile.loan)
            "$amount CR and loan $loan"
        } else amount

        // error case
        if (profile.balance == -1L) {
            credits = "Unknown"
        }

        mBuilderMain.addStatistic(
            CMDR_CREDITS,
            LEFT,
            R.string.credits,
            credits,
            mStatisticSettings.credits,
            CURRENCY
        )

        mBuilderMain.addStatistic(
            CMDR_LOCATION,
            LEFT,
            R.string.current_location,
            profile.systemName,
            null,
            NONE,
            DIMMED
        )

        mCommanderApi?.getDistanceToSol(profile.systemName)

        val hullPercentage: Int = profile.hull / 10000

        mBuilderMain.addStatistic(
            CMDR_SHIP,
            RIGHT,
            R.string.hull,
            "$hullPercentage%",
            null,
            NONE,
            if (hullPercentage >= 50) DIMMED else WARNING
        )

        val integrityPercentage: Int = profile.integrity / 10000

        mBuilderMain.addStatistic(
            CMDR_SHIP,
            CENTER,
            R.string.integrity,
            "$integrityPercentage%",
            null,
            NONE,
            if (integrityPercentage >= 50) DIMMED else WARNING
        )

        mBuilderMain.post()

        mCurrentSettings.credits = profile.balance
    }

    private fun launchRanks(ranks: FrontierRanksEvent) {
        mCombatRank.postValue(RankModel(RankModel.RankType.COMBAT, ranks.combat!!))
        mTradeRank.postValue(RankModel(RankModel.RankType.TRADING, ranks.trade!!))
        mExploreRank.postValue(RankModel(RankModel.RankType.EXPLORATION, ranks.explore!!))
        mCqcRank.postValue(RankModel(RankModel.RankType.CQC, ranks.cqc!!))
        mFederationRank.postValue(RankModel(RankModel.RankType.FEDERATION, ranks.federation!!))
        mEmpireRank.postValue(RankModel(RankModel.RankType.EMPIRE, ranks.empire!!))
        mAllianceRank.postValue(RankModel(RankModel.RankType.ALLIANCE, ranks.alliance!!))

        mIsRanksBusy.postValue(false)
    }

    private fun launchFleet(fleet: FrontierFleetEvent) {
        var assets: Long = 0

        if (fleet.frontierShips.any()) {
            fleet.frontierShips.forEach {
                if (it.isCurrentShip) {
                    mBuilderMain.addStatistic(
                        CMDR_SHIP,
                        LEFT,
                        R.string.current_ship,
                        it.model,
                        null,
                        NONE,
                        DIMMED
                    )
                }
                assets += it.totalValue
            }

            mBuilderMain.addStatistic(
                CMDR_CREDITS,
                RIGHT,
                R.string.assets_value,
                assets,
                mStatisticSettings.assets,
                CURRENCY
            )
            mBuilderMain.post()

            mCurrentSettings.assets = assets
        }
    }

    private fun launchDistanceSearch(distanceSearch: DistanceSearchEvent) {
        mBuilderMain.addStatistic(
            CMDR_LOCATION,
            RIGHT,
            if (distanceSearch.distance == 0.0) R.string.current_location else R.string.distance_sol,
            if (distanceSearch.distance == 0.0) "Discovered" else "${StatisticsBuilder.formatDouble(distanceSearch.distance)} LY",
            null,
            NONE,
            DIMMED
        )

        mBuilderProfit.post()
    }

    @SuppressLint("NullSafeMutableLiveData")
    private fun launchCurrentDiscoveries(discoveries: FrontierDiscoveriesEvent) {
        if (discoveries.summary != null)
            mCurrentDiscoverySummary.postValue(discoveries.summary)
        if (discoveries.discoveries != null)
            mCurrentDiscoveries.postValue(discoveries.discoveries)
    }

    private fun launchProfitChart(statistics: FrontierStatisticsEvent) {
        val combatTotal: Long =
            statistics.combat?.assassinationProfits!! + statistics.combat.bountyHuntingProfit + statistics.combat.combatBondProfits
        val total: Long =
            combatTotal + statistics.exploration?.explorationProfits!! + statistics.trading?.marketProfits!! + statistics.mining?.miningProfits!!

        val smugglingProfit = statistics.smuggling?.let {
            ProfitModel(
                SMUGGLING,
                it.blackMarketsProfits,
                round(statistics.smuggling.blackMarketsProfits / total.toFloat() * 1000) / 10
            )
        }

        val rescueProfit = statistics.searchAndRescue?.let {
            ProfitModel(
                SEARCH_RESCUE,
                it.searchRescueProfit,
                round(statistics.searchAndRescue.searchRescueProfit / total.toFloat() * 1000) / 10
            )
        }

        val models = arrayListOf(
            ProfitModel(EXPLORATION, statistics.exploration.explorationProfits),
            ProfitModel(COMBAT, combatTotal, round(combatTotal / total.toFloat() * 1000) / 10),
            ProfitModel(
                TRADING,
                statistics.trading.marketProfits,
                round(statistics.trading.marketProfits / total.toFloat() * 1000) / 10
            ),
            ProfitModel(
                MINING,
                statistics.trading.marketProfits,
                round(statistics.mining.miningProfits / total.toFloat() * 1000) / 10
            ),
            smugglingProfit!!,
            rescueProfit!!
        )

        var lowPercentage = 0f
        var percentageTotal = 0f

        models.forEach {
            if (it.percentage < 0.1) lowPercentage += it.percentage
            percentageTotal += it.percentage
        }
        models.first().percentage = 100f - percentageTotal + lowPercentage
        models.removeAll { it.percentage < 0.1 }

        mProfitChart.postValue(models)
    }

    private fun launchProfitStats(statistics: FrontierStatisticsEvent) {

        mBuilderProfit.addStatistic(
            PROFIT_COMBAT_ASSASSINATIONS,
            LEFT,
            R.string.assassinations,
            statistics.combat!!.assassinationProfits,
            mStatisticSettings.assassinationsProfit,
            CURRENCY
        )
        mBuilderProfit.addStatistic(
            PROFIT_COMBAT_BONDS,
            LEFT,
            R.string.combat_bonds,
            statistics.combat.combatBondProfits,
            mStatisticSettings.bondsProfit,
            CURRENCY
        )
        mBuilderProfit.addStatistic(
            PROFIT_COMBAT_BOUNTIES,
            LEFT,
            R.string.bounties,
            statistics.combat.bountyHuntingProfit,
            mStatisticSettings.bountiesProfit,
            CURRENCY
        )
        mBuilderProfit.addStatistic(
            PROFIT_COMBAT_BOUNTIES,
            RIGHT,
            R.string.highest_reward,
            statistics.combat.highestSingleReward,
            null,
            CURRENCY,
            DIMMED
        )

        mBuilderProfit.addStatistic(
            PROFIT_EXPLORATION,
            LEFT,
            R.string.exploration,
            statistics.exploration!!.explorationProfits,
            mStatisticSettings.explorationProfit,
            CURRENCY
        )
        mBuilderProfit.addStatistic(
            PROFIT_EXPLORATION,
            RIGHT,
            R.string.highest_reward,
            statistics.exploration.highestPayout,
            null,
            CURRENCY,
            DIMMED
        )

        mBuilderProfit.addStatistic(
            PROFIT_TRADING,
            LEFT,
            R.string.trading,
            statistics.trading!!.marketProfits,
            mStatisticSettings.tradingProfit,
            CURRENCY
        )

        mBuilderProfit.addStatistic(
            PROFIT_TRADING,
            RIGHT,
            R.string.highest_reward,
            statistics.trading.highestSingleTransaction,
            null,
            CURRENCY,
            DIMMED
        )

        mBuilderProfit.addStatistic(
            PROFIT_SMUGGLING,
            LEFT,
            R.string.smuggling,
            statistics.smuggling!!.blackMarketsProfits,
            mStatisticSettings.blackMarketProfit,
            CURRENCY
        )

        mBuilderProfit.addStatistic(
            PROFIT_SMUGGLING,
            RIGHT,
            R.string.highest_reward,
            statistics.smuggling.highestSingleTransaction,
            null,
            CURRENCY,
            DIMMED
        )

        mBuilderProfit.addStatistic(
            PROFIT_MINING,
            LEFT,
            R.string.mining,
            statistics.mining!!.miningProfits,
            mStatisticSettings.miningProfit,
            CURRENCY
        )
        mBuilderProfit.addStatistic(
            PROFIT_MINING,
            RIGHT,
            R.string.quantity,
            statistics.mining.quantityMined,
            mStatisticSettings.miningTotal,
            TONS,
            DIMMED
        )

        mBuilderProfit.addStatistic(
            PROFIT_SEARCH_RESCUE,
            LEFT,
            R.string.search_rescue,
            statistics.searchAndRescue!!.searchRescueProfit,
            mStatisticSettings.rescueProfit,
            CURRENCY
        )
        mBuilderProfit.addStatistic(
            PROFIT_SEARCH_RESCUE,
            RIGHT,
            R.string.total,
            statistics.searchAndRescue.searchRescueCount,
            null,
            INTEGER,
            DIMMED
        )

        mCurrentSettings.bountiesProfit = statistics.combat.bountyHuntingProfit
        mCurrentSettings.bondsProfit = statistics.combat.combatBondProfits
        mCurrentSettings.assassinationsProfit = statistics.combat.assassinationProfits
        mCurrentSettings.explorationProfit = statistics.exploration.explorationProfits
        mCurrentSettings.tradingProfit = statistics.trading.marketProfits
        mCurrentSettings.tradingMarkets = statistics.trading.marketsTradedWith
        mCurrentSettings.blackMarketProfit = statistics.smuggling.blackMarketsProfits
        mCurrentSettings.miningProfit = statistics.mining.miningProfits
        mCurrentSettings.miningTotal = statistics.mining.quantityMined
        mCurrentSettings.rescueProfit = statistics.searchAndRescue.searchRescueProfit
        mCurrentSettings.rescueTotal = statistics.searchAndRescue.searchRescueCount

        mBuilderProfit.post()
    }

    private fun launchCombatStats(statistics: FrontierStatisticsEvent) {

        val totalKills =
            statistics.combat!!.bountiesClaimed + statistics.combat.combatBonds + statistics.combat.assassinations

        mBuilderCombat.addStatistic(
            COMBAT_TOTAL_KILLS,
            LEFT,
            R.string.ships_destroyed,
            totalKills,
            mStatisticSettings.totalKills,
            INTEGER
        )
        mBuilderCombat.addStatistic(
            COMBAT_TOTAL_KILLS,
            RIGHT,
            R.string.skimmers_killed,
            statistics.combat.skimmersKilled,
            mStatisticSettings.skimmersKilled,
            INTEGER
        )
        mBuilderCombat.addStatistic(
            COMBAT_KILLS,
            LEFT,
            R.string.bounties,
            statistics.combat.bountiesClaimed,
            mStatisticSettings.bountiesTotal,
            INTEGER
        )
        mBuilderCombat.addStatistic(
            COMBAT_KILLS,
            CENTER,
            R.string.bonds,
            statistics.combat.combatBonds,
            mStatisticSettings.bondsTotal,
            INTEGER
        )
        mBuilderCombat.addStatistic(
            COMBAT_KILLS,
            RIGHT,
            R.string.assassinations,
            statistics.combat.assassinations,
            mStatisticSettings.assassinationsTotal,
            INTEGER
        )

        mCurrentSettings.totalKills = totalKills
        mCurrentSettings.skimmersKilled = statistics.combat.skimmersKilled
        mCurrentSettings.bountiesTotal = statistics.combat.bountiesClaimed
        mCurrentSettings.bondsTotal = statistics.combat.combatBonds
        mCurrentSettings.assassinationsTotal = statistics.combat.assassinations

        mBuilderCombat.post()
    }

    private fun launchExplorationStats(statistics: FrontierStatisticsEvent) {

        mBuilderExploration.addStatistic(
            EXPLORATION_HYPERSPACE,
            LEFT,
            R.string.hyperspace_jumps,
            statistics.exploration!!.totalHyperspaceJumps,
            mStatisticSettings.totalHyperspaceJumps,
            INTEGER
        )

        mBuilderExploration.addStatistic(
            EXPLORATION_HYPERSPACE,
            RIGHT,
            R.string.hyperspace_distance,
            statistics.exploration.totalHyperspaceDistance,
            mStatisticSettings.totalHyperspaceDistance,
            LIGHTYEAR
        )

        mBuilderExploration.addStatistic(
            EXPLORATION_SYSTEMS_VISITED,
            LEFT,
            R.string.systems_visited,
            statistics.exploration.systemsVisited,
            mStatisticSettings.systemsVisited,
            INTEGER
        )

        mBuilderExploration.addStatistic(
            EXPLORATION_SYSTEMS_VISITED,
            RIGHT,
            R.string.greatest_distance_from_start,
            statistics.exploration.greatestDistanceFromStart,
            null,
            LIGHTYEAR,
            DIMMED
        )

        mBuilderExploration.addStatistic(
            EXPLORATION_SCANS,
            LEFT,
            R.string.planets_scanned,
            statistics.exploration.planetsScannedToLevel2,
            mStatisticSettings.planetsScanned,
            INTEGER
        )

        mBuilderExploration.addStatistic(
            EXPLORATION_SCANS,
            RIGHT,
            R.string.planets_efficient_mapped,
            statistics.exploration.efficientScans,
            mStatisticSettings.planetsEfficientMapped,
            INTEGER
        )

        mCurrentSettings.totalHyperspaceJumps = statistics.exploration.totalHyperspaceJumps
        mCurrentSettings.totalHyperspaceDistance = statistics.exploration.totalHyperspaceDistance
        mCurrentSettings.systemsVisited = statistics.exploration.systemsVisited
        mCurrentSettings.planetsScanned = statistics.exploration.planetsScannedToLevel2
        mCurrentSettings.planetsEfficientMapped = statistics.exploration.efficientScans

        mBuilderExploration.post()
    }

    private fun launchPassengerStats(statistics: FrontierStatisticsEvent) {
        mBuilderPassenger.addStatistic(
            PASSENGERS_DELIVERED,
            LEFT,
            R.string.passengers_delivered,
            statistics.passengers!!.passengersMissionsDelivered,
            mStatisticSettings.passengersDelivered,
            INTEGER
        )

        mBuilderPassenger.addStatistic(
            PASSENGERS_DELIVERED,
            RIGHT,
            R.string.passengers_ejected,
            statistics.passengers.passengersMissionsEjected,
            null,
            INTEGER,
            DIMMED
        )

        mBuilderPassenger.addStatistic(
            PASSENGERS_TYPE,
            LEFT,
            R.string.passengers_bulk,
            statistics.passengers.passengersMissionsBulk,
            null,
            INTEGER,
            DIMMED
        )

        mBuilderPassenger.addStatistic(
            PASSENGERS_TYPE,
            RIGHT,
            R.string.passengers_vip,
            statistics.passengers.passengersMissionsVIP,
            null,
            INTEGER,
            DIMMED
        )

        mCurrentSettings.passengersDelivered = statistics.passengers.passengersMissionsDelivered

        mBuilderPassenger.post()
    }


    class Factory(private val client: CommanderClient?) :
        ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T = CommanderViewModel(client) as T
    }
}
