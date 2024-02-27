package com.ruuvi.commissioning;

import android.bluetooth.BluetoothDevice;
import android.util.Base64;
import android.util.Log;
import com.neovisionaries.bluetooth.ble.advertising.ADManufacturerSpecific;
import com.neovisionaries.bluetooth.ble.advertising.ADPayloadParser;
import com.neovisionaries.bluetooth.ble.advertising.ADStructure;
import com.neovisionaries.bluetooth.ble.advertising.EddystoneURL;
import com.ruuvi.commissioning.decoder.DecodeFormat2and4;
import com.ruuvi.commissioning.decoder.DecodeFormat3;
import com.ruuvi.commissioning.decoder.DecodeFormat5;
import com.ruuvi.commissioning.decoder.RuuviTagDecoder;

import java.util.Date;
import java.util.List;

public class RuuviTag {
    private static final String TAG = "RuuviTag";

    public String id;
    public String url;
    public int rssi;
    public double[] data;
    public double temperature;
    public double humidity;
    public double pressure;
    public byte[] rawData;
    public double accelX;
    public double accelY;
    public double accelZ;
    public double voltage;
    public Date updateAt;
    public int dataFormat;
    public double txPower;
    public int movementCounter;
    public int measurementSequenceNumber;

    public static RuuviTag Parse(BluetoothDevice device, int rssi, byte[] scanData) {
        RuuviTag tag = null;

        try {
            // Parse the payload of the advertisement packet
            // as a list of AD structures.
            List<ADStructure> structures =
                    ADPayloadParser.getInstance().parse(scanData);

            // For each AD structure contained in the advertisement packet.
            for (ADStructure structure : structures) {
                if (structure instanceof EddystoneURL) {
                    // Eddystone URL
                    EddystoneURL es = (EddystoneURL) structure;
                    if (es.getURL().toString().startsWith("https://ruu.vi/#") || es.getURL().toString().startsWith("https://r/")) {
                        tag = from(device.getAddress(), es.getURL().toString(), null, rssi);
                    }
                }
                // If the AD structure represents Eddystone TLM.
                else if (structure instanceof ADManufacturerSpecific) {
                    ADManufacturerSpecific es = (ADManufacturerSpecific) structure;
                    if (es.getCompanyId() == 0x0499) {
                        tag = from(device.getAddress(), null, scanData, rssi);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Parsing ble data failed");
        }

        if (tag != null) tag.updateAt = new Date();
        return tag;
    }

    private static RuuviTag from(String id, String url, byte[] rawData, int rssi) {
        RuuviTagDecoder decoder = null;
        if (url != null && url.contains("#")) {
            String data = url.split("#")[1];
            rawData = parseByteDataFromB64(data);
            decoder = new DecodeFormat2and4();
        } else if (rawData != null) {
            int protocolVersion = rawData[7];
            switch (protocolVersion) {
                case 3:
                    decoder = new DecodeFormat3();
                    break;
                case 5:
                    decoder = new DecodeFormat5();
                    break;
            }
        }
        if (decoder != null) {
            RuuviTag tag = decoder.decode(rawData, 7);
            if (tag != null) {
                tag.id = id;
                tag.url = url;
                tag.rssi = rssi;
                tag.rawData = rawData;
            }
            return tag;
        }
        return null;
    }

    private static byte[] parseByteDataFromB64(String data) {
        try {
            byte[] bData = Base64.decode(data, 0);
            int pData[] = new int[8];
            for (int i = 0; i < bData.length; i++)
                pData[i] = bData[i] & 0xFF;
            return bData;
        } catch (Exception e) {
            return null;
        }
    }
}
