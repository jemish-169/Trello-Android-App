package com.practice.trello.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.practice.trello.databinding.ItemCardsBinding
import com.practice.trello.models.Card

class CardListItemsAdapter(private val context: Context, private var list: ArrayList<Card>) :
    RecyclerView.Adapter<CardListItemsAdapter.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

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
            }
        }
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick(position: Int)
    }
}