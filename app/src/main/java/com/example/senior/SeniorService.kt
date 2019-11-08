package com.example.senior

import android.app.Service
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.app.NotificationCompat
import com.example.senior.movement.StepDetector
import com.example.senior.movement.StepListener
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
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*


class SeniorService: Service(), SensorEventListener, StepListener {
    private val name = "Service Senior"
    @RequiresApi(Build.VERSION_CODES.O)
    val timeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-mm-yy HH:mm")
    private lateinit var mainHandler: Handler
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    var mLastLocation: Location? = null
    private var address=""
    private var city=""
    private var uid=""
    private val geocoder= Geocoder(this, Locale.getDefault())
    private var addresses= listOf<Address>()
    private var latitude:Double=0.0
    private var longitude=0.0
    private var databaseLocation=" "
    private var databasePulse=0
    private var databaseSteps=0
    private lateinit var map: GoogleMap
    private val ref=FirebaseDatabase.getInstance().getReference("/seniors")
    private var PRIVATE_MODE = 0
    private val sharedPrefs by lazy { getSharedPreferences("uid", PRIVATE_MODE) }

    private lateinit var sensorManager: SensorManager
    private lateinit var simpleStepDetector: StepDetector
    private lateinit var accel: Sensor
    private var numSteps: Int = 0




    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
    private val sendData = object : Runnable {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun run() {
            //setTime()
            var senior=Senior(address, latitude, longitude, 0,numSteps)
            ref.child("$uid/now").setValue(senior)
            Log.d("jojjo","$mLastLocation")
            Log.d("jojjo","$numSteps")
            Log.d("jojjo","$city")
            mainHandler.postDelayed(this,1000)
            var currentDateTime=LocalDateTime.now()
            var time = currentDateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
            if(time=="08:00"){
                ref.child("$uid/8am").setValue(senior)
            }
            if(time=="15:00"){
                ref.child("$uid/3pm").setValue(senior)
            }
        }
    }
    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            if (intent.hasExtra("uid"))  {
                uid = intent.getStringExtra("uid")
                val editor = sharedPrefs.edit()
                editor.putString("uid", uid)
                editor.apply()
            }
            else{
                uid= sharedPrefs.getString("uid","jo")!!

            }
        }
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getLocation()
        createNotificationChannel()
        mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post(sendData)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        simpleStepDetector = StepDetector()
        simpleStepDetector.registerListener(this)
        sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_FASTEST)
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
            .setContentText(uid)
            .setSmallIcon(R.drawable.ic_local_hospital_black_24dp)
            .setContentIntent(pendingIntent)
            .build()
        startForeground(1, notification)
        Toast.makeText(
            this, "Rozpoczęcie pracy serwisu",
            Toast.LENGTH_SHORT
        ).show()
        //check if bluetooth works

        return START_STICKY
    }
    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            simpleStepDetector.updateAccel(
                event.timestamp, event.values[0], event.values[1], event.values[2]
            )
        }
    }
    override fun step(timeNs: Long) {
        numSteps++
        //text_steps.text = "Kroki:$numSteps"
    }
    private fun getLocation(){
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
                    Log.d("jojoj", "$city" )
                }
                else{
                    Toast.makeText(
                        this, "Nie można znaleźć lokalizacji",
                        Toast.LENGTH_SHORT
                    ).show()
                }}
            .addOnFailureListener{
                Log.d("jojjo","gównogównogówno")
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
    class Senior(val location:String,val latitude:Double, val longitude: Double, val pulse: Int, val steps:Int)

}