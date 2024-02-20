package com.practice.trello.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.GravityCompat
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.practice.trello.R
import com.practice.trello.adapter.BoardsItemAdapter
import com.practice.trello.databinding.ActivityMainBinding
import com.practice.trello.databinding.CustomDialogBoxBinding
import com.practice.trello.firebase.FireStoreClass
import com.practice.trello.models.Board
import com.practice.trello.models.User
import com.practice.trello.utils.Constants
import de.hdodenhof.circleimageview.CircleImageView

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    lateinit var binding: ActivityMainBinding
    private var user: User? = null
    private lateinit var mSharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Trello)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupActionbar()

        mSharedPreferences =
            this.getSharedPreferences(Constants.TRELLO_PREFERENCES, Context.MODE_PRIVATE)
        val tokenUpdated = mSharedPreferences.getBoolean(Constants.FCM_TOKEN_UPDATED, false)
        if (tokenUpdated) {
            showProgressDialog(resources.getString(R.string.progress_please_wait))
            FireStoreClass().loadUserData(this, true)
        } else {
            FirebaseMessaging.getInstance().token
                .addOnCompleteListener(OnCompleteListener { task ->
                    if (task.isSuccessful) {
                        updateFCMToken(task.result)
                    }
                })
        }


        binding.mainNavView.setNavigationItemSelectedListener(this)

        binding.mainIncludeAppBar.fabCreateBoard.setOnClickListener {
            val intent = Intent(this, CreateBoardActivity::class.java)
            intent.putExtra(Constants.NAME, user?.name ?: "")
            resultLauncher.launch(intent)
        }

    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                mSharedPreferences =
                    this.getSharedPreferences(Constants.TRELLO_PREFERENCES, Context.MODE_PRIVATE)
                val tokenUpdated = mSharedPreferences.getBoolean(Constants.FCM_TOKEN_UPDATED, false)
                if (tokenUpdated) {
                    showProgressDialog(resources.getString(R.string.progress_please_wait))
                    FireStoreClass().loadUserData(this, true)
                } else {
                    FirebaseMessaging.getInstance().token
                        .addOnCompleteListener(OnCompleteListener { task ->
                            if (task.isSuccessful) {
                                updateFCMToken(task.result)
                            }
                        })
                }
            }
        }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.main_nav_my_profile -> {
                val intent = Intent(this, MyProfileActivity::class.java)
                intent.putExtra(Constants.MY_PROFILE_DATA, user)
                resultLauncher.launch(intent)
            }

            R.id.main_nav_sign_out -> {
                alertDialogForSignOut()
            }
        }
        binding.mainDrawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    fun updateNavigationUserDetails(loggedInUser: User, readBoardList: Boolean) {
        user = loggedInUser
        val headerView = binding.mainNavView.getHeaderView(0)
        val profileImage: CircleImageView =
            headerView.findViewById(R.id.drawer_circle_iv)

        Glide
            .with(this)
            .load(user!!.image)
            .centerCrop()
            .placeholder(R.drawable.user_placeholder_img)
            .into(profileImage)

        val userName: TextView = headerView.findViewById(R.id.drawer_tv_user_name)!!
        userName.text = user!!.name

        if (readBoardList) {
            FireStoreClass().getBoardList(this)
        } else hideProgressDialog()

    }

    fun populateBoardsToUI(boardList: ArrayList<Board>) {
        hideProgressDialog()
        if (boardList.isEmpty()) {
            binding.mainIncludeAppBar.mainUiLayout.mainContentRvBoards.visibility = View.GONE
            binding.mainIncludeAppBar.mainUiLayout.mainContentTvNoBoards.visibility = View.VISIBLE
        } else {
            val mainContentRvBoards = binding.mainIncludeAppBar.mainUiLayout.mainContentRvBoards

            mainContentRvBoards.visibility = View.VISIBLE
            binding.mainIncludeAppBar.mainUiLayout.mainContentTvNoBoards.visibility = View.GONE
            mainContentRvBoards.setHasFixedSize(true)

            val adapter = BoardsItemAdapter(this, boardList)
            mainContentRvBoards.adapter = adapter

            adapter.setOnClickListener(object : BoardsItemAdapter.OnItemClickListener {
                override fun onClick(position: Int, model: Board) {
                    val intent = Intent(this@MainActivity, TaskListActivity::class.java)
                    intent.putExtra(Constants.DOCUMENT_ID, model.documentId)
                    startActivity(intent)
                }
            })
        }
    }

    private fun setupActionbar() {
        setSupportActionBar(binding.mainIncludeAppBar.mainAppBarToolBar)
        binding.mainIncludeAppBar.mainAppBarToolBar.setNavigationIcon(R.drawable.three_line_menu_24)
        binding.mainIncludeAppBar.mainAppBarToolBar.setNavigationOnClickListener {
            toggleDrawer()
        }
    }

    private fun toggleDrawer() {
        if (binding.mainDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.mainDrawerLayout.closeDrawer(GravityCompat.START)
        } else
            binding.mainDrawerLayout.openDrawer(GravityCompat.START)
    }

    override fun onBackPressed() {
        if (binding.mainDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.mainDrawerLayout.closeDrawer(GravityCompat.START)
        } else
            doubleBackToExit()
    }

    private fun alertDialogForSignOut() {
        val dialog = AlertDialog.Builder(this)
        val binding = CustomDialogBoxBinding.inflate(LayoutInflater.from(this))
        dialog.setView(binding.root)
        binding.customDialogTvMainText.text =
            resources.getString(R.string.are_you_sure_you_want_to_sign_out)

        val alertDialog: AlertDialog = dialog.create()
        alertDialog.show()
        binding.customDialogBtnYes.setOnClickListener {
            alertDialog.dismiss()
            FirebaseAuth.getInstance().signOut()
            mSharedPreferences.edit().clear().apply()
            val intent = Intent(this, IntroActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()

        }
        binding.customDialogBtnNo.setOnClickListener {
            alertDialog.dismiss()
        }
    }

    fun tokenUpdateSuccess() {
        hideProgressDialog()
        val editor: SharedPreferences.Editor = mSharedPreferences.edit()
        editor.putBoolean(Constants.FCM_TOKEN_UPDATED, true)
        editor.apply()
        showProgressDialog(resources.getString(R.string.progress_please_wait))
        FireStoreClass().loadUserData(this, true)
    }

    private fun updateFCMToken(token: String) {
        val userHashMap = HashMap<String, Any>()
        userHashMap[Constants.FCM_TOKEN] = token

        showProgressDialog(resources.getString(R.string.progress_please_wait))
        FireStoreClass().updateUserData(this, userHashMap)
    }

}