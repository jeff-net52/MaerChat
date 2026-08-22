package eu.siacs.conversations.persistance;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.UUID;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.ConscryptMode;

@RunWith(RobolectricTestRunner.class)
@ConscryptMode(ConscryptMode.Mode.OFF)
public class ImportBackupSecretStorageTest {

    private ImportBackupSecretStorage storage;

    @Before
    public void setUp() throws Exception {
        final KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(128);
        final SecretKey key = keyGenerator.generateKey();
        final AccountSecretStorage accountSecretStorage =
                new AccountSecretStorage(() -> key, new SecureRandom());
        final File directory =
                new File(
                        RuntimeEnvironment.getApplication().getNoBackupFilesDir(),
                        "import-secret-test-" + UUID.randomUUID());
        storage = new ImportBackupSecretStorage(directory, accountSecretStorage);
    }

    @Test
    public void persistsOnlyCiphertextAndDeletesIt() throws Exception {
        final UUID workRequestId = UUID.randomUUID();
        final String cleartext = "mot de passe de sauvegarde 🔐";

        storage.store(workRequestId, cleartext);

        final String onDisk =
                new String(
                        Files.readAllBytes(storage.file(workRequestId).toPath()),
                        StandardCharsets.UTF_8);
        assertFalse(onDisk.contains(cleartext));
        assertEquals(cleartext, storage.read(workRequestId));

        storage.delete(workRequestId);
        assertFalse(storage.file(workRequestId).exists());
    }

    @Test
    public void ciphertextCannotBeReadForAnotherWorkRequest() throws Exception {
        final UUID first = UUID.randomUUID();
        final UUID second = UUID.randomUUID();
        storage.store(first, "secret");
        assertTrue(storage.file(first).renameTo(storage.file(second)));

        assertThrows(Exception.class, () -> storage.read(second));
    }
}
