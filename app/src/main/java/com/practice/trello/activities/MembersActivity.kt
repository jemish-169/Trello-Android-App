package com.practice.trello.activities

import android.os.Bundle
import android.view.View
import com.practice.trello.R
import com.practice.trello.adapter.MemberListItemAdapter
import com.practice.trello.databinding.ActivityMembersBinding
import com.practice.trello.firebase.FireStoreClass
import com.practice.trello.models.Board
import com.practice.trello.models.User
import com.practice.trello.utils.Constants

class MembersActivity : BaseActivity() {

    private lateinit var binding: ActivityMembersBinding
    private lateinit var mBoardDetails: Board

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMembersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupActionbar()

        if (intent.hasExtra(Constants.BOARS_DETAIL)) {
            mBoardDetails = intent.getParcelableExtra<Board>(Constants.BOARS_DETAIL)!!
        }

        showProgressDialog(resources.getString(R.string.progress_please_wait))
        FireStoreClass().getAssignedMembersListDetails(this, mBoardDetails.assignedTo)
    }

    fun setUpMemberList(list: ArrayList<User>) {
        hideProgressDialog()
        binding.memberRvMembersList.setHasFixedSize(true)
        binding.memberRvMembersList.adapter = MemberListItemAdapter(this, list)
        binding.memberRvMembersList.visibility = View.VISIBLE
        binding.membersTvNoMembers.visibility = View.GONE
    }

    private fun setupActionbar() {
        setSupportActionBar(binding.membersToolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.membersToolBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}