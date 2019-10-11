package lobschat.hycode.lobschat;

/**
 * Created by HyCode on 3/12/2017.
 */

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
//import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import de.hdodenhof.circleimageview.CircleImageView;


public class ArtisanListAdapter extends BaseAdapter {

    private Activity activity;
    private ArrayList<HashMap<String, String>> data;
    private static LayoutInflater inflater = null;
    StorageReference storageReference;
    CircleImageView thumb_image;
    ImageView gallery;
    RelativeLayout mainLayout;
    SessionManagement session;
//    public ImageLoader imageLoader;

    public ArtisanListAdapter(Activity a, ArrayList<HashMap<String, String>> d) {
        activity = a;
        data = d;
        session = new SessionManagement(a);
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

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
        Typeface typeface = Typeface.createFromAsset(activity.getAssets(), "fonts/ERASMD.TTF");
        TextView user_name = (TextView) vi.findViewById(R.id.user_name); // title
        TextView chat_id = (TextView) vi.findViewById(R.id.chat_id); // title
        TextView otherUser = (TextView) vi.findViewById(R.id.otherUser); // title
        TextView user_date = (TextView) vi.findViewById(R.id.user_date); // artist name
        TextView last_mess = (TextView) vi.findViewById(R.id.last_mess); // duration
        TextView read_mess = (TextView) vi.findViewById(R.id.read_mess); // read
        user_date.setTypeface(typeface);
        chat_id.setTypeface(typeface);
        otherUser.setTypeface(typeface);
        user_name.setTypeface(typeface);
        last_mess.setTypeface(typeface);
        read_mess.setTypeface(typeface);
        thumb_image = (CircleImageView) vi.findViewById(R.id.list_image); // thumb image
        mainLayout = (RelativeLayout) vi.findViewById(R.id.mainLayout);
        TextView email = (TextView)vi.findViewById(R.id.email_txt);
        email.setTypeface(typeface);
        HashMap<String, String> listDriver = new HashMap<String, String>();
        listDriver = data.get(position);
        final String username = listDriver.get("Username");
        String chatId = listDriver.get("chatId");
        final String image = listDriver.get("Image");


        if (!image.isEmpty() && image != "" && image != "null" && image != null && !image.contains("null")) {
            try {
                storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(image);
//                GlideApp.with(activity)
//                        .using(new FirebaseImageLoader())
//                        .load(storageReference)
//                        .into(thumb_image);
            } catch (Exception er) {
            }


        }
        String usernameUc = username.substring(0, 1).toUpperCase() + username.substring(1).toLowerCase();
        // Setting all values in listview
        user_name.setText(usernameUc);
        chat_id.setText(chatId);
        email.setText(listDriver.get("Email"));
        last_mess.setText(listDriver.get("Business"));
        read_mess.setText(listDriver.get("read"));
        thumb_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                session.set("ImageClicked", image);
                session.set("ImageClickedName", username);
//                activity.startActivity(new Intent(activity, ImageClicked.class));

            }
        });
        return vi;
    }


}
