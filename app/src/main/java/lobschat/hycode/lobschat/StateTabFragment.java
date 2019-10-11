package lobschat.hycode.lobschat;

/**
 * Created by HyCode on 12/22/2017.
 */


import android.app.Activity;
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
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
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
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import java.util.HashMap;

import static android.location.LocationManager.NETWORK_PROVIDER;

public class StateTabFragment extends Fragment {
    ListView usersList;
    double max_distance = 10000;
    ArrayList<String> al = new ArrayList<>();
    int totalUsers, totalUsersGroup;
    ProgressDialog pd;
    //    ListView list;
    private static RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private static RecyclerView recyclerView;

    static View.OnClickListener myOnClickListener;
    private static ArrayList<Integer> removedItems;

    //ArrayList<HashMap<String, String>> al;
   static ArrayList<HashMap<String, String>> dataList;
    private static final String TAG = StateTabFragment.class.getSimpleName();

    static String prevMess;
    View rootView;
//    DatabaseReference nRootRef = FirebaseDatabase.getInstance().getReference();
//    DatabaseReference dblobschat = nRootRef.child("lobschat");
//    DatabaseReference nUsers = dblobschat.child("users");
//    DatabaseReference nChat = dblobschat.child("user-chat");
//    DatabaseReference ngroupChat = dblobschat.child("group-chat");

    DatabaseReference nRootRef;
    DatabaseReference dblobschat;
    DatabaseReference nUsers;
    //    DatabaseReference nChat = dblobschat.child("artisan-chat").child("user-chat");
    DatabaseReference nChat;
//    DatabaseReference ngroupChat = dblobschat.child("artisan-chat");


    SessionManagement sessionManagement;
    String address, city, state, knownName, country, postalCode;
    protected Context context;

    TextView gplast_mess_date, gpnum_users, gplast_mess, gpname, num_users;
    String userLatitude, userLongitude;
    ConstraintLayout groupLayout;
    int count2;
    boolean gpsAlert = false;
    Location locLastKnown;
    protected LocationManager locationManager;
    private AdView adView;
    String towers;
    boolean isNetworkEnabled;
    //    Firebase  oldMess;
    String grpUsername = "", grpstatus = "", grpmessage = "", grpchatId = "", grpdate = "", grpotherUser = "", grpgpsLat = "", grpgpsLog = "";
    String image = "", TodayDate;
    DatabaseReference lastMess;

    int dataNum = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        rootView = inflater.inflate(R.layout.fragment_tab, container, false);
        sessionManagement = new SessionManagement(getActivity());
        sessionManagement.set("ListPage", "StateTabFragment");
        myOnClickListener=new MyOnClickListener(getActivity());
        recyclerView = (RecyclerView) rootView.findViewById(R.id.list_recycler);
        try{
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        }catch (Exception ex){}

         nRootRef = FirebaseDatabase.getInstance().getReference();
         dblobschat = nRootRef.child("lobschat");
         nUsers = dblobschat.child("users");
        //    DatabaseReference nChat = dblobschat.child("artisan-chat").child("user-chat");
         nChat = dblobschat.child("artisan-chat");
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());


        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/ERASMD.TTF");

        userLatitude = sessionManagement.get("GpsLat");
        userLongitude = sessionManagement.get("GpsLog");

        num_users = (TextView) rootView.findViewById(R.id.num_users);
        MobileAds.initialize(getActivity(), "ca-app-pub-5527620381143347~8634487311");
        adView = (AdView) rootView.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();

        adView.loadAd(adRequest);


        dataList = new ArrayList<HashMap<String, String>>();
//        dataGroupList = new ArrayList<HashMap<String, String>>();
        totalUsers = 0;
        count2 = 0;


        registerForContextMenu(recyclerView);


        pd = new ProgressDialog(getActivity());
        pd.setMessage("Loading...");
//        pd.show();
        FloatingActionButton fabUser = (FloatingActionButton) rootView.findViewById(R.id.fabUser);


        fabUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sessionManagement.set("Page", "State");
                startActivity(new Intent(getActivity(), ArtisanList.class)
                        .putExtra("ArtisanPage", "State")
                        .putExtra("Page", "State")
                        .putExtra("Artisan", "State"));
            }
        });


firebasenUsers();



        return rootView;

    }

    private void firebasenUsers() {


        final String mainUser = sessionManagement.getUserDetails().get("User");
        lastMess = nChat.child(mainUser).child("lastMess");
        try {

            lastMess.child("State").addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(final DataSnapshot dataSnapshotParent, String s) {

//                    Map map = dataSnapshot.getValue(Map.class);

                    final String chatIdmessage = dataSnapshotParent.getValue().toString();
                    final String chatId = chatIdmessage.split("::")[0];
                    final String page = chatIdmessage.split("::")[1];

                    if (page.equalsIgnoreCase("State")) {

                        nUsers.child(chatId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                HashMap<String, String> mapList = new HashMap<String, String>();
                                String userName = dataSnapshotParent.getKey();

                                String state = "";
                                String image = "";
                                String token = "";
                                String sex = "";
                                String Email = "";
                                String desc = "";
                                String type = "";
                                for (DataSnapshot postSnap : dataSnapshot.getChildren()) {

                                    if (postSnap.getKey().equals("Image")) {
                                        image = postSnap.getValue().toString();
                                    }
                                    if (postSnap.getKey().equals("Sex")) {
                                        sex = postSnap.getValue().toString();
                                    }
                                    if (postSnap.getKey().equals("Token")) {
                                        token = postSnap.getValue().toString();
                                    }
                                    if (postSnap.getKey().equals("Email")) {
                                        Email = postSnap.getValue().toString();
                                    }
                                    if (postSnap.getKey().equals("State")) {
                                        state = postSnap.getValue().toString();
                                    }
                                    if (postSnap.getKey().equals("Business")) {
                                        desc = postSnap.getValue().toString();
                                    }
                                    if (postSnap.getKey().equals("Username")) {
                                        userName = postSnap.getValue().toString();
                                    }
                                    if (postSnap.getKey().equals("Type")) {
                                        type = postSnap.getValue().toString();
                                    }
                                }

                                if (!userName.isEmpty() && page.equalsIgnoreCase("State") && type.equalsIgnoreCase("Artisan")) {

                                    if (state.equalsIgnoreCase(sessionManagement.get("State"))) {


                                        int siz=0;
                                        int doesntExist=0;
                                        mapList.put("Username", userName);
                                        mapList.put("Sex", sex);
                                        mapList.put("message", desc);
                                        mapList.put("otherUser", "");
                                        mapList.put("date", "");
                                        mapList.put("read", "");
                                        mapList.put("UserToken", token);
                                        mapList.put("Image", image);
                                        mapList.put("chatId", chatId);
                                        for (HashMap<String, String> dat : dataList) {

                                            if(dat.containsValue(userName)){

                                                dataList.set(siz,mapList);

                                            }else{doesntExist=doesntExist+1;}
                                            if(doesntExist==dataList.size()){
                                                dataList.add(totalUsers,mapList);
                                                totalUsers=totalUsers+1;
                                            }
                                            siz=siz+1;
                                        }
                                        if(dataList.size()<1){
                                            dataList.add(totalUsers,mapList);
                                            totalUsers=totalUsers+1;
                                        }
                                    }
                                }
                                if (dataList.size() < 1) {
                                    recyclerView.setVisibility(View.GONE);
                                } else {
                                    recyclerView.setVisibility(View.VISIBLE);
                                    adapter = new StateTabAdapter(getActivity(), dataList);
                                    recyclerView.setAdapter(adapter);
                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });


                    }

                }

                @Override
                public void onChildChanged(final DataSnapshot dataSnapshotParent, String s) {

//                    Map map = dataSnapshot.getValue(Map.class);

                    final String chatIdmessage = dataSnapshotParent.getValue().toString();
                    final String chatId = chatIdmessage.split("::")[0];
                    final String page = chatIdmessage.split("::")[1];

                    if (page.equalsIgnoreCase("State")) {

                        nUsers.child(chatId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                HashMap<String, String> mapList = new HashMap<String, String>();
                                String userName = dataSnapshotParent.getKey();

                                String state = "";
                                String image = "";
                                String token = "";
                                String sex = "";
                                String Email = "";
                                String desc = "";
                                String type = "";
                                for (DataSnapshot postSnap : dataSnapshot.getChildren()) {

                                    if (postSnap.getKey().equals("Image")) {
                                        image = postSnap.getValue().toString();
                                    }
                                    if (postSnap.getKey().equals("Sex")) {
                                        sex = postSnap.getValue().toString();
                                    }
                                    if (postSnap.getKey().equals("Token")) {
                                        token = postSnap.getValue().toString();
                                    }
                                    if (postSnap.getKey().equals("Email")) {
                                        Email = postSnap.getValue().toString();
                                    }
                                    if (postSnap.getKey().equals("State")) {
                                        state = postSnap.getValue().toString();
                                    }
                                    if (postSnap.getKey().equals("Business")) {
                                        desc = postSnap.getValue().toString();
                                    }
                                    if (postSnap.getKey().equals("Username")) {
                                        userName = postSnap.getValue().toString();
                                    }
                                    if (postSnap.getKey().equals("Type")) {
                                        type = postSnap.getValue().toString();
                                    }
                                }

                                if (!userName.isEmpty() && page.equalsIgnoreCase("State") && state != "" && type.equalsIgnoreCase("Artisan")) {

                                    if (state.equalsIgnoreCase(sessionManagement.get("State"))) {

                                        int index = 0;
                                        for (HashMap<String, String> dat : dataList) {

                                            if (dat.containsValue(chatId)) {
                                                mapList.put("Username", userName);
                                                mapList.put("Sex", sex);
                                                mapList.put("message", desc);
                                                mapList.put("otherUser", "");
                                                mapList.put("date", "");
                                                mapList.put("read", "");
                                                mapList.put("UserToken", token);
                                                mapList.put("Image", image);
                                                mapList.put("chatId", chatId);
                                                mapList.put("Email", Email);
                                                dataList.set(index, mapList);
                                                break;
                                            }
                                            index = index + 1;
                                        }
                                    }
                                }
                                if (dataList.size() < 1) {
                                    recyclerView.setVisibility(View.GONE);
                                } else {
                                    recyclerView.setVisibility(View.VISIBLE);
                                    adapter = new StateTabAdapter(getActivity(), dataList);
                                    recyclerView.setAdapter(adapter);
                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });


                    }

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    HashMap<String, String> mapList = new HashMap<String, String>();
//                    Map map = dataSnapshot.getValue(Map.class);
                    String userName = dataSnapshot.getKey();
                    String chatIdmessage = dataSnapshot.getValue().toString();
                    String page = chatIdmessage.split("::")[1];

                    if (page.equalsIgnoreCase("State")) {

                        int index = 0;
                        for (HashMap<String, String> dat : dataList) {

                            if (dat.containsValue(chatIdmessage.split("::")[0])) {
                                //                            mapNew.put("Username", userName);
                                dataList.remove(index);

                                break;
                            }
                            index = index + 1;
                        }
                    }
                    if (dataList.size() < 1) {
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                        adapter = new StateTabAdapter(getActivity(), dataList);
                        recyclerView.setAdapter(adapter);
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
    }


    // Menu icons are inflated just as they were with actionbar
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if(sessionManagement.get("Type").equalsIgnoreCase("Artisan")){
            inflater.inflate(R.menu.artisan_tabfragment_menu, menu);
        }else {
            inflater.inflate(R.menu.state_tabfragment_menu, menu);
        }

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_settings:
                startActivity(new Intent(getActivity(), Settings.class));
                return true;
            case R.id.my_product:
                startActivity(new Intent(getActivity(),MyProducts.class));
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
                                        String mainkey = sessionManagement.getUserDetails().get("User");
                                        DatabaseReference checkUser = nUsers.child(mainkey);
                                        DatabaseReference dbStatus = checkUser.child("Status");
                                        dbStatus.setValue("offline");
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
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    Methods met = new Methods(getActivity());
                    met.showGPSDisabledAlertToUser();
                } else{
                    pd.setMessage("Refreshing Location...");
                    pd.show();
                    UpdateLocation updateLocation = new UpdateLocation(getActivity(), "State");
                    updateLocation.refresh("State");
                    pd.dismiss();
                }

                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onResume() {
        super.onResume();
        sessionManagement = new SessionManagement(getActivity());
        String mainkey = sessionManagement.getUserDetails().get("User");
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
        sessionManagement = new SessionManagement(getActivity());
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
            MenuItem mnu1 = menu.add(0, 0, 0, "View Profile");

        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getGroupId() == 0) {
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


    private static class MyOnClickListener implements View.OnClickListener {

        private final Context context;
        private SessionManagement sessionManagement;
        Intent intent;

        private MyOnClickListener(Context context) {
            this.context = context;
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
            sessionManagement = new SessionManagement(context);

            TextView chatId = (TextView) view.findViewById(R.id.chat_id);
            TextView user_name = (TextView) view.findViewById(R.id.user_name);
            TextView read_mess = (TextView) view.findViewById(R.id.read_mess); // read
//                read_mess.setText("read");
            TextView img = (TextView) view.findViewById(R.id.img_txt);
            TextView email = (TextView) view.findViewById(R.id.email_txt);
//                sessionManagement.set("chatWithImage",img.getText().toString());
//                sessionManagement.set("chatWithEmail",email.getText().toString());
            String chatWit = "";

            chatWit = chatId.getText().toString();
            sessionManagement.set("Page", "State");
            sessionManagement.set("ArtisanPage", "State");
            TextView sex = (TextView) view.findViewById(R.id.sex);
            TextView token = (TextView) view.findViewById(R.id.token);
            Intent intent = new Intent(context, FragmentChat.class);
            intent.putExtra("usernameId", sessionManagement.getUserDetails().get("User"));
            intent.putExtra("username", sessionManagement.get("MyUsername"));
            intent.putExtra("chatWithId", chatWit);
            intent.putExtra("UserToken", token.getText().toString());
            intent.putExtra("chatWithSex", sex.getText().toString());
            intent.putExtra("chatWithImage", img.getText().toString());
            intent.putExtra("chatWithEmail", email.getText().toString());
            intent.putExtra("Page", "State");
            intent.putExtra("ArtisanPage", "State");
            intent.putExtra("chatWith", user_name.getText().toString());
//            ((Activity) context).finish();
            viewHolder.itemView.getContext().startActivity(intent);
//
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