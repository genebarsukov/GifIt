package codewrencher.gifit.ui.help;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.Locale;

/**
 * Created by Gene on 1/2/2016.
 */
public class HelpFragmentPager extends FragmentPagerAdapter {
    private int page_count;

    public HelpFragmentPager(FragmentManager fm, int page_count) {
        super(fm);
        this.page_count = page_count;
    }

    @Override
    public Fragment getItem(int position) {
        return HelpFragment.newInstance(position);
    }

    @Override
    public int getCount() {
        return this.page_count;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Locale l = Locale.getDefault();
        switch (position) {
            case 0:
                return "help1";
            case 1:
                return "help2";
            case 2:
                return "help3";
        }
        return null;
    }
}
