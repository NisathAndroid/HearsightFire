package com.codewithkael.firebasevideocall.utils

enum class UserStatus {
    ONLINE,OFFLINE,IN_CALL
}

object IntentAction{
    const val isVideoCall="isVideoCall"
    const val isCaller="isCaller"
    const val target="target"
}