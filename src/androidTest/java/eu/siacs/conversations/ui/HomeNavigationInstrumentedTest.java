package eu.siacs.conversations.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import eu.siacs.conversations.R;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class HomeNavigationInstrumentedTest {

    private Activity activity;

    @After
    public void finishActivity() {
        if (activity != null) {
            activity.runOnUiThread(activity::finish);
        }
    }

    @Test
    public void contactsGroupsCallsAndLogoUseTheSharedNavigation() {
        final Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        final Intent intent =
                new Intent(instrumentation.getTargetContext(), StartConversationActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity = instrumentation.startActivitySync(intent);
        instrumentation.waitForIdleSync();

        final BottomNavigationView navigation = activity.findViewById(R.id.home_navigation);
        final ViewPager pager = activity.findViewById(R.id.start_conversation_view_pager);
        assertNotNull(navigation);
        assertNotNull(pager);
        assertEquals(View.VISIBLE, navigation.getVisibility());
        assertEquals(4, navigation.getMenu().size());
        assertNotNull(navigation.getMenu().findItem(R.id.nav_calls));
        assertEquals(R.id.nav_contacts, navigation.getSelectedItemId());
        assertEquals(0, pager.getCurrentItem());

        instrumentation.runOnMainSync(() -> navigation.setSelectedItemId(R.id.nav_groups));
        instrumentation.waitForIdleSync();
        assertEquals(R.id.nav_groups, navigation.getSelectedItemId());
        assertEquals(1, pager.getCurrentItem());

        instrumentation.runOnMainSync(() -> navigation.setSelectedItemId(R.id.nav_contacts));
        instrumentation.waitForIdleSync();
        assertEquals(R.id.nav_contacts, navigation.getSelectedItemId());
        assertEquals(0, pager.getCurrentItem());

        final AtomicReference<View> home = new AtomicReference<>();
        instrumentation.runOnMainSync(
                () ->
                        home.set(
                                activity.getLayoutInflater()
                                        .inflate(
                                                R.layout.fragment_conversations_overview,
                                                null,
                                                false)));
        final ImageView logo = home.get().findViewById(R.id.brand_wordmark);
        final BottomNavigationView homeNavigation = home.get().findViewById(R.id.home_navigation);
        assertNotNull(logo);
        assertNotNull(homeNavigation);
        assertEquals(ImageView.ScaleType.FIT_CENTER, logo.getScaleType());
        assertTrue(
                logo.getDrawable().getIntrinsicWidth() > logo.getDrawable().getIntrinsicHeight());
        assertNotNull(homeNavigation.getMenu().findItem(R.id.nav_calls));
    }
}
