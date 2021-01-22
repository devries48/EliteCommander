package com.devries48.elitecommander.adapters

import android.content.Context
import android.content.res.ColorStateList
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.devries48.elitecommander.R
import com.devries48.elitecommander.models.RankModel

object RankModelAdapter {

    /**
     * A Binding Adapter that is called whenever the value of the attribute `app:titleTextColor`
     * changes. Receives a popularity level that determines the icon and tint color to use.
     */
    @BindingAdapter("android:rankTextColor")
    @JvmStatic
    fun rankTextColor(view: TextView, model: RankModel) {
        val context: Context = view.context
        view.setTextColor(getAssociatedColor(context, model))
    }

    /**
     * A Binding Adapter that is called whenever the value of the attribute `android:progressTint`
     * changes. Depending on the value it determines the color of the progress bar.
     */
    @BindingAdapter("android:rankProgressTint")
    @JvmStatic
    fun rankProgressTint(view: ProgressBar, model: RankModel) {
        val context: Context = view.context
        view.progressTintList = ColorStateList.valueOf(getAssociatedColor(context, model))
    }

    /**
     * Binding Adapter to hide a view if the number is hundred.
     */
    @BindingAdapter("android:rankProgressHideIf100")
    @JvmStatic
    fun rankProgressHideIf100(view: ProgressBar, model: RankModel) {
        view.visibility = if (model.rank.progress == 100) View.GONE else View.VISIBLE
    }

    /**
     * Binding Adapter to hide a view if the number is hundred.
     */
    @BindingAdapter("android:rankTextHideIf100")
    @JvmStatic
    fun rankTextHideIf100(view: TextView, model: RankModel) {
        view.visibility = if (model.rank.progress == 100) View.GONE else View.VISIBLE
    }

    private fun getAssociatedColor(context: Context, model: RankModel): Int {
        return when (model.titleResId) {
            R.string.rank_combat -> ContextCompat.getColor(context, R.color.elite_orange)
            R.string.rank_trading -> ContextCompat.getColor(context, R.color.white)
            R.string.rank_explore -> ContextCompat.getColor(context, R.color.elite_teal)
            R.string.rank_cqc -> ContextCompat.getColor(context, R.color.elite_red)
            R.string.rank_federation -> ContextCompat.getColor(context, R.color.elite_federation)
            else -> ContextCompat.getColor(context, R.color.elite_empire)
        }
    }
}