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
import com.github.julienfauvel.localisation4ltrophy.models.Coordonnee
import com.github.kittinunf.fuel.httpGet


class LocalisationActivity : AppCompatActivity(), LocationListener {

    var longitude: Double? = 0.0
    var latitude: Double? = 0.0

    private val permissions = listOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION)

    private var locationManager: LocationManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_localisation)
        setSupportActionBar(toolbar)

        this.locationManager = getLocationManager()
        if (!this.locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER)!!) {
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

        checkPermissions()

        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        this.locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10f, this)

        val lastLocation = this.locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        setLocation(lastLocation)

        "http://api.4lentraid.com/coordonnee".httpGet().responseObject(Coordonnee.Deserializer()) { _, response, result ->
            val (coordonnee, err) = result
            if(err == null) {
                this.tv_ville_old.text =  getString(R.string.ancienne_position, coordonnee?.ville)
                this.tv_longitude_old.text = coordonnee?.longitude.toString()
                this.tv_latitude_old.text = coordonnee?.latitude.toString()
            } else {
                this.tv_ville_old.text =  getString(R.string.ancienne_position, "")
                this.tv_longitude_old.text = getString(R.string.default_value)
                this.tv_latitude_old.text = getString(R.string.default_value)
                Snackbar.make(this@LocalisationActivity.contraintLayout, "Error retrieving old position", Snackbar.LENGTH_LONG)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        this.locationManager?.removeUpdates(this)
    }

    override fun onResume() {
        super.onResume()
        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        this.locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10f, this)
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

    override fun onLocationChanged(location: Location?) {
        setLocation(location)
    }

    private fun setLocation(location: Location?) {
        this.longitude = location?.longitude
        this.latitude = location?.latitude

        tv_longitude.text = getString(R.string.tv_longitude, this.longitude)
        tv_latitude.text = getString(R.string.tv_latitude, this.latitude)
    }

    private fun checkPermissions() {
        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, Array(1, {_ -> permissions[0] }), 200)
        }
        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, Array(1, {_ -> permissions[1] }), 200)
        }
    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
    }

    override fun onProviderEnabled(p0: String?) {
    }

    override fun onProviderDisabled(p0: String?) {
    }
}
