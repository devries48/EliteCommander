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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.math.round

class MainViewModel(client: CommanderClient?) : ViewModel() {

    //<editor-fold desc="Private definitions">

    private val mCommanderApi = client
    private var mCurrentSettings = StatisticSettingsModel()
    private lateinit var mStatisticSettings: StatisticSettingsModel

    private val mName = MutableLiveData("")
    private val mNotoriety = MutableLiveData(0)

    private val mIsProfileBusy = MutableLiveData<Boolean>().default(true)
    private val mIsRanksBusy = MutableLiveData<Boolean>().default(true)
    private val mIsStatsBusy = MutableLiveData<Boolean>().default(true)

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
    private val mBuilderTrading: StatisticsBuilder = StatisticsBuilder()

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
    var isStatsBusy: MutableLiveData<Boolean> = mIsStatsBusy
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
        if (mIsProfileBusy.value == true || mIsStatsBusy.value == true || mIsRanksBusy.value == true)
            return

        mIsProfileBusy.postValue(true)
        mIsRanksBusy.postValue(true)
        mIsStatsBusy.postValue(true)

        mCommanderApi?.loadProfile()
        mCommanderApi?.loadCurrentJournal()
    }

    private fun loadSettings() {
        mStatisticSettings = SettingsUtils.getStatisticSettings()
        mCurrentSettings.timestamp = mStatisticSettings.timestamp
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

    internal fun getTradingStatistics(): LiveData<List<StatisticModel>> {
        return mBuilderTrading.statistics
    }

    internal fun getPassengerStatistics(): LiveData<List<StatisticModel>> {
        return mBuilderPassenger.statistics
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onFrontierProfileEvent(profile: FrontierProfileEvent) {
        GlobalScope.launch {
            if (!profile.success)
                sendAlert(R.string.frontier_profile)
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
            if (!ranks.success)
                sendAlert(R.string.frontier_journal_ranks)
            else
                launchRanks(ranks)

            mIsRanksBusy.postValue(false)
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onFrontierFleetEvent(fleet: FrontierFleetEvent) {
        GlobalScope.launch {
            if (!fleet.success)
                sendAlert(R.string.frontier_fleet)
            else
                launchFleet(fleet)
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onDistanceSearch(distanceSearch: DistanceSearchEvent) {
        GlobalScope.launch {
            if (!distanceSearch.success)
                sendAlert(R.string.edsm_distance)
            else
                launchDistanceSearch(distanceSearch)
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onCurrentDiscoveries(discoveries: FrontierDiscoveriesEvent) {
        GlobalScope.launch {
            if (!discoveries.success)
                sendAlert(R.string.Frontier_journal_discoveries)
            else
                launchCurrentDiscoveries(discoveries)
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onStatistics(statistics: FrontierStatisticsEvent) {
        GlobalScope.launch {
            if (!statistics.success) {
                sendAlert(R.string.Frontier_journal_statistics)
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

    private fun sendAlert(@StringRes message: Int) {
        mIsProfileBusy.postValue(false)
        mIsRanksBusy.postValue(false)
        mIsStatsBusy.postValue(false)

        EventBus.getDefault().post(AlertEvent(R.string.download_error, message))
    }

    private fun launchPlayerStats(statistics: FrontierStatisticsEvent) {
        mBuilderMain.addStatistic(
            StatisticsBuilder.Companion.StatisticType.CMDR_TIME_PLAYED,
            StatisticsBuilder.Companion.StatisticPosition.LEFT,
            R.string.time_played,
            statistics.exploration!!.timePlayed,
            mStatisticSettings.timePlayed,
            StatisticsBuilder.Companion.StatisticFormat.TIME
        )

        mBuilderMain.addStatistic(
            StatisticsBuilder.Companion.StatisticType.CMDR_TIME_PLAYED,
            StatisticsBuilder.Companion.StatisticPosition.RIGHT,
            R.string.last_journal,
            statistics.lastJournalDate!!,
            null,
            StatisticsBuilder.Companion.StatisticFormat.DATETIME,
            StatisticsBuilder.Companion.StatisticColor.DIMMED
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
            StatisticsBuilder.Companion.StatisticType.CMDR_CREDITS,
            StatisticsBuilder.Companion.StatisticPosition.LEFT,
            R.string.credits,
            credits,
            mStatisticSettings.credits,
            StatisticsBuilder.Companion.StatisticFormat.CURRENCY
        )

        mBuilderMain.addStatistic(
            StatisticsBuilder.Companion.StatisticType.CMDR_LOCATION,
            StatisticsBuilder.Companion.StatisticPosition.LEFT,
            R.string.current_location,
            profile.systemName,
            null,
            StatisticsBuilder.Companion.StatisticFormat.NONE,
            StatisticsBuilder.Companion.StatisticColor.DIMMED
        )

        mCommanderApi?.getDistanceToSol(profile.systemName)

        val hullPercentage: Int = profile.hull / 10000

        mBuilderMain.addStatistic(
            StatisticsBuilder.Companion.StatisticType.CMDR_SHIP,
            StatisticsBuilder.Companion.StatisticPosition.RIGHT,
            R.string.hull,
            "$hullPercentage%",
            null,
            StatisticsBuilder.Companion.StatisticFormat.NONE,
            if (hullPercentage >= 50) StatisticsBuilder.Companion.StatisticColor.DIMMED else StatisticsBuilder.Companion.StatisticColor.WARNING
        )

        val integrityPercentage: Int = profile.integrity / 10000

        mBuilderMain.addStatistic(
            StatisticsBuilder.Companion.StatisticType.CMDR_SHIP,
            StatisticsBuilder.Companion.StatisticPosition.CENTER,
            R.string.integrity,
            "$integrityPercentage%",
            null,
            StatisticsBuilder.Companion.StatisticFormat.NONE,
            if (integrityPercentage >= 50) StatisticsBuilder.Companion.StatisticColor.DIMMED else StatisticsBuilder.Companion.StatisticColor.WARNING
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
                        StatisticsBuilder.Companion.StatisticType.CMDR_SHIP,
                        StatisticsBuilder.Companion.StatisticPosition.LEFT,
                        R.string.current_ship,
                        it.model,
                        null,
                        StatisticsBuilder.Companion.StatisticFormat.NONE,
                        StatisticsBuilder.Companion.StatisticColor.DIMMED
                    )
                }
                assets += it.totalValue
            }

            mBuilderMain.addStatistic(
                StatisticsBuilder.Companion.StatisticType.CMDR_CREDITS,
                StatisticsBuilder.Companion.StatisticPosition.RIGHT,
                R.string.assets_value,
                assets,
                mStatisticSettings.assets,
                StatisticsBuilder.Companion.StatisticFormat.CURRENCY
            )
            mBuilderMain.post()

            mCurrentSettings.assets = assets
        }
    }

    private fun launchDistanceSearch(distanceSearch: DistanceSearchEvent) {
        mBuilderMain.addStatistic(
            StatisticsBuilder.Companion.StatisticType.CMDR_LOCATION,
            StatisticsBuilder.Companion.StatisticPosition.RIGHT,
            if (distanceSearch.distance == 0.0) R.string.current_location else R.string.distance_sol,
            if (distanceSearch.distance == 0.0) "Discovered" else "${StatisticsBuilder.formatDouble(distanceSearch.distance)} LY",
            null,
            StatisticsBuilder.Companion.StatisticFormat.NONE,
            StatisticsBuilder.Companion.StatisticColor.DIMMED
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
                ProfitModel.ProfitType.SMUGGLING,
                it.blackMarketsProfits,
                round(statistics.smuggling.blackMarketsProfits / total.toFloat() * 1000) / 10
            )
        }

        val rescueProfit = statistics.searchAndRescue?.let {
            ProfitModel(
                ProfitModel.ProfitType.SEARCH_RESCUE,
                it.searchRescueProfit,
                round(statistics.searchAndRescue.searchRescueProfit / total.toFloat() * 1000) / 10
            )
        }

        val models = arrayListOf(
            ProfitModel(ProfitModel.ProfitType.EXPLORATION, statistics.exploration.explorationProfits),
            ProfitModel(ProfitModel.ProfitType.COMBAT, combatTotal, round(combatTotal / total.toFloat() * 1000) / 10),
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
            StatisticsBuilder.Companion.StatisticType.PROFIT_COMBAT_ASSASSINATIONS,
            StatisticsBuilder.Companion.StatisticPosition.LEFT,
            R.string.assassinations,
            statistics.combat!!.assassinationProfits,
            mStatisticSettings.assassinationsProfit,
            StatisticsBuilder.Companion.StatisticFormat.CURRENCY
        )
        mBuilderProfit.addStatistic(
            StatisticsBuilder.Companion.StatisticType.PROFIT_COMBAT_BONDS,
            StatisticsBuilder.Companion.StatisticPosition.LEFT,
            R.string.combat_bonds,
            statistics.combat.combatBondProfits,
            mStatisticSettings.bondsProfit,
            StatisticsBuilder.Companion.StatisticFormat.CURRENCY
        )
        mBuilderProfit.addStatistic(
            StatisticsBuilder.Companion.StatisticType.PROFIT_COMBAT_BOUNTIES,
            StatisticsBuilder.Companion.StatisticPosition.LEFT,
            R.string.bounties,
            statistics.combat.bountyHuntingProfit,
            mStatisticSettings.bountiesProfit,
            StatisticsBuilder.Companion.StatisticFormat.CURRENCY
        )
        mBuilderProfit.addStatistic(
            StatisticsBuilder.Companion.StatisticType.PROFIT_COMBAT_BOUNTIES,
            StatisticsBuilder.Companion.StatisticPosition.RIGHT,
            R.string.highest_reward,
            statistics.combat.highestSingleReward,
            null,
            StatisticsBuilder.Companion.StatisticFormat.CURRENCY,
            StatisticsBuilder.Companion.StatisticColor.DIMMED
        )

        mBuilderProfit.addStatistic(
            StatisticsBuilder.Companion.StatisticType.PROFIT_EXPLORATION,
            StatisticsBuilder.Companion.StatisticPosition.LEFT,
            R.string.exploration,
            statistics.exploration!!.explorationProfits,
            mStatisticSettings.explorationProfit,
            StatisticsBuilder.Companion.StatisticFormat.CURRENCY
        )
        mBuilderProfit.addStatistic(
            StatisticsBuilder.Companion.StatisticType.PROFIT_EXPLORATION,
            StatisticsBuilder.Companion.StatisticPosition.RIGHT,
            R.string.highest_reward,
            statistics.exploration.highestPayout,
            null,
            StatisticsBuilder.Companion.StatisticFormat.CURRENCY,
            StatisticsBuilder.Companion.StatisticColor.DIMMED
        )

        mBuilderProfit.addStatistic(
            StatisticsBuilder.Companion.StatisticType.PROFIT_TRADING,
            StatisticsBuilder.Companion.StatisticPosition.LEFT,
            R.string.trading,
            statistics.trading!!.marketProfits,
            mStatisticSettings.tradingProfit,
            StatisticsBuilder.Companion.StatisticFormat.CURRENCY
        )

        mBuilderProfit.addStatistic(
            StatisticsBuilder.Companion.StatisticType.PROFIT_TRADING,
            StatisticsBuilder.Companion.StatisticPosition.RIGHT,
            R.string.highest_reward,
            statistics.trading.highestSingleTransaction,
            null,
            StatisticsBuilder.Companion.StatisticFormat.CURRENCY,
            StatisticsBuilder.Companion.StatisticColor.DIMMED
        )

        mBuilderProfit.addStatistic(
            StatisticsBuilder.Companion.StatisticType.PROFIT_SMUGGLING,
            StatisticsBuilder.Companion.StatisticPosition.LEFT,
            R.string.smuggling,
            statistics.smuggling!!.blackMarketsProfits,
            mStatisticSettings.blackMarketProfit,
            StatisticsBuilder.Companion.StatisticFormat.CURRENCY
        )

        mBuilderProfit.addStatistic(
            StatisticsBuilder.Companion.StatisticType.PROFIT_SMUGGLING,
            StatisticsBuilder.Companion.StatisticPosition.RIGHT,
            R.string.highest_reward,
            statistics.smuggling.highestSingleTransaction,
            null,
            StatisticsBuilder.Companion.StatisticFormat.CURRENCY,
            StatisticsBuilder.Companion.StatisticColor.DIMMED
        )

        mBuilderProfit.addStatistic(
            StatisticsBuilder.Companion.StatisticType.PROFIT_MINING,
            StatisticsBuilder.Companion.StatisticPosition.LEFT,
            R.string.mining,
            statistics.mining!!.miningProfits,
            mStatisticSettings.miningProfit,
            StatisticsBuilder.Companion.StatisticFormat.CURRENCY
        )
        mBuilderProfit.addStatistic(
            StatisticsBuilder.Companion.StatisticType.PROFIT_MINING,
            StatisticsBuilder.Companion.StatisticPosition.RIGHT,
            R.string.quantity,
            statistics.mining.quantityMined,
            mStatisticSettings.miningTotal,
            StatisticsBuilder.Companion.StatisticFormat.TONS,
            StatisticsBuilder.Companion.StatisticColor.DIMMED
        )

        mBuilderProfit.addStatistic(
            StatisticsBuilder.Companion.StatisticType.PROFIT_SEARCH_RESCUE,
            StatisticsBuilder.Companion.StatisticPosition.LEFT,
            R.string.search_rescue,
            statistics.searchAndRescue!!.searchRescueProfit,
            mStatisticSettings.rescueProfit,
            StatisticsBuilder.Companion.StatisticFormat.CURRENCY
        )
        mBuilderProfit.addStatistic(
            StatisticsBuilder.Companion.StatisticType.PROFIT_SEARCH_RESCUE,
            StatisticsBuilder.Companion.StatisticPosition.RIGHT,
            R.string.total,
            statistics.searchAndRescue.searchRescueCount,
            null,
            StatisticsBuilder.Companion.StatisticFormat.INTEGER,
            StatisticsBuilder.Companion.StatisticColor.DIMMED
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
            statistics.combat!!.bountiesClaimed + statistics.combat.combatBonds - (statistics.combat.assassinations + statistics.combat.skimmersKilled)

        mBuilderCombat.addStatistic(
            StatisticsBuilder.Companion.StatisticType.COMBAT_TOTAL_KILLS,
            StatisticsBuilder.Companion.StatisticPosition.LEFT,
            R.string.ships_destroyed,
            totalKills,
            mStatisticSettings.totalKills,
            StatisticsBuilder.Companion.StatisticFormat.INTEGER
        )
        mBuilderCombat.addStatistic(
            StatisticsBuilder.Companion.StatisticType.COMBAT_TOTAL_KILLS,
            StatisticsBuilder.Companion.StatisticPosition.RIGHT,
            R.string.skimmers_killed,
            statistics.combat.skimmersKilled,
            mStatisticSettings.skimmersKilled,
            StatisticsBuilder.Companion.StatisticFormat.INTEGER
        )
        mBuilderCombat.addStatistic(
            StatisticsBuilder.Companion.StatisticType.COMBAT_KILLS,
            StatisticsBuilder.Companion.StatisticPosition.LEFT,
            R.string.bounties,
            statistics.combat.bountiesClaimed,
            mStatisticSettings.bountiesTotal,
            StatisticsBuilder.Companion.StatisticFormat.INTEGER
        )
        mBuilderCombat.addStatistic(
            StatisticsBuilder.Companion.StatisticType.COMBAT_KILLS,
            StatisticsBuilder.Companion.StatisticPosition.CENTER,
            R.string.bonds,
            statistics.combat.combatBonds,
            mStatisticSettings.bondsTotal,
            StatisticsBuilder.Companion.StatisticFormat.INTEGER
        )
        mBuilderCombat.addStatistic(
            StatisticsBuilder.Companion.StatisticType.COMBAT_KILLS,
            StatisticsBuilder.Companion.StatisticPosition.RIGHT,
            R.string.assassinations,
            statistics.combat.assassinations,
            mStatisticSettings.assassinationsTotal,
            StatisticsBuilder.Companion.StatisticFormat.INTEGER
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
            StatisticsBuilder.Companion.StatisticType.EXPLORATION_HYPERSPACE,
            StatisticsBuilder.Companion.StatisticPosition.LEFT,
            R.string.hyperspace_jumps,
            statistics.exploration!!.totalHyperspaceJumps,
            mStatisticSettings.totalHyperspaceJumps,
            StatisticsBuilder.Companion.StatisticFormat.INTEGER
        )

        mBuilderExploration.addStatistic(
            StatisticsBuilder.Companion.StatisticType.EXPLORATION_HYPERSPACE,
            StatisticsBuilder.Companion.StatisticPosition.RIGHT,
            R.string.hyperspace_distance,
            statistics.exploration.totalHyperspaceDistance,
            mStatisticSettings.totalHyperspaceDistance,
            StatisticsBuilder.Companion.StatisticFormat.LIGHTYEAR
        )

        mBuilderExploration.addStatistic(
            StatisticsBuilder.Companion.StatisticType.EXPLORATION_SYSTEMS_VISITED,
            StatisticsBuilder.Companion.StatisticPosition.LEFT,
            R.string.systems_visited,
            statistics.exploration.systemsVisited,
            mStatisticSettings.systemsVisited,
            StatisticsBuilder.Companion.StatisticFormat.INTEGER
        )

        mBuilderExploration.addStatistic(
            StatisticsBuilder.Companion.StatisticType.EXPLORATION_SYSTEMS_VISITED,
            StatisticsBuilder.Companion.StatisticPosition.RIGHT,
            R.string.greatest_distance_from_start,
            statistics.exploration.greatestDistanceFromStart,
            null,
            StatisticsBuilder.Companion.StatisticFormat.LIGHTYEAR,
            StatisticsBuilder.Companion.StatisticColor.DIMMED
        )

        mBuilderExploration.addStatistic(
            StatisticsBuilder.Companion.StatisticType.EXPLORATION_SCANS,
            StatisticsBuilder.Companion.StatisticPosition.LEFT,
            R.string.planets_scanned,
            statistics.exploration.planetsScannedToLevel2,
            mStatisticSettings.planetsScanned,
            StatisticsBuilder.Companion.StatisticFormat.INTEGER
        )

        mBuilderExploration.addStatistic(
            StatisticsBuilder.Companion.StatisticType.EXPLORATION_SCANS,
            StatisticsBuilder.Companion.StatisticPosition.RIGHT,
            R.string.planets_efficient_mapped,
            statistics.exploration.efficientScans,
            mStatisticSettings.planetsEfficientMapped,
            StatisticsBuilder.Companion.StatisticFormat.INTEGER
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
            StatisticsBuilder.Companion.StatisticType.TRADING_MARKETS,
            StatisticsBuilder.Companion.StatisticPosition.LEFT,
            R.string.marketsTradedWith,
            statistics.trading!!.marketsTradedWith,
            mStatisticSettings.marketsTradedWith,
            StatisticsBuilder.Companion.StatisticFormat.INTEGER
        )
        mBuilderTrading.addStatistic(
            StatisticsBuilder.Companion.StatisticType.TRADING_MARKETS,
            StatisticsBuilder.Companion.StatisticPosition.RIGHT,
            R.string.blackMarkets,
            statistics.smuggling!!.blackMarketsTradedWith,
            mStatisticSettings.blackMarketsTradedWith,
            StatisticsBuilder.Companion.StatisticFormat.INTEGER
        )
        mBuilderTrading.addStatistic(
            StatisticsBuilder.Companion.StatisticType.TRADING_RESOURCES,
            StatisticsBuilder.Companion.StatisticPosition.LEFT,
            R.string.resourcesTraded,
            statistics.trading.resourcesTraded,
            null,
            StatisticsBuilder.Companion.StatisticFormat.INTEGER,
            StatisticsBuilder.Companion.StatisticColor.DIMMED
        )
        mBuilderTrading.addStatistic(
            StatisticsBuilder.Companion.StatisticType.TRADING_RESOURCES,
            StatisticsBuilder.Companion.StatisticPosition.RIGHT,
            R.string.smuggled,
            statistics.smuggling.resourcesSmuggled,
            null,
            StatisticsBuilder.Companion.StatisticFormat.INTEGER,
            StatisticsBuilder.Companion.StatisticColor.DIMMED
        )

        mCurrentSettings.marketsTradedWith = statistics.trading.marketsTradedWith
        mCurrentSettings.blackMarketsTradedWith = statistics.smuggling.blackMarketsTradedWith

        mBuilderTrading.post()
    }

    private fun launchPassengerStats(statistics: FrontierStatisticsEvent) {
        mBuilderPassenger.addStatistic(
            StatisticsBuilder.Companion.StatisticType.PASSENGERS_DELIVERED,
            StatisticsBuilder.Companion.StatisticPosition.LEFT,
            R.string.passengers_delivered,
            statistics.passengers!!.passengersMissionsDelivered,
            mStatisticSettings.passengersDelivered,
            StatisticsBuilder.Companion.StatisticFormat.INTEGER
        )

        mBuilderPassenger.addStatistic(
            StatisticsBuilder.Companion.StatisticType.PASSENGERS_DELIVERED,
            StatisticsBuilder.Companion.StatisticPosition.RIGHT,
            R.string.passengers_ejected,
            statistics.passengers.passengersMissionsEjected,
            null,
            StatisticsBuilder.Companion.StatisticFormat.INTEGER,
            StatisticsBuilder.Companion.StatisticColor.DIMMED
        )

        mBuilderPassenger.addStatistic(
            StatisticsBuilder.Companion.StatisticType.PASSENGERS_TYPE,
            StatisticsBuilder.Companion.StatisticPosition.LEFT,
            R.string.passengers_bulk,
            statistics.passengers.passengersMissionsBulk,
            null,
            StatisticsBuilder.Companion.StatisticFormat.INTEGER,
            StatisticsBuilder.Companion.StatisticColor.DIMMED
        )

        mBuilderPassenger.addStatistic(
            StatisticsBuilder.Companion.StatisticType.PASSENGERS_TYPE,
            StatisticsBuilder.Companion.StatisticPosition.RIGHT,
            R.string.passengers_vip,
            statistics.passengers.passengersMissionsVIP,
            null,
            StatisticsBuilder.Companion.StatisticFormat.INTEGER,
            StatisticsBuilder.Companion.StatisticColor.DIMMED
        )

        mCurrentSettings.passengersDelivered = statistics.passengers.passengersMissionsDelivered

        mBuilderPassenger.post()
    }


    class Factory(private val client: CommanderClient?) :
        ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T = MainViewModel(client) as T
    }
}