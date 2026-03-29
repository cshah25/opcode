package com.example.opcodeapp;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.opcodeapp.databinding.ActivityMainBinding;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    // Fragment IDs where the top and bottom navbars are hidden
    private static final List<Integer> hiddenToolbars = List.of(
            R.id.launchFragment,
            R.id.setupFragment
    );


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.topAppBar);

        // Obtain NavController from the NavHostFragment (safer than Navigation.findNavController)
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment != null
                ? navHostFragment.getNavController()
                : Navigation.findNavController(this, R.id.nav_host_fragment);

        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.launchFragment,
                R.id.setupFragment,
                R.id.EventCreatorFragment,
                R.id.EventListFragment,
                R.id.ProfileFragment
        ).build();

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        NavigationUI.setupWithNavController(binding.bottomNav, navController);

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int id = destination.getId();
            binding.bottomNav.setVisibility((hiddenToolbars.contains(id)) ? View.GONE : View.VISIBLE);
            binding.topAppBar.setVisibility((hiddenToolbars.contains(id)) ? View.GONE : View.VISIBLE);
            binding.topAppBar.setTitle("");
        });

        binding.profileIcon.setOnClickListener(v -> navController.navigate(R.id.ProfileFragment));
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment != null
                ? navHostFragment.getNavController()
                : Navigation.findNavController(this, R.id.nav_host_fragment);

        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
