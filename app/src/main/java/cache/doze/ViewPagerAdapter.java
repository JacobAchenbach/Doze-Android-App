package cache.doze;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.util.SparseArray;

import cache.doze.Fragments.Page1;
import cache.doze.Fragments.Page2;

/**
 * Created by Chris on 2/22/2018.
 */

public class ViewPagerAdapter extends FragmentStatePagerAdapter {
    Context context;
    TabLayout tabLayout;

    private static int numItems = 2;
    private SparseArray<Fragment> fragments = new SparseArray<>();

    public ViewPagerAdapter(Context context, FragmentManager fm, TabLayout tabLayout) {
        super(fm);
        this.context = context;
        this.tabLayout = tabLayout;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment frag;
        switch (position) {
            case 0:
                frag = Page1.newInstance(0, "Page2");
                break;
            case 1:
                frag = Page2.newInstance(1, "Page2");
                break;
            default:
                frag = null;
                break;
        }
        fragments.append(position, frag);
        return frag;
    }

    public Fragment getFragment(int key){
        return fragments.get(key);
    }

    @Override
    public int getCount() {
        return ViewPagerAdapter.numItems;
    }

    // Returns the page title for the top indicator
    @Override
    public CharSequence getPageTitle(int position) {
        String title;
        Drawable drawable;
        switch (position) {
            case 0:
                title = "Home";
                drawable = ContextCompat.getDrawable(context, R.drawable.baseline_home_black_24);
                break;
            case 1:
                title = "Contacts";
                drawable = ContextCompat.getDrawable(context, R.drawable.baseline_perm_contact_calendar_black_24);
                break;
            default:
                title = "Page " +position;
                drawable = ContextCompat.getDrawable(context, R.drawable.baseline_home_black_24);
        }
        return "";
    }

}