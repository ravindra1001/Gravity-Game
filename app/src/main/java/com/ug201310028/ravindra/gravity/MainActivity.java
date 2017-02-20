package com.ug201310028.ravindra.gravity;

import android.content.Context;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;


public class MainActivity extends AppCompatActivity{
    private GameView gameView;
    Handler handler;
    String showAd = "0";
    public void fun(){

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // for screen size
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        boolean network = haveNetworkConnection();
        handler = new Handler();

/*        SharedPreferences prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        Integer isFirst = Integer.valueOf(prefs.getString("firstTime","1"));
        if(isFirst==1){
            setContentView(R.layout.activity_intro);
            TextView start_btn = (TextView) findViewById(R.id.start_btn);

            if (start_btn != null) {
                start_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        //                finish();
                    }
                });
            }

            SharedPreferences.Editor editor = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).edit();
            editor.putString("firstTime", "0");
            editor.apply();

//            Intent intent = new Intent(this, IntroActivity.class);
//            startActivity(intent);
        }*/
//        else {
            gameView = new GameView(this, size.x, size.y, network, MainActivity.this, handler);
            setContentView(gameView);
//        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        gameView.pause();
    }
    @Override
    protected void onResume() {
        super.onResume();
        gameView.resume();
/*        Intent intent = new Intent(this,SecondActivity.class);
        startActivityForResult(intent,0);*/
//        startActivity(intent);
    }

    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

}
