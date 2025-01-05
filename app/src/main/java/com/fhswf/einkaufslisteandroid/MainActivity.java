package com.fhswf.einkaufslisteandroid;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import org.checkerframework.checker.nullness.qual.NonNull;


public class MainActivity extends AppCompatActivity {

    DrawerLayout drawerLayout; // Verwaltet das Hauptlayout des Navigation Drawers
    NavigationView navigationView; // Seitenmenü
    ActionBarDrawerToggle drawerToggle; // Für Syncronisation

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(com.fhswf.einkaufslisteandroid.R.layout.activity_main);


        drawerLayout = findViewById(R.id.sideMenuLayout);
        navigationView = findViewById(R.id.navigation_view);

        // Erstellt einen ActionBarDrawerToggle
        // Verknüpft das DrawerLayout mit der ActionBar
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.oeffneDrawer, R.string.schliesseDrawer);

        drawerLayout.addDrawerListener(drawerToggle); // Fügt den DrawerListener zum Layout hinzu, um auf Events zuzugreifen (öffnen, schließen)
        drawerToggle.syncState(); // Synchronisiert den Zustand des Drawer-Toggles

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Problemanfällig
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.itemLogout) {
                    Toast.makeText(MainActivity.this, "Erfolgreich ausgeloggt", Toast.LENGTH_SHORT).show();
                } else if (itemId == R.id.darkmode) {
                    Toast.makeText(MainActivity.this, "Darkmode aktiviert", Toast.LENGTH_SHORT).show();
                } else if (itemId == R.id.lightmode) {
                    Toast.makeText(MainActivity.this, "Darkmode deaktiviert", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@androidx.annotation.NonNull MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)){
            return true; // DrawerToggle hat die Aktion verarbeitet
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }else{
            super.onBackPressed();
        }
    }
}