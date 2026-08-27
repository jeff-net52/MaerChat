package eu.siacs.conversations.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import eu.siacs.conversations.R;
import eu.siacs.conversations.databinding.ActivityLinkedDevicesBinding;
import eu.siacs.conversations.entities.Account;
import eu.siacs.conversations.entities.LinkedDevice;
import eu.siacs.conversations.entities.PairingRequestInfo;
import eu.siacs.conversations.services.XmppConnectionService;
import eu.siacs.conversations.ui.adapter.LinkedDeviceAdapter;
import eu.siacs.conversations.ui.widget.AccountPickerDialog;
import eu.siacs.conversations.utils.MaerPairingUri;
import eu.siacs.conversations.utils.PairingReplayGuard;
import eu.siacs.conversations.xmpp.manager.LinkedDevicesManager;
import im.conversations.android.xmpp.IqErrorException;
import im.conversations.android.xmpp.model.error.Condition;
import java.text.DateFormat;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class LinkedDevicesActivity extends QrCodeScanningActivity
        implements XmppConnectionService.OnAccountUpdate, LinkedDeviceAdapter.Listener {

    private final LinkedDeviceAdapter adapter = new LinkedDeviceAdapter(this);

    private ActivityLinkedDevicesBinding binding;
    private Account selectedAccount;
    private ListenableFuture<?> devicesFuture;
    private ListenableFuture<?> operationFuture;
    private boolean devicesLoaded;
    private boolean devicesLoading;
    private boolean devicesError;
    private boolean operationInFlight;
    private boolean destroyed;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_linked_devices);
        Activities.setStatusAndNavigationBarColors(this, binding.getRoot());
        setSupportActionBar(binding.toolbar);
        configureActionBar(getSupportActionBar(), true);

        binding.devices.setAdapter(adapter);
        binding.account.setOnClickListener(view -> chooseAccount());
        binding.associate.setOnClickListener(view -> requestPermissionAndScanQrCode());
        binding.retry.setOnClickListener(view -> loadDevices());
        renderState();
    }

    @Override
    protected void onBackendConnected() {
        if (selectedAccount == null) {
            chooseAccount();
        } else if (isSelectedAccountAvailable()) {
            loadDevices();
        }
        renderState();
    }

    @Override
    protected void refreshUiReal() {
        if (isSelectedAccountAvailable()
                && !devicesLoaded
                && !devicesLoading
                && devicesFuture == null) {
            loadDevices();
        } else {
            renderState();
        }
    }

    @Override
    public void onAccountUpdate() {
        refreshUi();
    }

    private void chooseAccount() {
        if (!xmppConnectionServiceBound || operationInFlight) {
            return;
        }
        final var picker = new AccountPickerDialog.LinkedDevices(this);
        if (!picker.hasAnyAccounts()) {
            selectedAccount = null;
            renderState();
            return;
        }
        picker.pick(this::selectAccount);
    }

    private void selectAccount(final Account account) {
        if (account == selectedAccount) {
            if (isSelectedAccountAvailable() && !devicesLoading) {
                loadDevices();
            }
            return;
        }
        cancelFuture(devicesFuture);
        devicesFuture = null;
        selectedAccount = account;
        devicesLoaded = false;
        devicesLoading = false;
        devicesError = false;
        adapter.submitList(Collections.emptyList());
        renderState();
        loadDevices();
    }

    private boolean isSelectedAccountAvailable() {
        final var connection = selectedAccount == null ? null : selectedAccount.getXmppConnection();
        return connection != null
                && connection.getManager(LinkedDevicesManager.class).isAvailable();
    }

    private void loadDevices() {
        if (!isSelectedAccountAvailable() || devicesLoading) {
            renderState();
            return;
        }
        final Account account = selectedAccount;
        devicesLoading = true;
        devicesLoaded = false;
        devicesError = false;
        renderState();
        final var future =
                account.getXmppConnection().getManager(LinkedDevicesManager.class).getDevices();
        devicesFuture = future;
        Futures.addCallback(
                future,
                new FutureCallback<>() {
                    @Override
                    public void onSuccess(final List<LinkedDevice> devices) {
                        if (!isCurrent(account, future)) {
                            return;
                        }
                        devicesFuture = null;
                        devicesLoading = false;
                        devicesLoaded = true;
                        devicesError = false;
                        adapter.submitList(List.copyOf(devices));
                        renderState();
                    }

                    @Override
                    public void onFailure(@NonNull final Throwable throwable) {
                        if (!isCurrent(account, future)) {
                            return;
                        }
                        devicesFuture = null;
                        devicesLoading = false;
                        devicesLoaded = false;
                        devicesError = true;
                        renderState();
                    }
                },
                ContextCompat.getMainExecutor(this));
    }

    @Override
    void onQrCodeScanned(final String rawValue) {
        if (!isSelectedAccountAvailable() || operationInFlight) {
            renderState();
            return;
        }
        final MaerPairingUri pairingUri;
        try {
            pairingUri = MaerPairingUri.parse(rawValue);
        } catch (final IllegalArgumentException e) {
            Toast.makeText(this, R.string.invalid_pairing_qr_code, Toast.LENGTH_LONG).show();
            return;
        }
        if (PairingReplayGuard.isConsumed(this, pairingUri.getSessionId(), Instant.now())) {
            Toast.makeText(this, R.string.pairing_request_already_used, Toast.LENGTH_LONG).show();
            return;
        }
        inspect(pairingUri);
    }

    private void inspect(final MaerPairingUri pairingUri) {
        final Account account = selectedAccount;
        operationInFlight = true;
        renderState();
        final var future =
                account.getXmppConnection()
                        .getManager(LinkedDevicesManager.class)
                        .inspect(pairingUri);
        operationFuture = future;
        Futures.addCallback(
                future,
                new FutureCallback<>() {
                    @Override
                    public void onSuccess(final PairingRequestInfo info) {
                        if (!isCurrentOperation(account, future)) {
                            return;
                        }
                        operationFuture = null;
                        showApprovalConfirmation(account, pairingUri, info);
                    }

                    @Override
                    public void onFailure(@NonNull final Throwable throwable) {
                        if (!isCurrentOperation(account, future)) {
                            return;
                        }
                        operationFuture = null;
                        finishOperation();
                        Toast.makeText(
                                        LinkedDevicesActivity.this,
                                        pairingError(
                                                throwable,
                                                R.string.could_not_inspect_pairing_request),
                                        Toast.LENGTH_LONG)
                                .show();
                    }
                },
                ContextCompat.getMainExecutor(this));
    }

    private void showApprovalConfirmation(
            final Account account, final MaerPairingUri pairingUri, final PairingRequestInfo info) {
        if (destroyed || isFinishing()) {
            finishOperation();
            return;
        }
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.confirm_link_device)
                .setMessage(
                        getString(
                                R.string.confirm_link_device_message,
                                info.getLabel(),
                                info.getPlatform(),
                                account.getJid().asBareJid(),
                                pairingUri.getVerificationCode(),
                                format(info.getExpiresAt())))
                .setNegativeButton(R.string.cancel, (dialog, which) -> finishOperation())
                .setOnCancelListener(dialog -> finishOperation())
                .setPositiveButton(
                        R.string.link_device, (dialog, which) -> approve(account, pairingUri, info))
                .show();
    }

    private void approve(
            final Account account, final MaerPairingUri pairingUri, final PairingRequestInfo info) {
        if (account != selectedAccount || !isSelectedAccountAvailable()) {
            finishOperation();
            return;
        }
        if (!info.getExpiresAt().isAfter(Instant.now())) {
            finishOperation();
            Toast.makeText(this, R.string.pairing_request_expired, Toast.LENGTH_LONG).show();
            return;
        }
        final var future =
                account.getXmppConnection()
                        .getManager(LinkedDevicesManager.class)
                        .approve(pairingUri);
        operationFuture = future;
        Futures.addCallback(
                future,
                new FutureCallback<>() {
                    @Override
                    public void onSuccess(final String deviceId) {
                        if (!isCurrentOperation(account, future)) {
                            return;
                        }
                        operationFuture = null;
                        PairingReplayGuard.markConsumed(
                                LinkedDevicesActivity.this,
                                pairingUri.getSessionId(),
                                info.getExpiresAt(),
                                Instant.now());
                        finishOperation();
                        Toast.makeText(
                                        LinkedDevicesActivity.this,
                                        R.string.computer_linked,
                                        Toast.LENGTH_LONG)
                                .show();
                        loadDevices();
                    }

                    @Override
                    public void onFailure(@NonNull final Throwable throwable) {
                        if (!isCurrentOperation(account, future)) {
                            return;
                        }
                        operationFuture = null;
                        finishOperation();
                        Toast.makeText(
                                        LinkedDevicesActivity.this,
                                        pairingError(throwable, R.string.could_not_link_device),
                                        Toast.LENGTH_LONG)
                                .show();
                    }
                },
                ContextCompat.getMainExecutor(this));
    }

    @Override
    public void onRevoke(final LinkedDevice device) {
        if (!isSelectedAccountAvailable() || operationInFlight) {
            return;
        }
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.revoke_linked_device)
                .setMessage(getString(R.string.revoke_linked_device_message, device.getLabel()))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(
                        R.string.revoke_linked_device, (dialog, which) -> revoke(device.getId()))
                .show();
    }

    private void revoke(final String deviceId) {
        if (!isSelectedAccountAvailable()) {
            return;
        }
        final Account account = selectedAccount;
        operationInFlight = true;
        renderState();
        final var future =
                account.getXmppConnection().getManager(LinkedDevicesManager.class).revoke(deviceId);
        operationFuture = future;
        Futures.addCallback(
                future,
                new FutureCallback<>() {
                    @Override
                    public void onSuccess(final Void ignored) {
                        if (!isCurrentOperation(account, future)) {
                            return;
                        }
                        operationFuture = null;
                        finishOperation();
                        Toast.makeText(
                                        LinkedDevicesActivity.this,
                                        R.string.linked_device_revoked,
                                        Toast.LENGTH_LONG)
                                .show();
                        loadDevices();
                    }

                    @Override
                    public void onFailure(@NonNull final Throwable throwable) {
                        if (!isCurrentOperation(account, future)) {
                            return;
                        }
                        operationFuture = null;
                        finishOperation();
                        Toast.makeText(
                                        LinkedDevicesActivity.this,
                                        pairingError(
                                                throwable, R.string.could_not_revoke_linked_device),
                                        Toast.LENGTH_LONG)
                                .show();
                    }
                },
                ContextCompat.getMainExecutor(this));
    }

    private void finishOperation() {
        operationInFlight = false;
        renderState();
    }

    private boolean isCurrent(final Account account, final ListenableFuture<?> future) {
        return !destroyed && account == selectedAccount && future == devicesFuture;
    }

    private boolean isCurrentOperation(final Account account, final ListenableFuture<?> future) {
        return !destroyed && account == selectedAccount && future == operationFuture;
    }

    private void renderState() {
        if (binding == null) {
            return;
        }
        final boolean accountAvailable = isSelectedAccountAvailable();
        binding.account.setText(
                selectedAccount == null
                        ? getString(R.string.choose_account)
                        : selectedAccount.getJid().asBareJid().toString());
        binding.account.setEnabled(xmppConnectionServiceBound && !operationInFlight);
        binding.associate.setEnabled(accountAvailable && !operationInFlight);

        final int status;
        if (!accountAvailable) {
            status =
                    selectedAccount == null
                            ? R.string.choose_account_for_linked_devices
                            : R.string.linked_devices_offline;
        } else if (devicesLoading) {
            status = R.string.loading_linked_devices;
        } else if (devicesError) {
            status = R.string.could_not_load_linked_devices;
        } else if (devicesLoaded && adapter.getItemCount() == 0) {
            status = R.string.no_linked_devices;
        } else {
            status = 0;
        }
        binding.progress.setVisibility(devicesLoading ? View.VISIBLE : View.GONE);
        binding.retry.setVisibility(devicesError && accountAvailable ? View.VISIBLE : View.GONE);
        binding.status.setVisibility(status == 0 || devicesLoading ? View.GONE : View.VISIBLE);
        if (status != 0) {
            binding.status.setText(status);
        }
        binding.devices.setVisibility(status == 0 && !devicesLoading ? View.VISIBLE : View.GONE);
    }

    static int pairingError(final Throwable throwable, final int fallback) {
        if (!(throwable instanceof IqErrorException iqError)) {
            return fallback;
        }
        return pairingError(iqError.getErrorCondition(), fallback);
    }

    static int pairingError(final Condition condition, final int fallback) {
        if (condition instanceof Condition.ItemNotFound) {
            return R.string.pairing_request_expired;
        }
        if (condition instanceof Condition.Conflict) {
            return R.string.pairing_request_already_used;
        }
        if (condition instanceof Condition.ResourceConstraint) {
            return R.string.pairing_device_limit_reached;
        }
        if (condition instanceof Condition.PolicyViolation) {
            return R.string.pairing_too_many_attempts;
        }
        if (condition instanceof Condition.Forbidden
                || condition instanceof Condition.NotAuthorized) {
            return R.string.pairing_request_not_authorized;
        }
        if (condition instanceof Condition.BadRequest) {
            return R.string.invalid_pairing_qr_code;
        }
        return fallback;
    }

    private static String format(final java.time.Instant instant) {
        return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                .format(Date.from(instant));
    }

    private static void cancelFuture(final ListenableFuture<?> future) {
        if (future != null) {
            future.cancel(true);
        }
    }

    @Override
    protected void onDestroy() {
        destroyed = true;
        cancelFuture(devicesFuture);
        cancelFuture(operationFuture);
        devicesFuture = null;
        operationFuture = null;
        super.onDestroy();
    }
}
