package lobschat.hycode.lobschat;

/**
 * Created by HyCode on 5/16/2017.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
//import android.support.v7.widget.CircleImageView;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersInfo extends AppCompatActivity {

    private Button sendEmail, remove, signOut;
    private ImageView btn_select_image;
    private EditText oldEmail, newEmail, password, newPassword;
    private TextView profile_name, profile_desc,noUsersText,profile_email;
//    private TextView  profile_email, act_type;
    private ProgressDialog progressBar;

    private FirebaseAuth auth;
    SessionManagement session;
    StorageReference storageReference;
    String image = "";
    String phone = "";
//    CircleImageView profile_img, old_profile_img;
    static FirebaseUser userDetails = null;
    DatabaseReference nRootRef;
    DatabaseReference dblobschat;
    DatabaseReference nUsers;
    DatabaseReference nProducts;
    private final int PICK_IMAGE_REQUEST = 71;
    private ImageView imageView;
//    ArrayAdapter adapter;
    private Uri filePath;
//    Firebase reference1;
    Intent intent;
    String UserInfoName,UserInfoId;
    private AdView adView;
    String sex,email_txt,image_txt;
//    static boolean calledAlready=false;
    ViewPager imageviewpager;
    static ArrayList<HashMap<String, String>> dataList;
    private static RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private static RecyclerView recyclerView;
    int totalUsers=0;
    Button chatBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.users_info_recycler);
        session = new SessionManagement(UsersInfo.this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        intent=getIntent();
        UserInfoName=  intent.getStringExtra("UserInfoName");
        UserInfoId=  intent.getStringExtra("UserInfoId");
        image_txt=  intent.getStringExtra("image_txt");
        email_txt=  intent.getStringExtra("email_txt");
        toolbar.setTitle("  " + UserInfoName);
        // toolbar.setBackground(new ColorDrawable(Color.parseColor("#0000ff")));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        recyclerView = (RecyclerView) findViewById(R.id.users_info_recycler);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        findViewById(R.id.addProduct).setVisibility(View.GONE);
        dataList = new ArrayList<HashMap<String, String>>();

        noUsersText = (TextView) findViewById(R.id.noUsersText);
        chatBtn = (Button) findViewById(R.id.chat_btn);
        try{
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        }catch (Exception ex){}
        nRootRef = FirebaseDatabase.getInstance().getReference();
        dblobschat = nRootRef.child("lobschat");
        nUsers = dblobschat.child("users");
        nProducts = dblobschat.child("products");

        Firebase.setAndroidContext(this);

        auth = FirebaseAuth.getInstance();
        MobileAds.initialize(this, "ca-app-pub-5527620381143347~8634487311");
        adView = (AdView) findViewById(R.id.adView);

        AdRequest adRequest = new AdRequest.Builder().build();

        adView.loadAd(adRequest);



        profile_name = (TextView) findViewById(R.id.profile_name);
        profile_email = (TextView) findViewById(R.id.profile_email);
        profile_desc = (TextView) findViewById(R.id.profile_desc);

//        act_type = (TextView) findViewById(R.id.act_type);
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/ERASMD.TTF");
        profile_desc.setTypeface(typeface);
        chatBtn.setTypeface(typeface);
        chatBtn.setVisibility(View.GONE);
//        act_type.setTypeface(typeface);
        profile_email.setTypeface(typeface);
        profile_name.setTypeface(typeface);
        sex = intent.getStringExtra("Sex");//session.get("Sex");
//        if (sex.equalsIgnoreCase("Female")) {
//            GlideApp.with(UsersInfo.this)
//                    .load(ImgShowDefault(sex))
//                    .placeholder(ImgShowDefault(sex))
//                    .error(ImgShowDefault(sex))
//                    .into(profile_img);

        profile_name.setText(UserInfoName);

        nUsers.child(UserInfoId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
             if (postSnapshot.getKey().equals("Username")) {
                        profile_name.setText(postSnapshot.getValue().toString());
                 chatBtn.setText("Chat "+postSnapshot.getValue().toString());
                    }else if (postSnapshot.getKey().equals("Business")) {
                        profile_desc.setText(postSnapshot.getValue().toString());
                        if (profile_desc.getText().toString().isEmpty() ||
                                profile_desc.getText().toString() == "") {
                            profile_desc.setVisibility(View.GONE);
                        } else {
                            profile_desc.setVisibility(View.VISIBLE);
                        }
                    } else if (postSnapshot.getKey().equals("Email")) {
                        profile_email.setText(postSnapshot.getValue().toString());
                    }
                }
//                progressBar.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {

            }
        });

        nProducts.child(UserInfoId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    HashMap<String, String> mapList = new HashMap<String, String>();
                    String Key = postSnapshot.getKey();
                    mapList.put("Key", Key);
                    for (DataSnapshot productSnapshot : postSnapshot.getChildren()) {
                        if (productSnapshot.getKey().equals("Description")) {
                            mapList.put("Description", productSnapshot.getValue().toString());
                        } else if (productSnapshot.getKey().equals("Rating")) {
                            mapList.put("Rating", productSnapshot.getValue().toString());

                        } else if (productSnapshot.getKey().equals("Category")) {
                            mapList.put("Category", productSnapshot.getValue().toString());
                        } else if (productSnapshot.getKey().equals("Status")) {
                            mapList.put("Status", productSnapshot.getValue().toString());
                        } else if (productSnapshot.getKey().equals("Image")) {
                            mapList.put("Image", productSnapshot.getValue().toString());
                        }
                    }
                    int siz = 0;
                    int doesntExist = 0;
                    for (HashMap<String, String> dat : dataList) {

                        if (dat.containsValue(Key)) {

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
                    if (dataList.size() < 1 && mapList.size()>1) {
                        dataList.add(totalUsers, mapList);
                        totalUsers = totalUsers + 1;
                    }
                    if (dataList.size() < 1) {
                        noUsersText.setVisibility(View.VISIBLE);

                        recyclerView.setVisibility(View.GONE);
                    } else {
                        noUsersText.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);

                        adapter = new ProductInfoRecyclerAdapter(UsersInfo.this, dataList);
                        recyclerView.setAdapter(adapter);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {

            }
        });

chatBtn.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {

        String chatWit="";

        chatWit=UserInfoId;


        Intent intent=new Intent(UsersInfo.this, FragmentChat.class);
        intent.putExtra("usernameId", session.getUserDetails().get("User"));
        intent.putExtra("username", session.get("MyUsername"));
        intent.putExtra("chatWithId", chatWit);
        intent.putExtra("UserToken", intent.getStringExtra("UserToken"));
        intent.putExtra("chatWithSex", sex);
        intent.putExtra("chatWithImage", image_txt);
        intent.putExtra("chatWithEmail", email_txt);
        intent.putExtra("Page", session.get("Page"));
        intent.putExtra("ArtisanPage", session.get("ArtisanPage"));
        intent.putExtra("chatWith", UserInfoName);

        startActivity(intent);
    }
});





    }


    public void signOut() {
        auth.signOut();
        session.logoutUser();
    }

    @Override
    protected void onResume() {
        super.onResume();


    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }




}