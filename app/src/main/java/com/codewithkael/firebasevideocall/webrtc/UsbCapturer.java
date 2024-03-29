package com.codewithkael.firebasevideocall.webrtc;

import static com.serenegiant.usb.UVCCamera.DEFAULT_PREVIEW_FPS;
import static com.serenegiant.usb.UVCCamera.DEFAULT_PREVIEW_FRAME_FORMAT;
import static com.serenegiant.usb.UVCCamera.DEFAULT_PREVIEW_HEIGHT;
import static com.serenegiant.usb.UVCCamera.DEFAULT_PREVIEW_WIDTH;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.herohan.uvcapp.CameraHelper;
import com.herohan.uvcapp.ICameraHelper;
import com.serenegiant.usb.IFrameCallback;
import com.serenegiant.usb.Size;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;
import com.serenegiant.usb.UVCParam;

import org.webrtc.CapturerObserver;
import org.webrtc.NV21Buffer;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoFrame;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class UsbCapturer implements VideoCapturer, IFrameCallback, USBMonitor.OnDeviceConnectListener {
    private static final String TAG = "===>>>UsbCapturer";
    private Context context;
    private USBMonitor monitor;
    private SurfaceViewRenderer svVideoRender;
    private SurfaceTextureHelper surfaceTextureHelper;
    private CapturerObserver capturerObserver;
    private Executor executor = Executors.newSingleThreadExecutor();
    ICameraHelper mCameraHelper;
    private USBMonitor.OnDeviceConnectListener onDeviceConnectListener;
    private ICameraHelper.StateCallback stateCallback = new ICameraHelper.StateCallback() {
        @Override
        public void onAttach(UsbDevice device) {
            Log.d(TAG, "onAttach: *********************************-1");
        }

        @Override
        public void onDeviceOpen(UsbDevice device, boolean isFirstOpen) {
            Log.d(TAG, "onDeviceOpen: *********************************-1");
        }

        @Override
        public void onCameraOpen(UsbDevice device) {
            Log.d(TAG, "onCameraOpen: *********************************-1");
        }

        @Override
        public void onCameraClose(UsbDevice device) {
            Log.d(TAG, "onCameraClose: *********************************-1");
        }

        @Override
        public void onDeviceClose(UsbDevice device) {
            Log.d(TAG, "onDeviceClose: *********************************-1");
        }

        @Override
        public void onDetach(UsbDevice device) {
            Log.d(TAG, "onDetach: *********************************-1");
        }

        @Override
        public void onCancel(UsbDevice device) {
            Log.d(TAG, "onCancel: *********************************-1");
        }
    };

    public UsbCapturer(Context context, SurfaceViewRenderer svVideoRender, ICameraHelper mCameraHelper) {
        this.context = context;
        this.svVideoRender = svVideoRender;
        this.mCameraHelper = mCameraHelper;
        executor.execute(new Runnable() {
            @Override
            public void run() {
                monitor = new USBMonitor(context, ContextCompat.RECEIVER_EXPORTED, UsbCapturer.this);
                monitor.register();

            }
        });
    }

    @Override
    public void initialize(SurfaceTextureHelper surfaceTextureHelper, Context context, CapturerObserver capturerObserver) {
        this.surfaceTextureHelper = surfaceTextureHelper;
        this.capturerObserver = capturerObserver;
    }

    @Override
    public void startCapture(int i, int i1, int i2) {
        //  camera.startPreview();
        Log.d(TAG, "startCapture: ");

    }

    @Override
    public void stopCapture() throws InterruptedException {
        Log.d(TAG, "stopCapture: ");
        if (camera != null) {
            camera.stopPreview();
            camera.close();
            camera.destroy();
        }
    }

    @Override
    public void changeCaptureFormat(int i, int i1, int i2) {

    }

    @Override
    public void dispose() {
        monitor.unregister();
        monitor.destroy();
     //   surfaceTextureHelper.dispose();

    }

    @Override
    public boolean isScreencast() {
        return false;
    }

    UVCCamera camera;

    @Override
    public void onFrame(ByteBuffer frame) {
        Log.d(TAG, "onFrame: ========>1");
        executor.execute(new Runnable() {
            @Override
            public void run() {
                byte[] imageArray = new byte[frame.remaining()];
                frame.get(imageArray);
                long imageTime = System.currentTimeMillis();

                NV21Buffer nV21Buffer =
                        new NV21Buffer(imageArray, 200, 300, null);
                VideoFrame videoFrame = new VideoFrame(nV21Buffer, 0, imageTime);
                capturerObserver.onFrameCaptured(videoFrame);
            }
        });
    }


    @Override
    public void onAttach(UsbDevice device) {
        Log.d(TAG, "OnDeviceConnectListener => onAttach: statusDAttach= " + statusDAttach);
        if (!statusDAttach) {
            statusDAttach = true;
            monitor.requestPermission(device);
        }
    }

    @Override
    public void onDetach(UsbDevice device) {
        Log.d(TAG, "OnDeviceConnectListener => onDetach: ");
    }

    private boolean statusDOpen = false;
    private boolean statusDAttach = false;
    UVCParam uvcParam = new UVCParam();

    @Override
    public void onDeviceOpen(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock, boolean createNew) {
        Log.d(TAG, "OnDeviceConnectListener => onDeviceOpen: statusDOpen =" + statusDOpen);
        if (!statusDOpen) {
            statusDOpen = true;
          /*  DisplayMetrics displayMetrics = new DisplayMetrics();
            WindowManager windowsManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            windowsManager.getDefaultDisplay().getMetrics(displayMetrics);

            int screenWidthPixels = displayMetrics.widthPixels;
            int screenHeightPixels = displayMetrics.heightPixels;

            Size size = new Size(UVCCamera.UVC_VS_FRAME_MJPEG, screenWidthPixels, screenHeightPixels, 30, new ArrayList<>(DEFAULT_PREVIEW_FPS));

            // Set the custom preview size
            uvcParam.setPreviewSize(size);
            camera = new UVCCamera(uvcParam);
            camera.open(ctrlBlock);


            Log.d(TAG, "onDeviceOpen: " + camera.getSupportedSizeOne());
            //  camera.setPreviewSize(camera.getPreviewSize().width, camera.getPreviewSize().height, UVCCamera.UVC_VS_FRAME_MJPEG);


            camera.setFrameCallback(new IFrameCallback() {
                @Override
                public void onFrame(ByteBuffer frame) {
                    Log.d(TAG, "onFrame: ============>2");
                }
            }, UVCCamera.PIXEL_FORMAT_NV21);
            camera.setPreviewDisplay(svVideoRender.getHolder().getSurface());
            camera.startPreview();
*/
            mCameraHelper.openCamera();
            mCameraHelper.setPreviewSize(new Size(
                    DEFAULT_PREVIEW_FRAME_FORMAT,
                    DEFAULT_PREVIEW_WIDTH,
                    DEFAULT_PREVIEW_HEIGHT,
                    DEFAULT_PREVIEW_FPS,
                    new ArrayList<>(DEFAULT_PREVIEW_FPS)));


            mCameraHelper.addSurface(svVideoRender.getHolder(), false);


            mCameraHelper.setFrameCallback(UsbCapturer.this, UVCCamera.PIXEL_FORMAT_NV21);

            svVideoRender.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(@NonNull SurfaceHolder holder) {
                    Log.d(TAG, "surfaceCreated() called with: holder = [" + holder + "]");
                    mCameraHelper.addSurface(svVideoRender.getHolder(), false);

                }

                @Override
                public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
                    Log.d(TAG, "surfaceChanged() called with: holder = [" + holder + "], format = [" + format + "], width = [" + width + "], height = [" + height + "]");
                }

                @Override
                public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                    Log.d(TAG, "surfaceDestroyed: ");
                    mCameraHelper.removeSurface(holder.getSurface());
                }
            });

            mCameraHelper.startPreview();

        }

    }

    @Override
    public void onDeviceClose(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock) {
        Log.d(TAG, "OnDeviceConnectListener => onDeviceClose: ");
    }

    @Override
    public void onCancel(UsbDevice device) {
        Log.d(TAG, "OnDeviceConnectListener => onCancel: ");
    }

    @Override
    public void onError(UsbDevice device, USBMonitor.USBException e) {
        USBMonitor.OnDeviceConnectListener.super.onError(device, e);
        Log.d(TAG, "OnDeviceConnectListener => onError: ");
    }
}


/*
 * supportedSize:{"formats":[{"index":1,"subType":6,"defaultFrameIndex":1,
 * "frameDescriptors":[
 * {"width":2592,"height":1944,"subType":7,"defaultFrameInterval":666666,"frameIntervalType":6,"defaultFps":15,
 * "intervals":[{"index":0,"value":666666,"fps":15},{"index":1,"value":1000000,"fps":10},{"index":2,"value":1333333,"fps":7},
 * {"index":3,"value":2000000,"fps":5},{"index":4,"value":2666666,"fps":3},{"index":5,"value":4000000,"fps":2}]},
 * {"width":2048,"height":1536,"subType":7,"defaultFrameInterval":666666,"frameIntervalType":6,"defaultFps":15,
 * "intervals":[{"index":0,"value":666666,"fps":15},{"index":1,"value":1000000,"fps":10},{"index":2,"value":1333333,"fps":7},
 * {"index":3,"value":2000000,"fps":5},{"index":4,"value":2666666,"fps":3},{"index":5,"value":4000000,"fps":2}]},
 * {"width":1600,"height":1200,"subType":7,"defaultFrameInterval":666666,"frameIntervalType":6,"defaultFps":15,
 * "intervals":[{"index":0,"value":666666,"fps":15},{"index":1,"value":1000000,"fps":10},{"index":2,"value":1333333,"fps":7},
 * {"index":3,"value":2000000,"fps":5},{"index":4,"value":2666666,"fps":3},{"index":5,"value":4000000,"fps":2}]},
 * {"width":1920,"height":1080,"subType":7,"defaultFrameInterval":333333,"frameIntervalType":8,"defaultFps":30,
 * "intervals":[{"index":0,"value":333333,"fps":30},{"index":1,"value":500000,"fps":20},{"index":2,"value":666666,"fps":15},
 * {"index":3,"value":1000000,"fps":10},{"index":4,"value":1333333,"fps":7},{"index":5,"value":2000000,"fps":5},
 * {"index":6,"value":2666666,"fps":3},{"index":7,"value":4000000,"fps":2}]},
 * {"width":1280,"height":960,"subType":7,"defaultFrameInterval":333333,"frameIntervalType":8,"defaultFps":30,
 * "intervals":[{"index":0,"value":333333,"fps":30},{"index":1,"value":500000,"fps":20},{"index":2,"value":666666,"fps":15},
 * {"index":3,"value":1000000,"fps":10},{"index":4,"value":1333333,"fps":7},{"index":5,"value":2000000,"fps":5},
 * {"index":6,"value":2666666,"fps":3},{"index":7,"value":4000000,"fps":2}]},
 * {"width":1280,"height":720,"subType":7,"defaultFrameInterval":333333,"frameIntervalType":8,"defaultFps":30,
 * "intervals":[{"index":0,"value":333333,"fps":30},{"index":1,"value":500000,"fps":20},{"index":2,"value":666666,"fps":15},
 * {"index":3,"value":1000000,"fps":10},{"index":4,"value":1333333,"fps":7},{"index":5,"value":2000000,"fps":5},
 * {"index":6,"value":2666666,"fps":3},{"index":7,"value":4000000,"fps":2}]},
 * {"width":1024,"height":768,"subType":7,"defaultFrameInterval":333333,"frameIntervalType":8,"defaultFps":30,
 * "intervals":[{"index":0,"value":333333,"fps":30},{"index":1,"value":500000,"fps":20},{"index":2,"value":666666,"fps":15},
 * {"index":3,"value":1000000,"fps":10},{"index":4,"value":1333333,"fps":7},{"index":5,"value":2000000,"fps":5},
 * {"index":6,"value":2666666,"fps":3},{"index":7,"value":4000000,"fps":2}]},
 * {"width":960,"height":720,"subType":7,"defaultFrameInterval":333333,"frameIntervalType":8,"defaultFps":30,
 * "intervals":[{"index":0,"value":333333,"fps":30},{"index":1,"value":500000,"fps":20},{"index":2,"value":666666,"fps":15},
 * {"index":3,"value":1000000,"fps":10},{"index":4,"value":1333333,"fps":7},{"index":5,"value":2000000,"fps":5},
 * {"index":6,"value":2666666,"fps":3},{"index":7,"value":4000000,"fps":2}]},
 * {"width":800,"height":600,"subType":7,"defaultFrameInterval":333333,"frameIntervalType":8,"defaultFps":30,
 * "intervals":[{"index":0,"value":333333,"fps":30},{"index":1,"value":500000,"fps":20},{"index":2,"value":666666,"fps":15},{"index":3,"value":1000000,"fps":10},{"index":4,"value":1333333,"fps":7},{"index":5,"value":2000000,"fps":5},{"index":6,"value":2666666,"fps":3},{"index":7,"value":4000000,"fps":2}]},{"width":640,"height":480,"subType":7,"defaultFrameInterval":333333,"frameIntervalType":8,"defaultFps":30,"intervals":[{"index":0,"value":333333,"fps":30},{"index":1,"value":500000,"fps":20},{"index":2,"value":666666,"fps":15},{"index":3,"value":1000000,"fps":10},{"index":4,"value":1333333,"fps":7},{"index":5,"value":2000000,"fps":5},
 * {"index":6,"value":2666666,"fps":3},{"index":7,"value":4000000,"fps":2}]},{"width":320,"height":240,"subType":7,"defaultFrameInterval":3333
 *
 *
 * */