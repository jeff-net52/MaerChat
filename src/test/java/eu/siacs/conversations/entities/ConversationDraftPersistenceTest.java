package eu.siacs.conversations.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.ContentValues;
import eu.siacs.conversations.xmpp.Jid;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.ConscryptMode;

@RunWith(RobolectricTestRunner.class)
@ConscryptMode(ConscryptMode.Mode.OFF)
public class ConversationDraftPersistenceTest {

    @Test
    public void draftSurvivesDatabaseSerializationAndConversationRecreation() {
        final String draftText = "Un brouillon à conserver \ud83d\udcac";
        final Conversation original = conversation("");

        assertTrue(original.setNextMessage(draftText));

        final ContentValues persistedValues = original.getContentValues();
        final Conversation restored =
                conversation(persistedValues.getAsString(Conversation.ATTRIBUTES));

        assertEquals(draftText, restored.getNextMessage());
        final Conversation.Draft restoredDraft = restored.getDraft();
        assertNotNull(restoredDraft);
        assertEquals(draftText, restoredDraft.getMessage());
        assertTrue(restoredDraft.getTimestamp() > 0);
    }

    @Test
    public void clearingDraftIsPersistedAndDoesNotRestoreWhitespace() {
        final Conversation original = conversation("");
        original.setNextMessage("Message temporaire");

        assertTrue(original.setNextMessage("  \n\t "));

        final Conversation restored =
                conversation(original.getContentValues().getAsString(Conversation.ATTRIBUTES));
        assertEquals("", restored.getNextMessage());
        assertFalse(
                restored.getContentValues()
                        .getAsString(Conversation.ATTRIBUTES)
                        .contains("Message temporaire"));
    }

    private static Conversation conversation(final String attributes) {
        return new Conversation(
                "conversation-uuid",
                "Contact",
                null,
                "account-uuid",
                Jid.of("contact@example.org"),
                1L,
                Conversation.STATUS_AVAILABLE,
                Conversation.MODE_SINGLE,
                attributes);
    }
}
