package com.devries48.elitecommander.adapters

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.devries48.elitecommander.R
import com.devries48.elitecommander.models.RankModel

object RankModelAdapter {

    /**
     * A Binding Adapter that is called whenever the value of the attribute `android:rankTextColor`
     * changes. Receives a model that determines the tint color to use.
     */
    @BindingAdapter("android:rankTextColor")
    @JvmStatic
    fun rankTextColor(view: TextView, model: RankModel) {
        val context: Context = view.context
        var color = getAssociatedColor(context, model)

        if (view.id == R.id.titleTextView || view.id == R.id.reputationText || view.id == R.id.repText)
            color = darkenColor(color)

        view.setTextColor(color)

        if (view.ellipsize==TextUtils.TruncateAt.MARQUEE)
        {
            view.isSelected=true
        }
    }

    /**
     * A Binding Adapter that is called whenever the value of the attribute `android:progressTint`
     * changes. Depending on the value it determines the color of the progress bar.
     */
    @BindingAdapter("android:rankProgressTint")
    @JvmStatic
    fun rankProgressTint(view: ProgressBar, model: RankModel) {
        val context: Context = view.context
        var color = getAssociatedColor(context, model)

        if (view.id == R.id.reputationBar)
            color = darkenColor(color)

        view.progressTintList = ColorStateList.valueOf(color)
    }

    /**
     * Binding Adapter to hide a view if the rank is the top-level rank.
     */
    @BindingAdapter("android:rankAutoHide")
    @JvmStatic
    fun rankAutoHide(view: View, model: RankModel) {
        if (view.id == R.id.progressBar && model.name.isEmpty() || view.id == R.id.progressTextView && model.name.isEmpty()) // Alliance has no ranks
            view.visibility = View.GONE
        else {
            val isEndRank =
                !model.isFactionRank && model.rank.value == 8 || model.isFactionRank && model.rank.value == 13
            val isReputationView =
                view.id == R.id.reputationText || view.id == R.id.repText || view.id == R.id.reputationBar

            view.visibility =
                if (isEndRank || isReputationView && !model.isFactionRank || model.rank.name.isEmpty()) View.GONE else View.VISIBLE
        }
    }

    /**
     * Binding Adapter to enlarge the image for faction ranks.
     */
    @BindingAdapter("android:rankIsFaction")
    @JvmStatic
    fun rankIsFaction(view: ImageView, isFaction: Boolean) {
        if (isFaction) {
            val layoutParams = view.layoutParams
            layoutParams.height = 110
            layoutParams.width = 110
            view.layoutParams = layoutParams
        }
    }

    @ColorInt
    private fun darkenColor(@ColorInt color: Int): Int {
        return Color.HSVToColor(FloatArray(3).apply {
            Color.colorToHSV(color, this)
            this[2] *= 0.7f
        })
    }

    private fun getAssociatedColor(context: Context, model: RankModel): Int {
        return when (model.titleResId) {
            R.string.rank_combat -> ContextCompat.getColor(context, R.color.elite_orange)
            R.string.rank_trading -> ContextCompat.getColor(context, R.color.elite_trading)
            R.string.rank_explore -> ContextCompat.getColor(context, R.color.elite_exploration)
            R.string.rank_cqc -> ContextCompat.getColor(context, R.color.elite_cqc)
            R.string.rank_federation -> ContextCompat.getColor(context, R.color.elite_federation)
            R.string.rank_alliance -> ContextCompat.getColor(context, R.color.elite_alliance)
            else -> ContextCompat.getColor(context, R.color.elite_empire)
        }
    }
}