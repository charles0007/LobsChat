package lobschat.hycode.lobschat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.Window;
import android.view.WindowManager;

import com.google.firebase.database.FirebaseDatabase;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CALL_PHONE;

/**
 * Created by HyCode on 06-May-16.
 */
public class Splash extends Activity {


    protected LocationManager locationManager;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 1001;
    private static final int MY_PERMISSIONS_CALL_PHONE = 1005;
    static int flag = 0;
    SessionManagement sessionManagement;
    static boolean calledAlready = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManagement = new SessionManagement(this);
        if (Join.welcomeScreenIsShown) {
            // Open your Main Activity

            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);

            Thread timer = new Thread() {
                public void run() {
                    try {
                        sleep(3000);


                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        acceptPermit();
//                        Intent spl = new Intent(Splash.this, CabLogin.class);
//                        startActivity(spl);
//                        finish();
                    }
                }
            };
            timer.start();

            setContentView(R.layout.splash);
        } else {
            acceptPermit();
//            Intent spl = new Intent(Splash.this, CabLogin.class);
//            startActivity(spl);
//            finish();
        }

        try {
            if (!calledAlready) {
                FirebaseDatabase.getInstance().setPersistenceEnabled(true);
                calledAlready = true;
            }
        } catch (Exception ex) {

        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (flag == 0) {
            flag = 1;
        } else {
            flag = 1;
            acceptPermit();
//            Intent spl = new Intent(Splash.this, CabLogin.class);
//            startActivity(spl);
//            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    private void showGPSDisabledAlertToUser() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it?")
                .setCancelable(false)
                .setPositiveButton("Goto Settings Page To Enable GPS",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent callGPSSettingIntent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(callGPSSettingIntent);

                            }
                        });
        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
//                        dialog.cancel();
                        finish();
                        // moveTaskToBack(true);
                        // android.os.Process.killProcess(android.os.Process.myPid());
                        System.exit(0);
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    public void acceptPermit() {

//        if(sessionManagement.get("login_type").equalsIgnoreCase("joinartisan")){
//            startActivity(new Intent(Splash.this, JoinArtisan.class));
//        }else{
        finish();
        startActivity(new Intent(Splash.this, Join.class));

//        startActivity(new Intent(Splash.this, Intro.class));
//        }

    }


}
