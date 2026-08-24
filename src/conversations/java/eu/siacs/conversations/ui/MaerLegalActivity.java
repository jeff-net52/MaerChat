package eu.siacs.conversations.ui;

import android.os.Bundle;
import androidx.annotation.RawRes;
import androidx.annotation.StringRes;
import androidx.databinding.DataBindingUtil;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import eu.siacs.conversations.BuildConfig;
import eu.siacs.conversations.R;
import eu.siacs.conversations.databinding.ActivityMaerLegalBinding;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class MaerLegalActivity extends ActionBarActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivityMaerLegalBinding binding =
                DataBindingUtil.setContentView(this, R.layout.activity_maer_legal);
        Activities.setStatusAndNavigationBarColors(this, binding.getRoot());
        setSupportActionBar(binding.toolbar);
        configureActionBar(getSupportActionBar());
        setTitle(R.string.legal_information);

        binding.legalVersion.setText(
                getString(R.string.maer_legal_version, BuildConfig.VERSION_NAME));
        binding.fullLicense.setOnClickListener(
                view -> showDocument(R.string.maer_show_gpl, R.raw.gpl_v3));
        binding.thirdPartyNotices.setOnClickListener(
                view ->
                        showDocument(
                                R.string.maer_third_party_notice_title, R.raw.third_party_notices));
        binding.apacheLicense.setOnClickListener(
                view -> showDocument(R.string.maer_apache_license_title, R.raw.apache_2_0));
        binding.lgplLicense.setOnClickListener(
                view -> showDocument(R.string.maer_lgpl_license_title, R.raw.lgpl_v2_1));
        binding.permissiveNotices.setOnClickListener(
                view ->
                        showDocument(
                                R.string.maer_permissive_notices_title,
                                R.raw.permissive_license_notices));
        binding.releaseNotes.setOnClickListener(
                view -> showDocument(R.string.maer_release_notes_title, R.raw.release_notes));
    }

    private void showDocument(@StringRes final int title, @RawRes final int resource) {
        final String document;
        try {
            document = readRawText(resource);
        } catch (final IOException e) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(title)
                    .setMessage(R.string.maer_document_unavailable)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
            return;
        }
        new MaterialAlertDialogBuilder(this)
                .setTitle(title)
                .setMessage(document)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private String readRawText(@RawRes final int resource) throws IOException {
        try (final var reader =
                new BufferedReader(
                        new InputStreamReader(
                                getResources().openRawResource(resource),
                                StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
}
