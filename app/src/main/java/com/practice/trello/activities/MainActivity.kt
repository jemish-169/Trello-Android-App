package com.practice.trello.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.core.view.GravityCompat
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
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

    companion object {
        const val MY_PROFILE_REQUEST_CODE: Int = 11
        const val CREATE_BOARD_REQUEST_CODE: Int = 12
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Trello)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        setupActionbar()

        binding.mainNavView.setNavigationItemSelectedListener(this)

        FireStoreClass().loadUserData(this, true)

        binding.mainIncludeAppBar.fabCreateBoard.setOnClickListener {
            val intent = Intent(this, CreateBoardActivity::class.java)
            intent.putExtra(Constants.NAME, user?.name ?: "")
            startActivityForResult(intent, CREATE_BOARD_REQUEST_CODE)
        }

    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.main_nav_my_profile -> {
                startActivityForResult(
                    Intent(this, MyProfileActivity::class.java),
                    MY_PROFILE_REQUEST_CODE
                )
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
            showProgressDialog(resources.getString(R.string.progress_please_wait))
            FireStoreClass().getBoardList(this)
        }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == MY_PROFILE_REQUEST_CODE) {
            FireStoreClass().loadUserData(this, true)
        }

        if (resultCode == Activity.RESULT_OK && requestCode == CREATE_BOARD_REQUEST_CODE) {
            FireStoreClass().loadUserData(this, true)
        }
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
            val intent = Intent(this, IntroActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()

        }
        binding.customDialogBtnNo.setOnClickListener {
            alertDialog.dismiss()
        }
    }

}