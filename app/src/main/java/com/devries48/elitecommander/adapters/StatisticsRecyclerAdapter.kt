package com.devries48.elitecommander.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import com.devries48.elitecommander.R
import com.devries48.elitecommander.models.StatisticModel
import com.devries48.elitecommander.models.StatisticsBuilder
import com.devries48.elitecommander.models.StatisticsBuilder.Companion.StatisticColor.DIMMED
import com.devries48.elitecommander.models.StatisticsBuilder.Companion.StatisticColor.WARNING

class StatisticsRecyclerAdapter(var data: List<StatisticModel>?) :
    RecyclerView.Adapter<StatisticsRecyclerAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val leftNameTextView: TextView = view.findViewById(R.id.leftNameTextView)
        val leftValueTextView: TextView = view.findViewById(R.id.leftValueTextView)
        val leftDeltaTextView: TextView = view.findViewById(R.id.leftDeltaTextView)
        val rightNameTextView: TextView = view.findViewById(R.id.rightNameTextView)
        val rightValueTextView: TextView = view.findViewById(R.id.rightValueTextView)
        val rightDeltaTextView: TextView = view.findViewById(R.id.rightDeltaTextView)
        val middleNameTextView: TextView = view.findViewById(R.id.middleNameTextView)
        val middleValueTextView: TextView = view.findViewById(R.id.middleValueTextView)
        val middleDeltaTextView: TextView = view.findViewById(R.id.middleDeltaTextView)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_statistic, viewGroup, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return data?.size ?: 0
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        if (data == null) return

        val item = data!![position]

        setValues(
            viewHolder.leftNameTextView,
            viewHolder.leftValueTextView,
            viewHolder.leftDeltaTextView,
            item.leftTitleResId,
            item.leftValue,
            item.leftColor,
            item.leftDelta
        )

        setValues(
            viewHolder.middleNameTextView,
            viewHolder.middleValueTextView,
            viewHolder.middleDeltaTextView,
            item.middleTitleResId,
            item.middleValue,
            item.middleColor,
            item.middleDelta
        )
        setValues(
            viewHolder.rightNameTextView,
            viewHolder.rightValueTextView,
            viewHolder.rightDeltaTextView,
            item.rightTitleResId,
            item.rightValue,
            item.rightColor,
            item.rightDelta
        )
    }

    private fun setValues(
        nameTextView: TextView,
        valueTextView: TextView,
        deltaTextView: TextView,
        @StringRes titleResId: Int,
        value: String?,
        color: StatisticsBuilder.Companion.StatisticColor,
        delta: String?
    ) {
        val ctx = nameTextView.context
        var tTitle = ""
        var tValue = ""
        var tDelta = ""

        if (titleResId != 0) {
            tTitle = titleResId.let { ctx.getString(it) }

            if (value?.isNotEmpty() == true) {
                tValue = value
                valueTextView.setTextAppearance(getItemStyle(color))
            }
            if (delta?.isNotEmpty() == true) {
                tDelta = delta
                deltaTextView.setTextAppearance(getDeltaStyle(delta))
            }
        }

        nameTextView.text = tTitle
        valueTextView.text = tValue
        deltaTextView.text = tDelta
    }

    private fun getDeltaStyle(leftDelta: String): Int {
        return if (leftDelta.startsWith("-")) R.style.eliteStyle_RedText else R.style.eliteStyle_AzureText
    }

    private fun getItemStyle(color: StatisticsBuilder.Companion.StatisticColor): Int {
        return when (color) {
            DIMMED -> R.style.eliteStyle_Value
            WARNING -> R.style.eliteStyle_RedText
            else -> R.style.eliteStyle_Bright
        }
    }

    fun updateList(stats: List<StatisticModel>) {
        data = stats
        notifyDataSetChanged()
    }
}