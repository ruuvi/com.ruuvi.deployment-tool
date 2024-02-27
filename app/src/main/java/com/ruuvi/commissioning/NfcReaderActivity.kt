package com.ruuvi.commissioning

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.NfcAdapter.ACTION_TECH_DISCOVERED
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NfcA
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import kotlin.experimental.and

class NfcReaderActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nfc_reader)
    }

    override fun onResume() {
        super.onResume()

        NfcAdapter.getDefaultAdapter(this)?.let { nfcAdapter ->
            // An Intent to start your current Activity. Flag to singleTop
            // to imply that it should only be delivered to the current
            // instance rather than starting a new instance of the Activity.
            val launchIntent = Intent(this, this.javaClass)
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)

            // Supply this launch intent as the PendingIntent, set to cancel
            // one if it's already in progress. It never should be.
            val pendingIntent = PendingIntent.getActivity(
                this, 0, launchIntent, PendingIntent.FLAG_CANCEL_CURRENT
            )

            // Define your filters and desired technology types
            val filters = arrayOf(IntentFilter(ACTION_TECH_DISCOVERED))
            val techTypes = arrayOf(arrayOf(NfcA::class.java.name))

            // And enable your Activity to receive NFC events. Note that there
            // is no need to manually disable dispatch in onPause() as the system
            // very strictly performs this for you. You only need to disable
            // dispatch if you don't want to receive tags while resumed.
            nfcAdapter.enableForegroundDispatch(
                this, pendingIntent, filters, techTypes
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (NfcAdapter.ACTION_TECH_DISCOVERED == intent.action) {
            try {
                val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG) ?: return
                val data = parseIt(tag)
                if (data.size != 4) {
                    Toast.makeText(this, "NFC read failed", Toast.LENGTH_SHORT).show()
                    return
                }

                var nfcId: String
                val id: String
                val mac: String
                val sw: String
                try {
                    nfcId = Utils.byteArrayToHexString(tag.id)
                    nfcId = Utils.fixId(nfcId)
                    id = data.get(0)!!.replace("ID: ", "")
                    mac = data.get(1)!!.replace("MAC: ", "")
                    sw = data.get(2)!!.replace("SW: ", "")
                    if (id.isEmpty() || mac.isEmpty() || sw.isEmpty() || nfcId.isEmpty()) throw Exception()
                } catch (e: Exception) {
                    Toast.makeText(this, "NFC read failed", Toast.LENGTH_SHORT).show()
                    return
                }
                val result = RuuviTagNfcResult(id, mac, sw)
                val tags = Utils.getTags(this).toMutableList()
                var index = -1
                for (i in 0..tags.size-1) {
                    if (tags[i].mac.equals(result.mac)) {
                        index = i
                        break
                    }
                }
                if (index == -1) {
                    tags.add(result)
                }
                Utils.setTags(this, tags)
                var resultData = Intent()
                resultData.data = Uri.parse(mac)
                setResult(1337, resultData)
                Toast.makeText(this, "RuuviTag detected", Toast.LENGTH_SHORT).show();
            } catch (ex: java.lang.Exception) {
                Toast.makeText(this, "Unable to read NFC, try again", Toast.LENGTH_SHORT).show()
            }
            finish()
        }
    }


    fun parseIt(tag: Tag): List<String?> {
        var list = mutableListOf<String>()
        try {
            val ndef = Ndef.get(tag) ?: // NDEF is not supported by this Tag.
            return list

            val ndefMessage = ndef.cachedNdefMessage

            val records = ndefMessage.records
            records.map { ndefRecord ->
                val str = getTextFromNdefRecord(ndefRecord)
                if (str != null) list.add(str)
            }
        } catch (e: UnsupportedEncodingException) {
            // oh no
        }
        return list
    }


    fun getTextFromNdefRecord(ndefRecord: NdefRecord): String? {
        var result: String? = null
        try {
            val payload = ndefRecord.payload
            val textEncoding = if (payload[0] and 128.toByte() == 0.toByte()) "UTF-8" else "UTF-16"
            val languageSize = payload[0] and 51
            var end = payload.size
            for (i in 0.until(payload.size - 1)) {
                val b = payload[i].toInt()
                if (b == 0) {
                    end = i
                    break
                }
            }
            end = end - languageSize - 1
            result = String(payload, languageSize + 1,
                end, Charset.forName(textEncoding))
        } catch (e: Exception) {
        }

        return result
    }
}
