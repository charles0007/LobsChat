package lobschat.hycode.lobschat;

import android.annotation.SuppressLint;
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
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.firebase.client.Firebase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

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

import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;

/**
 * Created by HyCode on 3/8/2018.
 */

public class UpdateLocation implements LocationListener {
    TextView noUsersText;
    ArrayList<String> al = new ArrayList<>();

    ProgressDialog pd;
    ListView list;

    ArrayList<HashMap<String, String>> dataList,dataGroupList;

    DatabaseReference nRootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference dblobschat = nRootRef.child("lobschat");
    DatabaseReference nUsers = dblobschat.child("users");

    SessionManagement sessionManagement;
    String address,city,state,knownName,country,postalCode;
    Geocoder geocoder;
    List<Address> addresses;
    Location locLastKnown ;
    protected LocationManager locationManager;
    protected Context context;
//    boolean isNetworkEnabled;
    String towers;
    Context _context;
    String _type;

    // Constructor
    @SuppressLint("MissingPermission")
    public UpdateLocation(final Context context,String type){
        this._context = context;

        sessionManagement = new SessionManagement(context);
        locationManager = (LocationManager) _context.getSystemService(Context.LOCATION_SERVICE);
//        isNetworkEnabled = locationManager.isProviderEnabled(NETWORK_PROVIDER);
        Criteria crit = new Criteria();
        towers = locationManager.getBestProvider(crit, false);
        locLastKnown = locationManager.getLastKnownLocation(towers);
    if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ) {
        locationManager.requestLocationUpdates(GPS_PROVIDER, 0, 0, this);
    }else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ) {
            locationManager.requestLocationUpdates(NETWORK_PROVIDER, 0, 0, this);
        }
        geocoder = new Geocoder(context, Locale.getDefault());
        pd = new ProgressDialog(context);
        pd.setMessage("Refreshing current "+type.toLowerCase()+" location...");
        pd.show();
        pd.setCanceledOnTouchOutside(true);
        pd.setCancelable(true);

        pd.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                locationManager.removeUpdates(UpdateLocation.this);
            }
        });
    }

    @SuppressLint("MissingPermission")
    public void refresh(String type){
        this._type=type;

        String mainkey = sessionManagement.getUserDetails().get("User");

        DatabaseReference checkUser = nUsers.child(mainkey);
        DatabaseReference dbGpsLat = checkUser.child("GpsLat");
        DatabaseReference dbGpsLog = checkUser.child("GpsLog");
        DatabaseReference dbCity = checkUser.child("City");
        DatabaseReference dbState = checkUser.child("State");
        DatabaseReference dbCountry = checkUser.child("Country");

        if(locLastKnown!=null) {
            if (type.equalsIgnoreCase("Local")) {
                sessionManagement.set("GpsLat", locLastKnown.getLatitude() + "");
                sessionManagement.set("GpsLog", locLastKnown.getLongitude() + "");
                dbGpsLat.setValue(locLastKnown.getLatitude() + "");
                dbGpsLog.setValue(locLastKnown.getLongitude() + "");
                pd.dismiss();
                locationManager.removeUpdates(this);
            } else {

                try {
                    addresses = geocoder.getFromLocation(locLastKnown.getLatitude(), locLastKnown.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                    if (addresses != null && addresses.size() > 0) {
                        city = addresses.get(0).getLocality();
                        state = addresses.get(0).getAdminArea();
                        country = addresses.get(0).getCountryName();
                        if (city != null && type.equalsIgnoreCase("City")) {
                            String topic = "/topics/City" + sessionManagement.get("City");
                            FirebaseMessaging.getInstance().unsubscribeFromTopic(topic);
                            sessionManagement.set("City", city);
                            topic = "/topics/City" + city;
                            FirebaseMessaging.getInstance().subscribeToTopic(topic);
                            dbCity.setValue(city);
                            pd.dismiss();
                            locationManager.removeUpdates(this);
                        } else if (state != null && type.equalsIgnoreCase("State")) {
                            String topic = "/topics/State" + sessionManagement.get("State");
                            FirebaseMessaging.getInstance().unsubscribeFromTopic(topic);
                            sessionManagement.set("State", state);
                            topic = "/topics/State" + state;
                            FirebaseMessaging.getInstance().subscribeToTopic(topic);
                            dbState.setValue(state);
                            pd.dismiss();
                            locationManager.removeUpdates(this);
                        }
                        if (country != null ) {
                            sessionManagement.set("Country", country);
                            dbCountry.setValue(country);
                        }

                    }
                } catch (IOException e) {
                    city = getLocationTypeName(locLastKnown.getLatitude(),locLastKnown.getLongitude(),"city");
                    state = getLocationTypeName(locLastKnown.getLatitude(),locLastKnown.getLongitude(),"state");
                    country = getLocationTypeName(locLastKnown.getLatitude(),locLastKnown.getLongitude(),"country");

                    if (city != null && type.equalsIgnoreCase("City")) {
                        String topic = "/topics/City" + sessionManagement.get("City");
                        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic);
                        sessionManagement.set("City", city);
                        topic = "/topics/City" + city;
                        FirebaseMessaging.getInstance().subscribeToTopic(topic);
                        dbCity.setValue(city);

                        pd.dismiss();
                        locationManager.removeUpdates(this);
                    } else if (state != null && type.equalsIgnoreCase("State")) {
                        String topic = "/topics/State" + sessionManagement.get("State");
                        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic);
                        sessionManagement.set("State", state);
                        topic = "/topics/State" + state;
                        FirebaseMessaging.getInstance().subscribeToTopic(topic);
                        dbState.setValue(state);
                        pd.dismiss();
                        locationManager.removeUpdates(this);
                    }
                    if (country != null ) {
                        sessionManagement.set("Country", country);
                        dbCountry.setValue(country);
                    }
                    Log.d("TAG1", e.getMessage());
                    e.printStackTrace();


                } catch (Exception rt) {
                    Crashlytics.logException(rt);
                    Log.d("TAG2", rt.getMessage());
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
            DatabaseReference dbCity = checkUser.child("City");
            DatabaseReference dbState = checkUser.child("State");
            DatabaseReference dbCountry = checkUser.child("Country");
            if (location != null) {
                try {
                    if(_type.equalsIgnoreCase("Local")) {
                        sessionManagement.set("GpsLat", location.getLatitude() + "");
                        sessionManagement.set("GpsLog", location.getLongitude() + "");
                        dbGpsLat.setValue(location.getLatitude() + "");
                        dbGpsLog.setValue(location.getLongitude() + "");
                        pd.dismiss();
                        locationManager.removeUpdates(this);

                    }
                    addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                    if (addresses != null && addresses.size() > 0) {
                        city = addresses.get(0).getLocality();
                        state = addresses.get(0).getAdminArea();
                        country = addresses.get(0).getCountryName();

                        if (city != null && _type.equalsIgnoreCase("City")) {
                            String topic = "/topics/City" + sessionManagement.get("City");
                            FirebaseMessaging.getInstance().unsubscribeFromTopic(topic);
                            sessionManagement.set("City",city);
                            dbCity.setValue(city);
                            topic = "/topics/City" + city;
                            FirebaseMessaging.getInstance().subscribeToTopic(topic);
                            pd.dismiss();
                            locationManager.removeUpdates(this);

                        }
                        else if (state != null && _type.equalsIgnoreCase("State")) {
                            String topic = "/topics/State" + sessionManagement.get("State");
                            FirebaseMessaging.getInstance().unsubscribeFromTopic(topic);
                            sessionManagement.set("State",state);
                            topic = "/topics/State" + state;
                            FirebaseMessaging.getInstance().subscribeToTopic(topic);
                            dbState.setValue(state);
                            pd.dismiss();
                            locationManager.removeUpdates(this);

                        }
                        if (country != null ) {
                            sessionManagement.set("Country", country);
                            dbCountry.setValue(country);
                        }


                    }
                } catch (IOException e) {
                    city = getLocationTypeName(locLastKnown.getLatitude(),locLastKnown.getLongitude(),"city");
                    state = getLocationTypeName(locLastKnown.getLatitude(),locLastKnown.getLongitude(),"state");
                    country = getLocationTypeName(locLastKnown.getLatitude(),locLastKnown.getLongitude(),"country");

                    if (city != null && _type.equalsIgnoreCase("City")) {
                        String topic = "/topics/City" + sessionManagement.get("City");
                        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic);
                        sessionManagement.set("City", city);
                         topic = "/topics/City" + city;
                        FirebaseMessaging.getInstance().subscribeToTopic(topic);
                        dbCity.setValue(city);

                        pd.dismiss();
                        locationManager.removeUpdates(this);
                    } else if (state != null && _type.equalsIgnoreCase("State")) {
                        String topic = "/topics/State" + sessionManagement.get("State");
                        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic);
                        sessionManagement.set("State", state);
                        topic = "/topics/State" + state;
                        FirebaseMessaging.getInstance().subscribeToTopic(topic);
                        dbState.setValue(state);
                        pd.dismiss();
                        locationManager.removeUpdates(this);
                    }
                    if (country != null ) {
                        sessionManagement.set("Country", country);
                        dbCountry.setValue(country);
                    }
                    e.printStackTrace();

                } catch (Exception rt) {
                    city = getLocationTypeName(locLastKnown.getLatitude(),locLastKnown.getLongitude(),"city");
                    state = getLocationTypeName(locLastKnown.getLatitude(),locLastKnown.getLongitude(),"state");
                    country = getLocationTypeName(locLastKnown.getLatitude(),locLastKnown.getLongitude(),"country");

                    if (city != null && _type.equalsIgnoreCase("City")) {
                        String topic = "/topics/City" + sessionManagement.get("City");
                        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic);
                        sessionManagement.set("City", city);
                        topic = "/topics/City" + city;
                        FirebaseMessaging.getInstance().subscribeToTopic(topic);
                        dbCity.setValue(city);

                        pd.dismiss();
                        locationManager.removeUpdates(this);
                    } else if (state != null && _type.equalsIgnoreCase("State")) {
                        String topic = "/topics/State" + sessionManagement.get("State");
                        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic);
                        sessionManagement.set("State", state);
                        topic = "/topics/State" + state;
                        FirebaseMessaging.getInstance().subscribeToTopic(topic);
                        dbState.setValue(state);
                        pd.dismiss();
                        locationManager.removeUpdates(this);
                    }
                    if (country != null ) {
                        sessionManagement.set("Country", country);
                        dbCountry.setValue(country);
                    }
                    Log.d("TAG4", rt.getMessage());
                }
            }
            if(locationManager.isProviderEnabled(locationManager.GPS_PROVIDER)) {
                if (location != null && _type.equalsIgnoreCase("Local")) {
                        sessionManagement.set("GpsLat", location.getLatitude() + "");
                        sessionManagement.set("GpsLog", location.getLongitude() + "");
                        dbGpsLat.setValue(location.getLatitude() + "");
                        dbGpsLog.setValue(location.getLongitude() + "");
                    pd.dismiss();
                    locationManager.removeUpdates(this);

                }
                else{
                    if(locLastKnown!=null && _type.equalsIgnoreCase("Local")){
                        sessionManagement.set("GpsLat",locLastKnown.getLatitude() + "");
                        sessionManagement.set("GpsLog",locLastKnown.getLongitude() + "");
                        dbGpsLat.setValue(locLastKnown.getLatitude() + "");
                        dbGpsLog.setValue(locLastKnown.getLongitude() + "");
                        pd.dismiss();
                        locationManager.removeUpdates(this);

                    }
                }
            }else if(locationManager.isProviderEnabled(locationManager.NETWORK_PROVIDER)){

                if (location != null && _type.equalsIgnoreCase("Local")) {
                    sessionManagement.set("GpsLat", location.getLatitude() + "");
                    sessionManagement.set("GpsLog", location.getLongitude() + "");
                    dbGpsLat.setValue(location.getLatitude() + "");
                    dbGpsLog.setValue(location.getLongitude() + "");
                    pd.dismiss();
                    locationManager.removeUpdates(this);

                }
                else{
                    if(locLastKnown!=null && _type.equalsIgnoreCase("Local")){
                        sessionManagement.set("GpsLat",locLastKnown.getLatitude() + "");
                        sessionManagement.set("GpsLog",locLastKnown.getLongitude() + "");
                        dbGpsLat.setValue(locLastKnown.getLatitude() + "");
                        dbGpsLog.setValue(locLastKnown.getLongitude() + "");
                        pd.dismiss();
                        locationManager.removeUpdates(this);

                    }
                }
            }
            else{
                if(locLastKnown!=null && _type.equalsIgnoreCase("Local")){
                    sessionManagement.set("GpsLat",locLastKnown.getLatitude() + "");
                    sessionManagement.set("GpsLog",locLastKnown.getLongitude() + "");
                    dbGpsLat.setValue(locLastKnown.getLatitude() + "");
                    dbGpsLog.setValue(locLastKnown.getLongitude() + "");
                    pd.dismiss();
                    locationManager.removeUpdates(this);
                }
            }
// Simulate network access.
            //Thread.sleep(2000);
        } catch (Exception e) {
            Crashlytics.logException(e);
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

    }


    public static  String getLocationTypeName(double lat, double lon,String type) {
        JSONObject result = getLocationFormGoogle(lat + "," + lon);
        return getTypeAddress(result,type);
    }

    protected static JSONObject getLocationFormGoogle(String placesName) {

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

        } catch (ClientProtocolException e) {
        } catch (IOException e) {
        }

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(stringBuilder.toString());
        } catch (JSONException e) {

            e.printStackTrace();
        }

        return jsonObject;
    }

    protected static String getTypeAddress(JSONObject result,String type) {
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
                            }else if (types.getString(j).equals("administrative_area_level_1") && type.equalsIgnoreCase("state")) {
                                return component.getString("long_name");
                            }
                            else if (types.getString(j).equals("country") && type.equalsIgnoreCase("country")) {
                                return component.getString("long_name");
                            }else if (types.getString(j).equals("postal_code") && type.equalsIgnoreCase("postal")) {
                                return component.getString("long_name");
                            }
                        }
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return null;
    }



}
