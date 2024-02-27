package com.ruuvi.commissioning.decoder;

import com.ruuvi.commissioning.RuuviTag;

public interface RuuviTagDecoder {
    RuuviTag decode(byte[] data, int offset);
}
