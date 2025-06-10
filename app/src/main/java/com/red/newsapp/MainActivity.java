package com.red.newsapp;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.red.newsapp.fragments.NewsFragment;
import com.red.newsapp.fragments.SavedArticlesFragment;
import com.red.newsapp.fragments.ProfileFragment;
import com.red.newsapp.fragments.LoginFragment;
import androidx.appcompat.app.AppCompatDelegate;
import android.widget.Switch;

public class MainActivity extends AppCompatActivity implements LoginFragment.LoginFragmentListener {

    private static final String TAG = "MainActivity";
    private static final int NETWORK_CHECK_DELAY = 2000; // 2 detik delay

    private BottomNavigationView bottomNavigationView;
    private Switch switchTheme;
    private TextView tvThemeLabel;
    private TextView tvOfflineMessage;
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;
    private boolean isCurrentlyOffline = false;
    private android.os.Handler networkHandler;
    private Runnable networkCheckRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Terapkan mode tema yang sudah disimpan sebelum setContentView
        int nightMode = getSharedPreferences("settings", MODE_PRIVATE)
                .getInt("night_mode", AppCompatDelegate.MODE_NIGHT_NO);
        AppCompatDelegate.setDefaultNightMode(nightMode);

        setContentView(R.layout.activity_main);

        initViews();
        setupThemeSwitch(nightMode);
        setupNetworkMonitoring();

        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(new NewsFragment());
        }

        setupBottomNavigation();
    }

    private void initViews() {
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        switchTheme = findViewById(R.id.switchTheme);
        tvThemeLabel = findViewById(R.id.tvThemeLabel);
        tvOfflineMessage = findViewById(R.id.tvOfflineMessage);
        networkHandler = new android.os.Handler();

        // Debug log untuk memastikan view ditemukan
        Log.d(TAG, "tvOfflineMessage found: " + (tvOfflineMessage != null));
        if (tvOfflineMessage == null) {
            Log.e(TAG, "tvOfflineMessage is null! Check your layout XML.");
        }
    }

    private void setupThemeSwitch(int nightMode) {
        // Set switch sesuai mode saat ini
        boolean isNightMode = (nightMode == AppCompatDelegate.MODE_NIGHT_YES);
        switchTheme.setChecked(isNightMode);

        switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                saveNightModeState(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                saveNightModeState(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });
    }

    private void setupNetworkMonitoring() {
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        // Cek status jaringan saat startup
        checkNetworkStatusWithDelay(500); // Delay 500ms untuk startup

        // Setup network callback untuk monitoring perubahan jaringan
        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                Log.d(TAG, "Network available");
                checkNetworkStatusWithDelay(1000); // Delay 1 detik
            }

            @Override
            public void onLost(Network network) {
                Log.d(TAG, "Network lost");
                checkNetworkStatusWithDelay(1000); // Delay 1 detik
            }

            @Override
            public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
                Log.d(TAG, "Network capabilities changed");
                checkNetworkStatusWithDelay(1500); // Delay 1.5 detik
            }

            @Override
            public void onUnavailable() {
                Log.d(TAG, "Network unavailable");
                runOnUiThread(() -> updateOfflineMessageVisibility(true));
            }
        };

        // Register network callback
        if (connectivityManager != null) {
            try {
                NetworkRequest.Builder builder = new NetworkRequest.Builder()
                        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
                connectivityManager.registerNetworkCallback(builder.build(), networkCallback);
                Log.d(TAG, "Network callback registered successfully");
            } catch (Exception e) {
                Log.e(TAG, "Error registering network callback: " + e.getMessage());
            }
        }
    }

    private void checkNetworkStatusWithDelay(int delayMs) {
        // Cancel previous check if exists
        if (networkCheckRunnable != null) {
            networkHandler.removeCallbacks(networkCheckRunnable);
        }

        networkCheckRunnable = () -> {
            boolean isConnected = isNetworkAvailable();
            Log.d(TAG, "Delayed network check - Connected: " + isConnected);
            runOnUiThread(() -> updateOfflineMessageVisibility(!isConnected));
        };

        networkHandler.postDelayed(networkCheckRunnable, delayMs);
    }

    private void updateOfflineMessageVisibility(boolean showOffline) {
        if (tvOfflineMessage != null && isCurrentlyOffline != showOffline) {
            isCurrentlyOffline = showOffline;
            tvOfflineMessage.setVisibility(showOffline ? View.VISIBLE : View.GONE);
            Log.d(TAG, "Updated offline message visibility: " + (showOffline ? "VISIBLE" : "GONE"));
        }
    }

    private void checkNetworkStatus() {
        boolean isConnected = isNetworkAvailable();
        Log.d(TAG, "Network status check - Connected: " + isConnected);
        updateOfflineMessageVisibility(!isConnected);
    }

    private boolean isNetworkAvailable() {
        if (connectivityManager != null) {
            try {
                Network activeNetwork = connectivityManager.getActiveNetwork();
                if (activeNetwork != null) {
                    NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
                    boolean hasInternet = networkCapabilities != null &&
                            networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                            networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) &&
                            (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
                    Log.d(TAG, "Network capabilities check: " + hasInternet);
                    return hasInternet;
                } else {
                    Log.d(TAG, "No active network");
                }
            } catch (SecurityException e) {
                Log.e(TAG, "Security exception checking network: " + e.getMessage());
                // Fallback ke method lama
                return isNetworkConnectedLegacy();
            } catch (Exception e) {
                Log.e(TAG, "Exception checking network: " + e.getMessage());
                return isNetworkConnectedLegacy();
            }
        } else {
            Log.e(TAG, "ConnectivityManager is null");
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    private boolean isNetworkConnectedLegacy() {
        try {
            android.net.NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            boolean connected = activeNetworkInfo != null && activeNetworkInfo.isConnected();
            Log.d(TAG, "Legacy network check: " + connected);
            return connected;
        } catch (Exception e) {
            Log.e(TAG, "Legacy network check failed: " + e.getMessage());
            return false;
        }
    }

    private void saveNightModeState(int mode) {
        getSharedPreferences("settings", MODE_PRIVATE)
                .edit()
                .putInt("night_mode", mode)
                .apply();
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragment = null;

            if (item.getItemId() == R.id.app_bar_home) {
                fragment = new NewsFragment();
            } else if (item.getItemId() == R.id.app_bar_bookmark) {
                fragment = new SavedArticlesFragment();
            } else if (item.getItemId() == R.id.app_bar_profile) {
                fragment = new ProfileFragment();
            }

            if (fragment != null) {
                // Clear back stack when switching main tabs
                clearBackStack();
                loadFragment(fragment);
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    private void clearBackStack() {
        // Clear all fragments from back stack
        for (int i = 0; i < getSupportFragmentManager().getBackStackEntryCount(); i++) {
            getSupportFragmentManager().popBackStack();
        }
    }

    // LoginFragment.LoginFragmentListener implementation
    @Override
    public void onLoginSuccess() {
        // Navigate back to profile fragment
        getSupportFragmentManager().popBackStack();

        // Update bottom navigation to show profile tab as selected
        bottomNavigationView.setSelectedItemId(R.id.app_bar_profile);
    }

    @Override
    public void onNavigateToRegister() {
        // Navigate to RegisterActivity instead of RegisterFragment
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    public void onNavigateToLogin() {
        LoginFragment loginFragment = new LoginFragment();
        loginFragment.setLoginFragmentListener(this);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, loginFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onBackPressed() {
        // Handle back button press
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    // Method to navigate to specific fragment programmatically
    public void navigateToProfile() {
        bottomNavigationView.setSelectedItemId(R.id.app_bar_profile);
        clearBackStack();
        loadFragment(new ProfileFragment());
    }

    public void navigateToHome() {
        bottomNavigationView.setSelectedItemId(R.id.app_bar_home);
        clearBackStack();
        loadFragment(new NewsFragment());
    }

    public void navigateToBookmarks() {
        bottomNavigationView.setSelectedItemId(R.id.app_bar_bookmark);
        clearBackStack();
        loadFragment(new SavedArticlesFragment());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Cancel pending network checks
        if (networkHandler != null && networkCheckRunnable != null) {
            networkHandler.removeCallbacks(networkCheckRunnable);
        }

        // Unregister network callback
        if (connectivityManager != null && networkCallback != null) {
            try {
                connectivityManager.unregisterNetworkCallback(networkCallback);
                Log.d(TAG, "Network callback unregistered");
            } catch (Exception e) {
                Log.e(TAG, "Error unregistering network callback: " + e.getMessage());
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Cek network status saat kembali ke activity
        checkNetworkStatusWithDelay(1000);
    }

    // Method untuk testing - panggil dari fragment atau button
    public void testOfflineMessage() {
        if (tvOfflineMessage != null) {
            tvOfflineMessage.setVisibility(tvOfflineMessage.getVisibility() == View.VISIBLE ?
                    View.GONE : View.VISIBLE);
            Log.d(TAG, "Toggled offline message visibility");
        }
    }
}