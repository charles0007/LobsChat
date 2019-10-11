package lobschat.hycode.lobschat;

import android.app.ProgressDialog;
import android.content.Intent;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Created by HyCode on 5/31/2018.
 */

public class OldImageClicked extends AppCompatActivity {

    String image;
    SessionManagement session;
    StorageReference storageReference;
    ImageView gallery;
    ProgressDialog pd;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_layout);
        pd=new ProgressDialog(this);
        session = new SessionManagement(this);
        image = session.get("ImageClicked");

//        Fresco.initialize(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("  " + session.get("ImageClickedName"));
        // toolbar.setBackground(new ColorDrawable(Color.parseColor("#0000ff")));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
//        gallery = (ImageView) findViewById(R.id.gallery);
        pd.setMessage("Loading...");
        pd.setCancelable(false);
        pd.show();
        imageClicked(image);


    }


    public void imageClicked(String image) {

        if (!image.isEmpty() && image != "" && image != "null" && image != null && !image.contains("null")) {
            try {

                storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(image);
//                GlideApp.with(OldImageClicked.this)
//                        .using(new FirebaseImageLoader())
//                        .load(storageReference)
//                        .into(gallery);
                gallery.setContentDescription(session.get("ImageClickedName"));
                pd.dismiss();
            } catch (Exception er) {
                Toast.makeText(OldImageClicked.this,"Image VIew failed...",Toast.LENGTH_LONG).show();
                finish();
            }
        } else {
            gallery.setImageResource(R.drawable.default_img);
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


//    @GlideModule
//    public class MyGlideModule extends AppGlideModule {
//
//        @Inject
//        @Named(DEFAULT)
//        OkHttpClient client;
//
//        public MyGlideModule() {
//            MyApplication.getNetworkComponent().inject(this);
//        }
//
//        @Override
//        public boolean isManifestParsingEnabled() {
//            return false;
//        }
//
//        @Override
//        public void applyOptions(Context context, GlideBuilder builder) {
//            super.applyOptions(context, builder);
//            builder.setDefaultRequestOptions(
//                    new RequestOptions().format(DecodeFormat.PREFER_ARGB_8888)
//            );
//        }
//
//        @Override
//        public void registerComponents(Context context, Glide glide, Registry registry) {
//            super.registerComponents(context, glide, registry);
//            registry.replace(
//                    GlideUrl.class,
//                    InputStream.class,
//                    new OkHttpProgressUrlLoader.Factory(client)
//            );
//        }
//
//    }


}


