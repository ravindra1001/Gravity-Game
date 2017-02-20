package com.ug201310028.ravindra.gravity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

public class SecondActivity extends AppCompatActivity {

    private String TAG = SecondActivity.class.getSimpleName();
    InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        mInterstitialAd = new InterstitialAd(this);

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
/*                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);*/
                finish();
//                onBackPressed();
//                onBackPressed();
            }

            @Override
            public void onAdLeftApplication() {
                super.onAdLeftApplication();
                onBackPressed();
            }
        });

        // set the ad unit ID
        mInterstitialAd.setAdUnitId(getString(R.string.full_screen_ad));

        AdRequest adRequest = new AdRequest.Builder()
                .build();

        // Load ads into Interstitial Ads
        mInterstitialAd.loadAd(adRequest);

        mInterstitialAd.setAdListener(new AdListener() {
            public void onAdLoaded() {
                showInterstitial();
            }
        });
    }

    private void showInterstitial() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
    }

}