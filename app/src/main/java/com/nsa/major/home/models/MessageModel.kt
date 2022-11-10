package com.nsa.major.home.models

data class MessageModel(
    val name:String?=null,
    val senderId:String?=null,
    val message:String?=null,
    var messageId:String?=null,
    val time:Long?=null
)
