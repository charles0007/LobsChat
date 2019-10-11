package lobschat.hycode.lobschat;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;


/**
 * Created by HyCode on 12/23/2017.
 */

public class RegArtisan extends AppCompatActivity{
    boolean exist=false;
    Button btnReg;
    public static String userPresent;
    public static int useridPresent;
    EditText login_user,login_desc;
    SessionManagement session;
    static String rlogin_user,ruser,rpass,rlogin_desc;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 1001;
    boolean loginSuccess=false;
    private UserCabLoginTask mAuthTask = null;

    protected LocationManager locationManager;
    boolean boolVal =false,auth=true;
    View mRegFormView;
    public static boolean welcomeScreenIsShown = true;
    DatabaseReference nRootRef= FirebaseDatabase.getInstance().getReference();
    DatabaseReference dblobschat=nRootRef.child("lobschat");
    DatabaseReference nusers=dblobschat.child("users");
    private AdView adView;
    GoogleApiClient mGoogleApiClient;
    int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    ProgressDialog pd;
    TextView artLogin;
    String androidID;
    static String mainKey;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkPlayServices();
        androidID = android.provider.Settings.System.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        session = new SessionManagement(RegArtisan.this);
        
        setContentView(R.layout.join);
        btnReg = (Button) findViewById(R.id.btnReg);
        login_user = (EditText) findViewById(R.id.login_user);
        artLogin = (TextView) findViewById(R.id.artLogin);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(" LobsChat ");
        // toolbar.setColsetBackground(new ColorDrawable(Color.parseColor("#0000ff")));
        setSupportActionBar(toolbar);
        // Display icon in the toolbar
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.logof);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        // Sample AdMob app ID: ca-app-pub-3940256099942544~3347511713
//                String id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        MobileAds.initialize(this, "ca-app-pub-5527620381143347~2094474910");
        adView = (AdView) findViewById(R.id.adView);
//        adView.setAdSize(AdSize.SMART_BANNER);
//        adView.setAdUnitId("ca-app-pub-5527620381143347/8001407719");

//        AdRequest adRequest = new AdRequest.Builder().addTestDevice(id).build();
        AdRequest adRequest = new AdRequest.Builder().build();

        adView.loadAd(adRequest);

        artLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            finish();
            }
        });



        btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int rs = 0;
                ruser=rlogin_user = login_user.getText().toString()
                        .replace(".", "")
                        .replace("#", "")
                        .replace("$", "")
                        .replace("[", "")
                        .replace("]", "")
                        .replace("@","");
                rlogin_desc = login_desc.getText().toString();

                login_user.setError(null);login_desc.setError(null);

                if (TextUtils.isEmpty(rlogin_user)) {
                    login_user.setError(getString(R.string.error_field_required));
                } else if (rlogin_user.length() < 6) {
                    login_user.setError(getString(R.string.minimum_user));
                }else if (TextUtils.isEmpty(rlogin_desc)) {
                    login_desc.setError(getString(R.string.error_field_required));
                } else if (rlogin_desc.length() < 6) {
                    login_desc.setError("Business description is too short");
                }
                else {
                    pd = new ProgressDialog(RegArtisan.this);
                    pd.setMessage("Loading...");
                    pd.show();
                    pd.setCancelable(false);
                    pd.setCanceledOnTouchOutside(false);
                    if(androidID==null)androidID="001234500";
                    mainKey = rlogin_user+"--__" + androidID;
                    mAuthTask = new UserCabLoginTask(mainKey,rlogin_desc);
                    mAuthTask.execute((Void) null);

                }

            }
        });

    }



    // Menu icons are inflated just as they were with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_join, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_exit:
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle("Exit LobsChat?");
                alertDialogBuilder
                        .setMessage("Click yes to exit!")
                        .setCancelable(false)
                        .setPositiveButton("Yes",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        finish();
                                        System.exit(0);
                                    }
                                })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                dialog.cancel();
                            }
                        });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();

                return true;
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
    @Override
    protected void onDestroy(){
        super.onDestroy();

    }

    private void showGPSAlertToUser(){
        AlertDialog.Builder builder = new AlertDialog.Builder(RegArtisan.this);
        builder.setTitle(" GPS ACTIVATION");
        builder.setMessage("GPS is disabled in your device, Goto Settings Page To Enable GPS")
                .setCancelable(false)
                .setPositiveButton("OK",  new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try{
                            Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE");
                            intent.putExtra("enabled", true);
                            sendBroadcast(intent);

                        }catch(Exception rex){
                            try {String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
                                if(!provider.contains("gps")){ //if gps is disabled
                                    final Intent poke = new Intent();
                                    poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
                                    poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
                                    poke.setData(Uri.parse("3"));
                                    sendBroadcast(poke);
                                }
                            }catch(Exception re){
                                Intent callGPSSettingIntent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(callGPSSettingIntent);
                            }}
                        Intent callGPSSettingIntent = new Intent(
                                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(callGPSSettingIntent);

                    }
                });
        builder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                        try{
                            if (session.get("GpsLat")!=null){dialog.cancel();}
                            else{
                                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {dialog.cancel();}
                                else{
                                    finish();
                                    System.exit(0);
                                }

                            }
                        }catch (Exception ft){
                            finish();
                            System.exit(0);}

                    }
                });
        AlertDialog alert = builder.create();
        alert.show();

    }


    public class UserCabLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String ruserBg,rdesc;

        UserCabLoginTask(String userBg,String userdesc) {
            ruserBg = userBg;rdesc=userdesc;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                DatabaseReference checkUser = nusers.child(mainKey);

                DatabaseReference dbUser = checkUser.child("Username");

                dbUser.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        try {
                            if(dataSnapshot.getValue().toString().equals(ruser)){exist=true;}
                            else{exist=false;}

                        }catch (Exception er){exist=false;}

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        exist=false;
                    }
                });

                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                exist= false;
            }

            if (exist) {
                return false;
            }

            // TODO: register the new account here.
            return true;
        }


        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            AlertDialog.Builder builder = new AlertDialog.Builder(RegArtisan.this);
            if (success) {
                DatabaseReference checkUser = nusers.child(mainKey);
                DatabaseReference dbUser = checkUser.child("Username");
                DatabaseReference dbDesc = checkUser.child("Business");
                DatabaseReference dbType = checkUser.child("Type");
//                DatabaseReference dbEmail = checkUser.child("Email");
//                dbEmail.setValue(remail);
                dbUser.setValue(ruser);
                dbDesc.setValue(rdesc);
                dbType.setValue("Artisan");
                session.set("Type","Artisan");
                loginSuccess = true;
                userPresent = ruser;
                session.createLoginSession(mainKey);
                pd.dismiss();
                builder.setMessage(" Registration Successful ")
                        .setCancelable(true)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
//                                session.set("login_type","joinartisan");
                                startActivity(new Intent(RegArtisan.this, SetAllData.class));
                            }
                        });
            } else {
                if(exist){
                    pd.dismiss();
                    builder.setMessage(" User Already exist, try again ")
                            .setCancelable(true)
                            .setPositiveButton("OK", null);
                }
                else if(!isNetworkAvailable()){
                    pd.dismiss();
                    builder.setMessage(" Network Error ")
                            .setCancelable(true)
                            .setPositiveButton("OK", null);

                }else if(!exist){
                    mAuthTask = new UserCabLoginTask(mainKey,rdesc);
                    mAuthTask.execute((Void) null);
                }
                AlertDialog alert = builder.create();
                alert.show();
            }
        }

        @Override
        protected void onCancelled () {
            mAuthTask = null;
            pd.dismiss();
        }
    }




    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (adView!=null){
            adView.resume();
        }
        checkPlayServices();
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                //Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }



}



