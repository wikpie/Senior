package com.example.senior

import android.annotation.TargetApi
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private var PRIVATE_MODE = 0
    private val bluetoothAdapter:BluetoothAdapter?= BluetoothAdapter.getDefaultAdapter()
    private val REQUEST_ENABLE_BT = 1
    private val sharedPrefs by lazy { getSharedPreferences("main", PRIVATE_MODE) }
    private val ref=FirebaseDatabase.getInstance().getReference("seniors")
    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val permissions = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION)
        ActivityCompat.requestPermissions(this, permissions,0)
        Log.d("Main", sharedPrefs.getBoolean("main", false).toString())
        if (sharedPrefs.getBoolean("main", false)) {
            checkBluetoothWorking(this)
            // setContentView(R.layout.senior_service)
            Intent(this, SeniorService::class.java).also { intent ->
                startForegroundService(intent)
            }
        } else {
            setContentView(R.layout.activity_main)
            confirm.setOnClickListener {
                val uid=writeUid.text.toString()
                val name=name.text.toString()
                ref.child("$uid/imie").setValue(name)
                Intent(this, SeniorService::class.java).also { intent ->
                    intent.putExtra("uid",uid)
                    intent.putExtra("name",name)
                    startForegroundService(intent)
                }
                val editor = sharedPrefs.edit()
                editor.putBoolean("main", true)
                editor.apply()
            }

        }

    }
    private fun checkBluetoothWorking(context: Context){
        if (bluetoothAdapter == null) {
            Toast.makeText(context,"Bluetooth is not supported on this device", Toast.LENGTH_SHORT).show()
        }
        else{
            if (!bluetoothAdapter.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent,REQUEST_ENABLE_BT)
            }
        }
    }


}


