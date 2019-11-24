package com.example.myapplication.Services

import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessagingService


    class MyFirebaseInstanceIDService : FirebaseMessagingService() {
        var TAG = "Something"


        override fun onNewToken(p0: String) {
            super.onNewToken(p0)
            val firebaseUser = FirebaseAuth.getInstance().currentUser

            FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }

                val refreshToken = task.result!!.getToken()
                if (firebaseUser != null) {
                    updateToken(refreshToken)
                    Log.e("newToken", refreshToken)
                }


            })

        }

        companion object {
             fun updateToken(refreshToken: String?) {
                if (refreshToken == null) throw NullPointerException("FCM token is null.")

                ChatUtil.getFCMRegistrationTokens { tokens ->
                        if (tokens.contains(refreshToken))
                            return@getFCMRegistrationTokens

                        tokens.add(refreshToken)
                        ChatUtil.setFCMRegistrationTokens(tokens)
                    }
                }

            }


        }
