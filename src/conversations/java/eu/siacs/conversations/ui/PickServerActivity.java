package eu.siacs.conversations.ui;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import eu.siacs.conversations.Config;
import eu.siacs.conversations.R;
import eu.siacs.conversations.databinding.ActivityPickServerBinding;

public class PickServerActivity extends XmppActivity {

    private final OnBackPressedCallback onBackPressedCallback =
            new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    startActivity(new Intent(PickServerActivity.this, WelcomeActivity.class));
                    setEnabled(false);
                    try {
                        PickServerActivity.this.getOnBackPressedDispatcher().onBackPressed();
                    } finally {
                        setEnabled(true);
                    }
                }
            };

    @Override
    protected void refreshUiReal() {}

    @Override
    protected void onBackendConnected() {}

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            startActivity(new Intent(this, WelcomeActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        if (intent != null) {
            setIntent(intent);
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        if (getResources().getBoolean(R.bool.portrait_only)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        super.onCreate(savedInstanceState);
        getOnBackPressedDispatcher().addCallback(this, this.onBackPressedCallback);
        if (Config.DISALLOW_REGISTRATION_IN_UI) {
            WelcomeActivity.launch(this);
            finish();
            return;
        }
        ActivityPickServerBinding binding =
                DataBindingUtil.setContentView(this, R.layout.activity_pick_server);
        Activities.setStatusAndNavigationBarColors(this, binding.getRoot());
        setSupportActionBar(binding.toolbar);
        configureActionBar(getSupportActionBar());
        binding.useCim.setOnClickListener(v -> WelcomeActivity.launch(this));
        binding.useOwnProvider.setOnClickListener(v -> WelcomeActivity.launch(this));
    }

    public void addInviteUri(final Intent intent) {
        StartConversationActivity.addInviteUri(intent, this);
    }

    public static void launch(final AppCompatActivity activity) {
        WelcomeActivity.launch(activity);
    }
}
