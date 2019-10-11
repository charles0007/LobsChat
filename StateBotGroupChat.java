package lobschat.hycode.lobschat;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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


public class StateFragmentGroupChat extends AppCompatActivity implements AIListener{
    LinearLayout layout;
    Firebase reference1;

    DatabaseReference nRootRef;
    DatabaseReference dblobschat;
    DatabaseReference nChat;

    ProgressDialog pgd;
    boolean start;
    String userName,message;
    SessionManagement sessionManagement;

    sendNotifyTask mAuthTask;

    //new
    RecyclerView recyclerView;
    EditText editText;
    RelativeLayout addBtn;
    DatabaseReference ref,ref1,ref2;
    FirebaseRecyclerAdapter<ChatMessage,chat_rec> adapter;
    Boolean flagFab = true;

    private AIService aiService;
    //end new

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        start=true;
        sessionManagement=new SessionManagement(this);
        setContentView(R.layout.chat_activity);


        nRootRef = FirebaseDatabase.getInstance().getReference();
        dblobschat = nRootRef.child("lobschat");
        nChat = dblobschat.child("group-chat");


        pgd = new ProgressDialog(StateFragmentGroupChat.this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(sessionManagement.get("State")+" Chat");
        // toolbar.setBackground(new ColorDrawable(Color.parseColor("#0000ff")));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
///new

//        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.RECORD_AUDIO},1);


        recyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        editText = (EditText)findViewById(R.id.editText);
        addBtn = (RelativeLayout)findViewById(R.id.addBtn);

        recyclerView.setHasFixedSize(true);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        //end new

        nRootRef.keepSynced(true);
        Firebase.setAndroidContext(this);


        reference1 = new Firebase("https://lobschat.firebaseio.com/lobschat/group-chat/State/"+sessionManagement.get("City").replace(" ",""));

        final AIConfiguration config = new AIConfiguration("0c01e159babc4349b38eca698bd2f107",
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
                        ChatMessage chatMessage = new ChatMessage(messageText,sessionManagement.get("MyUsername"),sessionManagement.get("State"),"state");

                        String exceptUser=sessionManagement.getUserDetails().get("User");
                        mAuthTask = new sendNotifyTask(sessionManagement.get("State"),sessionManagement.get("State")+" Chat",messageText,exceptUser);
                        mAuthTask.execute((Void) null);

                        reference1.push().setValue(chatMessage);
                       aiRequest.setQuery(messageText);
                       new AsyncTask<AIRequest,Void,AIResponse>(){

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
                                   ChatMessage chatMessage = new ChatMessage(reply,"Bot",sessionManagement.get("State"),"state");
//                                    ChatMessage chatMessage = new ChatMessage(reply, "bot");
                                   nChat.child("State").child(sessionManagement.get("State").replace(" ","")).push().setValue(chatMessage);
                               }
                           }
                       }.execute(aiRequest);

                    }
                }catch (Exception exr) {
                    Toast.makeText(StateFragmentGroupChat.this, "Error occured, message not sent try again", Toast.LENGTH_LONG);
                }  editText.setText("");

            }
        });


        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                ImageView fab_img = (ImageView)findViewById(R.id.fab_img);
//                Bitmap img = BitmapFactory.decodeResource(getResources(),R.drawable.ic_send_white_24dp);
////                Bitmap img1 = BitmapFactory.decodeResource(getResources(),R.drawable.ic_mic_white_24dp);
//
//
////                if (s.toString().trim().length()!=0 && flagFab){
//                ImageViewAnimatedChange(StateFragmentGroupChat.this,fab_img,img);
////                    flagFab=false;
//
////                }
//                if (s.toString().trim().length()==0){
////                    ImageViewAnimatedChange(CityFragmentGroupChat.this,fab_img,img1);
////                    flagFab=true;
////                    user typing
////
//                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        adapter = new FirebaseRecyclerAdapter<ChatMessage, chat_rec>(ChatMessage.class,R.layout.msglist,chat_rec.class,nChat.child("State").child(sessionManagement.get("City").replace(" ",""))) {
            @Override
            protected void populateViewHolder(chat_rec viewHolder, ChatMessage model, int position) {
                if (model.getMsgState() != null && !model.getMsgState().isEmpty()) {
                    if (model.getMsgState().equalsIgnoreCase(sessionManagement.get("State"))) {
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
        final Animation anim_in  = AnimationUtils.loadAnimation(c, R.anim.zoom_in);
        anim_out.setAnimationListener(new Animation.AnimationListener()
        {
            @Override public void onAnimationStart(Animation animation) {}
            @Override public void onAnimationRepeat(Animation animation) {}
            @Override public void onAnimationEnd(Animation animation)
            {
                v.setImageBitmap(new_image);
                anim_in.setAnimationListener(new Animation.AnimationListener() {
                    @Override public void onAnimationStart(Animation animation) {}
                    @Override public void onAnimationRepeat(Animation animation) {}
                    @Override public void onAnimationEnd(Animation animation) {}
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
        ChatMessage chatMessage0 = new ChatMessage(message,sessionManagement.get("MyUsername"),sessionManagement.get("State"),"state");
        nChat.child("State").child(sessionManagement.get("City").replace(" ","")).push().setValue(chatMessage0);


        String reply = result.getFulfillment().getSpeech();
        ChatMessage chatMessage = new ChatMessage(reply,"Bot",sessionManagement.get("State"),"state");
        nChat.child("State").child(sessionManagement.get("City").replace(" ","")).push().setValue(chatMessage);

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
        private String recipient,title, message,exceptUser;

        sendNotifyTask(String nrecipient,String ntitle,String nmessage,String nexceptUser){
            recipient=nrecipient;
            title=ntitle;
            message=nmessage;
            exceptUser=nexceptUser;

        }
        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody body = new FormBody.Builder()
                        .add("Message",message)
                        .add("Recipient",recipient)
                        .add("Title",title)
                        .add("ExceptUser",exceptUser)
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
            }catch (Exception er){
                return false;
            }

        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;

            if (success) {
                Toast.makeText(StateFragmentGroupChat.this, "Success Sent", Toast.LENGTH_SHORT).show();
            } else {

                Toast.makeText(StateFragmentGroupChat.this, "Failed Sent", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled () {
            mAuthTask = null;

        }
    }
}