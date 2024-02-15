package com.practice.trello.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.practice.trello.R
import com.practice.trello.databinding.ItemBoardsBinding
import com.practice.trello.models.Board

class BoardsItemAdapter(private val context: Context, private val list: ArrayList<Board>) :
    RecyclerView.Adapter<BoardsItemAdapter.ViewHolder>() {

    private var onItemClickListener: OnItemClickListener? = null

    inner class ViewHolder(val binding: ItemBoardsBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BoardsItemAdapter.ViewHolder {
        val binding = ItemBoardsBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            with(list[position]) {
                Glide
                    .with(context)
                    .load(this.image)
                    .centerCrop()
                    .placeholder(R.drawable.employee_manages_project)
                    .into(binding.itemBoardIv)
                binding.itemBoardTvBoardName.text = this.name
                binding.itemBoardTvCreatedBy.text = "Created by ${this.createdBy}"
                holder.itemView.setOnClickListener {
                    onItemClickListener?.onClick(position, list[position])
                }
            }
        }
    }

    fun setOnClickListener(onItemClickListener: OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
    }

    interface OnItemClickListener {
        fun onClick(position: Int, model: Board)
    }
}
