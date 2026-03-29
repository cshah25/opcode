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

        NavController navController = getNavController();
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.launchFragment,
                R.id.setupFragment,
                R.id.EventCreatorFragment,
                R.id.EventListFragment
        ).build();

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.bottomNav, navController);

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int visibility = (hiddenToolbars.contains(destination.getId())) ? View.GONE : View.VISIBLE;
            binding.bottomNav.setVisibility(visibility);
            binding.topAppBar.setVisibility(visibility);
            binding.topAppBar.setTitle("");
        });

        binding.profileIcon.setOnClickListener(v -> navController.navigate(R.id.ProfileFragment));
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = getNavController();
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private NavController getNavController() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        return (navHostFragment != null)
                ? navHostFragment.getNavController()
                : Navigation.findNavController(this, R.id.nav_host_fragment);
    }

}
