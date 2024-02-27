package com.ruuvi.commissioning

import java.math.BigDecimal
import java.math.RoundingMode
import android.preference.PreferenceManager
import android.content.Context
import com.google.gson.Gson


class Utils {
    companion object {
        fun round(value: Double, places: Int): Double {
            if (places < 0) throw IllegalArgumentException()

            var bd = BigDecimal(value)
            bd = bd.setScale(places, RoundingMode.HALF_UP)
            return bd.toDouble()
        }

        fun byteArrayToHexString(inarray: ByteArray): String {
            var i: Int
            var j: Int
            var `in`: Int
            val hex = arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F")
            var out = ""

            j = 0
            while (j < inarray.size) {
                `in` = inarray[j].toInt() and 0xff
                i = `in` shr 4 and 0x0f
                out += hex[i]
                i = `in` and 0x0f
                out += hex[i]
                ++j
            }
            return out
        }

        fun fixId(mac: String): String {
            var out = StringBuilder(mac)
            var i = mac.length - 2
            while (i > 0) {
                out.insert(i, ":")
                i -= 2
            }
            return out.toString().toUpperCase()
        }

        private fun capitalize(s: String?): String {
            if (s == null || s.isEmpty()) {
                return ""
            }
            val first = s[0]
            return if (Character.isUpperCase(first)) {
                s
            } else {
                Character.toUpperCase(first) + s.substring(1)
            }
        }

        const val TAGS_KEY = "RUUVITAGS"
        fun setTags(context: Context, tags: List<RuuviTagNfcResult>): Boolean {
            val gson = Gson()
            val value = gson.toJson(tags)
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            if (preferences != null) {
                val editor = preferences.edit()
                editor.putString(TAGS_KEY, value)
                return editor.commit()
            }
            return false
        }

        fun getTags(context: Context): List<RuuviTagNfcResult> {
            var value: String? = null
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            if (preferences != null) {
                value = preferences.getString(TAGS_KEY, null)
            }
            if (value == null) return emptyList()
            val gson = Gson()
            return gson.fromJson(value, Array<RuuviTagNfcResult>::class.java).asList()
        }

        fun removeAllTags(context: Context) {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            if (preferences != null) {
                val editor = preferences.edit()
                editor.putString(TAGS_KEY, null)
                editor.commit()
            }
        }

        const val TAG_KEY = "RUUVITAG:"
        fun setTag(context: Context, tag: RuuviTag): Boolean {
            val gson = Gson()
            val value = gson.toJson(tag)
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            if (preferences != null) {
                val editor = preferences.edit()
                editor.putString(TAG_KEY+tag.id, value)
                return editor.commit()
            }
            return false
        }

        fun getTag(context: Context, mac: String): RuuviTag? {
            var value: String? = null
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            if (preferences != null) {
                value = preferences.getString(TAG_KEY+mac, null)
            }
            if (value == null) return null
            val gson = Gson()
            return gson.fromJson(value, RuuviTag::class.java)
        }
    }
}