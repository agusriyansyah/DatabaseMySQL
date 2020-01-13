package com.example.tugaselearning

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject

class MainActivity: AppCompatActivity() {
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var locationRequest: LocationRequest
    lateinit var locationCallback: LocationCallback

    val REQUEST_CODE =1000;

    lateinit var i: Intent
    lateinit var add:Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        add = findViewById(R.id.btn_send)

        i=intent
        if(i.hasExtra("ubah data")) {
            if(i.getStringExtra("ubah data").equals("i")) {
                onEditMode()
            }
        }
        add.setOnClickListener{
            onCreate()

        }

        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_CODE
            )
        } else {
            buildLocationRequest()
            buildLocationCallback()

            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_CODE
                )
            }

            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    private fun onCreate() {
        val loading = ProgressDialog(this)
        loading.setMessage("tambahkan data...")
        loading.show()

        AndroidNetworking.post(Api.CREATE)
            .addBodyParameter("isi",e_text_1.text.toString())
            .addBodyParameter("latitude",e_text_2.text.toString())
            .addBodyParameter("longitude",e_text_3.text.toString())
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    loading.dismiss()

                    Toast.makeText(applicationContext,response?.getString("message"),Toast.LENGTH_SHORT).show()

                    if (response?.getString("message")?.contains("berhasil")!!){
                        this@MainActivity.finish()
                    }
                }

                override fun onError(anError: ANError?) {
                    loading.dismiss()
                    Log.d("ONERROR",anError?.errorDetail?.toString())
                    Toast.makeText(applicationContext,"gagal",Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun onEditMode() {

        var text_isi=  e_text_1.text.toString()
        var text_lat = e_text_2.toString()
        var text_long = e_text_3.toString()

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE -> {
                if (grantResults.isNotEmpty()) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "berhasil", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "gagal", Toast.LENGTH_SHORT).show()
                    }

                }
            }
        }
    }

    private fun buildLocationCallback() {

        locationCallback = object : LocationCallback(){
            @SuppressLint("SetTextI18n")
            override fun onLocationResult(p0: LocationResult?) {
                val location = p0?.locations?.get(p0.locations.size-1)
                if (location != null){

                    e_text_2.text = "${location.latitude} "
                    e_text_3.text =" ${location.longitude}"

                }
            }
        }
    }

    private fun buildLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.fastestInterval = 2000
        locationRequest.smallestDisplacement = 20f
    }

}