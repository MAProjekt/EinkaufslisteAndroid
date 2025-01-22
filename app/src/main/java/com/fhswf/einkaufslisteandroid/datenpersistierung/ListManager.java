package com.fhswf.einkaufslisteandroid.datenpersistierung;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Auslagerungsklasse nur provisorisch zum Ausprobieren erstmal
 */
public class ListManager {

    public static List<String> loadListsFromJSON(Context context) {
        List<String> listNames = new ArrayList<>();
        File file = new File(context.getFilesDir(), "listen.json");

        if (file.exists()) {
            try {
                String content = new String(Files.readAllBytes(file.toPath()));
                JSONArray listsArray = new JSONArray(content);
                for (int i = 0; i < listsArray.length(); i++) {
                    JSONObject listObject = listsArray.getJSONObject(i);
                    listNames.add(listObject.getString("listName"));
                }
                Log.d("JSONOutput", "JSON Inhalt: " + content);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return listNames;
    }

}
