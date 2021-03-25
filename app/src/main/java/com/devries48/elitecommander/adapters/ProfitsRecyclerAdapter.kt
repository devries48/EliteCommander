package com.devries48.elitecommander.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.devries48.elitecommander.R
import com.devries48.elitecommander.models.ProfitModel

class ProfitsRecyclerAdapter(var data: List<ProfitModel>?) :
    RecyclerView.Adapter<ProfitsRecyclerAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val percentageTextView: TextView = view.findViewById(R.id.percentage)
        val titleTextView: TextView = view.findViewById(R.id.title)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_profit, viewGroup, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return if (data?.size == null)
            0
        else
            data?.size!!
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val item = data?.get(position)
        val ctx = viewHolder.titleTextView.context

        if (item != null) {
            val percentage = "${"%.1f".format(item.percentage)}%"
            viewHolder.percentageTextView.setCompoundDrawablesWithIntrinsicBounds(item.getColorDot(), 0, 0, 0)
            viewHolder.percentageTextView.text = percentage
            viewHolder.percentageTextView.setTextColor(item.getColor())

            viewHolder.titleTextView.text = ctx.getText(item.getTitle())
            viewHolder.titleTextView.setTextColor(item.getColor())
        }
    }

    fun updateList(profits: List<ProfitModel>?) {
        data = profits
        notifyDataSetChanged()
    }
}