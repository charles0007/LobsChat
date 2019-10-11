package lobschat.hycode.lobschat.firebase_notification;

/**
 * Created by Icode on 12/13/2017.
 */

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.io.IOException;

import lobschat.hycode.lobschat.SessionManagement;
import  lobschat.hycode.lobschat.firebase_notification.Config;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
    private static final String TAG = MyFirebaseInstanceIDService.class.getSimpleName();
SessionManagement sessionManagement;
    DatabaseReference nRootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference dblobschat = nRootRef.child("lobschat");
    DatabaseReference nusers = dblobschat.child("users");
    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        sessionManagement=new SessionManagement(this);
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        // Saving reg id to shared preferences
        storeRegIdInPref(refreshedToken);
        try {
            final String mainUser = sessionManagement.getUserDetails().get("User");
            DatabaseReference newuser = nusers.child(mainUser
                    .replace(".", "_")
                    .replace("#", "_")
                    .replace("$", "_")
                    .replace("[", "_")
                    .replace("]", "_")
                    .replace("@", "_"));
            DatabaseReference dbToken = newuser.child("Token");
            dbToken.setValue(refreshedToken);
        }catch (Exception ex){}
        // sending reg id to your server


        // Notify UI that registration has completed, so the progress indicator can be hidden.
        Intent registrationComplete = new Intent(Config.REGISTRATION_COMPLETE);
        registrationComplete.putExtra("token", refreshedToken);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    private void storeRegIdInPref(String token) {
       sessionManagement.set("Token",token);
    }

    private void updateRegistrationToServer(final String token,final String user) {
        // updating gcm token to server

        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("Token",token)
                .add("User",user)
                .build();

        Request request = new Request.Builder()
                .url("http://donationearners.net/update_data.php")
                .post(body)
                .build();
        try {
            client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}