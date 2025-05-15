package com.cyberguard.ui.ai

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.cyberguard.R
import android.os.Handler
import android.os.Looper
import android.graphics.Color
import android.graphics.drawable.RippleDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.content.res.ColorStateList

class ChatFragment : Fragment() {

    private lateinit var chatInput: EditText
    private lateinit var chatSendButton: ImageButton
    private lateinit var chatContainer: LinearLayout
    private lateinit var chatScrollView: ScrollView

    // a list just for storing messages while chatting only!
    private val chatMessages = mutableListOf<Pair<Boolean, String>>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        //inflating of layout
        val view = inflater.inflate(R.layout.activity_chat_popup, container, false)

        // here we made views (the xml layout stuff)
        chatInput = view.findViewById(R.id.chatInput)
        chatSendButton = view.findViewById(R.id.chatSendButton)
        chatContainer = view.findViewById(R.id.chatContainer)
        chatScrollView = view.findViewById(R.id.chatScrollView)

        // a button for sending messages to the ai model
        setupSendButton()

        // in case saved instances not null restore chat
        if (savedInstanceState != null) {
            val savedMessages = savedInstanceState.getStringArrayList("chatMessages")
            if (savedMessages != null) {
                for (message in savedMessages) {
                    val isUser = message.startsWith("USER:")
                    val messageText = if (isUser) message.substringAfter("USER:") else message.substringAfter("AI:")
                    chatMessages.add(Pair(isUser, messageText))
                    addMessageToChat(messageText, isUser, false) //
                }
            }
        }

        // just a click listener when we try to send a message
        chatSendButton.setOnClickListener {
            val userMessage = chatInput.text.toString().trim()
            if (userMessage.isNotEmpty()) {
                addMessageToChat(userMessage, true, true) // animation (when writing messages)
                chatMessages.add(Pair(true, userMessage)) // save user message

                // AI call
                ChatGPTHelper().askOpenRouter(userMessage) { response ->
                    activity?.runOnUiThread {
                        addMessageToChat(response, false, true) // AI response with animation
                        chatMessages.add(Pair(false, response)) // save AI response
                    }
                }
                chatInput.text.clear()
            }
        }

        return view
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val savedMessages = ArrayList<String>()
        for ((isUser, message) in chatMessages) {
            savedMessages.add(if (isUser) "USER:$message" else "AI:$message")
        }
        outState.putStringArrayList("chatMessages", savedMessages)
    }

    private fun setupSendButton() {
        chatSendButton.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    chatSendButton.setBackgroundColor(Color.parseColor("#30000000"))
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    chatSendButton.setBackgroundColor(Color.TRANSPARENT)
                    chatSendButton.performClick()
                    true
                }
                else -> false
            }
        }

        chatSendButton.background = createRippleDrawable()
    }

    private fun createRippleDrawable(): RippleDrawable {
        val rippleColor = ColorStateList.valueOf(Color.parseColor("#30000000"))
        val mask = ShapeDrawable(OvalShape())
        return RippleDrawable(rippleColor, null, mask)
    }

    private fun addMessageToChat(message: String, isUser: Boolean, animate: Boolean) {
        if (chatContainer.childCount > 0) {
            val lastMessageView = chatContainer.getChildAt(chatContainer.childCount - 1)
            val lastMessageText = if (isUser) {
                lastMessageView.findViewById<TextView>(R.id.UserSection).text.toString()
            } else {
                lastMessageView.findViewById<TextView>(R.id.cyberguardSection).text.toString()
            }
            if (lastMessageText == message) {
                return // just skip adding the message here if it already exists
            }
        }

        val inflater = LayoutInflater.from(requireContext())

        val chatBubble = inflater.inflate(R.layout.cyberguard_chat, chatContainer, false)

        val userText = chatBubble.findViewById<TextView>(R.id.UserText)
        val userSection = chatBubble.findViewById<TextView>(R.id.UserSection)
        val cyberguardText = chatBubble.findViewById<TextView>(R.id.cyberguardText)
        val cyberguardSection = chatBubble.findViewById<TextView>(R.id.cyberguardSection)

        userSection.setTextIsSelectable(true)
        cyberguardSection.setTextIsSelectable(true)

        if (isUser) {
            // user message setup
            userText.visibility = TextView.VISIBLE
            userSection.visibility = TextView.VISIBLE
            cyberguardText.visibility = TextView.GONE
            cyberguardSection.visibility = TextView.GONE

            userText.text = "You"
            userSection.text = message
        } else {
            // AI message setup
            userText.visibility = TextView.GONE
            userSection.visibility = TextView.GONE
            cyberguardText.visibility = TextView.VISIBLE
            cyberguardSection.visibility = TextView.VISIBLE

            cyberguardText.text = "CyberGuard"

            if (animate) {
                val handler = Handler(Looper.getMainLooper())
                val typingSpeed: Long = 5 // milliseconds between characters

                var index = 0
                val typingRunnable = object : Runnable {
                    override fun run() {
                        if (index <= message.length) {
                            cyberguardSection.text = message.substring(0, index)
                            index++
                            handler.postDelayed(this, typingSpeed)
                        }
                    }
                }
                handler.post(typingRunnable)
            } else {
                // skip animation for restored messages
                cyberguardSection.text = message
            }
        }

        // gravity for user and AI messages
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.gravity = if (isUser) Gravity.END else Gravity.START
        params.setMargins(0, 0, 0, 0)
        chatBubble.layoutParams = params

        chatContainer.addView(chatBubble)

        chatScrollView.post {
            chatScrollView.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }
}