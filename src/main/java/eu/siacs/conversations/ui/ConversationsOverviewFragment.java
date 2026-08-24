/*
 * Copyright (c) 2018, Daniel Gultsch All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package eu.siacs.conversations.ui;

import static androidx.recyclerview.widget.ItemTouchHelper.LEFT;
import static androidx.recyclerview.widget.ItemTouchHelper.RIGHT;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuProvider;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.search.SearchView;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import de.gultsch.common.MiniUri;
import de.gultsch.common.Patterns;
import eu.siacs.conversations.AppSettings;
import eu.siacs.conversations.BuildConfig;
import eu.siacs.conversations.Config;
import eu.siacs.conversations.R;
import eu.siacs.conversations.databinding.FragmentConversationsOverviewBinding;
import eu.siacs.conversations.entities.Account;
import eu.siacs.conversations.entities.Contact;
import eu.siacs.conversations.entities.Conversation;
import eu.siacs.conversations.entities.Conversational;
import eu.siacs.conversations.services.CallIntegrationConnectionService;
import eu.siacs.conversations.services.QuickConversationsService;
import eu.siacs.conversations.ui.activity.SettingsActivity;
import eu.siacs.conversations.ui.adapter.ConversationAdapter;
import eu.siacs.conversations.ui.adapter.SearchSuggestionAdapter;
import eu.siacs.conversations.ui.interfaces.OnConversationArchived;
import eu.siacs.conversations.ui.interfaces.OnConversationSelected;
import eu.siacs.conversations.ui.util.AvatarWorkerTask;
import eu.siacs.conversations.ui.util.PendingActionHelper;
import eu.siacs.conversations.ui.util.PendingItem;
import eu.siacs.conversations.ui.util.PresenceSelector;
import eu.siacs.conversations.ui.util.ScrollState;
import eu.siacs.conversations.ui.widget.AccountPickerDialog;
import eu.siacs.conversations.utils.AccountUtils;
import eu.siacs.conversations.utils.CharSequences;
import eu.siacs.conversations.utils.XmppUriLauncher;
import eu.siacs.conversations.xmpp.Jid;
import eu.siacs.conversations.xmpp.jingle.RtpCapability;
import eu.siacs.conversations.xmpp.manager.BookmarkManager;
import eu.siacs.conversations.xmpp.manager.JingleManager;
import eu.siacs.conversations.xmpp.manager.RosterManager;
import im.conversations.android.model.SearchSuggestion;
import im.conversations.android.provider.SearchSuggestionProvider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ConversationsOverviewFragment extends XmppFragment {

    private static final String STATE_SCROLL_POSITION =
            ConversationsOverviewFragment.class.getName() + ".scroll_state";
    private static final String STATE_FILTER =
            ConversationsOverviewFragment.class.getName() + ".conversation_filter";
    private static final String STATE_PENDING_CALL_ACTION =
            ConversationsOverviewFragment.class.getName() + ".pending_call_action";
    private static final String STATE_PENDING_CALL_ACCOUNT =
            ConversationsOverviewFragment.class.getName() + ".pending_call_account";
    private static final String STATE_PENDING_CALL_CONTACT =
            ConversationsOverviewFragment.class.getName() + ".pending_call_contact";

    enum ConversationFilter {
        ALL,
        UNREAD,
        GROUPS,
        FAVORITES,
        CALLS
    }

    private final List<Conversation> allConversations = new ArrayList<>();
    private final List<Conversation> conversations = new ArrayList<>();
    private final PendingItem<Conversation> swipedConversation = new PendingItem<>();
    private final PendingItem<ScrollState> pendingScrollState = new PendingItem<>();
    private FragmentConversationsOverviewBinding binding;
    private ConversationAdapter conversationsAdapter;
    private SearchSuggestionAdapter searchSuggestionAdapter;
    private final PendingActionHelper pendingActionHelper = new PendingActionHelper();
    private String pendingCallAction;
    private String pendingCallAccount;
    private Jid pendingCallContact;

    private final ActivityResultLauncher<Intent> callContactLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        final Intent data = result.getData();
                        if (result.getResultCode() != Activity.RESULT_OK || data == null) {
                            clearPendingCall();
                            return;
                        }
                        final List<Jid> contacts = ChooseContactActivity.extractJabberIds(data);
                        final String account =
                                data.getStringExtra(ChooseContactActivity.EXTRA_ACCOUNT);
                        if (contacts.isEmpty() || account == null || pendingCallAction == null) {
                            clearPendingCall();
                            return;
                        }
                        this.pendingCallAccount = account;
                        this.pendingCallContact = contacts.get(0).asBareJid();
                        requestPendingCallPermissions();
                    });

    private final ActivityResultLauncher<String[]> callPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestMultiplePermissions(),
                    permissions -> {
                        if (!isPermissionGranted(permissions, Manifest.permission.RECORD_AUDIO)) {
                            showCallPermissionDenied(R.string.no_microphone_permission);
                            return;
                        }
                        if (RtpSessionActivity.ACTION_MAKE_VIDEO_CALL.equals(pendingCallAction)
                                && !isPermissionGranted(permissions, Manifest.permission.CAMERA)) {
                            showCallPermissionDenied(R.string.no_camera_permission);
                            return;
                        }
                        placePendingCall();
                    });

    private final ItemTouchHelper.SimpleCallback callback =
            new ItemTouchHelper.SimpleCallback(0, LEFT | RIGHT) {
                @Override
                public boolean onMove(
                        @NonNull RecyclerView recyclerView,
                        @NonNull RecyclerView.ViewHolder viewHolder,
                        @NonNull RecyclerView.ViewHolder target) {
                    return false;
                }

                @Override
                public int getSwipeDirs(
                        @NonNull RecyclerView recyclerView,
                        @NonNull RecyclerView.ViewHolder viewHolder) {
                    return currentFilter == ConversationFilter.CALLS
                            ? 0
                            : super.getSwipeDirs(recyclerView, viewHolder);
                }

                @Override
                public void onChildDraw(
                        @NonNull Canvas c,
                        @NonNull RecyclerView recyclerView,
                        @NonNull RecyclerView.ViewHolder viewHolder,
                        float dX,
                        float dY,
                        int actionState,
                        boolean isCurrentlyActive) {
                    if (viewHolder
                            instanceof
                            ConversationAdapter.ConversationViewHolder conversationViewHolder) {
                        getDefaultUIUtil()
                                .onDraw(
                                        c,
                                        recyclerView,
                                        conversationViewHolder.binding.frame,
                                        dX,
                                        dY,
                                        actionState,
                                        isCurrentlyActive);
                    }
                }

                @Override
                public void clearView(
                        @NonNull RecyclerView recyclerView,
                        @NonNull RecyclerView.ViewHolder viewHolder) {
                    if (viewHolder
                            instanceof
                            ConversationAdapter.ConversationViewHolder conversationViewHolder) {
                        getDefaultUIUtil().clearView(conversationViewHolder.binding.frame);
                    }
                }

                @Override
                public float getSwipeEscapeVelocity(final float defaultEscapeVelocity) {
                    return 32 * defaultEscapeVelocity;
                }

                @Override
                public void onSwiped(
                        final RecyclerView.ViewHolder viewHolder, final int direction) {
                    int position = viewHolder.getLayoutPosition();
                    final Conversation conversation;
                    try {
                        conversation = conversations.get(position);
                    } catch (final IndexOutOfBoundsException e) {
                        return;
                    }
                    onConversationSwiped(conversation, position);
                }
            };
    private final MenuProvider globalMenuProvider =
            new MenuProvider() {
                @Override
                public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                    menuInflater.inflate(R.menu.fragment_global, menu);
                    AccountUtils.showHideMenuItems(menu);
                }

                @Override
                public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                    final var id = menuItem.getItemId();
                    if (id == R.id.action_settings) {
                        startActivity(new Intent(requireContext(), SettingsActivity.class));
                        return true;
                    } else if (id == R.id.action_accounts) {
                        AccountUtils.launchManageAccounts(requireXmppActivity());
                        return true;
                    } else if (id == R.id.action_account) {
                        AccountUtils.launchManageAccount(requireXmppActivity());
                        return true;
                    } else {
                        return false;
                    }
                }
            };

    private final MenuProvider menuProvider =
            new MenuProvider() {
                @Override
                public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                    menuInflater.inflate(R.menu.fragment_conversations_overview, menu);
                    final MenuItem privacyPolicyMenuItem =
                            menu.findItem(R.id.action_privacy_policy);
                    privacyPolicyMenuItem.setVisible(
                            BuildConfig.PRIVACY_POLICY != null
                                    && QuickConversationsService.isPlayStoreFlavor());
                    final var qrCodeActions = menu.findItem(R.id.action_qr_codes);
                    final var qrCodeScanMenuItem = menu.findItem(R.id.action_scan_qr_code);
                    final var showQrCodeMenuItem = menu.findItem(R.id.action_show_qr_code);
                    final var easyOnboardInvite = menu.findItem(R.id.action_easy_invite);
                    qrCodeActions.setVisible(true);
                    qrCodeScanMenuItem.setVisible(requireXmppActivity().isCameraFeatureAvailable());
                    showQrCodeMenuItem.setVisible(
                            new AccountPickerDialog.Enabled(requireXmppActivity())
                                    .hasAnyAccounts());
                    easyOnboardInvite.setVisible(
                            new AccountPickerDialog.EasyInvite(requireXmppActivity())
                                    .hasAnyAccounts());
                }

                @Override
                public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {

                    final var id = menuItem.getItemId();
                    if (id == R.id.action_easy_invite) {
                        selectAccountToStartEasyInvite();
                        return true;
                    } else if (id == R.id.action_show_qr_code) {
                        new AccountPickerDialog.Enabled(requireXmppActivity())
                                .pick(a -> requireXmppActivity().showQrCode(a));
                        return true;
                    } else if (id == R.id.action_scan_qr_code) {
                        if (requireActivity()
                                instanceof QrCodeScanningActivity qrCodeScanningActivity) {
                            qrCodeScanningActivity.requestPermissionAndScanQrCode();
                        }
                        return true;
                    } else if (id == R.id.action_privacy_policy) {
                        openPrivacyPolicy();
                        return true;
                    } else {
                        return false;
                    }
                }
            };
    private final OnBackPressedCallback searchViewOnBackPressedCallback =
            new OnBackPressedCallback(false) {
                @Override
                public void handleOnBackPressed() {
                    binding.searchView.hide();
                }
            };

    private void openPrivacyPolicy() {
        if (BuildConfig.PRIVACY_POLICY == null) {
            return;
        }
        final var viewPolicyIntent = new Intent(Intent.ACTION_VIEW);
        viewPolicyIntent.setData(Uri.parse(BuildConfig.PRIVACY_POLICY));
        try {
            startActivity(viewPolicyIntent);
        } catch (final ActivityNotFoundException e) {
            Toast.makeText(
                            requireContext(),
                            R.string.no_application_found_to_open_link,
                            Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private void onConversationSwiped(final Conversation c, final int position) {
        pendingActionHelper.execute();
        this.swipedConversation.push(c);
        conversationsAdapter.remove(swipedConversation.peek(), position);
        toggleHintVisibility();
        requireXmppActivity().xmppConnectionService.markRead(swipedConversation.peek());
        final boolean formerlySelected =
                ConversationFragment.getConversation(getActivity()) == swipedConversation.peek();
        if (getActivity() instanceof OnConversationArchived callback) {
            callback.onConversationArchived(swipedConversation.peek());
        }
        final int title;
        if (c.getMode() == Conversational.MODE_MULTI) {
            if (c.getMucOptions().isPrivateAndNonAnonymous()) {
                title = R.string.title_undo_swipe_out_group_chat;
            } else {
                title = R.string.title_undo_swipe_out_channel;
            }
        } else {
            title = R.string.title_undo_swipe_out_chat;
        }

        final Snackbar snackbar =
                Snackbar.make(binding.list, title, 5000)
                        .setAction(R.string.undo, v -> undoSwipe(formerlySelected, position))
                        .addCallback(
                                new Snackbar.Callback() {
                                    @Override
                                    public void onDismissed(
                                            Snackbar transientBottomBar, int event) {
                                        switch (event) {
                                            case DISMISS_EVENT_SWIPE:
                                            case DISMISS_EVENT_TIMEOUT:
                                                pendingActionHelper.execute();
                                                break;
                                        }
                                    }
                                });

        pendingActionHelper.push(
                () -> {
                    if (snackbar.isShownOrQueued()) {
                        snackbar.dismiss();
                    }
                    final Conversation conversation = swipedConversation.pop();
                    if (conversation == null) {
                        return;
                    }
                    if (!conversation.isRead()
                            && conversation.getMode() == Conversation.MODE_SINGLE) {
                        return;
                    }
                    requireXmppActivity().xmppConnectionService.archiveConversation(c);
                });
        snackbar.show();
    }

    private void undoSwipe(final boolean formerlySelected, final int position) {
        pendingActionHelper.undo();
        final var c = swipedConversation.pop();
        if (!conversations.contains(c)) {
            conversationsAdapter.insert(c, position);
        }
        toggleHintVisibility();
        if (formerlySelected) {
            if (getActivity() instanceof OnConversationSelected on) {
                on.onConversationSelected(c);
            }
        }
        if (binding.list.getLayoutManager() instanceof LinearLayoutManager llm
                && position > llm.findLastVisibleItemPosition()) {
            binding.list.smoothScrollToPosition(position);
        }
    }

    private ItemTouchHelper touchHelper;

    public static Conversation getSuggestion(final FragmentActivity activity) {
        final Conversation exception;
        Fragment fragment =
                activity.getSupportFragmentManager().findFragmentById(R.id.main_fragment);
        if (fragment instanceof ConversationsOverviewFragment conversationsOverviewFragment) {
            exception = conversationsOverviewFragment.swipedConversation.peek();
        } else {
            exception = null;
        }
        return getSuggestion(activity, exception);
    }

    public static @Nullable Conversation getSuggestion(
            final FragmentActivity activity, final Conversation exception) {
        Fragment fragment =
                activity.getSupportFragmentManager().findFragmentById(R.id.main_fragment);
        if (fragment instanceof ConversationsOverviewFragment conversationsOverviewFragment) {
            final var conversations = conversationsOverviewFragment.conversations;
            final var filtered = Collections2.filter(conversations, c -> c != exception);
            if (filtered.isEmpty()) {
                return null;
            } else {
                return Iterables.getFirst(filtered, null);
            }
        }
        return null;
    }

    public void onViewCreated(@NonNull final View view, final Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            return;
        }
        pendingScrollState.push(savedInstanceState.getParcelable(STATE_SCROLL_POSITION));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (this.binding != null) {
            this.binding.conversationFilters.setOnCheckedStateChangeListener(null);
            this.binding.homeNavigation.setOnItemSelectedListener(null);
        }
        this.binding = null;
        this.conversationsAdapter = null;
        this.searchSuggestionAdapter = null;
        this.touchHelper = null;
    }

    @Override
    public void onPause() {
        pendingActionHelper.execute();
        super.onPause();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            final String savedFilter = savedInstanceState.getString(STATE_FILTER);
            if (savedFilter != null) {
                try {
                    this.currentFilter = ConversationFilter.valueOf(savedFilter);
                } catch (final IllegalArgumentException ignored) {
                    this.currentFilter = ConversationFilter.ALL;
                }
            }
            this.pendingCallAction = savedInstanceState.getString(STATE_PENDING_CALL_ACTION);
            this.pendingCallAccount = savedInstanceState.getString(STATE_PENDING_CALL_ACCOUNT);
            final String pendingContact = savedInstanceState.getString(STATE_PENDING_CALL_CONTACT);
            if (pendingContact != null) {
                try {
                    this.pendingCallContact = Jid.of(pendingContact);
                } catch (final IllegalArgumentException ignored) {
                    clearPendingCall();
                }
            }
        }
        requireActivity()
                .getOnBackPressedDispatcher()
                .addCallback(this, this.searchViewOnBackPressedCallback);
    }

    @Override
    public View onCreateView(
            @NonNull final LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {
        this.binding =
                DataBindingUtil.inflate(
                        inflater, R.layout.fragment_conversations_overview, container, false);
        this.binding.searchBar.addMenuProvider(
                menuProvider, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
        this.binding.searchBar.addMenuProvider(
                globalMenuProvider, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
        this.binding
                .searchView
                .getEditText()
                .setOnEditorActionListener(
                        (v, actionId, event) -> {
                            startSearch(CharSequences.nullToEmpty(v.getText()));
                            this.binding.searchView.hide();
                            return true;
                        });
        this.binding
                .searchView
                .getEditText()
                .addTextChangedListener(new TextChangeListener(this::submitSearchSuggestion));
        this.binding.searchView.addTransitionListener(
                (searchView, oldState, newState) -> {
                    final boolean isShowing =
                            Arrays.asList(
                                            SearchView.TransitionState.SHOWING,
                                            SearchView.TransitionState.SHOWN)
                                    .contains(newState);
                    searchViewOnBackPressedCallback.setEnabled(isShowing);
                });
        this.binding.fab.setOnClickListener(
                view -> {
                    if (this.currentFilter == ConversationFilter.CALLS) {
                        showCallTypeChooser();
                    } else {
                        StartConversationActivity.launch(getActivity());
                    }
                });
        this.binding.profileAvatar.setOnClickListener(
                view -> {
                    final var service = requireXmppActivity().xmppConnectionService;
                    if (service == null) {
                        return;
                    }
                    final Account account = AccountUtils.getFirstEnabled(service);
                    if (account != null) {
                        requireXmppActivity().switchToAccount(account);
                    }
                });
        this.binding.conversationFilters.check(chipIdForFilter(this.currentFilter));
        this.binding.conversationFilters.setOnCheckedStateChangeListener(
                (group, checkedIds) -> {
                    if (checkedIds.isEmpty()) {
                        return;
                    }
                    this.currentFilter = filterForChip(checkedIds.get(0));
                    applyConversationFilter();
                });
        this.binding.homeNavigation.setOnItemSelectedListener(this::onHomeNavigationSelected);
        final int initialNavigation =
                this.currentFilter == ConversationFilter.CALLS
                        ? R.id.nav_calls
                        : requireActivity() instanceof ConversationsActivity conversationsActivity
                                ? conversationsActivity.consumeInitialHomeNavigation()
                                : R.id.nav_chats;
        this.binding.homeNavigation.setSelectedItemId(initialNavigation);

        this.conversationsAdapter =
                new ConversationAdapter(requireXmppActivity(), this.conversations);
        this.conversationsAdapter.setMessageProvider(
                conversation ->
                        this.currentFilter == ConversationFilter.CALLS
                                ? conversation.getLatestRtpSession()
                                : conversation.getLatestMessage());
        this.conversationsAdapter.setConversationClickListener(
                (view, conversation) -> {
                    if (getActivity() instanceof OnConversationSelected callback) {
                        callback.onConversationSelected(conversation);
                    } else {
                        Log.w(
                                ConversationsOverviewFragment.class.getCanonicalName(),
                                "Activity does not implement OnConversationSelected");
                    }
                });
        this.searchSuggestionAdapter = new SearchSuggestionAdapter();
        this.binding.list.setAdapter(this.conversationsAdapter);
        this.binding.list.setLayoutManager(
                new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        this.binding.list.addOnScrollListener(ExtendedFabSizeChanger.of(binding.fab));
        this.binding.searchSuggestionList.setAdapter(this.searchSuggestionAdapter);
        this.searchSuggestionAdapter.setOnSearchSuggestionClicked(this::executeSuggestion);
        this.touchHelper = new ItemTouchHelper(this.callback);
        this.touchHelper.attachToRecyclerView(this.binding.list);
        return binding.getRoot();
    }

    void selectHomeNavigationItem(@IdRes final int itemId) {
        if (this.binding == null) {
            this.currentFilter =
                    itemId == R.id.nav_calls ? ConversationFilter.CALLS : ConversationFilter.ALL;
            return;
        }
        this.binding.homeNavigation.setSelectedItemId(itemId);
    }

    private boolean onHomeNavigationSelected(@NonNull final MenuItem item) {
        final int id = item.getItemId();
        if (id == R.id.nav_chats) {
            if (this.currentFilter == ConversationFilter.CALLS) {
                this.currentFilter = ConversationFilter.ALL;
                this.binding.conversationFilters.check(R.id.filter_all);
            }
            updateHomeSectionUi();
            applyConversationFilter();
            return true;
        } else if (id == R.id.nav_contacts) {
            StartConversationActivity.launch(requireContext(), 0);
            return false;
        } else if (id == R.id.nav_groups) {
            StartConversationActivity.launch(requireContext(), 1);
            return false;
        } else if (id == R.id.nav_calls) {
            this.currentFilter = ConversationFilter.CALLS;
            updateHomeSectionUi();
            applyConversationFilter();
            return true;
        }
        return false;
    }

    private void updateHomeSectionUi() {
        final boolean calls = this.currentFilter == ConversationFilter.CALLS;
        this.binding.messageFilters.setVisibility(calls ? View.GONE : View.VISIBLE);
        this.binding.fab.setVisibility(View.VISIBLE);
        this.binding.fab.setText(fabLabelForFilter(this.currentFilter));
        this.binding.fab.setContentDescription(getString(fabLabelForFilter(this.currentFilter)));
        this.binding.fab.setIconResource(fabIconForFilter(this.currentFilter));
        this.binding.searchBar.setHint(calls ? R.string.search_calls : R.string.search_in_chats);
    }

    private void showCallTypeChooser() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.start_call)
                .setItems(
                        new CharSequence[] {
                            getString(R.string.audio_call), getString(R.string.video_call)
                        },
                        (dialog, which) -> launchCallContactChooser(callActionForChoice(which)))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void launchCallContactChooser(final String action) {
        this.pendingCallAction = action;
        this.pendingCallAccount = null;
        this.pendingCallContact = null;
        final Intent intent = new Intent(requireContext(), ChooseContactActivity.class);
        intent.putExtra(ChooseContactActivity.EXTRA_SHOW_ENTER_JID, true);
        intent.putExtra(ChooseContactActivity.EXTRA_TITLE_RES_ID, R.string.choose_call_contact);
        this.callContactLauncher.launch(intent);
    }

    private void requestPendingCallPermissions() {
        if (this.pendingCallAction == null
                || this.pendingCallAccount == null
                || this.pendingCallContact == null) {
            clearPendingCall();
            return;
        }
        final List<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.RECORD_AUDIO);
        if (RtpSessionActivity.ACTION_MAKE_VIDEO_CALL.equals(this.pendingCallAction)) {
            permissions.add(Manifest.permission.CAMERA);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT);
        }
        this.callPermissionLauncher.launch(permissions.toArray(new String[0]));
    }

    private boolean isPermissionGranted(
            final Map<String, Boolean> permissions, final String permission) {
        return ContextCompat.checkSelfPermission(requireContext(), permission)
                        == PackageManager.PERMISSION_GRANTED
                || Boolean.TRUE.equals(permissions.get(permission));
    }

    private void showCallPermissionDenied(@StringRes final int message) {
        Toast.makeText(
                        requireContext(),
                        getString(message, getString(R.string.app_name)),
                        Toast.LENGTH_SHORT)
                .show();
        clearPendingCall();
    }

    private void placePendingCall() {
        final var service = getXmppConnectionService();
        final String accountAddress = this.pendingCallAccount;
        final Jid contactAddress = this.pendingCallContact;
        final String action = this.pendingCallAction;
        clearPendingCall();
        if (service == null || accountAddress == null || contactAddress == null || action == null) {
            Toast.makeText(
                            requireContext(),
                            R.string.problem_connecting_to_account,
                            Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        final Account account;
        try {
            account = service.findAccountByJid(Jid.of(accountAddress));
        } catch (final IllegalArgumentException e) {
            Toast.makeText(
                            requireContext(),
                            R.string.problem_connecting_to_account,
                            Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        if (account == null) {
            Toast.makeText(
                            requireContext(),
                            R.string.problem_connecting_to_account,
                            Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        if (new AppSettings(requireContext()).isUseTor() || account.isOnion()) {
            Toast.makeText(requireContext(), R.string.disable_tor_to_make_call, Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        if (JingleManager.isBusy(service.getAccounts())) {
            Toast.makeText(requireContext(), R.string.only_one_call_at_a_time, Toast.LENGTH_LONG)
                    .show();
            return;
        }
        if (account.setOption(Account.OPTION_SOFT_DISABLED, false)) {
            service.updateAccount(account);
        }
        final Contact contact = account.getRoster().getContact(contactAddress);
        if (Config.USE_JINGLE_MESSAGE_INIT && RtpCapability.jmiSupport(contact)) {
            CallIntegrationConnectionService.placeCall(
                    service, account, contactAddress, RtpSessionActivity.actionToMedia(action));
            return;
        }
        final RtpCapability.Capability capability =
                RtpSessionActivity.ACTION_MAKE_VIDEO_CALL.equals(action)
                        ? RtpCapability.Capability.VIDEO
                        : RtpCapability.Capability.AUDIO;
        PresenceSelector.selectFullJidForDirectRtpConnection(
                requireActivity(),
                contact,
                capability,
                fullJid ->
                        CallIntegrationConnectionService.placeCall(
                                service,
                                account,
                                fullJid,
                                RtpSessionActivity.actionToMedia(action)));
    }

    private void clearPendingCall() {
        this.pendingCallAction = null;
        this.pendingCallAccount = null;
        this.pendingCallContact = null;
    }

    static @StringRes int fabLabelForFilter(final ConversationFilter filter) {
        return filter == ConversationFilter.CALLS ? R.string.start_call : R.string.start_chat;
    }

    static @DrawableRes int fabIconForFilter(final ConversationFilter filter) {
        return filter == ConversationFilter.CALLS
                ? R.drawable.ic_call_24dp
                : R.drawable.ic_chat_24dp;
    }

    static String callActionForChoice(final int choice) {
        return choice == 0
                ? RtpSessionActivity.ACTION_MAKE_VOICE_CALL
                : RtpSessionActivity.ACTION_MAKE_VIDEO_CALL;
    }

    private void startSearch(final String term) {
        final var intent = new Intent(requireContext(), SearchActivity.class);
        intent.putExtra(SearchActivity.EXTRA_SEARCH_TERM, term);
        startActivity(intent);
    }

    private void submitSearchSuggestion(final String raw) {
        final var search = raw.trim();
        if (Strings.isNullOrEmpty(search)) {
            this.searchSuggestionAdapter.submitList(Collections.emptyList());
            return;
        }
        final var builder = new ImmutableList.Builder<SearchSuggestion>();
        builder.add(new SearchSuggestion.Text(search));
        if (Patterns.URI_GENERIC.matcher(search).matches()) {
            final var xmppUri = MiniUri.getXmppUriOrNull(search);
            if (xmppUri != null && xmppUri.isAddress()) {
                builder.add(new SearchSuggestion.Uri(xmppUri));
            }
        }
        final var service = requireXmppActivity().xmppConnectionService;
        final List<SearchSuggestion.Sortable> suggestions;
        final boolean noteToSelf;
        if (service != null) {
            final var accounts = service.getAccounts();
            final var matchInAccount =
                    Iterables.any(
                            accounts,
                            a ->
                                    a != null
                                            && a.isEnabled()
                                            && CharSequences.containsAll(
                                                    a.getJid().asBareJid().toString(), search));
            noteToSelf =
                    matchInAccount
                            || CharSequences.containsAll(getString(R.string.note_to_self), search);
            final var provider = new SearchSuggestionProvider(accounts);
            suggestions = provider.suggest(search);
        } else {
            noteToSelf = false;
            suggestions = Collections.emptyList();
        }
        // we do not want to spam the list with tens of results of searching for individual letters
        if (suggestions.size() <= 8 || search.length() >= 3) {
            if (noteToSelf) {
                builder.add(new SearchSuggestion.Note());
            }
            builder.addAll(
                    new Ordering<SearchSuggestion.Sortable>() {
                        @Override
                        public int compare(
                                SearchSuggestion.Sortable left, SearchSuggestion.Sortable right) {
                            return left.address().compareTo(right.address());
                        }
                    }.sortedCopy(suggestions));
        }
        this.searchSuggestionAdapter.submitList(builder.build());
    }

    private void executeSuggestion(final SearchSuggestion suggestion) {
        if (suggestion instanceof SearchSuggestion.Text(String text)) {
            this.hideSearchView();
            startSearch(text);
        } else if (suggestion instanceof SearchSuggestion.Uri(MiniUri.Xmpp xmpp)) {
            this.hideSearchView();
            final var uriLauncher = new XmppUriLauncher(requireContext(), true);
            uriLauncher.launch(xmpp);
        } else if (suggestion instanceof SearchSuggestion.Bookmark b) {
            final var account =
                    requireXmppActivity().xmppConnectionService.findAccountByUuid(b.uuid());
            if (account == null) {
                return;
            }
            final var bookmark =
                    account.getXmppConnection()
                            .getManager(BookmarkManager.class)
                            .getBookmark(b.address());
            if (bookmark == null) {
                return;
            }
            this.hideSearchView();
            requireXmppActivity().openConversationsForBookmark(bookmark);
        } else if (suggestion instanceof SearchSuggestion.Contact c) {
            final var account =
                    requireXmppActivity().xmppConnectionService.findAccountByUuid(c.uuid());
            if (account == null) {
                return;
            }
            final var contact =
                    account.getXmppConnection()
                            .getManager(RosterManager.class)
                            .getContact(c.address());
            final var conversation =
                    requireXmppActivity()
                            .xmppConnectionService
                            .findOrCreateConversation(
                                    contact.getAccount(), contact.getAddress(), false, true);
            this.hideSearchView();
            requireXmppActivity().switchToConversation(conversation);
        } else if (suggestion instanceof SearchSuggestion.Note) {
            this.hideSearchView();
            final var picker = new AccountPickerDialog.Enabled(requireXmppActivity());
            picker.pick(
                    a -> {
                        final var contact = a.getSelfContact();
                        final var conversation =
                                requireXmppActivity()
                                        .xmppConnectionService
                                        .findOrCreateConversation(
                                                contact.getAccount(),
                                                contact.getAddress(),
                                                false,
                                                true);
                        requireXmppActivity().switchToConversation(conversation);
                    });
        }
    }

    private void hideSearchView() {
        if (ConversationsActivity.isTabletView(requireActivity())) {
            this.binding.searchView.hide();
        } else {
            this.binding.searchView.hide();
            this.binding.searchView.setVisible(false);
            this.binding.searchView.clearFocus();
        }
    }

    @Override
    public void onBackendConnected() {
        refresh();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle bundle) {
        super.onSaveInstanceState(bundle);
        ScrollState scrollState = getScrollState();
        if (scrollState != null) {
            bundle.putParcelable(STATE_SCROLL_POSITION, scrollState);
        }
        bundle.putString(STATE_FILTER, this.currentFilter.name());
        bundle.putString(STATE_PENDING_CALL_ACTION, this.pendingCallAction);
        bundle.putString(STATE_PENDING_CALL_ACCOUNT, this.pendingCallAccount);
        if (this.pendingCallContact != null) {
            bundle.putString(STATE_PENDING_CALL_CONTACT, this.pendingCallContact.toString());
        }
    }

    private ScrollState getScrollState() {
        if (this.binding == null) {
            return null;
        }
        if (this.binding.list.getLayoutManager()
                instanceof LinearLayoutManager linearLayoutManager) {
            final int position = linearLayoutManager.findFirstVisibleItemPosition();
            final View view = this.binding.list.getChildAt(0);
            if (view != null) {
                return new ScrollState(position, view.getTop());
            } else {
                return new ScrollState(position, 0);
            }
        }
        return null;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (requireXmppActivity().xmppConnectionService != null) {
            refresh();
        }
    }

    private void selectAccountToStartEasyInvite() {
        final var accountPicker = new AccountPickerDialog.EasyInvite(requireXmppActivity());
        accountPicker.pick(
                account -> {
                    EasyOnboardingInviteActivity.launch(account, requireContext());
                });
    }

    @Override
    void refresh() {
        if (this.binding == null) {
            Log.d(
                    Config.LOGTAG,
                    "ConversationsOverviewFragment.refresh() skipped updated because view binding"
                            + " or activity was null");
            return;
        }
        this.binding.searchBar.invalidateMenu();
        refreshProfileAvatar();
        this.requireXmppActivity()
                .xmppConnectionService
                .populateWithOrderedConversations(this.allConversations);
        Conversation removed = this.swipedConversation.peek();
        if (removed != null) {
            if (removed.isRead()) {
                this.allConversations.remove(removed);
            } else {
                pendingActionHelper.execute();
            }
        }
        applyConversationFilter();
        if (!this.conversations.isEmpty()) {
            final var scrollState = pendingScrollState.pop();
            if (scrollState != null) {
                setScrollPosition(scrollState);
            }
        }
    }

    private void refreshProfileAvatar() {
        final var service = requireXmppActivity().xmppConnectionService;
        if (service == null) {
            return;
        }
        final Account account = AccountUtils.getFirstEnabled(service);
        if (account == null) {
            this.binding.profileAvatar.setImageResource(R.drawable.ic_person_24dp);
            this.binding.profileAvatar.setBackgroundResource(R.drawable.background_profile_avatar);
            this.binding.profileAvatar.setContentDescription(getString(R.string.your_avatar));
            return;
        }
        AvatarWorkerTask.loadAvatar(
                account, this.binding.profileAvatar, R.dimen.home_profile_avatar);
    }

    private ConversationFilter currentFilter = ConversationFilter.ALL;

    private static ConversationFilter filterForChip(@IdRes final int checkedId) {
        if (checkedId == R.id.filter_unread) {
            return ConversationFilter.UNREAD;
        } else if (checkedId == R.id.filter_groups) {
            return ConversationFilter.GROUPS;
        } else if (checkedId == R.id.filter_favorites) {
            return ConversationFilter.FAVORITES;
        } else {
            return ConversationFilter.ALL;
        }
    }

    private static @IdRes int chipIdForFilter(final ConversationFilter filter) {
        return switch (filter) {
            case UNREAD -> R.id.filter_unread;
            case GROUPS -> R.id.filter_groups;
            case FAVORITES -> R.id.filter_favorites;
            case ALL, CALLS -> R.id.filter_all;
        };
    }

    private boolean matchesCurrentFilter(final Conversation conversation) {
        return matchesFilter(
                this.currentFilter,
                conversation.isRead(),
                conversation.getMode() == Conversational.MODE_MULTI,
                conversation.getBooleanAttribute(Conversation.ATTRIBUTE_PINNED_ON_TOP, false),
                conversation.getLatestRtpSession() != null);
    }

    static boolean matchesFilter(
            final ConversationFilter filter,
            final boolean read,
            final boolean group,
            final boolean favorite,
            final boolean hasCalls) {
        return switch (filter) {
            case ALL -> true;
            case UNREAD -> !read;
            case GROUPS -> group;
            case FAVORITES -> favorite;
            case CALLS -> hasCalls;
        };
    }

    private void applyConversationFilter() {
        final Conversation removed = this.swipedConversation.peek();
        this.conversations.clear();
        for (final Conversation conversation : this.allConversations) {
            if (conversation != removed && matchesCurrentFilter(conversation)) {
                this.conversations.add(conversation);
            }
        }
        if (this.conversationsAdapter != null) {
            this.conversationsAdapter.setShowDrafts(this.currentFilter != ConversationFilter.CALLS);
            this.conversationsAdapter.notifyDataSetChanged();
        }
        toggleHintVisibility();
    }

    private void toggleHintVisibility() {
        if (this.conversations.isEmpty()) {
            final boolean hasUnfilteredConversations = !this.allConversations.isEmpty();
            if (this.currentFilter == ConversationFilter.CALLS) {
                this.binding.emptyChatHintTitle.setText(R.string.no_recent_calls_title);
                this.binding.emptyChatHintText.setText(R.string.no_recent_calls_hint);
                this.binding.list.setVisibility(View.GONE);
                this.binding.emptyChatHint.setVisibility(View.VISIBLE);
                return;
            }
            this.binding.emptyChatHintTitle.setText(
                    hasUnfilteredConversations
                            ? R.string.no_filtered_conversations_title
                            : R.string.empty_conversations_title);
            this.binding.emptyChatHintText.setText(
                    hasUnfilteredConversations
                            ? R.string.no_filtered_conversations_hint
                            : R.string.empty_conversations_list_hint);
            this.binding.list.setVisibility(View.GONE);
            this.binding.emptyChatHint.setVisibility(View.VISIBLE);
        } else {
            this.binding.emptyChatHint.setVisibility(View.GONE);
            this.binding.list.setVisibility(View.VISIBLE);
        }
    }

    private void setScrollPosition(@NonNull final ScrollState scrollPosition) {
        if (binding.list.getLayoutManager() instanceof LinearLayoutManager linearLayoutManager) {
            linearLayoutManager.scrollToPositionWithOffset(
                    scrollPosition.position, scrollPosition.offset);
            if (scrollPosition.position > 0) {
                binding.fab.shrink();
            } else {
                binding.fab.extend();
            }
            binding.fab.clearAnimation();
        }
    }
}
