package lobschat.hycode.lobschat.firebase_notification;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import lobschat.hycode.lobschat.ArtisanFragmentGroupChat;
import lobschat.hycode.lobschat.ArtisanList;
import lobschat.hycode.lobschat.FragmentChat;
import lobschat.hycode.lobschat.Methods;
import lobschat.hycode.lobschat.R;
import lobschat.hycode.lobschat.SessionManagement;
import lobschat.hycode.lobschat.Settings;
import lobschat.hycode.lobschat.TabListAdapter;
import lobschat.hycode.lobschat.UpdateLocation;

/**
 * Created by Icode on 6/1/2018.
 */


public class old_artisan_tab_fragment extends Fragment {
    ListView usersList;
    double max_distance = 100;
    ArrayList<String> al = new ArrayList<>();
    int totalUsers, totalUsersGroup;
    ProgressDialog pd;
    ListView list;
    TabListAdapter adapter;
    //ArrayList<HashMap<String, String>> al;
    ArrayList<HashMap<String, String>> dataList, dataGroupList;
    private static final String TAG = lobschat.hycode.lobschat.ArtisanTabFragment.class.getSimpleName();

    static String prevMess;
    View rootView;
    DatabaseReference nRootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference dblobschat = nRootRef.child("lobschat");
    DatabaseReference nUsers = dblobschat.child("users");
    DatabaseReference nChat = dblobschat.child("user-chat");
    DatabaseReference ngroupChat = dblobschat.child("artisan-chat");

    SessionManagement sessionManagement;
    String address, city, state, knownName, country, postalCode;
    protected Context context;

    TextView gplast_mess_date, gpnum_users, gplast_mess, gpname, num_users;
    String userLatitude, userLongitude;
    RelativeLayout groupLayout;
    int count2;
    boolean gpsAlert = false;
    Location locLastKnown;
    protected LocationManager locationManager;

    String towers;
    boolean isNetworkEnabled;
    DatabaseReference lastMess, oldMess;
    String grpUsername = "", grpstatus = "", grpmessage = "", grpchatId = "", grpdate = "", grpotherUser = "", grpgpsLat = "", grpgpsLog = "";
    String image="";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        rootView = inflater.inflate(R.layout.fragment_tab, container, false);
        sessionManagement = new SessionManagement(getActivity());
        list = (ListView) rootView.findViewById(R.id.list);
//
//        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
//        isNetworkEnabled = locationManager.isProviderEnabled(NETWORK_PROVIDER);
//        Criteria crit = new Criteria();
//        towers = locationManager.getBestProvider(crit, false);
//        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED
//                && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED
//                && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.CHANGE_NETWORK_STATE)
//                != PackageManager.PERMISSION_GRANTED) {
//
//        }
//        locLastKnown = locationManager.getLastKnownLocation(towers);
        try {
            if (sessionManagement.get("Artisan")==null || sessionManagement.get("Artisan").isEmpty()) {
                sessionManagement.set("Artisan", "Local");
            }
            if (sessionManagement.get("ArtisanPage")==null || sessionManagement.get("ArtisanPage").isEmpty()) {
                sessionManagement.set("ArtisanPage", "Local");
            }
        }catch(Exception er){
            sessionManagement.set("Artisan", "Local");
            sessionManagement.set("ArtisanPage", "Local");
        }
        userLatitude=sessionManagement.get("GpsLat");
        userLongitude=sessionManagement.get("GpsLog");
//        last_mess=(TextView)rootView.findViewById(R.id.last_mess);
//        last_mess_date=(TextView)rootView.findViewById(R.id.last_mess_date);
        num_users = (TextView) rootView.findViewById(R.id.num_users);

//        usersList = (ListView)findViewById(R.id.usersList);
        dataList = new ArrayList<HashMap<String, String>>();
        dataGroupList = new ArrayList<HashMap<String, String>>();
        totalUsers = 0;
        count2=0;
        gpname = (TextView) rootView.findViewById(R.id.gp_name);
        gplast_mess = (TextView) rootView.findViewById(R.id.gplast_mess);
        gplast_mess_date = (TextView) rootView.findViewById(R.id.gp_date);
        gpnum_users = (TextView) rootView.findViewById(R.id.gpnum_users);

        groupLayout = (RelativeLayout) rootView.findViewById(R.id.groupLayout);
        try {
            if (sessionManagement.get("Artisan").equals("City")) {
                gpname.setText(sessionManagement.get("City") + " Artisans");
            } else if (sessionManagement.get("Artisan").equals("State")) {
                gpname.setText(sessionManagement.get("State") + " Artisans");
            } else {
                gpname.setText("Local Artisans");
                sessionManagement.set("Artisan","Local");
                sessionManagement.set("ArtisanPage","Local");
            }
        }catch (Exception er){gpname.setText("Local Artisans");}


        try{
            nUsers.child(sessionManagement.getUserDetails().get("User")).child("Image").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        image = dataSnapshot.getValue().toString();
                        sessionManagement.set("Image", image);
                    }catch (Exception er){
                        Toast.makeText(getActivity(),er.getMessage(),Toast.LENGTH_LONG).show();
                        nUsers.child(sessionManagement.getUserDetails().get("User")).child("Image").setValue("");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });}catch (Exception er){}


        groupLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), ArtisanFragmentGroupChat.class));
            }
        });

        pd = new ProgressDialog(getActivity());
        pd.setMessage("Loading...");
//        pd.show();
        FloatingActionButton fabUser = (FloatingActionButton)rootView.findViewById(R.id.fabUser);


        fabUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();

                try {
                    if (sessionManagement.get("Artisan").equals("City")) {
                        gpname.setText(sessionManagement.get("City") + " Artisans");
                    } else if (sessionManagement.get("Artisan").equals("State")) {
                        gpname.setText(sessionManagement.get("State") + " Artisans");
                    } else {
                        gpname.setText("Local Artisans");
                    }
                }catch (Exception er){gpname.setText("Local Artisans");}

                startActivity(new Intent(getActivity(), ArtisanList.class));
            }
        });

//        lastMess = new Firebase("https://lobschat.firebaseio.com/lobschat/user-chat/artisan/" + sessionManagement.get("usernameId")+"/lastMess" );
        lastMess=nChat.child("artisan").child(sessionManagement.get("usernameId")).child("lastMess");
        final String mainUser = sessionManagement.getUserDetails().get("User");
        try {
            if (sessionManagement.get("Artisan").equals("City")) {
                ngroupChat.child("City").child(sessionManagement.get("City")).addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        try {
                            HashMap<String, String> mapList = new HashMap<String, String>();
                            String Username = "", status = "", message = "", chatId = "", date = "", userType = "", gpsLat = "", gpsLog = "";
//                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

//
                            for (DataSnapshot postSnap : dataSnapshot.getChildren()) {

                                if (postSnap.getKey().equals("msgUser")) {
                                    grpUsername = postSnap.getValue().toString();
                                    continue;
                                } else if (postSnap.getKey().equals("msgText")) {
                                    grpmessage = postSnap.getValue().toString();
                                    continue;
                                } else if (postSnap.getKey().equals("msgDate")) {
                                    grpdate = postSnap.getValue().toString();
                                    continue;
                                } else if (postSnap.getKey().equals("msgLat")) {
                                    grpgpsLat = postSnap.getValue().toString();
                                    continue;
                                } else if (postSnap.getKey().equals("msgLng")) {
                                    grpgpsLog = postSnap.getValue().toString();
                                    continue;
                                }
                            }
                            try {
                                Location startPoint = new Location("locationA");

                                startPoint.setLatitude(Double.parseDouble(userLatitude));

                                startPoint.setLongitude(Double.parseDouble(userLongitude));

                                Location endPoint = new Location("locationA");

                                endPoint.setLatitude(Double.parseDouble(grpgpsLat));

                                endPoint.setLongitude(Double.parseDouble(grpgpsLog));

                                double distance = startPoint.distanceTo(endPoint);//distance is in meters

                                if (distance < max_distance) {
                                    if (!grpUsername.isEmpty()
                                            && !grpUsername.equals(sessionManagement.get("MyUsername"))) {
                                        mapList.put("username", grpUsername);
                                        if (!dataGroupList.contains(mapList)) {
                                            dataGroupList.add(mapList);
                                            count2 = count2 + 1;
                                        }
                                    }

                                    if (grpUsername.equals(sessionManagement.get("MyUsername"))) {
                                        grpUsername = "You";
                                    }

                                    String urs = "User";
                                    if (count2 > 1) urs = "Users";
                                    gpnum_users.setText(count2 + " " + urs);
                                    grpUsername = grpUsername.split("--__")[0];

                                    if (!grpmessage.isEmpty()) {
                                        String lstMess = grpmessage;
                                        if (grpmessage.length() > 20) {
                                            lstMess = grpmessage.substring(0, 20) + "...";
                                        }
                                        gplast_mess.setText(grpUsername + ":- " + lstMess);
                                    } else {
                                        gplast_mess.setText("No message ");
                                    }

                                    gplast_mess_date.setText(grpdate);
                                }
                            } catch (Exception nor) {

                            }
//                            }
                            pd.dismiss();
                        } catch (Exception er) {
                            Toast.makeText(getActivity(), "Local error2" + er.getMessage(), Toast.LENGTH_LONG).show();
                        }


                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                if (grpmessage.isEmpty()) {
                    gplast_mess.setText("No Message");
                }
            } else if (sessionManagement.get("Artisan").equals("State")) {
                ngroupChat.child("State").child(sessionManagement.get("State")).addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        try {
                            HashMap<String, String> mapList = new HashMap<String, String>();
                            String Username = "", status = "", message = "", chatId = "", date = "", userType = "", gpsLat = "", gpsLog = "";
//                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

//
                            for (DataSnapshot postSnap : dataSnapshot.getChildren()) {

                                if (postSnap.getKey().equals("msgUser")) {
                                    grpUsername = postSnap.getValue().toString();
                                    continue;
                                } else if (postSnap.getKey().equals("msgText")) {
                                    grpmessage = postSnap.getValue().toString();
                                    continue;
                                } else if (postSnap.getKey().equals("msgDate")) {
                                    grpdate = postSnap.getValue().toString();
                                    continue;
                                } else if (postSnap.getKey().equals("msgLat")) {
                                    grpgpsLat = postSnap.getValue().toString();
                                    continue;
                                } else if (postSnap.getKey().equals("msgLng")) {
                                    grpgpsLog = postSnap.getValue().toString();
                                    continue;
                                }
                            }
                            try {
                                Location startPoint = new Location("locationA");

                                startPoint.setLatitude(Double.parseDouble(userLatitude));

                                startPoint.setLongitude(Double.parseDouble(userLongitude));

                                Location endPoint = new Location("locationA");

                                endPoint.setLatitude(Double.parseDouble(grpgpsLat));

                                endPoint.setLongitude(Double.parseDouble(grpgpsLog));

                                double distance = startPoint.distanceTo(endPoint);//distance is in meters

                                if (distance < max_distance) {
                                    if (!grpUsername.isEmpty()
                                            && !grpUsername.equals(sessionManagement.get("MyUsername"))) {
                                        mapList.put("username", grpUsername);
                                        if (!dataGroupList.contains(mapList)) {
                                            dataGroupList.add(mapList);
                                            count2 = count2 + 1;
                                        }
                                    }

                                    if (grpUsername.equals(sessionManagement.get("MyUsername"))) {
                                        grpUsername = "You";
                                    }

                                    String urs = "User";
                                    if (count2 > 1) urs = "Users";
                                    gpnum_users.setText(count2 + " " + urs);
                                    grpUsername = grpUsername.split("--__")[0];

                                    if (!grpmessage.isEmpty()) {
                                        String lstMess = grpmessage;
                                        if (grpmessage.length() > 20) {
                                            lstMess = grpmessage.substring(0, 20) + "...";
                                        }
                                        gplast_mess.setText(grpUsername + ":- " + lstMess);
                                    } else {
                                        gplast_mess.setText("No message ");
                                    }

                                    gplast_mess_date.setText(grpdate);
                                }
                            } catch (Exception nor) {

                            }
//                            }
                            pd.dismiss();
                        } catch (Exception er) {
                            Toast.makeText(getActivity(), "Local error2" + er.getMessage(), Toast.LENGTH_LONG).show();
                        }


                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                if (grpmessage.isEmpty()) {
                    gplast_mess.setText("No Message");
                }
            }
            else{
                ngroupChat.child("Local").addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        try {
                            HashMap<String, String> mapList = new HashMap<String, String>();
                            String Username = "", status = "", message = "", chatId = "", date = "", userType = "", gpsLat = "", gpsLog = "";
//                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

//
                            for (DataSnapshot postSnap : dataSnapshot.getChildren()) {

                                if (postSnap.getKey().equals("msgUser")) {
                                    grpUsername = postSnap.getValue().toString();
                                    continue;
                                } else if (postSnap.getKey().equals("msgText")) {
                                    grpmessage = postSnap.getValue().toString();
                                    continue;
                                } else if (postSnap.getKey().equals("msgDate")) {
                                    grpdate = postSnap.getValue().toString();
                                    continue;
                                } else if (postSnap.getKey().equals("msgLat")) {
                                    grpgpsLat = postSnap.getValue().toString();
                                    continue;
                                } else if (postSnap.getKey().equals("msgLng")) {
                                    grpgpsLog = postSnap.getValue().toString();
                                    continue;
                                }
                            }
                            try {
                                Location startPoint = new Location("locationA");

                                startPoint.setLatitude(Double.parseDouble(userLatitude));

                                startPoint.setLongitude(Double.parseDouble(userLongitude));

                                Location endPoint = new Location("locationA");

                                endPoint.setLatitude(Double.parseDouble(grpgpsLat));

                                endPoint.setLongitude(Double.parseDouble(grpgpsLog));

                                double distance = startPoint.distanceTo(endPoint);//distance is in meters

                                if (distance < max_distance) {
                                    if (!grpUsername.isEmpty()
                                            && !grpUsername.equals(sessionManagement.get("MyUsername"))) {
                                        mapList.put("username", grpUsername);
                                        if (!dataGroupList.contains(mapList)) {
                                            dataGroupList.add(mapList);
                                            count2 = count2 + 1;
                                        }
                                    }

                                    if (grpUsername.equals(sessionManagement.get("MyUsername"))) {
                                        grpUsername = "You";
                                    }

                                    String urs = "User";
                                    if (count2 > 1) urs = "Users";
                                    gpnum_users.setText(count2 + " " + urs);
                                    grpUsername = grpUsername.split("--__")[0];

                                    if (!grpmessage.isEmpty()) {
                                        String lstMess = grpmessage;
                                        if (grpmessage.length() > 20) {
                                            lstMess = grpmessage.substring(0, 20) + "...";
                                        }
                                        gplast_mess.setText(grpUsername + ":- " + lstMess);
                                    } else {
                                        gplast_mess.setText("No message ");
                                    }

                                    gplast_mess_date.setText(grpdate);
                                }
                            } catch (Exception nor) {

                            }
//                            }
                            pd.dismiss();
                        } catch (Exception er) {
                            Toast.makeText(getActivity(), "Local error2" + er.getMessage(), Toast.LENGTH_LONG).show();
                        }


                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                if (grpmessage.isEmpty()) {
                    gplast_mess.setText("No Message");
                }
            }

            lastMess.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    HashMap<String, String> mapList = new HashMap<String, String>();
//                    Map map = dataSnapshot.getValue(Map.class);
                    String userName = dataSnapshot.getKey();
                    String chatIdmessage = dataSnapshot.getValue().toString();
                    String page=chatIdmessage.split("::")[2];

                    final String chatId=chatIdmessage.split("::")[0];
                    String message=chatIdmessage.split("::")[1];
                    try{
                        nUsers.child(chatId).child("Image").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                try{
                                    image=dataSnapshot.getValue().toString();

                                }catch (Exception er){

                                    nUsers.child(chatId).child("Image").setValue("");
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });}catch (Exception er){   nUsers.child(chatId).child("Image").setValue("");}


                    if (!userName.isEmpty() && page.equalsIgnoreCase("Local") && sessionManagement.get("ArtisanPage").equalsIgnoreCase("Local")) {
                        String lat=chatIdmessage.split("::")[3];
                        String log=chatIdmessage.split("::")[4];
                        Location startPoint = new Location("locationA");
                        startPoint.setLatitude(Double.parseDouble(userLatitude));
                        startPoint.setLongitude(Double.parseDouble(userLongitude));

                        Location endPoint = new Location("locationA");
                        endPoint.setLatitude(Double.parseDouble(lat));
                        endPoint.setLongitude(Double.parseDouble(log));

                        double distance = startPoint.distanceTo(endPoint);//distance is in meters
//                        oldMess.child(userName).setValue(chatIdmessage.split("::")[1]);
                        if(distance<=max_distance) {
//                        oldMess.child(userName).setValue(chatIdmessage.split("::")[1]);
                            sessionManagement.set("old" + userName, message);
                            mapList.put("Username", userName);
                            mapList.put("Image", image);
                            mapList.put("chatId", chatIdmessage.split("::")[0]);
                            if (!dataList.contains(mapList)) {
                                mapList.put("message", message);
                                mapList.put("otherUser", "");
                                mapList.put("date", "");
                                mapList.put("read", "");
                                dataList.add(mapList);
                                totalUsers = totalUsers + 1;
                            }
                        }
                    }
                    else if (!userName.isEmpty() && page.equalsIgnoreCase("City") && sessionManagement.get("ArtisanPage").equalsIgnoreCase("City")) {
                        String city=chatIdmessage.split("::")[3];
//                        oldMess.child(userName).setValue(chatIdmessage.split("::")[1]);
                        if(city.equalsIgnoreCase(sessionManagement.get("City").replace(" ",""))) {
//                        oldMess.child(userName).setValue(chatIdmessage.split("::")[1]);
                            sessionManagement.set("old" + userName, message);
                            mapList.put("Username", userName);
                            mapList.put("Image", image);
                            mapList.put("chatId", chatIdmessage.split("::")[0]);
                            if (!dataList.contains(mapList)) {
                                mapList.put("message", message);
                                mapList.put("otherUser", "");
                                mapList.put("date", "");
                                mapList.put("read", "");
                                dataList.add(mapList);
                                totalUsers = totalUsers + 1;
                            }
                        }
                    }
                    else if (!userName.isEmpty() && page.equalsIgnoreCase("State") && sessionManagement.get("ArtisanPage").equalsIgnoreCase("State")) {
                        String state=chatIdmessage.split("::")[3];
//                        oldMess.child(userName).setValue(chatIdmessage.split("::")[1]);
                        if(state.equalsIgnoreCase(sessionManagement.get("State").replace(" ",""))) {
//                        oldMess.child(userName).setValue(chatIdmessage.split("::")[1]);
                            sessionManagement.set("old" + userName, message);
                            mapList.put("Username", userName);
                            mapList.put("Image", image);
                            mapList.put("chatId", chatIdmessage.split("::")[0]);
                            if (!dataList.contains(mapList)) {
                                mapList.put("message", message);
                                mapList.put("otherUser", "");
                                mapList.put("date", "");
                                mapList.put("read", "");
                                dataList.add(mapList);
                                totalUsers = totalUsers + 1;
                            }
                        }
                    }
                    if (totalUsers < 1) {
                        list.setVisibility(View.GONE);
                    } else {
                        list.setVisibility(View.VISIBLE);
                        adapter = new TabListAdapter(getActivity(), dataList);
                        list.setAdapter(adapter);
                    }

                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    HashMap<String, String> mapList = new HashMap<String, String>();
                    HashMap<String, String> mapNew = new HashMap<String, String>();
//                    Map map = dataSnapshot.getValue(Map.class);
                    String userName = dataSnapshot.getKey();
                    String chatIdmessage = dataSnapshot.getValue().toString();
                    String page=chatIdmessage.split("::")[2];
                    String message=chatIdmessage.split("::")[1];
                    final String chatId=chatIdmessage.split("::")[0];
                    try{
                        nUsers.child(chatId).child("Image").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                try{
                                    image=dataSnapshot.getValue().toString();

                                }catch (Exception er){

                                    nUsers.child(chatId).child("Image").setValue("");
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });}catch (Exception er){   nUsers.child(chatId).child("Image").setValue("");}

                    String oldmessage=sessionManagement.get("old"+userName);

                    if (!userName.isEmpty() && page.equalsIgnoreCase("Local")) {

                        mapNew.put("Username", userName);
                        mapNew.put("Image", image);
                        mapNew.put("chatId", chatIdmessage.split("::")[0]);
                        mapNew.put("message", oldmessage);
//                        mapList.put("Page", chatIdmessage.split("::")[2]);
                        mapNew.put("otherUser","");
                        mapNew.put("date","");
                        mapNew.put("read","");
                        if(dataList.contains(mapNew)){
                            dataList.remove((mapNew));
                            mapNew.put("message", message);
                            dataList.add(mapNew);

                        }
                        sessionManagement.set("old"+userName,message);
                    }
                    if (totalUsers < 1) {
                        list.setVisibility(View.GONE);
                    } else {
                        list.setVisibility(View.VISIBLE);
                        adapter = new TabListAdapter(getActivity(), dataList);
                        list.setAdapter(adapter);
                    }
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    HashMap<String, String> mapList = new HashMap<String, String>();
//                    Map map = dataSnapshot.getValue(Map.class);
                    String userName = dataSnapshot.getKey();
                    String chatIdmessage = dataSnapshot.getValue().toString();
                    String oldmessage=sessionManagement.get("old"+userName);
                    if (!userName.isEmpty()) {

                        mapList.put("Username", userName);
                        mapList.put("chatId", chatIdmessage.split(":")[0]);
                        mapList.put("message", chatIdmessage.split(":")[1]);
                        mapList.put("otherUser","");
                        mapList.put("date","");
                        mapList.put("read","");
                        if (dataList.contains(mapList)) {
                            dataList.remove(mapList);
                            totalUsers = totalUsers - 1;
                        }

                    }
                    if (totalUsers < 1) {
                        list.setVisibility(View.GONE);
                    } else {
                        list.setVisibility(View.VISIBLE);
                        adapter = new TabListAdapter(getActivity(), dataList);
                        list.setAdapter(adapter);
                    }
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError firebaseError) {

                }
            });

        } catch (Exception e) {

        }

        // Click event for single list row
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                TextView chatId = (TextView) view.findViewById(R.id.chat_id);
                TextView user_name = (TextView)view.findViewById(R.id.user_name);
                TextView read_mess = (TextView)view.findViewById(R.id.read_mess); // read
//                read_mess.setText("read");
                String chatWit="";

                chatWit=chatId.getText().toString();
                sessionManagement.set("usernameId",sessionManagement.getUserDetails().get("User"));
                sessionManagement.set("username",sessionManagement.get("MyUsername"));
                sessionManagement.set("chatWithId",chatWit);
                sessionManagement.set("chatWith",user_name.getText().toString().toLowerCase());
                sessionManagement.set("Page","Artisan");
//
                startActivity(new Intent(getActivity(), FragmentChat.class));
            }
        });



        return rootView;

    }



    // Menu icons are inflated just as they were with actionbar
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.artisan_tabfragment_menu, menu);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        switch (id){
            case R.id.action_settings:
                startActivity(new Intent(getActivity(),Settings.class));
                return true;
            case R.id.action_local:
                sessionManagement.set("Artisan","Local");
                sessionManagement.set("ArtisanPage","Local");
                gpname.setText("Local Artisans");
                return true;
            case R.id.action_city:
                sessionManagement.set("Artisan","City");
                sessionManagement.set("ArtisanPage","City");
                gpname.setText(sessionManagement.get("State") + " Artisans");
                return true;
            case R.id.action_state:
                sessionManagement.set("Artisan","State");
                sessionManagement.set("ArtisanPage","State");
                gpname.setText(sessionManagement.get("State") + " Artisans");
                return true;
            case R.id.action_exit:
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                alertDialogBuilder.setTitle("Exit LobsCab?");
                alertDialogBuilder
                        .setMessage("Click yes to exit!")
                        .setCancelable(false)
                        .setPositiveButton("Yes",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
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
            case R.id.action_refresh:
                if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

                }if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Methods met=new Methods(getActivity());
                met.showGPSDisabledAlertToUser();
            }else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                UpdateLocation updateLocation=new UpdateLocation(getActivity(),"Local");
                updateLocation.refresh("Local");
            }

                return true;
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onResume() {
        super.onResume();
        String mainkey=sessionManagement.getUserDetails().get("User");
        DatabaseReference checkUser = nUsers.child(mainkey);
        DatabaseReference dbStatus = checkUser.child("Status");
        dbStatus.setValue("online");


    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onStop() {
        super.onStop();


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        String mainkey=sessionManagement.getUserDetails().get("User");
        DatabaseReference checkUser = nUsers.child(mainkey);
        DatabaseReference dbStatus = checkUser.child("Status");
        dbStatus.setValue("offline");
    }




}
