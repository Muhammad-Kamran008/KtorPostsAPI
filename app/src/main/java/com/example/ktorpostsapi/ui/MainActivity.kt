package com.example.ktorpostsapi.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.InputType
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.ktorpostsapi.R
import com.example.ktorpostsapi.databinding.ActivityMainBinding
import com.example.ktorpostsapi.model.Post
import com.example.ktorpostsapi.network.ApiRepository
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding:ActivityMainBinding
    private lateinit var chipGroup: ChipGroup
    private lateinit var headlineText: TextView
    private lateinit var urlTextField: TextInputEditText
    private lateinit var sendButton: Button
    private lateinit var loadingIndicator: ProgressBar
    private lateinit var responseText: TextView

    private val chipsList = listOf("/POST", "/GET", "/DELETE", "/PUT")
    private var selectedHeadline = chipsList[0]
    private val apiRepo = ApiRepository()
    private var post = Post(
        title = "How to Make HTTP Requests With Ktor-Client in Android",
        id = 1,
        body = "Ktor is a client-server framework that helps us build applications in Kotlin. It is a modern asynchronous framework backed by Kotlin coroutines.",
        userId = "1"
    )

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Views
        chipGroup = binding.chipGroup
        headlineText = binding.headlineText
        urlTextField = binding.urlTextField
        sendButton = binding.sendButton
        loadingIndicator = binding.loadingIndicator
        responseText = binding.responseText

        val urlTextField =binding.urlTextField
        var isLocked = true // Initially locked

        lockTextField(urlTextField, isLocked)

        urlTextField.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableStart = 0

                if (event.rawX <= (urlTextField.compoundDrawables[drawableStart].bounds.width() + urlTextField.paddingStart)) {
                    isLocked = !isLocked
                    lockTextField(urlTextField, isLocked)
                    return@setOnTouchListener true
                }
            }
            false
        }

        setupChipGroup()

        sendButton.setOnClickListener {
            handleApiCall()
        }
    }

    private fun lockTextField(editText: TextInputEditText, lock: Boolean) {
        if (lock) {
            // Disable editing and lock the field
            editText.inputType = InputType.TYPE_NULL
            editText.isFocusable = false
            editText.isFocusableInTouchMode = false
            editText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock_closed, 0, 0, 0)
        } else {
            // Enable editing
            editText.inputType = InputType.TYPE_TEXT_VARIATION_URI
            editText.isFocusable = true
            editText.isFocusableInTouchMode = true
            editText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock_open, 0, 0, 0)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupChipGroup() {
        chipsList.forEachIndexed { index, label ->
            val chip = Chip(this).apply {
                text = label
                isCheckable = true
                isCheckedIconVisible = true
                setOnClickListener {
                    selectedHeadline = chipsList[index]
                    responseText.text = ""
                    headlineText.text = selectedHeadline
                }
            }
            chipGroup.addView(chip)
        }
    }

    private fun handleApiCall() {
        loadingIndicator.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            var jsonResponse = ""
            when (selectedHeadline) {
                "/POST" -> jsonResponse = apiRepo.createNewPost(post).toString()
                "/GET" -> jsonResponse = apiRepo.getAllPosts().toString()
                "/PUT" -> jsonResponse = apiRepo.updatePost(1, post).toString()
                "/DELETE" -> jsonResponse = apiRepo.deletePost(2).status.toString()
            }
            withContext(Dispatchers.Main) {
                loadingIndicator.visibility = View.GONE
                responseText.text = jsonResponse
            }
        }
    }
}
