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
 * Dieser Adapter übernimmt eine Liste von E-Mail-Adressen und sorgt dafür, dass jede E-Mail in
 * einem eigenen Listenelement angezeigt wird.
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

    /**
     * Hier wird das Layout für ein einzelnes Listenelement (Member) aufgeblasen.
     * @param parent Die übergeordnete ViewGroup, in der das Element später angezeigt wird.
     * @param viewType Der Typ der Ansicht
     *
     * @return einen neuen MemberViewHolder, der das aufgeblasene Layout enthält.
     */
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
