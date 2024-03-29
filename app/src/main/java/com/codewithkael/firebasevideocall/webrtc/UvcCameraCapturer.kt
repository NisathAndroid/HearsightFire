package com.codewithkael.firebasevideocall.webrtc

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.media.ImageReader
import android.util.Log
import androidx.core.app.ActivityCompat
import com.herohan.uvcapp.ICameraHelper
import com.serenegiant.usb.IFrameCallback
import org.webrtc.CapturerObserver
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoCapturer
import org.webrtc.VideoFrame
import java.util.concurrent.Executors

private const val TAG = "===>>UvcCameraCap"
class UvcCameraCapturer(
    private val context: Context,
    private val width: Int,
    private val height: Int,
    private val frameRate: Int,
    private val capturerObserver: CapturerObserver,
    private val  mCameraHelp: ICameraHelper
) : VideoCapturer, ImageReader.OnImageAvailableListener {

    private var cameraDevice: CameraDevice? = null
    private var cameraCaptureSession: CameraCaptureSession? = null
    private var imageReader: ImageReader? = null
    private val executor = Executors.newSingleThreadExecutor()

    private val cameraManager: CameraManager by lazy {
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    override fun isScreencast(): Boolean = false
   override fun initialize(p0: SurfaceTextureHelper?, p1: Context?, p2: CapturerObserver?) {
       Log.d(TAG, "initialize: --------")
       mCameraHelp.startPreview()

    }

    override fun startCapture(width: Int, height: Int, framerate: Int) {
        try {
            val cameraId = getUvcCameraId()
            if (cameraId != null) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CAMERA
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }
                cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                    override fun onOpened(camera: CameraDevice) {
                        cameraDevice = camera
                        createCaptureSession()
                    }

                    override fun onDisconnected(camera: CameraDevice) {
                        // Handle camera disconnect
                    }

                    override fun onError(camera: CameraDevice, error: Int) {
                        // Handle camera error
                    }
                }, null)
            } else {
                capturerObserver.onCapturerStarted(false)
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
            capturerObserver.onCapturerStarted(false)
        }
    }

    override fun stopCapture() {
        cameraCaptureSession?.close()
        cameraCaptureSession = null
        cameraDevice?.close()
        cameraDevice = null
        imageReader?.close()
        imageReader = null
    }

    override fun changeCaptureFormat(width: Int, height: Int, framerate: Int) {}
    override fun dispose() {
        TODO("Not yet implemented")
    }

    override fun onImageAvailable(reader: ImageReader?) {
        val image = reader?.acquireLatestImage()
        val planes = image?.planes
        if (planes != null && planes.isNotEmpty()) {
            val buffer = planes[0].buffer
            val data = ByteArray(buffer.remaining())
            buffer.get(data)

            val videoFrame = VideoFrame(
               null,
                0,
                System.nanoTime()
            )
            capturerObserver.onFrameCaptured(videoFrame)
        }
        image?.close()

    }

    private fun getUvcCameraId(): String? {
        try {
            for (cameraId in cameraManager.cameraIdList) {
                val characteristics = cameraManager.getCameraCharacteristics(cameraId)
                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (facing == CameraCharacteristics.LENS_FACING_EXTERNAL) {
                    Log.d(TAG, "getUvcCameraId: =========> true")
                    return cameraId
                }
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
        return null
    }

    private fun createCaptureSession() {
        try {
          /*  val surfaceTexture = SurfaceTextureHelper.create("CaptureThread", null)
            surfaceTexture.setTextureSize(width, height)
            imageReader = ImageReader.newInstance(width, height, CameraEnumerationAndroid.CaptureFormat.FramerateRange(30,40).max, 2)
            imageReader?.setOnImageAvailableListener(this, executor)
            val surfaces = mutableListOf(surfaceTexture.surfaceTexture, imageReader?.surface)
            cameraDevice?.createCaptureSession(surfaces, object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    cameraCaptureSession = session
                    val captureRequestBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                    captureRequestBuilder?.addTarget(surfaceTexture.)
                    captureRequestBuilder?.addTarget(imageReader?.surface)
                    cameraCaptureSession?.setRepeatingRequest(captureRequestBuilder?.build(), null, null)
                    capturerObserver.onCapturerStarted(true)
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {
                    capturerObserver.onCapturerStarted(false)
                }
            }, null)*/
        } catch (e: CameraAccessException) {
            e.printStackTrace()
            capturerObserver.onCapturerStarted(false)
        }
    }
}
