package com.example.senior.bluetooth

import android.content.Context
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.scan.ScanResult
import com.polidea.rxandroidble2.scan.ScanSettings

class Ble {
    private val context: Context? = null
    var rxBleClient = RxBleClient.create(context!!)
    var scanSubscription = rxBleClient.scanBleDevices(
        ScanSettings.Builder() // .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY) // change if needed
// .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES) // change if needed
            .build() // add filters if needed
    )
        .subscribe(
            { scanResult: ScanResult? -> }
        ) { throwable: Throwable? -> }
    var macAddress = "AA:BB:CC:DD:EE:FF"
    var device = rxBleClient.getBleDevice(macAddress)

}