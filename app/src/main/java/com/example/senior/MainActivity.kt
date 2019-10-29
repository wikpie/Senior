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
    private val ref=FirebaseDatabase.getInstance().getReference("senior")
    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("Main", sharedPrefs.getBoolean("main", false).toString())
        if (sharedPrefs.getBoolean("main", false)) {
            checkBluetoothWorking(this)
            val permissions = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION)
            ActivityCompat.requestPermissions(this, permissions,0)
            // setContentView(R.layout.senior_service)
            Intent(this, SeniorService::class.java).also { intent ->
                startForegroundService(intent)
            }
        } else {
            setContentView(R.layout.activity_main)
            confirm.setOnClickListener {
                val uid=writeUid.text.toString()
                //if()
                Intent(this, SeniorService::class.java).also { intent ->
                    startForegroundService(intent)
                }
                val editor = sharedPrefs.edit()
                editor.putBoolean("main", true)
                editor.apply()
            }

        }
        val handler = Handler()
        val timer = Timer()
        val doAsynchronousTask = object : TimerTask() {
            override fun run() {
                handler.post {
                    try {
                        //setTime()
                        //val senior=SeniorService.Senior("sa",0,0)
                        //ref.child("1").setValue(senior)
                    } catch (e: Exception) {
                        Log.d("serwis",e.toString())
                    }
                }
            }
        }
        timer.schedule(doAsynchronousTask, 0, 10000)


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


