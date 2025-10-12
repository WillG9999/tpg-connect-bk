import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

public class AdminSetup {
    public static void main(String[] args) {
        try {
            // Initialize Firebase
            FileInputStream serviceAccount = new FileInputStream("/Users/willgraham/Desktop/keys/firebase-service-account.json");
            
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://connect-ea4c2-default-rtdb.firebaseio.com")
                    .build();

            FirebaseApp.initializeApp(options);

            // Get Firestore instance
            Firestore db = FirestoreClient.getFirestore();

            // Find user by email
            String email = "admin@connect.com";
            
            // Query the users collection for the email
            db.collection("users")
              .whereEqualTo("email", email)
              .get()
              .get()
              .forEach(document -> {
                  System.out.println("Found user: " + document.getId());
                  
                  // Update role to ADMIN
                  Map<String, Object> updates = new HashMap<>();
                  updates.put("role", "ADMIN");
                  
                  document.getReference().update(updates);
                  System.out.println("Updated user role to ADMIN");
              });

            System.out.println("Admin setup complete!");
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}