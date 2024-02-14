package com.practice.trello.activities

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.util.Patterns
import android.view.Menu
import android.view.MenuItem
import androidx.core.widget.doOnTextChanged
import com.practice.trello.R
import com.practice.trello.adapter.MemberListItemAdapter
import com.practice.trello.databinding.ActivityMembersBinding
import com.practice.trello.databinding.DialogSearchMemberBinding
import com.practice.trello.firebase.FireStoreClass
import com.practice.trello.models.Board
import com.practice.trello.models.User
import com.practice.trello.utils.Constants

class MembersActivity : BaseActivity() {

    private lateinit var binding: ActivityMembersBinding
    private lateinit var mBoardDetails: Board
    private var dialogBinding: DialogSearchMemberBinding? = null
    private lateinit var mAssignedMemberList: ArrayList<User>
    private var anyChangesMade = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMembersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupActionbar()

        if (intent.hasExtra(Constants.BOARS_DETAIL)) {
            mBoardDetails = intent.getParcelableExtra(Constants.BOARS_DETAIL)!!
        }

        showProgressDialog(resources.getString(R.string.progress_please_wait))
        FireStoreClass().getAssignedMembersListDetails(this, mBoardDetails.assignedTo)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add_members, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.action_add_members -> {
                alertDialogToAddMember()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (anyChangesMade) {
            setResult(Activity.RESULT_OK)
        }
        super.onBackPressed()
    }

    fun setUpMemberList(list: ArrayList<User>) {
        mAssignedMemberList = list
        hideProgressDialog()
        binding.memberRvMembersList.setHasFixedSize(true)
        binding.memberRvMembersList.adapter = MemberListItemAdapter(this, list)
    }

    fun memberDetails(user: User) {
        mBoardDetails.assignedTo.add(user.id)
        FireStoreClass().assignedMemberToBoard(this, mBoardDetails, user)
    }

    private fun setupActionbar() {
        setSupportActionBar(binding.membersToolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.membersToolBar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun alertDialogToAddMember() {
        val dialog = Dialog(this)
        dialogBinding = DialogSearchMemberBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding!!.root)

        dialog.show()

        dialogBinding?.memberDialogEtMemberName?.doOnTextChanged { _, _, _, _ -> removeError() }

        dialogBinding?.dialogSearchBtnAdd?.setOnClickListener {
            val email = dialogBinding?.memberDialogEtMemberName?.text.toString().trim()
            if (validateEmail(email)) {
                dialog.dismiss()
                showProgressDialog(resources.getString(R.string.progress_please_wait))
                FireStoreClass().getMemberDetails(this, email)
            }
        }
        dialogBinding?.dialogSearchBtnCancel?.setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun removeError() {
        dialogBinding?.memberDialogTilMemberName?.setErrorMessage("")
    }

    private fun validateEmail(email: String): Boolean {
        return if (email.isBlank()) {
            dialogBinding?.memberDialogTilMemberName?.setErrorMessage("Please enter an email.")
            false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            dialogBinding?.memberDialogTilMemberName?.setErrorMessage("Enter valid email.")
            false
        } else if (isMemberOnboard(email)) {
            dialogBinding?.memberDialogTilMemberName?.setErrorMessage("User is already on Board.")
            false
        } else true
    }

    private fun isMemberOnboard(email: String): Boolean {
        for (i in mAssignedMemberList) {
            if (i.email == email)
                return true
        }
        return false
    }

    fun memberAssignedSuccess(user: User) {
        hideProgressDialog()
        mAssignedMemberList.add(user)
        anyChangesMade = true
        setUpMemberList(mAssignedMemberList)
    }
}