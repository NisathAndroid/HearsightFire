package com.codewithkael.firebasevideocall.repository

import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.codewithkael.firebasevideocall.firebaseClient.FirebaseClient
import com.codewithkael.firebasevideocall.utils.DataModel
import com.codewithkael.firebasevideocall.utils.DataModelType.*
import com.codewithkael.firebasevideocall.utils.TYPE_OF_MODE
import com.codewithkael.firebasevideocall.utils.UserStatus
import com.codewithkael.firebasevideocall.webrtc.HsRtcClient
import com.codewithkael.firebasevideocall.webrtc.MyPeerObserver
import com.codewithkael.firebasevideocall.webrtc.UvcHearsight
import com.codewithkael.firebasevideocall.webrtc.WebRTCClient
import com.google.gson.Gson
import org.webrtc.*
import javax.inject.Inject
import javax.inject.Singleton
private val TAG = "===>>MainRepository"
@Singleton
class MainRepository @Inject constructor(
    private val firebaseClient: FirebaseClient,
    private val webRTCClient: WebRTCClient,
    private val hsRtcClient: HsRtcClient,
    private val uvcHearsight: UvcHearsight,
    private val gson: Gson
) : WebRTCClient.Listener, HsRtcClient.HSListener {

    private var target: String? = null
    var listener: Listener? = null
    private var remoteView: SurfaceViewRenderer? = null

    fun login(username: String, password: String, isDone: (Boolean, String?) -> Unit) {
        firebaseClient.login(username, password, isDone)
    }

    fun observeUsersStatus(status: (List<Pair<String, String>>) -> Unit) {
        firebaseClient.observeUsersStatus(status)
    }

    fun initFirebase() {
        Log.d(TAG, "initFirebase: ")
        firebaseClient.subscribeForLatestEvent(object : FirebaseClient.Listener {
            override fun onLatestEventReceived(event: DataModel) {
                listener?.onLatestEventReceived(event)
                when (event.type) {
                    Offer -> {
                        if (TYPE_OF_MODE.RUN_UVC_MODE=="1"){
                            Log.d(TAG, "onLatestEventReceived: RUN_UVC_MODE =Offer")
                            hsRtcClient.onRemoteSessionReceived(
                                SessionDescription(
                                    SessionDescription.Type.OFFER,
                                    event.data.toString()
                                )
                            )
                            if (target != null) {
                                hsRtcClient.answer(target!!)
                            } else
                                Log.d(TAG, "onLatestEventReceived: target is NULL... ")
                        }else{
                            webRTCClient.onRemoteSessionReceived(
                                SessionDescription(
                                    SessionDescription.Type.OFFER,
                                    event.data.toString()
                                )
                            )
                            if (target != null) {
                                webRTCClient.answer(target!!)
                            } else
                                Log.d(TAG, "onLatestEventReceived: target is NULL... ")
                        }
                      

                    }

                    Answer -> {
                        if (TYPE_OF_MODE.RUN_UVC_MODE=="1"){
                            Log.d(TAG, "onLatestEventReceived: RUN_UVC_MODE =Answer")
                            hsRtcClient.onRemoteSessionReceived(
                                SessionDescription(
                                    SessionDescription.Type.ANSWER,
                                    event.data.toString()
                                )
                            )
                        }else{
                            webRTCClient.onRemoteSessionReceived(
                                SessionDescription(
                                    SessionDescription.Type.ANSWER,
                                    event.data.toString()
                                )
                            )
                        }
                        
                    }

                    IceCandidates -> {
                        if (TYPE_OF_MODE.RUN_UVC_MODE=="1"){
                            Log.d(TAG, "onLatestEventReceived: RUN_UVC_MODE =IceCandidates")
                            val candidate: IceCandidate? = try {
                                gson.fromJson(event.data.toString(), IceCandidate::class.java)
                            } catch (e: Exception) {
                                null
                            }
                            candidate?.let {
                                hsRtcClient.addIceCandidateToPeer(it)
                            }
                        }else{
                            val candidate: IceCandidate? = try {
                                gson.fromJson(event.data.toString(), IceCandidate::class.java)
                            } catch (e: Exception) {
                                null
                            }
                            candidate?.let {
                                webRTCClient.addIceCandidateToPeer(it)
                            }
                        }
                    
                    }

                    EndCall -> {
                        Log.d(TAG, "onLatestEventReceived: RUN_UVC_MODE =EndCall")
                        if (TYPE_OF_MODE.RUN_UVC_MODE=="1"){
                            listener?.endCall()
                        }else{
                            listener?.endCall()    
                        }
                     
                    }

                    else -> Unit
                }
            }

        })
    }

    fun sendConnectionRequest(target: String, isVideoCall: Boolean, success: (Boolean) -> Unit) {
        firebaseClient.sendMessageToOtherClient(
            DataModel(
                type = if (isVideoCall) StartVideoCall else StartAudioCall,
                target = target
            ), success
        )
    }

    fun setTarget(target: String) {
        this.target = target
    }


    fun initHsRtcClient(username: String) {
        Log.d(TAG, "initHsRtcClient: ")
        hsRtcClient.listener = this
        hsRtcClient.initializeWebrtcClient(username, object : MyPeerObserver() {

            override fun onAddStream(p0: MediaStream?) {
                super.onAddStream(p0)
                Log.d(TAG, "onAddStream: ")
                try {
                    p0?.videoTracks?.get(0)?.addSink(remoteView)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }

            override fun onIceCandidate(p0: IceCandidate?) {
                super.onIceCandidate(p0)
                Log.d(TAG, "onIceCandidate: ")
                p0?.let {
                    hsRtcClient.sendIceCandidate(target!!, it)
                }
            }

            override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) {
                super.onConnectionChange(newState)
                Log.d(TAG, "onConnectionChange: ")
                if (newState == PeerConnection.PeerConnectionState.CONNECTED) {
                    // 1. change my status to in call
                    changeMyStatus(UserStatus.IN_CALL)
                    // 2. clear latest event inside my user section in firebase database
                    firebaseClient.clearLatestEvent()
                }
            }
        })
    }
    fun initWebrtcClient(username: String) {
        webRTCClient.listener = this
        webRTCClient.initializeWebrtcClient(username, object : MyPeerObserver() {

            override fun onAddStream(p0: MediaStream?) {
                super.onAddStream(p0)
                try {
                    p0?.videoTracks?.get(0)?.addSink(remoteView)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }

            override fun onIceCandidate(p0: IceCandidate?) {
                super.onIceCandidate(p0)
                p0?.let {
                    webRTCClient.sendIceCandidate(target!!, it)
                }
            }

            override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) {
                super.onConnectionChange(newState)
                if (newState == PeerConnection.PeerConnectionState.CONNECTED) {
                    // 1. change my status to in call
                    changeMyStatus(UserStatus.IN_CALL)
                    // 2. clear latest event inside my user section in firebase database
                    firebaseClient.clearLatestEvent()
                }
            }
        })
    }
    fun initLocalSurfaceView(localView: SurfaceViewRenderer, isVideoCall: Boolean, isUVC: Boolean) {
        if (TYPE_OF_MODE.RUN_UVC_MODE == "1") {
            hsRtcClient.initLocalSurfaceView(localView, isVideoCall, isUVC)
        } else {
            webRTCClient.initLocalSurfaceView(localView, isVideoCall, isUVC)
        }

    }

    fun initRemoteSurfaceView(remoteView: SurfaceViewRenderer, isUVC: Boolean) {
        if (TYPE_OF_MODE.RUN_UVC_MODE == "1") {
            hsRtcClient.initRemoteSurfaceView(remoteView, isUVC)
            this.remoteView = remoteView
        } else {
            webRTCClient.initRemoteSurfaceView(remoteView, isUVC)
            this.remoteView = remoteView
        }
    }

    fun startCall() {
        if (TYPE_OF_MODE.RUN_UVC_MODE == "1") {
            Log.d(TAG, "startCall: ")
            hsRtcClient.call(target!!)
        } else {
            webRTCClient.call(target!!)
        }
    }

    fun endCall() {
        if(TYPE_OF_MODE.RUN_UVC_MODE=="1"){
            hsRtcClient.closeConnection()
            changeMyStatus(UserStatus.ONLINE)
        }else{
            webRTCClient.closeConnection()
            changeMyStatus(UserStatus.ONLINE)
        }

    }

    fun sendEndCall() {
        if (TYPE_OF_MODE.RUN_UVC_MODE == "1") {
            onHsTransferEventToSocket(
                DataModel(
                    type = EndCall,
                    target = target!!
                )
            )
        } else {
            onTransferEventToSocket(
                DataModel(
                    type = EndCall,
                    target = target!!
                )
            )
        }
    }

    private fun changeMyStatus(status: UserStatus) {
        firebaseClient.changeMyStatus(status)
    }

    fun toggleAudio(shouldBeMuted: Boolean) {
        webRTCClient.toggleAudio(shouldBeMuted)
    }

    fun toggleVideo(shouldBeMuted: Boolean) {
        webRTCClient.toggleVideo(shouldBeMuted)
    }

    fun switchCamera() {
        webRTCClient.switchCamera()
    }

    override fun onTransferEventToSocket(data: DataModel) {
        firebaseClient.sendMessageToOtherClient(data) {}
    }

    override fun onHsTransferEventToSocket(data: DataModel) {
        Log.d(TAG, "onHsTransferEventToSocket: data => $data")
        firebaseClient.sendMessageToOtherClient(data) {}
    }

    fun setScreenCaptureIntent(screenPermissionIntent: Intent) {
        webRTCClient.setPermissionIntent(screenPermissionIntent)
    }

    fun toggleScreenShare(isStarting: Boolean) {
        if (isStarting) {
            Log.d(TAG, "toggleScreenShare: If")
            webRTCClient.startScreenCapturing()
        } else {
            Log.d(TAG, "toggleScreenShare: Else")
            webRTCClient.stopScreenCapturing()
        }
    }

    fun logOff(function: () -> Unit) = firebaseClient.logOff(function)
    interface Listener {
        fun onLatestEventReceived(data: DataModel)
        fun endCall()
    }


}