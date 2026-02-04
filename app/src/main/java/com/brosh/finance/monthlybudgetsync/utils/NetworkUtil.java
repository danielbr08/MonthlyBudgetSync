package com.brosh.finance.monthlybudgetsync.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Utility class for network-related operations.
 * Provides methods to check network connectivity status.
 */
public final class NetworkUtil {
    
    // Private constructor to prevent instantiation
    private NetworkUtil() {
        throw new AssertionError("Utility class - do not instantiate");
    }

    /**
     * Checks if the device has an active network connection.
     * 
     * @param context the application context
     * @return true if connected to a network, false otherwise
     */
    public static boolean isNetworkAvailable(@Nullable Context context) {
        if (context == null) {
            return false;
        }

        ConnectivityManager connectivityManager = 
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        
        if (connectivityManager == null) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network network = connectivityManager.getActiveNetwork();
            if (network == null) {
                return false;
            }
            
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
            return capabilities != null && (
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            );
        } else {
            // Fallback for older API levels
            @SuppressWarnings("deprecation")
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
    }

    /**
     * Checks if the device is connected to WiFi.
     * 
     * @param context the application context
     * @return true if connected to WiFi, false otherwise
     */
    @SuppressWarnings("unused") // May be used for future functionality
    public static boolean isWifiConnected(@Nullable Context context) {
        if (context == null) {
            return false;
        }

        ConnectivityManager connectivityManager = 
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        
        if (connectivityManager == null) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network network = connectivityManager.getActiveNetwork();
            if (network == null) {
                return false;
            }
            
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
            return capabilities != null && 
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
        } else {
            @SuppressWarnings("deprecation")
            NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            return wifiInfo != null && wifiInfo.isConnected();
        }
    }

    /**
     * Checks if the device is connected to cellular data.
     * 
     * @param context the application context
     * @return true if connected to cellular, false otherwise
     */
    @SuppressWarnings("unused") // May be used for future functionality
    public static boolean isCellularConnected(@Nullable Context context) {
        if (context == null) {
            return false;
        }

        ConnectivityManager connectivityManager = 
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        
        if (connectivityManager == null) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network network = connectivityManager.getActiveNetwork();
            if (network == null) {
                return false;
            }
            
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
            return capabilities != null && 
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR);
        } else {
            @SuppressWarnings("deprecation")
            NetworkInfo mobileInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            return mobileInfo != null && mobileInfo.isConnected();
        }
    }

    /**
     * Returns a human-readable description of the current network type.
     * 
     * @param context the application context
     * @return "WiFi", "Cellular", "Ethernet", "None", or "Unknown"
     */
    @SuppressWarnings("unused") // May be used for future functionality
    @NonNull
    public static String getNetworkType(@Nullable Context context) {
        if (context == null) {
            return "None";
        }

        ConnectivityManager connectivityManager = 
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        
        if (connectivityManager == null) {
            return "None";
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network network = connectivityManager.getActiveNetwork();
            if (network == null) {
                return "None";
            }
            
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
            if (capabilities == null) {
                return "None";
            }
            
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                return "WiFi";
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                return "Cellular";
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                return "Ethernet";
            }
        } else {
            @SuppressWarnings("deprecation")
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                return activeNetworkInfo.getTypeName();
            }
        }
        
        return "Unknown";
    }
}
