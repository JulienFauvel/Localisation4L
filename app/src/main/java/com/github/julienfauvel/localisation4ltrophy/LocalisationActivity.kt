package com.github.julienfauvel.localisation4ltrophy

import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem

import kotlinx.android.synthetic.main.activity_localisation.*
import kotlinx.android.synthetic.main.content_localisation.*
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.view.View
import com.github.julienfauvel.localisation4ltrophy.models.Coordonnee
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result


class LocalisationActivity : AppCompatActivity() {

    var longitude: Double? = null
    var latitude: Double? = null

    private val permissions = listOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION)

    private var locationManager: LocationManager? = null

    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            setLocation(location)
        }
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_localisation)
        setSupportActionBar(toolbar)

        // Activate GPS if not enabled
        this.locationManager = getLocationManager()
        if (!this.locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER)!!) {
            //Ask the user to enable GPS
            AlertDialog.Builder(this)
                .setTitle("Location Manager")
                .setMessage("Would you like to enable GPS?")
                .setPositiveButton("Yes", { _, _ ->
                    //Launch settings, allowing user to make a change
                    val i = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(i)
                })
                .setNegativeButton("No", { _, _ ->
                    //No location service, no Activity
                    finish()
                })
                .create()
                .show()
        }

        // Initialize application
        checkPermissions()
        setLocationListener()
        initView()
        getCoordonnee()
    }

    private fun setLocationListener() {
        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        this.locationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 0f, locationListener)
        val lastLocation = this.locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        setLocation(lastLocation)
    }

    private fun initView() {
        this.tv_ville_old.text = getString(R.string.ancienne_position, "?")

        this.retry.setOnClickListener {
            getCoordonnee()
        }

        this.btn_valider.setOnClickListener {
            this.btn_valider.isEnabled = false

            when (postCoordonnee()) {
                false -> this.btn_valider.isEnabled = true
            }
        }
    }

    private fun getCoordonnee() {
        "http://api.4lentraid.com/coordonnees".httpGet().responseObject(Coordonnee.DeserializerArray()) { _, _, result ->
            val (coordonnees, err) = result
            val lastCoordonnee = coordonnees?.get(coordonnees.size-1)
            if(err == null) {
                this.retry.visibility = View.INVISIBLE
                this.tv_ville_old.text =  getString(R.string.ancienne_position, lastCoordonnee?.ville)
                this.tv_longitude_old.text = getString(R.string.tv_longitude, lastCoordonnee?.longitude)
                this.tv_latitude_old.text = getString(R.string.tv_latitude, lastCoordonnee?.latitude)
            } else {
                this.retry.visibility = View.VISIBLE
                this.tv_ville_old.text =  getString(R.string.errorHttp)
                this.tv_longitude_old.text = getString(R.string.default_value)
                this.tv_latitude_old.text = getString(R.string.default_value)
                Snackbar.make(this@LocalisationActivity.contraintLayout, "Error retrieving old position", Snackbar.LENGTH_LONG)
            }
        }
    }

    private fun postCoordonnee(): Boolean {
        if(this.tv_ville.text.isEmpty()) {
            this.tv_ville.error = "La ville doit être renseignée"
            return false
        }

        if(this.longitude == null || this.latitude == null) {
            this.infoHttp.text = getString(R.string.errorGps)
            return false
        }

        val newCoordonnee = Coordonnee(this.tv_ville.text.toString(), this.longitude!!, this.latitude!!)
        Fuel.Companion.post("http://api.4lentraid.com/coordonnee", listOf("latitude" to newCoordonnee.latitude, "longitude" to newCoordonnee.longitude, "ville" to newCoordonnee.ville))
                .response { request, response, result ->
                    this.btn_valider.isEnabled = true

                    when (result) {
                        is Result.Success -> setPostSuccess()
                        is Result.Failure -> setPostError()
                    }
                }

        return true
    }

    private fun setPostSuccess() {
        this.infoHttp.text = getString(R.string.httpOk)
        this.infoHttp.setTextColor(android.R.color.holo_green_dark)
    }

    private fun setPostError() {
        this.infoHttp.text = getString(R.string.httpKo)
        this.infoHttp.setTextColor(android.R.color.holo_red_dark)
    }

    override fun onPause() {
        super.onPause()
        this.locationManager?.removeUpdates(locationListener)
    }

    override fun onResume() {
        super.onResume()
        setLocationListener()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_localisation, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun getLocationManager(): LocationManager {
        return getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    private fun setLocation(location: Location?) {
        if (location != null) {
            this.longitude = location.longitude
            this.latitude = location.latitude
            tv_longitude.text = getString(R.string.tv_longitude, this.longitude)
            tv_latitude.text = getString(R.string.tv_latitude, this.latitude)
        }
    }

    private fun checkPermissions() {
        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, Array(1, {_ -> permissions[0] }), 200)
        }
        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, Array(1, {_ -> permissions[1] }), 200)
        }
    }
}
