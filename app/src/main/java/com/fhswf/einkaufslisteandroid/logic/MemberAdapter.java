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
 * Adapter für die Anzeige der Mitglieder "Mail-Adressen" in einer RecyclerView.
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
     * Diese Methode wird aufgerufen, um ein neues ViewHolder zu erstellen.
     * @param parent Die übergeordnete ViewGroup, in der die neue ViewHolder eingefügt wird.
     * @param viewType Typ der Ansicht.
     *
     * @return Ein neues ViewHolder für die Anzeige eines E-Mails.
     */
    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.member_list, parent, false);
        return new MemberViewHolder(view);
    }

    /**
     * Bindet die Daten an eine View und stellt die enstprechenden E-Mail dar.
     * @param holder Ist der ViewHolder, der die Ansicht enthält.
     * @param position Postion des Elements in der Liste.
     */
    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        holder.emailTextView.setText(emails.get(position));
    }

    /**
     * Gibt die Anzahl der E-Mails in der Liste zurück.
     * @return Die Anzahl der E-Mails.
     */
    @Override
    public int getItemCount() {
        return emails.size();
    }

    /**
     * ViewHolder für die Anzeige einer einzelnen E-Mail.
     */
    public static class MemberViewHolder extends RecyclerView.ViewHolder {
        TextView emailTextView;

        /**
         * Konstruktor für den ViewHolder.
         * @param itemView Die View des einzelnen Elements der Liste.
         */
        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            emailTextView = itemView.findViewById(R.id.textViewEmail);
        }
    }
}
