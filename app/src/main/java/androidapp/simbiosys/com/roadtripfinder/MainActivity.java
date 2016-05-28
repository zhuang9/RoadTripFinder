package androidapp.simbiosys.com.roadtripfinder;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity {

    Toolbar toolbar;
    TabLayout tabLayout;
    ViewPager viewPager;
    List<Fragment> fragmentList;
    List<String> titleList;
    RadioGroup radioGroup_type, radioGroup_distance;
    RadioButton restaurant, cafe, gas, hotel;
    RadioButton radioButton500, radioButton1000, radioButton1500, radioButton2000, radioButton5000;
    String searchType, searchRadius;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        viewPager = (ViewPager) findViewById(R.id.viewPager);

        toolbar.inflateMenu(R.menu.tool_bar);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int menuItemId = item.getItemId();
                if (menuItemId == R.id.search) {
                    Intent intent = new Intent(MainActivity.this, AutoCompleteActivity.class);
                    startActivityForResult(intent, 1);
                } else if (menuItemId == R.id.setting) {
                    createMenuDialog();
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

    private void createMenuDialog() {
        //Menu view
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.search_preference, null);

        final SharedPreferences searchSetting = getSharedPreferences("searchSetting", MODE_PRIVATE);
        String Search_Type = searchSetting.getString("type", "restaurant");
        String Search_Radius = searchSetting.getString("radius", "1500");

        radioGroup_type = (RadioGroup) view.findViewById(R.id.search_types);
        radioGroup_distance = (RadioGroup) view.findViewById(R.id.search_distance);
        restaurant = (RadioButton) view.findViewById(R.id.type_restaurant);
        cafe = (RadioButton) view.findViewById(R.id.type_cafe);
        gas = (RadioButton) view.findViewById(R.id.type_gas);
        hotel = (RadioButton) view.findViewById(R.id.type_hotel);
        radioButton500 = (RadioButton) view.findViewById(R.id.distance_500);
        radioButton1000 = (RadioButton) view.findViewById(R.id.distance_1000);
        radioButton1500 = (RadioButton) view.findViewById(R.id.distance_1500);
        radioButton2000 = (RadioButton) view.findViewById(R.id.distance_2000);
        radioButton5000 = (RadioButton) view.findViewById(R.id.distance_5000);

        if (Search_Type == "restaurant") {
            restaurant.setChecked(true);
        } else if (Search_Type == "cafe") {
            cafe.setChecked(true);
        } else if (Search_Type == "gas") {
            gas.setChecked(true);
        } else if (Search_Type == "hotel") {
            hotel.setChecked(true);
        }

        if (Search_Radius == "500") {
            radioButton500.setChecked(true);
        } else if (Search_Radius == "1000") {
            radioButton1000.setChecked(true);
        } else if (Search_Radius == "1500") {
            radioButton1500.setChecked(true);
        } else if (Search_Radius == "2000") {
            radioButton2000.setChecked(true);
        } else if (Search_Radius == "5000") {
            radioButton5000.setChecked(true);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setView(view)
                .setTitle("Setting Google Places search");
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int checkedID0 = radioGroup_type.getCheckedRadioButtonId();
                int checkedID1 = radioGroup_distance.getCheckedRadioButtonId();
                SharedPreferences.Editor editor = searchSetting.edit();

                if (checkedID0 == R.id.type_restaurant) {
                    searchType = "restaurant";
                    editor.putString("type", "restaurant");
                    editor.commit();
                } else if (checkedID0 == R.id.type_cafe) {
                    searchType = "cafe";
                    editor.putString("type", "cafe");
                    editor.commit();
                } else if (checkedID0 == R.id.type_gas) {
                    searchType = "gas";
                    editor.putString("type", "gas_station");
                    editor.commit();
                } else if (checkedID0 == R.id.type_hotel) {
                    searchType = "hotel";
                    editor.putString("type", "hotel");
                    editor.commit();
                }

                if (checkedID1 == R.id.distance_500) {
                    searchRadius = "500";
                    editor.putString("radius", "500");
                    editor.commit();
                } else if (checkedID1 == R.id.distance_1000) {
                    searchRadius = "1000";
                    editor.putString("radius", "1000");
                    editor.commit();
                } else if (checkedID1 == R.id.distance_1500) {
                    searchRadius = "1500";
                    editor.putString("radius", "1500");
                    editor.commit();
                } else if (checkedID1 == R.id.distance_2000) {
                    searchRadius = "2000";
                    editor.putString("radius", "2000");
                    editor.commit();
                } else if (checkedID1 == R.id.distance_5000) {
                    searchRadius = "5000";
                    editor.putString("radius", "5000");
                    editor.commit();
                }
                Toast.makeText(getApplicationContext(), "Your search type is: " + searchType + " and radius is: " + searchRadius, Toast.LENGTH_LONG).show();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.create().show();
    }
}