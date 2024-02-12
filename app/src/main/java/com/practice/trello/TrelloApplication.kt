package com.practice.trello

import android.app.Application
import com.bumptech.glide.Glide

class TrelloApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Glide.get(this)
    }
}
