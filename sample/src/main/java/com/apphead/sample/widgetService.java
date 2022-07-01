package com.apphead.sample;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.AudioManager;
import android.media.MediaScannerConnection;
import android.media.ToneGenerator;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.apphead.sample.apphead.AppHeadArgs;
import com.apphead.sample.apphead.Head;
import com.apphead.sample.apphead.HeadService;
import com.apphead.sample.apphead.HeadView;
import com.apphead.sample.apphead.HeadViewListener;
import com.apphead.sample.apphead.WindowManagerHelper;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.UUID;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;

public class widgetService extends Service implements drawcallback {

    private static final String TAG = "widgetService";
    int Layout_Flag;
    View mFloatingView;
    HeadView headView;

    int FristClick=0;


    WindowManager myminger;
    Button btnWindowClose, btnWindowStart;
    private static final String CHANNEL_WHATEVER = "channel_whatever";
    private static final int NOTIFY_ID = 9906;
    static final String EXTRA_RESULT_CODE = "resultCode";
    static final String EXTRA_RESULT_INTENT = "resultIntent";
    static final String ACTION_SHOW =
            BuildConfig.APPLICATION_ID + ".SHOW";
    static final String ACTION_HIDE =
            BuildConfig.APPLICATION_ID + ".HIDE";
    static final int VIRT_DISPLAY_FLAGS =

            DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY |
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;
    private MediaProjection projection;
    private VirtualDisplay vdisplay;
    final private HandlerThread handlerThread =
            new HandlerThread(getClass().getSimpleName(),
                    android.os.Process.THREAD_PRIORITY_BACKGROUND);
    private Handler handler;
    private MediaProjectionManager mgr;
    private WindowManager wmgr;
    private ImageTransmogrifier it;
    View framView;
    Boolean click=true;


    OverlayView overlayView;
    private int resultCode;
    private Intent resultData;
    final private ToneGenerator beeper =
            new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
    WindowManager windowManager;

    @Override
    public void onCreate() {
        super.onCreate();

//        if(Head.INSTANCE.getHeadViewArgs()==null)
//        {
//            stopSelf();
//            return;
//        }

        mgr = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        wmgr = (WindowManager) getSystemService(WINDOW_SERVICE);

        handlerThread.start();


        handler = new Handler(handlerThread.getLooper());

      //  myminger = new WindowManagerHelper().manager(this);


    }

    Void setUpHeadView() {

       headView=   HeadView.Companion.setup(this);


      headView.listener=new HeadViewListener() {
            @Override
            public void onDismiss(@NotNull HeadView view) {
                Intrinsics.checkParameterIsNotNull(view, "view");
                Function1 var10000 = Head.INSTANCE.getHeadViewArgs().getOnDismiss$sample_debug();
                if (var10000 != null) {
                    Unit var2 = (Unit)var10000.invoke(view);
                }

                widgetService.this.stopSelf();
            }

            @Override
            public void onClick(@NotNull HeadView view) {




                startRecordingAndDetection();

                if (click)
                {
                    ((ImageView)  view.getRootView().findViewById(R.id.headImageView)).setImageDrawable(getDrawable(R.drawable.ic_messenger));

                    framView.setVisibility(View.VISIBLE);

                    startRecordingAndDetection();
                    click=false;

                }
                else {

                    ((ImageView)  view.getRootView().findViewById(R.id.headImageView)).setImageDrawable(getDrawable(R.drawable.ic_messenger_red));
                   // stopSelf();
                  //  stopForeground(true);
                    stopRecordingAndDetection();

                    framView.setVisibility(View.GONE);

                    click=true;
                }




                Function1 var10000 = Head.INSTANCE.getHeadViewArgs().getOnClick$sample_debug();
                if (var10000 != null) {
                    Unit var2 = (Unit)var10000.invoke(view);
                }

                if (Head.INSTANCE.getHeadViewArgs().getDismissOnClick$sample_debug()) {
                    widgetService.this.stopSelf();
                }
            }

            @Override
            public void onLongClick(@NotNull HeadView view) {
                Intrinsics.checkParameterIsNotNull(view, "view");
                Function1 var10000 = Head.INSTANCE.getHeadViewArgs().getOnLongClick$sample_debug();
                if (var10000 != null) {
                    Unit var2 = (Unit)var10000.invoke(view);
                }

            }
        };

        return  null;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG,"onBind called");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent.getAction() == null) {
            resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, 1337);
            resultData = intent.getParcelableExtra(EXTRA_RESULT_INTENT);
            foregroundify();




        }  else  if (ACTION_SHOW.equals(intent.getAction())) {
            if(headView.getVisibility()== View.GONE)
            {


                headView.setVisibility(View.VISIBLE);


            }

        } else if (ACTION_HIDE.equals(intent.getAction())) {

            if(headView.getVisibility()== View.VISIBLE)
            {


                        headView.setVisibility(View.GONE);


            }


        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
         stopRecordingAndDetection();


        super.onDestroy();
            if(mFloatingView!=null){
                windowManager.removeView(mFloatingView);
            }
            if (framView!=null)
            {
                windowManager.removeView(framView);
            }


    }
    WindowManager getWindowManager() {
        return(wmgr);
    }

    Handler getHandler() {
        return(handler);
    }

    void saveBitmapToStorage(final Bitmap bitmap) {

        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);

        byte[] newPng=baos.toByteArray();



        String id= UUID.randomUUID().toString();

        new Thread() {

            @Override
            public void run() {
                File output=new File(getExternalFilesDir(null),
                        "screenshot"+id+".png");

                try {
                    FileOutputStream fos=new FileOutputStream(output);

                    fos.write(newPng);
                    fos.flush();
                    fos.getFD().sync();
                    fos.close();

                    MediaScannerConnection.scanFile(widgetService.this,
                            new String[] {output.getAbsolutePath()},
                            new String[] {"image/png"},
                            null);

                    MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "yourTitle" , "yourDescription");

                }
                catch (Exception e) {
                    Log.e(getClass().getSimpleName(), "Exception writing out screenshot", e);
                }
            }
        }.start();

      //  beeper.startTone(ToneGenerator.TONE_PROP_ACK);
        //stopCapture();
    }

    private  void stopRecordingAndDetection() {
        if (projection!=null) {
            projection.stop();
            vdisplay.release();
            projection=null;

        }
    }

    private void startRecordingAndDetection() {
        projection=mgr.getMediaProjection(resultCode, resultData);
        it=new ImageTransmogrifier(this,this,false);

        MediaProjection.Callback cb=new MediaProjection.Callback() {


            @Override
            public void onStop() {
                vdisplay.release();
            }
        };

        vdisplay=projection.createVirtualDisplay("andshooter",
                it.getWidth(), it.getHeight(),
                getResources().getDisplayMetrics().densityDpi,
                VIRT_DISPLAY_FLAGS, it.getSurface(), null, handler);


        projection.registerCallback(cb, handler);




    }
    private void foregroundify() {
        NotificationManager mgr=
                (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.O &&
                mgr.getNotificationChannel(CHANNEL_WHATEVER)==null) {
            mgr.createNotificationChannel(new NotificationChannel(CHANNEL_WHATEVER,
                    "Whatever", NotificationManager.IMPORTANCE_DEFAULT));
        }

        NotificationCompat.Builder b=
                new NotificationCompat.Builder(this, CHANNEL_WHATEVER);

        b.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL);

        b.setContentTitle(getString(R.string.app_name))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker(getString(R.string.app_name));

        b.addAction(R.drawable.ic_stat_show,
                "showWindow",
                buildPendingIntent(ACTION_SHOW));

        b.addAction(R.drawable.ic_stat_hide,
               "HideWindow",
                buildPendingIntent(ACTION_HIDE));

        startForeground(NOTIFY_ID, b.build());
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O)
        {
            Layout_Flag= WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;

        }
        else {
            Layout_Flag= WindowManager.LayoutParams.TYPE_PHONE;
        }
        mFloatingView= LayoutInflater.from(getApplicationContext()).inflate(R.layout.floatingwindow,null);

        framView= LayoutInflater.from(getApplicationContext()).inflate(R.layout.framview,null);

        overlayView=framView.findViewById(R.id.overlay);



        btnWindowClose=mFloatingView.findViewById(R.id.btn_float_Close);
        btnWindowStart=mFloatingView.findViewById(R.id.btn_float_Start);

        btnWindowClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopSelf();
                stopForeground(true);
                 stopRecordingAndDetection();
            }
        });

        btnWindowStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startRecordingAndDetection();

            }

        });

        WindowManager.LayoutParams layoutParams=new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Layout_Flag, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        WindowManager.LayoutParams layoutParamsfram=new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                Layout_Flag, WindowManager.LayoutParams.FLAG_FULLSCREEN|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN|WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, PixelFormat.TRANSLUCENT);
        layoutParams.gravity= Gravity.TOP| Gravity.END;
        layoutParams.x=0;
        layoutParams.y=100;




        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY, // TYPE_SYSTEM_ALERT is denied in apiLevel >=19
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_FULLSCREEN,
                PixelFormat.TRANSLUCENT
        );

        windowManager =  WindowManagerHelper.INSTANCE.manager(this);

        // WindowManagerHelper.INSTANCE.manager(this).addView(mFloatingView,layoutParams);

       myminger=(WindowManager) getSystemService(WINDOW_SERVICE);

   //     windowManager.addView(mFloatingView,layoutParams);

//        headView = new HeadView(this);
//
//        headView.setup();



        setUpHeadView();

       myminger.addView(framView,layoutParamsfram);


//        mFloatingView.setOnTouchListener(new View.OnTouchListener() {
//
//            int initialX,initialY;
//            float initialTouchX,initialTouchY;
//            long startClickTime;
//
//
//
//
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//
//                switch (event.getAction())
//                {
//                    case MotionEvent.ACTION_DOWN:
//                        initialX=layoutParams.x;
//                        initialY=layoutParams.y;
//                        initialTouchX=event.getRawX();
//                        initialTouchY=event.getRawY();
//
//                        return  true;
//                    case MotionEvent.ACTION_UP:
//                        layoutParams.x=initialX+(int)(initialTouchX-event.getRawX());
//                        layoutParams.y=initialY+(int)(event.getRawY()-initialTouchY);
//
//                        return  true;
//                    case MotionEvent.ACTION_MOVE:
//                        layoutParams.x=initialX+(int)(initialTouchX-event.getRawX());
//                        layoutParams.y=initialY+(int)(event.getRawY()-initialTouchY);
//                        windowManager.updateViewLayout(mFloatingView,layoutParams);
//                        return  true;
//
//                }
//                return false;
//            }
//        });

    }


    private PendingIntent buildPendingIntent(String action) {
        Intent i=new Intent(this, getClass());

        i.setAction(action);

        return(PendingIntent.getService(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT));
    }


    @Override
    public void drawOnscreen(List<DetectionResult> results) {

        overlayView.setResults(results);

        if(overlayView!=null)
        {
            overlayView.postInvalidate();
        }



    }
}

