package lobschat.hycode.lobschat;

/**
 * Created by HyCode on 12/22/2017.
 */


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.bumptech.glide.module.AppGlideModule;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
//    protected Context context;


    SessionManagement sessionManagement;
//    String address,city,state,knownName,country,postalCode;
//    Geocoder geocoder;
//    sendRegTask mAuthTask;
DatabaseReference nusers;
    DatabaseReference newuser;
    DatabaseReference dbToken;
    DatabaseReference nRootRef;
    DatabaseReference dblobschat;

    static boolean calledAlready=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try{
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        }catch (Exception ex){}

         nRootRef= FirebaseDatabase.getInstance().getReference();
         dblobschat=nRootRef.child("lobschat");

        sessionManagement=new SessionManagement(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

//        mAuthTask = new sendRegTask();
//        mAuthTask.execute((Void) null);
        try {
             nusers = dblobschat.child("users");
             newuser = nusers.child(sessionManagement.get(sessionManagement.KEY_NAME));
             dbToken = newuser.child("Token");
            dbToken.setValue(sessionManagement.get("Token"));
        }catch(Exception ex){

        }

        // Display icon in the toolbar
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setHomeButtonEnabled(true);
//        getSupportActionBar().setIcon(R.drawable.logof);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        final ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(),this);
        viewPager.setAdapter(adapter);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);


        /**
         * on swiping the viewpager make respective tab selected
         * */
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                // on changing the page
                // make respected tab selected
//                viewPager.setCurrentItem(position);

            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });
    }

//    @Override
    public void onBackPressedd() {

        int cout=getSupportFragmentManager().getBackStackEntryCount();
        Toast.makeText(this,cout+" rrr",Toast.LENGTH_LONG).show();
        if(cout==0){
            super.onBackPressed();
        }else{
            getFragmentManager().popBackStack();

            finish();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onStop() {
        super.onStop();

    }

}