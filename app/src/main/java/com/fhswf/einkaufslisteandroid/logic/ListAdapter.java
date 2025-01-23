// Quelle: https://developer.android.com/develop/ui/views/layout/recyclerview?hl=de

package com.fhswf.einkaufslisteandroid.logic;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Klasse welche die Namen der Einkaufslisten anzeigt und sie durch ein Klick auf den Namen aufrufbar macht.
 * Die Einkaufslisten-Namen werden im HomeFragment angezeigt.
 */
public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

    private final List<String> listNames;
    private final OnListClickListener listener;

    /**
     * Interface, wenn auf ein Listenelement geklickt wird.
     */
    public interface OnListClickListener {
        void onListClick(String listName);
    }


    /**
     * Konstruktor für den ListAdapter.
     *
     * @param listNames Liste der Namen, die angezeigt werden sollen.
     * @param listener Listener für Klick-Ereignisse auf Listenelementen.
     */
    public ListAdapter(List<String> listNames, OnListClickListener listener) {
        this.listNames = listNames;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String listName = listNames.get(position);
        holder.textView.setText(listName);
        holder.itemView.setOnClickListener(v -> listener.onListClick(listName));
    }

    /**
     * Gibt die Anzahl der Elemente in der Liste zurück.
     *
     * @return Die Anzahl der Listenelemente.
     */
    @Override
    public int getItemCount() {
        return listNames.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }
    }
}
