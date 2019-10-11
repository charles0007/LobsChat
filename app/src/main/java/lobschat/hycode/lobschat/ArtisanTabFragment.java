package lobschat.hycode.lobschat;

/**
 * Created by HyCode on 12/22/2017.
 */


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.crashlytics.android.Crashlytics;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import java.util.HashMap;

import static android.location.LocationManager.NETWORK_PROVIDER;

public class ArtisanTabFragment extends Fragment {
    ListView usersList;
    double max_distance = 10000;
    ArrayList<String> al = new ArrayList<>();
    int totalUsers, totalUsersGroup;
    ProgressDialog pd;
    ListView list;
    TabListAdapter adapter;
    //ArrayList<HashMap<String, String>> al;
    ArrayList<HashMap<String, String>> dataList, dataGroupList;
    private static final String TAG = ArtisanTabFragment.class.getSimpleName();

    static String prevMess;
    View rootView;
    DatabaseReference nRootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference dblobschat = nRootRef.child("lobschat");
    DatabaseReference nUsers = dblobschat.child("users");
    DatabaseReference nChat = dblobschat.child("artisan-chat").child("user-chat");
    DatabaseReference ngroupChat = dblobschat.child("artisan-chat");

    SessionManagement sessionManagement;
    String address, city, state, knownName, country, postalCode;
    protected Context context;

    TextView gplast_mess_date, gpnum_users, gplast_mess, gpname, num_users;
    String userLatitude, userLongitude;
    ConstraintLayout groupLayout;
    int count2;

    protected LocationManager locationManager;

    DatabaseReference lastMess, oldMess;
    String grpUsername = "", grpstatus = "", grpmessage = "", grpchatId = "", grpdate = "", grpotherUser = "", grpgpsLat = "", grpgpsLog = "";
    String image = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        rootView = inflater.inflate(R.layout.fragment_tab, container, false);

        sessionManagement = new SessionManagement(getActivity());
        sessionManagement.set("ListPage","ArtisanTabFragment");
        list = (ListView) rootView.findViewById(R.id.list);

        Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/ERASMD.TTF");
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        try {
            if (sessionManagement.get("Artisan") == null || sessionManagement.get("Artisan").isEmpty()) {
                sessionManagement.set("Artisan", "Local");
            }
            if (sessionManagement.get("ArtisanPage") == null || sessionManagement.get("ArtisanPage").isEmpty()) {
                sessionManagement.set("ArtisanPage", "Local");
            }
        } catch (Exception er) {
            sessionManagement.set("Artisan", "Local");
            sessionManagement.set("ArtisanPage", "Local");
        }
        userLatitude = sessionManagement.get("GpsLat");
        userLongitude = sessionManagement.get("GpsLog");
//
        num_users = (TextView) rootView.findViewById(R.id.num_users);

//
        dataList = new ArrayList<HashMap<String, String>>();
        dataGroupList = new ArrayList<HashMap<String, String>>();
        totalUsers = 0;
        count2 = 0;
        gpname = (TextView) rootView.findViewById(R.id.gp_name);
        gplast_mess = (TextView) rootView.findViewById(R.id.gplast_mess);
        gplast_mess_date = (TextView) rootView.findViewById(R.id.gp_date);
        gpnum_users = (TextView) rootView.findViewById(R.id.gpnum_users);
        if(gplast_mess !=null){ gplast_mess.setTypeface(typeface);}
        if(gplast_mess_date !=null){ gplast_mess_date.setTypeface(typeface);}
        if(gpname !=null){ gpname.setTypeface(typeface);}
        if(gpnum_users !=null){ gpnum_users.setTypeface(typeface);}
        if(num_users !=null){ num_users.setTypeface(typeface);}

        registerForContextMenu(list);
        groupLayout = (ConstraintLayout) rootView.findViewById(R.id.groupLayout);
        try {
            if (sessionManagement.get("Artisan").equals("City")) {
                gpname.setText(sessionManagement.get("City") + " Artisans");
            } else if (sessionManagement.get("Artisan").equals("State")) {
                gpname.setText(sessionManagement.get("State") + " Artisans");
            } else {
                gpname.setText("Local Artisans");
                sessionManagement.set("Artisan", "Local");
                sessionManagement.set("ArtisanPage", "Local");
            }
        } catch (Exception er) {
            gpname.setText("Local Artisans");
        }


//        try {
//            nUsers.child(sessionManagement.getUserDetails().get("User")).child("Image").addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    try {
//                        image = dataSnapshot.getValue().toString();
//                        sessionManagement.set("Image", image);
//                    } catch (Exception er) {
//                        Toast.makeText(getActivity(), er.getMessage(), Toast.LENGTH_LONG).show();
//                        nUsers.child(sessionManagement.getUserDetails().get("User")).child("Image").setValue("");
//                    }
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//
//                }
//            });
//        } catch (Exception er) {
//        }
try{     nUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String childId = dataSnapshot.getKey();
                int index=0;
                HashMap<String, String> mapNew = new HashMap<String, String>();

                for (HashMap<String, String> dat : dataList) {
                    index=index+1;
                    if (dat.containsValue(childId)) {
                        for (DataSnapshot postSnap : dataSnapshot.getChildren()) {

                            if ( postSnap.getKey().equals("Image")) {
                                image = postSnap.getValue().toString();
                                sessionManagement.set(childId + "Image", image);
                                mapNew.put("Image", image);
                                dataList.set(index, mapNew);
                                list.setVisibility(View.VISIBLE);
                                adapter = new TabListAdapter(getActivity(), dataList);
                                list.setAdapter(adapter);
                                continue;
                            }
                        }
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });}catch (Exception er){  }

        groupLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), ArtisanFragmentGroupChat.class));
            }
        });

        pd = new ProgressDialog(getActivity());
        pd.setMessage("Loading...");
//        pd.show();
        FloatingActionButton fabUser = (FloatingActionButton) rootView.findViewById(R.id.fabUser);


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
                } catch (Exception er) {
                    gpname.setText("Local Artisans");
                }

                startActivity(new Intent(getActivity(), ArtisanList.class));
            }
        });

//        lastMess = new Firebase("https://lobschat.firebaseio.com/lobschat/user-chat/artisan/" + sessionManagement.get("usernameId")+"/lastMess" );
        final String mainUser = sessionManagement.getUserDetails().get("User");
        lastMess = nChat.child(mainUser).child("lastMess");
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
                                        sessionManagement.set("lastMess", grpUsername + ":- " + lstMess);
                                        gplast_mess.setText(grpUsername + ":- " + lstMess);
                                    } else {
                                        sessionManagement.set("lastMess", "No message");
                                        gplast_mess.setText("No message ");
                                    }

                                    gplast_mess_date.setText(grpdate);
                                }
                            } catch (Exception nor) {
                                Crashlytics.logException(nor);

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
                    sessionManagement.set("lastMess", "No message");
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
                                        sessionManagement.set("lastMess", grpUsername + ":- " + lstMess);
                                        gplast_mess.setText(grpUsername + ":- " + lstMess);
                                    } else {
                                        gplast_mess.setText("No message ");
                                    }

                                    gplast_mess_date.setText(grpdate);
                                }
                            } catch (Exception nor) {
                                Crashlytics.logException(nor);
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
                    sessionManagement.set("lastMess", "No message");
                    gplast_mess.setText("No Message");
                }
            } else {
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
                                        sessionManagement.set("lastMess", grpUsername + ":- " + lstMess);
                                        gplast_mess.setText(grpUsername + ":- " + lstMess);
                                    } else {
                                        sessionManagement.set("lastMess", "No message");
                                        gplast_mess.setText("No message ");
                                    }

                                    gplast_mess_date.setText(grpdate);
                                }
                            } catch (Exception nor) {
                                Crashlytics.logException(nor);
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
                    sessionManagement.set("lastMess", "No message");
                    gplast_mess.setText("No Message");
                }
            }
            if(grpmessage.isEmpty() || grpmessage==null) {gplast_mess.setText("No Message");}
            lastMess.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    HashMap<String, String> mapList = new HashMap<String, String>();
//                    Map map = dataSnapshot.getValue(Map.class);
                    String userName = dataSnapshot.getKey();
                    String chatIdmessage = dataSnapshot.getValue().toString();
                    String page = chatIdmessage.split("::")[2];

                    final String chatId = chatIdmessage.split("::")[0];
                    String message = chatIdmessage.split("::")[1];
                    try {
                        nUsers.child(chatId).child("Image").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                try {
                                    image = dataSnapshot.getValue().toString();

                                } catch (Exception er) {

                                    nUsers.child(chatId).child("Image").setValue("");
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    } catch (Exception er) {
                        nUsers.child(chatId).child("Image").setValue("");
                    }


                    if (!userName.isEmpty() && page.equalsIgnoreCase("Local") && sessionManagement.get("ArtisanPage").equalsIgnoreCase("Local")) {
                        String lat = chatIdmessage.split("::")[3];
                        String log = chatIdmessage.split("::")[4];
                        String Email = chatIdmessage.split("::")[5];
                        String sex = chatIdmessage.split("::")[6];
                        if(sex.equalsIgnoreCase("m")){sex="Male";}else{sex="Female";}
                        mapList.put("Sex", sex);
                        Location startPoint = new Location("locationA");
                        startPoint.setLatitude(Double.parseDouble(userLatitude));
                        startPoint.setLongitude(Double.parseDouble(userLongitude));

                        Location endPoint = new Location("locationA");
                        endPoint.setLatitude(Double.parseDouble(lat));
                        endPoint.setLongitude(Double.parseDouble(log));

                        double distance = startPoint.distanceTo(endPoint);//distance is in meters
//                        oldMess.child(userName).setValue(chatIdmessage.split("::")[1]);
                        if (distance <= max_distance) {
//                        oldMess.child(userName).setValue(chatIdmessage.split("::")[1]);
                            sessionManagement.set("old" + userName, message);
                            mapList.put("Username", userName);
                            mapList.put("Image", image);
                            mapList.put("Email", Email);
                            mapList.put("chatId", chatIdmessage.split("::")[0]);
                            if (!dataList.contains(mapList)) {
                                mapList.put("message", message);
                                mapList.put("otherUser", "");
                                mapList.put("date", "");
                                mapList.put("read", "");
                                dataList.add(mapList);
//                                dataList.set(totalUsers,mapList);
                                totalUsers = totalUsers + 1;
                            }
                        }
                    } else if (!userName.isEmpty() && page.equalsIgnoreCase("City") && sessionManagement.get("ArtisanPage").equalsIgnoreCase("City")) {
                        String city = chatIdmessage.split("::")[3];
                        String Email = chatIdmessage.split("::")[4];
                        String sex = chatIdmessage.split("::")[5];
                        if(sex.equalsIgnoreCase("m")){sex="Male";}else{sex="Female";}
                        mapList.put("Sex", sex);
//                        oldMess.child(userName).setValue(chatIdmessage.split("::")[1]);
                        if (city.equalsIgnoreCase(sessionManagement.get("City").replace(" ", ""))) {
//                        oldMess.child(userName).setValue(chatIdmessage.split("::")[1]);
                            sessionManagement.set("old" + userName, message);
                            mapList.put("Username", userName);
                            mapList.put("Image", image);
                            mapList.put("Email", Email);
                            mapList.put("chatId", chatIdmessage.split("::")[0]);
                            if (!dataList.contains(mapList)) {
                                mapList.put("message", message);
                                mapList.put("otherUser", "");
                                mapList.put("date", "");
                                mapList.put("read", "");

                                dataList.add(mapList);
//                                dataList.set(totalUsers,mapList);
                                totalUsers = totalUsers + 1;
                            }
                        }
                    } else if (!userName.isEmpty() && page.equalsIgnoreCase("State") && sessionManagement.get("ArtisanPage").equalsIgnoreCase("State")) {
                        String state = chatIdmessage.split("::")[3];
                        String Email = chatIdmessage.split("::")[4];
                        String sex = chatIdmessage.split("::")[5];
                        if(sex.equalsIgnoreCase("m")){sex="Male";}else{sex="Female";}
                        mapList.put("Sex", sex);
//                        oldMess.child(userName).setValue(chatIdmessage.split("::")[1]);
                        if (state.equalsIgnoreCase(sessionManagement.get("State").replace(" ", ""))) {
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
                                mapList.put("Email", Email);
                                dataList.add(mapList);
//                                dataList.set(totalUsers,mapList);
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
                    String page = chatIdmessage.split("::")[2];

                    String message = chatIdmessage.split("::")[1];
                    final String chatId = chatIdmessage.split("::")[0];
                    try {
                        nUsers.child(chatId).child("Image").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                try {
                                    image = dataSnapshot.getValue().toString();

                                } catch (Exception er) {

                                    nUsers.child(chatId).child("Image").setValue("");
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    } catch (Exception er) {
                        nUsers.child(chatId).child("Image").setValue("");
                    }

//                    String oldmessage = sessionManagement.get("old" + userName);

                    if (!userName.isEmpty() ) {

                        mapNew.put("Username", userName);
                        mapNew.put("Image", image);
                        mapNew.put("chatId", chatIdmessage.split("::")[0]);
                        mapNew.put("message", message);
//                        mapList.put("Page", chatIdmessage.split("::")[2]);
                        mapNew.put("otherUser", "");
                        mapNew.put("date", "");
                        mapNew.put("read", "");

                        int index = 0;
                        for (HashMap<String, String> dat : dataList) {

                            if (dat.containsValue(chatIdmessage.split("::")[0])) {
                                //                            mapNew.put("Username", userName);


                                dataList.set(index, mapNew);
                                list.setVisibility(View.VISIBLE);
                                adapter = new TabListAdapter(getActivity(), dataList);
                                list.setAdapter(adapter);
                                break;
                            }
                            index = index + 1;
                        }
//                        if (dataList.contains(mapNew)) {
//                            dataList.remove((mapNew));
//                            mapNew.put("message", message);
//                            dataList.add(mapNew);
//
//                        }
//                        sessionManagement.set("old" + userName, message);
                    }
//                    if (totalUsers < 1) {
//                        list.setVisibility(View.GONE);
//                    } else {
//                        list.setVisibility(View.VISIBLE);
//                        adapter = new TabListAdapter(getActivity(), dataList);
//                        list.setAdapter(adapter);
//                    }
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    HashMap<String, String> mapList = new HashMap<String, String>();
//                    Map map = dataSnapshot.getValue(Map.class);
                    String userName = dataSnapshot.getKey();
                    String chatIdmessage = dataSnapshot.getValue().toString();
                    String oldmessage = sessionManagement.get("old" + userName);
                    if (!userName.isEmpty()) {

                        int index = 0;
                        for (HashMap<String, String> dat : dataList) {

                            if (dat.containsValue(chatIdmessage.split("::")[0])) {
                                //                            mapNew.put("Username", userName);

                                dataList.remove(index);
                                list.setVisibility(View.VISIBLE);
                                adapter = new TabListAdapter(getActivity(), dataList);
                                list.setAdapter(adapter);
                                break;
                            }
                            index = index + 1;
                        }
//                        mapList.put("Username", userName);
//                        mapList.put("chatId", chatIdmessage.split(":")[0]);
//                        mapList.put("message", chatIdmessage.split(":")[1]);
//                        mapList.put("otherUser", "");
//                        mapList.put("date", "");
//                        mapList.put("read", "");
//                        if (dataList.contains(mapList)) {
//                            dataList.remove(mapList);
//                            totalUsers = totalUsers - 1;
//                        }

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
            Crashlytics.logException(e);
        }

        // Click event for single list row
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                TextView chatId = (TextView) view.findViewById(R.id.chat_id);
                TextView user_name = (TextView) view.findViewById(R.id.user_name);
                TextView read_mess = (TextView) view.findViewById(R.id.read_mess); // read
//                read_mess.setText("read");
                TextView img = (TextView)view.findViewById(R.id.img_txt);
                TextView email = (TextView)view.findViewById(R.id.email_txt);
                TextView sex = (TextView) view.findViewById(R.id.sex);
                sessionManagement.set("chatWithSex", sex.getText().toString());
                sessionManagement.set("chatWithImage",img.getText().toString());
                sessionManagement.set("chatWithEmail",email.getText().toString());
                String chatWit = "";

                chatWit = chatId.getText().toString();
                sessionManagement.set("usernameId", sessionManagement.getUserDetails().get("User"));
                sessionManagement.set("username", sessionManagement.get("MyUsername"));
                sessionManagement.set("chatWithId", chatWit);
                sessionManagement.set("chatWith", user_name.getText().toString().toLowerCase());
                sessionManagement.set("Page", "Artisan");
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
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_settings:
                startActivity(new Intent(getActivity(), Settings.class));
                return true;
            case R.id.action_local:
                sessionManagement.set("Artisan", "Local");
                sessionManagement.set("ArtisanPage", "Local");
//                gpname.setText("Local Artisans");
//                gplast_mess.setText(sessionManagement.get("lastMess"));

                ft.detach(ArtisanTabFragment.this).attach(ArtisanTabFragment.this).commit();
                return true;
            case R.id.action_city:
                sessionManagement.set("Artisan", "City");
                sessionManagement.set("ArtisanPage", "City");

                ft.detach(ArtisanTabFragment.this).attach(ArtisanTabFragment.this).commit();
//                gpname.setText(sessionManagement.get("City") + " Artisans");
//                gplast_mess.setText(sessionManagement.get("lastMess"));
                return true;
            case R.id.action_state:
                sessionManagement.set("Artisan", "State");
                sessionManagement.set("ArtisanPage", "State");
//                gpname.setText(sessionManagement.get("State") + " Artisans");
//                gplast_mess.setText(sessionManagement.get("lastMess"));
                ft.detach(ArtisanTabFragment.this).attach(ArtisanTabFragment.this).commit();
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

                }
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    Methods met = new Methods(getActivity());
                    met.showGPSDisabledAlertToUser();
                } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

                    pd.setMessage("Refreshing Location...");
                    pd.show();
                    UpdateLocation updateLocation = new UpdateLocation(getActivity(), "Local");
                    updateLocation.refresh("Local");
                    pd.dismiss();
                }

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();

        try {
            if (sessionManagement.get("Artisan").equals("City")) {
                gpname.setText(sessionManagement.get("City") + " Artisans");
            } else if (sessionManagement.get("Artisan").equals("State")) {
                gpname.setText(sessionManagement.get("State") + " Artisans");
            } else {
                gpname.setText("Local Artisans");
                sessionManagement.set("Artisan", "Local");
                sessionManagement.set("ArtisanPage", "Local");
            }
        } catch (Exception er) {
            gpname.setText("Local Artisans");
        }


    }

    @Override
    public void onResume() {
        super.onResume();
        String mainkey = sessionManagement.getUserDetails().get("User");
        DatabaseReference checkUser = nUsers.child(mainkey);
        DatabaseReference dbStatus = checkUser.child("Status");
        dbStatus.setValue("online");
//        FragmentTransaction ft = getFragmentManager().beginTransaction();
//        ft.detach(ArtisanTabFragment.this).attach(ArtisanTabFragment.this).commit();


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
        String mainkey = sessionManagement.getUserDetails().get("User");
        DatabaseReference checkUser = nUsers.child(mainkey);
        DatabaseReference dbStatus = checkUser.child("Status");
        dbStatus.setValue("offline");
    }

    /**
     * MENU
     */

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.list) {
            MenuItem mnu1 = menu.add(3, 0, 0, "View Profile");

        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getGroupId()==3) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            long selectid = info.id; //_id from database in this case
            int selectpos = info.position; //position in the adapter
            HashMap<String, String> value;
            String chatId, username, usernameUc;
            switch (item.getItemId()) {
                case 0:
                    value = dataList.get(selectpos);
                    chatId = value.get("chatId");
                    username = value.get("Username");
                    usernameUc = username.substring(0, 1).toUpperCase() + username.substring(1).toLowerCase();
//                Toast.makeText(UsersList.this,"User: "+usernameUc, Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(getContext(), UsersInfo.class);
                    intent.putExtra("UserInfoId", chatId);
                    intent.putExtra("UserInfoName", usernameUc);
                    intent.putExtra("UserInfoUsername", username);
                    startActivity(intent);

                    return true;
                case 1:
                    Toast.makeText(getContext(), "2Just now", Toast.LENGTH_LONG).show();
                    return true;
                default:
                    return super.onContextItemSelected(item);
            }
        }
            return false;

    }
}
