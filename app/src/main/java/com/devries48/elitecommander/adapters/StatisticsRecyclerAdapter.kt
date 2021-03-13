package com.devries48.elitecommander.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.devries48.elitecommander.R
import com.devries48.elitecommander.models.StatisticModel
import com.devries48.elitecommander.models.StatisticsBuilder

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
        val middleNameTextView: TextView = view.findViewById(R.id.middleNameTextView)
        val middleValueTextView: TextView = view.findViewById(R.id.middleValueTextView)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_statistic, viewGroup, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        Log.d("Main Statistics items:  ", data?.size.toString())
        return data?.size ?: 0
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        if (data == null) return

        val item = data!![position]
        val ctx = viewHolder.leftNameTextView.context

        if (item.leftTitleResId != 0) {
            viewHolder.leftNameTextView.text = item.leftTitleResId.let { ctx.getString(it) }
            if (item.leftValue?.isNotEmpty() == true) {
                viewHolder.leftValueTextView.text = item.leftValue
                viewHolder.leftValueTextView.setTextAppearance(getItemStyle(item.leftColor))
            }
            if (item.leftDelta?.isNotEmpty() == true) {
                viewHolder.leftDeltaTextView.text = item.leftDelta
            }
        }

        if (item.rightTitleResId != 0)
            viewHolder.rightNameTextView.text = item.rightTitleResId.let { ctx.getString(it) }
        if (item.rightValue?.isNotEmpty() == true) {
            viewHolder.rightValueTextView.text = item.rightValue
            viewHolder.rightValueTextView.setTextAppearance(getItemStyle(item.rightColor))
        }

        if (item.middleTitleResId != 0)
            viewHolder.middleNameTextView.text = item.middleTitleResId.let { ctx.getString(it) }
        if (item.middleValue?.isNotEmpty() == true) {
            viewHolder.middleValueTextView.text = item.middleValue
            viewHolder.middleValueTextView.setTextAppearance(getItemStyle(item.middleColor))
        }
    }

    private fun getItemStyle(color: StatisticsBuilder.Companion.StatisticColor): Int {
        return when (color) {
            StatisticsBuilder.Companion.StatisticColor.DIMMED -> R.style.eliteStyle_LightOrangeText
            StatisticsBuilder.Companion.StatisticColor.WARNING -> R.style.eliteStyle_RedText
            else -> R.style.eliteStyle_YellowText
        }
    }

    fun updateList(stats: List<StatisticModel>) {
        data = stats
        notifyDataSetChanged()
    }
}