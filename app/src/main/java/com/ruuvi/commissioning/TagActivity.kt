package com.ruuvi.commissioning

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar

import kotlinx.android.synthetic.main.activity_tag.*
import kotlinx.android.synthetic.main.content_tag.*
import android.bluetooth.BluetoothAdapter



class TagActivity : AppCompatActivity() {
    lateinit var ruuviTag: RuuviTagNfcResult

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tag)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        val mac = intent.dataString
        val tags = Utils.getTags(this).toMutableList()
        for (tag in tags) {
            if (tag.mac.equals(mac)) {
                ruuviTag = tag
                break
            }
        }

        mac_tw.text = mac_tw.text.toString() + " " + ruuviTag.mac
        sw_tw.text = sw_tw.text.toString() + " " + ruuviTag.sw
        notes.setText(ruuviTag.notes)
        fab.setOnClickListener { view ->
            Dialogs.confirm(this, "Are you sure you want to remove this tag?", yes = {
                tags.removeAt(tags.indexOf(ruuviTag))
                Utils.setTags(this, tags)
                finish()
            }, no = {
            })
        }
    }

    override fun onPause() {
        super.onPause()
        val tags = Utils.getTags(this).toMutableList()
        ruuviTag.notes = notes.text.toString()
        var index = -1
        for (i in 0..tags.size-1) {
            if (tags[i].mac == ruuviTag.mac) {
                index = i
                break
            }
        }
        if (index != -1) {
            tags.set(index, ruuviTag)
        }
        Utils.setTags(this, tags)
    }

    override fun onSupportNavigateUp(): Boolean {
        finishAfterTransition()
        return true
    }

}
