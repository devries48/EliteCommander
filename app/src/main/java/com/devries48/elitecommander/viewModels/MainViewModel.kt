package com.devries48.elitecommander.viewModels

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
import com.devries48.elitecommander.network.CommanderClient
import com.devries48.elitecommander.utils.SettingsUtils
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.math.round

@DelicateCoroutinesApi
class MainViewModel : ViewModel() {

    // <editor-fold desc="Private definitions">

    private val mCommanderApi = CommanderClient.instance
    private var mCurrentSettings = StatisticSettingsModel()
    private lateinit var mStatisticSettings: StatisticSettingsModel

    private val mName = MutableLiveData("")
    private val mNotoriety = MutableLiveData(0)

    private val mIsProfileBusy = MutableLiveData<Boolean>().default(false)
    private val mIsRanksBusy = MutableLiveData<Boolean>().default(false)
    private val mIsStatsBusy = MutableLiveData<Boolean>().default(false)
    private val mIsDiscoveryBusy = MutableLiveData<Boolean>().default(false)

    private val mCombatRank =
        MutableLiveData(RankModel(RankModel.RankType.COMBAT, FrontierRanksEvent.FrontierRank()))
    private val mTradeRank =
        MutableLiveData(RankModel(RankModel.RankType.TRADING, FrontierRanksEvent.FrontierRank()))
    private val mExploreRank =
        MutableLiveData(RankModel(RankModel.RankType.EXPLORATION, FrontierRanksEvent.FrontierRank()))
    private val mCqcRank =
        MutableLiveData(RankModel(RankModel.RankType.CQC, FrontierRanksEvent.FrontierRank()))
    private val mFederationRank =
        MutableLiveData(RankModel(RankModel.RankType.FEDERATION, FrontierRanksEvent.FrontierRank()))
    private val mEmpireRank =
        MutableLiveData(RankModel(RankModel.RankType.EMPIRE, FrontierRanksEvent.FrontierRank()))
    private val mAllianceRank =
        MutableLiveData(RankModel(RankModel.RankType.ALLIANCE, FrontierRanksEvent.FrontierRank()))

    private var mCurrentDiscoveries = MutableLiveData<List<FrontierDiscovery>>()
    private var mCurrentDiscoverySummary =
        MutableLiveData(FrontierDiscoverySummary(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0, 0, null))

    private var mProfitChart = MutableLiveData<List<ProfitModel>>()

    private val mBuilderProfit: RowBuilder = RowBuilder()
    private val mBuilderMain: RowBuilder = RowBuilder()
    private val mBuilderCombat: RowBuilder = RowBuilder()
    private val mBuilderExploration: RowBuilder = RowBuilder()
    private val mBuilderPassenger: RowBuilder = RowBuilder()
    private val mBuilderTrading: RowBuilder = RowBuilder()

    // </editor-fold>

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
    var isStatsBusy: MutableLiveData<Boolean> = mIsStatsBusy
    var isDiscoveryBusy: MutableLiveData<Boolean> = mIsDiscoveryBusy
    var isProfileBusy: MutableLiveData<Boolean> = mIsProfileBusy

    init {
        EventBus.getDefault().register(this)
        loadSettings()
    }

    override fun onCleared() {
        super.onCleared()
        EventBus.getDefault().unregister(this)
    }

    fun load() {
        setAllBusyIndicators(true)

        mCommanderApi.loadProfile()
        mCommanderApi.loadCurrentJournal()
    }

    private fun loadSettings() {
        mStatisticSettings = SettingsUtils.getStatisticSettings()
        mCurrentSettings.timestamp = mStatisticSettings.timestamp
    }

    internal fun getMainStatistics(): LiveData<List<RowModel>> {
        return mBuilderMain.rows
    }

    internal fun getCurrentDiscoveries(): LiveData<List<FrontierDiscovery>> {
        return mCurrentDiscoveries
    }

    internal fun getProfitStatistics(): LiveData<List<RowModel>> {
        return mBuilderProfit.rows
    }

    internal fun getProfitChart(): LiveData<List<ProfitModel>> {
        return mProfitChart
    }

    internal fun getCombatStatistics(): LiveData<List<RowModel>> {
        return mBuilderCombat.rows
    }

    internal fun getExplorationStatistics(): LiveData<List<RowModel>> {
        return mBuilderExploration.rows
    }

    internal fun getTradingStatistics(): LiveData<List<RowModel>> {
        return mBuilderTrading.rows
    }

    internal fun getPassengerStatistics(): LiveData<List<RowModel>> {
        return mBuilderPassenger.rows
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onFrontierProfileEvent(profile: FrontierProfileEvent) {
        GlobalScope.launch {
            if (!profile.success) sendAlert(R.string.frontier_profile, profile.error)
            else {
                launchProfile(profile)
                saveStatisticsSettings()
            }
            mIsProfileBusy.postValue(false)
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onFrontierRanksEvent(ranks: FrontierRanksEvent) {
        GlobalScope.launch {
            if (ranks.success) launchRanks(ranks)

            mIsRanksBusy.postValue(false)
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onFrontierFleetEvent(fleet: FrontierFleetEvent) {
        GlobalScope.launch {
            if (!fleet.success) sendAlert(R.string.frontier_fleet, fleet.error) else launchFleet(fleet)
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onDistanceSearch(distanceSearch: DistanceSearchEvent) {
        GlobalScope.launch {
            if (!distanceSearch.success) sendAlert(R.string.edsm_distance, distanceSearch.error)
            else launchDistanceSearch(distanceSearch)
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onCurrentDiscoveries(discoveries: FrontierDiscoveriesEvent) {
        GlobalScope.launch {
            if (!discoveries.success) sendAlert(R.string.frontier_journal_discoveries, discoveries.error)
            else launchCurrentDiscoveries(discoveries)

            mIsDiscoveryBusy.postValue(false)
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onStatistics(statistics: FrontierStatisticsEvent) {
        GlobalScope.launch {
            if (!statistics.success) {
                sendAlert(R.string.frontier_journal_statistics, statistics.error)
                mIsStatsBusy.postValue(false)
                return@launch
            }

            launchPlayerStats(statistics)
            launchProfitChart(statistics)
            launchProfitStats(statistics)
            launchCombatStats(statistics)
            launchExplorationStats(statistics)
            launchTradingStats(statistics)
            launchPassengerStats(statistics)

            saveStatisticsSettings()

            mIsStatsBusy.postValue(false)
        }
    }

    private fun saveStatisticsSettings() {
        if (SettingsUtils.canSaveSettings(mCurrentSettings))
            SettingsUtils.setStatisticsSettings(mCurrentSettings)
    }

    private fun sendAlert(@StringRes message: Int, title: String?) {
        setAllBusyIndicators(false)
        EventBus.getDefault().post(AlertEvent(title, message))
    }

    private fun setAllBusyIndicators(switchOn: Boolean) {
        mIsProfileBusy.postValue(switchOn)
        mIsRanksBusy.postValue(switchOn)
        mIsStatsBusy.postValue(switchOn)
        mIsDiscoveryBusy.postValue(switchOn)
    }

    private fun launchPlayerStats(statistics: FrontierStatisticsEvent) {
        mBuilderMain.addStatistic(
            RowBuilder.StatisticType.CMDR_TIME_PLAYED,
            RowBuilder.RowPosition.LEFT,
            R.string.time_played,
            statistics.exploration!!.timePlayed,
            mStatisticSettings.timePlayed,
            RowBuilder.RowFormat.TIME
        )

        mBuilderMain.addStatistic(
            RowBuilder.StatisticType.CMDR_TIME_PLAYED,
            RowBuilder.RowPosition.RIGHT,
            R.string.last_journal,
            statistics.lastJournalDate!!,
            null,
            RowBuilder.RowFormat.DATETIME,
            RowBuilder.RowColor.DIMMED
        )

        mBuilderMain.post()
        mCurrentSettings.timePlayed = statistics.exploration.timePlayed
    }

    private fun launchProfile(profile: FrontierProfileEvent) {
        mName.postValue(profile.name)

        val amount = profile.balance
        var credits =
            if (profile.loan != 0L) {
                val loan = RowBuilder.formatCurrency(profile.loan)
                "$amount CR and loan $loan"
            } else amount

        // error case
        if (profile.balance == -1L) {
            credits = "Unknown"
        }

        mBuilderMain.addStatistic(
            RowBuilder.StatisticType.CMDR_CREDITS,
            RowBuilder.RowPosition.LEFT,
            R.string.credits,
            credits,
            mStatisticSettings.credits,
            RowBuilder.RowFormat.CURRENCY
        )

        mBuilderMain.addStatistic(
            RowBuilder.StatisticType.CMDR_LOCATION,
            RowBuilder.RowPosition.LEFT,
            R.string.current_location,
            profile.systemName,
            null,
            RowBuilder.RowFormat.NONE,
            RowBuilder.RowColor.DIMMED
        )

        mCommanderApi.getDistanceToSol(profile.systemName)

        val hullPercentage: Int = profile.hull / 10000

        mBuilderMain.addStatistic(
            RowBuilder.StatisticType.CMDR_SHIP,
            RowBuilder.RowPosition.RIGHT,
            R.string.hull,
            "$hullPercentage%",
            null,
            RowBuilder.RowFormat.NONE,
            if (hullPercentage >= 50) RowBuilder.RowColor.DIMMED
            else RowBuilder.RowColor.WARNING
        )

        val integrityPercentage: Int = profile.integrity / 10000

        mBuilderMain.addStatistic(
            RowBuilder.StatisticType.CMDR_SHIP,
            RowBuilder.RowPosition.CENTER,
            R.string.integrity,
            "$integrityPercentage%",
            null,
            RowBuilder.RowFormat.NONE,
            if (integrityPercentage >= 50) RowBuilder.RowColor.DIMMED
            else RowBuilder.RowColor.WARNING
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
    }

    private fun launchFleet(fleet: FrontierFleetEvent) {
        var assets: Long = 0

        if (fleet.frontierShips.any()) {
            fleet.frontierShips.forEach {
                if (it.isCurrentShip) {
                    mBuilderMain.addStatistic(
                        RowBuilder.StatisticType.CMDR_SHIP,
                        RowBuilder.RowPosition.LEFT,
                        R.string.current_ship,
                        it.model,
                        null,
                        RowBuilder.RowFormat.NONE,
                        RowBuilder.RowColor.DIMMED
                    )
                }
                assets += it.totalValue
            }

            mBuilderMain.addStatistic(
                RowBuilder.StatisticType.CMDR_CREDITS,
                RowBuilder.RowPosition.RIGHT,
                R.string.assets_value,
                assets,
                mStatisticSettings.assets,
                RowBuilder.RowFormat.CURRENCY
            )
            mBuilderMain.post()

            mCurrentSettings.assets = assets
        }
    }

    private fun launchDistanceSearch(distanceSearch: DistanceSearchEvent) {
        mBuilderMain.addStatistic(
            RowBuilder.StatisticType.CMDR_LOCATION,
            RowBuilder.RowPosition.RIGHT,
            R.string.distance_sol,
            if (distanceSearch.distance == 0.0) "Discovered"
            else "${RowBuilder.formatDouble(distanceSearch.distance)} LY",
            null,
            RowBuilder.RowFormat.NONE,
            RowBuilder.RowColor.DIMMED
        )

        mBuilderProfit.post()
    }

    @SuppressLint("NullSafeMutableLiveData")
    private fun launchCurrentDiscoveries(discoveries: FrontierDiscoveriesEvent) {
        if (discoveries.summary != null) mCurrentDiscoverySummary.postValue(discoveries.summary)
        if (discoveries.discoveries != null) mCurrentDiscoveries.postValue(discoveries.discoveries)
    }

    private fun launchProfitChart(statistics: FrontierStatisticsEvent) {
        val combatTotal: Long =
            statistics.combat?.assassinationProfits!! +
                    statistics.combat.bountyHuntingProfit +
                    statistics.combat.combatBondProfits
        val total: Long =
            combatTotal +
                    statistics.exploration?.explorationProfits!! +
                    statistics.trading?.marketProfits!! +
                    statistics.mining?.miningProfits!!

        val smugglingProfit =
            statistics.smuggling?.let {
                ProfitModel(
                    ProfitModel.ProfitType.SMUGGLING,
                    it.blackMarketsProfits,
                    round(statistics.smuggling.blackMarketsProfits / total.toFloat() * 1000) / 10
                )
            }

        val rescueProfit =
            statistics.searchAndRescue?.let {
                ProfitModel(
                    ProfitModel.ProfitType.SEARCH_RESCUE,
                    it.searchRescueProfit,
                    round(statistics.searchAndRescue.searchRescueProfit / total.toFloat() * 1000) / 10
                )
            }

        val models =
            arrayListOf(
                ProfitModel(
                    ProfitModel.ProfitType.EXPLORATION, statistics.exploration.explorationProfits
                ),
                ProfitModel(
                    ProfitModel.ProfitType.COMBAT,
                    combatTotal,
                    round(combatTotal / total.toFloat() * 1000) / 10
                ),
                ProfitModel(
                    ProfitModel.ProfitType.TRADING,
                    statistics.trading.marketProfits,
                    round(statistics.trading.marketProfits / total.toFloat() * 1000) / 10
                ),
                ProfitModel(
                    ProfitModel.ProfitType.MINING,
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
            RowBuilder.StatisticType.PROFIT_COMBAT_ASSASSINATIONS,
            RowBuilder.RowPosition.LEFT,
            R.string.assassinations,
            statistics.combat!!.assassinationProfits,
            mStatisticSettings.assassinationsProfit,
            RowBuilder.RowFormat.CURRENCY
        )
        mBuilderProfit.addStatistic(
            RowBuilder.StatisticType.PROFIT_COMBAT_BONDS,
            RowBuilder.RowPosition.LEFT,
            R.string.combat_bonds,
            statistics.combat.combatBondProfits,
            mStatisticSettings.bondsProfit,
            RowBuilder.RowFormat.CURRENCY
        )
        mBuilderProfit.addStatistic(
            RowBuilder.StatisticType.PROFIT_COMBAT_BOUNTIES,
            RowBuilder.RowPosition.LEFT,
            R.string.bounties,
            statistics.combat.bountyHuntingProfit,
            mStatisticSettings.bountiesProfit,
            RowBuilder.RowFormat.CURRENCY
        )
        mBuilderProfit.addStatistic(
            RowBuilder.StatisticType.PROFIT_COMBAT_BOUNTIES,
            RowBuilder.RowPosition.RIGHT,
            R.string.highest_reward,
            statistics.combat.highestSingleReward,
            null,
            RowBuilder.RowFormat.CURRENCY,
            RowBuilder.RowColor.DIMMED
        )

        mBuilderProfit.addStatistic(
            RowBuilder.StatisticType.PROFIT_EXPLORATION,
            RowBuilder.RowPosition.LEFT,
            R.string.exploration,
            statistics.exploration!!.explorationProfits,
            mStatisticSettings.explorationProfit,
            RowBuilder.RowFormat.CURRENCY
        )
        mBuilderProfit.addStatistic(
            RowBuilder.StatisticType.PROFIT_EXPLORATION,
            RowBuilder.RowPosition.RIGHT,
            R.string.highest_reward,
            statistics.exploration.highestPayout,
            null,
            RowBuilder.RowFormat.CURRENCY,
            RowBuilder.RowColor.DIMMED
        )

        mBuilderProfit.addStatistic(
            RowBuilder.StatisticType.PROFIT_TRADING,
            RowBuilder.RowPosition.LEFT,
            R.string.trading,
            statistics.trading!!.marketProfits,
            mStatisticSettings.tradingProfit,
            RowBuilder.RowFormat.CURRENCY
        )

        mBuilderProfit.addStatistic(
            RowBuilder.StatisticType.PROFIT_TRADING,
            RowBuilder.RowPosition.RIGHT,
            R.string.highest_reward,
            statistics.trading.highestSingleTransaction,
            null,
            RowBuilder.RowFormat.CURRENCY,
            RowBuilder.RowColor.DIMMED
        )

        mBuilderProfit.addStatistic(
            RowBuilder.StatisticType.PROFIT_SMUGGLING,
            RowBuilder.RowPosition.LEFT,
            R.string.smuggling,
            statistics.smuggling!!.blackMarketsProfits,
            mStatisticSettings.blackMarketProfit,
            RowBuilder.RowFormat.CURRENCY
        )

        mBuilderProfit.addStatistic(
            RowBuilder.StatisticType.PROFIT_SMUGGLING,
            RowBuilder.RowPosition.RIGHT,
            R.string.highest_reward,
            statistics.smuggling.highestSingleTransaction,
            null,
            RowBuilder.RowFormat.CURRENCY,
            RowBuilder.RowColor.DIMMED
        )

        mBuilderProfit.addStatistic(
            RowBuilder.StatisticType.PROFIT_MINING,
            RowBuilder.RowPosition.LEFT,
            R.string.mining,
            statistics.mining!!.miningProfits,
            mStatisticSettings.miningProfit,
            RowBuilder.RowFormat.CURRENCY
        )
        mBuilderProfit.addStatistic(
            RowBuilder.StatisticType.PROFIT_MINING,
            RowBuilder.RowPosition.RIGHT,
            R.string.quantity,
            statistics.mining.quantityMined,
            mStatisticSettings.miningTotal,
            RowBuilder.RowFormat.TONS,
            RowBuilder.RowColor.DIMMED
        )

        mBuilderProfit.addStatistic(
            RowBuilder.StatisticType.PROFIT_SEARCH_RESCUE,
            RowBuilder.RowPosition.LEFT,
            R.string.search_rescue,
            statistics.searchAndRescue!!.searchRescueProfit,
            mStatisticSettings.rescueProfit,
            RowBuilder.RowFormat.CURRENCY
        )
        mBuilderProfit.addStatistic(
            RowBuilder.StatisticType.PROFIT_SEARCH_RESCUE,
            RowBuilder.RowPosition.RIGHT,
            R.string.total,
            statistics.searchAndRescue.searchRescueCount,
            null,
            RowBuilder.RowFormat.INTEGER,
            RowBuilder.RowColor.DIMMED
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
            statistics.combat!!.bountiesClaimed + statistics.combat.combatBonds -
                    (statistics.combat.assassinations + statistics.combat.skimmersKilled)

        mBuilderCombat.addStatistic(
            RowBuilder.StatisticType.COMBAT_TOTAL_KILLS,
            RowBuilder.RowPosition.LEFT,
            R.string.ships_destroyed,
            totalKills,
            mStatisticSettings.totalKills,
            RowBuilder.RowFormat.INTEGER
        )
        mBuilderCombat.addStatistic(
            RowBuilder.StatisticType.COMBAT_TOTAL_KILLS,
            RowBuilder.RowPosition.RIGHT,
            R.string.skimmers_killed,
            statistics.combat.skimmersKilled,
            mStatisticSettings.skimmersKilled,
            RowBuilder.RowFormat.INTEGER
        )
        mBuilderCombat.addStatistic(
            RowBuilder.StatisticType.COMBAT_KILLS,
            RowBuilder.RowPosition.LEFT,
            R.string.bounties,
            statistics.combat.bountiesClaimed,
            mStatisticSettings.bountiesTotal,
            RowBuilder.RowFormat.INTEGER
        )
        mBuilderCombat.addStatistic(
            RowBuilder.StatisticType.COMBAT_KILLS,
            RowBuilder.RowPosition.CENTER,
            R.string.bonds,
            statistics.combat.combatBonds,
            mStatisticSettings.bondsTotal,
            RowBuilder.RowFormat.INTEGER
        )
        mBuilderCombat.addStatistic(
            RowBuilder.StatisticType.COMBAT_KILLS,
            RowBuilder.RowPosition.RIGHT,
            R.string.assassinations,
            statistics.combat.assassinations,
            mStatisticSettings.assassinationsTotal,
            RowBuilder.RowFormat.INTEGER
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
            RowBuilder.StatisticType.EXPLORATION_HYPERSPACE,
            RowBuilder.RowPosition.LEFT,
            R.string.hyperspace_jumps,
            statistics.exploration!!.totalHyperspaceJumps,
            mStatisticSettings.totalHyperspaceJumps,
            RowBuilder.RowFormat.INTEGER
        )

        mBuilderExploration.addStatistic(
            RowBuilder.StatisticType.EXPLORATION_HYPERSPACE,
            RowBuilder.RowPosition.RIGHT,
            R.string.hyperspace_distance,
            statistics.exploration.totalHyperspaceDistance,
            mStatisticSettings.totalHyperspaceDistance,
            RowBuilder.RowFormat.LIGHTYEAR
        )

        mBuilderExploration.addStatistic(
            RowBuilder.StatisticType.EXPLORATION_SYSTEMS_VISITED,
            RowBuilder.RowPosition.LEFT,
            R.string.systems_visited,
            statistics.exploration.systemsVisited,
            mStatisticSettings.systemsVisited,
            RowBuilder.RowFormat.INTEGER
        )

        mBuilderExploration.addStatistic(
            RowBuilder.StatisticType.EXPLORATION_SYSTEMS_VISITED,
            RowBuilder.RowPosition.RIGHT,
            R.string.greatest_distance_from_start,
            statistics.exploration.greatestDistanceFromStart,
            null,
            RowBuilder.RowFormat.LIGHTYEAR,
            RowBuilder.RowColor.DIMMED
        )

        mBuilderExploration.addStatistic(
            RowBuilder.StatisticType.EXPLORATION_SCANS,
            RowBuilder.RowPosition.LEFT,
            R.string.planets_scanned,
            statistics.exploration.planetsScannedToLevel2,
            mStatisticSettings.planetsScanned,
            RowBuilder.RowFormat.INTEGER
        )

        mBuilderExploration.addStatistic(
            RowBuilder.StatisticType.EXPLORATION_SCANS,
            RowBuilder.RowPosition.RIGHT,
            R.string.planets_efficient_mapped,
            statistics.exploration.efficientScans,
            mStatisticSettings.planetsEfficientMapped,
            RowBuilder.RowFormat.INTEGER
        )

        mCurrentSettings.totalHyperspaceJumps = statistics.exploration.totalHyperspaceJumps
        mCurrentSettings.totalHyperspaceDistance = statistics.exploration.totalHyperspaceDistance
        mCurrentSettings.systemsVisited = statistics.exploration.systemsVisited
        mCurrentSettings.planetsScanned = statistics.exploration.planetsScannedToLevel2
        mCurrentSettings.planetsEfficientMapped = statistics.exploration.efficientScans

        mBuilderExploration.post()
    }

    private fun launchTradingStats(statistics: FrontierStatisticsEvent) {

        mBuilderTrading.addStatistic(
            RowBuilder.StatisticType.TRADING_MARKETS,
            RowBuilder.RowPosition.LEFT,
            R.string.markets_traded_with,
            statistics.trading!!.marketsTradedWith,
            mStatisticSettings.marketsTradedWith,
            RowBuilder.RowFormat.INTEGER
        )
        mBuilderTrading.addStatistic(
            RowBuilder.StatisticType.TRADING_MARKETS,
            RowBuilder.RowPosition.RIGHT,
            R.string.black_markets,
            statistics.smuggling!!.blackMarketsTradedWith,
            mStatisticSettings.blackMarketsTradedWith,
            RowBuilder.RowFormat.INTEGER
        )
        mBuilderTrading.addStatistic(
            RowBuilder.StatisticType.TRADING_RESOURCES,
            RowBuilder.RowPosition.LEFT,
            R.string.resources_traded,
            statistics.trading.resourcesTraded,
            null,
            RowBuilder.RowFormat.INTEGER,
            RowBuilder.RowColor.DIMMED
        )
        mBuilderTrading.addStatistic(
            RowBuilder.StatisticType.TRADING_RESOURCES,
            RowBuilder.RowPosition.RIGHT,
            R.string.smuggled,
            statistics.smuggling.resourcesSmuggled,
            null,
            RowBuilder.RowFormat.INTEGER,
            RowBuilder.RowColor.DIMMED
        )

        mCurrentSettings.marketsTradedWith = statistics.trading.marketsTradedWith
        mCurrentSettings.blackMarketsTradedWith = statistics.smuggling.blackMarketsTradedWith

        mBuilderTrading.post()
    }

    private fun launchPassengerStats(statistics: FrontierStatisticsEvent) {
        mBuilderPassenger.addStatistic(
            RowBuilder.StatisticType.PASSENGERS_DELIVERED,
            RowBuilder.RowPosition.LEFT,
            R.string.passengers_delivered,
            statistics.passengers!!.passengersMissionsDelivered,
            mStatisticSettings.passengersDelivered,
            RowBuilder.RowFormat.INTEGER
        )

        mBuilderPassenger.addStatistic(
            RowBuilder.StatisticType.PASSENGERS_DELIVERED,
            RowBuilder.RowPosition.RIGHT,
            R.string.passengers_ejected,
            statistics.passengers.passengersMissionsEjected,
            null,
            RowBuilder.RowFormat.INTEGER,
            RowBuilder.RowColor.DIMMED
        )

        mBuilderPassenger.addStatistic(
            RowBuilder.StatisticType.PASSENGERS_TYPE,
            RowBuilder.RowPosition.LEFT,
            R.string.passengers_bulk,
            statistics.passengers.passengersMissionsBulk,
            null,
            RowBuilder.RowFormat.INTEGER,
            RowBuilder.RowColor.DIMMED
        )

        mBuilderPassenger.addStatistic(
            RowBuilder.StatisticType.PASSENGERS_TYPE,
            RowBuilder.RowPosition.RIGHT,
            R.string.passengers_vip,
            statistics.passengers.passengersMissionsVIP,
            null,
            RowBuilder.RowFormat.INTEGER,
            RowBuilder.RowColor.DIMMED
        )

        mCurrentSettings.passengersDelivered = statistics.passengers.passengersMissionsDelivered

        mBuilderPassenger.post()
    }

    class Factory : ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = MainViewModel() as T
    }
}
