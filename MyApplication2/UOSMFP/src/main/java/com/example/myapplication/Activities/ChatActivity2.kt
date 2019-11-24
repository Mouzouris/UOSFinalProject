package com.example.myapplication.Activities

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.Fragments.APIService
import com.example.myapplication.Model.ImageMessage
import com.example.myapplication.Model.TextMessage
import com.example.myapplication.Model.UserModel
import com.example.myapplication.Notifications.Client
import com.example.myapplication.R
import com.example.myapplication.Services.ChatUtil
import com.example.myapplication.Services.StorageUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Section
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.activity_chat2.*
import java.io.ByteArrayOutputStream
import java.util.*

private const val RC_SELECT_IMAGE = 2

 class ChatActivity2 : AppCompatActivity() {

    private lateinit var currentChannelId: String
    private lateinit var current_user: UserModel
    private lateinit var otherUserId: String

    private lateinit var messagesListenerRegistration: ListenerRegistration
    private var shouldInitRecyclerView = true
    private lateinit var messagesSection: Section
    private lateinit var service : APIService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat2)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = intent.getStringExtra("USER_NAME")


       ChatUtil.getCurrentUser {
            current_user = it
        }

        otherUserId = intent.getStringExtra("USER_ID")!!
        ChatUtil.getOrCreateChatChannel(otherUserId) { channelId ->
            currentChannelId = channelId

            messagesListenerRegistration =
                    ChatUtil.addChatMessagesListener(channelId, this, this::updateRecyclerView)

            imageView_send.setOnClickListener {
                val messageToSend =
                        TextMessage(editText_message.text.toString(), Calendar.getInstance().time,
                                FirebaseAuth.getInstance().currentUser!!.uid,
                                otherUserId, current_user.name)
                if (editText_message.text.toString() == ""){
                    Toast.makeText(applicationContext, "You can't send an empty message", Toast.LENGTH_SHORT).show()
                }else {
                    ChatUtil.sendMessage(messageToSend, channelId)
                    editText_message.setText("")
                }
            }

            fab_send_image.setOnClickListener {
                val intent = Intent().apply {
                    type = "image/*"
                    action = Intent.ACTION_GET_CONTENT
                    putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))
                }
                startActivityForResult(Intent.createChooser(intent, "Select Image"), RC_SELECT_IMAGE)
            }
        }
        service = Client.getClient("https://fcm.googleapis.com/").create(APIService::class.java)

    }


     override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
         super.onActivityResult(requestCode, resultCode, data)
         if (requestCode == RC_SELECT_IMAGE && resultCode == Activity.RESULT_OK &&
                data != null && data.data != null) {
            val selectedImagePath = data.data

             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                 val selectedImageBmp =  ImageDecoder.createSource(contentResolver, selectedImagePath!!)
                 val outputStream = ByteArrayOutputStream()
                val conversion =ImageDecoder.decodeBitmap(selectedImageBmp)
                 conversion.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                   val selectedImageBytes = outputStream.toByteArray()

                StorageUtil.uploadMessageImage(selectedImageBytes) { imagePath ->
                    val messageToSend =
                            ImageMessage(imagePath, Calendar.getInstance().time,
                                    FirebaseAuth.getInstance().currentUser!!.uid,
                                    otherUserId, current_user.name)
                    ChatUtil.sendMessage(messageToSend, currentChannelId)
                }
            } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P){

                val selectedImageBmp=MediaStore.Images.Media.getBitmap(contentResolver, selectedImagePath)

             val outputStream = ByteArrayOutputStream()

            selectedImageBmp.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            val selectedImageBytes = outputStream.toByteArray()

            StorageUtil.uploadMessageImage(selectedImageBytes) { imagePath ->
                val messageToSend =
                        ImageMessage(imagePath, Calendar.getInstance().time,
                                FirebaseAuth.getInstance().currentUser!!.uid,
                                otherUserId, current_user.name)
                ChatUtil.sendMessage(messageToSend, currentChannelId)
            }
            }
        }
    }

    private fun updateRecyclerView(messages: List<Item>) {
        fun init() {
            recycler_view_messages.apply {
                layoutManager = LinearLayoutManager(this@ChatActivity2)
                adapter = GroupAdapter<ViewHolder>().apply {
                    messagesSection = Section(messages)
                    this.add(messagesSection)
                }
            }
            shouldInitRecyclerView = false
        }

        fun updateItems() = messagesSection.update(messages)

        if (shouldInitRecyclerView)
            init()
        else
            updateItems()

        recycler_view_messages.scrollToPosition(recycler_view_messages.adapter!!.itemCount - 1)
    }



}