package com.ruuvi.commissioning

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TagAdapter(private val myDataset: Array<RuuviTagNfcResult>, val clickListener: (String) -> (Unit)) :
    RecyclerView.Adapter<TagAdapter.MyViewHolder>() {

    class MyViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): TagAdapter.MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.tag_list_item, parent, false) as View
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.view.findViewById<TextView>(R.id.mac_tw).text = myDataset[position].mac
        holder.view.findViewById<TextView>(R.id.notes_tw).text = myDataset[position].notes
        holder.view.setOnClickListener { clickListener(myDataset[position].mac!!) }
        /*
        holder.view.setOnClickListener {
            val intent = Intent(holder.view.context, TagActivity::class.java)
            intent.data = Uri.parse(myDataset[position].mac)
            holder.view.context.startActivity(intent)
        }
        */
    }

    override fun getItemCount() = myDataset.size
}
