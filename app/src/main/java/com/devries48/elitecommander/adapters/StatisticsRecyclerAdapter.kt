package com.devries48.elitecommander.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.devries48.elitecommander.R
import com.devries48.elitecommander.models.EliteStatistic


class StatisticsRecyclerAdapter(var data: List<EliteStatistic>) :
    RecyclerView.Adapter<StatisticsRecyclerAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.nameTextView)
        val valueTextView: TextView = view.findViewById(R.id.valueTextView)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_statistic, viewGroup, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        Log.d("Adapter Size ", data.size.toString())
        return data.size
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val ctx = viewHolder.nameTextView.context
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        val item = data[position]
        viewHolder.nameTextView.text = item.stringRes.let { ctx.getString(it) }
        viewHolder.valueTextView.text = item.value
    }

    fun updateList(stats:List<EliteStatistic>){
        data= stats
        notifyDataSetChanged()
    }

}

