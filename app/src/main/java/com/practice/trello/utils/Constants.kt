package com.practice.trello.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import com.practice.trello.activities.MyProfileActivity

object Constants {

    const val USERS: String = "Users"
    const val BOARDS: String = "Boards"

    const val ID: String = "id"
    const val NAME: String = "name"
    const val EMAIL: String = "email"
    const val IMAGE: String = "image"
    const val MOBILE: String = "mobile"
    const val FCM_TOKEN: String = "fcmToken"
    const val ASSIGNED_TO: String = "assignedTo"
    const val DOCUMENT_ID: String = "documentId"
    const val TASK_LIST: String = "taskList"
    const val BOARS_DETAIL: String = "board_detail"

    fun showImageChooser(activity: Activity) {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activity.startActivityForResult(galleryIntent, MyProfileActivity.PICK_IMAGE_REQUEST_CODE)
    }

    fun getFileExtension(activity: Activity, uri: Uri?): String? {
        return MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(activity.contentResolver.getType(uri!!))
    }
}