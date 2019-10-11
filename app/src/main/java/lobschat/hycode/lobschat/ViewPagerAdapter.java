package lobschat.hycode.lobschat;

/**
 * Created by HyCode on 12/22/2017.
 */


import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

class ViewPagerAdapter extends FragmentPagerAdapter {
FragmentManager fragmentManager;
SessionManagement sessionManagement;
//    private String title[] = {"Location", "City","State","Router", "Business"};
    private String title[] = {"Location", "City","State"};
    public ViewPagerAdapter(FragmentManager manager, Context _context) {
        super(manager);
        fragmentManager=manager;
        sessionManagement=new SessionManagement(_context);
        title[0] ="NearBy ";// {"Location", "City","State"};
        title[1] =sessionManagement.get("City")+" City";
        title[2] =sessionManagement.get("State")+" State";
    }
    @Override
    public Fragment getItem(int index) {
        switch (index) {
            case 0:
                return new LocalTabFragment();
                // Location fragment activity
//                try {
////                    fragmentManager.beginTransaction()
////                            .replace(R.id.tabs, new LocalTabFragment())
////                            .addToBackStack(null)
////                            .commit();
//                    return new LocalTabFragment();
//                }catch (Exception ex){
//                    return new LocalTabFragment();
//                }
            case 1:
                // City fragment activity
                return new CityTabFragment();
            case 2:

                    return new StateTabFragment();
        }

        return null;
    }

    @Override
    public int getCount() {
        // get item count - equal to number of tabs
        return 3;
    }
    @Override
    public CharSequence getPageTitle(int position) {
        return title[position];
    }


}