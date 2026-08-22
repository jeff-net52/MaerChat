package eu.siacs.conversations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.res.Configuration;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.ConscryptMode;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 35)
@ConscryptMode(ConscryptMode.Mode.OFF)
public class ThemeConfigurationTest {

    @Test
    public void applicationThemeFollowsSystemDayAndNightConfiguration() {
        final Context day =
                configuredContext(
                        Configuration.UI_MODE_NIGHT_NO, Configuration.ORIENTATION_PORTRAIT);
        final Context night =
                configuredContext(
                        Configuration.UI_MODE_NIGHT_YES, Configuration.ORIENTATION_PORTRAIT);

        final int dayPrimary = themedColor(day, androidx.appcompat.R.attr.colorPrimary);
        final int nightPrimary = themedColor(night, androidx.appcompat.R.attr.colorPrimary);

        assertEquals(day.getColor(R.color.md_theme_light_primary), dayPrimary);
        assertEquals(night.getColor(R.color.md_theme_dark_primary), nightPrimary);
        assertNotEquals(dayPrimary, nightPrimary);
    }

    @Test
    public void themeResourcesResolveAfterOrientationAndSizeChange() {
        final Context landscape =
                configuredContext(
                        Configuration.UI_MODE_NIGHT_NO, Configuration.ORIENTATION_LANDSCAPE);

        assertEquals(BuildConfig.APP_NAME, landscape.getString(R.string.app_name));
        assertEquals(
                landscape.getColor(R.color.md_theme_light_primary),
                themedColor(landscape, androidx.appcompat.R.attr.colorPrimary));
    }

    private static Context configuredContext(final int nightMode, final int orientation) {
        final Context application = RuntimeEnvironment.getApplication();
        final Configuration configuration =
                new Configuration(application.getResources().getConfiguration());
        configuration.uiMode =
                (configuration.uiMode & ~Configuration.UI_MODE_NIGHT_MASK) | nightMode;
        configuration.orientation = orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            configuration.screenWidthDp = 800;
            configuration.screenHeightDp = 480;
        }
        return application.createConfigurationContext(configuration);
    }

    private static int themedColor(final Context context, final int attribute) {
        final ContextThemeWrapper themed =
                new ContextThemeWrapper(context, R.style.Theme_Conversations3);
        final TypedValue value = new TypedValue();
        assertTrue(themed.getTheme().resolveAttribute(attribute, value, true));
        return value.resourceId == 0 ? value.data : themed.getColor(value.resourceId);
    }
}
