package com.devries48.elitecommander.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.devries48.elitecommander.R
import com.devries48.elitecommander.events.FrontierDiscovery
import com.devries48.elitecommander.utils.NamingUtils

class DiscoveriesRecyclerAdapter(var data: List<FrontierDiscovery>?) :
    RecyclerView.Adapter<DiscoveriesRecyclerAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val bodyImageView: ImageView = view.findViewById(R.id.bodyImageView)
        val bodyNameTextView: TextView = view.findViewById(R.id.bodyNameTextView)
        val discoveredTextView: TextView = view.findViewById(R.id.discoveredTextView)
        val firstDiscoveredTextView: TextView = view.findViewById(R.id.firstDiscoveredTextView)
        val firstDiscoveredLabelTextView: TextView =
            view.findViewById(R.id.firstDiscoveredLabelTextView)
        val mappedTitleTextView: TextView = view.findViewById(R.id.mappedTitleTextView)
        val mappedTextView: TextView = view.findViewById(R.id.mappedTextView)
        val firstMappedTextView: TextView = view.findViewById(R.id.firstMappedTextView)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_discovery, viewGroup, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return if (data?.size == null)
            0
        else
            data?.size!!
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val ctx = viewHolder.bodyNameTextView.context
        val item = data?.get(position)

        if (item != null) {
            val bodyResources = NamingUtils.getDiscoveryBodyResources(item.body, item.star)

            if (bodyResources.first == 0)
                viewHolder.bodyNameTextView.text = if (item.star.isEmpty()) item.body else item.star
            else
                viewHolder.bodyNameTextView.text = ctx.getString(bodyResources.first)

            viewHolder.bodyImageView.setImageResource(bodyResources.second)
            viewHolder.discoveredTextView.text =
                String.format(ctx.getString(R.string.format_number), item.discoveryCount)

            if (item.firstDiscoveredCount + item.firstMappedCount == 0)
                viewHolder.firstDiscoveredLabelTextView.visibility = View.GONE
            else viewHolder.firstDiscoveredLabelTextView.visibility = View.VISIBLE

            if (item.firstDiscoveredCount == 0)
                viewHolder.firstDiscoveredTextView.text = ""
            else
                viewHolder.firstDiscoveredTextView.text =
                    String.format(ctx.getString(R.string.format_number), item.firstDiscoveredCount)

            val totalFirstMapped :Int= item.firstMappedCount + item.firstDiscoveredAndMappedCount

            if (item.mappedCount + totalFirstMapped == 0) {
                viewHolder.mappedTitleTextView.visibility = View.GONE
                viewHolder.mappedTextView.text = ""
            } else {
                viewHolder.mappedTitleTextView.visibility = View.VISIBLE
                if (item.mappedCount == 0)
                    viewHolder.mappedTextView.text = ""
                else
                    viewHolder.mappedTextView.text =
                        String.format(ctx.getString(R.string.format_number), item.mappedCount)

                if (totalFirstMapped == 0)
                    viewHolder.firstMappedTextView.text = ""
                else
                    viewHolder.firstMappedTextView.text =
                        String.format(ctx.getString(R.string.format_number), totalFirstMapped)
            }
        }
    }

    fun updateList(discoveries: List<FrontierDiscovery>) {
        data = discoveries
        notifyDataSetChanged()
    }

}

