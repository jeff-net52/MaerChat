package eu.siacs.conversations.ui.fragment.settings;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragmentCompat;
import com.google.common.base.Strings;
import eu.siacs.conversations.BuildConfig;
import eu.siacs.conversations.R;
import eu.siacs.conversations.ui.LinkedDevicesActivity;
import eu.siacs.conversations.utils.AccountUtils;

public class MainSettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.preferences_main, rootKey);
        final var accountAndProfile = findPreference("account_and_profile");
        final var about = findPreference("about");
        final var connection = findPreference("connection");
        final var linkedDevices = findPreference("linked_devices");
        final var up = findPreference("up");
        if (accountAndProfile == null
                || about == null
                || connection == null
                || linkedDevices == null
                || up == null) {
            throw new IllegalStateException(
                    "The preference resource file is missing some preferences");
        }
        accountAndProfile.setOnPreferenceClickListener(
                preference -> {
                    AccountUtils.launchManageAccounts(requireActivity());
                    return true;
                });
        linkedDevices.setOnPreferenceClickListener(
                preference -> {
                    startActivity(new Intent(requireContext(), LinkedDevicesActivity.class));
                    return true;
                });
        about.setTitle(getString(R.string.title_activity_about_x, BuildConfig.APP_NAME));
        about.setSummary(
                String.format(
                        "%s %s %s @ %s · %s · %s",
                        BuildConfig.APP_NAME,
                        BuildConfig.VERSION_NAME,
                        im.conversations.webrtc.BuildConfig.WEBRTC_VERSION,
                        Strings.nullToEmpty(Build.MANUFACTURER),
                        Strings.nullToEmpty(Build.DEVICE),
                        Strings.nullToEmpty(Build.VERSION.RELEASE)));
        if (ConnectionSettingsFragment.hideChannelDiscovery()) {
            connection.setSummary(R.string.pref_connection_summary);
        }
        up.setVisible(!Strings.isNullOrEmpty(getString(R.string.default_push_server)));
    }

    @Override
    public void onStart() {
        super.onStart();
        requireActivity().setTitle(R.string.title_activity_settings);
    }
}
