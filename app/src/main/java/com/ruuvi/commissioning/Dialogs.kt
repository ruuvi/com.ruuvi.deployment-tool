package com.ruuvi.commissioning

import android.app.AlertDialog
import android.content.Context

class Dialogs {
    companion object {
        fun confirm(context: Context, message: String, yes: () -> Unit, no: () -> Unit) {
            val builder = AlertDialog.Builder(context)
            builder.setMessage(message)
            builder.setPositiveButton("Yes"){_,_ ->
                yes()
            }
            builder.setNeutralButton("Cancel"){_,_ ->
                no()
            }
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }
    }
}