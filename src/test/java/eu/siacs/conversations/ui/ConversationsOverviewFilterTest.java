package eu.siacs.conversations.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import eu.siacs.conversations.R;
import eu.siacs.conversations.utils.MaerCallInvite;
import org.junit.Test;

public class ConversationsOverviewFilterTest {

    @Test
    public void allFilterIncludesEveryConversation() {
        assertTrue(
                ConversationsOverviewFragment.matchesFilter(
                        ConversationsOverviewFragment.ConversationFilter.ALL,
                        true,
                        false,
                        false,
                        false));
    }

    @Test
    public void unreadFilterOnlyIncludesUnreadConversations() {
        assertTrue(
                ConversationsOverviewFragment.matchesFilter(
                        ConversationsOverviewFragment.ConversationFilter.UNREAD,
                        false,
                        false,
                        false,
                        false));
        assertFalse(
                ConversationsOverviewFragment.matchesFilter(
                        ConversationsOverviewFragment.ConversationFilter.UNREAD,
                        true,
                        false,
                        false,
                        false));
    }

    @Test
    public void groupFilterOnlyIncludesGroupConversations() {
        assertTrue(
                ConversationsOverviewFragment.matchesFilter(
                        ConversationsOverviewFragment.ConversationFilter.GROUPS,
                        true,
                        true,
                        false,
                        false));
        assertFalse(
                ConversationsOverviewFragment.matchesFilter(
                        ConversationsOverviewFragment.ConversationFilter.GROUPS,
                        false,
                        false,
                        false,
                        false));
    }

    @Test
    public void favoritesFilterUsesPinnedState() {
        assertTrue(
                ConversationsOverviewFragment.matchesFilter(
                        ConversationsOverviewFragment.ConversationFilter.FAVORITES,
                        true,
                        false,
                        true,
                        false));
        assertFalse(
                ConversationsOverviewFragment.matchesFilter(
                        ConversationsOverviewFragment.ConversationFilter.FAVORITES,
                        false,
                        true,
                        false,
                        false));
    }

    @Test
    public void callsFilterOnlyIncludesConversationsWithCallHistory() {
        assertTrue(
                ConversationsOverviewFragment.matchesFilter(
                        ConversationsOverviewFragment.ConversationFilter.CALLS,
                        true,
                        false,
                        false,
                        true));
        assertFalse(
                ConversationsOverviewFragment.matchesFilter(
                        ConversationsOverviewFragment.ConversationFilter.CALLS,
                        false,
                        false,
                        false,
                        false));
    }

    @Test
    public void callsFilterUsesCallFabPresentation() {
        assertEquals(
                R.string.start_call,
                ConversationsOverviewFragment.fabLabelForFilter(
                        ConversationsOverviewFragment.ConversationFilter.CALLS));
        assertEquals(
                R.drawable.ic_call_24dp,
                ConversationsOverviewFragment.fabIconForFilter(
                        ConversationsOverviewFragment.ConversationFilter.CALLS));
    }

    @Test
    public void callTypeChoicesKeepNativeJingleAndAddMaerMeetings() {
        assertEquals(
                RtpSessionActivity.ACTION_MAKE_VOICE_CALL,
                ConversationsOverviewFragment.callActionForChoice(0));
        assertEquals(
                RtpSessionActivity.ACTION_MAKE_VIDEO_CALL,
                ConversationsOverviewFragment.callActionForChoice(1));
        assertEquals(
                MaerCallInvite.Mode.AUDIO,
                ConversationsOverviewFragment.maerMeetingModeForAction(
                        ConversationsOverviewFragment.callActionForChoice(2)));
        assertEquals(
                MaerCallInvite.Mode.VIDEO,
                ConversationsOverviewFragment.maerMeetingModeForAction(
                        ConversationsOverviewFragment.callActionForChoice(3)));
        assertFalse(
                ConversationsOverviewFragment.isMaerMeetingAction(
                        RtpSessionActivity.ACTION_MAKE_VIDEO_CALL));
        assertThrows(
                IllegalArgumentException.class,
                () -> ConversationsOverviewFragment.callActionForChoice(4));
    }
}
