package eu.siacs.conversations.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.view.View;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import eu.siacs.conversations.R;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class LinkedDevicesInstrumentedTest {

    private Activity activity;

    @After
    public void finishActivity() {
        if (activity != null) {
            activity.runOnUiThread(activity::finish);
        }
    }

    @Test
    public void linkedDevicesUsesTheRealInteractiveScreen() {
        final Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        final Intent intent =
                new Intent(instrumentation.getTargetContext(), LinkedDevicesActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity = instrumentation.startActivitySync(intent);
        instrumentation.waitForIdleSync();

        assertNotNull(activity.findViewById(R.id.toolbar));
        assertNotNull(activity.findViewById(R.id.devices));
        assertNotNull(activity.findViewById(R.id.progress));
        assertNotNull(activity.findViewById(R.id.status));
        assertNotNull(activity.findViewById(R.id.retry));
        assertNotNull(activity.findViewById(R.id.account));
        final View associate = activity.findViewById(R.id.associate);
        assertNotNull(associate);
        assertEquals(View.VISIBLE, associate.getVisibility());
        assertTrue(associate.isClickable());
    }
}
