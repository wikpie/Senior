package com.example.senior

import android.app.Service
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

import com.google.android.gms.maps.model.Marker
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class SeniorService: Service(),OnMapReadyCallback {
    private val name = "Service Senior"
    @RequiresApi(Build.VERSION_CODES.O)
    val timeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-mm-yy HH:mm")
    private lateinit var timeHandler: Handler
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    var mLastLocation: Location? = null
    private var address=""
    private var city=""
    private val geocoder= Geocoder(this, Locale.getDefault())
    private var addresses= listOf<Address>()
    private var latitude:Double=0.0
    private var longitude=0.0
    private var databaseLocation=" "
    private var databasePulse=0
    private var databaseSteps=0
    private lateinit var map: GoogleMap
    private val ref=FirebaseDatabase.getInstance().getReference("/seniors")



    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
    private val updateTime = object : Runnable {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun run() {
            setTime()
            timeHandler.postDelayed(this, 1000)
           // var senior=Senior(databaseLocation,databasePulse,databaseSteps)
           // ref.child("1").setValue(senior)
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val input = intent?.getStringExtra("inputExtra")
        createNotificationChannel()
        Log.d("serwis", ref.toString())
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, 0
        )
        //var senior=Senior(databaseLocation,databasePulse,databaseSteps)
        //ref.child("1").setValue(senior)
        val notification = NotificationCompat.Builder(this, name)
            .setContentTitle("Serwis monitorujący zachowanie osoby starszej")
            .setContentText(input)
            .setSmallIcon(R.drawable.ic_local_hospital_black_24dp)
            .setContentIntent(pendingIntent)
            .build()
        startForeground(1, notification)
        Toast.makeText(
            this, "Rozpoczęcie pracy serwisu",
            Toast.LENGTH_SHORT
        ).show()
        //check if bluetooth works
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val handler = Handler()
        val timer = Timer()
        val doAsynchronousTask = object : TimerTask() {
            override fun run() {
                handler.post {
                    try {
                        setTime()
                        //val senior=Senior(databaseLocation,databasePulse,databaseSteps)
                        //ref.child("1").setValue(senior)
                    } catch (e: Exception) {
                    Log.d("serwis",e.toString())
                    }
                }
            }
        }
        timer.schedule(doAsynchronousTask, 0, 60000)
        return START_STICKY
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map=googleMap

        mFusedLocationClient.lastLocation
            .addOnSuccessListener{ location: Location? ->
                if(location!=null){
                    mLastLocation=location
                    latitude =  location.latitude
                    longitude = location.longitude
                    Log.d("Main",latitude.toString())
                    Log.d("Main",longitude.toString())
                    addresses=geocoder.getFromLocation(latitude,longitude,1)
                    address= addresses[0].getAddressLine(0)
                    city= addresses[0].locality
                    val senior=Senior(city+address, mLastLocation!!,0,0)
                    ref.child("marek").setValue(senior)
                }
                else{
                    Toast.makeText(
                        this, "Nie można znaleźć lokalizacji",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }

    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setTime(){
        val currentTime= LocalDateTime.now()
        val time=currentTime.format(timeFormat)
    }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                name, "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager!!.createNotificationChannel(serviceChannel)
        }



    }
    class Senior(val location:String,val locationLat:Location, val pulse: Int, val steps:Int ) {

    }

}