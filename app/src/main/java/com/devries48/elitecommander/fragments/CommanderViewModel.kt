package com.devries48.elitecommander.fragments

import android.icu.text.DecimalFormat
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.devries48.elitecommander.R
import com.devries48.elitecommander.frontier.api.CommanderApi
import com.devries48.elitecommander.frontier.api.events.CommanderPosition
import com.devries48.elitecommander.frontier.api.events.Credits
import com.devries48.elitecommander.frontier.api.events.Ranks
import com.devries48.elitecommander.frontier.api.models.EliteStatistic
import com.devries48.elitecommander.utils.NamingUtils
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


//TODO: use interface as argument!
class CommanderViewModel(api: CommanderApi?) : ViewModel() {

    val name: LiveData<String> = mName
    val credits: LiveData<String> = mCredits
    val location: LiveData<String> = mLocation
    val combatRank: LiveData<RankModel> = mCombatRank
    val tradeRank: LiveData<RankModel> = mTradeRank
    val exploreRank: LiveData<RankModel> = mExploreRank
    val cqcRank: LiveData<RankModel> = mCqcRank
    val federationRank: LiveData<RankModel> = mFederationRank
    val empireRank: LiveData<RankModel> = mEmpireRank

    internal fun getMainStatistics(): MutableLiveData<List<EliteStatistic>> {
        if (mMainStatistics == null) {
            mMainStatistics = MutableLiveData()
            mMainStatistics!!.postValue(mMainStatisticsList)
            loadMainStatistics()
        }
        return mMainStatistics as MutableLiveData<List<EliteStatistic>>
    }


    init {
        EventBus.getDefault().register(this)

        api?.getCommanderStatus()
    }

    override fun onCleared() {
        super.onCleared()

        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCreditsEvent(credits: Credits) {
        val resId = R.string.Credits

        // Check download error
        if (!credits.success) {
            //NotificationsUtils.displayGenericDownloadErrorSnackbar(getActivity()) TODO: Error Handling
            return
        }

        // Check error case
        if (credits.balance == -1L) {
            setMainStatistic(resId, "Unknown")
            return
        }

        val amount: String = currencyFormat(credits.balance)

        if (credits.loan != 0L) {
            val loan: String = currencyFormat(credits.loan)
            setMainStatistic(resId, "$amount CR (with a $loan CR loan)")
        } else {
            setMainStatistic(resId, "$amount CR")
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onPositionEvent(position: CommanderPosition) {
        val resId = R.string.CurrentLocation

        // Check download error
        if (!position.success) {
            //NotificationsUtils.displayGenericDownloadErrorSnackbar(getActivity()) TODO: Error Handling
            return
        }
        setMainStatistic(resId, position.systemName)
        // Get the commander name
        mName.value = position.name
        //mLocation.value = position.systemName
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRanksEvents(ranks: Ranks) {

        // Check download error
        if (!ranks.success) {
            //NotificationsUtils.displayGenericDownloadErrorSnackbar(getActivity()) TODO: Error Handling
            return
        }

        mCombatRank.value = RankModel(
            NamingUtils.getCombatRankDrawableId(ranks.combat!!.value),
            ranks.combat,
            ranks.combat.name,
            R.string.rank_combat,
            true
        )
        mTradeRank.value = RankModel(
            NamingUtils.getTradeRankDrawableId(ranks.trade!!.value),
            ranks.trade,
            ranks.trade.name,
            R.string.rank_trading,
            true
        )
        mExploreRank.value = RankModel(
            NamingUtils.getExplorationRankDrawableId(ranks.explore!!.value),
            ranks.explore,
            ranks.explore.name,
            R.string.rank_explore,
            true
        )
        mCqcRank.value = RankModel(
            NamingUtils.getCqcRankDrawableId(ranks.cqc!!.value),
            ranks.cqc,
            ranks.cqc.name,
            R.string.rank_cqc,
            true
        )
        mFederationRank.value =
            RankModel(
                NamingUtils.getFederationRankDrawableId(ranks.federation!!.value),
                ranks.federation,
                ranks.federation.name,
                R.string.rank_federation,
                false
            )
        mEmpireRank.value =
            RankModel(
                NamingUtils.getEmpireRankDrawableId(ranks.empire!!.value),
                ranks.empire,
                ranks.empire.name,
                R.string.rank_empire,
                false
            )
    }


    companion object {

        private val mName = MutableLiveData("")
        private val mCredits = MutableLiveData("")
        private val mLocation = MutableLiveData("")

        private val mCombatRank =
            MutableLiveData(RankModel(0, Ranks.Rank("", 0, 0), "", R.string.empty_string, false))
        private val mTradeRank =
            MutableLiveData(RankModel(0, Ranks.Rank("", 0, 0), "", R.string.empty_string, false))
        private val mExploreRank =
            MutableLiveData(RankModel(0, Ranks.Rank("", 0, 0), "", R.string.empty_string, false))
        private val mCqcRank =
            MutableLiveData(RankModel(0, Ranks.Rank("", 0, 0), "", R.string.empty_string, false))
        private val mFederationRank =
            MutableLiveData(RankModel(0, Ranks.Rank("", 0, 0), "", R.string.empty_string, false))
        private val mEmpireRank =
            MutableLiveData(RankModel(0, Ranks.Rank("", 0, 0), "", R.string.empty_string, false))

        private var mMainStatistics: MutableLiveData<List<EliteStatistic>>? = null
        private val mMainStatisticsList = ArrayList<EliteStatistic>()

        private fun currencyFormat(amount: Long): String {
            val formatter = DecimalFormat("###,###,###,###")
            return formatter.format(amount)
        }

        private fun loadMainStatistics() {
            mMainStatisticsList.add(EliteStatistic(R.string.Credits))
            mMainStatisticsList.add(EliteStatistic(R.string.CurrentLocation))
        }

        private fun setMainStatistic(@StringRes stringRes: Int, value: String) {
            val stat: EliteStatistic? = mMainStatisticsList.find { it.stringRes == stringRes }
if (stat != null){
    stat.value=value
    mMainStatistics!!.postValue(mMainStatisticsList) //for background postValue

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
    val rank: Ranks.Rank,
    val name: String,
    val titleResId: Int,
    val isPlayerRank: Boolean
)