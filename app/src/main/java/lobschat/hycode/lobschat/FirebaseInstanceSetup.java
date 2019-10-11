package lobschat.hycode.lobschat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseInstanceSetup {

private static FirebaseDatabase database;
private static DatabaseReference mDatabaseRef;
//FirebaseInstanceSetup.getDatabaseRef();//
    public static DatabaseReference getDatabaseRef() {
        if (mDatabaseRef == null) {
            getDatabase();
            mDatabaseRef = database.getReference();
            // ...
        }
        return mDatabaseRef;
    }

    public static FirebaseDatabase getDatabase() {
        if (database == null) {
            database = FirebaseDatabase.getInstance();
            database.setPersistenceEnabled(true);
            // ...
        }
        return database;
    }
}
