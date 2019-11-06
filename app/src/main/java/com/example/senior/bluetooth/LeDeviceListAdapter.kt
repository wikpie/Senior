package com.example.senior.bluetooth

import android.bluetooth.BluetoothDevice


class LeDeviceListAdapter {

    // Adapter for holding devices found through scanning.


    private val mLeDevices: ArrayList<BluetoothDevice> = ArrayList<BluetoothDevice>()
//	private LayoutInflater mInflator;
//	private Activity mContext;

    fun addDevice(device: BluetoothDevice) {
        if (!mLeDevices.contains(device)) {
            mLeDevices.add(device)
        }
    }

    fun getDevice(position: Int): BluetoothDevice {
        return mLeDevices[position]
    }

    fun clear() {
        mLeDevices.clear()
    }

    val count: Int
        get() = mLeDevices.size

    fun getItem(i: Int): Any {
        return mLeDevices[i]
    }

    fun getItemId(i: Int): Long {
        return i.toLong()
    }

}