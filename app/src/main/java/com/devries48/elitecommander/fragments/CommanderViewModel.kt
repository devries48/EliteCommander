package com.devries48.elitecommander.fragments

import android.icu.text.DecimalFormat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.devries48.elitecommander.frontier.api.CommanderApi
import com.devries48.elitecommander.frontier.api.models.CommanderPosition
import com.devries48.elitecommander.frontier.api.models.Credits
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


//TODO: use interface as argument!
class CommanderViewModel(api: CommanderApi?) : ViewModel() {

    val name: LiveData<String> = mName
    val credits: LiveData<String> = mCredits
    val location: LiveData<String> = mLocation

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
            Companion.mCredits.value = "Unknown"
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
        mName.value=position.name
        mLocation.value = position.systemName
    }


    companion object {
        private fun currencyFormat(amount: Long): String {
            val formatter = DecimalFormat("###,###,###,###")
            return formatter.format(amount)
        }

        private val mName = MutableLiveData("")
        private val mCredits = MutableLiveData("")
        private val mLocation = MutableLiveData("")
    }
}


class CommanderViewModelFactory(private val api: CommanderApi?) :
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T = CommanderViewModel(api) as T
}
