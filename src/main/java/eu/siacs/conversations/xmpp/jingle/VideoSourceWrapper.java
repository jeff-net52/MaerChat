package eu.siacs.conversations.xmpp.jingle;

import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import androidx.annotation.Nullable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import eu.siacs.conversations.Config;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerationAndroid;
import org.webrtc.CameraEnumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.EglBase;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.ScreenCapturerAndroid;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoSource;

class VideoSourceWrapper {

    private static final int CAPTURING_RESOLUTION = 1920;
    private static final int CAPTURING_MAX_FRAME_RATE = 30;

    private VideoCapturer videoCapturer;
    private CameraEnumerationAndroid.CaptureFormat captureFormat;
    private final Set<String> availableCameras;
    private boolean isFrontCamera = false;
    private boolean screenCapture;
    private VideoSource videoSource;
    private SurfaceTextureHelper surfaceTextureHelper;
    private Context context;
    private EglBase.Context eglBaseContext;

    VideoSourceWrapper(
            CameraVideoCapturer cameraVideoCapturer,
            CameraEnumerationAndroid.CaptureFormat captureFormat,
            Set<String> cameras) {
        this.videoCapturer = cameraVideoCapturer;
        this.captureFormat = captureFormat;
        this.availableCameras = cameras;
    }

    private int getFrameRate() {
        return getFrameRate(captureFormat);
    }

    private static int getFrameRate(final CameraEnumerationAndroid.CaptureFormat captureFormat) {
        return Math.max(
                captureFormat.framerate.min,
                Math.min(CAPTURING_MAX_FRAME_RATE, captureFormat.framerate.max));
    }

    public void initialize(
            final PeerConnectionFactory peerConnectionFactory,
            final Context context,
            final EglBase.Context eglBaseContext) {
        final SurfaceTextureHelper surfaceTextureHelper =
                SurfaceTextureHelper.create("webrtc", eglBaseContext);
        if (surfaceTextureHelper == null) {
            throw new IllegalStateException("Could not create SurfaceTextureHelper");
        }
        this.context = context;
        this.eglBaseContext = eglBaseContext;
        this.surfaceTextureHelper = surfaceTextureHelper;
        this.videoSource = peerConnectionFactory.createVideoSource(false);
        this.videoCapturer.initialize(
                surfaceTextureHelper, context, this.videoSource.getCapturerObserver());
    }

    public VideoSource getVideoSource() {
        final VideoSource videoSource = this.videoSource;
        if (videoSource == null) {
            throw new IllegalStateException("VideoSourceWrapper was not initialized");
        }
        return videoSource;
    }

    public void startCapture() {
        final int frameRate = getFrameRate();
        Log.d(
                Config.LOGTAG,
                String.format(
                        "start capturing at %dx%d@%d",
                        captureFormat.width, captureFormat.height, frameRate));
        this.videoCapturer.startCapture(captureFormat.width, captureFormat.height, frameRate);
    }

    public void stopCapture() throws InterruptedException {
        this.videoCapturer.stopCapture();
    }

    public void dispose() {
        this.videoCapturer.dispose();
        if (this.surfaceTextureHelper != null) {
            this.surfaceTextureHelper.dispose();
            this.surfaceTextureHelper = null;
        }
        if (this.videoSource != null) {
            dispose(this.videoSource);
        }
    }

    private static void dispose(final VideoSource videoSource) {
        try {
            videoSource.dispose();
        } catch (final IllegalStateException e) {
            Log.e(Config.LOGTAG, "unable to dispose video source", e);
        }
    }

    public ListenableFuture<Boolean> switchCamera() {
        if (screenCapture || !(videoCapturer instanceof CameraVideoCapturer cameraVideoCapturer)) {
            return Futures.immediateFailedFuture(
                    new IllegalStateException("Camera is unavailable during screen sharing"));
        }
        final SettableFuture<Boolean> future = SettableFuture.create();
        cameraVideoCapturer.switchCamera(
                new CameraVideoCapturer.CameraSwitchHandler() {
                    @Override
                    public void onCameraSwitchDone(final boolean isFrontCamera) {
                        VideoSourceWrapper.this.isFrontCamera = isFrontCamera;
                        future.set(isFrontCamera);
                    }

                    @Override
                    public void onCameraSwitchError(final String message) {
                        future.setException(
                                new IllegalStateException(
                                        String.format("Unable to switch camera %s", message)));
                    }
                });
        return future;
    }

    public boolean isFrontCamera() {
        return this.isFrontCamera;
    }

    public boolean isCameraSwitchable() {
        return !screenCapture && this.availableCameras.size() > 1;
    }

    public boolean isScreenCapture() {
        return screenCapture;
    }

    public synchronized void switchToScreenCapture(
            final Intent permissionData, final MediaProjection.Callback callback)
            throws InterruptedException {
        replaceCapturer(
                new ScreenCapturerAndroid(permissionData, callback), screenCaptureFormat(), true);
    }

    public synchronized void switchToCameraCapture() throws InterruptedException {
        final VideoSourceWrapper camera = new Factory(requireContext()).create();
        if (camera == null) {
            throw new IllegalStateException("No camera is available");
        }
        final VideoCapturer capturer = camera.videoCapturer;
        camera.videoCapturer = null;
        replaceCapturer(capturer, camera.captureFormat, false);
        this.isFrontCamera = camera.isFrontCamera;
    }

    private void replaceCapturer(
            final VideoCapturer replacement,
            final CameraEnumerationAndroid.CaptureFormat replacementFormat,
            final boolean screen)
            throws InterruptedException {
        if (replacement == null || videoSource == null) {
            throw new IllegalStateException("Video source is not initialized");
        }
        final SurfaceTextureHelper replacementHelper =
                SurfaceTextureHelper.create(
                        screen ? "webrtc-screen" : "webrtc-camera", eglBaseContext);
        if (replacementHelper == null) {
            replacement.dispose();
            throw new IllegalStateException("Could not create SurfaceTextureHelper");
        }
        final VideoCapturer currentCapturer = videoCapturer;
        final CameraEnumerationAndroid.CaptureFormat currentFormat = captureFormat;
        final SurfaceTextureHelper currentHelper = surfaceTextureHelper;
        final CapturerSwapTransaction.Participant current =
                participant(currentCapturer, currentFormat, currentHelper, false);
        final CapturerSwapTransaction.Participant candidate =
                participant(replacement, replacementFormat, replacementHelper, true);
        CapturerSwapTransaction.execute(
                current,
                candidate,
                () -> {
                    videoCapturer = replacement;
                    captureFormat = replacementFormat;
                    screenCapture = screen;
                    surfaceTextureHelper = replacementHelper;
                });
    }

    private CapturerSwapTransaction.Participant participant(
            final VideoCapturer capturer,
            final CameraEnumerationAndroid.CaptureFormat format,
            final SurfaceTextureHelper helper,
            final boolean initialize) {
        return new CapturerSwapTransaction.Participant() {
            @Override
            public void start() {
                if (initialize) {
                    capturer.initialize(
                            helper, requireContext(), videoSource.getCapturerObserver());
                }
                capturer.startCapture(format.width, format.height, getFrameRate(format));
            }

            @Override
            public void stop() throws InterruptedException {
                try {
                    capturer.stopCapture();
                } catch (final IllegalStateException e) {
                    // MediaProjection can already be stopped from the system privacy indicator.
                    Log.d(Config.LOGTAG, "video capturer was already stopped", e);
                }
            }

            @Override
            public void dispose() {
                try {
                    capturer.dispose();
                } finally {
                    if (helper != null) {
                        helper.dispose();
                    }
                }
            }
        };
    }

    private CameraEnumerationAndroid.CaptureFormat screenCaptureFormat() {
        final DisplayMetrics metrics = new DisplayMetrics();
        requireContext()
                .getSystemService(WindowManager.class)
                .getDefaultDisplay()
                .getRealMetrics(metrics);
        final int largest = Math.max(metrics.widthPixels, metrics.heightPixels);
        final double scale =
                largest > CAPTURING_RESOLUTION ? (double) CAPTURING_RESOLUTION / largest : 1.0;
        final int width = Math.max(2, ((int) (metrics.widthPixels * scale)) & ~1);
        final int height = Math.max(2, ((int) (metrics.heightPixels * scale)) & ~1);
        return new CameraEnumerationAndroid.CaptureFormat(
                width, height, 0, CAPTURING_MAX_FRAME_RATE);
    }

    private Context requireContext() {
        if (context == null) {
            throw new IllegalStateException("Video source is not initialized");
        }
        return context;
    }

    public static class Factory {
        final Context context;

        public Factory(final Context context) {
            this.context = context;
        }

        public VideoSourceWrapper create() {
            final CameraEnumerator enumerator = new Camera2Enumerator(context);
            final Set<String> deviceNames = ImmutableSet.copyOf(enumerator.getDeviceNames());
            for (final String deviceName : deviceNames) {
                if (isFrontFacing(enumerator, deviceName)) {
                    final VideoSourceWrapper videoSourceWrapper =
                            of(enumerator, deviceName, deviceNames);
                    if (videoSourceWrapper == null) {
                        return null;
                    }
                    videoSourceWrapper.isFrontCamera = true;
                    return videoSourceWrapper;
                }
            }
            if (deviceNames.isEmpty()) {
                return null;
            } else {
                return of(enumerator, Iterables.get(deviceNames, 0), deviceNames);
            }
        }

        @Nullable
        private VideoSourceWrapper of(
                final CameraEnumerator enumerator,
                final String deviceName,
                final Set<String> availableCameras) {
            final CameraVideoCapturer capturer = enumerator.createCapturer(deviceName, null);
            if (capturer == null) {
                return null;
            }
            final ArrayList<CameraEnumerationAndroid.CaptureFormat> choices =
                    new ArrayList<>(enumerator.getSupportedFormats(deviceName));
            Collections.sort(choices, (a, b) -> b.width - a.width);
            for (final CameraEnumerationAndroid.CaptureFormat captureFormat : choices) {
                if (captureFormat.width <= CAPTURING_RESOLUTION) {
                    return new VideoSourceWrapper(capturer, captureFormat, availableCameras);
                }
            }
            return null;
        }

        private static boolean isFrontFacing(
                final CameraEnumerator cameraEnumerator, final String deviceName) {
            try {
                return cameraEnumerator.isFrontFacing(deviceName);
            } catch (final NullPointerException e) {
                return false;
            }
        }
    }
}
