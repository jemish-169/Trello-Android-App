package com.practice.trello.firebase

import android.app.Activity
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.practice.trello.R
import com.practice.trello.activities.CardDetailsActivity
import com.practice.trello.activities.CreateBoardActivity
import com.practice.trello.activities.MainActivity
import com.practice.trello.activities.MembersActivity
import com.practice.trello.activities.MyProfileActivity
import com.practice.trello.activities.SignInActivity
import com.practice.trello.activities.SignUpActivity
import com.practice.trello.activities.TaskListActivity
import com.practice.trello.models.Board
import com.practice.trello.models.User
import com.practice.trello.utils.Constants

class FireStoreClass {
    private val mFireStore = FirebaseFirestore.getInstance()

    fun registerUser(activity: SignUpActivity, userInfo: User) {
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserId())
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.signUpSuccess()
            }
            .addOnFailureListener {
                Toast.makeText(activity, "Registration Failure!!!.", Toast.LENGTH_SHORT).show()
            }
    }

    fun getBoardList(activity: MainActivity) {
        mFireStore.collection(Constants.BOARDS)
            .whereArrayContains(Constants.ASSIGNED_TO, getCurrentUserId())
            .get()
            .addOnSuccessListener { document ->
                val boardList: ArrayList<Board> = ArrayList()
                for (i in document.documents) {
                    val board = i.toObject(Board::class.java)!!
                    board.documentId = i.id
                    boardList.add(board)
                }
                activity.populateBoardsToUI(boardList)
            }
            .addOnFailureListener {
                activity.hideProgressDialog()
                activity.binding.mainIncludeAppBar.mainUiLayout.mainContentRvBoards.visibility =
                    View.GONE
                activity.binding.mainIncludeAppBar.mainUiLayout.mainContentTvNoBoards.visibility =
                    View.VISIBLE
                activity.binding.mainIncludeAppBar.mainUiLayout.mainContentTvNoBoards.text =
                    activity.getString(R.string.failed_to_load_boards)
            }
    }

    fun createBoard(activity: CreateBoardActivity, board: Board) {
        mFireStore.collection(Constants.BOARDS)
            .document()
            .set(board, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(activity, "Board created successfully.", Toast.LENGTH_SHORT).show()
                activity.boardCreatedSuccessFully()
            }
            .addOnFailureListener {
                activity.hideProgressDialog()
                Toast.makeText(activity, "Some error occured.", Toast.LENGTH_SHORT).show()

            }

    }

    fun getCurrentUserId(): String {
        val currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserID = ""
        if (currentUser != null) {
            currentUserID = currentUser.uid
        }
        return currentUserID
    }

    fun updateUserData(activity: MyProfileActivity, userHashMap: HashMap<String, Any>) {
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserId())
            .update(userHashMap)
            .addOnSuccessListener {
                activity.profileUpdateSuccess()
            }
            .addOnFailureListener {
                activity.hideProgressDialog()
                Toast.makeText(activity, R.string.error_in_profile_update, Toast.LENGTH_SHORT)
                    .show()
            }
    }

    fun loadUserData(activity: Activity, readBoardList: Boolean = false) {
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserId())
            .get()
            .addOnSuccessListener { documentSnapShot ->
                val loggedInUser = documentSnapShot.toObject(User::class.java)
                if (loggedInUser != null)
                    when (activity) {
                        is SignInActivity -> {
                            activity.signInSuccess()
                        }

                        is MainActivity -> {
                            activity.updateNavigationUserDetails(loggedInUser, readBoardList)
                        }

                        is MyProfileActivity -> {
                            activity.setUserDataInUI(loggedInUser)
                        }
                    }
            }
    }

    fun addUpdateTaskList(activity: Activity, board: Board) {
        val taskListHashMap = HashMap<String, Any>()
        taskListHashMap[Constants.TASK_LIST] = board.taskList

        mFireStore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(taskListHashMap)
            .addOnSuccessListener {
                if (activity is TaskListActivity)
                    activity.addUpdateTaskListSuccess()
                else if (activity is CardDetailsActivity)
                    activity.addUpdateTaskListSuccess()
            }
            .addOnFailureListener {
                if (activity is TaskListActivity)
                    activity.hideProgressDialog()
                else if (activity is CardDetailsActivity)
                    activity.hideProgressDialog()
                Toast.makeText(activity, "Task update Failure!!!.", Toast.LENGTH_SHORT).show()
            }

    }

    fun getBoardDetails(activity: TaskListActivity, documentId: String) {
        mFireStore.collection(Constants.BOARDS)
            .document(documentId)
            .get()
            .addOnSuccessListener { document ->
                val board = document.toObject(Board::class.java)!!
                board.documentId = document.id
                activity.boardDetails(board)
            }
            .addOnFailureListener {
                activity.hideProgressDialog()
                Toast.makeText(activity, "getting boards Failure!!!.", Toast.LENGTH_SHORT).show()
            }
    }

    fun getAssignedMembersListDetails(activity: MembersActivity, assignedTo: ArrayList<String>) {
        mFireStore.collection(Constants.USERS)
            .whereIn(Constants.ID, assignedTo)
            .get()
            .addOnSuccessListener { document ->
                val userList: ArrayList<User> = ArrayList()
                for (i in document.documents) {
                    val user = i.toObject((User::class.java))!!
                    userList.add(user)
                }
                activity.setUpMemberList(userList)
            }
            .addOnFailureListener {
                activity.hideProgressDialog()
                Toast.makeText(activity, "Failed to load members", Toast.LENGTH_SHORT).show()
            }
    }

    fun getMemberDetails(activity: MembersActivity, email: String) {
        mFireStore.collection(Constants.USERS)
            .whereEqualTo(Constants.EMAIL, email)
            .get()
            .addOnSuccessListener { document ->
                if (document.documents.size > 0) {
                    val user = document.documents[0].toObject(User::class.java)!!
                    activity.memberDetails(user)
                } else {
                    activity.hideProgressDialog()
                    activity.showErrorSnackBar("No such member found.")
                }
            }
            .addOnFailureListener {
                activity.hideProgressDialog()
                Toast.makeText(activity, "Failed to load user details.", Toast.LENGTH_SHORT).show()
            }
    }

    fun assignedMemberToBoard(activity: MembersActivity, board: Board, user: User) {
        val assignedToHashMap = HashMap<String, Any>()
        assignedToHashMap[Constants.ASSIGNED_TO] = board.assignedTo
        mFireStore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(assignedToHashMap)
            .addOnSuccessListener {
                activity.memberAssignedSuccess(user)
            }
            .addOnFailureListener {
                activity.hideProgressDialog()
                Toast.makeText(activity, "Failed to updating board details.", Toast.LENGTH_SHORT)
                    .show()

            }
    }
}