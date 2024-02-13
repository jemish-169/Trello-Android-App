package com.practice.trello.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.practice.trello.R
import com.practice.trello.adapter.TaskListItemAdapter
import com.practice.trello.databinding.ActivityTaskListBinding
import com.practice.trello.firebase.FireStoreClass
import com.practice.trello.models.Board
import com.practice.trello.models.Card
import com.practice.trello.models.Task
import com.practice.trello.utils.Constants

class TaskListActivity : BaseActivity() {
    lateinit var binding: ActivityTaskListBinding
    private lateinit var mBoardDetails: Board
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_members, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_members -> {
                val intent = Intent(this, MembersActivity::class.java)
                intent.putExtra(Constants.BOARS_DETAIL, mBoardDetails)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun boardDetails(board: Board) {
        mBoardDetails = board
        hideProgressDialog()
        setupActionbar()

        val addTaskList = Task(resources.getString(R.string.item_task_add_List))
        board.taskList.add(addTaskList)
        binding.rvTaskList.setHasFixedSize(true)
        val adapter = TaskListItemAdapter(this, board.taskList)
        binding.rvTaskList.adapter = adapter
    }

    private fun setupActionbar() {
        setSupportActionBar(binding.taskListToolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.taskListToolBar.title = mBoardDetails.name
        binding.taskListToolBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    fun addUpdateTaskListSuccess() {
//        hideProgressDialog()
//        showProgressDialog(resources.getString(R.string.progress_please_wait))
        FireStoreClass().getBoardDetails(this, mBoardDetails.documentId)
    }

    fun createTaskList(taskListName: String) {
        val task = Task(taskListName, FireStoreClass().getCurrentUserId())
        mBoardDetails.taskList.add(0, task)
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)
        showProgressDialog(resources.getString(R.string.progress_please_wait))
        FireStoreClass().addUpdateTaskList(this, mBoardDetails)
    }

    fun updateTaskList(position: Int, listName: String, model: Task) {
        val task = Task(listName, model.createdBy)
        mBoardDetails.taskList[position] = task
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        showProgressDialog(resources.getString(R.string.progress_please_wait))
        FireStoreClass().addUpdateTaskList(this, mBoardDetails)
    }

    fun deleteTaskList(position: Int) {
        mBoardDetails.taskList.removeAt(position)
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        showProgressDialog(resources.getString(R.string.progress_please_wait))
        FireStoreClass().addUpdateTaskList(this, mBoardDetails)
    }

    fun addCardToTaskList(position: Int, cardName: String) {
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)
        val currentUser = FireStoreClass().getCurrentUserId()

        val cardAssignedUsersList: ArrayList<String> = ArrayList()
        cardAssignedUsersList.add(currentUser)

        val card = Card(cardName, currentUser, cardAssignedUsersList)

        val cardList = mBoardDetails.taskList[position].cards

        cardList.add(card)

        val task = Task(
            mBoardDetails.taskList[position].title,
            mBoardDetails.taskList[position].createdBy,
            cardList
        )
        mBoardDetails.taskList[position] = task

        showProgressDialog(resources.getString(R.string.progress_please_wait))
        FireStoreClass().addUpdateTaskList(this, mBoardDetails)
    }

    fun cardDetails(position: Int) {

    }
}