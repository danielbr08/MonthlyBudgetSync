package com.brosh.finance.monthlybudgetsync.utils;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.brosh.finance.monthlybudgetsync.R;
import com.brosh.finance.monthlybudgetsync.login.Login;
import com.brosh.finance.monthlybudgetsync.objects.User;

/**
 * Centralized session checks for activities that require a logged-in user.
 */
public final class SessionGuardUtil {

    private SessionGuardUtil() {
        throw new AssertionError("Utility class - do not instantiate");
    }

    /**
     * @return true if {@code user} is non-null; otherwise redirects to login and returns false.
     */
    public static boolean requireUser(@NonNull Activity activity, @Nullable User user) {
        if (user != null) {
            return true;
        }
        redirectToLogin(activity);
        return false;
    }

    /**
     * Redirects to the login screen and finishes the current activity.
     */
    public static void redirectToLogin(@NonNull Activity activity) {
        TextUtil.showMessage(
                activity.getString(R.string.login),
                Toast.LENGTH_SHORT,
                activity.getApplicationContext());
        activity.startActivity(new Intent(activity, Login.class));
        activity.finish();
    }
}
