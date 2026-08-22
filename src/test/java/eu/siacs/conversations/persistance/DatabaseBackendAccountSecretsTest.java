package eu.siacs.conversations.persistance;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import eu.siacs.conversations.crypto.sasl.ChannelBinding;
import eu.siacs.conversations.crypto.sasl.HashedToken;
import eu.siacs.conversations.entities.Account;
import eu.siacs.conversations.xmpp.Jid;
import java.security.SecureRandom;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.ConscryptMode;

@RunWith(RobolectricTestRunner.class)
@ConscryptMode(ConscryptMode.Mode.OFF)
public class DatabaseBackendAccountSecretsTest {

    private static final String DATABASE_NAME = "history";

    private Context context;
    private DatabaseBackend backend;
    private SecretKey key;

    @Before
    public void setUp() throws Exception {
        context = RuntimeEnvironment.getApplication();
        context.deleteDatabase(DATABASE_NAME);
        final KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(128);
        key = keyGenerator.generateKey();
        backend = new DatabaseBackend(context, storage(key));
    }

    @After
    public void tearDown() {
        if (backend != null) {
            backend.close();
        }
        context.deleteDatabase(DATABASE_NAME);
    }

    @Test
    public void createAndUpdateNeverPersistCleartextCredentials() {
        final Account account = account("initial-password", "initial-fast-token");
        backend.createAccount(account);

        StoredSecrets stored = storedSecrets(backend.getReadableDatabase(), account.getUuid());
        assertEncrypted(stored.password, "initial-password");
        assertEncrypted(stored.fastToken, "initial-fast-token");
        assertEquals("initial-password", backend.getAccounts().get(0).getPassword());
        assertEquals("initial-fast-token", backend.getAccounts().get(0).getFastToken());

        account.setPassword("updated-password");
        account.setFastToken(
                new HashedToken.Mechanism("SHA-256", ChannelBinding.NONE), "updated-fast-token");
        assertTrue(backend.updateAccount(account));

        stored = storedSecrets(backend.getReadableDatabase(), account.getUuid());
        assertEncrypted(stored.password, "updated-password");
        assertEncrypted(stored.fastToken, "updated-fast-token");
    }

    @Test
    public void upgradesLegacyPasswordAndFastTokenAtomically() throws Exception {
        final Account account = account("legacy-password", "legacy-fast-token");
        final SQLiteDatabase db = backend.getWritableDatabase();
        final ContentValues legacyValues = account.getContentValues();
        legacyValues.put(
                Account.KEYS,
                "{\"pre_auth_registration\":\"legacy-secret\","
                        + "\"show_error\":false,\"future_key\":{\"enabled\":true}}");
        legacyValues.put(Account.OPTIONS, 1 << Account.OPTION_REGISTER);
        db.insert(Account.TABLENAME, null, legacyValues);
        db.setVersion(55);
        backend.close();

        backend = new DatabaseBackend(context, storage(key));
        backend.getWritableDatabase();

        final StoredSecrets stored =
                storedSecrets(backend.getReadableDatabase(), account.getUuid());
        assertEncrypted(stored.password, "legacy-password");
        assertEncrypted(stored.fastToken, "legacy-fast-token");
        final Account migrated = backend.getAccounts().get(0);
        assertEquals("legacy-password", migrated.getPassword());
        assertEquals("legacy-fast-token", migrated.getFastToken());
        assertFalse(migrated.isOptionSet(Account.OPTION_REGISTER));
        assertEquals(0, storedOptions(account.getUuid()) & (1 << Account.OPTION_REGISTER));

        final JSONObject migratedKeys = new JSONObject(storedKeys(account.getUuid()));
        assertFalse(migratedKeys.has("pre_auth_registration"));
        assertFalse(migratedKeys.getBoolean("show_error"));
        assertTrue(migratedKeys.getJSONObject("future_key").getBoolean("enabled"));
    }

    @Test
    public void decryptionFailureDoesNotOverwriteExistingCiphertext() throws Exception {
        final Account account = account("recoverable-password", "recoverable-fast-token");
        backend.createAccount(account);
        final StoredSecrets original =
                storedSecrets(backend.getReadableDatabase(), account.getUuid());
        backend.close();

        final KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(128);
        backend = new DatabaseBackend(context, storage(keyGenerator.generateKey()));
        final Account unavailable = backend.getAccounts().get(0);
        assertFalse(unavailable.isPasswordStorageAvailable());
        assertFalse(unavailable.isFastTokenStorageAvailable());
        unavailable.setDisplayName("Still writable");
        assertTrue(backend.updateAccount(unavailable));

        final StoredSecrets afterUpdate =
                storedSecrets(backend.getReadableDatabase(), account.getUuid());
        assertEquals(original.password, afterUpdate.password);
        assertEquals(original.fastToken, afterUpdate.fastToken);
    }

    @Test
    public void restoredAccountIsProtectedBeforeInsert() throws Exception {
        final Account account = account("backup-password", "backup-fast-token");
        final ContentValues backupValues = account.getContentValues();
        backupValues.put(
                Account.KEYS, "{\"pre_auth_registration\":\"backup-secret\",\"show_error\":true}");
        backupValues.put(Account.OPTIONS, 1 << Account.OPTION_REGISTER);

        backend.restoreAccount(backend.getWritableDatabase(), backupValues);

        final StoredSecrets stored =
                storedSecrets(backend.getReadableDatabase(), account.getUuid());
        assertEncrypted(stored.password, "backup-password");
        assertEncrypted(stored.fastToken, "backup-fast-token");
        final JSONObject restoredKeys = new JSONObject(storedKeys(account.getUuid()));
        assertFalse(restoredKeys.has("pre_auth_registration"));
        assertTrue(restoredKeys.getBoolean("show_error"));
        assertEquals(0, storedOptions(account.getUuid()) & (1 << Account.OPTION_REGISTER));
    }

    private static Account account(final String password, final String fastToken) {
        final Account account = new Account(Jid.of("alice@example.com"), password);
        account.setFastToken(new HashedToken.Mechanism("SHA-256", ChannelBinding.NONE), fastToken);
        return account;
    }

    private static AccountSecretStorage storage(final SecretKey key) {
        return new AccountSecretStorage(() -> key, new SecureRandom());
    }

    private static void assertEncrypted(final String stored, final String cleartext) {
        assertNotEquals(cleartext, stored);
        assertFalse(stored.contains(cleartext));
        assertTrue(stored.startsWith(AccountSecretStorage.ENVELOPE_PREFIX));
    }

    private static StoredSecrets storedSecrets(final SQLiteDatabase db, final String uuid) {
        try (final Cursor cursor =
                db.query(
                        Account.TABLENAME,
                        new String[] {Account.PASSWORD, Account.FAST_TOKEN},
                        Account.UUID + "=?",
                        new String[] {uuid},
                        null,
                        null,
                        null)) {
            assertTrue(cursor.moveToFirst());
            return new StoredSecrets(cursor.getString(0), cursor.getString(1));
        }
    }

    private String storedKeys(final String uuid) {
        try (final Cursor cursor =
                backend.getReadableDatabase()
                        .query(
                                Account.TABLENAME,
                                new String[] {Account.KEYS},
                                Account.UUID + "=?",
                                new String[] {uuid},
                                null,
                                null,
                                null)) {
            assertTrue(cursor.moveToFirst());
            return cursor.getString(0);
        }
    }

    private int storedOptions(final String uuid) {
        try (final Cursor cursor =
                backend.getReadableDatabase()
                        .query(
                                Account.TABLENAME,
                                new String[] {Account.OPTIONS},
                                Account.UUID + "=?",
                                new String[] {uuid},
                                null,
                                null,
                                null)) {
            assertTrue(cursor.moveToFirst());
            return cursor.getInt(0);
        }
    }

    private record StoredSecrets(String password, String fastToken) {}
}
