package lobschat.hycode.lobschat;

        import android.app.ProgressDialog;
        import android.content.Context;
        import android.content.Intent;

        import android.graphics.Bitmap;
        import android.graphics.Color;
        import android.graphics.drawable.BitmapDrawable;
        import android.os.Build;
        import android.os.Bundle;
        import android.support.annotation.Nullable;
        import android.support.v7.app.AppCompatActivity;
        import android.support.v7.graphics.Palette;
        import android.support.v7.widget.Toolbar;
        import android.view.Gravity;
        import android.view.LayoutInflater;
        import android.view.MenuItem;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ImageButton;
        import android.widget.ImageView;
        import android.widget.PopupWindow;
        import android.widget.ProgressBar;

        import com.github.chrisbanes.photoview.PhotoView;
        import com.google.firebase.storage.FirebaseStorage;
        import com.google.firebase.storage.StorageReference;


        import javax.sql.DataSource;

        import static android.content.Context.LAYOUT_INFLATER_SERVICE;

/**
 * Created by HyCode on 5/31/2018.
 */

        public class ViewImageFile extends PopupWindow {


        View view;
        Context mContext;
        PhotoView photoView;
        ProgressBar loading;
        ViewGroup parent;
        private static ViewImageFile instance = null;



        public ViewImageFile(Context ctx, View v,String username, String imageUrl, Bitmap bitmap) {
        super(((LayoutInflater) ctx.getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.image_layout, null), ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT);

        if (Build.VERSION.SDK_INT >= 21) {
        setElevation(5.0f);
        }
        this.mContext = ctx;
        this.view = getContentView();
        ImageButton closeButton = (ImageButton) this.view.findViewById(R.id.ib_close);
        setOutsideTouchable(true);

        setFocusable(true);
        // Set a click listener for the popup window close button
        closeButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
        // Dismiss the popup window
        dismiss();
        }
        });

        photoView = (PhotoView) view.findViewById(R.id.image);
        loading = (ProgressBar) view.findViewById(R.id.loading);
        photoView.setMaximumScale(6);
        parent = (ViewGroup) photoView.getParent();
        // ImageUtils.setZoomable(imageView);
        //----------------------------
        if (bitmap != null) {
        loading.setVisibility(View.GONE);
        if (Build.VERSION.SDK_INT >= 16) {
        parent.setBackground(new BitmapDrawable(mContext.getResources(), Constants.fastblur(Bitmap.createScaledBitmap(bitmap, 50, 50, true))));// ));
        } else {
        onPalette(Palette.from(bitmap).generate());

        }
        photoView.setImageBitmap(bitmap);
        } else {
        loading.setIndeterminate(true);
        loading.setVisibility(View.VISIBLE);


//        GlideApp.with(ctx).asBitmap()
//        .load(imageUrl)
//
//        .error(R.drawable.default_img)
//        .listener(new RequestListener<Bitmap>() {
//        @Override
//        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
//        loading.setIndeterminate(false);
//        loading.setBackgroundColor(Color.LTGRAY);
//        return false;
//        }
//
//        @Override
//        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
//        if (Build.VERSION.SDK_INT >= 16) {
//        parent.setBackground(new BitmapDrawable(mContext.getResources(), Constants.fastblur(Bitmap.createScaledBitmap(resource, 50, 50, true))));// ));
//        } else {
//        onPalette(Palette.from(resource).generate());
//
//        }
//        photoView.setImageBitmap(resource);
//
//        loading.setVisibility(View.GONE);
//        return false;
//        }
//        })
//
//
//        .diskCacheStrategy(DiskCacheStrategy.ALL)
//        .into(photoView);G

        showAtLocation(v, Gravity.CENTER, 0, 0);
        }
        //------------------------------

        }

        public void onPalette(Palette palette) {
        if (null != palette) {
        ViewGroup parent = (ViewGroup) photoView.getParent().getParent();
        parent.setBackgroundColor(palette.getDarkVibrantColor(Color.GRAY));
        }
        }

        }

		
		
		
//
//
//
//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.image_layout);
//        pd=new ProgressDialog(this);
//        session = new SessionManagement(this);
//        image = session.get("ImageClicked");
//
////        Fresco.initialize(this);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        toolbar.setTitle("  " + session.get("ImageClickedName"));
//        // toolbar.setBackground(new ColorDrawable(Color.parseColor("#0000ff")));
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setHomeButtonEnabled(true);
////        gallery = (ImageView) findViewById(R.id.gallery);
//        pd.setMessage("Loading...");
//        pd.setCancelable(false);
//        pd.show();
//        imageClicked(image);
//
//
//    }
//
//
//    public void imageClicked(String image) {
//
//        if (!image.isEmpty() && image != "" && image != "null" && image != null && !image.contains("null")) {
//            try {
//
//                storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(image);
//                Glide.with(ImageClicked.this)
//                        .using(new FirebaseImageLoader())
//                        .load(storageReference)
//                        .into(gallery);
//                gallery.setContentDescription(session.get("ImageClickedName"));
//                pd.dismiss();
//            } catch (Exception er) {
//                Toast.makeText(ImageClicked.this,"Image VIew failed...",Toast.LENGTH_LONG).show();
//                finish();
//            }
//        } else {
//            gallery.setImageResource(R.drawable.default_img);
//            pd.dismiss();
//        }
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case android.R.id.home:
//                finish();
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//
//
//    }
//}
