package com.codewithkael.firebasevideocall.webrtc

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.Parcelable
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import com.herohan.uvcapp.CameraHelper
import com.herohan.uvcapp.ICameraHelper
/*import com.jiangdg.ausbc.callback.IPreviewDataCallBack
import com.jiangdg.ausbc.camera.CameraUvcStrategy
import com.jiangdg.ausbc.camera.bean.CameraRequest*/
import org.webrtc.CapturerObserver
import org.webrtc.NV21Buffer
import org.webrtc.SurfaceTextureHelper
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoCapturer
import org.webrtc.VideoFrame
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.logging.LogManager
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "===>>UvcHearsight"
@Singleton
class UvcHearsight @Inject constructor(
   private var ctx: Context?,
    private var svVideoRender: SurfaceViewRenderer?,
   private var hsRtcClient: HsRtcClient?,
   private var isVideoCall: Boolean,
   private var isUVC: Boolean
) : VideoCapturer{



    override fun initialize(p0: SurfaceTextureHelper?, p1: Context?, p2: CapturerObserver?) {
        Log.d(TAG, "initialize: =======================>")
    }

    override fun startCapture(p0: Int, p1: Int, p2: Int) {
        Log.d(TAG, "startCapture: =======================>")
    }

    override fun stopCapture() {

    }

    override fun changeCaptureFormat(p0: Int, p1: Int, p2: Int) {

    }

    override fun dispose() {

    }

    override fun isScreencast(): Boolean {

    return false}



    /*
private var context: Context? = null
private var svVideoRender: SurfaceViewRenderer? = null
private var surfaceTextureHelper: SurfaceTextureHelper? = null
private var capturerObserver: CapturerObserver? = null
private var executor: Executor = Executors.newSingleThreadExecutor()
private var UVC_PREVIEW_WIDTH = 1280
private var UVC_PREVIEW_HEIGHT = 720
private var UVC_PREVIEW_FPS = 30
private var cameraUvcStrategy: CameraUvcStrategy? = null
private var webRTCClient: HsRtcClient? = null
private var isVideoCall: Boolean? = null

init {
    Log.d(TAG, "null() called")
    this.context = ctx
    this.svVideoRender = svVideoRender
    this.webRTCClient = hsRtcClient!!
    this.isVideoCall = isVideoCall
    try {
        cameraUvcStrategy = CameraUvcStrategy(context!!)
    } catch (e: Exception) {
        Toast.makeText(context, "Error--> ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

override fun initialize(
    surfaceTextureHelper: SurfaceTextureHelper?,
    context: Context?,
    capturerObserver: CapturerObserver?
) {
    Log.d(
        TAG,
        "initialize() called with: surfaceTextureHelper = $surfaceTextureHelper, context = $context, capturerObserver = $capturerObserver"
    )
    this.surfaceTextureHelper = surfaceTextureHelper
    this.capturerObserver = capturerObserver
}

override fun startCapture(w: Int, h: Int, fps: Int) {
    Log.d(TAG, "startCapture() called with: w = $w, h = $h, fps = $fps")
    if (cameraUvcStrategy != null) {
        UVC_PREVIEW_FPS = fps
        UVC_PREVIEW_HEIGHT = h
        UVC_PREVIEW_WIDTH = w
        cameraUvcStrategy!!.addPreviewDataCallBack(object : IPreviewDataCallBack {
            override fun onPreviewData(
                data: ByteArray?,
                format: IPreviewDataCallBack.DataFormat
            ) {
                Log.d(TAG, "onPreviewData() called with: data = $data, format = $format")
                val nV21Buffer: NV21Buffer =
                    NV21Buffer(data, UVC_PREVIEW_WIDTH, UVC_PREVIEW_HEIGHT, null)
                val videoFrame: VideoFrame = VideoFrame(nV21Buffer, 0, System.nanoTime())
                capturerObserver?.onFrameCaptured(videoFrame)
            }

        })
        cameraUvcStrategy?.startPreview(getCameraRequest(), svVideoRender?.holder)


    }
}

override fun stopCapture() {
    Log.d(TAG, "stopCapture() called")
    if (cameraUvcStrategy != null) cameraUvcStrategy?.stopPreview()
}

override fun changeCaptureFormat(p0: Int, p1: Int, p2: Int) {
    Log.d(TAG, "changeCaptureFormat() called with: p0 = $p0, p1 = $p1, p2 = $p2")
}

override fun dispose() {

}

override fun isScreencast(): Boolean {

    return false
}

private fun getCameraRequest(): CameraRequest {
    return CameraRequest.Builder()
        .setFrontCamera(false)

        .setContinuousAFModel(true)
        .setContinuousAutoModel(true)
        .setPreviewWidth(UVC_PREVIEW_WIDTH)
        .setPreviewHeight(UVC_PREVIEW_HEIGHT)
        .create()
}*/
}