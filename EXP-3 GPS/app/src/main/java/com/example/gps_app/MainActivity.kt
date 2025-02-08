package com.example.gps_app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import java.util.*
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    private lateinit var locationManager: LocationManager
    private lateinit var mainLayout: LinearLayout
    private lateinit var weatherCard: LinearLayout
    private lateinit var cityText: TextView
    private lateinit var latitudeText: TextView
    private lateinit var longitudeText: TextView
    private lateinit var temperatureText: TextView
    private lateinit var descriptionText: TextView
    private lateinit var weatherIcon: ImageView

    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Main layout with subtle white gradient background
        mainLayout = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(Color.parseColor("#000000"), Color.parseColor("#f8f8f8"))
            )
            setPadding(30, 50, 30, 50)
        }

        // Weather card
        weatherCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(30, 40, 30, 40)
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 40f
                colors = intArrayOf(Color.parseColor("#ffffff"), Color.parseColor("#f0f0f0"))
            }
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 50)
            }
            // Increase card size by setting a minimum height (e.g., 300dp)
            val minHeight = (450* resources.displayMetrics.density).toInt()
            minimumHeight = minHeight
        }


        // UI elements inside weather card
        cityText = createStyledTextView("Fetching City...", 40f, Color.BLACK)
        latitudeText = createStyledTextView("", 25f, Color.DKGRAY)
        longitudeText = createStyledTextView("", 25f, Color.DKGRAY)
        temperatureText = createStyledTextView("", 30f, Color.BLACK)
        descriptionText = createStyledTextView("", 25f, Color.DKGRAY)
        weatherIcon = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(150, 150).apply {
                setMargins(0, 20, 0, 20)
            }
            setImageResource(android.R.drawable.ic_menu_compass) // Placeholder icon for weather
        }

        // Add icons to the text fields using compound drawables
        // City icon on the left of cityText
        cityText.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_mylocation, 0, 0, 0)
        // Latitude icon on the left of latitudeText
        latitudeText.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_directions, 0, 0, 0)
        // Longitude icon on the left of longitudeText
        longitudeText.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_mapmode, 0, 0, 0)

        // Add UI elements to weather card
        weatherCard.apply {
            addView(cityText)
            addView(latitudeText)
            addView(longitudeText)
            addView(weatherIcon)
            addView(temperatureText)
            addView(descriptionText)
        }

        // Add weather card to the main layout
        mainLayout.addView(weatherCard)

        // Set the main layout as the content view
        setContentView(mainLayout)

        // Initialize location manager
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Request location permission and fetch city
        checkLocationPermissionAndFetchCity()
    }

    private fun createStyledTextView(text: String, textSize: Float, textColor: Int): TextView {
        return TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 10, 0, 10)
            }
            this.text = text
            this.textSize = textSize
            setTextColor(textColor)
            gravity = Gravity.CENTER
        }
    }

    private fun checkLocationPermissionAndFetchCity() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            fetchCity()
        }
    }

    private fun fetchCity() {
        val location = getLastKnownLocation()
        if (location != null) {
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                val cityName = addresses[0].locality
                runOnUiThread {
                    cityText.text = cityName
                    updateRandomValues()
                }
            } else {
                runOnUiThread {
                    cityText.text = "City not found"
                    updateRandomValues()
                }
            }
        } else {
            cityText.text = "Unable to fetch location"
            updateRandomValues()
        }
    }

    private fun updateRandomValues() {
        // Generate random latitude and longitude
        val randomLatitude = Random.nextDouble(-90.0, 90.0)
        val randomLongitude = Random.nextDouble(-180.0, 180.0)
        latitudeText.text = "Latitude: %.4f".format(randomLatitude)
        longitudeText.text = "Longitude: %.4f".format(randomLongitude)

        // Generate random temperature between -10 and 40
        val randomTemperature = Random.nextInt(25, 40)
        temperatureText.text = "$randomTemperature°C"

        // Random weather descriptions
        val weatherDescriptions = listOf("Sunny", "Cloudy",  "Stormy",  "Windy")
        val randomDescription = weatherDescriptions.random()
        descriptionText.text = "Weather: $randomDescription"
    }

    private fun getLastKnownLocation(): Location? {
        val providers = locationManager.getProviders(true)
        var bestLocation: Location? = null
        for (provider in providers) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                val location = locationManager.getLastKnownLocation(provider) ?: continue
                if (bestLocation == null || location.accuracy < bestLocation.accuracy) {
                    bestLocation = location
                }
            }
        }
        return bestLocation
    }
}
