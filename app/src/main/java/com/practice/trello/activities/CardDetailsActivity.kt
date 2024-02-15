package com.practice.trello.activities

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import androidx.core.widget.doOnTextChanged
import com.practice.trello.R
import com.practice.trello.databinding.ActivityCardDetailsBinding
import com.practice.trello.databinding.CustomDialogBoxBinding
import com.practice.trello.dialogs.LabelColorListDialog
import com.practice.trello.firebase.FireStoreClass
import com.practice.trello.models.Board
import com.practice.trello.models.Card
import com.practice.trello.models.Task
import com.practice.trello.utils.Constants

class CardDetailsActivity : BaseActivity() {
    private lateinit var binding: ActivityCardDetailsBinding
    private lateinit var mBoardDetails: Board
    private var mTaskListPosition = -1
    private var mCardListPosition = -1
    private var mSelectedColor: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCardDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getIntentData()
        setupActionbar()

        binding.cardDetailEtCardName.doOnTextChanged { _, _, _, _ -> removeError() }
        binding.cardDetailEtCardName.setText(mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].name)
        mSelectedColor =
            mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].labelColor
        setColor()

        binding.cardDetailBtnUpdate.setOnClickListener {
            if (validateCardName()) {
                updateCardDetails()
            }
        }
        binding.cardDetailTvSelectColor.setOnClickListener {
            labelColorListDialog()
        }
    }

    private fun labelColorListDialog() {
        val colorList = colorList()
        val listDialog = object : LabelColorListDialog(
            this,
            colorList,
            mSelectedColor
        ) {
            override fun onItemSelected(color: String) {
                mSelectedColor = color
                setColor()
            }
        }
        listDialog.show()
    }

    private fun setColor() {
        if (mSelectedColor.isNotBlank()) {
            binding.cardDetailTvSelectColor.text = ""
            binding.cardDetailTvSelectColor.setBackgroundColor(Color.parseColor(mSelectedColor))
        } else {
            binding.cardDetailTvSelectColor.text = resources.getString(R.string.select_label_color)
            binding.cardDetailTvSelectColor.setBackgroundResource(android.R.color.transparent)

        }
    }

    private fun removeError() {
        binding.cardDetailTilCardName.setErrorMessage("")
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_delete_card, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_delete_card -> {
                alertDialogForDeleteCard(mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].name)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getIntentData() {
        if (intent.hasExtra(Constants.BOARS_DETAIL)) mBoardDetails =
            intent.getParcelableExtra(Constants.BOARS_DETAIL)!!

        if (intent.hasExtra(Constants.TASK_LIST_ITEM_POSITION)) mTaskListPosition =
            intent.getIntExtra(Constants.TASK_LIST_ITEM_POSITION, -1)

        if (intent.hasExtra(Constants.CARD_LIST_ITEM_POSITION)) mCardListPosition =
            intent.getIntExtra(Constants.CARD_LIST_ITEM_POSITION, -1)
    }

    private fun setupActionbar() {
        setSupportActionBar(binding.cardDetailsToolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.cardDetailsToolBar.title =
            mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].name
        binding.cardDetailsToolBar.setNavigationOnClickListener {
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    fun addUpdateTaskListSuccess() {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun updateCardDetails() {
        val card = Card(
            binding.cardDetailEtCardName.text.toString().trim(),
            mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].createdBy,
            mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].assignedTo,
            mSelectedColor
        )

        val taskList: ArrayList<Task> = mBoardDetails.taskList
        taskList.removeAt(taskList.size - 1)

        mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition] = card

        showProgressDialog(resources.getString(R.string.progress_please_wait))
        FireStoreClass().addUpdateTaskList(this, mBoardDetails)
    }

    private fun deleteCard() {
        val cardList: ArrayList<Card> = mBoardDetails.taskList[mTaskListPosition].cards
        cardList.removeAt(mCardListPosition)
        val taskList: ArrayList<Task> = mBoardDetails.taskList
        taskList.removeAt(taskList.size - 1)
        taskList[mTaskListPosition].cards = cardList

        showProgressDialog(resources.getString(R.string.progress_please_wait))
        FireStoreClass().addUpdateTaskList(this, mBoardDetails)

        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun validateCardName(): Boolean {
        val isCardBlank = binding.cardDetailEtCardName.text.toString().trim().isBlank()
        return if (isCardBlank) {
            binding.cardDetailTilCardName.setErrorMessage("Card name can not be empty.")
            false
        } else true
    }

    private fun alertDialogForDeleteCard(title: String) {
        val dialog = AlertDialog.Builder(this)
        val binding = CustomDialogBoxBinding.inflate(LayoutInflater.from(this))
        dialog.setView(binding.root)
        binding.customDialogTvMainText.text =
            resources.getString(R.string.are_you_sure_you_want_to_delete, title)

        val alertDialog: AlertDialog = dialog.create()
        alertDialog.show()
        binding.customDialogBtnYes.setOnClickListener {
            alertDialog.dismiss()
            deleteCard()

        }
        binding.customDialogBtnNo.setOnClickListener {
            alertDialog.dismiss()
        }
    }

    private fun colorList(): ArrayList<String> {
        val colorList: ArrayList<String> = ArrayList()
        colorList.add("#BEADFA")
        colorList.add("#D2E0FB")
        colorList.add("#A8DF8E")
        colorList.add("#FFBFBF")
        colorList.add("#D67BFF")
        colorList.add("#FEFFAC")
        colorList.add("#45FFCA")
        colorList.add("#989898")
        colorList.add("#e7bc91")
        colorList.add("")

        return colorList
    }
}