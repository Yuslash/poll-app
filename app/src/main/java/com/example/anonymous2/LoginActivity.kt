package com.example.anonymous2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : ComponentActivity() {

    private lateinit var userId: EditText
    private lateinit var password: EditText
    private lateinit var loginbtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        userId = findViewById(R.id.identity)
        password = findViewById(R.id.input2)
        loginbtn = findViewById(R.id.loginbtn)


        // Login animatioins
        val scrollView = findViewById<HorizontalScrollView>(R.id.horizotanlMan)
        val container = findViewById<LinearLayout>(R.id.imageAnimationContainer)


        startSeamlessScrolling(scrollView,container)

        // we are gonna add the text to userId and password by inputs field and click button
        loginbtn.setOnClickListener {
            // first intial values from input to value
            val id = userId.text.toString()
            val pass = password.text.toString()

            //lets create a edge case if user leaves the input field blanks
            if (id.isBlank() || pass.isBlank()) {
              Toast.makeText(this, "Please enter userid and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // lets initial the request
            val request = LoginRequest(id, pass)

            //lets send the request to server and get back the response
            RetrofitClient.instance.login(request).enqueue(object: Callback<LoginResponse> {
                //create a response function
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if(response.isSuccessful) {
                        val intent = Intent(this@LoginActivity, ChatRoomActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, "Invalid credentitals", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                   Toast.makeText(this@LoginActivity, "App call failed ${t.message}", Toast.LENGTH_SHORT).show()
                }

            })

        }

    }


    // scrolll animation private function
   private fun startSeamlessScrolling(scrollView: HorizontalScrollView, container: LinearLayout) {
       scrollView.post {
           val totalWidth = container.measuredWidth // Total width of the container
           val initialX = 0
           val speed = 5     // Adjust this for scrolling speed (lower is faster)

           scrollView.scrollTo(initialX, 0) // Start from the beginning

           scrollView.post(object : Runnable {
               override fun run() {
                   val currentX = scrollView.scrollX
                   if (currentX >= totalWidth / 2) {
                       // Reset to start when half of the duplicated content is scrolled
                       scrollView.scrollTo(0, 0)
                   } else {
                       // Scroll by a small amount
                       scrollView.scrollBy(speed, 0)
                   }
                   scrollView.postDelayed(this, 16) // Approx. 60 FPS
               }
           })
       }
   }


}