package lobschat.hycode.lobschat;

import android.*;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
//import android.provider.ContactsContract.Data;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.gson.Gson;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import ai.api.android.AIService;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;


public class ArtisanChat extends AppCompatActivity {
    LinearLayout layout;
    Firebase reference1, reference2, lastMess;
    PostRequestData postRequestData;
    DatabaseReference nRootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference dblobschat = nRootRef.child("lobschat");
    DatabaseReference nChat = dblobschat.child("user-chat");


    ProgressDialog pgd;
    boolean start;
    String userName, message, mess_date;
    static int firstClick = 0;
    String gpsLat, gpsLng;
    SessionManagement sessionManagement;
    FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
    String address, city, state, knownName, country, postalCode;
    sendNotifyTask mAuthTask;
    String Id = "";
    String userIntId = "";

    DatabaseReference nuser, nlastmess, nlastmessUser, nchatwiith, nlastChatwithMess, nlastChatwithMessUser;
    //new
    RecyclerView recyclerView;
    EditText editText;
    RelativeLayout addBtn;
    DatabaseReference ref, ref1, ref2;
    FirebaseRecyclerAdapter<ChatMessage, chat_rec> adapter;
    Boolean flagFab = true;

    private AIService aiService;
    //end new

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        start = true;
        sessionManagement = new SessionManagement(this);

        setContentView(R.layout.chat_activity);
///new

        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.RECORD_AUDIO}, 1);
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/ERASMD.TTF");

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        editText = (EditText) findViewById(R.id.editText);
        addBtn = (RelativeLayout) findViewById(R.id.addBtn);

        editText.setTypeface(typeface);

        recyclerView.setHasFixedSize(true);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        //end new
        pgd = new ProgressDialog(ArtisanChat.this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        toolbar.setTitle("  " + sessionManagement.get("chatWith"));

        // toolbar.setBackground(new ColorDrawable(Color.parseColor("#0000ff")));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        Firebase.setAndroidContext(this);
        RelativeLayout send_layout=findViewById(R.id.send_layout);
        if(sessionManagement.get("Type").equalsIgnoreCase("Artisan")) {
            String subDetails = sessionManagement.get("SubStatus");
            String Activation = sessionManagement.get("Activation");
            String subDate = sessionManagement.get("SubDate");
            Calendar cn = Calendar.getInstance();
            Calendar cs = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String currentDate = dateFormat.format(cn.getTime());
            subDetails = subDetails + " : " + Activation;
            try {

                Date sd = dateFormat.parse(subDate);
                Date cd = dateFormat.parse(currentDate);
                long signUpdatesd = sd.getTime() / (24 * 60 * 60);
                long currentDatecd = cd.getTime() / (24 * 60 * 60);

//                    Trial Version
                if (subDetails.contains("Trial")) {
                    if (currentDatecd - signUpdatesd >= 30) {
                        send_layout.setVisibility(View.INVISIBLE);
                    } else {
                        send_layout.setVisibility(View.VISIBLE);
                    }
                } else if (subDetails.contains("Subcribed")) {
//                Subcribed
                    if (currentDatecd - signUpdatesd >= 90) {
                        send_layout.setVisibility(View.INVISIBLE);
                    } else {
                        send_layout.setVisibility(View.VISIBLE);
                    }
                } else {
                    send_layout.setVisibility(View.INVISIBLE);

                }

            } catch (Exception ex) {
                Crashlytics.logException(ex);
            }

        }


        reference1 = new Firebase("https://lobschat.firebaseio.com/lobschat/user-chat/" + sessionManagement.get("usernameId") + "--__--" + sessionManagement.get("chatWithId"));
        reference2 = new Firebase("https://lobschat.firebaseio.com/lobschat/user-chat/" + sessionManagement.get("chatWithId") + "--__--" + sessionManagement.get("usernameId"));
        ref1 = nChat.child(sessionManagement.get("usernameId") + "--__--" + sessionManagement.get("chatWithId"));
        ref2 = nChat.child(sessionManagement.get("chatWithId") + "--__--" + sessionManagement.get("usernameId"));

        nuser = nChat.child(sessionManagement.get("usernameId"));
        nlastmess = nuser.child("lastMess");
        nlastmessUser = nlastmess.child(sessionManagement.get("chatWith").toLowerCase());
        nchatwiith = nChat.child(sessionManagement.get("chatWithId"));
        nlastChatwithMess = nchatwiith.child("lastMess");
        nlastChatwithMessUser = nlastChatwithMess.child(sessionManagement.get("MyUsername").toLowerCase());


        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String messageText = editText.getText().toString();
                try {

                    if (!messageText.equals("")) {
                        ChatMessage chatMessage = new ChatMessage(messageText, sessionManagement.get("MyUsername"), sessionManagement.get("chatWith"));

                        mAuthTask = new sendNotifyTask(sessionManagement.get("chatWith"), sessionManagement.get("MyUsername"), messageText, sessionManagement.get("MyUsername"), sessionManagement.get("chatWithId"));
                        mAuthTask.execute((Void) null);
                        reference1.push().setValue(chatMessage);
                        reference2.push().setValue(chatMessage);
                        if (messageText.length() > 20) {
                            messageText = messageText.replace("::", "").substring(0, 20) + "...";
                        }
                        if (sessionManagement.get("Page").equalsIgnoreCase("Local")) {
                            nlastmessUser.setValue(sessionManagement.get("chatWithId") + "::" + messageText + "::" + sessionManagement.get("Page") + "::" + sessionManagement.get("GpsLat") + "::" + sessionManagement.get("GpsLog")+ "::" + sessionManagement.get("Email"));
                            nlastChatwithMessUser.setValue(sessionManagement.get("usernameId") + "::" + messageText + "::" + sessionManagement.get("Page") + "::" + sessionManagement.get("GpsLat") + "::" + sessionManagement.get("GpsLog")+ "::" + sessionManagement.get("Email"));
                        } else {
                            nlastmessUser.setValue(sessionManagement.get("chatWithId") + "::" + messageText + "::" + sessionManagement.get("Page") + "::" + sessionManagement.get(sessionManagement.get("Page")).replace(" ", "")+ "::" + sessionManagement.get("Email"));
                            nlastChatwithMessUser.setValue(sessionManagement.get("usernameId") + "::" + messageText + "::" + sessionManagement.get("Page") + "::" + sessionManagement.get(sessionManagement.get("Page")).replace(" ", "")+ "::" + sessionManagement.get("Email"));
                        }
                    } else {
//                        startListening();
                    }
                } catch (Exception exr) {
                    Toast.makeText(ArtisanChat.this, "Error occured, message not sent try again", Toast.LENGTH_LONG);
                }
                editText.setText("");
            }
        });




        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ImageView fab_img = (ImageView) findViewById(R.id.fab_img);
                Bitmap img = BitmapFactory.decodeResource(getResources(), R.drawable.ic_send_white_24dp);

                ImageViewAnimatedChange(ArtisanChat.this, fab_img, img);



                if (s.toString().trim().length() == 0) {

                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        adapter = new FirebaseRecyclerAdapter<ChatMessage, chat_rec>(ChatMessage.class, R.layout.msglist, chat_rec.class, ref1) {
            @Override
            protected void populateViewHolder(chat_rec viewHolder, ChatMessage model, int position) {
                try {
                    if (model.getMsgUser().equals(sessionManagement.get("MyUsername"))) {
                        viewHolder.rightText.setText(model.getMsgText());
                        viewHolder.rightText.setVisibility(View.VISIBLE);
                        viewHolder.leftText.setVisibility(View.GONE);
                    } else {
                        viewHolder.leftText.setText(model.getMsgText());
                        viewHolder.rightText.setVisibility(View.GONE);
                        viewHolder.leftText.setVisibility(View.VISIBLE);
                    }
                } catch (Exception ex) {
                }
            }
        };

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);

                int msgCount = adapter.getItemCount();
                int lastVisiblePosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();

                if (lastVisiblePosition == -1 ||
                        (positionStart >= (msgCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    recyclerView.scrollToPosition(positionStart);

                }

            }
        });

        recyclerView.setAdapter(adapter);


    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            finish();
        }else if (id == R.id.profile) {
            startActivity(new Intent(ArtisanChat.this, UsersInfo.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {

        super.onResume();
    }

    public void ImageViewAnimatedChange(Context c, final ImageView v, final Bitmap new_image) {
        final Animation anim_out = AnimationUtils.loadAnimation(c, R.anim.zoom_out);
        final Animation anim_in = AnimationUtils.loadAnimation(c, R.anim.zoom_in);
        anim_out.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                v.setImageBitmap(new_image);
                anim_in.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                    }
                });
                v.startAnimation(anim_in);
            }
        });
        v.startAnimation(anim_out);
    }

    public class sendNotifyTask extends AsyncTask<Void, Void, Boolean> {
        private String recipient, title, message, exceptUser, recipientId;

        sendNotifyTask(String nrecipient, String ntitle, String nmessage, String nexceptUser, String nrecipientId) {
            recipient = nrecipient;
            title = ntitle;
            message = nmessage;
            exceptUser = nexceptUser;
            recipientId = nrecipientId;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            Gson gson = new Gson();
            DateFormat df = new SimpleDateFormat("EEE,dd MM yyyy,HH:mm:ss");
            String date = df.format(Calendar.getInstance().getTime());
            DataMessage data = new DataMessage(recipient, message, title, "", date, exceptUser);
//            data.setTitle("Enter your message here");
            postRequestData = new PostRequestData();
            postRequestData.setTo(sessionManagement.get("Token"));


            postRequestData.setData(data);
            String json = gson.toJson(postRequestData);
            String url = "https://fcm.googleapis.com/fcm/send";

            final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody body = RequestBody.create(JSON, json);
                Request request = new Request.Builder()
                        .url(url)
                        .header("Authorization", "AAAAn8HdEPs:APA91bEJtr1jb7ieDMM_yFnDTVrvifbcrtZrSl3UJ9V86CSLTA2xBGT2y1wF8H2f_uH1XcOdRZCwzkkVf0J5Go0iNuuf3iD_v_a7iUaWDWBBHPt0wCCphbdIY8jvjbXML7pfYJyKAgJP")
                        .post(body)
                        .build();



                try {
                    client.newCall(request).execute();
                } catch (IOException e) {

                    e.printStackTrace();
                    return false;
                }
                // TODO: register the new account here.
                return true;
            } catch (Exception er) {
                return false;
            }

        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;

            if (success) {
                Toast.makeText(ArtisanChat.this, "Success Sent", Toast.LENGTH_SHORT).show();
            } else {

                Toast.makeText(ArtisanChat.this, "Failed Sent", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;

        }
    }


}