package eu.siacs.conversations.persistance;

import android.content.Context;
import android.util.AtomicFile;
import androidx.annotation.VisibleForTesting;
import com.google.common.io.ByteStreams;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.UUID;

/**
 * Holds a backup passphrase between the UI and WorkManager without persisting plaintext in
 * WorkManager's database.
 *
 * <p>Each value is encrypted with Android Keystore and stored in the app's no-backup directory. The
 * WorkRequest UUID is authenticated with the ciphertext and is the only reference passed to the
 * worker.
 */
public final class ImportBackupSecretStorage {

    private static final String FILE_PREFIX = "backup-import-secret-";
    private static final String AAD_COLUMN = "backup-passphrase";
    private static final int MAX_ENVELOPE_BYTES = 128 * 1024;

    private final File directory;
    private final AccountSecretStorage accountSecretStorage;

    public ImportBackupSecretStorage(final Context context) {
        this(context.getApplicationContext().getNoBackupFilesDir(), new AccountSecretStorage());
    }

    @VisibleForTesting
    ImportBackupSecretStorage(
            final File directory, final AccountSecretStorage accountSecretStorage) {
        this.directory = directory;
        this.accountSecretStorage = accountSecretStorage;
    }

    public void store(final UUID workRequestId, final String cleartext)
            throws GeneralSecurityException, IOException {
        if (cleartext == null || cleartext.isEmpty()) {
            throw new IllegalArgumentException("Backup passphrase must not be empty");
        }
        final String encrypted =
                accountSecretStorage.encrypt(workRequestId.toString(), AAD_COLUMN, cleartext);
        final byte[] encoded = encrypted.getBytes(StandardCharsets.UTF_8);
        if (encoded.length > MAX_ENVELOPE_BYTES) {
            throw new IOException("Encrypted backup passphrase is too large");
        }
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Could not create backup secret directory");
        }
        final AtomicFile atomicFile = atomicFile(workRequestId);
        FileOutputStream outputStream = null;
        try {
            outputStream = atomicFile.startWrite();
            outputStream.write(encoded);
            atomicFile.finishWrite(outputStream);
        } catch (final IOException | RuntimeException e) {
            if (outputStream != null) {
                atomicFile.failWrite(outputStream);
            }
            throw e;
        }
    }

    public String read(final UUID workRequestId) throws GeneralSecurityException, IOException {
        final byte[] encoded;
        try (final FileInputStream inputStream = atomicFile(workRequestId).openRead()) {
            encoded = ByteStreams.toByteArray(inputStream);
        }
        if (encoded.length == 0 || encoded.length > MAX_ENVELOPE_BYTES) {
            throw new IOException("Invalid encrypted backup passphrase size");
        }
        return accountSecretStorage.decrypt(
                workRequestId.toString(), AAD_COLUMN, new String(encoded, StandardCharsets.UTF_8));
    }

    /** Deletes the primary and any recovery copy left by an interrupted atomic write. */
    public void delete(final UUID workRequestId) {
        atomicFile(workRequestId).delete();
    }

    @VisibleForTesting
    File file(final UUID workRequestId) {
        return atomicFile(workRequestId).getBaseFile();
    }

    private AtomicFile atomicFile(final UUID workRequestId) {
        return new AtomicFile(new File(directory, FILE_PREFIX + workRequestId));
    }
}
