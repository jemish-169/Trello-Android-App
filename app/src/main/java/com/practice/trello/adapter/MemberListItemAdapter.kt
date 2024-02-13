package com.practice.trello.adapter


import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.practice.trello.R
import com.practice.trello.databinding.ItemMemberBinding
import com.practice.trello.models.User

class MemberListItemAdapter(private val context: Context, private var list: ArrayList<User>) :
    RecyclerView.Adapter<MemberListItemAdapter.ViewHolder>() {


    inner class ViewHolder(val binding: ItemMemberBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMemberBinding.inflate(LayoutInflater.from(context), parent, false)
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
                    .placeholder(R.drawable.user_placeholder_img)
                    .into(binding.itemBoardIv)
                binding.itemBoardTvBoardName.text = this.name
                binding.itemBoardTvCreatedBy.text = this.email
            }
        }
    }
}