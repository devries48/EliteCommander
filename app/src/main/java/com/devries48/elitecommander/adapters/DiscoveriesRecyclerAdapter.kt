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
        val discoveriesTextView: TextView = view.findViewById(R.id.discoveriesTextView)
        val mappedTextView: TextView = view.findViewById(R.id.mappedTextView)
        val bonusTextView: TextView = view.findViewById(R.id.bonusTextView)
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

            viewHolder.discoveriesTextView.text =
                String.format(ctx.getString(R.string.format_number), item.discoveryCount)

            viewHolder.bodyImageView.setImageResource(bodyResources.second)
        }
    }

    fun updateList(discoveries: List<FrontierDiscovery>) {
        data = discoveries
        notifyDataSetChanged()
    }

}

