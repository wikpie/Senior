package com.example.senior.bluetooth

import android.app.ListActivity
import android.bluetooth.BluetoothAdapter
import android.os.Handler

private const val SCAN_PERIOD: Long = 10000

/**
 * Activity for scanning and displaying available BLE devices.
 */
class DeviceScan(
    private val bluetoothAdapter: BluetoothAdapter,
    private val handler: Handler
) : ListActivity() {

    private var mScanning: Boolean = false

    private fun scanLeDevice(enable: Boolean) {
        when (enable) {
            true -> {
                // Stops scanning after a pre-defined scan period.
                handler.postDelayed({
                    mScanning = false
                   // bluetoothAdapter.stopLeScan(leScanCallback)
                }, SCAN_PERIOD)
                mScanning = true
               // bluetoothAdapter.startLeScan(leScanCallback)
            }
            else -> {
                mScanning = false
               // bluetoothAdapter.stopLeScan(leScanCallback)
            }
        }
    }
    val leDeviceListAdapter=LeDeviceListAdapter()

    private val leScanCallback = BluetoothAdapter.LeScanCallback { device, rssi, scanRecord ->
        runOnUiThread {
            leDeviceListAdapter.addDevice(device)
        }
    }
}