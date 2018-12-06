package com.crazyma.flexboxlayoutsample

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class FlexboxAdapter(
    private val context: Context
) : RecyclerView.Adapter<FlexboxAdapter.ViewHolder>() {

    var list = mutableListOf<String>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textView: TextView = itemView as TextView
    }

    fun insertItem(index: Int, text: String) {
        list.add(index, text)
        notifyItemInserted(index)
    }

    fun insertItem(index: Int, texts: List<String>){
        list.addAll(index ,texts)
        notifyItemRangeInserted(index, texts.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.view_flex_item, parent, false)!!
        return ViewHolder(view)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = list[position]
    }
}