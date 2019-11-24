package com.example.myapplication.Model

data class ChatChannel(val userIds: MutableList<String>) {
    constructor() : this(mutableListOf())
    //holda all collections of the firestore and pulls the client end
}