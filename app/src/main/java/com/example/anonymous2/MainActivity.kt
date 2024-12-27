package com.example.anonymous2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val navigateButton = findViewById<Button>(R.id.btn_navigate)
        val chatroomnavi = findViewById<Button>(R.id.chatroombtn)

        // Set click listener
        navigateButton.setOnClickListener {
            // Navigate to LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        chatroomnavi.setOnClickListener {
            // Navigate to chat room
            val intent = Intent(this, ChatRoomActivity::class.java)
            startActivity(intent)
        }
    }
}