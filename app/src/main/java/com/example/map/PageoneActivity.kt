package com.example.map

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.map.monitoring.MainActivity

class PageoneActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pageone)
        //tombol start
        val myButton1 = findViewById<Button>(R.id.in_button_act)
        myButton1.setOnClickListener() {
                val intent = Intent (this, MainActivity::class.java)
                startActivity(intent)

        }
        val btnAbout = findViewById<Button>(R.id.btn_show_dialog)
        btnAbout.setOnClickListener {
            val dialogBinding = layoutInflater.inflate(R.layout.activity_dialog, null)

            val mydialog = Dialog(this)
            mydialog.setContentView(dialogBinding)
            mydialog.setCancelable(true)
            mydialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            mydialog.show()

            val dialogText = "Cara penggunaan alat dan aplikasi dapat dilihat pada buku panduan melalui link berikut https://bit.ly/4eTa88B"

            // Pendeteksian URL di dalam teks
            val urlPattern = Patterns.WEB_URL
            val clickableText = SpannableString(dialogText)
            val matcher = urlPattern.matcher(dialogText)

            while (matcher.find()) {
                val start = matcher.start()
                val end = matcher.end()

                clickableText.setSpan(object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        // Intent untuk membuka URL
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(dialogText.substring(start, end))
                        startActivity(intent)
                    }
                }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }

            // Menetapkan teks yang dapat diklik ke dalam TextView di dalam dialog
            val textView = dialogBinding.findViewById<TextView>(R.id.alert_message2)
            textView.text = clickableText
            textView.movementMethod = LinkMovementMethod.getInstance()

            val yesbtn = dialogBinding.findViewById<Button>(R.id.alert_yes)
            yesbtn.setOnClickListener {
                mydialog.dismiss()
            }
        }

        checkNotificationPermission()
        //checkLocationServices()

    }

    private fun requestPermission(){
        ActivityCompat.requestPermissions(
            this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),0
        )
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS),1)
    }

    private fun checkNotificationPermission(){
        if (ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ){

        }else{
            requestPermission()
        }
    }

    private fun checkLocationServices() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!isGpsEnabled && !isNetworkEnabled) {
            Toast.makeText(this, "Location services are disabled", Toast.LENGTH_LONG).show()

            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        } else {
            Toast.makeText(this, "Location services are enabled", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0){
            Toast.makeText(this, getString(R.string.notification_granted), Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(this, getString(R.string.notification_ungranted), Toast.LENGTH_SHORT).show()
        }
        if (requestCode==1){
            if (grantResults.isNotEmpty() && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, getString(R.string.notification_granted), Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this, getString(R.string.notification_ungranted), Toast.LENGTH_SHORT).show()
            }
        }
    }
}