package lobschat.hycode.lobschat;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;



public class SettingPassword extends DialogFragment {
    private Button changePassword;

    private EditText newPassword,oldPassword,confirmPassword;
    Settings settings;
    ProgressDialog pd;
    private FirebaseAuth auth;
    private UserLoginTask mAuthTask = null;
    public static SettingPassword New() {
        return new SettingPassword();
    }
SessionManagement sessionManagement;
    String rlogin_email="";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        settings = new Settings();

        View view = inflater.inflate(R.layout.setting_password, null);
        changePassword = (Button) view.findViewById(R.id.changePass);
        sessionManagement=new SessionManagement(getActivity());
        oldPassword = (EditText) view.findViewById(R.id.oldPassword);
        newPassword = (EditText) view.findViewById(R.id.newPassword);
        confirmPassword = (EditText) view.findViewById(R.id.confirmPassword);
        rlogin_email= sessionManagement.get("Email");
        auth = FirebaseAuth.getInstance();

        pd = new ProgressDialog(getActivity());

        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                if (!newPassword.getText().toString().trim().equals("")) {
                    if(TextUtils.isEmpty(oldPassword.getText().toString())){
                        oldPassword.setError("This field is required");
                    }else if(TextUtils.isEmpty(newPassword.getText().toString())){
                        newPassword.setError("This field is required");
                    }else if(TextUtils.isEmpty(confirmPassword.getText().toString())){
                        confirmPassword.setError("This field is required");
                    }else if(!newPassword.getText().toString().equals(confirmPassword.getText().toString())){
                        newPassword.setError("Password Mismerge");
                        confirmPassword.setError("Password Mismerge");
                    }else {
                        if (newPassword.getText().toString().trim().length() < 6) {
                            newPassword.setError("Password too short, enter minimum 6 characters");
//                        progressBar.setVisibility(View.GONE);
//                        newPassword.setVisibility(View.VISIBLE);
//                        changePassword.setVisibility(View.VISIBLE);
                            pd.dismiss();
                        } else {
                            pd.setMessage("Applying changes...");
                            pd.show();
                            pd.setCanceledOnTouchOutside(false);
                            pd.setCancelable(false);
                            mAuthTask = new UserLoginTask(rlogin_email, oldPassword.getText().toString());
                            mAuthTask.execute((Void) null);

                        }
                    }
                } else if (newPassword.getText().toString().trim().equals("")) {
                    newPassword.setError("Enter password");
                    pd.dismiss();
                }
            }
        });

        return view;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {


        super.onDismiss(dialog);
    }

    //sign out method
    public void signOut() {

        auth.signOut();
        getActivity().finish();
        new SessionManagement(getActivity()).logoutUser();
    }


    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
        public boolean boolVal;
        private final String remailBg, rpassBg;

        UserLoginTask(String emailBg, String passBg) {
            remailBg = emailBg;
            rpassBg = passBg;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {

                auth.signInWithEmailAndPassword(remailBg, rpassBg)
                        .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                //progressBar.setVisibility(View.GONE);
                                auth = FirebaseAuth.getInstance();
                                if (!task.isSuccessful()) {
                                    // there was an error

                                    boolVal = false;
                                } else {
                                    boolVal = true;

                                }
                            }
                        });
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Toast.makeText(getActivity(), "Auth: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return false;
            }

            // TODO: register the new account here.
            return boolVal;
        }


        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;

            if (success) {
                if (auth.getCurrentUser() != null) {
                    if (auth.getCurrentUser().getEmail().equalsIgnoreCase(rlogin_email)) {

                        auth.getCurrentUser().updatePassword(newPassword.getText().toString().trim())
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                            builder.setMessage("Password is updated, sign in with new password!")
                                                    .setCancelable(false)
                                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int id) {
                                                            pd.dismiss();
                                                            signOut();
                                                        }
                                                    });
                                            AlertDialog alert = builder.create();
                                            alert.show();
                                        } else {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                            builder.setMessage("Failed to update password!")
                                                    .setCancelable(false)
                                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int id) {
                                                            pd.dismiss();
                                                        }
                                                    });
                                            AlertDialog alert = builder.create();
                                            alert.show();
                                            //  Toast.makeText(Profile.this, "Failed to update password!", Toast.LENGTH_SHORT).show();
                                            //  progressBar.setVisibility(View.GONE);
                                        }
                                    }
                                });



                    } else {
                        noNetworkAuth();
                    }
                } else {
                    noNetworkAuth();
                }
            } else {
                if (isNetworkAvailable()) {
                    notAuth();
                } else {
                    networkError();
                }

            }

        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            pd.dismiss();
        }



        void notAuth() {
            pd.dismiss();
            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity());
            builder.setTitle("LobsChat");
            builder.setMessage("Password Incorrect, try again")
                    .setCancelable(true)
                    .setPositiveButton("OK", null);
            android.support.v7.app.AlertDialog alert = builder.create();
            alert.show();
        }

        void noNetworkAuth() {
            mAuthTask = new UserLoginTask(rlogin_email, oldPassword.getText().toString());
            mAuthTask.execute((Void) null);
//

        }

        void networkError() {
            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity());
            builder.setMessage("Network connection error")
                    .setCancelable(true)
                    .setPositiveButton("OK", null);
            android.support.v7.app.AlertDialog alert = builder.create();
            alert.show();
        }

        private boolean isNetworkAvailable() {
            ConnectivityManager connectivityManager
                    = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }



    }


}
