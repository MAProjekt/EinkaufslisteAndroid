package com.fhswf.einkaufslisteandroid;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
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

//import com.fhswf.einkaufslisteandroid.datenpersistierung.FirestoreManager;
import com.fhswf.einkaufslisteandroid.datenpersistierung.FirestoreManager;
import com.fhswf.einkaufslisteandroid.fragment.GroupsFragment;
import com.fhswf.einkaufslisteandroid.fragment.HomeFragment;
import com.fhswf.einkaufslisteandroid.fragment.UebersichtFragment;
import com.fhswf.einkaufslisteandroid.models.Product;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
//import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
                Toast.makeText(this, "TextView wurde nicht gefunden.", Toast.LENGTH_SHORT).show();
            }

            TextView sideMenuUsername = headerView.findViewById(R.id.sideMenuUsername);
            if (sideMenuUsername != null) {
                String displayName = user.getDisplayName();
                sideMenuUsername.setText(displayName != null && !displayName.isEmpty() ? displayName : user.getEmail());
            } else {
                Toast.makeText(this, "TextView sideMenuUsername wurde nicht gefunden.", Toast.LENGTH_SHORT).show();
            }
        }

        // getHeaderView(0) wird verwendet, um auf den Header des NavigationView zuzugreifen,
        // um darin enthaltene Views zu ändern, wie z.B. den Benutzernamen.
        View headerView = navigationView.getHeaderView(0);
        ImageButton editUsernameButton = headerView.findViewById(R.id.editSideMenuUsernameButton);
        editUsernameButton.setOnClickListener(v -> showEditUsernameDialog());

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
        } else if (itemId == R.id.nav_groups) {
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
        } else if (itemId == R.id.listeHinzufuegen) {
            showCreateListDialog();
            return true;
        } else if (itemId == R.id.kopiereUID){
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                String userId = user.getUid();
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard != null) {
                    // Erstellen eines Clip-Objekts und Kopieren der UID in die Zwischenablage
                    android.content.ClipData clip = android.content.ClipData.newPlainText("User UID", userId);
                    clipboard.setPrimaryClip(clip);

                    Toast.makeText(this, "UID in die Zwischenablage kopiert", Toast.LENGTH_SHORT).show();
                }
            }
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
     * Methode um bei Knopfdruck auf den Edit-Button (Stift) im nav_menu den Nutzernamen zu
     * bearbeiten, mithilfe eines Dialoges.
     */
    private void showEditUsernameDialog() {
        EditText input = new EditText(this);
        input.setHint("Neuer Nutzername");

        new AlertDialog.Builder(this)
                .setTitle("Neuer Nutzername")
                .setView(input)
                .setPositiveButton("Bestätigen", (dialog, which) -> {
                    String newUsername = input.getText().toString().trim();
                    if (newUsername.isEmpty() || mAuth.getCurrentUser() == null) {
                        Toast.makeText(this, "Bitte einen gültigen Namen eingeben!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    mAuth.getCurrentUser().updateProfile(
                            new UserProfileChangeRequest.Builder().setDisplayName(newUsername).build()
                    ).addOnCompleteListener(task -> Toast.makeText(this,
                            task.isSuccessful() ? "Nutzername aktualisiert!" : "Fehler beim Aktualisieren!",
                            Toast.LENGTH_SHORT).show());

                    // View in nav_menu aktualisieren
                    TextView sideMenuUsername = findViewById(R.id.sideMenuUsername);
                    if (sideMenuUsername != null) {
                        sideMenuUsername.setText(newUsername);
                    }
                })
                .setNegativeButton("Abbrechen", null)
                .show();
    }
}