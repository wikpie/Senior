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
import com.google.android.gms.location.*
import com.google.android.gms.maps.GoogleMap
import com.google.firebase.database.FirebaseDatabase
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.RxBleDevice
import com.polidea.rxandroidble2.scan.ScanResult
import com.polidea.rxandroidble2.scan.ScanSettings
import io.reactivex.disposables.Disposable
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class SeniorService: Service(), SensorEventListener, StepListener {
    private val name = "Service Senior"
    @RequiresApi(Build.VERSION_CODES.O)
    private lateinit var mainHandler: Handler
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    var mLastLocation: Location? = null
    var uid=""
    private lateinit var locationRequest:LocationRequest
    private lateinit var locationCallback:LocationCallback
    private var latitude=0.0
    private var longitude=0.0
    private var databasePulse=0
    private lateinit var map: GoogleMap
    private val ref=FirebaseDatabase.getInstance().getReference("/seniors")
    private var PRIVATE_MODE = 0
    private val sharedPrefs by lazy { getSharedPreferences("uid", PRIVATE_MODE) }
    private lateinit var sensorManager: SensorManager
    private lateinit var simpleStepDetector: StepDetector
    private lateinit var accel: Sensor
    private var numSteps: Int = 0
    private var macAdrress=""
    val HEART_RATE_SERVICE_UUID = UUID.fromString("0000180D-0000-1000-8000-00805F9B34FB")
    val HEART_RATE_MEASUREMENT_CHAR_UUID = UUID.fromString("00002A37-0000-1000-8000-00805F9B34FB")
    val HEART_RATE_CONTROL_POINT_CHAR_UUID =  UUID.fromString("00002A39-0000-1000-8000-00805F9B34FB")
    private lateinit var rxBleClient: RxBleClient
    private lateinit var scanSubscription: Disposable
    private lateinit var connectSubs: Disposable
    private var listName= mutableListOf<String>()
    private var listMac= mutableListOf<String>()
    private lateinit var device: RxBleDevice

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    private val sendData = object : Runnable {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun run() {
            if(::device.isInitialized){
                connect()
                connectSubs.dispose()
            }
            var senior=Senior(latitude, longitude, databasePulse,numSteps)
            var seniorEarlier=SeniorEarlier(latitude,longitude,databasePulse)
            val seniorResetNow=Senior(latitude,longitude,databasePulse,0)
            val seniorReset=SeniorEarlier(0.0,0.0,0)
            ref.child("$uid/now").setValue(senior)
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
                for(i in 0..23) {
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
                uid= sharedPrefs.getString("uid"," ")!!
            }
        }
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 20 * 1000
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations){
                    latitude =  location.latitude
                    longitude = location.longitude
                }
            }}
        getLocation()
        createNotificationChannel()
        rxBleClient = RxBleClient.create(this)
        scanSubscription =rxBleClient.scanBleDevices(
            ScanSettings.Builder()
                .build()
        )
            .subscribe(
                { scanResult: ScanResult? ->
                    val deviceName= scanResult?.bleDevice?.name
                    val deviceMac= scanResult?.bleDevice?.macAddress
                    if(deviceName!=null) {
                        listName.add(deviceName.toString())
                        listMac.add(deviceMac.toString())
                        scanSubscription.dispose()
                        macAdrress=listMac[listMac.size-1]
                        connect()
                        connectSubs.dispose()
                    }
                },
                { throwable: Throwable? ->
                    return@subscribe
                }
            )
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
                }
                else{
                    Toast.makeText(
                        this, "Nie można znaleźć lokalizacji",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .addOnFailureListener{
                Toast.makeText(
                    this, "Nie można znaleźć lokalizacji",
                    Toast.LENGTH_SHORT
                ).show()
            }

        startLocationUpdates()
    }
    private fun startLocationUpdates() {
        mFusedLocationClient.requestLocationUpdates(locationRequest,
            locationCallback,
            Looper.getMainLooper())
    }
    private fun connect(){
        device=rxBleClient.getBleDevice(macAdrress)
        connectSubs=device.establishConnection(true)
            .flatMapSingle{
                it.readCharacteristic(HEART_RATE_MEASUREMENT_CHAR_UUID) }
            .subscribe ({ characteristicValue->
                databasePulse= characteristicValue.toString().toInt()
            },
                {
                    return@subscribe
                })
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
    class Senior(val latitude:Double, val longitude: Double, val pulse: Int, val steps:Int)
    class SeniorEarlier(val latitude:Double, val longitude: Double, val pulse:Int)
}