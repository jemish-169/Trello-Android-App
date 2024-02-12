package com.practice.trello.activities

import android.os.Bundle
import com.practice.trello.R
import com.practice.trello.databinding.ActivityTaskListBinding
import com.practice.trello.firebase.FireStoreClass
import com.practice.trello.models.Board
import com.practice.trello.utils.Constants

class TaskListActivity : BaseActivity() {
    lateinit var binding: ActivityTaskListBinding
    private var mDocumentId: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (intent.hasExtra(Constants.DOCUMENT_ID)) {
            mDocumentId = intent.getStringExtra(Constants.DOCUMENT_ID)
        }
        if (!mDocumentId.isNullOrEmpty()) {
            showProgressDialog(resources.getString(R.string.progress_please_wait))
            FireStoreClass().getBoardDetails(this, mDocumentId!!)
        }
    }

    fun boardDetails(board: Board) {
        hideProgressDialog()
        setupActionbar(board.name)
    }

    private fun setupActionbar(boardName: String) {
        setSupportActionBar(binding.taskListToolBar)
        binding.taskListToolBar.title = boardName
        binding.taskListToolBar.setNavigationIcon(R.drawable.three_line_menu_24)
        binding.taskListToolBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}