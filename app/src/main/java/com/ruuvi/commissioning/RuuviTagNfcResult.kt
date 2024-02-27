package com.ruuvi.commissioning

import java.util.*

data class RuuviTagNfcResult (
    var id : String? = "",
    var mac : String? = "",
    var sw : String? = "",
    var notes : String? = "",
    var addedAt : Date = Date()
)
