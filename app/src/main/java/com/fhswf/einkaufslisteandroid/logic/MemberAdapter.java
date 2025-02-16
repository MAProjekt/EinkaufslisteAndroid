package com.fhswf.einkaufslisteandroid.logic;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fhswf.einkaufslisteandroid.R;

import java.util.List;

/**
 * Adapter für die Anzeige der Mitglieder (E-Mail-Adressen) in einer RecyclerView.
 */
public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {
    private List<String> emails;
    private Context context;

    /**
     * Konstruktor für den Adapter.
     *
     * @param context Der Kontext der Anwendung.
     * @param emails  Die Liste der Mitglieds-E-Mails.
     */
    public MemberAdapter(Context context, List<String> emails) {
        this.context = context;
        this.emails = emails;
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.member_list, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        holder.emailTextView.setText(emails.get(position));
    }

    @Override
    public int getItemCount() {
        return emails.size();
    }

    public static class MemberViewHolder extends RecyclerView.ViewHolder {
        TextView emailTextView;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            emailTextView = itemView.findViewById(R.id.textViewEmail);
        }
    }
}
