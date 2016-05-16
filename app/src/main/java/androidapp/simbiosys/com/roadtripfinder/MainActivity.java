package androidapp.simbiosys.com.roadtripfinder;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity {

    Toolbar toolbar;
    TabLayout tabLayout;
    ViewPager viewPager;
    List<Fragment> fragmentList;
    List<String> titleList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.Toolbar);
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        viewPager = (ViewPager) findViewById(R.id.viewPager);

        toolbar.inflateMenu(R.menu.tool_bar);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int menuItemId = item.getItemId();
                if(menuItemId == R.id.search) {
                    Intent intent = new Intent(MainActivity.this, AutoCompleteActivity.class);
                    startActivityForResult(intent, 1);
                }
                else if(menuItemId == R.id.setting) {

                }
                return true;
            }
        });

        fragmentList = new ArrayList<>();
        fragmentList.add(new DirectionFragment());
        fragmentList.add(new RestaurantFragment());
        fragmentList.add(new AccountFragment());

        titleList = new ArrayList<>();
        titleList.add("Direction");
        titleList.add("Restaurant");
        titleList.add("Account");

        MyPagerAdapter adapter = new MyPagerAdapter(getSupportFragmentManager(), fragmentList, titleList);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);
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