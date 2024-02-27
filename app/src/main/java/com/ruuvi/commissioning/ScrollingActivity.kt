package com.ruuvi.commissioning

import android.Manifest
import android.app.ActivityOptions
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_scrolling.*

class ScrollingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scrolling)
        setSupportActionBar(toolbar)
        fab.setOnClickListener { view ->
            val intent = Intent(this, NfcReaderActivity::class.java)
            startActivityForResult(intent, 1337)
        }
        requestPermissions()
    }

    override fun onResume() {
        super.onResume()
        setList()
    }

    fun setList() {
        val tags = Utils.getTags(this)
        val tagAdapter = TagAdapter(tags.toTypedArray(), clickListener = {
            val intent = Intent(this, TagActivity::class.java)
            intent.data = Uri.parse(it)
            startActivityForResult(intent, 1337)
        })
        findViewById<RecyclerView>(R.id.tag_list).apply {
            adapter = tagAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1337 && data != null) {
            val intent = Intent(this, TagActivity::class.java)
            intent.data = data.data
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_scrolling, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        when (item.itemId) {
            R.id.action_export -> {
                CsvExporter(this).toCsv()
                return true
            }
            R.id.action_clear_all -> {
                Dialogs.confirm(this, "Are you sure you want to delete all tags?", yes = {
                    Utils.removeAllTags(this)
                    setList()
                }, no = {
                    // ok then
                    // ¯\_(ツ)_/¯
                })
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            10 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // party
                    (application as App).StartScanning()
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                        requestPermissions()
                    } else {
                        showPermissionSnackbar(this)
                    }
                    Toast.makeText(applicationContext, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showPermissionSnackbar(activity: AppCompatActivity) {
        val snackbar = Snackbar.make(findViewById(R.id.tag_content_container), "Please enable location permission in settings.", Snackbar.LENGTH_LONG)
        snackbar.setAction("Settings") {
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            val uri = Uri.fromParts("package", activity.packageName, null)
            intent.data = uri
            activity.startActivity(intent)
        }
        snackbar.show()
    }

    fun getNeededPermissions(): List<String> {
        val permissionCoarseLocation = ContextCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_COARSE_LOCATION)

        val listPermissionsNeeded = ArrayList<String>()

        if (permissionCoarseLocation != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        return listPermissionsNeeded
    }

    private fun showPermissionDialog(activity: AppCompatActivity): Boolean {
        val listPermissionsNeeded = getNeededPermissions()

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(activity, listPermissionsNeeded.toTypedArray(), 10)
        }

        return !listPermissionsNeeded.isEmpty()
    }

    fun requestPermissions() {
        if (getNeededPermissions().isNotEmpty()) {
            val alertDialog = AlertDialog.Builder(this).create()
            alertDialog.setTitle("Location permission is needed in order to scan for RuuviTags")
            alertDialog.setButton(
                AlertDialog.BUTTON_NEUTRAL, "Ok"
            ) { dialog, _ -> dialog.dismiss() }
            alertDialog.setOnDismissListener { showPermissionDialog(this) }
            alertDialog.show()
        } else {
            checkBluetooth()
        }
    }

    fun checkBluetooth(): Boolean {
        if (isBluetoothEnabled()) {
            return true
        }
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        this.startActivityForResult(enableBtIntent, 87)
        return false
    }

    fun isBluetoothEnabled(): Boolean {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled
    }
}
