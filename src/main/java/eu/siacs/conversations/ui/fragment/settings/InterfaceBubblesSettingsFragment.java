package eu.siacs.conversations.ui.fragment.settings;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.SwitchPreferenceCompat;
import eu.siacs.conversations.AppSettings;
import eu.siacs.conversations.R;

public class InterfaceBubblesSettingsFragment extends XmppPreferenceFragment {

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.preferences_interface_bubbles, rootKey);
        final var showAvatars11 = findPreference(AppSettings.SHOW_AVATARS_11);
        final var showAvatarsAccount = findPreference(AppSettings.SHOW_AVATARS_ACCOUNTS);
        final var avatarDisplay = findPreference(AppSettings.AVATAR_DISPLAY);
        final SwitchPreferenceCompat alignStart = findPreference(AppSettings.ALIGN_START);
        if (showAvatars11 == null
                || showAvatarsAccount == null
                || avatarDisplay == null
                || alignStart == null) {
            throw new IllegalStateException(
                    "The preference resource file is missing some preferences");
        }
        updateShowAvatars11Summary(showAvatars11, alignStart.isChecked());
        updateAvatarPreferences();
    }

    @Override
    protected void onSharedPreferenceChanged(@NonNull String key) {
        super.onSharedPreferenceChanged(key);
        switch (key) {
            case AppSettings.ALIGN_START -> runOnUiThread(this::updateShowAvatars11Summary);
            case AppSettings.AVATAR_DISPLAY -> runOnUiThread(this::updateAvatarPreferences);
            case AppSettings.SHOW_CONNECTION_OPTIONS -> reconnectAccounts();
        }
    }

    private void updateAvatarPreferences() {
        final var showAvatars11 = findPreference(AppSettings.SHOW_AVATARS_11);
        final var showAvatarsAccount = findPreference(AppSettings.SHOW_AVATARS_ACCOUNTS);
        if (showAvatars11 == null || showAvatarsAccount == null) {
            return;
        }
        final boolean enabled =
                new AppSettings(requireContext()).getAvatarDisplay()
                        != AppSettings.AvatarDisplay.NEVER;
        showAvatars11.setEnabled(enabled);
        showAvatarsAccount.setEnabled(enabled);
    }

    private void updateShowAvatars11Summary() {
        final var showAvatars11 = findPreference(AppSettings.SHOW_AVATARS_11);
        updateShowAvatars11Summary(showAvatars11);
    }

    private void updateShowAvatars11Summary(final Preference preference) {
        final var appSettings = new AppSettings(requireContext());
        updateShowAvatars11Summary(preference, appSettings.isAlignStart());
    }

    private void updateShowAvatars11Summary(final Preference preference, final boolean alignStart) {
        preference.setSummary(
                alignStart
                        ? R.string.pref_show_11_chats_summary
                        : R.string.pref_show_11_sender_chats_summary);
    }

    @Override
    public void onStart() {
        super.onStart();
        requireActivity().setTitle(R.string.pref_title_bubbles);
    }
}
