package com.devries48.elitecommander.adapters

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.text.TextUtils
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.devries48.elitecommander.R
import com.devries48.elitecommander.models.RankModel

object RanksAdapter {

    /**
     * A Binding Adapter that is called whenever the value of the attribute `android:rankTextColor`
     * changes. Receives a model that determines the tint color to use.
     */
    @BindingAdapter("android:rankTextColor")
    @JvmStatic
    fun rankTextColor(view: TextView, model: RankModel) {
        val context: Context = view.context
        val color = getAssociatedColor(context, model, view)

        view.setTextColor(color)

        if (view.ellipsize == TextUtils.TruncateAt.MARQUEE)
            view.isSelected = true
    }

    /**
     * A Binding Adapter that is called whenever the value of the attribute `android:progressTint`
     * changes. Depending on the value it determines the color of the progress bar.
     */
    @BindingAdapter("android:rankProgressTint")
    @JvmStatic
    fun rankProgressTint(view: ProgressBar, model: RankModel) {
        val context: Context = view.context
        var color = getAssociatedColor(context, model, view)

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
        if (model.getName().isEmpty() && (view.id == R.id.progressTextView || view.id == R.id.progressBar))
            view.visibility = View.GONE
        else {
            val isEndRank = isEndRank(model)
            val isReputationView = isReputationView(view)

            if (isEndRank && !isReputationView)
                view.visibility = View.INVISIBLE
            else if (!model.isFactionRank && isReputationView)
                view.visibility = View.GONE
        }
    }

    @ColorInt
    private fun darkenColor(@ColorInt color: Int): Int {
        return Color.HSVToColor(FloatArray(3).apply {
            Color.colorToHSV(color, this)
            this[2] *= 0.7f
        })
    }

    private fun getAssociatedColor(context: Context, model: RankModel, view: View): Int {
        var color = when (model.getTitleResId()) {
            R.string.rank_combat -> ContextCompat.getColor(context, R.color.orange)
            R.string.rank_trading -> ContextCompat.getColor(context, R.color.rank_trading)
            R.string.rank_explore -> ContextCompat.getColor(context, R.color.rank_exploration)
            R.string.rank_cqc -> ContextCompat.getColor(context, R.color.rank_cqc)
            R.string.rank_federation -> ContextCompat.getColor(context, R.color.rank_federation)
            R.string.rank_alliance -> ContextCompat.getColor(context, R.color.rank_alliance)
            else -> ContextCompat.getColor(context, R.color.rank_empire)
        }

        if (view.id == R.id.titleTextView || view.id == R.id.reputationText || view.id == R.id.repText)
            color = darkenColor(color)

        return color
    }

    private fun isReputationView(view: View): Boolean {
        return view.id == R.id.reputationText || view.id == R.id.repText || view.id == R.id.reputationBar
    }

    private fun isEndRank(model: RankModel): Boolean {
        return !model.isFactionRank && model.rank.value == 8 || model.isFactionRank && model.rank.value == 13
    }
}