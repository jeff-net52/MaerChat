package eu.siacs.conversations.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.os.Looper;
import android.view.View;
import com.google.android.material.button.MaterialButton;
import eu.siacs.conversations.R;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.ConscryptMode;
import org.robolectric.annotation.LooperMode;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 35)
@ConscryptMode(ConscryptMode.Mode.OFF)
@LooperMode(LooperMode.Mode.PAUSED)
public class ScanQrCodeActivityTest {

    @Test
    public void cameraFailureShowsAUserFacingRetryAction() {
        final var controller =
                Robolectric.buildActivity(ScanQrCodeActivity.class).create().start().resume();
        final ScanQrCodeActivity activity = controller.get();

        activity.showCameraError();
        Shadows.shadowOf(Looper.getMainLooper()).idle();

        final View error = activity.findViewById(R.id.scan_activity_camera_error);
        final View scanner = activity.findViewById(R.id.scan_activity_mask);
        final MaterialButton retry = activity.findViewById(R.id.scan_activity_retry_camera);
        assertEquals(View.VISIBLE, error.getVisibility());
        assertEquals(View.GONE, scanner.getVisibility());
        assertEquals(activity.getString(R.string.retry_camera), retry.getText().toString());
        assertTrue(retry.isEnabled());

        retry.performClick();
        Shadows.shadowOf(Looper.getMainLooper()).idle();
        assertEquals(View.VISIBLE, error.getVisibility());

        controller.pause().stop().destroy();
    }
}
