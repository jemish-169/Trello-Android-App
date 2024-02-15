package com.practice.trello.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.practice.trello.databinding.ItemCardsBinding
import com.practice.trello.models.Card

class CardListItemsAdapter(private val context: Context, private var list: ArrayList<Card>) :
    RecyclerView.Adapter<CardListItemsAdapter.ViewHolder>() {

    private var onItemClickListener: OnItemClickListener? = null

    inner class ViewHolder(val binding: ItemCardsBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCardsBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            with(list[position]) {
                binding.cardItemTvCardName.text = this.name
                if (this.labelColor.isNotEmpty())
                    binding.cardItemViewLabelColor.setBackgroundColor(Color.parseColor(this.labelColor))
                else {
                    binding.cardItemViewLabelColor.visibility = View.GONE
                }
                holder.itemView.setOnClickListener {
                    if (onItemClickListener != null) {
                        onItemClickListener!!.onClick(position)
                    }
                }
            }
        }
    }

    fun setOnClickListener(onItemClickListener: OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
    }

    interface OnItemClickListener {
        fun onClick(cardPosition: Int)
    }
}