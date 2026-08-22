package eu.siacs.conversations.persistance;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.util.Base64;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.ConscryptMode;

@RunWith(RobolectricTestRunner.class)
@ConscryptMode(ConscryptMode.Mode.OFF)
public class AccountSecretStorageTest {

    private AccountSecretStorage storage;

    @Before
    public void setUp() throws GeneralSecurityException {
        final KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(128);
        final SecretKey key = keyGenerator.generateKey();
        storage = new AccountSecretStorage(() -> key, new SecureRandom());
    }

    @Test
    public void roundTripsUnicodeAndUsesFreshIv() throws Exception {
        final String cleartext = "correct horse battery staple 🔐";
        final String first = storage.encrypt("account-a", "password", cleartext);
        final String second = storage.encrypt("account-a", "password", cleartext);

        assertTrue(storage.isEncrypted(first));
        assertFalse(first.contains(cleartext));
        assertNotEquals(first, second);
        assertEquals(cleartext, storage.decrypt("account-a", "password", first));
        assertEquals(cleartext, storage.decrypt("account-a", "password", second));
    }

    @Test
    public void ciphertextIsBoundToAccountAndColumn() throws Exception {
        final String encrypted = storage.encrypt("account-a", "password", "secret");

        assertDecryptionFails("account-b", "password", encrypted);
        assertDecryptionFails("account-a", "fast_token", encrypted);
    }

    @Test
    public void rejectsTamperingAndPlaintext() throws Exception {
        final String encrypted = storage.encrypt("account-a", "password", "secret");
        final byte[] envelope =
                Base64.decode(
                        encrypted.substring(AccountSecretStorage.ENVELOPE_PREFIX.length()),
                        Base64.NO_WRAP | Base64.NO_PADDING);
        envelope[envelope.length - 1] ^= 0x01;
        final String tampered =
                AccountSecretStorage.ENVELOPE_PREFIX
                        + Base64.encodeToString(envelope, Base64.NO_WRAP | Base64.NO_PADDING);

        assertDecryptionFails("account-a", "password", tampered);
        assertDecryptionFails("account-a", "password", "secret");
    }

    private void assertDecryptionFails(
            final String accountUuid, final String column, final String encryptedValue)
            throws Exception {
        try {
            storage.decrypt(accountUuid, column, encryptedValue);
            fail("Expected authenticated decryption to fail");
        } catch (final GeneralSecurityException expected) {
            // Expected.
        }
    }
}
