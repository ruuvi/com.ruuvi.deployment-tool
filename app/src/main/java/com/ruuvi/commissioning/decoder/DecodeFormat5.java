package com.ruuvi.commissioning.decoder;

import com.ruuvi.commissioning.RuuviTag;

public class DecodeFormat5 implements RuuviTagDecoder {
    // offset = 7
    @Override
    public RuuviTag decode(byte[] data, int offset) {
        RuuviTag tag = new RuuviTag();
        tag.dataFormat = 5;
        tag.temperature = (data[1 + offset] << 8 | data[2 + offset] & 0xFF) / 200d;
        tag.humidity = ((data[3 + offset] & 0xFF) << 8 | data[4 + offset] & 0xFF) / 400d;
        tag.pressure = (double) ((data[5 + offset] & 0xFF) << 8 | data[6 + offset] & 0xFF) + 50000;
        tag.pressure /= 100.0;

        tag.accelX = (data[7 + offset] << 8 | data[8 + offset] & 0xFF) / 1000d;
        tag.accelY = (data[9 + offset] << 8 | data[10 + offset] & 0xFF) / 1000d;
        tag.accelZ = (data[11 + offset] << 8 | data[12 + offset] & 0xFF) / 1000d;

        int powerInfo = (data[13 + offset] & 0xFF) << 8 | data[14 + offset] & 0xFF;
        if ((powerInfo >>> 5) != 0b11111111111) {
            tag.voltage = (powerInfo >>> 5) / 1000d + 1.6d;
        }
        if ((powerInfo & 0b11111) != 0b11111) {
            tag.txPower = (powerInfo & 0b11111) * 2 - 40;
        }
        tag.movementCounter = data[15 + offset] & 0xFF;
        tag.measurementSequenceNumber = (data[17 + offset] & 0xFF) << 8 | data[16 + offset] & 0xFF;

        return tag;
    }
}