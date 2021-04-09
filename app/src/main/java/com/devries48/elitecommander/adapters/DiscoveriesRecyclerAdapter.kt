package com.devries48.elitecommander.adapters

import android.content.Context
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
        val firstDiscoveredLabelTextView: TextView = view.findViewById(R.id.firstDiscoveredLabelTextView)
        val mappedTitleTextView: TextView = view.findViewById(R.id.mappedTitleTextView)
        val mappedTextView: TextView = view.findViewById(R.id.mappedTextView)
        val firstMappedTextView: TextView = view.findViewById(R.id.firstMappedTextView)
        val valueTextView: TextView = view.findViewById(R.id.valueTextView)
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

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val item = data?.get(position)
        val ctx = viewHolder.bodyNameTextView.context

        if (item != null) {

            displayBody(item, ctx, viewHolder)
            displayDiscovered(item, ctx, viewHolder)
            displayMapped(item, ctx, viewHolder)

            viewHolder.valueTextView.text =
                String.format(ctx.getString(R.string.format_currency), item.estimatedValue)
        }
    }

    private fun displayBody(item: FrontierDiscovery, ctx: Context, view: ViewHolder) {
        val bodyResources = NamingUtils.getDiscoveryBodyResources(item.body, item.star)

        try {
            if (bodyResources.first == 0)
                view.bodyNameTextView.text = if (item.star.isEmpty()) item.body else item.star
            else
                view.bodyNameTextView.text = ctx.getString(bodyResources.second)

            view.bodyImageView.setImageResource(bodyResources.first)

        } catch (e: Exception) {
            println("Error in DiscoveriesAdapter displayBody")
        }
    }

    private fun displayDiscovered(item: FrontierDiscovery, ctx: Context, view: ViewHolder) {
        val totalFirstDiscovered: Int = item.firstDiscoveredCount + item.firstDiscoveredAndMappedCount

        view.discoveredTextView.text = String.format(ctx.getString(R.string.format_number), item.discoveryCount)

        // No first discoveries and mappings, hide the 'first' label
        view.firstDiscoveredLabelTextView.visibility =
            if (item.firstDiscoveredCount + item.firstMappedCount + item.firstDiscoveredAndMappedCount == 0)
                View.GONE
            else
                View.VISIBLE

        // No first discoveries, display empty string
        view.firstDiscoveredTextView.text = if (totalFirstDiscovered == 0) "" else String.format(
            ctx.getString(R.string.format_number),
            totalFirstDiscovered
        )
    }


    private fun displayMapped(item: FrontierDiscovery, ctx: Context, view: ViewHolder) {
        val totalFirstMapped: Int = item.firstMappedCount + item.firstDiscoveredAndMappedCount

        if (item.mappedCount + totalFirstMapped == 0) {
            view.mappedTitleTextView.visibility = View.GONE
            view.mappedTextView.text = ""
            view.firstMappedTextView.text = ""
        } else {
            view.mappedTitleTextView.visibility = View.VISIBLE
            view.mappedTextView.text = emptyWhenZero(item.mappedCount, ctx)
            view.firstMappedTextView.text = emptyWhenZero(totalFirstMapped, ctx)
        }
    }

    private fun emptyWhenZero(value: Int, ctx: Context): String {
        return if (value != 0)
            String.format(ctx.getString(R.string.format_number), value)
        else
            ""
    }

    fun updateList(discoveries: List<FrontierDiscovery>) {
        data = discoveries
        notifyDataSetChanged()
    }

}