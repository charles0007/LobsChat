package lobschat.hycode.lobschat;

        import android.app.Activity;
        import android.graphics.Typeface;
        import android.support.constraint.ConstraintLayout;
        import android.support.v7.widget.CardView;
        import android.support.v7.widget.RecyclerView;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ImageView;
        import android.widget.TextView;
        import android.widget.Toast;

        import com.google.firebase.storage.FirebaseStorage;
        import com.google.firebase.storage.StorageReference;

        import java.util.ArrayList;
        import java.util.HashMap;

        import de.hdodenhof.circleimageview.CircleImageView;

public class UsersInfoRecyclerAdapter extends RecyclerView.Adapter<UsersInfoRecyclerAdapter.MyViewHolder> {

    //    private ArrayList<DataModel> dataSet;
    private Activity activity;
    private ArrayList<HashMap<String, String>> data;
    private static LayoutInflater inflater = null;
    StorageReference storageReference;
    SessionManagement session;


    public static class MyViewHolder extends RecyclerView.ViewHolder {


        TextView product_desc;
        TextView rating;
        TextView category;
        TextView status;
        TextView key;
        TextView img ;
        ImageView imageView;
        public MyViewHolder(View itemView) {
            super(itemView);

            View vi=itemView;
            product_desc = (TextView) vi.findViewById(R.id.product_desc); // title
            rating = (TextView) vi.findViewById(R.id.rating); // title
            category = (TextView) vi.findViewById(R.id.category); // title
            status = (TextView) vi.findViewById(R.id.status); // artist name
            img = (TextView) vi.findViewById(R.id.img_txt);
            key = (TextView) vi.findViewById(R.id.key);
            imageView = (ImageView) vi.findViewById(R.id.imageviewpager);



        }
    }

    public UsersInfoRecyclerAdapter(Activity a, ArrayList<HashMap<String, String>> d) {

        activity = a;
        data = d;

        session = new SessionManagement(a);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent,
                                           int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.users_info, parent, false);

        view.setOnClickListener(ArtisanList.myOnClickListener);



        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int listPosition) {

        TextView product_desc=holder.product_desc;
        TextView rating=holder.rating;
        TextView category=holder.category;
        TextView status=holder.status;
        TextView key=holder.key;
        TextView img=holder.img;
        ImageView imageView=holder.imageView;

        Typeface typeface = Typeface.createFromAsset(activity.getAssets(), "fonts/ERASMD.TTF");
        product_desc.setTypeface(typeface);
        rating.setTypeface(typeface);
        category.setTypeface(typeface);
        status.setTypeface(typeface);

        HashMap<String, String> listDriver = new HashMap<String, String>();
        listDriver = data.get(listPosition);
        final String Desc = listDriver.get("Description");
        String Rating = listDriver.get("Rating");
        final String Image = listDriver.get("Image");
        final String Category = listDriver.get("Category");
        final String Status = listDriver.get("Status");
        final String Key = listDriver.get("Key");

        try {
            storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(Image);

            GlideApp.with(activity)
                    .load(storageReference)
                    .placeholder(R.drawable.default_img)
                    .error(R.drawable.default_img)
                    .circleCrop()
                    .into(imageView);
        } catch (Exception er) {
            GlideApp.with(activity)
                    .load(R.drawable.default_img)
                    .circleCrop()
                    .into(imageView);
        }
//
//            }else{
//                ImgShowDefault(sex);
//            }
//        }else{
//          ImgShowDefault(sex);
//        }
        final String DescUc = Desc.substring(0, 1).toUpperCase() + Desc.substring(1).toLowerCase();
        // Setting all values in listview
        product_desc.setText(DescUc);
        key.setText(Key);
        status.setText(Status);
        category.setText(Category);
        rating.setText(Rating);
       img.setText(Image);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Image == null || Image.isEmpty() || Image == "" || Image == "null" || Image.contains("null")) {
                    Toast.makeText(activity,"Image not available",Toast.LENGTH_LONG).show();
                }else {
                    session.set("ImageClicked", Image);
                    session.set("ImageClickedName", DescUc);
                    new ImageClicked(activity, v, DescUc, Image, null, "default");
                }


            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }


}
