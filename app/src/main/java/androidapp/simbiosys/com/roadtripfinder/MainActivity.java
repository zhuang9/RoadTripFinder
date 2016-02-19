package androidapp.simbiosys.com.roadtripfinder;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity {

    TabLayout tabLayout;
    ViewPager viewPager;
    List<Fragment> fragmentList;
    List<String> titleList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        viewPager = (ViewPager) findViewById(R.id.viewPager);

        fragmentList = new ArrayList<>();
        fragmentList.add(new DirectionFragment());
        fragmentList.add(new RestaurantFragment());
        fragmentList.add(new ShareFragment());
        fragmentList.add(new AccountFragment());

        titleList = new ArrayList<>();
        titleList.add("Direction");
        titleList.add("Restaurant");
        titleList.add("Account");
        titleList.add("Share");

        MyPagerAdapter adapter = new MyPagerAdapter(getSupportFragmentManager(), fragmentList, titleList);
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    public class MyPagerAdapter extends FragmentStatePagerAdapter {

        List<Fragment> fragmentList;
        List<String> titleList;

        public MyPagerAdapter(FragmentManager fm, List<Fragment> fragmentList, List<String> titleList) {
            super(fm);
            this.fragmentList = fragmentList;
            this.titleList = titleList;
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return titleList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titleList.get(position);
        }
    }
}