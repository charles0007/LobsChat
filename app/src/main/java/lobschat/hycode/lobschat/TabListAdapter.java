package lobschat.hycode.lobschat;

/**
 * Created by HyCode on 3/12/2017.
 */

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

//import com.bumptech.glide.Glide;
import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import de.hdodenhof.circleimageview.CircleImageView;


public class TabListAdapter extends BaseAdapter {

    private Activity activity;
    private ArrayList<HashMap<String, String>> data;
    private static LayoutInflater inflater = null;
    StorageReference storageReference;
    CircleImageView thumb_image;
    ImageView gallery;
    ConstraintLayout mainLayout;
//    RelativeLayout mainLayout;
    SessionManagement session;
    TextView user_name;
    TextView chat_id;
    TextView otherUser;
    TextView user_date;
    TextView last_mess;
    TextView read_mess;
    TextView img ;
    TextView email,txtSex,txtToken ;
//    public ImageLoader imageLoader;

    public TabListAdapter(Activity a, ArrayList<HashMap<String, String>> d) {
        activity = a;
        data = d;

        session = new SessionManagement(a);
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        imageLoader=new ImageLoader(activity.getApplicationContext());
    }

    public int getCount() {
        return data.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, final View convertView, ViewGroup parent) {
        View vi = convertView;
        if (convertView == null)
            vi = inflater.inflate(R.layout.chat_list_row, null);


         user_name = (TextView) vi.findViewById(R.id.user_name); // title
         chat_id = (TextView) vi.findViewById(R.id.chat_id); // title
         otherUser = (TextView) vi.findViewById(R.id.otherUser); // title
         user_date = (TextView) vi.findViewById(R.id.user_date); // artist name
         last_mess = (TextView) vi.findViewById(R.id.last_mess); // duration
         read_mess = (TextView) vi.findViewById(R.id.read_mess); // read
        txtSex = (TextView) vi.findViewById(R.id.sex); // Sex
        txtToken = (TextView) vi.findViewById(R.id.token); // Token
        thumb_image = (CircleImageView) vi.findViewById(R.id.list_image); // thumb image
        mainLayout = (ConstraintLayout) vi.findViewById(R.id.mainLayout);
//                mainLayout = (RelativeLayout) vi.findViewById(R.id.mainLayout);
         img = (TextView) vi.findViewById(R.id.img_txt);
         email = (TextView) vi.findViewById(R.id.email_txt);
        Typeface typeface = Typeface.createFromAsset(activity.getAssets(), "fonts/ERASMD.TTF");
        user_name.setTypeface(typeface);
        chat_id.setTypeface(typeface);
        otherUser.setTypeface(typeface);
        user_date.setTypeface(typeface);
        last_mess.setTypeface(typeface);
        read_mess.setTypeface(typeface);
        txtSex.setTypeface(typeface);
        img.setTypeface(typeface);
        email.setTypeface(typeface);
        HashMap<String, String> listDriver = new HashMap<String, String>();
        listDriver = data.get(position);
        final String username = listDriver.get("Username");
        String chatId = listDriver.get("chatId");
        final String image = listDriver.get("Image");
        final String sex = listDriver.get("Sex");

//        ImgShowDefault(sex);

//        if (image != null) {
//            if (!image.isEmpty() && image != "" && image != "null" && !image.contains("null")) {
                try {
                    storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(image);
//.using(new FirebaseImageLoader())
                    GlideApp.with(activity)
                            .load(storageReference)
                            .placeholder(ImgShowDefault(sex))
                            .error(ImgShowDefault(sex))
                            .circleCrop()
                            .into(thumb_image);
                } catch (Exception er) {
                    GlideApp.with(activity)
                            .load(ImgShowDefault(sex))
                            .circleCrop()
                            .into(thumb_image);
                }
//
//            }else{
//                ImgShowDefault(sex);
//            }
//        }else{
//          ImgShowDefault(sex);
//        }
        String usernameUc = username.substring(0, 1).toUpperCase() + username.substring(1).toLowerCase();
        // Setting all values in listview
        user_name.setText(usernameUc);
        chat_id.setText(chatId);
        otherUser.setText(listDriver.get("otherUser"));
        user_date.setText(listDriver.get("date"));
        last_mess.setText(listDriver.get("message"));
        read_mess.setText(listDriver.get("read"));
        img.setText(listDriver.get("Image"));
        email.setText(listDriver.get("Email"));
        txtToken.setText(listDriver.get("UserToken"));
        txtSex.setText(sex);
        thumb_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (image == null || image.isEmpty() || image == "" || image == "null" || image.contains("null")) {
                    Toast.makeText(activity,"Image not available",Toast.LENGTH_LONG).show();
                }else {
                    session.set("ImageClicked", image);
                    session.set("ImageClickedName", username);
                    new ImageClicked(activity, v, username, image, null, sex);
                }
//                activity.startActivity(new Intent(activity, ImageClicked.class));

            }
        });



        return vi;
    }

    public int ImgShowDefault(String sex){
        int s;
        if(sex.equalsIgnoreCase("Female")){
            s=R.drawable.userff;
//            Glide.with(activity).clear(thumb_image);
//            thumb_image.setImageResource(R.drawable.userff);

        }else{
            s=R.drawable.usermm;
//            Glide.with(activity).clear(thumb_image);
//            thumb_image.setImageResource(R.drawable.userff);

        }
        return s;
    }

}
