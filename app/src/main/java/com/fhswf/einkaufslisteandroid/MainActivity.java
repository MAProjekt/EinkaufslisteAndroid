// Quelle: https://developer.android.com/reference/android/app/AlertDialog

package com.fhswf.einkaufslisteandroid;

import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.fhswf.einkaufslisteandroid.datenpersistierung.FirestoreManager;
import com.fhswf.einkaufslisteandroid.fragment.GroupsFragment;
import com.fhswf.einkaufslisteandroid.fragment.HomeFragment;
import com.fhswf.einkaufslisteandroid.fragment.UebersichtFragment;
import com.fhswf.einkaufslisteandroid.logic.AuthService;
import com.fhswf.einkaufslisteandroid.logic.DialogHelper;
import com.fhswf.einkaufslisteandroid.logic.ThemeLogic;
import com.fhswf.einkaufslisteandroid.models.Product;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;

import android.Manifest;

/**
 * MainActivity ist der zentrale Einstiegspunkt der App.
 * Diese Activity verwaltet den Navigation Drawer, Toolbar und den Wechsel zwischen den Fragmenten,
 * hier mithilfe des Menüs.
 */
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private DrawerLayout drawerLayout; // Verwaltet das Hauptlayout des Navigation Drawers

    private FirestoreManager firestoreManager;
    private FirebaseAuth mAuth;

    /**
     * Initialisiert die MainActivity, setzt Layout, Toolbar, Navigation Drawer und lädt das
     * Standardfragment.
     * @param savedInstanceState der zuvor gespeicherte Zustand der Activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialisiere FirestoreManager und FirebaseAuth
        firestoreManager = new FirestoreManager();
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            checkAndAssignFcmToken(user.getUid());
        }

        //EdgeToEdge.enable(this);
        setContentView(com.fhswf.einkaufslisteandroid.R.layout.activity_main);
        // Farbe der Statusleiste setzen
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.dataLeiste));

        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.sideMenuLayout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        toggleActionBar(savedInstanceState, navigationView, toolbar);

        loadUserInfo(user, navigationView);
        userInfoEdit(navigationView);

        // Farbe der NavigationBar ändern je nach Mode (Darkmode)
        ThemeLogic.updateNavigationBarColor(this);

        checkNotificationPermission();
    }


    /**
     * Initialisiert die ActionBar und den Navigation Drawer.
     * @param savedInstanceState der zuvor gespeicherte Zustand der Activity.
     * @param navigationView das Navigation Drawer.
     * @param toolbar die Toolbar.
     */
    private void toggleActionBar(Bundle savedInstanceState, NavigationView navigationView, Toolbar toolbar){
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.schliesseDrawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_view_tag, new HomeFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }
    }

    /**
     * Lödt die Benutzerinformationen in die Header-Ansicht.
     * @param user der eingeloggte Benutzer.
     * @param navigationView der Navigation Drawer.
     */
    private void loadUserInfo(FirebaseUser user, NavigationView navigationView){
        // User im nav_menu 1 (nav_header) anzeigen
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
    }

    /**
     * Ermöglicht das Editieren des Nutzernamens.
     * Und lädt ein Benutzerbild, wenn es vorhanden ist.
     * @param navigationView das Navigation Drawer.
     */
    private void userInfoEdit(NavigationView navigationView){
        // getHeaderView(0) wird verwendet, um auf den Header des NavigationView zuzugreifen,
        // um darin enthaltene Views zu ändern, wie z.B. den Benutzernamen.
        View headerView = navigationView.getHeaderView(0);
        ImageButton editUsernameButton = headerView.findViewById(R.id.editSideMenuUsernameButton);
        editUsernameButton.setOnClickListener(v -> showEditUsernameDialog());

        // Profilbild laden (falls vorhanden) oder Standardbild anzeigen
        ImageView profileImage = headerView.findViewById(R.id.profileImage);
        if (profileImage != null) {
            AuthService.getInstance().loadUserProfileImage(this, profileImage);
        } else {
            Toast.makeText(this, "Profilbild-ImageView wurde nicht gefunden.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Behandelt die Auswahl von Menü-Elementen im Navigation-Drawer und lädt das entsprechende
     * Fragment dann.
     * @param item das ausgewählte Menü-Item
     * @return true, wenn das Item verarbeitet wurde, sonst false
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        Fragment selectedFragment = null;

        if (itemId == R.id.nav_home) {
            selectedFragment = new HomeFragment();
        } else if (itemId == R.id.nav_uebersicht) {
            selectedFragment = new UebersichtFragment();
        } else if (itemId == R.id.nav_groups) {
            selectedFragment = new GroupsFragment();
        } else if (itemId == R.id.darkmode) {
            ThemeLogic.toggleDarkMode(this);

            return true;
        } else if (itemId == R.id.nav_logout) {
            Toast.makeText(MainActivity.this, "Erfolgreich ausgeloggt!", Toast.LENGTH_SHORT).show();
            AuthService.getInstance().signOut(this);
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

    /**
     * Überschreibt das Verhalten der Zurück-Taste.
     * Schließt den Navigation Drawer, wenn dieser geöffnet ist, ansonsten wird die Standardaktion
     * ausgeführt. Dieses Verhalten verhindert, dass Benutzer versehentlich die App verlassen,
     * während sie noch im Menü navigieren. Es ist also eine Verbesserung der Usability.
     */
    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }else {
            super.onBackPressed();
        }
    }

    /**
     * Methode um das zweite Menü aufzurufen und einzubinden.
     * @param menu Element aus der XML das ein klassisches Menü darstellt
     * @return true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.nav_menu2, menu);
        return true;
    }

    /**
     * Behandelt die Auswahl von Menüelementen im Optionsmenü.
     * @param item das ausgewählte Menü-Item.
     * @return true, wenn das Item verabeitet wurde, ansonsten die Standard-Aktion.
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.listeHinzufuegen) {
            showCreateListDialog();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Dialog in dem der User einen Namen für die Liste vergeben muss. Nach der Bestätigung
     * wird die Liste erstellt.
     */
    private void showCreateListDialog() {
        DialogHelper.showCreateListDialog(this, listName -> {
            List<Product> products = new ArrayList<>();
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
        });
    }

    /**
     * Methode um bei Knopfdruck auf den Edit-Button (Stift) im nav_menu den Nutzernamen zu
     * bearbeiten, mithilfe eines Dialoges.
     */
    private void showEditUsernameDialog() {
        EditText input = new EditText(this);
        input.setHint("Neuer Nutzername");

        new android.app.AlertDialog.Builder(this)
                .setTitle("Neuer Nutzername")
                .setView(input)
                .setPositiveButton("Bestätigen", (dialog, which) -> {
                    String newUsername = input.getText().toString().trim();
                    if (newUsername.isEmpty() || mAuth.getCurrentUser() == null) {
                        Toast.makeText(this, "Bitte einen gültigen Namen eingeben!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    AuthService.getInstance().updateDisplayName(newUsername, task -> Toast.makeText(this,
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

    /**
     * Methode zum Prüfen und Anfordern der Berechtigungen
     */
    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    /**
     * Holt sich den FCM-Token des jeweiligen Users, um zu gucken wen er die Benachrichtigung senden
     * soll. Und speichert diese anschließend in Firestore ab.
     * @param userId Id des eingeloggten Users.
     */
    private void checkAndAssignFcmToken(String userId) {
        firestoreManager.getFcmToken(userId, existingToken -> {
            if (existingToken == null || existingToken.isEmpty()) {
                FirebaseMessaging.getInstance().getToken()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                String newToken = task.getResult();
                                firestoreManager.saveFcmToken(userId, newToken);
                            }
                        });
            }
        });
    }
}