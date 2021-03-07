package com.devries48.elitecommander.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.devries48.elitecommander.R
import com.devries48.elitecommander.models.StatisticModel1

class StatisticsRecyclerAdapter1(var data: List<StatisticModel1>) :
    RecyclerView.Adapter<StatisticsRecyclerAdapter1.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.nameTextView)
        val valueTextView: TextView = view.findViewById(R.id.valueTextView)
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
        Log.d("Main Statistics items:  ", data.size.toString())
        return data.size
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val ctx = viewHolder.nameTextView.context
        val item = data[position]

        viewHolder.nameTextView.text = item.stringRes.let { ctx.getString(it) }
        viewHolder.valueTextView.text = item.value

        if (item.rightStringRes != 0) {
            viewHolder.rightNameTextView.text = item.rightStringRes.let { ctx.getString(it) }
        }
        if (item.rightValue?.isNotEmpty() == true) {
            viewHolder.rightValueTextView.text = item.rightValue
            viewHolder.rightValueTextView.setTextAppearance(item.rightValueStyleRes)
        }

        if (item.middleStringRes != 0) {
            viewHolder.middleNameTextView.text = item.middleStringRes.let { ctx.getString(it) }
        }
        if (item.middleValue?.isNotEmpty() == true) {
            viewHolder.middleValueTextView.text = item.middleValue
            viewHolder.middleValueTextView.setTextAppearance(item.middleValueStyleRes)
        }
    }

    fun updateList(stats: List<StatisticModel1>) {
        data = stats
        notifyDataSetChanged()
    }
}