package lobschat.hycode.lobschat;

import android.content.Context;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class Intro extends AppCompatActivity {
    ViewPager viewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intro);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        InnerImageAdapter adapter = new InnerImageAdapter(this);
        viewPager.setAdapter(adapter);
    }

    private class InnerImageAdapter extends PagerAdapter {
        Context context;
        private int[] GalImages = new int[]{R.drawable.logo_512,R.drawable.usermm, R.drawable.userm_avatar, R.drawable.logo_512};

        InnerImageAdapter(Context _con) {
            this.context = _con;
        }

        @Override

        public int getCount() {

            return GalImages.length;

        }


        @Override

        public boolean isViewFromObject(View view, Object object) {

            return view == ((ImageView) object);

        }


        @Override

        public Object instantiateItem(ViewGroup container, int position) {

            ImageView imageView = new ImageView(context);
            int pad = context.getResources().getDimensionPixelOffset(R.dimen.into_img);
//            imageView.setPadding(pad, pad, pad, pad);
            imageView.setMaxHeight(pad);
            imageView.setMaxWidth(pad);
            imageView.setImageResource(GalImages[position]);
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            ((ViewPager) container).addView(imageView, 0);

            return imageView;

        }


        @Override

        public void destroyItem(ViewGroup container, int position, Object object) {

            ((ViewPager) container).removeView((ImageView) object);

        }
    }

}
