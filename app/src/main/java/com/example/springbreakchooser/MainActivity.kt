package com.example.springbreakchooser

import android.app.Activity
import android.content.Intent
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

class MainActivity : AppCompatActivity() {
    private val REQUEST_CODE_SPEECH_INPUT = 100
    private lateinit var textDisplay: EditText
    private val languages = arrayOf("English", "German", "Chinese")
    private val languageCodes = arrayOf("en-US", "de-DE", "zh-CN")
    private lateinit var languageListView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textDisplay = findViewById(R.id.enteredText)
        languageListView = findViewById(R.id.languageListView)
        languageListView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_single_choice, languages)
        languageListView.choiceMode = ListView.CHOICE_MODE_SINGLE
        languageListView.setOnItemClickListener { _, _, position, _ ->
            val selectedLanguage = languageCodes[position]
            speak(selectedLanguage)
        }
    }

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
}