package com.example.myapplication.Adapter;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.example.myapplication.Fragments.DisplayReadings;
import com.example.myapplication.Fragments.DisplayReadings2;
import com.example.myapplication.Fragments.Kalmanfilter;
import com.example.myapplication.Fragments.MotionFragment;
import com.example.myapplication.StepDetector.StepCounter2;

public class SectionsPagerAdapter extends FragmentStatePagerAdapter {



    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
            //Log.d(TAG, "Constructor for adapter");
    }


    @Override
    public Fragment getItem(int position) {
        //Log.d(TAG, "Get item: " + position);

        switch (position) {

            case 0:
                return MotionFragment.newInstance(position);
            case 1:
               return DisplayReadings.newInstance(position);
            case 2:
                return DisplayReadings2.newInstance(position);
            case 3:
                return Kalmanfilter.newInstance(position);
            case 4:
                return StepCounter2.newInstance(position);

            default:
                return null;

        }


    }


    @Override
    public int getCount() {
        return 5;
    }

}
