package eu.siacs.conversations.ui;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import eu.siacs.conversations.AppSettings;
import eu.siacs.conversations.ui.util.SettingsUtils;

public abstract class BaseActivity extends AppCompatActivity {
    private Boolean isDynamicColors;
    private String appliedColorPalette;
    private String appliedMessageFont;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        final var appSettings = new AppSettings(this);
        this.appliedColorPalette = appSettings.getColorPalette();
        this.appliedMessageFont = appSettings.getMessageFont();
        if (!appSettings.isDynamicColorsDesired()) {
            getTheme().applyStyle(appSettings.getColorPaletteStyle(), true);
        }
        getTheme().applyStyle(appSettings.getFontStyle(), true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        final var appSettings = new AppSettings(this);
        if (!this.appliedColorPalette.equals(appSettings.getColorPalette())
                || !this.appliedMessageFont.equals(appSettings.getMessageFont())) {
            recreate();
            return;
        }
        final int desiredNightMode = appSettings.getDesiredNightMode();
        if (setDesiredNightMode(desiredNightMode)) {
            return;
        }
        final boolean isDynamicColors = appSettings.isDynamicColorsDesired();
        setDynamicColors(isDynamicColors);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SettingsUtils.applyScreenshotSetting(this);
    }

    public void setDynamicColors(final boolean isDynamicColors) {
        if (this.isDynamicColors == null) {
            this.isDynamicColors = isDynamicColors;
        } else {
            if (this.isDynamicColors != isDynamicColors) {
                Log.i(
                        "Recreating {} because dynamic color setting has changed",
                        getClass().getSimpleName());
                recreate();
            }
        }
    }

    public boolean setDesiredNightMode(final int desiredNightMode) {
        if (desiredNightMode == AppCompatDelegate.getDefaultNightMode()) {
            return false;
        }
        AppCompatDelegate.setDefaultNightMode(desiredNightMode);
        Log.i("Recreating {} because desired night mode has changed", getClass().getSimpleName());
        recreate();
        return true;
    }
}
