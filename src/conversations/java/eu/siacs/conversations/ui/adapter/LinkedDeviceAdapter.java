package eu.siacs.conversations.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import eu.siacs.conversations.R;
import eu.siacs.conversations.databinding.ItemLinkedDeviceBinding;
import eu.siacs.conversations.entities.LinkedDevice;
import java.text.DateFormat;
import java.time.Instant;
import java.util.Date;

public class LinkedDeviceAdapter
        extends ListAdapter<LinkedDevice, LinkedDeviceAdapter.ViewHolder> {

    private static final DiffUtil.ItemCallback<LinkedDevice> DIFF =
            new DiffUtil.ItemCallback<>() {
                @Override
                public boolean areItemsTheSame(
                        @NonNull final LinkedDevice first,
                        @NonNull final LinkedDevice second) {
                    return first.getId().equals(second.getId());
                }

                @Override
                public boolean areContentsTheSame(
                        @NonNull final LinkedDevice first,
                        @NonNull final LinkedDevice second) {
                    return first.equals(second);
                }
            };

    private final Listener listener;

    public LinkedDeviceAdapter(final Listener listener) {
        super(DIFF);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        return new ViewHolder(
                DataBindingUtil.inflate(
                        LayoutInflater.from(parent.getContext()),
                        R.layout.item_linked_device,
                        parent,
                        false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final LinkedDevice device = getItem(position);
        final var context = holder.binding.getRoot().getContext();
        final String lastSeen =
                device.getLastSeenAt() == null
                        ? context.getString(R.string.linked_device_never_seen)
                        : format(device.getLastSeenAt());
        holder.binding.label.setText(device.getLabel());
        holder.binding.details.setText(
                context.getString(
                        R.string.linked_device_details,
                        device.getPlatform(),
                        format(device.getCreatedAt()),
                        lastSeen,
                        format(device.getExpiresAt())));
        holder.binding.revoke.setContentDescription(
                context.getString(R.string.revoke_linked_device_description, device.getLabel()));
        holder.binding.revoke.setOnClickListener(view -> listener.onRevoke(device));
    }

    private static String format(final Instant instant) {
        return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                .format(Date.from(instant));
    }

    public interface Listener {
        void onRevoke(LinkedDevice device);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemLinkedDeviceBinding binding;

        private ViewHolder(final ItemLinkedDeviceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
