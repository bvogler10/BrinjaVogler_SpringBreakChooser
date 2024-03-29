package com.example.springbreakchooser

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import java.lang.Exception
import java.util.Locale

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    //vars for language choice
    private val REQUEST_CODE_SPEECH_INPUT = 100
    private lateinit var textDisplay: EditText
    private val languages = arrayOf("English", "German", "Chinese")
    private val languageCodes = arrayOf("en-US", "de-DE", "zh-CN")
    private lateinit var languageListView: ListView
    private var currentLanguage: String = "en-US"
    private val locations = mapOf(
        "en-US" to listOf("New York City", "Boston"),
        "zh-CN" to listOf("Beijing", "Taipei"),
        "de-DE" to listOf("Berlin", "Dresden")
    )
    private lateinit var tts: TextToSpeech
    private var greetingSpoken = false
    private var googleOpened = false

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
            currentLanguage = selectedLanguage
            speak(selectedLanguage)
        }

        //shaking
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager?
        val sensorShake = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(sensorEvent: SensorEvent) {
                if (sensorEvent != null) {
                    val x = sensorEvent.values[0]
                    val y = sensorEvent.values[1]
                    val z = sensorEvent.values[2]

                    if (x > 2 || x < -2 || y > 12 || y < -12 || z > 2 || z < -2) {
                        if (!googleOpened) {
                            openGoogleMaps(currentLanguage)
                        }
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor, i: Int) {
                // Do nothing
            }
        }
        sensorManager?.registerListener(sensorEventListener, sensorShake, SensorManager.SENSOR_DELAY_NORMAL)
        //text to speech
        tts = TextToSpeech(this, this)

    }
    //speech to text
    private fun speak(language: String) {
        val mIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        mIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        mIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
        mIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something")

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

    private fun openGoogleMaps(language: String) {
        googleOpened = true
        val languageLocations = locations[currentLanguage]
        val spot = languageLocations!!.random()
        val locale = Locale(language)
        tts.setLanguage(locale)
        val greeting = when (language) {
            "en-US" -> "Hello"
            "zh-CN" -> "你好"
            "de-DE" -> "Guten Tag"
            else -> "Hello"
        }
        if (!greetingSpoken) {
            tts.speak(greeting, TextToSpeech.QUEUE_FLUSH, null, "")
            greetingSpoken = true
        }
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("geo:0,0?q=$spot")
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        tts.stop()
        tts.shutdown()
        greetingSpoken = false
        googleOpened = false
    }

    override fun onResume() {
        super.onResume()
        greetingSpoken = false
        googleOpened = false
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val locale = Locale(currentLanguage)
            tts.setLanguage(locale)
        }
    }
}