package com.codewithkael.firebasevideocall.webrtc

import android.content.Context
import android.util.Log
import com.codewithkael.firebasevideocall.utils.DataModel
import com.google.gson.Gson
import com.herohan.uvcapp.ICameraHelper
import org.webrtc.AudioTrack
import org.webrtc.Camera2Enumerator
import org.webrtc.CameraVideoCapturer
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RendererCommon
import org.webrtc.RendererCommon.RendererEvents
import org.webrtc.SessionDescription
import org.webrtc.SurfaceTextureHelper
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoCapturer
import org.webrtc.VideoTrack
import javax.inject.Inject
import javax.inject.Singleton


private const val TAG = "===>>HsRtcClient"

@Singleton
class HsRtcClient @Inject constructor(
    private val context: Context,
    private val gson: Gson
) {
    private lateinit var mCameraHelp: ICameraHelper
    var listener: HSListener? = null
    private lateinit var username: String

    //webrtc variables
    private val eglBaseContext = EglBase.create().eglBaseContext
    private val peerConnectionFactory by lazy { createPeerConnectionFactory() }
    private var peerConnection: PeerConnection? = null
    private val iceServer = listOf(
        PeerConnection.IceServer.builder("turn:a.relay.metered.ca:443?transport=tcp")
            .setUsername("83eebabf8b4cce9d5dbcb649")
            .setPassword("2D7JvfkOQtBdYW3R").createIceServer(),
        PeerConnection.IceServer(
            "turn:openrelay.metered.ca:80",
            "openrelayproject",
            "openrelayproject"
        ),
        PeerConnection.IceServer(
            "turn:openrelay.metered.ca:443",
            "openrelayproject",
            "openrelayproject"
        ),
        PeerConnection.IceServer.builder("stun:iphone-stun.strato-iphone.de:3478")
            .setUsername("83eebabf8b4cce9d5dbcb612")
            .setPassword("2D7JvfkOQtBdYW5t").createIceServer(),
        PeerConnection.IceServer("stun:openrelay.metered.ca:80"),
    )

    //call variables
    private lateinit var localSurfaceView: SurfaceViewRenderer
    private lateinit var remoteSurfaceView: SurfaceViewRenderer
    private var localStream: MediaStream? = null
    private var localTrackId = ""
    private var localStreamId = ""
    private var localAudioTrack: AudioTrack? = null
    private var localVideoTrack: VideoTrack? = null
    private var surfaceTextureHelper: SurfaceTextureHelper? = null
    private val mediaConstraint = MediaConstraints().apply {
        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
    }
    init {
        initPeerConnectionFactory()
    }

    private fun initPeerConnectionFactory() {
        Log.d(TAG, "initPeerConnectionFactory: 1")
        val options = PeerConnectionFactory.InitializationOptions.builder(context)
            .setEnableInternalTracer(true)
            .setFieldTrials("WebRTC-H264HighProfile/Enabled/")
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)
    }

    private fun createPeerConnectionFactory(): PeerConnectionFactory {
        Log.d(TAG, "createPeerConnectionFactory: 2")
        return PeerConnectionFactory.builder()
            .setVideoDecoderFactory(DefaultVideoDecoderFactory(eglBaseContext))
            .setVideoEncoderFactory(
                DefaultVideoEncoderFactory(
                    eglBaseContext, true, true
                )
            )
            .setOptions(PeerConnectionFactory.Options().apply {
                disableNetworkMonitor = false
                disableEncryption = false
            }).createPeerConnectionFactory()
    }

    fun initializeWebrtcClient(username: String, myPeerObserver: MyPeerObserver) {
        Log.d(
            TAG,
            "initializeWebrtcClient() called with-3: username = $username, myPeerObserver = $myPeerObserver"
        )
        this.username = username
        localTrackId = "${username}_track"
        localStreamId = "${username}_stream"
        peerConnection = createPeerConnection(myPeerObserver)
    }

    private fun createPeerConnection(observer: PeerConnection.Observer): PeerConnection? {
        Log.d(TAG, "createPeerConnection: -4")
        return peerConnectionFactory.createPeerConnection(iceServer, observer)
    }
    fun call(target: String) {
        Log.d(TAG, "call() called with-5: target = $target")
        peerConnection?.createOffer(object :MySdpObserver(){

        },mediaConstraint)
    }

    fun initLocalSurfaceView(localView: SurfaceViewRenderer, isVideoCall: Boolean, uvc: Boolean) {
        Log.d(
            TAG,
            "initLocalSurfaceView() called with-6: localView = $localView, videoCall = $isVideoCall, uvc = $uvc"
        )
        this.localSurfaceView = localView
        initSurfaceView(localView)
        startLocalStreaming(localView, isVideoCall)
    }


    val rendererEvents: RendererEvents = object : RendererCommon.RendererEvents {
        override fun onFirstFrameRendered() {
            // This method is called when the first frame is rendered.
            // You can perform any necessary actions here.
            Log.d(TAG, "onFirstFrameRendered: ")
        }

        override fun onFrameResolutionChanged(
            videoWidth: Int,
            videoHeight: Int,
            rotation: Int
        ) {
            Log.d(
                TAG,
                "onFrameResolutionChanged() called with: videoWidth = $videoWidth, videoHeight = $videoHeight, rotation = $rotation"
            )
            // This method is called when the frame resolution or rotation changes.
            // You can handle these changes as needed.
        }
    }

    private val localVideoSource by lazy { peerConnectionFactory.createVideoSource(false) }
    fun onCameraHelper(mCameraHelper: ICameraHelper) {
        Log.d(TAG, "onCameraHelper: --------")
        this.mCameraHelp = mCameraHelper
    }
    private fun initSurfaceView(view: SurfaceViewRenderer) {
        Log.d(TAG, "initSurfaceView() called with-7: view = $view")
        view.run {
            setMirror(false)
            setEnableHardwareScaler(true)
            init(eglBaseContext, rendererEvents)
        }
    }


    private fun startLocalStreaming(localView: SurfaceViewRenderer, isVideoCall: Boolean) {
        Log.d(TAG, "startLocalStreaming: -8")
        localStream = peerConnectionFactory.createLocalMediaStream(localStreamId)
        if (isVideoCall) {
            startCapturingCamera(localView)
        }
    }
    private val videoCapturer = getVideoCapturer(context)
    private fun startCapturingCamera(localView: SurfaceViewRenderer) {
        Log.d(TAG, "startCapturingCamera: -9")
        surfaceTextureHelper = SurfaceTextureHelper.create(
            Thread.currentThread().name, eglBaseContext
        )
      val uvcCam:VideoCapturer=  UsbCapturer(context,localView,mCameraHelp)
      uvcCam.initialize(surfaceTextureHelper,context,localVideoSource.capturerObserver)

       /* videoCapturer?.initialize(
            surfaceTextureHelper, context, localVideoSource.capturerObserver
        )

        videoCapturer?.startCapture(
            720, 480, 20
        )*/
        uvcCam.startCapture(720,480,20)
        localVideoTrack =
            peerConnectionFactory.createVideoTrack(localTrackId + "_video", localVideoSource)
        localVideoTrack?.addSink(localView)
        localStream?.addTrack(localVideoTrack)
    }
    private fun getVideoCapturer(context: Context): CameraVideoCapturer? {

        return   Camera2Enumerator(context).run {
                Log.d(TAG, "getVideoCapturer: ${Camera2Enumerator.isSupported(context)}")
                deviceNames.find {
                    Log.d(TAG, "getVideoCapturer() called find device = $it")
                    isFrontFacing(it)

                }?.let {
                    Log.d(TAG, "getVideoCapturer() called = $it")
                    createCapturer(it, null)
                } ?: throw IllegalStateException()
            }

    }
    fun initRemoteSurfaceView(remoteView: SurfaceViewRenderer, uvc: Boolean) {
        Log.d(TAG, "initRemoteSurfaceView() called with: remoteView = $remoteView, uvc = $uvc")

    }



    fun closeConnection() {
        Log.d(TAG, "closeConnection() called")
    }

    fun onRemoteSessionReceived(sessionDescription: SessionDescription) {
        Log.d(
            TAG,
            "onRemoteSessionReceived() called with: sessionDescription = $sessionDescription"
        )

    }

    fun answer(target: String) {
        Log.d(TAG, "answer() called with: target = $target")
    }

    fun addIceCandidateToPeer(iceCandidate: IceCandidate) {
        Log.d(TAG, "addIceCandidateToPeer() called with: it = $iceCandidate")
    }


    fun sendIceCandidate(target: String, iceCandidate: IceCandidate) {
        Log.d(TAG, "sendIceCandidate() called with: target = $target, it = $iceCandidate")
    }


    interface HSListener {
        fun onHsTransferEventToSocket(data: DataModel)
    }
}