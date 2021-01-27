package com.devries48.elitecommander.fragments

import android.icu.text.DecimalFormat
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.devries48.elitecommander.R
import com.devries48.elitecommander.events.DistanceSearchEvent
import com.devries48.elitecommander.events.FrontierFleetEvent
import com.devries48.elitecommander.events.FrontierProfileEvent
import com.devries48.elitecommander.events.FrontierRanksEvent
import com.devries48.elitecommander.models.EliteStatisticModel
import com.devries48.elitecommander.models.RankModel
import com.devries48.elitecommander.network.CommanderApi
import com.devries48.elitecommander.utils.NamingUtils
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


//TODO: use interface as argument!
class CommanderViewModel(api: CommanderApi?) : ViewModel() {
    private val commanderApi = api

    val name: LiveData<String> = mName
    val combatRank: LiveData<RankModel> = mCombatRank
    val tradeRank: LiveData<RankModel> = mTradeRank
    val exploreRank: LiveData<RankModel> = mExploreRank
    val cqcRank: LiveData<RankModel> = mCqcRank
    val federationRank: LiveData<RankModel> = mFederationRank
    val empireRank: LiveData<RankModel> = mEmpireRank

    internal fun getMainStatistics(): LiveData<List<EliteStatisticModel>> {
        return mMainStatistics as MutableLiveData<List<EliteStatisticModel>>
    }

    init {
        EventBus.getDefault().register(this)
        loadMainStatistics()

//        val journals = FrontierJournal()
//        journals.parseResponse("")
//        var stats: FrontierJournalStatisticsResponse? = journals.getStatistics()
//        println("COMMANDER STATS" + (stats?.bankAccount?.currentWealth ?: ""))

        commanderApi?.loadProfile()
        commanderApi?.loadRanks()
    }

    override fun onCleared() {
        super.onCleared()

        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onFrontierProfileEvent(profileEvent: FrontierProfileEvent) {
        if (!profileEvent.success) {
            //NotificationsUtils.displayGenericDownloadErrorSnackbar(getActivity()) TODO: Error Handling
            return
        }

        mName.value = profileEvent.name
        setMainStatistic(R.string.CurrentLocation, profileEvent.systemName)
        commanderApi?.getDistanceToSol(profileEvent.systemName)

        // Hull damage

        val hullPercentage:Int=profileEvent.hull/10000

        setMainStatisticRight(
            R.string.CurrentShip,
            R.string.hull,
            "$hullPercentage%",
            R.style.eliteStyle_LightOrangeText
        )


        // Check error case
        if (profileEvent.balance == -1L) {
            setMainStatistic(R.string.Credits, "Unknown")
            return
        }

        val amount = currencyFormat(profileEvent.balance)

        if (profileEvent.loan != 0L) {
            val loan: String = currencyFormat(profileEvent.loan)
            setMainStatistic(R.string.Credits, "$amount CR (with a $loan CR loan)")
        } else {
            setMainStatistic(R.string.Credits, "$amount CR")
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onFrontierRanksEvent(ranksEvent: FrontierRanksEvent) {

        // Check download error
        if (!ranksEvent.success) {
            //NotificationsUtils.displayGenericDownloadErrorSnackbar(getActivity()) TODO: Error Handling
            return
        }

        mCombatRank.value = RankModel(
            NamingUtils.getCombatRankDrawableId(ranksEvent.combat!!.value),
            ranksEvent.combat,
            ranksEvent.combat.name,
            R.string.rank_combat
        )
        mTradeRank.value = RankModel(
            NamingUtils.getTradeRankDrawableId(ranksEvent.trade!!.value),
            ranksEvent.trade,
            ranksEvent.trade.name,
            R.string.rank_trading
        )
        mExploreRank.value = RankModel(
            NamingUtils.getExplorationRankDrawableId(ranksEvent.explore!!.value),
            ranksEvent.explore,
            ranksEvent.explore.name,
            R.string.rank_explore
        )
        mCqcRank.value = RankModel(
            NamingUtils.getCqcRankDrawableId(ranksEvent.cqc!!.value),
            ranksEvent.cqc,
            ranksEvent.cqc.name,
            R.string.rank_cqc
        )
        mFederationRank.value =
            RankModel(
                NamingUtils.getFederationRankDrawableId(ranksEvent.federation!!.value),
                ranksEvent.federation,
                ranksEvent.federation.name,
                R.string.rank_federation,
                true
            )
        mEmpireRank.value =
            RankModel(
                NamingUtils.getEmpireRankDrawableId(ranksEvent.empire!!.value),
                ranksEvent.empire,
                ranksEvent.empire.name,
                R.string.rank_empire,
                true
            )
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onFrontierFleetEvent(fleetEvent: FrontierFleetEvent) {
        if (!fleetEvent.success) {
            //NotificationsUtils.displayGenericDownloadErrorSnackbar(getActivity()) TODO: Error Handling
            return
        }

        var assetsValue: Long = 0

        if (fleetEvent.frontierShips.any()) {
            fleetEvent.frontierShips.forEach {
                if (it.isCurrentShip) {
                    setMainStatistic(R.string.CurrentShip, it.model)
                }
                assetsValue += it.totalValue
            }

            val assets: String = currencyFormat(assetsValue)
            setMainStatistic(R.string.AssetsValue, "$assets CR")
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDistanceSearch(distanceSearch: DistanceSearchEvent) {
        if (!distanceSearch.success) {
            //NotificationsUtils.displayGenericDownloadErrorSnackbar(getActivity()) TODO: Error Handling
            return
        }

        if (distanceSearch.distance == 0.0f) {
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
                "${floatFormat(distanceSearch.distance)} LY",
                R.style.eliteStyle_LightOrangeText
            )
        }
    }

    companion object {

        private val mName = MutableLiveData("")

        private val mCombatRank =
            MutableLiveData(
                RankModel(
                    0,
                    FrontierRanksEvent.FrontierRank("", 0, 0),
                    "",
                    R.string.empty_string
                )
            )
        private val mTradeRank =
            MutableLiveData(
                RankModel(
                    0,
                    FrontierRanksEvent.FrontierRank("", 0, 0),
                    "",
                    R.string.empty_string
                )
            )
        private val mExploreRank =
            MutableLiveData(
                RankModel(
                    0,
                    FrontierRanksEvent.FrontierRank("", 0, 0),
                    "",
                    R.string.empty_string
                )
            )
        private val mCqcRank =
            MutableLiveData(
                RankModel(
                    0,
                    FrontierRanksEvent.FrontierRank("", 0, 0),
                    "",
                    R.string.empty_string
                )
            )
        private val mFederationRank =
            MutableLiveData(
                RankModel(
                    0,
                    FrontierRanksEvent.FrontierRank("", 0, 0),
                    "",
                    R.string.empty_string,
                    true
                )
            )
        private val mEmpireRank =
            MutableLiveData(
                RankModel(
                    0,
                    FrontierRanksEvent.FrontierRank("", 0, 0),
                    "",
                    R.string.empty_string,
                    true
                )
            )

        private var mMainStatistics: MutableLiveData<List<EliteStatisticModel>>? = null
        private val mMainStatisticsList = ArrayList<EliteStatisticModel>()

        private fun currencyFormat(amount: Long): String {
            val formatter = DecimalFormat("###,###,###,###")
            return formatter.format(amount)
        }

        private fun floatFormat(value: Float): String {
            val formatter = DecimalFormat("###,###,###.#")
            return formatter.format(value)
        }

        private fun loadMainStatistics() {
            if (mMainStatistics == null) {
                mMainStatistics = MutableLiveData()

                mMainStatisticsList.add(EliteStatisticModel(R.string.Credits))
                mMainStatisticsList.add(EliteStatisticModel(R.string.AssetsValue))
                mMainStatisticsList.add(EliteStatisticModel(R.string.CurrentLocation))
                mMainStatisticsList.add(EliteStatisticModel(R.string.CurrentShip))

                mMainStatistics!!.value = mMainStatisticsList
            }
        }

        private fun setMainStatistic(@StringRes stringRes: Int, value: String) {
            val stat: EliteStatisticModel? = mMainStatisticsList.find { it.stringRes == stringRes }
            if (stat != null) {
                stat.value = value
                mMainStatistics!!.postValue(mMainStatisticsList)
            }
        }

        private fun setMainStatisticRight(
            @StringRes nameRes: Int,
            @StringRes rightStringRes: Int,
            rightValue: String,
            @StyleRes rightValueStyleRes: Int
        ) {
            val stat: EliteStatisticModel? = mMainStatisticsList.find { it.stringRes == nameRes }
            if (stat != null) {
                stat.rightValue = rightValue
                stat.rightStringRes = rightStringRes
                stat.rightValueStyleRes = rightValueStyleRes
                mMainStatistics!!.postValue(mMainStatisticsList)
            }
        }

    }
}

class CommanderViewModelFactory(private val api: CommanderApi?) :
    ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T = CommanderViewModel(api) as T
}

