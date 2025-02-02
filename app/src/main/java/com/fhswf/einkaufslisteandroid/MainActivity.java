package com.fhswf.einkaufslisteandroid;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
//import com.fhswf.einkaufslisteandroid.datenpersistierung.FirestoreManager;
import com.fhswf.einkaufslisteandroid.datenpersistierung.FirestoreManager;
import com.fhswf.einkaufslisteandroid.fragment.GroupsFragment;
import com.fhswf.einkaufslisteandroid.fragment.HomeFragment;
import com.fhswf.einkaufslisteandroid.fragment.UebersichtFragment;
import com.fhswf.einkaufslisteandroid.logic.ProductAdapter;
import com.fhswf.einkaufslisteandroid.models.Group;
import com.fhswf.einkaufslisteandroid.models.Product;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
//import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private DrawerLayout drawerLayout; // Verwaltet das Hauptlayout des Navigation Drawers

    private FirestoreManager firestoreManager;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firestoreManager = new FirestoreManager();
        mAuth = FirebaseAuth.getInstance();

        //EdgeToEdge.enable(this);
        setContentView(com.fhswf.einkaufslisteandroid.R.layout.activity_main);
        // Farbe der Statusleiste setzen
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.default_ThemeOverlay_AppCompat));

        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.sideMenuLayout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.schliesseDrawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_view_tag, new HomeFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }

        // Teil um Produkt hinzuzufügen
        FloatingActionButton add_button = findViewById(R.id.add_button);
        add_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "FAB gedrückt!", Toast.LENGTH_SHORT).show();
                //Produkt produkt = new Produkt("Banane", "3");
            }
        });

        // User im nav_menu 1 (nav_header) anzeigen
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            View headerView = navigationView.getHeaderView(0);
            TextView nutzerNameSlideshow = headerView.findViewById(R.id.nutzerNameSlideshow);
            if (nutzerNameSlideshow != null) {
                nutzerNameSlideshow.setText(user.getEmail());
            } else {
                Toast.makeText(this, "TextView nicht gefunden.", Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        //FirebaseUser user = mAuth.getCurrentUser();

        Fragment selectedFragment = null;

        if (itemId == R.id.nav_home) {
            selectedFragment = new HomeFragment();
        } else if (itemId == R.id.nav_uebersicht) {
            selectedFragment = new UebersichtFragment();
        } else if (itemId == R.id.nav_ueber_uns) {
            selectedFragment = new GroupsFragment();
        } else if (itemId == R.id.darkmode) {
            activateDarkMode();
            return true;
        } else if (itemId == R.id.nav_logout) {
            Toast.makeText(MainActivity.this, "Erfolgreich ausgeloggt!", Toast.LENGTH_SHORT).show();

            mAuth.signOut();
            startActivity(new Intent(this, Login.class));
            finish();
        }

        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container_view_tag, selectedFragment) // Container ID aus deinem Layout
                    .commit();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }else {
            super.onBackPressed();
        }
    }

    /**
     * Methode um das zweite Menü aufzurufen und einzubinden
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.nav_menu2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (itemId == R.id.itemLogout) {
            mAuth.signOut();
            startActivity(new Intent(this, Login.class));
            finish();
            return true;
        } else if (itemId == R.id.listeHinzufuegen){
            showCreateListDialog();
            return true;
        } else if (itemId == R.id.gruppeErstellen){
            showCreateGroup();
            return true;
        } else if (itemId == R.id.gruppenListeErstellen){
            showCreateGroupListDialog();
            return true;
        }else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void activateDarkMode() {
        int currentMode = AppCompatDelegate.getDefaultNightMode();

        if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            Toast.makeText(this, "Darkmode deaktiviert", Toast.LENGTH_SHORT).show();
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            Toast.makeText(this, "Darkmode aktiviert", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Dialog in dem der User einen Namen für die Liste vergeben muss
     *
     */
    private void showCreateListDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Neue Liste erstellen");

        EditText input = new EditText(this);
        input.setHint("Listenname eingeben");
        builder.setView(input);

        builder.setPositiveButton("Erstellen", (dialog, which) -> {
            String listName = input.getText().toString().trim();
            List<Product> products = new ArrayList<>();  //<---- Liste für Produkte, das muss noch im FirestoreManager korrigiert werden
            if (!listName.isEmpty()) {
                String userId = mAuth.getCurrentUser().getUid();

                firestoreManager.saveList(userId, listName, products,
                        // Erfolgshandler
                        message -> {
                            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                            // Fragment aktualisieren, um die neue Liste anzuzeigen
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container_view_tag, new HomeFragment())
                                    .commit();
                        },
                        e -> Toast.makeText(MainActivity.this, "Fehler: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            } else {
                Toast.makeText(this, "Listenname darf nicht leer sein!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Abbrechen", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    /**
     * Methode um einen Dialog zu öffnen, bei dem der Nutzer eine Gruppe erstellen kann.
     * Notiz: Prüfung auf bereits vorhandene Namen, etwas komplex je nachdem ob wir prüfen
     * das ein User aus Gruppen die zwar nicht seine sind, aber in denen er dazugehört.
     */
    private void showCreateGroup(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Neue Gruppe erstellen");

        EditText input = new EditText(this);
        input.setHint("Gruppenname eingeben");
        builder.setView(input);

        builder.setPositiveButton("Erstellen", (dialog, which) -> {
            String groupName = input.getText().toString().trim();
            if (groupName.isEmpty()) {
                Toast.makeText(this, "Bitte einen Gruppennamen eingeben!", Toast.LENGTH_SHORT).show();
                return;
            }
            int groupId = new Random().nextInt(900000) + 100000;
            String groupIdString = String.valueOf(groupId);

            firestoreManager.createGroup(groupIdString, groupName, mAuth.getCurrentUser().getUid(),
                    successMessage -> Toast.makeText(this, successMessage, Toast.LENGTH_SHORT).show(),
                    e -> Toast.makeText(this, "Fehler: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        });

        builder.setNegativeButton("Abbrechen", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    /**
     * Methode um Liste für eine Gruppe zu erstellen
     */
    private void showCreateGroupListDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.create_groups_list, null);

        Spinner spinnerGroups = dialogView.findViewById(R.id.spinnerGroupsChoose);
        EditText editTextListName = dialogView.findViewById(R.id.editTextGroupListName);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Neue Gruppen-Liste erstellen");
        builder.setView(dialogView);

        // FÜr Ordnung nutzen diese hier
        Map<String, String> groupMap = new HashMap<>();
        // Gruppen des aktuellen Users abrufen
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        firestoreManager.getGroupsForUser(userId,
                groups -> {
                    List<String> groupNames = new ArrayList<>();
                    for (Group group : groups) {
                        groupNames.add(group.getGroupName());
                        groupMap.put(group.getGroupName(), group.getGroupId()); // Speichert Name -> ID
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_spinner_item, groupNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerGroups.setAdapter(adapter);
                },
                e -> Toast.makeText(this, "Fehler beim Laden der Gruppen: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        builder.setPositiveButton("Erstellen", (dialog, which) -> {
            String listName = editTextListName.getText().toString().trim();
            if (listName.isEmpty()) {
                Toast.makeText(this, "Der Listenname darf nicht leer sein!!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Gewählte Gruppe holen
            String selectedGroupName = (String) spinnerGroups.getSelectedItem();
            if (selectedGroupName == null) {
                Toast.makeText(this, "Es wurde keine Gruppe ausgewählt!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Die gespeicherte ID aus der Map abrufen
            String groupId = groupMap.get(selectedGroupName);
            if (groupId == null) {
                Toast.makeText(this, "Fehler: Die Gruppen-ID wurde nicht gefunden!", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d("GruppenID", groupId);
            List<Product> products = new ArrayList<>();

            // Speichere die Liste in Firestore
            firestoreManager.saveGroupList(groupId, listName, products,
                    message -> {
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container_view_tag, new HomeFragment())
                                .commit();
                    },
                    e -> Toast.makeText(this, "Fehler: " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );
        });

        builder.setNegativeButton("Abbrechen", (dialog, which) -> dialog.cancel());
        builder.show();
    }


}