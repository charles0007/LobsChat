package lobschat.hycode.lobschat;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;


import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;


/**
 * Created by HyCode on 12/28/2017.
 */

public class SetAllData extends AppCompatActivity implements LocationListener {
    TextView noUsersText;
    ArrayList<String> al = new ArrayList<>();

    ProgressDialog pd;
    ListView list;
    JSONObject jsonObject;
    ArrayList<HashMap<String, String>> dataList, dataGroupList;

    DatabaseReference nRootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference dblobschat = nRootRef.child("lobschat");
    DatabaseReference nUsers = dblobschat.child("users");
    DatabaseReference nChat = dblobschat.child("user-chat");
    DatabaseReference ngroupChat = dblobschat.child("group-chat");
    SessionManagement sessionManagement;
    String address, city, state, knownName, country, postalCode;
    Geocoder geocoder;
    List<Address> addresses;
    Location locLastKnown;
    protected LocationManager locationManager;
    protected LocationListener locationListener;
    protected Context context;
    //    boolean isNetworkEnabled;
    String towers;
    boolean loadPage1 = false, loadPage2 = false, loadPage3 = false;
    String userLatitude, userLongitude;
    Firebase reference1;
    String mainkey;
    DatabaseReference checkUser;
    DatabaseReference dbLocName;
    DatabaseReference dbGpsLat;
    DatabaseReference dbGpsLog;
    DatabaseReference dbAddress;
    DatabaseReference dbCity;
    DatabaseReference dbState;
    DatabaseReference dbCountry;
    DatabaseReference dbPostal;
    DatabaseReference dbStatus;
    DatabaseReference dbSex;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.get_data);

        sessionManagement = new SessionManagement(this);

        userLatitude = sessionManagement.get("GpsLat");
        userLongitude = sessionManagement.get("GpsLog");
        mainkey = sessionManagement.getUserDetails().get("User");
        checkUser = nUsers.child(mainkey);
        dbLocName = checkUser.child("LocName");
        dbGpsLat = checkUser.child("GpsLat");
        dbGpsLog = checkUser.child("GpsLog");
        dbAddress = checkUser.child("Address");
        dbCity = checkUser.child("City");
        dbState = checkUser.child("State");
        dbCountry = checkUser.child("Country");
        dbPostal = checkUser.child("Postal");
        dbStatus = checkUser.child("Status");
        dbSex = checkUser.child("Sex");
        Firebase.setAndroidContext(this);
        //  FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);

        }

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {

            showGPSAlertToUser();
        }
//        isNetworkEnabled = locationManager.isProviderEnabled(NETWORK_PROVIDER);
        Criteria crit = new Criteria();
        towers = locationManager.getBestProvider(crit, false);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CHANGE_NETWORK_STATE)
                != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        try {
            dblobschat.child("ServerKey").addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                @Override
                public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                    sessionManagement.set("ServerKey", dataSnapshot.getValue().toString());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    sessionManagement.set("ServerKey", "");
                }
            });
        } catch (Exception ex) {
            sessionManagement.set("ServerKey", "");
        }


        locationManager.requestLocationUpdates(towers, 1, 1, this);
        reference1 = new Firebase("https://lobschat.firebaseio.com/lobschat/users/" + sessionManagement.getUserDetails().get("User"));

        reference1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
//            Toast.makeText(SetAllData.this,postSnapshot.getKey()+" : "+postSnapshot.getValue().toString(),Toast.LENGTH_LONG).show();
                    if (postSnapshot.getKey().equals("Type")) {
                        sessionManagement.set("Type", postSnapshot.getValue().toString());
                    } else if (postSnapshot.getKey().equals("Username")) {
                        sessionManagement.set("MyUsername", postSnapshot.getValue().toString());
                    } else if (postSnapshot.getKey().equals("Image")) {
                        sessionManagement.set("Image", postSnapshot.getValue().toString());
                    } else if (postSnapshot.getKey().equals("Description")) {
                        sessionManagement.set("Description", postSnapshot.getValue().toString());
                    } else if (postSnapshot.getKey().equals("Business")) {
                        sessionManagement.set("Description", postSnapshot.getValue().toString());
                    } else if (postSnapshot.getKey().equals("SubStatus")) {
                        sessionManagement.set("SubStatus", postSnapshot.getValue().toString());
                    } else if (postSnapshot.getKey().equals("SubDate")) {
                        sessionManagement.set("SubDate", postSnapshot.getValue().toString());
                    } else if (postSnapshot.getKey().equals("Activation")) {
                        sessionManagement.set("Activation", postSnapshot.getValue().toString());
                    } else if (postSnapshot.getKey().equals("SubDaysLeft")) {
                        sessionManagement.set("SubDaysLeft", postSnapshot.getValue().toString());
                    } else if (postSnapshot.getKey().equals("TodayDate")) {
                        sessionManagement.set("TodayDate", postSnapshot.getValue().toString());
                    } else if (postSnapshot.getKey().equals("Sex")) {
                        sessionManagement.set("Sex", postSnapshot.getValue().toString());
                    } else if (postSnapshot.getKey().equals("City") && postSnapshot.getValue().toString() != "" && !postSnapshot.getValue().toString().isEmpty()) {
                        FirebaseMessaging.getInstance().unsubscribeFromTopic("/topics/City" + postSnapshot.getValue().toString());
                    } else if (postSnapshot.getKey().equals("State") && postSnapshot.getValue().toString() != "" && !postSnapshot.getValue().toString().isEmpty()) {
                        FirebaseMessaging.getInstance().unsubscribeFromTopic("/topics/State" + postSnapshot.getValue().toString());
                    }
                }

                if (sessionManagement.get("GpsLat") != null && !sessionManagement.get("GpsLat").isEmpty() &&
                        sessionManagement.get("City") != null && !sessionManagement.get("City").isEmpty() &&
                        sessionManagement.get("State") != null && !sessionManagement.get("State").isEmpty() &&
                        sessionManagement.get("Type") != null && !sessionManagement.get("Type").isEmpty() &&
                        sessionManagement.get("MyUsername") != null && !sessionManagement.get("MyUsername").isEmpty()) {
                    locationManager.removeUpdates(SetAllData.this);
                    finish();
                    loadPage1 = true;
                    // if (!loadPage2 && !loadPage3) {
                    startActivity(new Intent(SetAllData.this, MainActivity.class));
                    //}
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        geocoder = new Geocoder(this, Locale.getDefault());

        locLastKnown = locationManager.getLastKnownLocation(towers);


        sessionManagement.set("Artisan", "Local");
        sessionManagement.set("ArtisanPage", "Local");

        pd = new ProgressDialog(this);
        pd.setMessage("Gathering data...");
        pd.show();
        pd.setCanceledOnTouchOutside(false);
        pd.setCancelable(false);
        if (locLastKnown != null) {
            try {
                addresses = geocoder.getFromLocation(locLastKnown.getLatitude(), locLastKnown.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                if (addresses != null && addresses.size() > 0) {
                    address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                    city = addresses.get(0).getLocality();
                    state = addresses.get(0).getAdminArea();
                    country = addresses.get(0).getCountryName();
                    postalCode = addresses.get(0).getPostalCode();
                    knownName = addresses.get(0).getFeatureName(); // Only
                    sessionManagement.set("GpsLat", locLastKnown.getLatitude() + "");
                    sessionManagement.set("GpsLog", locLastKnown.getLongitude() + "");
                    dbGpsLat.setValue(locLastKnown.getLatitude() + "");
                    dbGpsLog.setValue(locLastKnown.getLongitude() + "");
                    if (address != null) {
                        sessionManagement.set("Address", address);
                        dbAddress.setValue(address);
                    }
                    if (city != null) {
                        sessionManagement.set("City", city);

                        FirebaseMessaging.getInstance().subscribeToTopic("/topics/City" + city);
                        dbCity.setValue(city);
                    }
                    if (state != null) {
                        sessionManagement.set("State", state);
                        FirebaseMessaging.getInstance().subscribeToTopic("/topics/State" + state);
                        dbState.setValue(state);
                    }
                    if (country != null) {
                        sessionManagement.set("Country", country);
                        dbCountry.setValue(country);
                    }
                    if (postalCode != null) {
                        sessionManagement.set("Postal", postalCode);
                        dbPostal.setValue(postalCode);
                    }
                    if (knownName != null) {
                        sessionManagement.set("LocName", knownName);
                        dbLocName.setValue(knownName);
                    }
                    dbStatus.setValue("online");
                } else {
                    address="Address";city="Lagos";state="Lagos";country="Nigeria";postalCode="123";
                    knownName="Name";
                    if (address != null) {
                        sessionManagement.set("Address", address);
//                        dbAddress.setValue(address);
                    }
                    if (city != null) {
                        sessionManagement.set("City", city);

                        FirebaseMessaging.getInstance().subscribeToTopic("/topics/City" + city);
                        dbCity.setValue(city);
                    }
                    if (state != null) {
                        sessionManagement.set("State", state);
                        FirebaseMessaging.getInstance().subscribeToTopic("/topics/State" + state);
                        dbState.setValue(state);
                    }
                    if (country != null) {
                        sessionManagement.set("Country", country);
                        dbCountry.setValue(country);
                    }
                    if (postalCode != null) {
                        sessionManagement.set("Postal", postalCode);
//                        dbPostal.setValue(postalCode);
                    }
                    if (knownName != null) {
                        sessionManagement.set("LocName", knownName);
//                        dbLocName.setValue(knownName);
                    }
                    dbStatus.setValue("online");
                    //   getCurrentLocationViaJSON(locLastKnown.getLatitude(), locLastKnown.getLongitude());
//                    Toast.makeText(SetAllData.this,currentLocation,Toast.LENGTH_LONG).show();
                }
            } catch (Exception rt) {
                Crashlytics.logException(rt);
                try {
                    //    getCurrentLocationViaJSON(locLastKnown.getLatitude(), locLastKnown.getLongitude());

                } catch (Exception ex) {
                }
            }
        }

    }

    @Override
    public void onLocationChanged(Location location) {

        try {
            String mainkey = sessionManagement.getUserDetails().get("User");

            DatabaseReference checkUser = nUsers.child(mainkey);
            DatabaseReference dbGpsLat = checkUser.child("GpsLat");
            DatabaseReference dbGpsLog = checkUser.child("GpsLog");
            DatabaseReference dbAddress = checkUser.child("Address");
            DatabaseReference dbCity = checkUser.child("City");
            DatabaseReference dbState = checkUser.child("State");
            DatabaseReference dbCountry = checkUser.child("Country");
            DatabaseReference dbPostal = checkUser.child("Postal");
            DatabaseReference dbLocName = checkUser.child("LocName");
            DatabaseReference dbStatus = checkUser.child("Status");
            sessionManagement.set("Artisan", "Local");
            sessionManagement.set("ArtisanPage", "Local");

            if (location != null) {
                try {
                    addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                    if (addresses != null && addresses.size() > 0) {
                        address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                        city = addresses.get(0).getLocality();
                        state = addresses.get(0).getAdminArea();
                        country = addresses.get(0).getCountryName();
                        postalCode = addresses.get(0).getPostalCode();
                        knownName = addresses.get(0).getFeatureName(); // Only
                        if (address != null) {
                            sessionManagement.set("Address", address);
                            dbAddress.setValue(address);
                        }
                        if (city != null) {
                            sessionManagement.set("City", city);
                            FirebaseMessaging.getInstance().subscribeToTopic("/topics/City" + city);
                            dbCity.setValue(city);
                        }
                        if (state != null) {
                            sessionManagement.set("State", state);
                            FirebaseMessaging.getInstance().subscribeToTopic("/topics/State" + state);
                            dbState.setValue(state);
                        }
                        if (country != null) {
                            sessionManagement.set("Country", country);
                            dbCountry.setValue(country);
                        }
                        if (postalCode != null) {
                            sessionManagement.set("Postal", postalCode);
                            dbPostal.setValue(postalCode);
                        }
                        if (knownName != null) {
                            sessionManagement.set("LocName", knownName);
                            dbLocName.setValue(knownName);
                        }
                        dbStatus.setValue("online");


                    } else {
                        //  getCurrentLocationViaJSON(location.getLatitude(), location.getLongitude());
                    }

                } catch (IOException e) {

                    city = getLocationTypeName(location.getLatitude(), location.getLongitude(), "city");
                    state = getLocationTypeName(location.getLatitude(), location.getLongitude(), "state");
                    country = getLocationTypeName(location.getLatitude(), location.getLongitude(), "country");
                    if (city != null && state != null) {
                        if (city.equalsIgnoreCase("OVER_QUERY_LIMIT") || state.equalsIgnoreCase("OVER_QUERY_LIMIT") || city == null) {
                            pd.dismiss();
                            AlertDialog.Builder builder = new AlertDialog.Builder(SetAllData.this);
                            builder.setMessage("OVER_QUERY_LIMIT")
                                    .setCancelable(true)
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            System.exit(0);
                                        }
                                    });
                            AlertDialog alert = builder.create();
                            alert.show();
                        } else {
                            if (!city.isEmpty() && city != null) {
                                sessionManagement.set("City", city);
                                FirebaseMessaging.getInstance().subscribeToTopic("/topics/City" + city);
                                dbCity.setValue(city);
                            }
                            if (state != null && !state.isEmpty()) {
                                sessionManagement.set("State", state);
                                FirebaseMessaging.getInstance().subscribeToTopic("/topics/State" + state);
                                dbState.setValue(state);
                            }
                            if (country != null && !country.isEmpty()) {
                                sessionManagement.set("Country", country);
                                dbCountry.setValue(country);
                            }
                        }
                    } else {
                        pd.dismiss();
                    }
                    Crashlytics.logException(e);
                    // getCurrentLocationViaJSON(location.getLatitude(), location.getLongitude());
                    e.printStackTrace();
                } catch (Exception rt) {
                    Crashlytics.logException(rt);
                    //  getCurrentLocationViaJSON(location.getLatitude(), location.getLongitude());
                }
            }
            if (locationManager.isProviderEnabled(locationManager.GPS_PROVIDER)) {
                if (location != null) {
                    sessionManagement.set("GpsLat", location.getLatitude() + "");
                    sessionManagement.set("GpsLog", location.getLongitude() + "");
                    dbGpsLat.setValue(location.getLatitude() + "");
                    dbGpsLog.setValue(location.getLongitude() + "");
                } else {
                    if (locLastKnown != null) {
                        sessionManagement.set("GpsLat", locLastKnown.getLatitude() + "");
                        sessionManagement.set("GpsLog", locLastKnown.getLongitude() + "");
                        dbGpsLat.setValue(locLastKnown.getLatitude() + "");
                        dbGpsLog.setValue(locLastKnown.getLongitude() + "");
                    }
                }
            } else if (locationManager.isProviderEnabled(locationManager.NETWORK_PROVIDER)) {
                if (location != null) {
                    sessionManagement.set("GpsLat", location.getLatitude() + "");
                    sessionManagement.set("GpsLog", location.getLongitude() + "");
                    dbGpsLat.setValue(location.getLatitude() + "");
                    dbGpsLog.setValue(location.getLongitude() + "");
                } else {
                    if (locLastKnown != null) {
                        sessionManagement.set("GpsLat", locLastKnown.getLatitude() + "");
                        sessionManagement.set("GpsLog", locLastKnown.getLongitude() + "");
                        dbGpsLat.setValue(locLastKnown.getLatitude() + "");
                        dbGpsLog.setValue(locLastKnown.getLongitude() + "");
                    }
                }
            } else {
                if (locLastKnown != null) {
                    sessionManagement.set("GpsLat", locLastKnown.getLatitude() + "");
                    sessionManagement.set("GpsLog", locLastKnown.getLongitude() + "");
                    dbGpsLat.setValue(locLastKnown.getLatitude() + "");
                    dbGpsLog.setValue(locLastKnown.getLongitude() + "");
                }
            }
// Simulate network access.
            //Thread.sleep(2000);
        } catch (
                Exception e)

        {

        }

        if (sessionManagement.get("GpsLat") != null && !sessionManagement.get("GpsLat").

                isEmpty() &&
                sessionManagement.get("City") != null && !sessionManagement.get("City").

                isEmpty() &&
                sessionManagement.get("State") != null && !sessionManagement.get("State").

                isEmpty() &&
                sessionManagement.get("Type") != null && !sessionManagement.get("Type").

                isEmpty() &&
                sessionManagement.get("MyUsername") != null && !sessionManagement.get("MyUsername").

                isEmpty())

        {
            locationManager.removeUpdates(this);

            finish();
            loadPage2 = true;
            //if (!loadPage1 && !loadPage3) {
            startActivity(new Intent(this, MainActivity.class));
            //}
        } else

        {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                return;
            }
            locationManager.requestLocationUpdates(towers, 1, 1, this);
        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {
//        String mainkey=sessionManagement.getUserDetails().get("User");
//        DatabaseReference checkUser = nUsers.child(mainkey);
//        DatabaseReference dbStatus = checkUser.child("Status");
//        dbStatus.setValue("offline");
    }


    @Override
    public void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            showGPSAlertToUser();
        }
        String mainkey = sessionManagement.getUserDetails().get("User");
        DatabaseReference checkUser = nUsers.child(mainkey);
        DatabaseReference dbStatus = checkUser.child("Status");

        geocoder = new Geocoder(this, Locale.getDefault());

        locLastKnown = locationManager.getLastKnownLocation(towers);

        DatabaseReference dbLocName = checkUser.child("LocName");

        if (locLastKnown != null) {
            try {
                addresses = geocoder.getFromLocation(locLastKnown.getLatitude(), locLastKnown.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                if (addresses != null && addresses.size() > 0) {
                    address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                    city = addresses.get(0).getLocality();
                    state = addresses.get(0).getAdminArea();
                    country = addresses.get(0).getCountryName();
                    postalCode = addresses.get(0).getPostalCode();
                    knownName = addresses.get(0).getFeatureName(); // Only
                    if (knownName != null) {
                        sessionManagement.set("LocName", knownName);
                        dbLocName.setValue(knownName);
                    }
                    if (address != null)
                        sessionManagement.set("Address", address);
                    if (city != null)
                        sessionManagement.set("City", city);
                    if (country != null)
                        sessionManagement.set("Country", country);
                    if (state != null)
                        sessionManagement.set("State", state);
                } else {
                    //  getCurrentLocationViaJSON(locLastKnown.getLatitude(), locLastKnown.getLongitude());
                }

            } catch (Exception rt) {
                city = getLocationTypeName(locLastKnown.getLatitude(), locLastKnown.getLongitude(), "city");
                state = getLocationTypeName(locLastKnown.getLatitude(), locLastKnown.getLongitude(), "state");
                country = getLocationTypeName(locLastKnown.getLatitude(), locLastKnown.getLongitude(), "country");
                if (city != null && state != null) {
                    if (city.equalsIgnoreCase("OVER_QUERY_LIMIT") || state.equalsIgnoreCase("OVER_QUERY_LIMIT")) {
                        pd.dismiss();
                        AlertDialog.Builder builder = new AlertDialog.Builder(SetAllData.this);
                        builder.setMessage("OVER_QUERY_LIMIT")
                                .setCancelable(true)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        System.exit(0);
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                    } else {

                        if (!city.isEmpty() && city != null) {
                            sessionManagement.set("City", city);
                            FirebaseMessaging.getInstance().subscribeToTopic("/topics/City" + city);
                            dbCity.setValue(city);
                        }
                        if (state != null && !state.isEmpty()) {
                            sessionManagement.set("State", state);
                            FirebaseMessaging.getInstance().subscribeToTopic("/topics/State" + state);
                            dbState.setValue(state);
                        }
                        if (country != null && !country.isEmpty()) {
                            sessionManagement.set("Country", country);
                            dbCountry.setValue(country);
                        }
                    }
                } else {
                    pd.dismiss();
                }
                Crashlytics.logException(rt);

            }
        }
        dbStatus.setValue("online");

        if (sessionManagement.get("GpsLat") != null && !sessionManagement.get("GpsLat").isEmpty() &&
                sessionManagement.get("City") != null && !sessionManagement.get("City").isEmpty() &&
                sessionManagement.get("State") != null && !sessionManagement.get("State").isEmpty() &&
                sessionManagement.get("Type") != null && !sessionManagement.get("Type").isEmpty() &&
                sessionManagement.get("MyUsername") != null && !sessionManagement.get("MyUsername").isEmpty()) {
            locationManager.removeUpdates(this);
            finish();
            loadPage3 = true;
            if (!loadPage2 && !loadPage1) {
                startActivity(new Intent(this, MainActivity.class));
            }
        }
//        else{
//            startActivity(new Intent(this, MainActivity.class));
//        }
        locationManager.requestLocationUpdates(towers, 1, 1, this);

    }

    private void showGPSAlertToUser() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(" LobsChat GPS ACTIVATION");
        builder.setMessage("GPS is disabled in your device, Goto Settings Page To Enable GPS")
                .setCancelable(true)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE");
                            intent.putExtra("enabled", true);
                            sendBroadcast(intent);
                        } catch (Exception rex) {
                            Intent callGPSSettingIntent = new Intent(
                                    android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(callGPSSettingIntent);
                        }
                        try {
                            String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
                            if (!provider.contains("gps")) { //if gps is disabled
                                final Intent poke = new Intent();
                                poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
                                poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
                                poke.setData(Uri.parse("3"));
                                sendBroadcast(poke);
                            }
                        } catch (Exception re) {

                            Intent callGPSSettingIntent = new Intent(
                                    android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(callGPSSettingIntent);
                        }
                    }
                });
        builder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            dialog.cancel();
                        } else {
                            finish();
                            System.exit(0);
                        }
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();

    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        locationManager.removeUpdates(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(this);
    }


    public static String getLocationTypeName(double lat, double lon, String type) {
        JSONObject result = getLocationFormGoogle(lat + "," + lon);

        return getTypeAddress(result, type);
    }

    protected static JSONObject getLocationFormGoogle(String placesName) {

        String apiRequest = "https://maps.googleapis.com/maps/api/geocode/json?latlng=" + placesName; //+ "&ka&sensor=false"

        HttpGet httpGet = new HttpGet(apiRequest);
        DefaultHttpClient client = new DefaultHttpClient();
        HttpResponse response;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            InputStream stream = entity.getContent();
            int b;
            while ((b = stream.read()) != -1) {
                stringBuilder.append((char) b);
            }

        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(stringBuilder.toString());
        } catch (JSONException e) {

            e.printStackTrace();
        }

        return jsonObject;
    }

    protected static String getTypeAddress(JSONObject result, String type) {
        try {
            if (result.has("results")) {
                try {
                    JSONArray array = result.getJSONArray("results");
                    if (array.length() > 0) {
                        JSONObject place = array.getJSONObject(0);
                        JSONArray components = place.getJSONArray("address_components");
                        for (int i = 0; i < components.length(); i++) {
                            JSONObject component = components.getJSONObject(i);
                            JSONArray types = component.getJSONArray("types");
                            for (int j = 0; j < types.length(); j++) {
                                if (types.getString(j).equals("locality") && type.equalsIgnoreCase("city")) {
                                    return component.getString("long_name");
                                } else if (types.getString(j).equals("administrative_area_level_1") && type.equalsIgnoreCase("state")) {
                                    return component.getString("long_name");
                                } else if (types.getString(j).equals("country") && type.equalsIgnoreCase("country")) {
                                    return component.getString("long_name");
                                } else if (types.getString(j).equals("postal_code") && type.equalsIgnoreCase("postal")) {
                                    return component.getString("long_name");
                                }
                            }
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    return result.getString("status");
                } catch (JSONException e) {

                    e.printStackTrace();
                }

            }
        } catch (Exception ex) {

        }
        return null;
    }


    public class AsyncLocation extends AsyncTask<Void, Void, Boolean> {
        String placesName;
        String type;

        AsyncLocation(double lat, double lon, String type) {

            this.placesName = lat + "," + lon;
            this.type = type;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {

            boolean result = false;
            String apiRequest = "https://maps.googleapis.com/maps/api/geocode/json?latlng=" + placesName; //+ "&ka&sensor=false"

//       AsyncHttpClient client = new AsyncHttpClient();
//       client.setMaxRetriesAndTimeout(3, 5000); //old
//        client.addHeader("Accept-Encoding", "identity"); // disable gzip
//       client.setTimeout(600000);
//       RequestParams params = new RequestParams();
////        params.put("companyname", this.companycode);
//
//        final JSONObject[] jsonObject = {null};
//    client.get(apiRequest, new JsonHttpResponseHandler() {
//        @Override
//        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//            super.onSuccess(statusCode, headers, response);
//            try {
//                jsonObject[0] = new JSONObject(String.valueOf(response));
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//
//        }
//
//        @Override
//        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
//            super.onFailure(statusCode, headers, throwable, errorResponse);
//
//            if (statusCode == 404) {
////                  Toast.makeText(SetAllData.this.getApplicationContext(), "Requested resource not found", 1).show();
//            } else if (statusCode == 500) {
//                //  Toast.makeText(SetAllData.this.getApplicationContext(), "Something went wrong at server end", 1).show();
//            } else {
//                //Toast.makeText(SetAllData.this.getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet]", 1).show();
//            }
//        }
//    });


            HttpGet httpGet = new HttpGet(apiRequest);
            DefaultHttpClient client = new DefaultHttpClient();
            HttpResponse response;
            StringBuilder stringBuilder = new StringBuilder();

            try {
                response = client.execute(httpGet);
                HttpEntity entity = response.getEntity();
                InputStream stream = entity.getContent();
                int b;
                while ((b = stream.read()) != -1) {
                    stringBuilder.append((char) b);
                }
                result = true;
            } catch (ClientProtocolException e) {
                result = false;
            } catch (IOException e) {
                result = false;
            }

            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(stringBuilder.toString());
                result = true;
            } catch (JSONException e) {
                e.printStackTrace();
                result = false;
            }

            getTypeAddress(jsonObject, type);
            city = getLocationTypeName(locLastKnown.getLatitude(), locLastKnown.getLongitude(), "city");
            state = getLocationTypeName(locLastKnown.getLatitude(), locLastKnown.getLongitude(), "state");
            country = getLocationTypeName(locLastKnown.getLatitude(), locLastKnown.getLongitude(), "country");


            if (!city.isEmpty()) {
                sessionManagement.set("City", city);
                FirebaseMessaging.getInstance().subscribeToTopic("/topics/City" + city);
                dbCity.setValue(city);
            }
            if (state != null && !state.isEmpty()) {
                sessionManagement.set("State", state);
                FirebaseMessaging.getInstance().subscribeToTopic("/topics/State" + state);
                dbState.setValue(state);
            }
            if (country != null && !country.isEmpty()) {
                sessionManagement.set("Country", country);
                dbCountry.setValue(country);
            }


//            return jsonObject;
            return result;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

        }
    }

}
