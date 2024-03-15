package com.example.springbreakchooser

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import java.lang.Exception
import java.util.Locale
import java.util.Objects
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {
    //vars for language choice
    private val REQUEST_CODE_SPEECH_INPUT = 100
    private lateinit var textDisplay: EditText
    private val languages = arrayOf("English", "German", "Chinese")
    private val languageCodes = arrayOf("en-US", "de-DE", "zh-CN")
    private lateinit var languageListView: ListView

    //vars for shake event
    private var sensorManager: SensorManager? = null
    private var acceleration = 0f
    private var currentAcceleration = 0f
    private var lastAcceleration = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //language speech to text
        textDisplay = findViewById(R.id.enteredText)
        languageListView = findViewById(R.id.languageListView)
        languageListView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_single_choice, languages)
        languageListView.choiceMode = ListView.CHOICE_MODE_SINGLE
        languageListView.setOnItemClickListener { _, _, position, _ ->
            val selectedLanguage = languageCodes[position]
            speak(selectedLanguage)
        }

        //shaking
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        Objects.requireNonNull(sensorManager)!!
            .registerListener(sensorListener, sensorManager!!
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL)

        acceleration = 10f
        currentAcceleration = SensorManager.GRAVITY_EARTH
        lastAcceleration = SensorManager.GRAVITY_EARTH


    }
    //speech to text
    private fun speak(language: String) {
        val mIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        mIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        mIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
        mIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hi say something")

        try {
            startActivityForResult(mIntent, REQUEST_CODE_SPEECH_INPUT)
        }
        catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            REQUEST_CODE_SPEECH_INPUT -> {
                if (resultCode == Activity.RESULT_OK && null != data) {
                    val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    textDisplay.setText(result?.get(0) ?: "")
                }
            }
        }
    }

    //shaking sensor listener
    private val sensorListener: SensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            lastAcceleration = currentAcceleration

            currentAcceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
            val delta: Float = currentAcceleration - lastAcceleration
            acceleration = acceleration * 0.9f + delta

            // show a toast when phone is shaken
            if (acceleration > 5) {
                Toast.makeText(applicationContext, "Shake event detected", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }
    override fun onResume() {
        sensorManager?.registerListener(sensorListener, sensorManager!!.getDefaultSensor(
            Sensor .TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL
        )
        super.onResume()
    }

    override fun onPause() {
        sensorManager!!.unregisterListener(sensorListener)
        super.onPause()
    }
}