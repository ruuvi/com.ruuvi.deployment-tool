package com.ruuvi.commissioning

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

class CsvExporter(val context: Context) {
    fun toCsv() {
        val tags = Utils.getTags(context)
        val cacheDir = File(context.cacheDir.path + "/export/")
        cacheDir.mkdirs()
        val csvFile = File.createTempFile(
            "ruuvi_export_" + Date().time + "_",
            ".csv",
            cacheDir
        )
        var fileWriter: FileWriter?

        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
        try {
            fileWriter = FileWriter(csvFile.absolutePath)

            fileWriter.append("added,mac,sw,notes,last seen,dataFormat,temperature,humidity,pressure,rssi,acceleration x,acceleration y,acceleration z,voltage,movement counter,measurement sequence number")
            fileWriter.append('\n')


            tags.forEach {
                fileWriter.append(df.format(it.addedAt))
                fileWriter.append(',')
                fileWriter.append(it.mac)
                fileWriter.append(',')
                fileWriter.append(it.sw)
                fileWriter.append(',')
                fileWriter.append(it.notes)
                val tag = Utils.getTag(context, it.mac!!)
                if (tag != null) {
                    fileWriter.append(',')
                    fileWriter.append(df.format(tag.updateAt))
                    fileWriter.append(',')
                    fileWriter.append(tag.dataFormat.toString())
                    fileWriter.append(',')
                    fileWriter.append(tag.temperature.toString())
                    fileWriter.append(',')
                    fileWriter.append(tag.humidity.toString())
                    fileWriter.append(',')
                    fileWriter.append(tag.pressure.toString())
                    fileWriter.append(',')
                    fileWriter.append(tag.rssi.toString())
                    if (tag.dataFormat == 3 || tag.dataFormat == 5) {
                        fileWriter.append(',')
                        fileWriter.append(tag.accelX.toString())
                        fileWriter.append(',')
                        fileWriter.append(tag.accelY.toString())
                        fileWriter.append(',')
                        fileWriter.append(tag.accelZ.toString())
                        fileWriter.append(',')
                        fileWriter.append(tag.voltage.toString())
                    }
                    if (tag.dataFormat == 5) {
                        fileWriter.append(',')
                        fileWriter.append(tag.movementCounter.toString())
                        fileWriter.append(',')
                        fileWriter.append(tag.measurementSequenceNumber.toString())
                    } else if (tag.dataFormat == 3) {

                        fileWriter.append(",,")
                    }
                } else {
                    fileWriter.append(",,,,,,,,,,,,")
                }
                fileWriter.append('\n')
            }

            fileWriter.flush()
            fileWriter.close()
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to create CSV file", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
            return
        }

        Toast.makeText(context, ".csv created, opening share menu", Toast.LENGTH_SHORT).show()
        val uri = FileProvider.getUriForFile(context, "com.ruuvi.commissioning.fileprovider", csvFile)

        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_STREAM, uri)
        sendIntent.type = "text/csv"
        sendIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        context.startActivity(Intent.createChooser(sendIntent, "RuuviTag csv export"))
    }
}