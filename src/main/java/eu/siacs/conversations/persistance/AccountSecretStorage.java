package eu.siacs.conversations.persistance;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import androidx.annotation.VisibleForTesting;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyStore;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/** Encrypts account credentials before they cross the SQLite persistence boundary. */
public final class AccountSecretStorage {

    static final String ENVELOPE_PREFIX = "xmpp-account-secret:v1:";

    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";
    private static final String KEY_ALIAS = "maer_chat_account_secrets_v1";
    private static final String CIPHER = "AES/GCM/NoPadding";
    private static final int IV_BYTES = 12;
    private static final int TAG_BITS = 128;

    private final SecretKeyProvider secretKeyProvider;
    private final SecureRandom secureRandom;

    public AccountSecretStorage() {
        this(new AndroidKeyStoreSecretKeyProvider(), new SecureRandom());
    }

    @VisibleForTesting
    AccountSecretStorage(
            final SecretKeyProvider secretKeyProvider, final SecureRandom secureRandom) {
        this.secretKeyProvider = secretKeyProvider;
        this.secureRandom = secureRandom;
    }

    /**
     * Encrypts a value using a fresh IV. The account UUID and column name are authenticated as AAD,
     * so ciphertext cannot be moved to another account or credential field.
     */
    public String encrypt(final String accountUuid, final String column, final String cleartext)
            throws GeneralSecurityException, IOException {
        if (cleartext == null) {
            return null;
        }
        final Cipher cipher = Cipher.getInstance(CIPHER);
        // Android Keystore rejects a caller-provided IV when randomized encryption is required.
        // Let the provider generate it, then persist it next to the authenticated ciphertext.
        cipher.init(Cipher.ENCRYPT_MODE, secretKeyProvider.getOrCreate(), secureRandom);
        final byte[] iv = cipher.getIV();
        if (iv == null || iv.length != IV_BYTES) {
            throw new GeneralSecurityException("Unexpected AES-GCM IV length");
        }
        cipher.updateAAD(aad(accountUuid, column));
        final byte[] ciphertext = cipher.doFinal(cleartext.getBytes(StandardCharsets.UTF_8));
        final ByteBuffer envelope = ByteBuffer.allocate(iv.length + ciphertext.length);
        envelope.put(iv);
        envelope.put(ciphertext);
        return ENVELOPE_PREFIX
                + Base64.encodeToString(envelope.array(), Base64.NO_WRAP | Base64.NO_PADDING);
    }

    /** Decrypts a versioned envelope. Plaintext is deliberately rejected after migration. */
    public String decrypt(
            final String accountUuid, final String column, final String encryptedValue)
            throws GeneralSecurityException, IOException {
        if (encryptedValue == null) {
            return null;
        }
        if (!encryptedValue.startsWith(ENVELOPE_PREFIX)) {
            throw new GeneralSecurityException("Account secret is not encrypted");
        }
        final byte[] envelope;
        try {
            envelope =
                    Base64.decode(
                            encryptedValue.substring(ENVELOPE_PREFIX.length()),
                            Base64.NO_WRAP | Base64.NO_PADDING);
        } catch (final IllegalArgumentException e) {
            throw new GeneralSecurityException("Invalid account secret envelope", e);
        }
        if (envelope.length < IV_BYTES + TAG_BITS / Byte.SIZE) {
            throw new GeneralSecurityException("Truncated account secret envelope");
        }
        final byte[] iv = new byte[IV_BYTES];
        final byte[] ciphertext = new byte[envelope.length - IV_BYTES];
        System.arraycopy(envelope, 0, iv, 0, iv.length);
        System.arraycopy(envelope, iv.length, ciphertext, 0, ciphertext.length);
        final Cipher cipher = Cipher.getInstance(CIPHER);
        cipher.init(
                Cipher.DECRYPT_MODE,
                secretKeyProvider.getOrCreate(),
                new GCMParameterSpec(TAG_BITS, iv));
        cipher.updateAAD(aad(accountUuid, column));
        return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
    }

    public boolean isEncrypted(final String value) {
        return value == null || value.startsWith(ENVELOPE_PREFIX);
    }

    private static byte[] aad(final String accountUuid, final String column) {
        return (accountUuid + '\u0000' + column).getBytes(StandardCharsets.UTF_8);
    }

    @VisibleForTesting
    interface SecretKeyProvider {
        SecretKey getOrCreate() throws GeneralSecurityException, IOException;
    }

    private static final class AndroidKeyStoreSecretKeyProvider implements SecretKeyProvider {

        private SecretKey cachedKey;

        @Override
        public synchronized SecretKey getOrCreate() throws GeneralSecurityException, IOException {
            if (cachedKey != null) {
                return cachedKey;
            }
            final KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
            keyStore.load(null);
            final Key existingKey = keyStore.getKey(KEY_ALIAS, null);
            if (existingKey != null) {
                if (!(existingKey instanceof SecretKey)) {
                    throw new GeneralSecurityException(
                            "Android Keystore alias does not contain a secret key");
                }
                cachedKey = (SecretKey) existingKey;
                return cachedKey;
            }
            final KeyGenerator keyGenerator =
                    KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE);
            keyGenerator.init(
                    new KeyGenParameterSpec.Builder(
                                    KEY_ALIAS,
                                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                            .setRandomizedEncryptionRequired(true)
                            // No biometric/device-credential gate: the XMPP service must reconnect
                            // while the screen is locked and without foreground UI.
                            .setUserAuthenticationRequired(false)
                            .build());
            cachedKey = keyGenerator.generateKey();
            return cachedKey;
        }
    }
}
