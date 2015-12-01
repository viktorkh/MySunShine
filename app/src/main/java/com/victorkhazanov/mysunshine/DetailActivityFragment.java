package com.victorkhazanov.mysunshine;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {

    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";

    private ShareActionProvider mShareActionProvider;
    private String mForecast;
    private Uri mUri;



    public DetailActivityFragment() {

        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.detailfragment,menu);

        MenuItem menuItem =menu.findItem(R.id.action_share);

        ShareActionProvider mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);


        if(mShareActionProvider != null){

            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
        else
            LogUtil.e("ShareActionProvider is null !!!");

        //super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView =inflater.inflate(R.layout.fragment_detail, container, false);


        Intent intent =getActivity().getIntent();

        if(intent != null && intent.hasExtra(Intent.EXTRA_TEXT)){

            mForecast = intent.getStringExtra(Intent.EXTRA_TEXT);


            ((TextView)  rootView.findViewById(R.id.detail_text)).setText(mForecast);
        }




        return rootView;
    }

    private Intent createShareForecastIntent(){

        Intent shareIntent =new Intent(Intent.ACTION_SEND);

        shareIntent.addFlags(Intent. FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");

        shareIntent.putExtra(
            Intent.EXTRA_TEXT,mForecast+FORECAST_SHARE_HASHTAG
        );

        return  shareIntent;
    }
}
