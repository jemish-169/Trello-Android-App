package com.practice.trello.activities

import android.app.Activity
import android.app.Dialog
import android.os.AsyncTask
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
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

class MembersActivity : BaseActivity() {

    private lateinit var binding: ActivityMembersBinding
    private lateinit var mBoardDetails: Board
    private var dialogBinding: DialogSearchMemberBinding? = null
    private lateinit var mAssignedMemberList: ArrayList<User>
    private var anyChangesMade = false
    private lateinit var adapter: MemberListItemAdapter


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
        adapter = MemberListItemAdapter(this, list, mBoardDetails.createdById, getCurrentUserId())
        binding.memberRvMembersList.adapter = adapter
        adapter.setOnClickListener(object : MemberListItemAdapter.OnItemClickListener {
            override fun onClick(position: Int, user: User) {
                showProgressDialog(resources.getString(R.string.progress_please_wait))
                FireStoreClass().removeMemberFromBoard(
                    this@MembersActivity,
                    mBoardDetails,
                    position,
                    user
                )
            }
        })
    }

    fun memberDetails(user: User) {
        mBoardDetails.assignedTo.add(user.id)
        FireStoreClass().assignedMemberToBoard(this, mBoardDetails, user)
    }

    private fun setupActionbar() {
        setSupportActionBar(binding.membersToolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.membersToolBar.setNavigationOnClickListener {
            if (anyChangesMade) {
                setResult(Activity.RESULT_OK)
            }
            onBackPressed()
        }
    }

    private fun alertDialogToAddMember() {
        if (getCurrentUserId() == mBoardDetails.createdById) {
            val dialog = Dialog(this)
            dialogBinding = DialogSearchMemberBinding.inflate(layoutInflater)
            dialog.setContentView(dialogBinding!!.root)
            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)
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
        adapter.notifyDataSetChanged()
        SendNotificationToUserRemoveAsyncTask(mBoardDetails.name, user.fcmToken).execute()
    }

    fun memberRemovedSuccess(position: Int, user: User) {
        hideProgressDialog()
        mBoardDetails.assignedTo.remove(user.id)
        mAssignedMemberList.remove(user)
        anyChangesMade = true
        adapter.notifyItemRemoved(position)
        SendNotificationToUserAsyncTask(mBoardDetails.name, user.fcmToken).execute()
    }

    private inner class SendNotificationToUserAsyncTask(val boardName: String, val token: String) :
        AsyncTask<Any, Void, String>() {

        override fun doInBackground(vararg params: Any?): String {
            var result: String

            var connection: HttpURLConnection? = null
            try {
                val url = URL(Constants.FCM_BASE_URL)
                connection = url.openConnection() as HttpURLConnection
                connection.doOutput = true
                connection.doInput = true
                connection.instanceFollowRedirects = false
                connection.requestMethod = "POST"

                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("charset", "utf-8")
                connection.setRequestProperty("Accept", "application/json")

                connection.setRequestProperty(
                    Constants.FCM_AUTHORIZATION,
                    "${Constants.FCM_KEY} = ${Constants.FCM_SERVER_KEY}"
                )
                connection.useCaches = false
                val wr = DataOutputStream(connection.outputStream)
                val jsonRequest = JSONObject()
                val dataObject = JSONObject()
                dataObject.put(Constants.FCM_KEY_TITLE, "Assigned to The board $boardName")
                dataObject.put(
                    Constants.FCM_KEY_MESSAGE,
                    "You have been assigned to the new board by ${mAssignedMemberList[0].name}"
                )
                jsonRequest.put(Constants.FCM_KEY_DATA, dataObject)
                jsonRequest.put(Constants.FCM_KEY_TO, token)

                wr.writeBytes(jsonRequest.toString())
                wr.flush()
                wr.close()

                val httpResult: Int = connection.responseCode
                if (httpResult == HttpURLConnection.HTTP_OK) {
                    val inputStream = connection.inputStream

                    val reader = BufferedReader(InputStreamReader(inputStream))

                    val sb = StringBuilder()
                    var line: String?
                    try {
                        while (reader.readLine().also { line = it } != null) {
                            sb.append(line + "\n")
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } finally {
                        try {
                            inputStream.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                    result = sb.toString()
                } else {
                    result = connection.responseMessage
                }
            } catch (e: SocketTimeoutException) {
                result = "Connection TimeOut"
            } catch (e: Exception) {
                result = "Error : " + e.message
            } finally {
                connection?.disconnect()
            }
            return result
        }
    }

    private inner class SendNotificationToUserRemoveAsyncTask(
        val boardName: String,
        val token: String
    ) :
        AsyncTask<Any, Void, String>() {
        override fun doInBackground(vararg params: Any?): String {
            var result: String

            var connection: HttpURLConnection? = null
            try {
                val url = URL(Constants.FCM_BASE_URL)
                connection = url.openConnection() as HttpURLConnection
                connection.doOutput = true
                connection.doInput = true
                connection.instanceFollowRedirects = false
                connection.requestMethod = "POST"

                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("charset", "utf-8")
                connection.setRequestProperty("Accept", "application/json")

                connection.setRequestProperty(
                    Constants.FCM_AUTHORIZATION,
                    "${Constants.FCM_KEY} = ${Constants.FCM_SERVER_KEY}"
                )
                connection.useCaches = false
                val wr = DataOutputStream(connection.outputStream)
                val jsonRequest = JSONObject()
                val dataObject = JSONObject()
                dataObject.put(Constants.FCM_KEY_TITLE, "Removed from The board $boardName")
                dataObject.put(
                    Constants.FCM_KEY_MESSAGE,
                    "You have been removed from the $boardName board}"
                )
                jsonRequest.put(Constants.FCM_KEY_DATA, dataObject)
                jsonRequest.put(Constants.FCM_KEY_TO, token)

                wr.writeBytes(jsonRequest.toString())
                wr.flush()
                wr.close()

                val httpResult: Int = connection.responseCode
                if (httpResult == HttpURLConnection.HTTP_OK) {
                    val inputStream = connection.inputStream

                    val reader = BufferedReader(InputStreamReader(inputStream))

                    val sb = StringBuilder()
                    var line: String?
                    try {
                        while (reader.readLine().also { line = it } != null) {
                            sb.append(line + "\n")
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } finally {
                        try {
                            inputStream.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                    result = sb.toString()
                } else {
                    result = connection.responseMessage
                }
            } catch (e: SocketTimeoutException) {
                result = "Connection TimeOut"
            } catch (e: Exception) {
                result = "Error : " + e.message
            } finally {
                connection?.disconnect()
            }
            return result
        }
    }
}