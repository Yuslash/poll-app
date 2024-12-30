package com.example.anonymous2

import android.content.res.Resources
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.BounceInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.RotateAnimation
import android.view.animation.ScaleAnimation
import android.view.animation.TranslateAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.ComponentActivity
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject

class ChatRoomActivity : ComponentActivity() {

    private lateinit var socket: Socket
    private lateinit var sendButton: Button
    private lateinit var onlineUsers: TextView

    private var selectedEffect: String = "scale_sender" // Default animation effect
    private val userColors: MutableMap<String, Int> = mutableMapOf() // Map to store unique colors for users

    override fun onCreate(savedInstanceState: Bundle?) {

        fun Int.dpToPx(): Int {
            return (this * Resources.getSystem().displayMetrics.density).toInt()
        }

        super.onCreate(savedInstanceState)

        // Set the content view
        setContentView(R.layout.chat_room)

        // here we are gonna create that poll layout in programmatic layout
        val topBar = findViewById<LinearLayout>(R.id.topBar)

        // logic state here
        var isExpand = false

        // create a new layout in programmatically
        val pollLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,

            ).apply {
                setMargins(20,20,20,20)
            }
            setBackgroundResource(R.drawable.radial_background)
            setPadding(40,40,40,40)

            setOnClickListener{
                if(isExpand) {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(20,20,20,20)
                    }
                } else {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, // Expanded width
                        200.dpToPx() // Expanded height
                    ).apply {
                        setMargins(20,20,20,20)
                    }
                }
                isExpand = !isExpand
                requestLayout()
            }
        }

        val imagePoll = ImageView(this).apply {
            setImageResource(R.drawable.poll_icon)
            layoutParams = LinearLayout.LayoutParams(25.dpToPx(), 25.dpToPx()) // Set width and height
        }

        val textPoll = TextView(this).apply {
            text = "Open a Poll"
            setTextAppearance(R.style.collapsedAppearence_Expanded)

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply {
                setMargins(20,0,0,0)
            }
        }

        // top horiztonal for icon and header
        val headerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            )
            gravity = Gravity.CENTER
        }

        // lets write the isExpand logic here to show what it has to be

       headerLayout.addView(imagePoll)
        headerLayout.addView(textPoll)

        pollLayout.addView(headerLayout)

       // adding the new layout below the tobar
        // first we are ognna create a rootLayout

        val rootParent = topBar.parent as LinearLayout
        val tobarIndex = rootParent.indexOfChild(topBar)
        rootParent.addView(pollLayout, tobarIndex + 1)

        // ok we have successufully created the poll layout now we need to expand that

        val messageContainer = findViewById<LinearLayout>(R.id.messageContainer)
        val messageScrollView = findViewById<ScrollView>(R.id.messageScrollView)

        // Connect to the socket server
        socket = IO.socket("https://simple-socketio-production.up.railway.app")
        socket.connect()

        // Handle incoming messages from the server
        socket.on("broadcast_message") { args ->
            runOnUiThread {
                val data = args[0] as JSONObject
                val senderId = data.getString("id")
                val message = data.getString("message")

                // Create a new TextView for each message
                val messageView = TextView(this).apply {
                    text = if (senderId == socket.id()) message else "$message"
                    setPadding(40, 40, 40, 40)
                    setTextAppearance(
                        if (senderId == socket.id()) R.style.customStyles else R.style.receiverMessageBackground
                    )
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(0, 40, 0, 0)
                        if (senderId != socket.id()) gravity = Gravity.START
                    }

                    // Set background color dynamically
                    if (senderId == socket.id()) {
                        setBackgroundResource(R.drawable.message_background)
                    } else {
                        val backgroundDrawable = resources.getDrawable(R.drawable.receivermessage, null)
                        backgroundDrawable?.mutate()?.setTint(getColorForUser(senderId))
                        background = backgroundDrawable
                    }
                }

                // Apply the selected animation effect
                applyMessageAnimation(messageView, selectedEffect)

                // Add a long-press listener for choosing animations
                messageView.setOnLongClickListener {
                    val effects = arrayOf("Scale Sender", "Scale Receiver", "Shake", "Fade In", "Rotate", "Bounce")
                    val effectKeys = arrayOf("scale_sender", "scale_receiver", "shake", "fade_in", "rotate", "bounce")

                    val builder = android.app.AlertDialog.Builder(this@ChatRoomActivity)
                    builder.setTitle("Choose an Animation Effect")
                    builder.setItems(effects) { _, which ->
                        // Update the persistent animation effect for future messages
                        selectedEffect = effectKeys[which]

                        // Apply the selected effect to the current message
                        applyMessageAnimation(messageView, selectedEffect)
                    }
                    builder.show()

                    true // Indicate the long-press event was handled
                }

                // Add the new message view to the container
                messageContainer.addView(messageView)

                // Scroll automatically when a new message is added
                messageContainer.post {
                    messageScrollView.fullScroll(View.FOCUS_DOWN)
                }
            }
        }

        // Handle online user count from the server
        onlineUsers = findViewById(R.id.onlineCount)
        socket.on("online_count") { args ->
            runOnUiThread {
                val onlineCount = args[0] as Int
                onlineUsers.text = "$onlineCount"
            }
        }

        // Handle send button click
        sendButton = findViewById(R.id.sendbtn)
        val messageInput = findViewById<EditText>(R.id.inputmessage)

        sendButton.setOnClickListener {
            val text = messageInput.text.toString()
            if (text.isNotEmpty()) {
                socket.emit("message", text)
                messageInput.text.clear()

                // Hide the keyboard after clicking the button
                val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(messageInput.windowToken, 0)
            }
        }
    }

    // Function to apply animation to a message view
    private fun applyMessageAnimation(messageView: TextView, effect: String) {
        val animation = when (effect) {
            "scale_sender" -> ScaleAnimation(
                0.7f, 1f,  // FromX, ToX
                0.7f, 1f,  // FromY, ToY
                Animation.RELATIVE_TO_SELF, 1f,  // PivotX (right)
                Animation.RELATIVE_TO_SELF, 1f   // PivotY (bottom)
            )
            "scale_receiver" -> ScaleAnimation(
                0.7f, 1f,  // FromX, ToX
                0.7f, 1f,  // FromY, ToY
                Animation.RELATIVE_TO_SELF, 0f,  // PivotX (left)
                Animation.RELATIVE_TO_SELF, 1f   // PivotY (bottom)
            )
            "shake" -> TranslateAnimation(
                -10f, 10f,  // Shake between -10px and +10px horizontally
                0f, 0f      // No vertical movement
            ).apply {
                repeatCount = 5
                repeatMode = Animation.REVERSE
            }
            "fade_in" -> AlphaAnimation(0f, 1f)  // Fade in from transparent to opaque
            "rotate" -> RotateAnimation(
                0f, 360f,  // Rotate from 0° to 360°
                Animation.RELATIVE_TO_SELF, 0.5f,  // PivotX: Center
                Animation.RELATIVE_TO_SELF, 0.5f   // PivotY: Center
            )
            "bounce" -> TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 1f,
                Animation.RELATIVE_TO_SELF, 0f
            ).apply { interpolator = BounceInterpolator() }
            else -> null  // No animation
        }

        animation?.apply {
            duration = 300
            interpolator = DecelerateInterpolator()
            messageView.startAnimation(this)
        }
    }

    // Function to generate or retrieve a unique color for a user
    private fun getColorForUser(userId: String): Int {
        if (!userColors.containsKey(userId)) {
            // Generate a unique color from a predefined palette
            val colors = listOf(
                "#FFCDD2", "#F8BBD0", "#E1BEE7", "#D1C4E9", "#C5CAE9",
                "#BBDEFB", "#B3E5FC", "#B2EBF2", "#B2DFDB", "#C8E6C9",
                "#DCEDC8", "#F0F4C3", "#FFECB3", "#FFE0B2", "#FFCCBC"
            )
            val color = colors[Math.abs(userId.hashCode() % colors.size)]
            userColors[userId] = android.graphics.Color.parseColor(color)
        }
        return userColors[userId]!!
    }

    override fun onDestroy() {
        super.onDestroy()
        socket.disconnect()
    }
}
