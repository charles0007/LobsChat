package lobschat.hycode.lobschat;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import static lobschat.hycode.lobschat.Settings.userDetails;

public class SettingEmail extends DialogFragment {
    private Button changeEmail;
    private ProgressBar progressBar;
    private EditText newEmail;
    Settings settings;
    private FirebaseAuth auth;
    ProgressDialog pd;

    public static SettingEmail New() {
        return new SettingEmail();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        settings = new Settings();
        View view = inflater.inflate(R.layout.setting_email, null);

        changeEmail = (Button) view.findViewById(R.id.changeEmail);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        newEmail = (EditText) view.findViewById(R.id.new_email);
        auth = FirebaseAuth.getInstance();
        pd = new ProgressDialog(getActivity());
        changeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pd.setMessage("Applying changes...");
                pd.show();
                pd.setCanceledOnTouchOutside(false);
                pd.setCancelable(false);
                if (userDetails != null && !newEmail.getText().toString().trim().equals("")) {
                    userDetails.updateEmail(newEmail.getText().toString().trim())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                        builder.setMessage("Email address is updated. Please sign in with new email id!")
                                                .setCancelable(false)
                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        signOut();
                                                    }
                                                });
                                        AlertDialog alert = builder.create();
                                        alert.show();

                                        pd.dismiss();
                                    } else {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                        builder.setMessage("Failed to update email!")
                                                .setCancelable(false)
                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        pd.dismiss();
                                                    }
                                                });
                                        AlertDialog alert = builder.create();
                                        alert.show();

                                    }
                                }
                            });
                } else if (newEmail.getText().toString().trim().equals("")) {
                    newEmail.setError("Enter email");
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
        new SessionManagement(getActivity()).logoutUser();
    }
}
