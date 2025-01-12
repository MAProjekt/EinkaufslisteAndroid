package com.fhswf.einkaufslisteandroid.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.fhswf.einkaufslisteandroid.R;
import com.fhswf.einkaufslisteandroid.datenpersistierung.JsonListManager;
import com.fhswf.einkaufslisteandroid.logic.ListAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1"; // vordef. Parameter
    private static final String ARG_PARAM2 = "param2";// vordef. Parameter

    // TODO: Rename and change types of parameters
    private String mParam1; // vordef. Parameter
    private String mParam2; // vordef. Parameter

    public HomeFragment() {
    }

//    /**
//     * Use this factory method to create a new instance of
//     * this fragment using the provided parameters.
//     *
//     * @param param1 Parameter 1.
//     * @param param2 Parameter 2.
//     * @return A new instance of fragment HomeFragment.
//     */
//    // TODO: Rename and change types and number of parameters
//    public static HomeFragment newInstance(String param1, String param2) {
//        HomeFragment fragment = new HomeFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
//        return fragment;
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    /**
     * Methode wurde ergänzt, dient als Gegenstück liest die JSON aus in eine Liste der Produkte
     * und listet die Listen dann in der App auf.
     *
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.ViewLists);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Listen aus JSON laden
        List<String> listNames = JsonListManager.loadListsFromJSON(getContext());
        ListAdapter adapter = new ListAdapter(listNames, this::onListClicked);
        recyclerView.setAdapter(adapter);

        return view;
    }

    /**
     * Hilfsmethode für die onCreateView + sollte noch ausgelagert werden
     * @return
     */
//    private List<String> loadListsFromJSON() {
//        List<String> listNames = new ArrayList<>();
//        File file = new File(requireContext().getFilesDir(), "listen.json");
//
//        if (file.exists()) {
//            try {
//                String content = new String(Files.readAllBytes(file.toPath()));
//                JSONArray listsArray = new JSONArray(content);
//                for (int i = 0; i < listsArray.length(); i++) {
//                    JSONObject listObject = listsArray.getJSONObject(i);
//                    listNames.add(listObject.getString("listName"));
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//
//        return listNames;
//    }

    /**
     * Methode wenn eine Liste angeklickt wird.
     * @param listName
     */
    private void onListClicked(String listName) {
        Toast.makeText(getContext(), "Liste ausgewählt: " + listName, Toast.LENGTH_SHORT).show();
        // Hier kannst du Produkte der Liste anzeigen lassen


    }



}