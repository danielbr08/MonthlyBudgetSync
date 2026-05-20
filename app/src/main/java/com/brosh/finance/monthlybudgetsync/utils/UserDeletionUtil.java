package com.brosh.finance.monthlybudgetsync.utils;

import android.util.Log;

import androidx.annotation.NonNull;

import com.brosh.finance.monthlybudgetsync.config.Config;
import com.brosh.finance.monthlybudgetsync.config.Definitions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Utility class for deleting users from Firebase.
 * 
 * <p><b>USAGE FROM INTELLIJ DEBUGGER (Evaluate Expression):</b></p>
 * <pre>
 * // SIMPLEST - Delete by userId only (looks up email automatically)
 * UserDeletionUtil.deleteUser("USER_ID_HERE")
 * 
 * // Delete everything for a user (Auth + Firestore + Realtime DB)
 * UserDeletionUtil.deleteUserCompletely("USER_ID_HERE", "user@email.com")
 * 
 * // Delete only from databases (keeps Auth account)
 * UserDeletionUtil.deleteUserFromDatabases("USER_ID_HERE", "user@email.com")
 * </pre>
 * 
 * <p><b>WARNING:</b> These operations are irreversible!</p>
 */
public final class UserDeletionUtil {

    private static final String TAG = "UserDeletionUtil";

    private UserDeletionUtil() {
        // Utility class - prevent instantiation
    }

    /**
     * SIMPLEST METHOD - Just provide userId, email will be looked up.
     * Deletes from Firestore and Realtime Database.
     * 
     * Usage: UserDeletionUtil.deleteUser("u73XfT4C89eDmN4ySoJPPgWpzAH3")
     *
     * @param userId The Firebase user UID
     */
    public static void deleteUser(String userId) {
        Log.w(TAG, "");
        Log.w(TAG, "========================================");
        Log.w(TAG, "⚠️ STARTING USER DELETION: " + userId);
        Log.w(TAG, "========================================");
        
        try {
            // Delete from Firestore
            Log.i(TAG, "Step 1: Deleting from Firestore...");
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .delete()
                    .addOnSuccessListener(aVoid -> Log.i(TAG, "✅ Firestore: users/" + userId + " DELETED"))
                    .addOnFailureListener(e -> Log.e(TAG, "❌ Firestore FAILED: " + e.getMessage()));
        } catch (Exception e) {
            Log.e(TAG, "❌ Firestore exception: " + e.getMessage());
        }

        try {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            
            // Delete from Users node
            Log.i(TAG, "Step 2: Deleting from Users node...");
            database.getReference("Users").child(userId).removeValue()
                    .addOnSuccessListener(aVoid -> Log.i(TAG, "✅ Users/" + userId + " DELETED"))
                    .addOnFailureListener(e -> Log.e(TAG, "❌ Users FAILED: " + e.getMessage()));

            // Delete from Monthly Budget node
            Log.i(TAG, "Step 3: Deleting from Monthly Budget node...");
            database.getReference("Monthly Budget").child(userId).removeValue()
                    .addOnSuccessListener(aVoid -> Log.i(TAG, "✅ Monthly Budget/" + userId + " DELETED"))
                    .addOnFailureListener(e -> Log.e(TAG, "❌ Monthly Budget FAILED: " + e.getMessage()));

            // Delete from Shares node
            Log.i(TAG, "Step 4: Deleting from Shares node...");
            database.getReference("Shares").child(userId).removeValue()
                    .addOnSuccessListener(aVoid -> Log.i(TAG, "✅ Shares/" + userId + " DELETED"))
                    .addOnFailureListener(e -> Log.e(TAG, "❌ Shares FAILED: " + e.getMessage()));

            // Delete from Owners node
            Log.i(TAG, "Step 5: Deleting from Owners node...");
            database.getReference("Owners").child(userId).removeValue()
                    .addOnSuccessListener(aVoid -> Log.i(TAG, "✅ Owners/" + userId + " DELETED"))
                    .addOnFailureListener(e -> Log.e(TAG, "❌ Owners FAILED: " + e.getMessage()));

        } catch (Exception e) {
            Log.e(TAG, "❌ Realtime Database exception: " + e.getMessage());
        }

        Log.w(TAG, "========================================");
        Log.w(TAG, "📋 All deletion requests SENT - check logs above for results");
        Log.w(TAG, "========================================");
    }

    /**
     * Delete user with email (also cleans up Email Uid mapping).
     * 
     * Usage: UserDeletionUtil.deleteUserWithEmail("userId", "email@example.com")
     *
     * @param userId The Firebase user UID
     * @param email  The user's email address
     */
    public static void deleteUserWithEmail(String userId, String email) {
        Log.w(TAG, "");
        Log.w(TAG, "========================================");
        Log.w(TAG, "⚠️ STARTING USER DELETION WITH EMAIL");
        Log.w(TAG, "   UserId: " + userId);
        Log.w(TAG, "   Email: " + email);
        Log.w(TAG, "========================================");
        
        // First do the basic deletion
        deleteUser(userId);
        
        // Also delete Email Uid mapping
        if (email != null && !email.isEmpty()) {
            try {
                String emailComma = email.replace(".", ",");
                Log.i(TAG, "Step 6: Deleting Email Uid mapping: " + emailComma);
                FirebaseDatabase.getInstance()
                        .getReference("Email Uid")
                        .child(emailComma)
                        .removeValue()
                        .addOnSuccessListener(aVoid -> Log.i(TAG, "✅ Email Uid/" + emailComma + " DELETED"))
                        .addOnFailureListener(e -> Log.e(TAG, "❌ Email Uid FAILED: " + e.getMessage()));
            } catch (Exception e) {
                Log.e(TAG, "❌ Email Uid exception: " + e.getMessage());
            }
        }
    }

    /**
     * Deletes user completely from:
     * - Firebase Authentication
     * - Firestore (users collection)
     * - Realtime Database (Users, Email Uid, Monthly Budget, Shares, Owners)
     *
     * @param userId The Firebase user UID
     * @param email  The user's email address
     */
    public static void deleteUserCompletely(String userId, String email) {
        Log.w(TAG, "⚠️ STARTING COMPLETE USER DELETION for: " + userId);
        
        // First delete from databases
        deleteUserWithEmail(userId, email);
        
        // Then delete from Firebase Auth (if current user)
        deleteFromFirebaseAuth(userId);
    }

    /**
     * Deletes user from all databases but keeps Firebase Auth account.
     * Use this if you only want to clear data but keep the login.
     *
     * @param userId The Firebase user UID
     * @param email  The user's email address
     */
    public static void deleteUserFromDatabases(String userId, String email) {
        deleteUserWithEmail(userId, email);
    }

    /**
     * Deletes user document from Firestore.
     *
     * @param userId The Firebase user UID
     */
    public static void deleteFromFirestore(String userId) {
        Log.i(TAG, "Deleting from Firestore: " + userId);
        
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .delete()
                .addOnSuccessListener(aVoid -> Log.i(TAG, "✅ Firestore: User document deleted"))
                .addOnFailureListener(e -> Log.e(TAG, "❌ Firestore: Failed to delete - " + e.getMessage()));
    }

    /**
     * Deletes user data from Firebase Realtime Database.
     * Removes:
     * - Users/{userId}
     * - Email Uid/{emailComma}
     * - Monthly Budget/{userId} (user's budget data)
     * - Shares/{userId} (if user is a guest)
     * - Owners/{userId} (if user is an owner)
     *
     * @param userId The Firebase user UID
     * @param email  The user's email address
     */
    public static void deleteFromRealtimeDatabase(String userId, String email) {
        deleteUserWithEmail(userId, email);
    }

    /**
     * Deletes user from Firebase Authentication.
     * Note: This only works if the userId matches the currently logged-in user.
     * For deleting other users, use Firebase Admin SDK or Firebase Console.
     *
     * @param userId The Firebase user UID
     */
    public static void deleteFromFirebaseAuth(String userId) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        
        if (currentUser == null) {
            Log.w(TAG, "⚠️ FirebaseAuth: No user currently logged in. Cannot delete from Auth.");
            Log.w(TAG, "💡 To delete from Auth, either:");
            Log.w(TAG, "   1. Log in as the user first, or");
            Log.w(TAG, "   2. Delete manually from Firebase Console");
            return;
        }

        if (!currentUser.getUid().equals(userId)) {
            Log.w(TAG, "⚠️ FirebaseAuth: Current user (" + currentUser.getUid() + ") doesn't match target user (" + userId + ")");
            Log.w(TAG, "💡 To delete from Auth, either:");
            Log.w(TAG, "   1. Log in as the target user first, or");
            Log.w(TAG, "   2. Delete manually from Firebase Console");
            return;
        }

        currentUser.delete()
                .addOnSuccessListener(aVoid -> Log.i(TAG, "✅ FirebaseAuth: User account deleted"))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ FirebaseAuth: Failed to delete - " + e.getMessage());
                    Log.w(TAG, "💡 User may need to re-authenticate first (recent login required)");
                });
    }

    /**
     * Looks up a user's data in the database and logs it.
     * Useful for verifying what data exists before deletion.
     *
     * @param userId The Firebase user UID
     * @param email  The user's email address (optional, can be null)
     */
    public static void inspectUserData(String userId, String email) {
        Log.i(TAG, "🔍 INSPECTING USER DATA FOR: " + userId);
        
        FirebaseDatabase database = DBUtil.getDatabase();

        // Check Users node
        database.getReference(Definitions.USERS).child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.i(TAG, "📁 Users/" + userId + " exists: " + snapshot.exists());
                        if (snapshot.exists()) {
                            Log.i(TAG, "   Data: " + snapshot.getValue());
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error checking Users: " + error.getMessage());
                    }
                });

        // Check Monthly Budget node
        database.getReference(Definitions.MONTHLY_BUDGET).child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.i(TAG, "📁 Monthly Budget/" + userId + " exists: " + snapshot.exists());
                        if (snapshot.exists()) {
                            Log.i(TAG, "   Children count: " + snapshot.getChildrenCount());
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error checking Monthly Budget: " + error.getMessage());
                    }
                });

        // Check Email Uid node
        if (email != null && !email.isEmpty()) {
            String emailComma = email.replace(".", ",");
            database.getReference(Definitions.EMAIL_UID).child(emailComma)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Log.i(TAG, "📁 Email Uid/" + emailComma + " exists: " + snapshot.exists());
                            if (snapshot.exists()) {
                                Log.i(TAG, "   Value: " + snapshot.getValue());
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e(TAG, "Error checking Email Uid: " + error.getMessage());
                        }
                    });
        }

        // Check Shares node
        database.getReference(Definitions.SHARES).child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.i(TAG, "📁 Shares/" + userId + " exists: " + snapshot.exists());
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error checking Shares: " + error.getMessage());
                    }
                });

        // Check Owners node
        database.getReference(Definitions.OWNERS).child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.i(TAG, "📁 Owners/" + userId + " exists: " + snapshot.exists());
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error checking Owners: " + error.getMessage());
                    }
                });

        // Check Firestore
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(doc -> Log.i(TAG, "📁 Firestore users/" + userId + " exists: " + doc.exists()))
                .addOnFailureListener(e -> Log.e(TAG, "Error checking Firestore: " + e.getMessage()));
    }

    /**
     * Gets the current logged-in user's ID and email.
     * Useful for quickly getting the values to pass to deletion methods.
     */
    public static void printCurrentUser() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Log.i(TAG, "📌 CURRENT USER:");
            Log.i(TAG, "   UID: " + user.getUid());
            Log.i(TAG, "   Email: " + user.getEmail());
            Log.i(TAG, "");
            Log.i(TAG, "💡 To delete this user, run:");
            Log.i(TAG, "   UserDeletionUtil.deleteUserCompletely(\"" + user.getUid() + "\", \"" + user.getEmail() + "\")");
        } else {
            Log.w(TAG, "⚠️ No user currently logged in");
        }
    }
}
