package lobschat.hycode.lobschat;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;


/**
 * Created by HyCode on 12/23/2017.
 */

public class Join extends AppCompatActivity {
    boolean exist = false;
    Button btnJoin;
    public static String userPresent;
    public static int useridPresent;
    AutoCompleteTextView login_email;
    EditText login_pass;
    SessionManagement session;
    static String rlogin_email, ruser, rlogin_pass;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 1001;
    boolean loginSuccess = false;
    private UserLoginTask mAuthTask = null;

    protected LocationManager locationManager;
    boolean boolVal = true;
    View mRegFormView;
    public static boolean welcomeScreenIsShown = true;
    DatabaseReference nRootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference dblobschat = nRootRef.child("lobschat");
    DatabaseReference nusers = dblobschat.child("users");
    private AdView adView;
    GoogleApiClient mGoogleApiClient;
    int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    ProgressDialog pd;
    TextView regLogin;
    String androidID;
    static String mainKey;
    private FirebaseAuth auth;
    String TodayDate;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}$", Pattern.CASE_INSENSITIVE);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkPlayServices();
        androidID = android.provider.Settings.System.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        session = new SessionManagement(Join.this);
        welcomeScreenIsShown = false;
        auth = FirebaseAuth.getInstance();
        if (ActivityCompat.checkSelfPermission(Join.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(Join.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Join.this, new String[]{ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);

        }
//        checkGps();
        loginSuccess = session.checkLogin();
        if (loginSuccess) {
            finish();
            startHome();

        }
        setContentView(R.layout.join);
        btnJoin = (Button) findViewById(R.id.btnJoin);
        login_email = (AutoCompleteTextView) findViewById(R.id.login_email);
        login_pass = (EditText) findViewById(R.id.login_pass);
        regLogin = (TextView) findViewById(R.id.regLogin);
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/ERASMD.TTF");
        btnJoin.setTypeface(typeface);
        login_pass.setTypeface(typeface);
        login_email.setTypeface(typeface);
        regLogin.setTypeface(typeface);
        addAdapterToViews();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(" LobsChat ");
        // toolbar.setColsetBackground(new ColorDrawable(Color.parseColor("#0000ff")));
        setSupportActionBar(toolbar);
        // Display icon in the toolbar
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.logo_sm);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        // Sample AdMob app ID: ca-app-pub-3940256099942544~3347511713
//                String id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        MobileAds.initialize(this, "ca-app-pub-5527620381143347~8634487311");
        adView = (AdView) findViewById(R.id.adView);
//        adView.setAdSize(AdSize.SMART_BANNER);
//        adView.setAdUnitId("ca-app-pub-5527620381143347/8001407719");

//        AdRequest adRequest = new AdRequest.Builder().addTestDevice(id).build();
        AdRequest adRequest = new AdRequest.Builder().build();

        adView.loadAd(adRequest);

        regLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(Join.this, Register.class));
            }
        });


        btnJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int rs = 0;
                rlogin_email = login_email.getText().toString();
                rlogin_pass = login_pass.getText().toString();

                login_email.setError(null);
                login_pass.setError(null);

                if (TextUtils.isEmpty(rlogin_email)) {
                    login_email.setError(getString(R.string.error_field_required));
                } else if (TextUtils.isEmpty(rlogin_pass)) {
                    login_pass.setError(getString(R.string.error_field_required));
                } else if (rlogin_pass.length() < 6) {
                    login_pass.setError(getString(R.string.minimum_password));
                } else {

                    pd = new ProgressDialog(Join.this);
                    pd.setMessage("Loading...");
                    pd.show();
                    pd.setCancelable(false);
                    pd.setCanceledOnTouchOutside(false);
//                    if(androidID==null)androidID="001234500";
//                    mainKey =  rlogin_email+"--__" + androidID;
                    mAuthTask = new UserLoginTask(rlogin_email, rlogin_pass);
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
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }


    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String remailBg, rpassBg;

        UserLoginTask(String emailBg, String passBg) {
            remailBg = emailBg;
            rpassBg = passBg;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {

                auth.signInWithEmailAndPassword(remailBg, rpassBg)
                        .addOnCompleteListener(Join.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                //progressBar.setVisibility(View.GONE);
                                auth = FirebaseAuth.getInstance();
                                if (!task.isSuccessful()) {
                                    // there was an error

                                    boolVal = false;
                                } else {
                                    boolVal = true;

                                }
                            }
                        });
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Toast.makeText(Join.this, "Auth: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return false;
            }

            // TODO: register the new account here.
            return boolVal;
        }


        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;

            if (success) {
                if (auth.getCurrentUser() != null) {
                    if (auth.getCurrentUser().getEmail().equalsIgnoreCase(rlogin_email)) {
                        if (!checkIfEmailVerified()) {
                            pd.dismiss();
                            AlertDialog.Builder builder = new AlertDialog.Builder(Join.this);
                            builder.setTitle("LobsChat");
                            builder.setMessage("Email not verified, verify email and try again")
                                    .setCancelable(true)
                                    .setPositiveButton("OK", null);
                            AlertDialog alert = builder.create();
                            alert.show();

                        } else {
                            loginSuccess = true;
                            session.createLoginSession(rlogin_email
                                    .replace(".", "_")
                                    .replace("#", "_")
                                    .replace("$", "_")
                                    .replace("[", "_")
                                    .replace("]", "_")
                                    .replace("@", "_"));
                            session.createLoginSessionMainEmail(rlogin_email);
                            finish();
                            startHome();
                            pd.dismiss();
                        }

                    } else {
                        noNetworkAuth();
                    }
                } else {
                    noNetworkAuth();
                }
            } else {
                if (isNetworkAvailable()) {
                    notAuth();
                } else {
                    networkError();
                }

            }

        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            pd.dismiss();
        }
    }


    private boolean canToggleGPS() {
        PackageManager pacman = getPackageManager();
        PackageInfo pacInfo = null;

        try {
            pacInfo = pacman.getPackageInfo("com.android.settings", PackageManager.GET_RECEIVERS);
        } catch (PackageManager.NameNotFoundException e) {
            return false; //package not found
        }

        if (pacInfo != null) {
            for (ActivityInfo actInfo : pacInfo.receivers) {
                //test if recevier is exported. if so, we can toggle GPS.
                if (actInfo.name.equals("com.android.settings.widget.SettingsAppWidgetProvider") && actInfo.exported) {
                    return true;
                }
            }
        }

        return false; //default
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adView != null) {
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

    void notAuth() {
        pd.dismiss();
        AlertDialog.Builder builder = new AlertDialog.Builder(Join.this);
        builder.setTitle("LobsChat");
        builder.setMessage(R.string.auth_failed)
                .setCancelable(true)
                .setPositiveButton("OK", null);
        AlertDialog alert = builder.create();
        alert.show();
    }

    void noNetworkAuth() {
        mAuthTask = new UserLoginTask(rlogin_email, rlogin_pass);
        mAuthTask.execute((Void) null);
//

    }

    void networkError() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Join.this);
        builder.setMessage("Network connection error")
                .setCancelable(true)
                .setPositiveButton("OK", null);
        AlertDialog alert = builder.create();
        alert.show();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    private boolean checkIfEmailVerified() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user.isEmailVerified()) {

            // user is verified, so you can finish this activity or send user to activity which you want.

            return true;
        } else {
            // email is not verified, so just prompt the message to the user and restart this activity.
            // NOTE: don't forget to log out the user.
            FirebaseAuth.getInstance().signOut();
            return false;
            //restart this activity

        }
    }

    public void startHome() {
        Intent intent;
        if (session.get("GpsLat") != null && !session.get("GpsLat").isEmpty() &&
                session.get("City") != null && !session.get("City").isEmpty() &&
                session.get("State") != null && !session.get("State").isEmpty() &&
                session.get("Type") != null && !session.get("Type").isEmpty() &&
                session.get("MyUsername") != null && !session.get("MyUsername").isEmpty()
                ) {
            if (session.get("Type").equalsIgnoreCase("Artisan")) {
                nusers.child(session.getUserDetails().get("User")).child("TodayDate").setValue(ServerValue.TIMESTAMP);
                try {

                    nusers.child(session.getUserDetails().get("User")).child("TodayDate").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            try {
                                TodayDate = dataSnapshot.getValue().toString();
                                session.set("TodayDate", TodayDate);
                            } catch (Exception er) {
                                Toast.makeText(Join.this, er.getMessage(), Toast.LENGTH_LONG).show();

                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                } catch (Exception er) {
                }
                try {
                    long subDaysLeft = Integer.parseInt(session.get("SubDaysLeft"));
                    String todayDate = session.get("TodayDate");
                    String SubStatus = session.get("SubStatus");
                    String SubDate = session.get("SubDate");
                    //todaydate-subdate
                    try {

                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        Date sd = dateFormat.parse(SubDate);
                        Date cd = dateFormat.parse(todayDate);
                        long subUpdatesd = sd.getTime() / (24 * 60 * 60);
                        long currentDatecd = cd.getTime() / (24 * 60 * 60);
                        long dateDiff = currentDatecd - subUpdatesd;
                        subDaysLeft = subDaysLeft - dateDiff;

                    } catch (Exception ex) {

                    }
                    if (subDaysLeft <= 0) {
                        nusers.child(session.getUserDetails().get("User")).child("SubDaysLeft").setValue("0");
                        nusers.child(session.getUserDetails().get("User")).child("SubStatus").setValue("Expired");
                    }
                } catch (Exception ex) {

                }
            }
//            if(subDaysLeft>0){subDaysLeft-1}

            intent = new Intent(Join.this, MainActivity.class);
        } else {
            intent = new Intent(Join.this, SetAllData.class);
        }
        startActivity(intent);
    }

    private void addAdapterToViews() {

        Account[] accounts = AccountManager.get(this).getAccounts();
        Set<String> emailSet = new HashSet<String>();
        for (Account account : accounts) {
            if (EMAIL_PATTERN.matcher(account.name).matches()) {
                emailSet.add(account.name);
            }
        }
        login_email.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<String>(emailSet)));

    }
}



