package com.example.senior

import android.app.Service
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
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.senior.movement.StepDetector
import com.example.senior.movement.StepListener
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.firebase.database.FirebaseDatabase
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.RxBleDevice
import com.polidea.rxandroidble2.scan.ScanResult
import com.polidea.rxandroidble2.scan.ScanSettings
import java.time.LocalDateTime
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
    private var uid=""
    private val geocoder= Geocoder(this, Locale.getDefault())
    private var addresses= listOf<Address>()
    private var latitude:Double=0.0
    private var longitude=0.0
    private var databaseLocation=" "
    private var databasePulse=0
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
            var senior=Senior(address, latitude, longitude, databasePulse,numSteps)
            var seniorEarlier=SeniorEarlier(address,databasePulse)
            val seniorResetNow=Senior(address,latitude,longitude,databasePulse,0)
            val seniorReset=SeniorEarlier("",0)
            ref.child("$uid/now").setValue(senior)
            Log.d("jojjo","$mLastLocation")
            Log.d("jojjo","$numSteps")
            mainHandler.postDelayed(this,1000)
            var currentDateTime=LocalDateTime.now()
            var time = currentDateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
            when(time) {
                "00:00" -> ref.child("$uid/0").setValue(seniorEarlier)
                "01:00" -> ref.child("$uid/1").setValue(seniorEarlier)
                "02:00" -> ref.child("$uid/2").setValue(seniorEarlier)
                "03:00" -> ref.child("$uid/3").setValue(seniorEarlier)
                "04:00" -> ref.child("$uid/4").setValue(seniorEarlier)
                "05:00" -> ref.child("$uid/5").setValue(seniorEarlier)
                "06:00" -> ref.child("$uid/6").setValue(seniorEarlier)
                "07:00" -> ref.child("$uid/7").setValue(seniorEarlier)
                "08:00" -> ref.child("$uid/8").setValue(seniorEarlier)
                "09:00" -> ref.child("$uid/9").setValue(seniorEarlier)
                "10:00" -> ref.child("$uid/10").setValue(seniorEarlier)
                "11:00" -> ref.child("$uid/11").setValue(seniorEarlier)
                "12:00" -> ref.child("$uid/12").setValue(seniorEarlier)
                "13:00" -> ref.child("$uid/13").setValue(seniorEarlier)
                "14:00" -> ref.child("$uid/14").setValue(seniorEarlier)
                "15:00" -> ref.child("$uid/15").setValue(seniorEarlier)
                "16:00" -> ref.child("$uid/16").setValue(seniorEarlier)
                "17:00" -> ref.child("$uid/17").setValue(seniorEarlier)
                "18:00" -> ref.child("$uid/18").setValue(seniorEarlier)
                "19:00" -> ref.child("$uid/19").setValue(seniorEarlier)
                "20:00" -> ref.child("$uid/20").setValue(seniorEarlier)
                "21:00" -> ref.child("$uid/21").setValue(seniorEarlier)
                "22:00" -> ref.child("$uid/22").setValue(seniorEarlier)
                "23:00" -> ref.child("$uid/23").setValue(seniorEarlier)

            }
            if(time=="00:01"){
                ref.child("$uid/now").setValue(seniorResetNow)
                numSteps=0
                databasePulse=0
                for(i in 7..21) {
                    ref.child("$uid/$i").setValue(seniorReset)
                }
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
                }
                else{
                    Toast.makeText(
                        this, "Nie można znaleźć lokalizacji",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
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
    class SeniorEarlier(val location:String, val pulse:Int)
}