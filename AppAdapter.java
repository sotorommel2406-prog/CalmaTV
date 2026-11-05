package com.relax.tvlauncher;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.VH> {

    public interface Listener {
        void onAppClick(AppEntry entry);
        void onAppLongClick(AppEntry entry);
    }

    private final List<AppEntry> items = new ArrayList<>();
    private final Listener listener;

    public AppAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<AppEntry> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        AppEntry e = items.get(position);
        h.icon.setImageDrawable(e.icon);
        h.label.setText(e.label);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class VH extends RecyclerView.ViewHolder {
        final ImageView icon;
        final TextView label;

        VH(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.app_icon);
            label = itemView.findViewById(R.id.app_label);

            itemView.setOnClickListener(v -> {
                int p = getBindingAdapterPosition();
                if (p != RecyclerView.NO_POSITION) listener.onAppClick(items.get(p));
            });
            itemView.setOnLongClickListener(v -> {
                int p = getBindingAdapterPosition();
                if (p != RecyclerView.NO_POSITION) listener.onAppLongClick(items.get(p));
                return true;
            });
            // Efecto suave de "flotar" al enfocar con el control remoto
            itemView.setOnFocusChangeListener((v, hasFocus) ->
                    v.animate()
                            .scaleX(hasFocus ? 1.1f : 1f)
                            .scaleY(hasFocus ? 1.1f : 1f)
                            .setDuration(140)
                            .start());
        }
    }
}
