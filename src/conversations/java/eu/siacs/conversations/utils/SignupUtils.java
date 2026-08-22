package eu.siacs.conversations.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.google.common.collect.Iterables;
import eu.siacs.conversations.Config;
import eu.siacs.conversations.Conversations;
import eu.siacs.conversations.entities.Account;
import eu.siacs.conversations.persistance.DatabaseBackend;
import eu.siacs.conversations.ui.ConversationsActivity;
import eu.siacs.conversations.ui.ManageAccountActivity;
import eu.siacs.conversations.ui.WelcomeActivity;
import eu.siacs.conversations.xmpp.Jid;
import java.util.Collection;

public class SignupUtils {

    public static boolean isSupportTokenRegistry() {
        return false;
    }

    public static Intent getTokenRegistrationIntent(
            final Context context, final Jid ignoredJid, final String ignoredPreAuth) {
        // Maer accounts are provisioned by the server administrator. Never pass invitation
        // tokens to the legacy account-creation flow; return the existing-account login instead.
        return getSignUpIntent(context);
    }

    public static Intent getSignUpIntent(final Context context) {
        return getSignUpIntent(context, false);
    }

    public static Intent getSignUpIntent(
            final Context context, final boolean ignoredToServerChooser) {
        return new Intent(context, WelcomeActivity.class);
    }

    public static Intent getRedirectionIntent(final Context context) {
        final var state = getSetupState(Conversations.getInstance(context).getAccounts());
        Log.d(Config.LOGTAG, "setup state: " + state);
        final Intent intent;
        if (state instanceof Done) {
            intent = new Intent(context, ConversationsActivity.class);
        } else if (state instanceof Pending) {
            // Legacy pending registration rows must not revive an account-creation screen.
            intent = new Intent(context, WelcomeActivity.class);
        } else if (state instanceof None) {
            if (Config.X509_VERIFICATION) {
                intent = new Intent(context, ManageAccountActivity.class);
            } else {
                intent = new Intent(context, WelcomeActivity.class);
            }
        } else {
            throw new AssertionError("Invalid setup state");
        }
        intent.putExtra("init", true);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return intent;
    }

    private static SetupState getSetupState(
            final Collection<DatabaseBackend.AccountWithOptions> accounts) {
        if (accounts.isEmpty()) {
            return new None();
        }
        final var pending =
                Iterables.all(accounts, a -> !a.isOptionSet(Account.OPTION_LOGGED_IN_SUCCESSFULLY));
        if (pending) {
            return new Pending(Iterables.getFirst(accounts, null));
        }
        return new Done();
    }

    public sealed interface SetupState permits None, Pending, Done {}

    public record None() implements SetupState {}

    public record Pending(DatabaseBackend.AccountWithOptions account) implements SetupState {}

    public record Done() implements SetupState {}
}
