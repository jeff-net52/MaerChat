package eu.siacs.conversations.ui;

import static eu.siacs.conversations.utils.PermissionUtils.allGranted;
import static eu.siacs.conversations.utils.PermissionUtils.writeGranted;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.security.KeyChain;
import android.security.KeyChainAliasCallback;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import com.google.common.base.Strings;
import de.gultsch.common.MiniUri;
import eu.siacs.conversations.BuildConfig;
import eu.siacs.conversations.Config;
import eu.siacs.conversations.R;
import eu.siacs.conversations.databinding.ActivityWelcomeBinding;
import eu.siacs.conversations.entities.Account;
import eu.siacs.conversations.services.XmppConnectionService;
import eu.siacs.conversations.utils.AccountUtils;
import eu.siacs.conversations.utils.Compatibility;
import eu.siacs.conversations.utils.InstallReferrerUtils;
import eu.siacs.conversations.utils.LoginJid;
import eu.siacs.conversations.xmpp.Jid;
import java.util.Arrays;

public class WelcomeActivity extends QrCodeProcessingActivity
        implements XmppConnectionService.OnAccountCreated,
                XmppConnectionService.OnAccountUpdate,
                KeyChainAliasCallback {

    private static final int REQUEST_IMPORT_BACKUP = 0x63fb;
    private static final String STATE_ADVANCED_LOGIN = "advanced_login";

    private MiniUri.Xmpp inviteUri;
    private ActivityWelcomeBinding binding;
    private Account pendingAccount;
    private boolean advancedLogin;
    private boolean navigatingToConversations;

    public static void launch(AppCompatActivity activity) {
        Intent intent = new Intent(activity, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.overridePendingTransition(0, 0);
    }

    public void onInstallReferrerDiscovered(final Uri referrer) {
        // Referrers can contain JIDs and pre-authentication tokens. Never include the raw URI in
        // logs, even in debug builds.
        Log.d(Config.LOGTAG, "welcome activity: install referrer discovered");
        if (MiniUri.getOrNull(referrer) instanceof MiniUri.Xmpp xmpp) {
            runOnUiThread(() -> processXmppUri(xmpp));
        } else {
            Log.i(Config.LOGTAG, "install referrer was not an XMPP uri");
        }
    }

    private void processXmppUri(final MiniUri.Xmpp xmppUri) {
        if (!xmppUri.isAddress()) {
            return;
        }
        if (xmppUri.isAction(MiniUri.Xmpp.ACTION_REGISTER)) {
            this.inviteUri = null;
            Toast.makeText(
                            this,
                            R.string.account_registrations_are_not_supported,
                            Toast.LENGTH_LONG)
                    .show();
            return;
        }
        // Roster invitations that used to request in-band registration now stay in the login
        // flow. Once an existing account is authenticated, the normal invite handling resumes.
        this.inviteUri = xmppUri;
    }

    @Override
    protected void refreshUiReal() {
        if (!xmppConnectionServiceBound || navigatingToConversations) {
            return;
        }
        if (pendingAccount == null) {
            pendingAccount = AccountUtils.getPendingAccount(xmppConnectionService);
        }
        if (pendingAccount == null) {
            setLoginBusy(false);
            return;
        }

        final Account.State status = pendingAccount.getStatus();
        if (status == Account.State.ONLINE) {
            navigatingToConversations = true;
            clearPasswordField();
            startActivity(
                    StartConversationActivity.startOrConversationsActivity(this, pendingAccount));
            finish();
            return;
        }

        if (isActionableConnectionError(status)) {
            setLoginBusy(false);
            showConnectionError(status);
        } else if (status == Account.State.DISABLED || status == Account.State.LOGGED_OUT) {
            setLoginBusy(false);
        } else {
            setLoginBusy(true);
            binding.loginStatus.setText(status.getReadableId());
        }
    }

    @Override
    protected void onBackendConnected() {
        for (final Account account : xmppConnectionService.getAccounts()) {
            if (account.isOptionSet(Account.OPTION_LOGGED_IN_SUCCESSFULLY)) {
                navigatingToConversations = true;
                startActivity(
                        StartConversationActivity.startOrConversationsActivity(this, account));
                finish();
                return;
            }
        }
        pendingAccount = AccountUtils.getPendingAccount(xmppConnectionService);
        if (pendingAccount != null && binding.accountIdentifier.getText().length() == 0) {
            populateIdentifier(pendingAccount.getJid().asBareJid());
        }
        refreshUiReal();
    }

    @Override
    public void onStart() {
        super.onStart();
        new InstallReferrerUtils(this);
    }

    @Override
    public void onStop() {
        clearPasswordField();
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // BaseActivity applies the global screenshot preference during resume. The credential
        // screen is always sensitive, independently of that preference.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
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
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_welcome);
        Activities.setStatusAndNavigationBarColors(this, binding.getRoot());
        setSupportActionBar(binding.toolbar);
        configureActionBar(getSupportActionBar(), false);
        setTitle(null);
        advancedLogin =
                savedInstanceState != null
                        && savedInstanceState.getBoolean(STATE_ADVANCED_LOGIN, false);
        applyAdvancedLoginMode(false);
        binding.loginButton.setOnClickListener(v -> submitCredentials());
        binding.advancedLoginToggle.setOnClickListener(v -> toggleAdvancedLoginMode());
        binding.accountPassword.setOnEditorActionListener(
                (view, actionId, event) -> {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        submitCredentials();
                        return true;
                    }
                    return false;
                });
        final TextWatcher clearErrorTextWatcher =
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(
                            final CharSequence s,
                            final int start,
                            final int count,
                            final int after) {}

                    @Override
                    public void onTextChanged(
                            final CharSequence s,
                            final int start,
                            final int before,
                            final int count) {
                        clearLoginErrors();
                    }

                    @Override
                    public void afterTextChanged(final Editable s) {}
                };
        binding.accountIdentifier.addTextChangedListener(clearErrorTextWatcher);
        binding.accountPassword.addTextChangedListener(clearErrorTextWatcher);
        setLoginBusy(true);
    }

    @Override
    protected void onSaveInstanceState(@NonNull final Bundle outState) {
        outState.putBoolean(STATE_ADVANCED_LOGIN, advancedLogin);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onAccountUpdate() {
        refreshUi();
    }

    private void submitCredentials() {
        if (!xmppConnectionServiceBound) {
            showLoginError(R.string.maer_login_error_initializing, null);
            return;
        }
        clearLoginErrors();
        final String password = binding.accountPassword.getText().toString();
        if (password.isEmpty()) {
            binding.accountPasswordLayout.setError(
                    getString(R.string.password_should_not_be_empty));
            binding.accountPassword.requestFocus();
            return;
        }

        final Jid jid;
        try {
            jid =
                    LoginJid.build(
                            binding.accountIdentifier.getText().toString(),
                            advancedLogin,
                            BuildConfig.DEFAULT_XMPP_DOMAIN);
        } catch (final IllegalArgumentException e) {
            binding.accountIdentifierLayout.setError(
                    getString(advancedLogin ? R.string.invalid_jid : R.string.invalid_username));
            binding.accountIdentifier.requestFocus();
            return;
        }

        Account account = xmppConnectionService.findAccountByJid(jid);
        if (account != null && account.isOptionSet(Account.OPTION_LOGGED_IN_SUCCESSFULLY)) {
            clearPasswordField();
            navigatingToConversations = true;
            startActivity(StartConversationActivity.startOrConversationsActivity(this, account));
            finish();
            return;
        }
        if (pendingAccount != null && pendingAccount != account) {
            xmppConnectionService.deleteAccount(pendingAccount);
            pendingAccount = null;
        }

        if (account == null) {
            account = new Account(jid, password);
            account.setOption(Account.OPTION_REGISTER, false);
            pendingAccount = account;
            try {
                xmppConnectionService.createAccount(account);
            } catch (final IllegalStateException e) {
                // AccountSecretStorage fails closed when Android Keystore is unavailable.
                // Surface that condition without retaining the submitted password in the UI.
                pendingAccount = null;
                clearPasswordField();
                setLoginBusy(false);
                showLoginError(R.string.maer_login_error_save, null);
                return;
            }
        } else {
            pendingAccount = account;
            account.setPassword(password);
            account.setOption(Account.OPTION_REGISTER, false);
            account.setOption(Account.OPTION_DISABLED, false);
            account.setOption(Account.OPTION_SOFT_DISABLED, false);
            final boolean updated;
            try {
                updated = xmppConnectionService.updateAccount(account);
            } catch (final IllegalStateException e) {
                // See the creation path above: never bypass encrypted persistence.
                clearPasswordField();
                setLoginBusy(false);
                showLoginError(R.string.maer_login_error_save, null);
                return;
            }
            if (!updated) {
                clearPasswordField();
                setLoginBusy(false);
                showLoginError(R.string.maer_login_error_save, null);
                return;
            }
        }
        clearPasswordField();
        setLoginBusy(true);
        binding.loginStatus.setText(R.string.account_status_connecting);
    }

    private void toggleAdvancedLoginMode() {
        final String current = binding.accountIdentifier.getText().toString().trim();
        advancedLogin = !advancedLogin;
        if (advancedLogin && !current.isEmpty() && current.indexOf('@') < 0) {
            try {
                binding.accountIdentifier.setText(
                        LoginJid.build(current, false, BuildConfig.DEFAULT_XMPP_DOMAIN).toString());
            } catch (final IllegalArgumentException ignored) {
                // The regular validation message will be shown if the user submits this value.
            }
        } else if (!advancedLogin && !current.isEmpty()) {
            try {
                final Jid jid = LoginJid.build(current, true, BuildConfig.DEFAULT_XMPP_DOMAIN);
                if (BuildConfig.DEFAULT_XMPP_DOMAIN.equals(jid.getDomain().toString())) {
                    binding.accountIdentifier.setText(jid.getLocal());
                }
            } catch (final IllegalArgumentException ignored) {
                // Keep the input intact so switching modes never destroys what the user typed.
            }
        }
        applyAdvancedLoginMode(true);
    }

    private void applyAdvancedLoginMode(final boolean requestFocus) {
        if (advancedLogin) {
            binding.accountIdentifierLayout.setHint(R.string.maer_login_complete_jid);
            binding.accountIdentifierLayout.setSuffixText(null);
            binding.serverSummary.setText(R.string.maer_login_advanced_summary);
            binding.advancedLoginToggle.setText(R.string.maer_login_use_default_server);
        } else {
            binding.accountIdentifierLayout.setHint(R.string.username);
            binding.accountIdentifierLayout.setSuffixText("@" + BuildConfig.DEFAULT_XMPP_DOMAIN);
            binding.serverSummary.setText(
                    getString(R.string.maer_login_server_summary, BuildConfig.DEFAULT_XMPP_DOMAIN));
            binding.advancedLoginToggle.setText(R.string.maer_login_use_complete_jid);
        }
        clearLoginErrors();
        if (requestFocus) {
            binding.accountIdentifier.requestFocus();
            binding.accountIdentifier.setSelection(binding.accountIdentifier.length());
        }
    }

    private void populateIdentifier(final Jid jid) {
        if (BuildConfig.DEFAULT_XMPP_DOMAIN.equals(jid.getDomain().toString())) {
            advancedLogin = false;
            binding.accountIdentifier.setText(jid.getLocal());
        } else {
            advancedLogin = true;
            binding.accountIdentifier.setText(jid.toString());
        }
        applyAdvancedLoginMode(false);
    }

    private void setLoginBusy(final boolean busy) {
        binding.loginProgress.setVisibility(busy ? View.VISIBLE : View.GONE);
        binding.loginStatus.setVisibility(busy ? View.VISIBLE : View.GONE);
        binding.loginButton.setEnabled(!busy);
        binding.accountIdentifier.setEnabled(!busy);
        binding.accountPassword.setEnabled(!busy);
        binding.advancedLoginToggle.setEnabled(!busy);
    }

    private void showConnectionError(final Account.State status) {
        final int message = getConnectionErrorMessage(status);
        final View focus;
        if (status == Account.State.UNAUTHORIZED
                || status == Account.State.TEMPORARY_AUTH_FAILURE) {
            focus = binding.accountPassword;
            binding.accountPasswordLayout.setError(getString(message));
        } else {
            focus = null;
        }
        showLoginError(message, focus);
    }

    private void showLoginError(@StringRes final int message, final View focus) {
        binding.loginError.setText(message);
        binding.loginError.setVisibility(View.VISIBLE);
        if (focus != null) {
            focus.requestFocus();
        }
    }

    private void clearLoginErrors() {
        binding.accountIdentifierLayout.setError(null);
        binding.accountPasswordLayout.setError(null);
        binding.loginError.setVisibility(View.GONE);
    }

    private void clearPasswordField() {
        if (binding != null) {
            binding.accountPassword.getText().clear();
        }
    }

    private static boolean isActionableConnectionError(final Account.State status) {
        return status.isError()
                || status == Account.State.NO_INTERNET
                || status == Account.State.AIRPLANE_MODE
                || status == Account.State.MISSING_INTERNET_PERMISSION;
    }

    @StringRes
    private static int getConnectionErrorMessage(final Account.State status) {
        return switch (status) {
            case UNAUTHORIZED, TEMPORARY_AUTH_FAILURE -> R.string.maer_login_error_authentication;
            case TLS_ERROR,
                    TLS_ERROR_DOMAIN,
                    TLS_ERROR_UNTRUSTED,
                    TLS_ERROR_PROTOCOL,
                    CHANNEL_BINDING,
                    DOWNGRADE_ATTACK ->
                    R.string.maer_login_error_tls;
            case SERVER_NOT_FOUND,
                    HOST_UNKNOWN,
                    CONNECTION_TIMEOUT,
                    SEE_OTHER_HOST,
                    STREAM_OPENING_ERROR ->
                    R.string.maer_login_error_server;
            case NO_INTERNET, AIRPLANE_MODE, MISSING_INTERNET_PERMISSION ->
                    R.string.maer_login_error_network;
            case INCOMPATIBLE_SERVER, INCOMPATIBLE_CLIENT -> R.string.maer_login_error_incompatible;
            default -> R.string.maer_login_error_generic;
        };
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.welcome_menu, menu);
        final MenuItem scan = menu.findItem(R.id.action_scan_qr_code);
        scan.setVisible(Compatibility.hasFeatureCamera(this));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_import_backup:
                if (hasStoragePermission(REQUEST_IMPORT_BACKUP)) {
                    startActivity(new Intent(this, ImportBackupActivity.class));
                }
                break;
            case R.id.action_scan_qr_code:
                requestPermissionAndScanQrCode();
                break;
            case R.id.action_add_account_with_cert:
                addAccountFromKey();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addAccountFromKey() {
        try {
            KeyChain.choosePrivateKeyAlias(this, this, null, null, null, -1, null);
        } catch (final ActivityNotFoundException e) {
            Toast.makeText(this, R.string.device_does_not_support_certificates, Toast.LENGTH_LONG)
                    .show();
        }
    }

    @Override
    public void alias(final String alias) {
        if (Strings.isNullOrEmpty(alias)) {
            runOnUiThread(
                    () ->
                            Toast.makeText(
                                            this,
                                            R.string.no_certificate_selected,
                                            Toast.LENGTH_LONG)
                                    .show());
            return;
        }
        xmppConnectionService.createAccountFromKey(alias, this);
    }

    @Override
    public void onAccountCreated(final Account account) {
        final Intent intent = new Intent(this, EditAccountActivity.class);
        intent.putExtra("jid", account.getJid().asBareJid().toString());
        intent.putExtra("init", true);
        addInviteUri(intent);
        startActivity(intent);
    }

    @Override
    public void informUser(final int r) {
        runOnUiThread(() -> Toast.makeText(this, r, Toast.LENGTH_LONG).show());
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0) {
            if (allGranted(grantResults)) {
                switch (requestCode) {
                    case REQUEST_IMPORT_BACKUP:
                        startActivity(new Intent(this, ImportBackupActivity.class));
                        break;
                }
            } else if (Arrays.asList(permissions)
                    .contains(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(
                                this,
                                getString(
                                        R.string.no_storage_permission,
                                        getString(R.string.app_name)),
                                Toast.LENGTH_SHORT)
                        .show();
            }
        }
        if (writeGranted(grantResults, permissions)) {
            if (xmppConnectionService != null) {
                xmppConnectionService.restartFileObserverAndCheckForDeletedFiles();
            }
        }
    }

    public void addInviteUri(Intent to) {
        final Intent from = getIntent();
        if (from != null && from.hasExtra(StartConversationActivity.EXTRA_INVITE_URI)) {
            final String invite = from.getStringExtra(StartConversationActivity.EXTRA_INVITE_URI);
            to.putExtra(StartConversationActivity.EXTRA_INVITE_URI, invite);
        } else if (this.inviteUri != null) {
            Log.d(Config.LOGTAG, "injecting referrer uri into on-boarding flow");
            to.putExtra(StartConversationActivity.EXTRA_INVITE_URI, this.inviteUri.toString());
        }
    }
}
