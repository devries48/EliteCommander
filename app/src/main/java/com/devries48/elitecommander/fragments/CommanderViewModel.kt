package com.devries48.elitecommander.fragments

import android.annotation.SuppressLint
import android.icu.text.DecimalFormat
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.devries48.elitecommander.R
import com.devries48.elitecommander.declarations.default
import com.devries48.elitecommander.events.*
import com.devries48.elitecommander.models.*
import com.devries48.elitecommander.models.ProfitModel.ProfitType.*
import com.devries48.elitecommander.models.StatisticsBuilder.StatisticFormat.CURRENCY
import com.devries48.elitecommander.models.StatisticsBuilder.StatisticPosition.LEFT
import com.devries48.elitecommander.models.StatisticsBuilder.StatisticType.*
import com.devries48.elitecommander.network.CommanderNetwork
import com.devries48.elitecommander.utils.NamingUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.math.round

class CommanderViewModel(network: CommanderNetwork?) : ViewModel() {

    private val commanderApi = network

    val name: LiveData<String> = mName
    val combatRank: LiveData<RankModel> = mCombatRank
    val tradeRank: LiveData<RankModel> = mTradeRank
    val exploreRank: LiveData<RankModel> = mExploreRank
    val cqcRank: LiveData<RankModel> = mCqcRank
    val federationRank: LiveData<RankModel> = mFederationRank
    val empireRank: LiveData<RankModel> = mEmpireRank
    val allianceRank: LiveData<RankModel> = mAllianceRank
    val currentDiscoverySummary: LiveData<FrontierDiscoverySummary> = mCurrentDiscoverySummary
    var isRanksBusy: MutableLiveData<Boolean> = mIsRanksBusy

    init {
        EventBus.getDefault().register(this)
        loadMainStatistics()
    }

    override fun onCleared() {
        super.onCleared()
        EventBus.getDefault().unregister(this)
    }

    internal fun getMainStatistics(): LiveData<List<StatisticModel1>> {
        return mMainStatistics as MutableLiveData<List<StatisticModel1>>
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onFrontierProfileEvent(profile: FrontierProfileEvent) {
        GlobalScope.launch {
            if (!profile.success) {
                //NotificationsUtils.displayGenericDownloadErrorSnackbar(getActivity()) TODO: Error Handling
                return@launch
            }
            launchProfile(profile)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onFrontierRanksEvent(ranks: FrontierRanksEvent) {
        GlobalScope.launch {
            if (!ranks.success) {
                //NotificationsUtils.displayGenericDownloadErrorSnackbar(getActivity()) TODO: Error Handling
                mIsRanksBusy.postValue(false)
                return@launch
            }
            launchRanks(ranks)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onFrontierFleetEvent(fleet: FrontierFleetEvent) {
        GlobalScope.launch {
            if (!fleet.success) {
                //NotificationsUtils.displayGenericDownloadErrorSnackbar(getActivity()) TODO: Error Handling
                return@launch
            }
            launchFleet(fleet)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDistanceSearch(distanceSearch: DistanceSearchEvent) {
        GlobalScope.launch {
            if (!distanceSearch.success) {
                //NotificationsUtils.displayGenericDownloadErrorSnackbar(getActivity()) TODO: Error Handling
                return@launch
            }
            launchDistanceSearch(distanceSearch)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCurrentDiscoveries(discoveries: FrontierDiscoveriesEvent) {
        GlobalScope.launch {
            if (!discoveries.success) {
                //NotificationsUtils.displayGenericDownloadErrorSnackbar(getActivity()) TODO: Error Handling
                return@launch
            }
            launchCurrentDiscoveries(discoveries)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onStatistics(statistics: FrontierStatisticsEvent) {
        GlobalScope.launch {
            if (!statistics.success) {
                //NotificationsUtils.displayGenericDownloadErrorSnackbar(getActivity()) TODO: Error Handling
                return@launch
            }

            launchProfitChart(statistics)
            launchProfitStats(statistics)
        }
    }


    private fun launchProfile(profile: FrontierProfileEvent) {
        mName.postValue(profile.name)
        setMainStatistic(R.string.CurrentLocation, profile.systemName)
        commanderApi?.getDistanceToSol(profile.systemName)

        // Hull damage
        val hullPercentage: Int = profile.hull / 10000

        setMainStatisticRight(
            R.string.CurrentShip,
            R.string.hull,
            "$hullPercentage%",
            if (hullPercentage >= 50) R.style.eliteStyle_LightOrangeText else R.style.eliteStyle_RedText
        )

        // Integrity
        val integrityPercentage: Int = profile.integrity / 10000

        setMainStatisticMiddle(
            R.string.CurrentShip,
            R.string.integrity,
            "$integrityPercentage%",
            if (integrityPercentage >= 50) R.style.eliteStyle_LightOrangeText else R.style.eliteStyle_RedText
        )

        // Check error case
        if (profile.balance == -1L) {
            setMainStatistic(R.string.Credits, "Unknown")
            return
        }

        val amount = currencyFormat(profile.balance)

        if (profile.loan != 0L) {
            val loan: String = currencyFormat(profile.loan)
            setMainStatistic(R.string.Credits, "$amount CR (with a $loan CR loan)")
        } else {
            setMainStatistic(R.string.Credits, "$amount CR")
        }
    }

    private fun launchRanks(ranks: FrontierRanksEvent) {
        mCombatRank.postValue(
            RankModel(
                NamingUtils.getCombatRankDrawableId(ranks.combat!!.value),
                ranks.combat,
                ranks.combat.name,
                R.string.rank_combat
            )
        )
        mTradeRank.postValue(
            RankModel(
                NamingUtils.getTradeRankDrawableId(ranks.trade!!.value),
                ranks.trade,
                ranks.trade.name,
                R.string.rank_trading
            )
        )

        mExploreRank.postValue(
            RankModel(
                NamingUtils.getExplorationRankDrawableId(ranks.explore!!.value),
                ranks.explore,
                ranks.explore.name,
                R.string.rank_explore
            )
        )
        mCqcRank.postValue(
            RankModel(
                NamingUtils.getCqcRankDrawableId(ranks.cqc!!.value),
                ranks.cqc,
                ranks.cqc.name,
                R.string.rank_cqc
            )
        )
        mFederationRank.postValue(
            RankModel(
                NamingUtils.getFederationRankDrawableId(ranks.federation!!.value),
                ranks.federation,
                ranks.federation.name,
                R.string.rank_federation,
                true
            )
        )
        mEmpireRank.postValue(
            RankModel(
                NamingUtils.getEmpireRankDrawableId(ranks.empire!!.value),
                ranks.empire,
                ranks.empire.name,
                R.string.rank_empire,
                true
            )
        )
        mAllianceRank.postValue(
            RankModel(
                NamingUtils.getAllianceRankDrawableId(),
                ranks.alliance!!,
                "",
                R.string.rank_alliance,
                true
            )
        )

        mIsRanksBusy.postValue(false)
    }

    private fun launchFleet(fleet: FrontierFleetEvent) {
        var assetsValue: Long = 0

        if (fleet.frontierShips.any()) {
            fleet.frontierShips.forEach {
                if (it.isCurrentShip) {
                    setMainStatistic(R.string.CurrentShip, it.model)
                }
                assetsValue += it.totalValue
            }

            val assets: String = currencyFormat(assetsValue)
            setMainStatistic(R.string.AssetsValue, "$assets CR")
        }
    }

    private fun launchDistanceSearch(distanceSearch: DistanceSearchEvent) {
        if (distanceSearch.distance == 0.0) {
            setMainStatisticRight(
                R.string.CurrentLocation,
                0,
                "Discovered",
                R.style.eliteStyle_LightOrangeText
            )
        } else {
            setMainStatisticRight(
                R.string.CurrentLocation,
                R.string.distance_sol,
                "${doubleFormat(distanceSearch.distance)} LY",
                R.style.eliteStyle_LightOrangeText
            )
        }
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
                round((statistics.smuggling.blackMarketsProfits / total.toFloat()) * 1000) / 10
            )
        }

        val rescueProfit = statistics.searchAndRescue?.let {
            ProfitModel(
                SEARCH_RESCUE,
                it.searchRescueProfit,
                round((statistics.searchAndRescue.searchRescueProfit / total.toFloat()) * 1000) / 10
            )
        }

        val models = arrayListOf(
            ProfitModel(EXPLORATION, statistics.exploration.explorationProfits),
            ProfitModel(COMBAT, combatTotal, round((combatTotal / total.toFloat()) * 1000) / 10),
            ProfitModel(
                TRADING,
                statistics.trading.marketProfits,
                round((statistics.trading.marketProfits / total.toFloat()) * 1000) / 10
            ),
            ProfitModel(
                MINING,
                statistics.trading.marketProfits,
                round((statistics.mining.miningProfits / total.toFloat()) * 1000) / 10
            ),
            smugglingProfit!!,
            rescueProfit!!
        )

        var lowPercentage = 0f
        var lowTotal = 0L
        var percentageTotal = 0f

        models.forEach {
            if (it.percentage < 1.0) {
                lowPercentage += it.percentage
                lowTotal += it.amount
            }
            percentageTotal += it.percentage
        }
        models.first().percentage = 100f - percentageTotal

        if (lowPercentage >= 1.0) {
            models.removeAll { it.percentage < 1.0 }

            models.add(
                ProfitModel(
                    OTHER,
                    lowTotal,
                    lowPercentage
                )
            )
        }

        mProfitChart.postValue(models)
    }

    private fun launchProfitStats(statistics: FrontierStatisticsEvent) {
        // Profit stats
        mBuilderProfit.addStatistic(
            PROFIT_COMBAT_BOUNTIES,
            LEFT,
            R.string.bounties,
            statistics.combat!!.bountyHuntingProfit,
            true,
            CURRENCY
        )
        mBuilderProfit.addStatistic(
            PROFIT_COMBAT_BONDS,
            LEFT,
            R.string.combat_bonds,
            statistics.combat.combatBondProfits,
            true,
            CURRENCY
        )
        mBuilderProfit.addStatistic(
            PROFIT_COMBAT_ASSASSINATIONS,
            LEFT,
            R.string.assassinations,
            statistics.combat.assassinationProfits,
            true,
            CURRENCY
        )
        mBuilderProfit.addStatistic(
            PROFIT_TRADING,
            LEFT,
            R.string.trading,
            statistics.trading!!.marketProfits,
            true,
            CURRENCY
        )
        mBuilderProfit.addStatistic(
            PROFIT_EXPLORATION,
            LEFT,
            R.string.exploration,
            statistics.exploration!!.explorationProfits,
            true,
            CURRENCY
        )
        mBuilderProfit.addStatistic(
            PROFIT_SMUGGLING,
            LEFT,
            R.string.smuggling,
            statistics.smuggling!!.blackMarketsProfits,
            true,
            CURRENCY
        )
        mBuilderProfit.addStatistic(
            PROFIT_SEARCH_RESCUE,
            LEFT,
            R.string.search_rescue,
            statistics.searchAndRescue!!.searchRescueProfit,
            true,
            CURRENCY
        )

        mBuilderProfit.postValues()
    }


    companion object {
        private val mName = MutableLiveData("")
        private val mIsRanksBusy = MutableLiveData<Boolean>().default(true)

        private val mCombatRank =
            MutableLiveData(RankModel(0, FrontierRanksEvent.FrontierRank("", 0, 0), "", R.string.empty_string))
        private val mTradeRank =
            MutableLiveData(RankModel(0, FrontierRanksEvent.FrontierRank("", 0, 0), "", R.string.empty_string))
        private val mExploreRank =
            MutableLiveData(RankModel(0, FrontierRanksEvent.FrontierRank("", 0, 0), "", R.string.empty_string))
        private val mCqcRank =
            MutableLiveData(RankModel(0, FrontierRanksEvent.FrontierRank("", 0, 0), "", R.string.empty_string))
        private val mFederationRank =
            MutableLiveData(RankModel(0, FrontierRanksEvent.FrontierRank("", 0, 0), "", R.string.empty_string, true))
        private val mEmpireRank =
            MutableLiveData(RankModel(0, FrontierRanksEvent.FrontierRank("", 0, 0), "", R.string.empty_string, true))
        private val mAllianceRank =
            MutableLiveData(RankModel(0, FrontierRanksEvent.FrontierRank("", 0, 0), "", R.string.empty_string, true))

        private var mMainStatistics: MutableLiveData<List<StatisticModel1>>? = null
        private val mMainStatisticsList = ArrayList<StatisticModel1>()
        private var mCurrentDiscoveries = MutableLiveData<List<FrontierDiscovery>>()
        private var mCurrentDiscoverySummary = MutableLiveData(FrontierDiscoverySummary(0, 0, 0, 0, 0, 0, 0, 0, 0, 0))

        private var mProfitChart = MutableLiveData<List<ProfitModel>>()
        private val mBuilderProfit: StatisticsBuilder = StatisticsBuilder()

        private fun currencyFormat(amount: Long): String {
            val formatter = DecimalFormat("###,###,###,###")
            return formatter.format(amount)
        }

        private fun doubleFormat(value: Double): String {
            val formatter = DecimalFormat("###,###,###.#")
            return formatter.format(value)
        }

        private fun loadMainStatistics() {
            if (mMainStatistics == null) {
                mMainStatistics = MutableLiveData()

                mMainStatisticsList.add(StatisticModel1(R.string.Credits))
                mMainStatisticsList.add(StatisticModel1(R.string.AssetsValue))
                mMainStatisticsList.add(StatisticModel1(R.string.CurrentLocation))
                mMainStatisticsList.add(StatisticModel1(R.string.CurrentShip))

                mMainStatistics!!.value = mMainStatisticsList
            }
        }

        private fun setMainStatistic(@StringRes stringRes: Int, value: String) {
            val stat: StatisticModel1? = mMainStatisticsList.find { it.stringRes == stringRes }
            if (stat != null) {
                stat.value = value
                mMainStatistics!!.postValue(mMainStatisticsList)
            }
        }

        private fun setMainStatisticMiddle(
            @StringRes nameRes: Int,
            @StringRes middleStringRes: Int,
            middleValue: String,
            @StyleRes middleValueStyleRes: Int
        ) {
            val stat: StatisticModel1? = mMainStatisticsList.find { it.stringRes == nameRes }
            if (stat != null) {
                stat.middleValue = middleValue
                stat.middleStringRes = middleStringRes
                stat.middleValueStyleRes = middleValueStyleRes
                mMainStatistics!!.postValue(mMainStatisticsList)
            }
        }

        private fun setMainStatisticRight(
            @StringRes nameRes: Int,
            @StringRes rightStringRes: Int,
            rightValue: String,
            @StyleRes rightValueStyleRes: Int
        ) {
            val stat: StatisticModel1? = mMainStatisticsList.find { it.stringRes == nameRes }
            if (stat != null) {
                stat.rightValue = rightValue
                stat.rightStringRes = rightStringRes
                stat.rightValueStyleRes = rightValueStyleRes
                mMainStatistics!!.postValue(mMainStatisticsList)
            }
        }
    }
}

class CommanderViewModelFactory(private val network: CommanderNetwork?) :
    ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T = CommanderViewModel(network) as T
}