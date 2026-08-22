package eu.siacs.conversations.utils;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import com.google.common.collect.Iterables;
import de.gultsch.common.MiniUri;
import eu.siacs.conversations.Conversations;
import eu.siacs.conversations.R;
import eu.siacs.conversations.persistance.DatabaseBackend;
import eu.siacs.conversations.ui.EditAccountActivity;
import eu.siacs.conversations.ui.ShareWithActivity;
import eu.siacs.conversations.ui.StartConversationActivity;
import java.util.Collection;
import java.util.Objects;

public class XmppUriLauncher {

    private final Context context;
    private final boolean scanned;

    public XmppUriLauncher(final AppCompatActivity context) {
        this(context, false);
    }

    public XmppUriLauncher(final Context context, final boolean scanned) {
        this.context = context;
        this.scanned = scanned;
    }

    public void launch(final MiniUri.Xmpp xmppUri) {
        final var accounts = Conversations.getInstance(context).getAccounts();
        launch(accounts, xmppUri);
    }

    private void launch(
            final Collection<DatabaseBackend.AccountWithOptions> accounts, final MiniUri.Xmpp uri) {
        final var addresses = DatabaseBackend.AccountWithOptions.getAddresses(accounts);
        final Intent intent;
        final var jid = uri.asJid();
        // Account provisioning is administrative in Maer Chat. Reject registration URIs before
        // considering account state, token-registry support, or any legacy creation activity.
        if (uri.isAction(MiniUri.Xmpp.ACTION_REGISTER)) {
            showError(R.string.account_registrations_are_not_supported);
            return;
        }

        if (accounts.isEmpty()) {
            if (uri.isAddress()) {
                intent = SignupUtils.getSignUpIntent(context);
                intent.putExtra(StartConversationActivity.EXTRA_INVITE_URI, uri.asUri().toString());
                this.context.startActivity(intent);
            } else {
                showError(R.string.invalid_jid);
            }
            return;
        }

        if (uri.isAction(MiniUri.Xmpp.ACTION_MESSAGE)) {
            final String body = uri.getParameter("body");

            if (jid != null) {
                final Class<?> clazz = findShareViaAccountClass();
                if (clazz != null) {
                    intent = new Intent(context, clazz);
                    intent.putExtra("contact", jid.toString());
                    intent.putExtra("body", body);
                } else {
                    intent = new Intent(context, StartConversationActivity.class);
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setData(uri.asUri());
                    intent.putExtra(
                            "account",
                            Objects.requireNonNull(Iterables.getFirst(addresses, null)).toString());
                }
            } else {
                intent = new Intent(context, ShareWithActivity.class);
                intent.setAction(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, body);
            }
        } else if (jid != null && addresses.contains(jid)) {
            intent = new Intent(context, EditAccountActivity.class);
            intent.setAction(Intent.ACTION_VIEW);
            intent.putExtra("jid", jid.asBareJid().toString());
            intent.setData(uri.asUri());
            intent.putExtra("scanned", scanned);
        } else if (uri.isAddress()) {
            intent = new Intent(context, StartConversationActivity.class);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            intent.putExtra("scanned", scanned);
            intent.setData(uri.asUri());
        } else {
            showError(R.string.invalid_jid);
            return;
        }
        this.context.startActivity(intent);
    }

    private void showError(@StringRes int error) {
        Toast.makeText(this.context, error, Toast.LENGTH_LONG).show();
    }

    private static Class<?> findShareViaAccountClass() {
        try {
            return Class.forName("eu.siacs.conversations.ui.ShareViaAccountActivity");
        } catch (final ClassNotFoundException e) {
            return null;
        }
    }
}
