package lobschat.hycode.lobschat;

/**
 * Created by Icode on 12/26/2017.
 */

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by HyCode on 3/9/2017.
 */

public class ArtisanList extends AppCompatActivity {
    boolean boolVal;
    TextView abtRemote;
    DatabaseReference nRootRef;// = FirebaseDatabase.getInstance().getReference();
    DatabaseReference dblobschat;// = nRootRef.child("lobschat");
    DatabaseReference nUsers;// = dblobschat.child("users");
    //    TabListAdapter adapte0r;
    int totalUsers = 0, totalUsersGroup = 0;
    TextView noUsersText;
    //    ProgressDialog pd;
    static ArrayList<HashMap<String, String>> dataList;
    SessionManagement sessionManagement;

    String userLatitude, userLongitude;
    FirebaseRemoteConfig remoteConfig;
    double max_distance = 10000;
    Intent intent;
    String ArtisanPage;

    private static RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private static RecyclerView recyclerView;

    static View.OnClickListener myOnClickListener;
    private static ArrayList<Integer> removedItems;

    //    static boolean calledAlready=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManagement = new SessionManagement(this);
        setContentView(R.layout.artisan_list_recycler);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/ERASMD.TTF");
//        list = (ListView) findViewById(R.id.list);
        myOnClickListener = new MyOnClickListener(this);
        try{
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        }catch (Exception ex){}
         nRootRef = FirebaseDatabase.getInstance().getReference();
         dblobschat = nRootRef.child("lobschat");
         nUsers = dblobschat.child("users");

        recyclerView = (RecyclerView) findViewById(R.id.artisan_list_recycler);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        dataList = new ArrayList<HashMap<String, String>>();

        noUsersText = (TextView) findViewById(R.id.noUsersText);
        userLatitude = sessionManagement.get("GpsLat");
        userLongitude = sessionManagement.get("GpsLog");
        intent = getIntent();

        ArtisanPage = intent.getStringExtra("ArtisanPage");
        sessionManagement.set("ArtisanPage", ArtisanPage);
        noUsersText.setTypeface(typeface);

//        pd = new ProgressDialog(this);
//        pd.setMessage("Loading...");
//        pd.show();
//        pd.setCancelable(true);
//        pd.setCanceledOnTouchOutside(false);


        if (ArtisanPage != null) {
            if (ArtisanPage.equals("City") || ArtisanPage.equals("State")) {
                toolbar.setTitle(sessionManagement.get(ArtisanPage) + " Artisan List");
            } else {
                toolbar.setTitle("Local Artisan List");
            }
        } else {
            toolbar.setTitle("Local Artisan List");
        }
        // toolbar.setBackground(new ColorDrawable(Color.parseColor("#0000ff")));
        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(toolbar);
        // Display icon in the toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);


        removedItems = new ArrayList<Integer>();

        firebasenUsers();


    }


    private void firebasenUsers() {

        final String mainUser = sessionManagement.getUserDetails().get("User");


        nUsers.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                try {
                    HashMap<String, String> mapList = new HashMap<String, String>();

                    String Username = "", status = "", userType = "", chatId = "", date = "",
                            Email = "", gpsLat = "", gpsLog = "", city = "", state = "", image = "", sex = "", Token = "", Business = "";
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
                        } else if (postSnapshot.getKey().equals("Image")) {
                            image = postSnapshot.getValue().toString();
                            continue;
                        } else if (postSnapshot.getKey().equals("Sex")) {
                            sex = postSnapshot.getValue().toString();
                            continue;
                        } else if (postSnapshot.getKey().equals("Token")) {
                            Token = postSnapshot.getValue().toString();
                            continue;
                        } else if (postSnapshot.getKey().equals("Business")) {
                            Business = postSnapshot.getValue().toString();
                            if (Business.length() > 20) {
                                Business = Business.substring(0, 20) + "...";
                            }
                            continue;
                        } else if (postSnapshot.getKey().equals("Email")) {
                            Email = postSnapshot.getValue().toString();
                            continue;
                        }
                    }

                    try {

                        if (ArtisanPage.equals("Local") && userType.equals("Artisan") && !chatId.equals(mainUser)) {
                            Location startPoint = new Location("locationA");
                            startPoint.setLatitude(Double.parseDouble(userLatitude));
                            startPoint.setLongitude(Double.parseDouble(userLongitude));

                            Location endPoint = new Location("locationA");
                            endPoint.setLatitude(Double.parseDouble(gpsLat));
                            endPoint.setLongitude(Double.parseDouble(gpsLog));

                            double distance = startPoint.distanceTo(endPoint);//distance is in meters
                            if (distance <= max_distance) {
                                if (!Username.isEmpty() && !chatId.equals(mainUser)) {
                                    mapList.put("Username", Username);
                                    mapList.put("read", status);
                                    mapList.put("chatId", chatId);
                                    mapList.put("Image", image);
                                    mapList.put("Email", Email);
                                    mapList.put("Sex", sex);
                                    mapList.put("otherUser", "");
                                    mapList.put("UserToken", Token);
                                    mapList.put("date", date);
                                    mapList.put("message", Business);
                                    int siz = 0;
                                    int doesntExist = 0;
                                    for (HashMap<String, String> dat : dataList) {

                                        if (dat.containsValue(Username)) {

                                            dataList.set(siz, mapList);

                                        } else {
                                            doesntExist = doesntExist + 1;
                                        }
                                        if (doesntExist == dataList.size()) {
                                            dataList.add(totalUsers, mapList);
                                            totalUsers = totalUsers + 1;
                                        }
                                        siz = siz + 1;
                                    }
                                    if (dataList.size() < 1) {
                                        dataList.add(totalUsers, mapList);
                                        totalUsers = totalUsers + 1;
                                    }

                                }
                            }
                        }
                    } catch (Exception nor) {
                        Crashlytics.logException(nor);
                    }
                    if (city.equals(sessionManagement.get("City")) && ArtisanPage.equals("City") && userType.equals("Artisan")) {
                        if (!Username.isEmpty() && !chatId.equals(mainUser)) {
                            mapList.put("Username", Username);
                            mapList.put("read", status);
                            mapList.put("chatId", chatId);
                            mapList.put("Image", image);
                            mapList.put("Email", Email);
                            mapList.put("Sex", sex);
                            mapList.put("otherUser", "");
                            mapList.put("UserToken", Token);
                            mapList.put("date", date);
                            mapList.put("message", Business);
                            int siz = 0;
                            int doesntExist = 0;
                            for (HashMap<String, String> dat : dataList) {

                                if (dat.containsValue(Username)) {

                                    dataList.set(siz, mapList);

                                } else {
                                    doesntExist = doesntExist + 1;
                                }
                                if (doesntExist == dataList.size()) {
                                    dataList.add(totalUsers, mapList);
                                    totalUsers = totalUsers + 1;
                                }
                                siz = siz + 1;
                            }
                            if (dataList.size() < 1) {
                                dataList.add(totalUsers, mapList);
                                totalUsers = totalUsers + 1;
                            }
                        }
                    } else if (state.equals(sessionManagement.get("State")) && ArtisanPage.equals("State") && userType.equals("Artisan")) {
                        if (!Username.isEmpty() && !chatId.equals(mainUser)) {
                            mapList.put("Username", Username);
                            mapList.put("read", status);
                            mapList.put("chatId", chatId);
                            mapList.put("Image", image);
                            mapList.put("Email", Email);
                            mapList.put("Sex", sex);
                            mapList.put("otherUser", "");
                            mapList.put("UserToken", Token);
                            mapList.put("date", date);
                            mapList.put("message", Business);
                            int siz = 0;
                            int doesntExist = 0;
                            for (HashMap<String, String> dat : dataList) {

                                if (dat.containsValue(Username)) {

                                    dataList.set(siz, mapList);

                                } else {
                                    doesntExist = doesntExist + 1;
                                }
                                if (doesntExist == dataList.size()) {
                                    dataList.add(totalUsers, mapList);
                                    totalUsers = totalUsers + 1;
                                }
                                siz = siz + 1;
                            }
                            if (dataList.size() < 1) {
                                dataList.add(totalUsers, mapList);
                                totalUsers = totalUsers + 1;
                            }
                        }
                    }

                    if (dataList.size() < 1) {
                        noUsersText.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        noUsersText.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        adapter = new ListRecyclerAdapter(ArtisanList.this, dataList);
                        recyclerView.setAdapter(adapter);
                    }
//                    pd.dismiss();
                } catch (Exception er) {
                    Crashlytics.logException(er);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                try {
                    HashMap<String, String> mapList = new HashMap<String, String>();
                    String Username = "", status = "", chatId = "", gpsLat = "", gpsLog = "", Business = "",
                            userType = "", image = "", sex = "", city = "", state = "", Token = "", Email = "", date = "";

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
                        } else if (postSnapshot.getKey().equals("Image")) {
                            image = postSnapshot.getValue().toString();
                            continue;
                        } else if (postSnapshot.getKey().equals("Sex")) {
                            sex = postSnapshot.getValue().toString();
                            continue;
                        } else if (postSnapshot.getKey().equals("Token")) {
                            Token = postSnapshot.getValue().toString();
                            continue;
                        } else if (postSnapshot.getKey().equals("Business")) {
                            Business = postSnapshot.getValue().toString();
                            if (Business.length() > 20) {
                                Business = Business.substring(0, 20) + "...";
                            }
                            continue;
                        } else if (postSnapshot.getKey().equals("Email")) {
                            Email = postSnapshot.getValue().toString();
                            continue;
                        }
                    }
                    try {

                        if (ArtisanPage.equals("Local") && userType.equals("Artisan") && !chatId.equals(mainUser)) {
                            Location startPoint = new Location("locationA");
                            startPoint.setLatitude(Double.parseDouble(userLatitude));
                            startPoint.setLongitude(Double.parseDouble(userLongitude));

                            Location endPoint = new Location("locationA");
                            endPoint.setLatitude(Double.parseDouble(gpsLat));
                            endPoint.setLongitude(Double.parseDouble(gpsLog));

                            double distance = startPoint.distanceTo(endPoint);//distance is in meters

                            if (distance < max_distance) {

                                int index = 0;
                                HashMap<String, String> mapNew = new HashMap<String, String>();

                                for (HashMap<String, String> dat : dataList) {

                                    if (dat.containsValue(chatId)) {
                                        mapList.put("Username", Username);
                                        mapList.put("read", status);
                                        mapList.put("chatId", chatId);
                                        mapList.put("Image", image);
                                        mapList.put("Email", Email);
                                        mapList.put("Sex", sex);
                                        mapList.put("otherUser", "");
                                        mapList.put("UserToken", Token);
                                        mapList.put("date", date);
                                        mapList.put("message", Business);

                                        dataList.set(index, mapList);
                                    }
                                    index = index + 1;
                                }
                            }
                        } else if (city.equals(sessionManagement.get("City")) && ArtisanPage.equals("City") && userType.equals("Artisan")) {
                            if (!Username.isEmpty() && !chatId.equals(mainUser)) {
                                mapList.put("Username", Username);
                                mapList.put("read", status);
                                mapList.put("chatId", chatId);
                                mapList.put("Image", image);
                                mapList.put("Email", Email);
                                mapList.put("Sex", sex);
                                mapList.put("otherUser", "");
                                mapList.put("UserToken", Token);
                                mapList.put("date", date);
                                mapList.put("message", Business);
                                int siz = 0;
                                int doesntExist = 0;
                                for (HashMap<String, String> dat : dataList) {

                                    if (dat.containsValue(Username)) {

                                        dataList.set(siz, mapList);

                                    } else {
                                        doesntExist = doesntExist + 1;
                                    }
                                    if (doesntExist == dataList.size()) {
                                        dataList.add(totalUsers, mapList);
                                        totalUsers = totalUsers + 1;
                                    }
                                    siz = siz + 1;
                                }
                                if (dataList.size() < 1) {
                                    dataList.add(totalUsers, mapList);
                                    totalUsers = totalUsers + 1;
                                }
                            }
                        } else if (city.equals(sessionManagement.get("State")) && ArtisanPage.equals("State") && userType.equals("Artisan")) {
                            if (!Username.isEmpty() && !chatId.equals(mainUser)) {

                            }
                        }
                        mapList.put("Username", Username);
                        mapList.put("read", status);
                        mapList.put("chatId", chatId);
                        mapList.put("Image", image);
                        mapList.put("Email", Email);
                        mapList.put("Sex", sex);
                        mapList.put("otherUser", "");
                        mapList.put("UserToken", Token);
                        mapList.put("date", date);
                        mapList.put("message", Business);
                        int siz = 0;
                        int doesntExist = 0;
                        for (HashMap<String, String> dat : dataList) {

                            if (dat.containsValue(Username)) {

                                dataList.set(siz, mapList);

                            } else {
                                doesntExist = doesntExist + 1;
                            }
                            if (doesntExist == dataList.size()) {
                                dataList.add(totalUsers, mapList);
                                totalUsers = totalUsers + 1;
                            }
                            siz = siz + 1;
                        }
                        if (dataList.size() < 1) {
                            dataList.add(totalUsers, mapList);
                            totalUsers = totalUsers + 1;
                        }
                    } catch (Exception nor) {
                        Crashlytics.logException(nor);
                    }
                    if (dataList.size() < 1) {
                        noUsersText.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        noUsersText.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        adapter = new ListRecyclerAdapter(ArtisanList.this, dataList);
                        recyclerView.setAdapter(adapter);
                    }

//                    pd.dismiss();
                } catch (Exception er) {
                    Crashlytics.logException(er);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                try {

                    String Username = "", status = "", chatId = "";
                    chatId = dataSnapshot.getKey();

                    int index = 0;


                    for (HashMap<String, String> dat : dataList) {

                        if (dat.containsValue(chatId)) {

                            dataList.remove(index);
                        }
                        index = index + 1;
                    }

                } catch (Exception er) {
                    Crashlytics.logException(er);
                }

                if (dataList.size() < 1) {
                    noUsersText.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    noUsersText.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    adapter = new ListRecyclerAdapter(ArtisanList.this, dataList);
                    recyclerView.setAdapter(adapter);

                }
//                pd.dismiss();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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


    private static class MyOnClickListener implements View.OnClickListener {

        private final Context context;
        private SessionManagement sessionManagement;
        Intent intent;

        private MyOnClickListener(Context context) {
            this.context = context;
            sessionManagement = new SessionManagement(context);
        }

        @Override
        public void onClick(View v) {
//            removeItem(v);
            ChatArtisan(v);
        }

        private void ChatArtisan(View view) {

            int selectedItemPosition = recyclerView.getChildPosition(view);
            RecyclerView.ViewHolder viewHolder
                    = recyclerView.findViewHolderForPosition(selectedItemPosition);

            String ArtisanPage = sessionManagement.get("ArtisanPage");// new ArtisanList().ArtisanPage;

            TextView chatId = (TextView) viewHolder.itemView.findViewById(R.id.chat_id);
            TextView user_name = (TextView) viewHolder.itemView.findViewById(R.id.user_name);
            TextView read = (TextView) viewHolder.itemView.findViewById(R.id.read_mess);
            TextView mess = (TextView) viewHolder.itemView.findViewById(R.id.last_mess); // read
            TextView img = (TextView) viewHolder.itemView.findViewById(R.id.img_txt);
            TextView email = (TextView) viewHolder.itemView.findViewById(R.id.email_txt);
            TextView sex = (TextView) viewHolder.itemView.findViewById(R.id.sex);
            mess.setVisibility(View.GONE);
            read.setVisibility(View.VISIBLE);
            String chatWit = "";
            chatWit = chatId.getText().toString();

            Intent intent = new Intent(context, FragmentChat.class);
            intent.putExtra("chatWithSex", sex.getText().toString());
            intent.putExtra("chatWithImage", img.getText().toString());
            intent.putExtra("chatWithEmail", email.getText().toString());
            intent.putExtra("usernameId", sessionManagement.getUserDetails().get("User"));
            intent.putExtra("username", sessionManagement.get("MyUsername"));
            intent.putExtra("chatWithId", chatWit);
            intent.putExtra("Page", ArtisanPage);
            intent.putExtra("chatWith", user_name.getText().toString());
            ((Activity) context).finish();
            viewHolder.itemView.getContext().startActivity(intent);
        }

//        private void removeItem(View v) {
//            int selectedItemPosition = recyclerView.getChildPosition(v);
//            RecyclerView.ViewHolder viewHolder
//                    = recyclerView.findViewHolderForPosition(selectedItemPosition);
//            TextView textViewName
//                    = (TextView) viewHolder.itemView.findViewById(R.id.textViewName);
//            String selectedName = (String) textViewName.getText();
//            int selectedItemId = -1;
//            for (int i = 0; i < MyData.nameArray.length; i++) {
//                if (selectedName.equals(MyData.nameArray[i])) {
//                    selectedItemId = MyData.id_[i];
//                }
//            }
//            removedItems.add(selectedItemId);
//            data.remove(selectedItemPosition);
//            adapter.notifyItemRemoved(selectedItemPosition);
//        }
    }


}
