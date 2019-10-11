package lobschat.hycode.lobschat;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by HyCode on 1/6/2018.
 */

public class Register  extends AppCompatActivity {
    Button btnCabreg;
    TextView loginChatreg;
    EditText inputEmail, inputPass, inputRepass, inputUser,inputDesc;
    private FirebaseAuth auth;
//    private View mProgressView;
//    private View mRegFormView;
    private UserRegTask mAuthTask = null;
    boolean boolVal,exist=false;
//    StringRequest request;
    ProgressDialog pd;
    String regexception;
    String ruser;
    DatabaseReference nRootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference dblobschat = nRootRef.child("lobschat");
    DatabaseReference nusers = dblobschat.child("users");
    RadioGroup rdgrp,rdsex;
    String Rdtype,RdSex;
    String remail,rpass,repass,rdesc;
    SessionManagement session;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        session=new SessionManagement(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("  Reistration");
        // toolbar.setBackground(new ColorDrawable(Color.parseColor("#0000ff")));
        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(toolbar);
        // Display icon in the toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        auth = FirebaseAuth.getInstance();

        inputEmail = (EditText) findViewById(R.id.reg_email);
        inputUser = (EditText) findViewById(R.id.reg_user);
        inputPass = (EditText) findViewById(R.id.reg_pass);
        inputDesc = (EditText) findViewById(R.id.reg_des);
        inputRepass = (EditText) findViewById(R.id.reg_repass);
        rdgrp = (RadioGroup) findViewById(R.id.rdgrp);
        rdsex = (RadioGroup) findViewById(R.id.rdsex);



        btnCabreg = (Button) findViewById(R.id.btnCabreg);
        loginChatreg = (TextView) findViewById(R.id.loginCabreg);

        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/ERASMD.TTF");
        inputEmail.setTypeface(typeface);
        inputUser.setTypeface(typeface);
        inputPass.setTypeface(typeface);
        inputDesc.setTypeface(typeface);
        inputRepass.setTypeface(typeface);

        loginChatreg.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                finish();
            }
        } );

        btnCabreg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                RadioButton rdClick=(RadioButton)findViewById(rdgrp.getCheckedRadioButtonId());
             Rdtype=rdClick.getText().toString();
             if(Rdtype.contains("Artisan")){
                 inputDesc.setHint("Business Description for artisan");
             }else{
                 inputDesc.setHint("Short Description");
             }
                RadioButton rdSexClick=(RadioButton)findViewById(rdsex.getCheckedRadioButtonId());
                RdSex=rdClick.getText().toString();



                remail = inputEmail.getText().toString().toLowerCase();
                rpass = inputPass.getText().toString();
                rdesc = inputDesc.getText().toString();
                repass = inputRepass.getText().toString();
                ruser = inputUser.getText().toString().toLowerCase();

                inputEmail.setError(null);
                inputDesc.setError(null);
                inputUser.setError(null);
                inputPass.setError(null);
                inputRepass.setError(null);

                if (TextUtils.isEmpty(remail)) {
                    inputEmail.setError(getString(R.string.error_field_required));
                }
                else if (TextUtils.isEmpty(ruser)) {
                    inputUser.setError(getString(R.string.error_field_required));
                }else if (TextUtils.isEmpty(rdesc) && Rdtype.equals("Artisan")) {
                    inputDesc.setError(getString(R.string.error_field_required)+" for Artisan");
                }
                else if (TextUtils.isEmpty(rpass)) {
                    inputPass.setError(getString(R.string.error_field_required));
                }
                else if (TextUtils.isEmpty(repass)) {
                    inputRepass.setError(getString(R.string.error_field_required));
                }
                else if (rpass.length() < 6) {
                    inputPass.setError(getString(R.string.minimum_password));
                }
                else if (!rpass.equals(repass)) {
                    inputRepass.setError("Password mismerge");
                }
                else {
//                    showProgress(true);
                    pd = new ProgressDialog(Register.this);
                    pd.setMessage("Loading...");
                    pd.show();
                    pd.setCancelable(false);
                    pd.setCanceledOnTouchOutside(false);
                    mAuthTask = new UserRegTask(remail,rpass);
                    mAuthTask.execute((Void) null);


                }

            }
        });
    }


    public class UserRegTask extends AsyncTask<Void, Void, Boolean> {

        private final String remailBg,rpassBg;

        UserRegTask(String emailBg,String passBg) {
            remailBg = emailBg;
            rpassBg=passBg;

        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            try {

                //create user
nusers.addValueEventListener(new ValueEventListener() {
    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        try{

        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

            for (DataSnapshot postSnap : postSnapshot.getChildren()) {

                if(postSnap.getKey().equals("Username")){
                    if(postSnap.getValue().toString().equals(ruser)){
                        exist=true;boolVal=false;
                        regexception="Username already exist,try another username";
                    }
                    else {exist=false;boolVal=true;
                    }
                }
            }
        }
    }catch (Exception e){exist=false;boolVal=true;}
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
exist=false;
    }
});

// Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
//                Toast.makeText(Register.this, "Exception1: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                return false;
            }catch (Exception e){
//                Toast.makeText(Register.this, "Exception2: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                return false;
            }


            // TODO: register the new account here.
            return boolVal;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;

            if (success && !exist) {
                auth.createUserWithEmailAndPassword(remail, rpass)
                        .addOnCompleteListener(Register.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                pd.dismiss();
//                                Toast.makeText(Register.this, "Second", Toast.LENGTH_SHORT).show();
                                if (!task.isSuccessful()) {
                                    regexception=task.getException().getMessage();
                                    if(regexception==null || regexception.equals("") || regexception.isEmpty()){
                                        regexception="Registration failed, try again";
                                    }
                                    if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                        regexception="Registration failed, User with this email already exist.";
                                    }
                                    AlertDialog.Builder builder = new AlertDialog.Builder(Register.this);
                                    builder.setMessage(regexception)
                                            .setCancelable(true)
                                            .setPositiveButton("OK", null);
                                    AlertDialog alert = builder.create();
                                    alert.show();
                                } else {
                                    sendVerificationEmail();
                                }
                            }
                        });
            }
            else{
                pd.dismiss();
                AlertDialog.Builder builder = new AlertDialog.Builder(Register.this);
                builder.setMessage(regexception)
                        .setCancelable(true)
                        .setPositiveButton("OK", null);
                AlertDialog alert = builder.create();
                alert.show();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            pd.dismiss();
        }
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

    private void sendVerificationEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // email sent

                            try {
                               Calendar c=Calendar.getInstance();
                                SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd");
                               String SubDate=dateFormat.format(c.getTime());

                                DatabaseReference newuser = nusers.child(remail
                                        .replace(".","_")
                                        .replace("#","_")
                                        .replace("$","_")
                                        .replace("[","_")
                                        .replace("]","_")
                                        .replace("@","_"));
                                DatabaseReference dbUser = newuser.child("Username");
                                DatabaseReference dbType = newuser.child("Type");
                                DatabaseReference dbSex = newuser.child("Sex");
                                DatabaseReference dbImage = newuser.child("Image");
                                DatabaseReference dbToken = newuser.child("Token");
                                DatabaseReference dbEmail = newuser.child("Email");
                                DatabaseReference dbSubStatus = newuser.child("SubStatus");
                                DatabaseReference dbSubDate= newuser.child("SubDate");
                                DatabaseReference dbActivation= newuser.child("Activation");
                                dbEmail.setValue(remail);
                                dbImage.setValue("null");
                                dbUser.setValue(ruser);
                                dbType.setValue(Rdtype);
                                dbSex.setValue(RdSex);
                                dbSubStatus.setValue("Trial");
                                dbActivation.setValue("Active");
                                dbSubDate.setValue(SubDate);
                                dbToken.setValue(session.get("Token"));
                                if(Rdtype.equals("Artisan")){
                                DatabaseReference dbDesc = newuser.child("Business");
                                dbDesc.setValue(rdesc);
                                    DatabaseReference dbSubDaysLeft = newuser.child("SubDaysLeft");
                                    dbSubDaysLeft.setValue("30");
                                }else{
                                    DatabaseReference dbDesc = newuser.child("Description");
                                    dbDesc.setValue(rdesc);

                                }

                            }catch (Exception exr){}
                            // after email is sent just logout the user and finish this activity
//                            FirebaseAuth.getInstance().signOut();
                            AlertDialog.Builder builder = new AlertDialog.Builder(Register.this);
                            builder.setMessage("Registration successful!, verification email will be sent to you shortly")
                                    .setCancelable(false)
                                    .setPositiveButton("OK",  new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            session.createLoginSession(remail
                                                    .replace(".","_")
                                                    .replace("#","_")
                                                    .replace("$","_")
                                                    .replace("[","_")
                                                    .replace("]","_")
                                                    .replace("@","_"));
                                            session.createLoginSessionMainEmail(remail);
                                            startActivity(new Intent(Register.this, SetAllData.class));
                                            finish();
                                        }
                                    });
                            AlertDialog alert = builder.create();
                            alert.show();
                        }
                        else
                        {
                            // email not sent, so display message and restart the activity or do whatever you wish to do
                            AlertDialog.Builder builder = new AlertDialog.Builder(Register.this);
                            builder.setMessage("verification not sent, try again")
                                    .setCancelable(false)
                                    .setPositiveButton("OK",  new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {

                                        }
                                    });
                            AlertDialog alert = builder.create();
                            alert.show();
                            //restart this activity
                            overridePendingTransition(0, 0);
                            finish();
                            overridePendingTransition(0, 0);
                            startActivity(getIntent());

                        }
                 pd.dismiss();
                    }
                });
    }





}
