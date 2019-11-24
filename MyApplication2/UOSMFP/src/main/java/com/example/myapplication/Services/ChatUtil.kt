 package com.example.myapplication.Services

import android.content.Context
import android.util.Log
import com.example.myapplication.Interfaces.Message
import com.example.myapplication.Interfaces.MessageType
import com.example.myapplication.Model.ChatChannel
import com.example.myapplication.Model.ImageMessage
import com.example.myapplication.Model.TextMessage
import com.example.myapplication.Model.UserModel
import com.example.myapplication.RecyclerViewItems.ImageMessageItem
import com.example.myapplication.RecyclerViewItems.TextMessageItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.xwray.groupie.kotlinandroidextensions.Item

object ChatUtil {
    private val firestoreInstance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private val current_user: DocumentReference
        get() = firestoreInstance.document("User/${FirebaseAuth.getInstance().currentUser?.uid
                ?: throw NullPointerException("UID is null.")}")

    private val chatChannelsCollectionRef = firestoreInstance.collection("chatChannels")


    fun getCurrentUser(onComplete: (UserModel) -> Unit) {
        current_user.get()
                .addOnSuccessListener {
                    onComplete(it.toObject(UserModel::class.java)!!)
                }
    }


    fun getOrCreateChatChannel(other_user_id: String,
                               onComplete: (channelId: String) -> Unit) {
        current_user.collection("engagedChatChannels").document(other_user_id).get().addOnSuccessListener {
                    if (it.exists()) {
                        onComplete(it["channelId"] as String)
                        return@addOnSuccessListener
                    }

                    val currrentuserid = FirebaseAuth.getInstance().currentUser!!.uid

                    val newChannel = chatChannelsCollectionRef.document()
                    newChannel.set(ChatChannel(mutableListOf(currrentuserid, other_user_id)))

                    current_user.collection("engagedChatChannels").document(other_user_id).set(mapOf("channelId" to newChannel.id))

                    firestoreInstance.collection("User").document(other_user_id).collection("engagedChatChannels").document(currrentuserid).set(mapOf("channelId" to newChannel.id))

                    onComplete(newChannel.id)
                }
    }

    fun addChatMessagesListener(channelId: String, context: Context,
                                onListen: (List<Item>) -> Unit): ListenerRegistration {
        return chatChannelsCollectionRef.document(channelId).collection("messages").orderBy("time").addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    if (firebaseFirestoreException != null) {
                        Log.e("FIRESTORE", "ChatMessagesListener error.", firebaseFirestoreException)
                        return@addSnapshotListener
                    }

                    val items = mutableListOf<Item>()
                    querySnapshot!!.documents.forEach {
                        if (it["type"] == MessageType.TEXT)
                            items.add(TextMessageItem(it.toObject(TextMessage::class.java)!!, context))
                        else
                            items.add(ImageMessageItem(it.toObject(ImageMessage::class.java)!!, context))
                        return@forEach
                    }
                    onListen(items)
                }
    }

    fun sendMessage(message: Message, channelId: String) {
        chatChannelsCollectionRef.document(channelId).collection("messages").add(message)
    }

    //region FCM
    fun getFCMRegistrationTokens(onComplete: (tokens: MutableList<String>) -> Unit) {
        current_user.get().addOnSuccessListener {
            val user = it.toObject(UserModel::class.java)!!
            onComplete(user.registrationTokens)
        }
    }

    fun setFCMRegistrationTokens(registrationTokens: MutableList<String>) {
        current_user.update(mapOf("registrationTokens" to registrationTokens))
    }
    //endregion FCM
}
