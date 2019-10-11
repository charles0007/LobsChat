package lobschat.hycode.lobschat;

/**
 * Created by HyCode on 3/12/2017.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class ListAdapter extends BaseAdapter {

    private Activity activity;
    private ArrayList<HashMap<String, String>> data;
    private static LayoutInflater inflater = null;
//    public ImageLoader imageLoader;

    public ListAdapter(Activity a, ArrayList<HashMap<String, String>> d) {
        activity = a;
        data = d;
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

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        if (convertView == null)
            vi = inflater.inflate(R.layout.list_row, null);

        TextView user_name = (TextView) vi.findViewById(R.id.user_name); // title
        TextView user_date = (TextView) vi.findViewById(R.id.user_date); // artist name
        TextView last_mess = (TextView) vi.findViewById(R.id.last_mess); // duration
        TextView read_mess = (TextView) vi.findViewById(R.id.read_mess); // read
        ImageView thumb_image = (ImageView) vi.findViewById(R.id.list_image); // thumb image

        HashMap<String, String> listDriver = new HashMap<String, String>();
        listDriver = data.get(position);
        String passenger = listDriver.get("passenger");
        String passengerUc = passenger.substring(0, 1).toUpperCase() + passenger.substring(1).toLowerCase();
        // Setting all values in listview
        user_name.setText(passengerUc);
        user_date.setText(listDriver.get("time"));
        last_mess.setText(listDriver.get("message"));
        read_mess.setText(listDriver.get("read"));

        return vi;
    }


}
