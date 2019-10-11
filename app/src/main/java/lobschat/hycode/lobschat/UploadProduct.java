package lobschat.hycode.lobschat;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class UploadProduct extends AppCompatActivity {
    DatabaseReference nRootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference dblobschat = nRootRef.child("lobschat");
    DatabaseReference nusers = dblobschat.child("users");
    DatabaseReference nProduct = dblobschat.child("products");
    EditText txttitle, txtdescription, txtcategory;
    ImageView choose_img, view_img;
    Button btn_post;
    String title, description, category;
    SessionManagement sessionManagement;
    StorageReference storageReference;
    private final int PICK_IMAGE_REQUEST = 171;
    private Uri filePath;
    TextView img_txt,txtInsertId;
    String insertId;
    String UserInfoName,UserInfoId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManagement = new SessionManagement(this);
        setContentView(R.layout.upload_products);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        UserInfoName= sessionManagement.get("MyUsername");
        UserInfoId=  sessionManagement.getUserDetails().get("User");
        toolbar.setTitle("  " + UserInfoName);
        // toolbar.setBackground(new ColorDrawable(Color.parseColor("#0000ff")));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        txttitle = (EditText) findViewById(R.id.title);
        txtdescription = (EditText) findViewById(R.id.edit_description);
        txtcategory = (EditText) findViewById(R.id.edit_category);
        choose_img = (ImageView) findViewById(R.id.insert_img);
        view_img = (ImageView) findViewById(R.id.view_image);
        btn_post = (Button) findViewById(R.id.btn_post);
        img_txt = (TextView) findViewById(R.id.img_txt);
        txtInsertId = (TextView) findViewById(R.id.txtInsertId);

        final String empty = "This field is required";

        btn_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                title = txttitle.getText().toString();
                description = txtdescription.getText().toString();
                category = txtcategory.getText().toString();
                if (TextUtils.isEmpty(title)) {
                    txttitle.setError(empty);
                } else if (TextUtils.isEmpty(description)) {
                    txtdescription.setError(empty);
                } else if (TextUtils.isEmpty(category)) {
                    txtcategory.setError(empty);
                }else if(TextUtils.isEmpty(img_txt.getText().toString())){
                    AlertDialog.Builder alertDialog=new AlertDialog.Builder(UploadProduct.this);
                    alertDialog.setMessage("Upload an Image");
                    alertDialog.setPositiveButton("Ok",null);
                    alertDialog.create().show();
                } else {
                    HashMap<String, String> mapList = new HashMap();
                   String image=img_txt.getText().toString();
                    mapList.put("Description", description);
                    mapList.put("Rating", "");
                    mapList.put("Category", category);
                    mapList.put("Status", "Open");
                    mapList.put("Image", image);
                    insertId=txtInsertId.getText().toString();
                    nProduct.child(sessionManagement.getUserDetails().get("User")).child(insertId).setValue(mapList);
                    img_txt.setText("");
                    txtInsertId.setText("");
                    AlertDialog.Builder alertDialog=new AlertDialog.Builder(UploadProduct.this);
                    alertDialog.setMessage(title+" uploaded successfully");
                    alertDialog.setPositiveButton("Add Product", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                            startActivity((new Intent(UploadProduct.this,UploadProduct.class)));
                        }
                    });
                    alertDialog.setNegativeButton("Finish", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
                    alertDialog.create().show();
                }

            }
        });

        choose_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                startActivity(new Intent(Settings.this, Profile.class));
                chooseImage();

            }
        });

    }


    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            filePath = data.getData();

            uploadImage();

        }
    }


    private void uploadImage() {

        if (filePath != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            storageReference = FirebaseStorage.getInstance().getReference().child(sessionManagement.getUserDetails().get("User")).child("product_images/" + UUID.randomUUID().toString());

            storageReference.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            String name = taskSnapshot.getMetadata().getName();
                            String url = taskSnapshot.getDownloadUrl().toString();
//                            nUsers.child("Image").setValue(url);

                            insertId= nProduct.child(sessionManagement.getUserDetails().get("User")).push().getKey();
                            txtInsertId.setText(insertId);
                            nProduct.child(sessionManagement.getUserDetails().get("User")).child(insertId).child("Image").setValue(url);
                            img_txt.setText(url);
                            try {
                                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                                view_img.setImageBitmap(bitmap);

                                Toast.makeText(UploadProduct.this, "Uploaded", Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                Toast.makeText(UploadProduct.this, "Failed :" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(UploadProduct.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded " + (int) progress + "%");
                        }
                    });
        }
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
