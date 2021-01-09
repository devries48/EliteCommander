package com.devries48.elitecommander.fragments

import android.icu.text.DecimalFormat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.devries48.elitecommander.R
import com.devries48.elitecommander.frontier.api.CommanderApi
import com.devries48.elitecommander.frontier.api.models.CommanderPosition
import com.devries48.elitecommander.frontier.api.models.Credits
import com.devries48.elitecommander.frontier.api.models.Ranks
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
        // Check download error
        if (!credits.success) {
            //NotificationsUtils.displayGenericDownloadErrorSnackbar(getActivity()) TODO: Error Handling
            return
        }

        // Check error case
        if (credits.balance == -1L) {
            mCredits.value = "Unknown"
            return
        }

        val amount: String = currencyFormat(credits.balance)

        if (credits.loan != 0L) {
            val loan: String = currencyFormat(credits.loan)
            mCredits.value = "$amount credits (with a $loan credits loan)"

        } else {
            mCredits.value = "$amount credits"
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onPositionEvent(position: CommanderPosition) {
        // Check download error
        if (!position.success) {
            //NotificationsUtils.displayGenericDownloadErrorSnackbar(getActivity()) TODO: Error Handling
            return
        }
        mName.value = position.name
        mLocation.value = position.systemName
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
            0
        )
        mTradeRank.value = RankModel(
            NamingUtils.getTradeRankDrawableId(ranks.trade!!.value),
            ranks.trade,
            ranks.trade.name,
            R.string.rank_trading,
            0
        )
        mExploreRank.value = RankModel(
            NamingUtils.getExplorationRankDrawableId(ranks.explore!!.value),
            ranks.explore,
            ranks.explore.name,
            R.string.rank_explore,
            0
        )
        mCqcRank.value = RankModel(
            NamingUtils.getCqcRankDrawableId(ranks.cqc!!.value),
            ranks.cqc,
            ranks.cqc.name,
            R.string.rank_cqc,
            0
        )
        mFederationRank.value =
            RankModel(
                NamingUtils.getFederationRankDrawableId(ranks.federation!!.value),
                ranks.federation,
                ranks.federation.name,
                R.string.rank_federation,
                R.drawable.faction_federation
            )
        mEmpireRank.value =
            RankModel(
                NamingUtils.getEmpireRankDrawableId(ranks.empire!!.value),
                ranks.empire,
                ranks.empire.name,
                R.string.rank_empire,
                R.drawable.faction_empire
            )
    }


    companion object {
        private fun currencyFormat(amount: Long): String {
            val formatter = DecimalFormat("###,###,###,###")
            return formatter.format(amount)
        }

        private val mName = MutableLiveData("")
        private val mCredits = MutableLiveData("")
        private val mLocation = MutableLiveData("")
        private val mCombatRank = MutableLiveData(RankModel(0, Ranks.Rank("", 0, 0), "", R.string.empty_string, 0))
        private val mTradeRank = MutableLiveData(RankModel(0, Ranks.Rank("", 0, 0), "", R.string.empty_string, 0))
        private val mExploreRank = MutableLiveData(RankModel(0, Ranks.Rank("", 0, 0), "", R.string.empty_string, 0))
        private val mCqcRank = MutableLiveData(RankModel(0, Ranks.Rank("", 0, 0), "", R.string.empty_string, 0))
        private val mFederationRank = MutableLiveData(RankModel(0, Ranks.Rank("", 0, 0), "", R.string.empty_string, 0))
        private val mEmpireRank = MutableLiveData(RankModel(0, Ranks.Rank("", 0, 0), "", R.string.empty_string, 0))
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
    val factionBackgroundResId: Int
)