package eu.siacs.conversations.worker;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import eu.siacs.conversations.utils.BackupFileHeader;
import org.junit.Test;

public class BackupKeyDerivationTest {

    private static final byte[] SALT =
            new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};

    @Test
    public void currentBackupKdfIsDeterministicAndUsesAes256() throws Exception {
        final byte[] first = ExportBackupWorker.getKey("correct horse", SALT);
        final byte[] second = ExportBackupWorker.getKey("correct horse", SALT);

        assertEquals(32, first.length);
        assertArrayEquals(first, second);
    }

    @Test
    public void legacyBackupsRemainReadableWithTheirOriginalKdf() throws Exception {
        final byte[] legacy =
                ExportBackupWorker.getKey("correct horse", SALT, BackupFileHeader.LEGACY_VERSION);
        final byte[] current = ExportBackupWorker.getKey("correct horse", SALT);

        assertEquals(16, legacy.length);
        assertFalse(java.util.Arrays.equals(legacy, current));
    }
}
