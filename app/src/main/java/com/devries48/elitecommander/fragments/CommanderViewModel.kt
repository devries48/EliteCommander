package com.devries48.elitecommander.fragments

import android.icu.text.DecimalFormat
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.devries48.elitecommander.R
import com.devries48.elitecommander.events.FrontierCreditsEvent
import com.devries48.elitecommander.events.FrontierFleetEvent
import com.devries48.elitecommander.events.FrontierProfileEvent
import com.devries48.elitecommander.events.FrontierRanksEvent
import com.devries48.elitecommander.models.EliteStatistic
import com.devries48.elitecommander.network.CommanderApi
import com.devries48.elitecommander.utils.NamingUtils
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


//TODO: use interface as argument!
class CommanderViewModel(api: CommanderApi?) : ViewModel() {

    val name: LiveData<String> = mName
    val combatRank: LiveData<RankModel> = mCombatRank
    val tradeRank: LiveData<RankModel> = mTradeRank
    val exploreRank: LiveData<RankModel> = mExploreRank
    val cqcRank: LiveData<RankModel> = mCqcRank
    val federationRank: LiveData<RankModel> = mFederationRank
    val empireRank: LiveData<RankModel> = mEmpireRank

    internal fun getMainStatistics(): LiveData<List<EliteStatistic>> {
        return mMainStatistics as MutableLiveData<List<EliteStatistic>>
    }

    init {
        EventBus.getDefault().register(this)
        loadMainStatistics()
        api?.getCommanderStatus()
    }

    override fun onCleared() {
        super.onCleared()

        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onFrontierCreditsEvent(frontierCreditsEvent: FrontierCreditsEvent) {
        val resId = R.string.Credits

        // Check download error
        if (!frontierCreditsEvent.success) {
            //NotificationsUtils.displayGenericDownloadErrorSnackbar(getActivity()) TODO: Error Handling
            return
        }

        // Check error case
        if (frontierCreditsEvent.balance == -1L) {
            setMainStatistic(resId, "Unknown")
            return
        }

        val amount: String = currencyFormat(frontierCreditsEvent.balance)

        if (frontierCreditsEvent.loan != 0L) {
            val loan: String = currencyFormat(frontierCreditsEvent.loan)
            setMainStatistic(resId, "$amount CR (with a $loan CR loan)")
        } else {
            setMainStatistic(resId, "$amount CR")
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onFrontierProfileEvent(profileEvent: FrontierProfileEvent) {
        if (!profileEvent.success) {
            //NotificationsUtils.displayGenericDownloadErrorSnackbar(getActivity()) TODO: Error Handling
            return
        }

        mName.value = profileEvent.name
        setMainStatistic(R.string.CurrentLocation, profileEvent.systemName)

        // Check error case
        if (profileEvent.balance == -1L) {
            setMainStatistic(R.string.Credits, "Unknown")
            return
        }

        val amount: String = currencyFormat(profileEvent.balance)

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
            R.string.rank_combat,
            true
        )
        mTradeRank.value = RankModel(
            NamingUtils.getTradeRankDrawableId(ranksEvent.trade!!.value),
            ranksEvent.trade,
            ranksEvent.trade.name,
            R.string.rank_trading,
            true
        )
        mExploreRank.value = RankModel(
            NamingUtils.getExplorationRankDrawableId(ranksEvent.explore!!.value),
            ranksEvent.explore,
            ranksEvent.explore.name,
            R.string.rank_explore,
            true
        )
        mCqcRank.value = RankModel(
            NamingUtils.getCqcRankDrawableId(ranksEvent.cqc!!.value),
            ranksEvent.cqc,
            ranksEvent.cqc.name,
            R.string.rank_cqc,
            true
        )
        mFederationRank.value =
            RankModel(
                NamingUtils.getFederationRankDrawableId(ranksEvent.federation!!.value),
                ranksEvent.federation,
                ranksEvent.federation.name,
                R.string.rank_federation,
                false
            )
        mEmpireRank.value =
            RankModel(
                NamingUtils.getEmpireRankDrawableId(ranksEvent.empire!!.value),
                ranksEvent.empire,
                ranksEvent.empire.name,
                R.string.rank_empire,
                false
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

    companion object {

        private val mName = MutableLiveData("")

        private val mCombatRank =
            MutableLiveData(
                RankModel(
                    0,
                    FrontierRanksEvent.FrontierRank("", 0, 0),
                    "",
                    R.string.empty_string,
                    false
                )
            )
        private val mTradeRank =
            MutableLiveData(
                RankModel(
                    0,
                    FrontierRanksEvent.FrontierRank("", 0, 0),
                    "",
                    R.string.empty_string,
                    false
                )
            )
        private val mExploreRank =
            MutableLiveData(
                RankModel(
                    0,
                    FrontierRanksEvent.FrontierRank("", 0, 0),
                    "",
                    R.string.empty_string,
                    false
                )
            )
        private val mCqcRank =
            MutableLiveData(
                RankModel(
                    0,
                    FrontierRanksEvent.FrontierRank("", 0, 0),
                    "",
                    R.string.empty_string,
                    false
                )
            )
        private val mFederationRank =
            MutableLiveData(
                RankModel(
                    0,
                    FrontierRanksEvent.FrontierRank("", 0, 0),
                    "",
                    R.string.empty_string,
                    false
                )
            )
        private val mEmpireRank =
            MutableLiveData(
                RankModel(
                    0,
                    FrontierRanksEvent.FrontierRank("", 0, 0),
                    "",
                    R.string.empty_string,
                    false
                )
            )

        private var mMainStatistics: MutableLiveData<List<EliteStatistic>>? = null
        private val mMainStatisticsList = ArrayList<EliteStatistic>()

        private fun currencyFormat(amount: Long): String {
            val formatter = DecimalFormat("###,###,###,###")
            return formatter.format(amount)
        }

        private fun loadMainStatistics() {
            if (mMainStatistics == null) {
                mMainStatistics = MutableLiveData()

                mMainStatisticsList.add(EliteStatistic(R.string.Credits))
                mMainStatisticsList.add(EliteStatistic(R.string.AssetsValue))
                mMainStatisticsList.add(EliteStatistic(R.string.CurrentLocation))
                mMainStatisticsList.add(EliteStatistic(R.string.CurrentShip))

                mMainStatistics!!.value = mMainStatisticsList
            }
        }

        private fun setMainStatistic(@StringRes stringRes: Int, value: String) {
            val stat: EliteStatistic? = mMainStatisticsList.find { it.stringRes == stringRes }
            if (stat != null) {
                stat.value = value
                mMainStatistics!!.postValue( mMainStatisticsList)
            }
        }

    }
}

class CommanderViewModelFactory(private val api: CommanderApi?) :
    ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T = CommanderViewModel(api) as T
}

data class RankModel(
    val logoResId: Int,
    val rank: FrontierRanksEvent.FrontierRank,
    val name: String,
    val titleResId: Int,
    val isPlayerRank: Boolean
)