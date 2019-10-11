package lobschat.hycode.lobschat;

/**
 * Created by HyCode on 12/26/2017.
 */

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by HyCode on 3/9/2017.
 */

public class UsersList extends AppCompatActivity {
    boolean boolVal;
    TextView abtRemote;
    DatabaseReference nRootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference dblobschat = nRootRef.child("lobschat");
    DatabaseReference nUsers = dblobschat.child("users");
    TabListAdapter adapter;
    int totalUsers = 0, totalUsersGroup = 0;
    TextView noUsersText;
    ProgressDialog pd;
    ArrayList<HashMap<String, String>> dataList, dataGroupList;
    SessionManagement sessionManagement;
    ListView list, group_list;
    String userLatitude, userLongitude;
    FirebaseRemoteConfig remoteConfig;
    double max_distance = 100;
    private AdView adView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        sessionManagement = new SessionManagement(this);
        sessionManagement.set("ListPage", "UsersList");
        setContentView(R.layout.userslist);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        list = (ListView) findViewById(R.id.list);
        dataList = new ArrayList<HashMap<String, String>>();
        dataGroupList = new ArrayList<HashMap<String, String>>();
        noUsersText = (TextView) findViewById(R.id.noUsersText);
        userLatitude = sessionManagement.get("GpsLat");
        userLongitude = sessionManagement.get("GpsLog");
        //  FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/ERASMD.TTF");
        noUsersText.setTypeface(typeface);
        pd = new ProgressDialog(this);
        pd.setMessage("Loading...");
        pd.show();
        pd.setCancelable(true);
        pd.setCanceledOnTouchOutside(false);

        registerForContextMenu(list);


//        remoteConfig=FirebaseRemoteConfig.getInstance();
//        remoteConfig.setConfigSettings(new FirebaseRemoteConfigSettings.Builder().setDeveloperModeEnabled(true).build());
//        HashMap<String,Object> defaults=new HashMap<>();
//        defaults.put("max_distance",100);
//        remoteConfig.setDefaults(defaults);
//        final Task<Void> fetch=remoteConfig.fetch(0);
        if (sessionManagement.get("Page").equalsIgnoreCase("City") || sessionManagement.get("Page").equalsIgnoreCase("State")) {
            toolbar.setTitle(sessionManagement.get(sessionManagement.get("Page")) + " User List ");
        } else {
            toolbar.setTitle(sessionManagement.get("Page") + " User List ");
        }

        // toolbar.setBackground(new ColorDrawable(Color.parseColor("#0000ff")));
        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(toolbar);
        // Display icon in the toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

//        MobileAds.initialize(this, "ca-app-pub-5527620381143347~8634487311");
//        adView = (AdView) findViewById(R.id.adView);
////        adView.setAdSize(AdSize.SMART_BANNER);
////        adView.setAdUnitId("ca-app-pub-5527620381143347/8001407719");
//
////        AdRequest adRequest = new AdRequest.Builder().addTestDevice(id).build();
//        AdRequest adRequest = new AdRequest.Builder().build();
//
//        adView.loadAd(adRequest);

        final String mainUser = sessionManagement.getUserDetails().get("User");

        nUsers.orderByChild("Status").limitToLast(200).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                try {
                    HashMap<String, String> mapList = new HashMap<String, String>();

                    String Username = "", status = "", userType = "", chatId = "",
                            Email = "", gpsLat = "", gpsLog = "", city = "", state = "", image = "", Desc = "",userSex="";
                    chatId = dataSnapshot.getKey();

                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                        if (postSnapshot.getKey().equals("Username")) {
                            Username = postSnapshot.getValue().toString();
                            continue;
                        } else if (postSnapshot.getKey().equals("Status")) {
                            status = postSnapshot.getValue().toString();
                            continue;
                        } else if (postSnapshot.getKey().equals("GpsLat")) {
                            gpsLat = postSnapshot.getValue().toString();
                            continue;
                        } else if (postSnapshot.getKey().equals("GpsLog")) {
                            gpsLog = postSnapshot.getValue().toString();
                            continue;
                        } else if (postSnapshot.getKey().equals("City")) {
                            city = postSnapshot.getValue().toString();
                            continue;
                        } else if (postSnapshot.getKey().equals("State")) {
                            state = postSnapshot.getValue().toString();
                            continue;
                        } else if (postSnapshot.getKey().equals("Type")) {
                            userType = postSnapshot.getValue().toString();
                            continue;
                        }  else if (postSnapshot.getKey().equals("Sex")) {
                            userSex = postSnapshot.getValue().toString();
                            continue;
                        }else if (postSnapshot.getKey().equals("Image")) {
                            image = postSnapshot.getValue().toString();
                            continue;
                        } else if (postSnapshot.getKey().equals("Email")) {
                            Email = postSnapshot.getValue().toString();
                            continue;
                        } else if (postSnapshot.getKey().equals("Description")) {
                            Desc = postSnapshot.getValue().toString();
                            if (Desc.length() > 20) {
                                Desc = Desc.substring(0, 20) + "...";
                            }
                            continue;
                        } else if (postSnapshot.getKey().equals("Business")) {
                            Desc = postSnapshot.getValue().toString();
                            if (Desc.length() > 20) {
                                Desc = Desc.substring(0, 20) + "...";
                            }
                            continue;
                        }
                    }

                    try {
                        Location startPoint = new Location("locationA");
                        startPoint.setLatitude(Double.parseDouble(userLatitude));
                        startPoint.setLongitude(Double.parseDouble(userLongitude));

                        Location endPoint = new Location("locationA");
                        endPoint.setLatitude(Double.parseDouble(gpsLat));
                        endPoint.setLongitude(Double.parseDouble(gpsLog));

                        double distance = startPoint.distanceTo(endPoint);//distance is in meters
                        if (distance <= max_distance && sessionManagement.get("Page").equals("Local")) {
                            if (!Username.isEmpty() && !chatId.equals(mainUser)) {
                                mapList.put("Username", Username);
                                mapList.put("read", status);
                                mapList.put("chatId", chatId);
                                mapList.put("Email", Email);
                                mapList.put("message", Desc);
                                mapList.put("Image", image);
                                mapList.put("Sex", userSex);
                                if (!dataList.contains(mapList)) {
                                    dataList.add(mapList);
                                    dataList.set(totalUsers,mapList);
                                    totalUsers = totalUsers + 1;

                                }

                            }
                        }
                    } catch (Exception nor) {
                        Crashlytics.logException(nor);
                    }
                    if (city.equals(sessionManagement.get("City")) && sessionManagement.get("Page").equals("City")) {
                        if (!Username.isEmpty() && !chatId.equals(mainUser)) {
                            mapList.put("Username", Username);
                            mapList.put("read", status);
                            mapList.put("chatId", chatId);
                            mapList.put("Image", image);
                            mapList.put("message", Desc);
                            mapList.put("Email", Email);
                            mapList.put("Sex", userSex);
                            if (!dataList.contains(mapList)) {
                                dataList.add(mapList);
                                dataList.set(totalUsers,mapList);
                                totalUsers = totalUsers + 1;

                            }
                        }
                    } else if (state.equals(sessionManagement.get("State")) && sessionManagement.get("Page").equals("State")) {
                        if (!Username.isEmpty() && !chatId.equals(mainUser)) {
                            mapList.put("Username", Username);
                            mapList.put("read", status);
                            mapList.put("chatId", chatId);
                            mapList.put("Image", image);
                            mapList.put("message", Desc);
                            mapList.put("Email", Email);
                            mapList.put("Sex", userSex);
                            if (!dataList.contains(mapList)) {
                                dataList.add(mapList);
                                dataList.set(totalUsers,mapList);
                                totalUsers = totalUsers + 1;

                            }
                        }
                    } else if (userType.equals("Artisan") && sessionManagement.get("Page").equals("Business")) {
                        if (!Username.isEmpty() && !chatId.equals(mainUser) && userType.equalsIgnoreCase("Artisan")) {
                            mapList.put("Username", Username);
                            mapList.put("read", status);
                            mapList.put("chatId", chatId);
                            mapList.put("message", Desc);
                            mapList.put("Email", Email);
                            mapList.put("Image", image);
                            mapList.put("Sex", userSex);
                            if (!dataList.contains(mapList)) {
                                dataList.add(mapList);
                                dataList.set(totalUsers,mapList);
                                totalUsers = totalUsers + 1;

                            }
                        }
                    }

                    if (totalUsers < 1) {
                        noUsersText.setVisibility(View.VISIBLE);
                        list.setVisibility(View.GONE);
                    } else {
                        noUsersText.setVisibility(View.GONE);
                        list.setVisibility(View.VISIBLE);
                        adapter = new TabListAdapter(UsersList.this, dataList);
                        list.setAdapter(adapter);
                    }
                    pd.dismiss();
                } catch (Exception er) {
                    Crashlytics.logException(er);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                try {
                    HashMap<String, String> mapList = new HashMap<String, String>();
                    String Username = "", status = "", chatId = "", gpsLat = "", gpsLog = "";
                    chatId = dataSnapshot.getKey();
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                        if (postSnapshot.getKey().equals("Username")) {
                            Username = postSnapshot.getValue().toString();
                            continue;
                        } else if (postSnapshot.getKey().equals("Status")) {
                            status = postSnapshot.getValue().toString();
                            continue;
                        } else if (postSnapshot.getKey().equals("GpsLat")) {
                            gpsLat = postSnapshot.getValue().toString();
                            continue;
                        } else if (postSnapshot.getKey().equals("GpsLog")) {
                            gpsLog = postSnapshot.getValue().toString();
                            continue;
                        }
                    }
                    try {
                        Location startPoint = new Location("locationA");
                        startPoint.setLatitude(Double.parseDouble(userLatitude));
                        startPoint.setLongitude(Double.parseDouble(userLongitude));

                        Location endPoint = new Location("locationA");
                        endPoint.setLatitude(Double.parseDouble(gpsLat));
                        endPoint.setLongitude(Double.parseDouble(gpsLog));

                        double distance = startPoint.distanceTo(endPoint);//distance is in meters

                        if (distance < max_distance) {
                            if (status.equals("offline") && !Username.isEmpty() && !chatId.equals(mainUser)) {
                                mapList.put("Username", Username);
                                mapList.put("read", "online");
                                mapList.put("chatId", chatId);
                                dataList.remove(mapList);
                                mapList.put("Username", Username);
                                mapList.put("Status", "offline");
                                mapList.put("chatId", chatId);
                                if (!dataList.contains(mapList)) {
                                    dataList.add(mapList);
                                }
                            } else if (status.equals("online") && !Username.isEmpty() && !chatId.equals(mainUser)) {
                                mapList.put("Username", Username);
                                mapList.put("read", "offline");
                                mapList.put("chatId", chatId);
                                dataList.remove(mapList);
                                mapList.put("Username", Username);
                                mapList.put("read", "online");
                                mapList.put("chatId", chatId);
                                if (!dataList.contains(mapList)) {
                                    dataList.add(mapList);
                                }
                            }
                        }
                    } catch (Exception nor) {
                        Crashlytics.logException(nor);
                    }
                    if (totalUsers < 1) {
                        noUsersText.setVisibility(View.VISIBLE);
                        list.setVisibility(View.GONE);
                    } else {
                        noUsersText.setVisibility(View.GONE);
                        list.setVisibility(View.VISIBLE);
                        adapter = new TabListAdapter(UsersList.this, dataList);
                        list.setAdapter(adapter);
                    }
                    pd.dismiss();
                } catch (Exception er) {
                    Crashlytics.logException(er);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                try {
                    HashMap<String, String> mapList = new HashMap<String, String>();
                    String Username = "", status = "", chatId = "";
                    chatId = dataSnapshot.getKey();
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                        if (postSnapshot.getKey().equals("Username")) {
                            Username = postSnapshot.getValue().toString();
                            continue;
                        }
                    }
                    if (!Username.isEmpty() && !chatId.equals(mainUser)) {
                        mapList.put("Username", Username);
                        mapList.put("chatId", chatId);
                        dataList.remove(mapList);
                        totalUsers = totalUsers - 1;
                    }
                } catch (Exception er) {
                    Crashlytics.logException(er);
                }

                if (totalUsers < 1) {
                    noUsersText.setVisibility(View.VISIBLE);
                    list.setVisibility(View.GONE);
                } else {
                    noUsersText.setVisibility(View.GONE);
                    list.setVisibility(View.VISIBLE);
                    adapter = new TabListAdapter(UsersList.this, dataList);
                    list.setAdapter(adapter);

                }
                pd.dismiss();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }


        });

        // Click event for single list row
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                TextView chatId = (TextView) view.findViewById(R.id.chat_id);
                TextView user_name = (TextView) view.findViewById(R.id.user_name);
                TextView read = (TextView) view.findViewById(R.id.read_mess);
                TextView mess = (TextView) view.findViewById(R.id.last_mess); // read
                TextView img = (TextView) view.findViewById(R.id.img_txt);
                TextView email = (TextView) view.findViewById(R.id.email_txt);
                TextView sex = (TextView) view.findViewById(R.id.sex);
                sessionManagement.set("chatWithSex", sex.getText().toString());
                sessionManagement.set("chatWithImage", img.getText().toString());
                sessionManagement.set("chatWithEmail", email.getText().toString());

                mess.setVisibility(View.GONE);
                read.setVisibility(View.VISIBLE);
                String chatWit = "";
                chatWit = chatId.getText().toString();
                sessionManagement.set("usernameId", sessionManagement.getUserDetails().get("User"));
                sessionManagement.set("username", sessionManagement.get("MyUsername"));
                sessionManagement.set("chatWithId", chatWit);

                sessionManagement.set("chatWith", user_name.getText().toString());

                finish();
                startActivity(new Intent(UsersList.this, FragmentChat.class));
            }
        });

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }


    /**
     * MENU
     */

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.list) {
            MenuItem mnu1 = menu.add(0, 0, 0, "View Profile");
//            MenuItem mnu2 = menu.add(0, 1, 1, "Block/Unblock");
//            MenuInflater inflater = getMenuInflater();
//            inflater.inflate(R.menu.menu_list, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        long selectid = info.id; //_id from database in this case
        int selectpos = info.position; //position in the adapter
        HashMap<String, String> value;
        String chatId,username,usernameUc;
        switch (item.getItemId()) {
            case 0:
                 value= dataList.get(selectpos);
              chatId=   value.get("chatId");
                 username=  value.get("Username");
                 usernameUc = username.substring(0, 1).toUpperCase() + username.substring(1).toLowerCase();
//                Toast.makeText(UsersList.this,"User: "+usernameUc, Toast.LENGTH_LONG).show();
                Intent intent=new Intent(UsersList.this, UsersInfo.class);
                intent.putExtra("UserInfoId",chatId);
                intent.putExtra("UserInfoName",usernameUc);
                intent.putExtra("UserInfoUsername",username);
                startActivity(intent);

                return true;
            case 1:
                Toast.makeText(UsersList.this, "2Just now", Toast.LENGTH_LONG).show();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
}
