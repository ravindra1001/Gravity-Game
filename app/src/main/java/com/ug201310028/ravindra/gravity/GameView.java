package com.ug201310028.ravindra.gravity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.util.ArrayList;
import java.util.Random;

public class GameView extends SurfaceView implements Runnable {
    public int width;
    public int height;
    public int POWER_HEIGHT = 400;
    public int LEVEL_HEIGHT = 100;
    public Context context;
    public int first_time = 1;

    InterstitialAd mInterstitialAd;


    public int flag = 0;
    public boolean network;

    public int launch_start = 0;
    public int max_power = 25;
    public int min_power = 1;
    public int power = 0;
    public double power_direction = 1;
    public float launch_angle = 0;
    public int level = 0;
    public int launched = 0;
    public int isFirst;

    ArrayList<Planet> planets = new ArrayList<Planet>();
    Planet home_planet = new Planet();
    Planet goal_planet = new Planet();
    Player player = new Player();

    volatile boolean playing;
    private Thread gameThread = null;
    private Paint paint;
    private Canvas canvas;
    private SurfaceHolder surfaceHolder;


    public void showAd(){
        mInterstitialAd = new InterstitialAd(context);
        mInterstitialAd.setAdUnitId(context.getString(R.string.full_screen_ad));
        AdRequest adRequest = new AdRequest.Builder().build();
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
    Activity activity;
    Handler handler;

    public GameView(Context context, int screenX, int screenY,boolean network,Activity activity, Handler handler) {
        super(context);
        this.context = context;
        this.activity = activity;
        this.handler = handler;
        SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        isFirst = Integer.valueOf(prefs.getString("firstTime","1"));
        if(isFirst==1){
            SharedPreferences.Editor editor = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).edit();
            editor.putString("firstTime", "0");
            editor.apply();

/*            Intent intent = new Intent(context, IntroActivity.class);
            context.startActivity(intent);*/
        }
        level = Integer.valueOf(prefs.getString("level", "0"));
        this.network = network;
        mInterstitialAd = new InterstitialAd(context);

        if(level==0){
            SharedPreferences.Editor editor = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).edit();
            editor.putString("level", "0");
            editor.apply();
        }


        width = screenX;
        height = screenY;
        surfaceHolder = getHolder();
        paint = new Paint();
        new_game(4,0);
    }


    private void new_game(int num_planets, int difficulty){
        if(level<8){
            new_game1(level);
            return;
        }

        planets.clear();
//        launch_start = 0;
//        player.x = -20;
//        player.y = -20;

        //home planet
        home_planet.radius = new Random().nextInt(20)+40;
        home_planet.x = new Random().nextInt(800 - 2 * (int)home_planet.radius)+home_planet.radius;
        home_planet.y = new Random().nextInt(600 - 2 * (int)home_planet.radius)+ home_planet.radius;
        home_planet.gravity = home_planet.radius/2;
        home_planet.density = home_planet.gravity/home_planet.radius;



        //goal planet
        goal_planet.radius = new Random().nextInt(20)+40;
        goal_planet.x = new Random().nextInt(800 - 2 * (int)goal_planet.radius) + goal_planet.radius;
        goal_planet.y = new Random().nextInt(600 - 2 * (int)goal_planet.radius) + goal_planet.radius;

        while(Math.sqrt(Math.pow(goal_planet.x-home_planet.x,2) + Math.pow(goal_planet.y-home_planet.y,2)) < home_planet.radius+goal_planet.radius + 200){
//            goal_planet.radius = new Random().nextInt(40)+80;
            goal_planet.x = new Random().nextInt(800 - 2 * (int)goal_planet.radius) + goal_planet.radius;
            goal_planet.y = new Random().nextInt(600 - 2 * (int)goal_planet.radius) + goal_planet.radius;
        }
        goal_planet.gravity = (goal_planet.radius * (new Random().nextInt(5)+10))/10;
        goal_planet.density = goal_planet.gravity/goal_planet.radius;



        //other planets
        for(int i=0;i<num_planets;i++){
            Planet planet = new Planet();
            int low = 40 + difficulty;
            int high = 60 + difficulty;
            planet.radius = new Random().nextInt(high-low)+low;

            low = (int) planet.radius;
            high = (int) (800-planet.radius);
            planet.x = new Random().nextInt(high-low) + low;

            low = (int) planet.radius;
            high = (int) (600 - planet.radius);
            planet.y = new Random().nextInt(high-low) + low;

            while(true) {
                boolean touching = false;
                if (Math.sqrt(Math.pow(home_planet.x - planet.x, 2) + Math.pow(home_planet.y - planet.y, 2)) < home_planet.radius + planet.radius) {
                    touching = true;
                }

                if (Math.sqrt(Math.pow(goal_planet.x - planet.x, 2) + Math.pow(goal_planet.y - planet.y, 2)) < goal_planet.radius + planet.radius) {
                    touching = true;
                }

                if (planets.size() > 0) {
                    for (Planet p : planets) {
                        if (Math.sqrt(Math.pow(p.x - planet.x, 2) + Math.pow(p.y - planet.y, 2)) < p.radius + planet.radius) {
                            touching = true;
                        }
                    }
                }
                if(touching==false)break;
                else{
                    low = 40 + difficulty;
                    high = 60 + difficulty;
                    planet.radius = new Random().nextInt(high-low)+low;

                    low = (int) planet.radius;
                    high = (int) (800-planet.radius);
                    planet.x = new Random().nextInt(high-low) + low;

                    low = (int) planet.radius;
                    high = (int) (600 - planet.radius);
                    planet.y = new Random().nextInt(high-low) + low;
                }
            }

            low = 8;
            high = 12 + difficulty/10;
            planet.gravity = (planet.radius * (new Random().nextInt(high-low)+low))/10;
            planet.density = planet.gravity/planet.radius;

            planets.add(planet);
        }

        home_planet.x = home_planet.x/(float)800 * width;
        home_planet.y = home_planet.y/(float)600 * height;
        home_planet.radius = home_planet.radius/(float)700 * (width+height)/2;

        goal_planet.x = goal_planet.x/(float)800 * width;
        goal_planet.y = goal_planet.y/(float)600 * height;
        goal_planet.radius = goal_planet.radius/(float)700 * (width+height)/2;

        for(Planet planet : planets){
            planet.x = planet.x/(float)800 * width;
            planet.y = planet.y/(float)600 * height;
            planet.radius = planet.radius/(float)700 * (width+height)/2;
        }
        min_power = 1;
        max_power = 30;
    }


    @Override
    public void run() {
        while (playing) {
            if(launched==0){
                update1();
            }
            else {
                final int action = update();
                if(action < 0){
                    power = min_power;
                    player.x_power = 0;
                    player.y_power = 0;
                }
                else if(action > 0){
                    level++;
                    final Context context = getContext(); // from MySurfaceView/Activity
                    if (network) {
//                        Intent intent = new Intent(context, SecondActivity.class);
//                        context.startActivity(intent);
//                        showAd1(context);
//                        showAd();
                        activity.runOnUiThread(new Runnable() {
                            public void run() {
                                showAd();
                            }
                        });

                    }
                    new_game(5, 0);
                    /*power = min_power;
                    player.x_power = 0;
                    player.y_power = 0;
                    player.x =home_planet.x + home_planet.radius;
                    player.y = home_planet.y;*/
                }
            }
            draw();
            control();
        }
        try {
            Thread.sleep(100);
        } catch (InterruptedException exception) {
            exception.printStackTrace();
        }
    }

    public void showAd1(final Context context){
/*        final InterstitialAd interstitial = new InterstitialAd(context);
        interstitial.setAdUnitId(context.getString(R.string.full_screen_ad));
        AdRequest adRequest = new AdRequest.Builder().build();
        interstitial.loadAd(adRequest);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                handler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        // process incoming messages here
                        msg.obj();
                    }
                };

                handler.post(new Runnable() { // This thread runs in the UI
                    @Override
                    public void run() {
                        if (interstitial != null && interstitial.isLoaded()) {
                            interstitial.show();
                            interstitial.loadAd(new AdRequest.Builder().build());
                        }
                    }
                });
                Looper.loop();
            }
        };
        new Thread(runnable).start();*/


/*
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (interstitial != null && interstitial.isLoaded()) {
                    interstitial.show();
                    interstitial.loadAd(new AdRequest.Builder().build());
                }
            }
        });*/

        /*PropertyChangeListener listener= new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent event) {
                if(event.getPropertyName()=="level_end")
                {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (interstitial != null && interstitial.isLoaded()) {
                                interstitial.show();
                                interstitial.loadAd(new AdRequest.Builder().build());
                            }
                        }
                    });
                }
            }
        };*/


    }

    private void draw_intro(){
        if(surfaceHolder.getSurface().isValid()){
            canvas = surfaceHolder.lockCanvas();
            canvas.drawColor(Color.WHITE);
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.FILL);

            Bitmap mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.home_pic);
            Bitmap mBitmap1 = Bitmap.createScaledBitmap(mBitmap,width/2,height/2,true);
//            mBitmap.setWidth(width/2);
//            mBitmap.setHeight(height/2);

            canvas.drawBitmap(mBitmap1,width/4, 50, paint);

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(5);
            canvas.drawRect(width / 4, 50, width / 4 + width / 2, height / 2 + 50, paint);

            paint.setStyle(Paint.Style.FILL);



            paint.setTextSize(40);
            paint.setTextAlign(Paint.Align.CENTER);
            String intro = "Launch your spaceship towards its destination(Green Planet)";
            canvas.drawText(intro, 0, intro.length(), width / 2, height / 2 + 80 + 50, paint);

            intro = "but keep in mind the gravity of red planets";
            canvas.drawText(intro, 0,intro.length(),width/2,height/2+120+50,paint);

            intro = "PLAY NOW";
            paint.setTextSize(80);
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            paint.setColor(Color.RED);
            canvas.drawText(intro, 0,intro.length(),width/2,height/2+220+50,paint);

            surfaceHolder.unlockCanvasAndPost(canvas);
        }


    }

    private void draw() {
        if(isFirst==1){
            draw_intro();
            return;
        }
        if(surfaceHolder.getSurface().isValid()){
            canvas = surfaceHolder.lockCanvas();

            canvas.drawColor(Color.WHITE);

            paint.setColor(Color.CYAN);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(home_planet.x, home_planet.y, home_planet.radius, paint);
            paint.setColor(Color.BLACK);
            paint.setTextSize(40);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(home_planet.density + "", 0, 3, home_planet.x, home_planet.y, paint);


            paint.setColor(Color.GREEN);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(goal_planet.x, goal_planet.y, goal_planet.radius, paint);
            paint.setTextSize(40);
            paint.setColor(Color.BLACK);
            canvas.drawText(goal_planet.density + "", 0, 3, goal_planet.x, goal_planet.y, paint);

            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.FILL);
            for(int i = 0;i<planets.size();i++){
                Planet p = planets.get(i);
                paint.setColor(Color.RED);
                canvas.drawCircle(p.x,p.y,p.radius,paint);
                paint.setTextSize(40);
                paint.setColor(Color.BLACK);
                canvas.drawText(p.density + "", 0, 3, p.x, p.y, paint);
            }

            //drawing player
            paint.setColor(Color.BLACK);
            paint.setStrokeWidth(20);
//            canvas.drawLine(player.x, player.y, player.x - player.x_power + 10, player.y - player.y_power + 10, paint);
            canvas.drawCircle(player.x, player.y, 10, paint);

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(5);
            canvas.drawRect(width - 100, 0, width, POWER_HEIGHT, paint);

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.GREEN);
            canvas.drawRect(width - 100, POWER_HEIGHT - power / (float) max_power * POWER_HEIGHT, width, POWER_HEIGHT, paint);

            paint.setTextSize(30);
            paint.setColor(Color.BLACK);
            canvas.drawText("power", 0, 5, width - 60, POWER_HEIGHT + 50, paint);
            paint.setTextSize(40);
            canvas.drawText((int) ((power / (float) max_power) * 100) + " %   ", 0, 5, width - 40, POWER_HEIGHT + 100, paint);

            paint.setTextSize(40);
            canvas.drawText("level", 0, 5, width - 60, height - (LEVEL_HEIGHT+10), paint);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawRect(width - 100, height - LEVEL_HEIGHT, width, height, paint);
            paint.setStrokeWidth(5);
            canvas.drawText(level + 1 +  "   ", 0, 3, width - 60, height - (LEVEL_HEIGHT-60), paint);

            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    // done with launch function
    private void launch(float x, float y,  int click, float angle){
       // if(player==null){
            if(launch_start==0 && click > 0){
                launch_start = 1;
                power = 8;
            }
            else if(launch_start > 0 && click>0){
                power += power_direction;
                if (power >= max_power){
                    power_direction = (int) - Math.sqrt(Math.pow(power_direction , 2));
                }
                if (power <= min_power){
                    power_direction = (int) Math.sqrt(Math.pow(power_direction , 2));
                }
            }
            else if(launch_start > 0 && click == 0){
//                player = new Player();

                player.x = x;
                player.y = y;
                player.x_power = (float) (Math.cos(angle)*power);
                player.y_power = (float) (Math.sin(angle)*power);

                launch_start = 0;
            }
       // }
    }

    private void launch1(){
        player.x_power = (float) (Math.cos(launch_angle)*power);
        player.y_power = (float) (Math.sin(launch_angle)*power);
    }

    private int update1(){
        update_power();
        player.x = (float) (Math.cos(launch_angle)*home_planet.radius + home_planet.x);
        player.y = (float) (Math.sin(launch_angle)*home_planet.radius + home_planet.y);
        return 0;
    }


    private int update(){
        if(player != null){
            for(Planet i:planets){
                float distance = (float) Math.abs((Math.sqrt(Math.pow((i.x - player.x), 2) + Math.pow((i.y - player.y),2))-i.density));
                float angle = (float) Math.atan((i.y-player.y)/(i.x-player.x)+0.0000001);

                if(i.x < player.x){
                    angle += Math.toRadians(180);
                }
                float grav_effect = (float) (i.density * 10 * Math.pow((i.radius / (i.radius + distance)), 2));
/*                player.x_power += Math.cos(angle)*grav_effect/10;
                player.y_power += Math.sin(angle)*grav_effect/10;*/
                player.x_power += Math.cos(angle)*grav_effect;
                player.y_power += Math.sin(angle)*grav_effect;
            }

            // for home planet
            float distance = (float) Math.abs(Math.sqrt(Math.pow((home_planet.x - player.x), 2) + Math.pow((home_planet.y - player.y), 2) - home_planet.density));
            float angle = (float) Math.atan((home_planet.y - player.y) / (home_planet.x - player.x + 0.0000001));

            if(home_planet.x < player.x){
                angle += Math.toRadians(180);
            }
            float grav_effect = (float) (home_planet.density * 10 * Math.pow((home_planet.radius / (home_planet.radius + distance+1)), 2));
            player.x_power += Math.cos(angle)*grav_effect/10;
            player.y_power += Math.sin(angle)*grav_effect/10;
           /* player.x_power += Math.cos(angle)*grav_effect;
            player.y_power += Math.sin(angle)*grav_effect;*/



            // for goal planet
            float distance1 = (float) Math.abs((Math.sqrt(Math.pow((goal_planet.x - player.x), 2) + Math.pow((goal_planet.y - player.y),2))-goal_planet.density));
            float angle1 = (float) Math.atan((goal_planet.y-player.y)/(goal_planet.x-player.x + 0.0000001));

            if(goal_planet.x < player.x){
                angle1 += Math.toRadians(180);
            }
            float grav_effect1 = (float) (goal_planet.density * 10 * Math.pow((goal_planet.radius / (goal_planet.radius + distance1+1)), 2));
            player.x_power += Math.cos(angle1)*grav_effect1/10;
            player.y_power += Math.sin(angle1)*grav_effect1/10;
/*            player.x_power += Math.cos(angle1)*grav_effect1;
            player.y_power += Math.sin(angle1)*grav_effect1;*/


/*            player.x += player.x_power/10;
            player.y += player.y_power/10;*/
            player.x += player.x_power;
            player.y += player.y_power;


            for (Planet i : planets){
                if (Math.sqrt(Math.pow((i.x-player.x),2) + Math.pow((i.y-player.y),2)) < i.radius){
                    launched = 0;
                    return -1;
                }
            }

            if (Math.sqrt(Math.pow((home_planet.x-player.x),2) + Math.pow((home_planet.y-player.y),2)) < home_planet.radius){
                launched = 0;
                return -1;
            }

            if (Math.sqrt(Math.pow((goal_planet.x-player.x),2) + Math.pow((goal_planet.y-player.y),2)) < goal_planet.radius){
                launched = 0;
                return 1;
            }

            if(player.x < 0 || player.x > width || player.y < 0 || player.y > height){
                launched = 0;
                return -1;
            }
        }
        return 0;
    }

    private void control() {
        try {
            gameThread.sleep(17);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void pause() {
        playing = false;
        SharedPreferences.Editor editor = getContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).edit();
        editor.putString("level", String.valueOf(level));
        editor.putString("first_time", "0");
        editor.apply();
        try {
            gameThread.join();
        } catch (InterruptedException e) {
        }
    }
    public void resume() {
        playing = true;
        gameThread = new Thread(this);
        gameThread.start();
    }


    void update_power(){
        if(flag == 1){
            power += power_direction;
            if (power >= max_power){
                power_direction = - Math.sqrt(Math.pow(power_direction , 2));
            }
            if (power <= min_power) {
                power_direction =  Math.sqrt(Math.pow(power_direction, 2));
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if(launched == 0) {
            float x = motionEvent.getX();
            float y = motionEvent.getY();

            float f_angle = (float) Math.atan((y - home_planet.y) / (x - home_planet.x + 0.0000001));
            if (x < home_planet.x) {
                f_angle += Math.toRadians(180);
            }
            launch_angle = f_angle;

            switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_UP:
                    if(isFirst==0){
                        flag = 0;
                        launch_start = 1;
                        launched = 1;
                        launch1();
                    }
                    break;
                case MotionEvent.ACTION_DOWN:
                    if(isFirst==1){
                        isFirst = 0;
                    }
                    else {
                        flag = 1;
                    }
                    break;
            }
        }
        return true;
    }


    public void new_game1(int level){
        planets.clear();
        switch(level){
            case 0:
                min_power = 1;
                max_power = 30;

                //home planet
                home_planet.x = 100/(float)800 * width;
                home_planet.y = 300/(float)600 * height;
                home_planet.radius = 40/(float)700 * (width+height)/2;
                home_planet.gravity = 40*300;
                home_planet.density = (float) 0.5;

                goal_planet.x = 700/(float)800 * width;
                goal_planet.y = 300/(float)600 * height;
                goal_planet.radius = 40/(float)700 * (width+height)/2;
                goal_planet.gravity = 8*300;
                goal_planet.density = (float) 0.1;

                Planet p = new Planet();
                p.x = 200/(float)800 * width;
                p.y = 100/(float)600 * height;
                p.radius = 50/(float)700 * (width+height)/2;
                p.gravity = 100*300;
                p.density = 1;
                planets.add(p);

                p = new Planet();
                p.x = 400/(float)800 * width;
                p.y = 100/(float)600 * height;
                p.radius = 50/(float)700 * (width+height)/2;
                p.gravity = 100*300;
                p.density = 1;
                planets.add(p);

                p = new Planet();
                p.x = 600/(float)800 * width;
                p.y = 100/(float)600 * height;
                p.radius = 50/(float)700 * (width+height)/2;
                p.gravity = 100*1000;
                p.density = 1;
                planets.add(p);
                break;
            case 1:
                min_power = 1;
                max_power = 30;

                //home planet
                home_planet.x = 100/(float)800 * width;
                home_planet.y = 300/(float)600 * height;
                home_planet.radius = 40/(float)700 * (width+height)/2;
                home_planet.gravity = 40*1000;
                home_planet.density = (float) 0.5;

                goal_planet.x = 700/(float)800 * width;
                goal_planet.y = 300/(float)600 * height;
                goal_planet.radius = 40/(float)700 * (width+height)/2;
                goal_planet.gravity = 8*1000;
                goal_planet.density = (float) 0.1;

                p = new Planet();
                p.x = 400/(float)800 * width;
                p.y = 100/(float)600 * height;
                p.radius = 50/(float)700 * (width+height)/2;
                p.gravity = 100*1000;
                p.density = 1;
                planets.add(p);

                p = new Planet();
                p.x = 400/(float)800 * width;
                p.y = 300/(float)600 * height;
                p.radius = 50/(float)700 * (width+height)/2;
                p.gravity = 100*1000;
                p.density = 1;
                planets.add(p);

                p = new Planet();
                p.x = 400/(float)800 * width;
                p.y = 500/(float)600 * height;
                p.radius = 50/(float)700 * (width+height)/2;
                p.gravity = 100*1000;
                p.density = 1;
                planets.add(p);
                break;

            case 2:
                min_power = 1;
                max_power = 30;

                //home planet
                home_planet.x = 100/(float)800 * width;
                home_planet.y = 300/(float)600 * height;
                home_planet.radius = 40/(float)700 * (width+height)/2;
                home_planet.gravity = 40;
                home_planet.density = (float) 0.5;

                goal_planet.x = 700/(float)800 * width;
                goal_planet.y = 300/(float)600 * height;
                goal_planet.radius = 40/(float)700 * (width+height)/2;
                goal_planet.gravity = 40;
                goal_planet.density = (float) 0.5;

                p = new Planet();
                p.x = 400/(float)800 * width;
                p.y = 100/(float)600 * height;
                p.radius = 50/(float)700 * (width+height)/2;
                p.gravity = 100;
                p.density = 1;
                planets.add(p);

                p = new Planet();
                p.x = 400/(float)800 * width;
                p.y = 550/(float)600 * height;
                p.radius = 75/(float)700 * (width+height)/2;
                p.gravity = 350;
                p.density = 7/3;
                planets.add(p);
                break;

            case 3:
                min_power = 1;
                max_power = 30;

                //home planet
                home_planet.x = 100/(float)800 * width;
                home_planet.y = 300/(float)600 * height;
                home_planet.radius = 40/(float)700 * (width+height)/2;
                home_planet.gravity = 40;
                home_planet.density = (float) 0.5;

                goal_planet.x = 700/(float)800 * width;
                goal_planet.y = 300/(float)600 * height;
                goal_planet.radius = 40/(float)700 * (width+height)/2;
                goal_planet.gravity = 40;
                goal_planet.density = (float) 0.5;

                p = new Planet();
                p.x = 250/(float)800 * width;
                p.y = 225/(float)600 * height;
                p.radius = 40/(float)700 * (width+height)/2;
                p.gravity = 80;
                p.density = 1;
                planets.add(p);

                p = new Planet();
                p.x = 350/(float)800 * width;
                p.y = 375/(float)600 * height;
                p.radius = 40/(float)700 * (width+height)/2;
                p.gravity = 80;
                p.density = 1;
                planets.add(p);

                p = new Planet();
                p.x = 450/(float)800 * width;
                p.y = 225/(float)600 * height;
                p.radius = 40/(float)700 * (width+height)/2;
                p.gravity = 80;
                p.density = 1;
                planets.add(p);

                p = new Planet();
                p.x = 550/(float)800 * width;
                p.y = 375/(float)600 * height;
                p.radius = 40/(float)700 * (width+height)/2;
                p.gravity = 80;
                p.density = 1;
                planets.add(p);
                break;

            case 4:
                min_power = 1;
                max_power = 30;

                //home planet
                home_planet.x = 100/(float)800 * width;
                home_planet.y = 150/(float)600 * height;
                home_planet.radius = 40/(float)700 * (width+height)/2;
                home_planet.gravity = 40;
                home_planet.density = (float) 0.5;

                goal_planet.x = 700/(float)800 * width;
                goal_planet.y = 300/(float)600 * height;
                goal_planet.radius = 40/(float)700 * (width+height)/2;
                goal_planet.gravity = 80;
                goal_planet.density = (float) 6;

                p = new Planet();
                p.x = 300/(float)800 * width;
                p.y = 550/(float)600 * height;
                p.radius = 50/(float)700 * (width+height)/2;
                p.gravity = 200;
                p.density = 1;
                planets.add(p);

                p = new Planet();
                p.x = 410/(float)800 * width;
                p.y = 550/(float)600 * height;
                p.radius = 50/(float)700 * (width+height)/2;
                p.gravity = 200;
                p.density = 1;
                planets.add(p);

                p = new Planet();
                p.x = 520/(float)800 * width;
                p.y = 550/(float)600 * height;
                p.radius = 50/(float)700 * (width+height)/2;
                p.gravity = 200;
                p.density = 1;
                planets.add(p);

  /*              p = new Planet();
                p.x = 630/(float)800 * width;
                p.y = 550/(float)600 * height;
                p.radius = 50/(float)700 * (width+height)/2;
                p.gravity = 200;
                p.density = 1;
                planets.add(p);*/

                p = new Planet();
                p.x = 210/(float)800 * width;
                p.y = 165/(float)600 * height;
                p.radius = 50/(float)700 * (width+height)/2;
                p.gravity = 100;
                p.density = 1;
                planets.add(p);

                p = new Planet();
                p.x = 350/(float)800 * width;
                p.y = 80/(float)600 * height;
                p.radius = 50/(float)700 * (width+height)/2;
                p.gravity = 100;
                p.density = 1;
                planets.add(p);
                break;


            case 5:
                min_power = 1;
                max_power = 30;

                //home planet
                home_planet.x = 100/(float)800 * width;
                home_planet.y = 100/(float)600 * height;
                home_planet.radius = 40/(float)700 * (width+height)/2;
                home_planet.gravity = 40;
                home_planet.density = (float) 0.5;

                goal_planet.x = 700/(float)800 * width;
                goal_planet.y = 500/(float)600 * height;
                goal_planet.radius = 50/(float)700 * (width+height)/2;
                goal_planet.gravity = 150;
                goal_planet.density = (float) 1.5;

                p = new Planet();
                p.x = 245/(float)800 * width;
                p.y = 200/(float)600 * height;
                p.radius = 20/(float)700 * (width+height)/2;
                p.gravity = 60;
                p.density = (float) 1.5;
                planets.add(p);

                p = new Planet();
                p.x = 175/(float)800 * width;
                p.y = 200/(float)600 * height;
                p.radius = 10/(float)700 * (width+height)/2;
                p.gravity = 100;
                p.density = 5;
                planets.add(p);

                break;


            case 6:
                min_power = 1;
                max_power = 30;

                //home planet
                home_planet.x = 688/(float)800 * width;
                home_planet.y = 288/(float)600 * height;
                home_planet.radius = 45/(float)700 * (width+height)/2;
                home_planet.gravity = 44;
                home_planet.density = (float) 0.5;

                goal_planet.x = 74/(float)800 * width;
                goal_planet.y = 203/(float)600 * height;
                goal_planet.radius = 57/(float)700 * (width+height)/2;
                goal_planet.gravity = 150;
                goal_planet.density = (float) 1.3;

                p = new Planet();
                p.x = 117/(float)800 * width;
                p.y = 398/(float)600 * height;
                p.radius = 55/(float)700 * (width+height)/2;
                p.gravity = 140;
                p.density = (float) 1.2;
                planets.add(p);

                p = new Planet();
                p.x = 443/(float)800 * width;
                p.y = 175/(float)600 * height;
                p.radius = 54/(float)700 * (width+height)/2;
                p.gravity = 110;
                p.density = 1;
                planets.add(p);

                p = new Planet();
                p.x = 511/(float)800 * width;
                p.y = 345/(float)600 * height;
                p.radius = 59/(float)700 * (width+height)/2;
                p.gravity = 120;
                p.density = 1;
                planets.add(p);
                break;


            case 7:
                min_power = 1;
                max_power = 30;

                //home planet
                home_planet.x = 100/(float)800 * width;
                home_planet.y = 450/(float)600 * height;
                home_planet.radius = 50/(float)700 * (width+height)/2;
                home_planet.gravity = 50*80;
                home_planet.density = (float) 0.5;

                goal_planet.x = 500/(float)800 * width;
                goal_planet.y = 200/(float)600 * height;
                goal_planet.radius = 50/(float)700 * (width+height)/2;
                goal_planet.gravity = 60*80;
                goal_planet.density = (float) 1.5;

                p = new Planet();
                p.x = 350/(float)800 * width;
                p.y = 200/(float)600 * height;
                p.radius = 40/(float)700 * (width+height)/2;
                p.gravity = 100*80;
                p.density = (float) 1.25;
                planets.add(p);

                p = new Planet();
                p.x = 650/(float)800 * width;
                p.y = 200/(float)600 * height;
                p.radius = 40/(float)700 * (width+height)/2;
                p.gravity = 80*80;
                p.density = 1;
                planets.add(p);

                p = new Planet();
                p.x = 500/(float)800 * width;
                p.y = 50/(float)600 * height;
                p.radius = 40/(float)700 * (width+height)/2;
                p.gravity = 50*80;
                p.density = 5/8;
                planets.add(p);

                p = new Planet();
                p.x = 500/(float)800 * width;
                p.y = 350/(float)600 * height;
                p.radius = 40/(float)700 * (width+height)/2;
                p.gravity = 90*80;
                p.density = 9/8;
                planets.add(p);

                p = new Planet();
                p.x = 400/(float)800 * width;
                p.y = 100/(float)600 * height;
                p.radius = 40/(float)700 * (width+height)/2;
                p.gravity = 120*80;
                p.density = (float) 1.5;
                planets.add(p);

                p = new Planet();
                p.x = 600/(float)800 * width;
                p.y = 100/(float)600 * height;
                p.radius = 40/(float)700 * (width+height)/2;
                p.gravity = 150;
                p.density = 15/8;
                planets.add(p);

                p = new Planet();
                p.x = 400/(float)800 * width;
                p.y = 300/(float)600 * height;
                p.radius = 40/(float)700 * (width+height)/2;
                p.gravity = 80*80;
                p.density = 1;
                planets.add(p);

                p = new Planet();
                p.x = 600/(float)800 * width;
                p.y = 300/(float)600 * height;
                p.radius = 40/(float)700 * (width+height)/2;
                p.gravity = 100*80;
                p.density = 5/4;
                planets.add(p);
                break;
        }
    }
}