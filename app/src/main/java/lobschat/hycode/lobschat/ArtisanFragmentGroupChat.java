package lobschat.hycode.lobschat;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import ai.api.AIDataService;
import ai.api.AIListener;
import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Result;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;


public class ArtisanFragmentGroupChat extends AppCompatActivity implements AIListener {
    LinearLayout layout;
    Firebase reference1;

    DatabaseReference nRootRef;
    DatabaseReference dblobschat;
    DatabaseReference nChat;

    ProgressDialog pgd;
    boolean start;
    String userName, message;
    SessionManagement sessionManagement;
    String address, city, state, knownName, country, postalCode;
    sendNotifyTask mAuthTask;
    int max_distance = 10000;

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
        setContentView(R.layout.artisan_chat);

        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/ERASMD.TTF");
        nRootRef = FirebaseDatabase.getInstance().getReference();
        dblobschat = nRootRef.child("lobschat");

        nChat = dblobschat.child("artisan-chat");


        pgd = new ProgressDialog(ArtisanFragmentGroupChat.this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (sessionManagement.get("ArtisanPage") != null) {
            if (sessionManagement.get("ArtisanPage").equals("City") || sessionManagement.get("ArtisanPage").equals("State")) {
                toolbar.setTitle(sessionManagement.get(sessionManagement.get("ArtisanPage")) + " Artisan Discussion");
            } else {
                toolbar.setTitle("Local Artisan Discussion");
            }
        } else {
            toolbar.setTitle("Local Artisan Discussion");
        }

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        editText = (EditText) findViewById(R.id.editText);
        addBtn = (RelativeLayout) findViewById(R.id.addBtn);
        editText.setTypeface(typeface);
        recyclerView.setHasFixedSize(true);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);


        nRootRef.keepSynced(true);
        Firebase.setAndroidContext(this);

        RelativeLayout send_layout = findViewById(R.id.send_layout);

        if (sessionManagement.get("Type").equalsIgnoreCase("Artisan")) {
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

            }

        }


        if (sessionManagement.get("ArtisanPage").equals("City") || sessionManagement.get("ArtisanPage").equals("State")) {
            reference1 = new Firebase("https://lobschat.firebaseio.com/lobschat/artisan-chat/" + sessionManagement.get("ArtisanPage") + "/" + sessionManagement.get(sessionManagement.get("ArtisanPage")).replace(" ", ""));
        } else {
            reference1 = new Firebase("https://lobschat.firebaseio.com/lobschat/artisan-chat/Local");
        }//0c01e159babc4349b38eca698bd2f107
        final AIConfiguration config = new AIConfiguration("2f98032aa53346dda39b6ac7126c51a3",
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);

        aiService = AIService.getService(this, config);
        aiService.setListener(this);

        final AIDataService aiDataService = new AIDataService(config);

        final AIRequest aiRequest = new AIRequest();


        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = editText.getText().toString();
                try {
                    if (!messageText.equals("")) {
                        ChatMessage chatMessage;

                        String exceptUser = sessionManagement.getUserDetails().get("User");
                        if (sessionManagement.get("ArtisanPage").equals("City") || sessionManagement.get("ArtisanPage").equals("State")) {
                            chatMessage = new ChatMessage(messageText, sessionManagement.get("MyUsername"), sessionManagement.get("City"), "city");
                            mAuthTask = new sendNotifyTask(sessionManagement.get(sessionManagement.get("ArtisanPage")), sessionManagement.get(sessionManagement.get("ArtisanPage")) + " Artisan", messageText, exceptUser);
                        } else {
                            chatMessage = new ChatMessage(messageText, sessionManagement.get("MyUsername"), sessionManagement.get("GpsLat"), sessionManagement.get("GpsLog"), "local");
                            mAuthTask = new sendNotifyTask("Local", "Local Artisan", messageText, exceptUser);
                        }
                        mAuthTask.execute((Void) null);

                        reference1.push().setValue(chatMessage);
                        aiRequest.setQuery(messageText);
                        new AsyncTask<AIRequest, Void, AIResponse>() {

                            @Override
                            protected AIResponse doInBackground(AIRequest... aiRequests) {
                                final AIRequest request = aiRequests[0];
                                try {
                                    final AIResponse response = aiDataService.request(aiRequest);
                                    return response;
                                } catch (AIServiceException e) {
                                }
                                return null;
                            }

                            @Override
                            protected void onPostExecute(AIResponse response) {
                                if (response != null) {

                                    Result result = response.getResult();
                                    String reply = result.getFulfillment().getSpeech();
                                    ChatMessage chatMessage;

                                    if (sessionManagement.get("ArtisanPage").equals("City") || sessionManagement.get("ArtisanPage").equals("State")) {
                                        chatMessage = new ChatMessage(reply, "Bot", sessionManagement.get(sessionManagement.get("ArtisanPage")), sessionManagement.get("ArtisanPage").toLowerCase());

                                        nChat.child(sessionManagement.get("ArtisanPage")).child(sessionManagement.get(sessionManagement.get("ArtisanPage")).replace(" ", "")).push().setValue(chatMessage);
                                    } else {
                                        chatMessage = new ChatMessage(reply, "Bot", sessionManagement.get("GpsLat"), sessionManagement.get("GpsLog"), "local");

                                        nChat.child("Local").push().setValue(chatMessage);
                                    }
                                }
                            }
                        }.execute(aiRequest);

                    }
                } catch (Exception exr) {
                    Toast.makeText(ArtisanFragmentGroupChat.this, "Error occured, message not sent try again", Toast.LENGTH_LONG);
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

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        if (sessionManagement.get("ArtisanPage").equals("City") || sessionManagement.get("ArtisanPage").equals("State")) {
            adapter = new FirebaseRecyclerAdapter<ChatMessage, chat_rec>(ChatMessage.class, R.layout.msglist, chat_rec.class, nChat.child(sessionManagement.get("ArtisanPage")).child(sessionManagement.get(sessionManagement.get("ArtisanPage")).replace(" ", ""))) {
                @Override
                protected void populateViewHolder(chat_rec viewHolder, ChatMessage model, int position) {
                    if (model.getMsgCity() != null && !model.getMsgCity().isEmpty()) {
                        if (model.getMsgCity().equalsIgnoreCase(sessionManagement.get(sessionManagement.get("ArtisanPage")))) {
                            if (model.getMsgUser().equalsIgnoreCase(sessionManagement.get("MyUsername"))) {


                                viewHolder.rightText.setText(model.getMsgText());

                                viewHolder.rightText.setVisibility(View.VISIBLE);
                                viewHolder.leftText.setVisibility(View.GONE);
                            } else {
                                viewHolder.leftText.setText(model.getMsgUser() + " \n " + model.getMsgText());

                                viewHolder.rightText.setVisibility(View.GONE);
                                viewHolder.leftText.setVisibility(View.VISIBLE);
                            }
                        }
                    }

                }
            };
        } else {
            adapter = new FirebaseRecyclerAdapter<ChatMessage, chat_rec>(ChatMessage.class, R.layout.msglist, chat_rec.class, nChat.child("Local")) {
                @Override
                protected void populateViewHolder(chat_rec viewHolder, ChatMessage model, int position) {
                    double distance = 0;
                    try {
                        Location startPoint = new Location("locationA");
                        startPoint.setLatitude(Double.parseDouble(sessionManagement.get("GpsLat")));
                        startPoint.setLongitude(Double.parseDouble(sessionManagement.get("GpsLog")));

                        Location endPoint = new Location("locationA");
                        endPoint.setLatitude(Double.parseDouble(model.getMsgLat()));
                        endPoint.setLongitude(Double.parseDouble(model.getMsgLng()));

                        distance = startPoint.distanceTo(endPoint);//distance is in meters
                    } catch (Exception er) {
                    }
                    if (distance <= max_distance) {
                        try {
                            if (model.getMsgUser().equalsIgnoreCase(sessionManagement.get("MyUsername"))) {


                                viewHolder.rightText.setText(model.getMsgText());

                                viewHolder.rightText.setVisibility(View.VISIBLE);
                                viewHolder.leftText.setVisibility(View.GONE);
                            } else {
                                viewHolder.leftText.setText(model.getMsgUser() + " \n " + model.getMsgText());

                                viewHolder.rightText.setVisibility(View.GONE);
                                viewHolder.leftText.setVisibility(View.VISIBLE);
                            }

                        } catch (Exception ex) {
                        }

                    }
                }


            };
        }
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
        }
        return super.onOptionsItemSelected(item);
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

    @Override
    protected void onResume() {

        super.onResume();
    }

    @Override
    public void onResult(AIResponse response) {

        Result result = response.getResult();
        String message = result.getResolvedQuery();
        ChatMessage chatMessage0, chatMessage;
        if (sessionManagement.get("ArtisanPage").equals("City") || sessionManagement.get("ArtisanPage").equals("State")) {

            chatMessage0 = new ChatMessage(message, sessionManagement.get("MyUsername"), sessionManagement.get("City"), "city");
            nChat.child(sessionManagement.get("ArtisanPage")).child(sessionManagement.get(sessionManagement.get("ArtisanPage")).replace(" ", "")).push().setValue(chatMessage0);
            String reply = result.getFulfillment().getSpeech();
            chatMessage = new ChatMessage(reply, "Bot", sessionManagement.get(sessionManagement.get("ArtisanPage")), sessionManagement.get("ArtisanPage").toLowerCase());
            nChat.child(sessionManagement.get("ArtisanPage")).child(sessionManagement.get(sessionManagement.get("ArtisanPage")).replace(" ", "")).push().setValue(chatMessage);
        } else {
            chatMessage0 = new ChatMessage(message, sessionManagement.get("MyUsername"), sessionManagement.get("GpsLat"), sessionManagement.get("GpsLog"), "local");
            nChat.child("Local").push().setValue(chatMessage0);
            String reply = result.getFulfillment().getSpeech();
            chatMessage = new ChatMessage(reply, "Bot", sessionManagement.get("GpsLat"), sessionManagement.get("GpsLog"), "local");
            nChat.child("Local").push().setValue(chatMessage);
        }
    }

    @Override
    public void onError(AIError error) {

    }

    @Override
    public void onAudioLevel(float level) {

    }

    @Override
    public void onListeningStarted() {

    }

    @Override
    public void onListeningCanceled() {

    }

    @Override
    public void onListeningFinished() {

    }

    public class sendNotifyTask extends AsyncTask<Void, Void, Boolean> {
        private String recipient, title, message, exceptUser;

        sendNotifyTask(String nrecipient, String ntitle, String nmessage, String nexceptUser) {
            recipient = nrecipient;
            title = ntitle;
            message = nmessage;
            exceptUser = nexceptUser;

        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody body = new FormBody.Builder()
                        .add("Message", message)
                        .add("Recipient", recipient)
                        .add("Title", title)
                        .add("ExceptUser", exceptUser)
                        .build();
                Request request = new Request.Builder()
                        .url("http://donationearners.net/get_data.php")
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
                Toast.makeText(ArtisanFragmentGroupChat.this, "Success Sent", Toast.LENGTH_SHORT).show();
            } else {

                Toast.makeText(ArtisanFragmentGroupChat.this, "Failed Sent", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;

        }
    }
}