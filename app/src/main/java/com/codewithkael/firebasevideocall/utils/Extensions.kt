package com.codewithkael.firebasevideocall.utils

import android.os.Build
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.permissionx.guolindev.PermissionX

fun AppCompatActivity.getCameraAndMicPermission(success:()->Unit){
    val p = mutableListOf<String>()
    p.add(android.Manifest.permission.CAMERA)
    p.add(android.Manifest.permission.RECORD_AUDIO)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
       // p.add(android.Manifest.permission.FOREGROUND_SERVICE_MEDIA_PROJECTION)
        p.add(android.Manifest.permission.FOREGROUND_SERVICE)
        p.add(android.Manifest.permission.FOREGROUND_SERVICE_CAMERA)

    }
    PermissionX.init(this)
        .permissions(p)
        .request{allGranted,_,_ ->

            if (allGranted){
                success()
            } else{
                Toast.makeText(this, "camera and mic permission is required", Toast.LENGTH_SHORT)
                    .show()
            }
        }
}

fun Int.convertToHumanTime() : String{
    val seconds = this%60
    val minutes = this/60
    val secondsString = if (seconds<10) "0$seconds" else "$seconds"
    val minutesString = if (minutes < 10) "0$minutes" else "$minutes"
    return "$minutesString:$secondsString"
}