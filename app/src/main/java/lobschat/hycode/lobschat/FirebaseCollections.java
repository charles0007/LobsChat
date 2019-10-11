package lobschat.hycode.lobschat;

import android.content.Context;
import android.location.Location;
import android.view.View;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class FirebaseCollections {

    DatabaseReference nRootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference dblobschat = nRootRef.child("lobschat");
    DatabaseReference nUsers = dblobschat.child("users");
    //    DatabaseReference nChat = dblobschat.child("artisan-chat").child("user-chat");
    DatabaseReference nChat = dblobschat.child("artisan-chat");
    ArrayList<HashMap<String, String>> dataList=new ArrayList<HashMap<String, String>>();;
    ArrayList<HashMap<String, String>> LocalArtisans=new ArrayList<HashMap<String, String>>();;
    ArrayList<HashMap<String, String>> CityArtisans=new ArrayList<HashMap<String, String>>();;
    ArrayList<HashMap<String, String>> StateArtisans=new ArrayList<HashMap<String, String>>();;
    ArrayList<HashMap<String, String>> LocalTab=new ArrayList<HashMap<String, String>>();;
    ArrayList<HashMap<String, String>> CityTab=new ArrayList<HashMap<String, String>>();;
    ArrayList<HashMap<String, String>> StateTab=new ArrayList<HashMap<String, String>>();;

//    LocalTab = new ArrayList<HashMap<String, String>>();
//    CityTab = new ArrayList<HashMap<String, String>>();
//    StateTab = new ArrayList<HashMap<String, String>>();
//    LocalArtisans = new ArrayList<HashMap<String, String>>();
//    CityArtisans = new ArrayList<HashMap<String, String>>();
//    StateArtisans = new ArrayList<HashMap<String, String>>();
    String ArtisanPage="Local";
    SessionManagement sessionManagement;
    DatabaseReference lastMess;
    int totalUsers = 0, totalUsersGroup = 0;
    public void FirebaseCollections(Context _contex, final String ArtisanPage){
sessionManagement=new SessionManagement(_contex);

       final double max_distance = 100;
        final String userLatitude = sessionManagement.get("GpsLat");
        final String userLongitude = sessionManagement.get("GpsLog");
        final String mainUser = sessionManagement.getUserDetails().get("User");


        ///ArtisanList
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


            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }


        });

       //Local City State Tabs
        lastMess=nChat.child(mainUser).child("lastMess");
        lastMess.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(final DataSnapshot dataSnapshotParent, String s) {
                HashMap<String, String> mapList = new HashMap<String, String>();
//                    Map map = dataSnapshot.getValue(Map.class);
//                    String userName = dataSnapshotParent.getKey();
                String chatIdmessage = dataSnapshotParent.getValue().toString();
                final String chatId=chatIdmessage.split("::")[0];
                final String page=chatIdmessage.split("::")[1];


                if(page.equalsIgnoreCase("Local")) {
                    nUsers.child(chatId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            HashMap<String, String> mapList = new HashMap<String, String>();
                            String userName = dataSnapshotParent.getKey();
                            String lat="";
                            String log="";
                            String image="";
                            String token="";
                            String sex="";
                            String Email="";
                            String desc="";
                            String type="";
                            for (DataSnapshot postSnap : dataSnapshot.getChildren()) {

                                if (postSnap.getKey().equals("Image")) {
                                    image=postSnap.getValue().toString();
                                }
                                if (postSnap.getKey().equals("Sex")) {
                                    sex=postSnap.getValue().toString();
                                }
                                if (postSnap.getKey().equals("Token")) {
                                    token=postSnap.getValue().toString();
                                }if (postSnap.getKey().equals("Email")) {
                                    Email=postSnap.getValue().toString();
                                }if (postSnap.getKey().equals("GpsLat")) {
                                    lat=postSnap.getValue().toString();
                                }if (postSnap.getKey().equals("GpsLog")) {
                                    log=postSnap.getValue().toString();
                                }if (postSnap.getKey().equals("Business")) {
                                    desc=postSnap.getValue().toString();
                                }if (postSnap.getKey().equals("Username")) {
                                    userName=postSnap.getValue().toString();
                                }if (postSnap.getKey().equals("Type")) {
                                    type=postSnap.getValue().toString();
                                }
                            }

                            if (!userName.isEmpty() && page.equalsIgnoreCase("Local") && log!="" && type.equalsIgnoreCase("Artisan") ) {
                                Location startPoint = new Location("locationA");
                                startPoint.setLatitude(Double.parseDouble(userLatitude));
                                startPoint.setLongitude(Double.parseDouble(userLongitude));

                                Location endPoint = new Location("locationA");
                                endPoint.setLatitude(Double.parseDouble(lat));
                                endPoint.setLongitude(Double.parseDouble(log));

                                double distance = startPoint.distanceTo(endPoint);//distance is in meters
//                        oldMess.child(userName).setValue(chatIdmessage.split("::")[1]);
                                if(distance<=max_distance) {


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
                                    if(dataList.size()>0) {
                                        for (HashMap<String, String> dat : dataList) {

                                            if (dat.containsValue(userName)) {

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
                                    }else{
                                        dataList.add(totalUsers,mapList);
                                        totalUsers=totalUsers+1;
                                    }

                                }
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
                HashMap<String, String> mapList = new HashMap<String, String>();
//                    Map map = dataSnapshot.getValue(Map.class);

                String chatIdmessage = dataSnapshotParent.getValue().toString();
                final String chatId=chatIdmessage.split("::")[0];
                final String page=chatIdmessage.split("::")[1];

                if(page.equalsIgnoreCase("Local")) {
                    nUsers.child(chatId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            HashMap<String, String> mapList = new HashMap<String, String>();
                            String userName = dataSnapshotParent.getKey();
                            String lat="";
                            String log="";
                            String image="";
                            String token="";
                            String sex="";
                            String Email="";
                            String desc="";
                            String type="";
                            for (DataSnapshot postSnap : dataSnapshot.getChildren()) {

                                if (postSnap.getKey().equals("Image")) {
                                    image=postSnap.getValue().toString();
                                }
                                if (postSnap.getKey().equals("Sex")) {
                                    sex=postSnap.getValue().toString();
                                }
                                if (postSnap.getKey().equals("Token")) {
                                    token=postSnap.getValue().toString();
                                }if (postSnap.getKey().equals("Email")) {
                                    Email=postSnap.getValue().toString();
                                }if (postSnap.getKey().equals("GpsLat")) {
                                    lat=postSnap.getValue().toString();
                                }if (postSnap.getKey().equals("GpsLog")) {
                                    log=postSnap.getValue().toString();
                                }if (postSnap.getKey().equals("Business")) {
                                    desc=postSnap.getValue().toString();
                                }if (postSnap.getKey().equals("Username")) {
                                    userName=postSnap.getValue().toString();
                                }if (postSnap.getKey().equals("Type")) {
                                    type=postSnap.getValue().toString();
                                }
                            }

                            if (!userName.isEmpty() && page.equalsIgnoreCase("Local") && log!="" && type.equalsIgnoreCase("Artisan") ) {
                                Location startPoint = new Location("locationA");
                                startPoint.setLatitude(Double.parseDouble(userLatitude));
                                startPoint.setLongitude(Double.parseDouble(userLongitude));

                                Location endPoint = new Location("locationA");
                                endPoint.setLatitude(Double.parseDouble(lat));
                                endPoint.setLongitude(Double.parseDouble(log));

                                double distance = startPoint.distanceTo(endPoint);//distance is in meters
//                        oldMess.child(userName).setValue(chatIdmessage.split("::")[1]);
                                if(distance<=max_distance) {

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
                                            dataList.set(index, mapList);
                                            break;
                                        }
                                        index = index + 1;
                                    }
                                }
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
                String page=chatIdmessage.split("::")[1];

                if (page.equalsIgnoreCase("Local")) {

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


            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {

            }
        });


    }

}
