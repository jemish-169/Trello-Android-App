package com.practice.trello.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.practice.trello.adapter.LabelColorListAdapter
import com.practice.trello.databinding.DialogColorListBinding

abstract class LabelColorListDialog(
    context: Context,
    private var list: ArrayList<String>,
    private val mSelectedColor: String = ""
) : Dialog(context) {

    private lateinit var binding: DialogColorListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DialogColorListBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setCancelable(true)
        setCanceledOnTouchOutside(true)
        setUpRecyclerview()
    }

    private fun setUpRecyclerview() {
        val adapter = LabelColorListAdapter(context, list, mSelectedColor)
        binding.dialogColorRvList.adapter = adapter
        adapter.onItemClickListener = object : LabelColorListAdapter.OnItemClickListener {
            override fun onClick(position: Int, color: String) {
                dismiss()
                onItemSelected(color)
            }
        }
    }

    protected abstract fun onItemSelected(color: String)
}