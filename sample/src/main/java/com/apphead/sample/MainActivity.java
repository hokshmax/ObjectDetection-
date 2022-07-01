package com.apphead.sample;

import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.apphead.sample.apphead.BadgeView;
import com.apphead.sample.apphead.DismissView;
import com.apphead.sample.apphead.HeadView;
import com.apphead.sample.apphead.ServiceHelper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager;
import com.google.firebase.ml.custom.FirebaseCustomRemoteModel;
import com.apphead.sample.apphead.AppHead;

import com.apphead.sample.apphead.Head;

import org.jetbrains.annotations.Nullable;

import kotlin.Unit;


public class MainActivity extends FragmentActivity//, AppCompatActivity
{
    Button btn_start;
    private static final int REQUEST_SCREENSHOT = 59706;
    private MediaProjectionManager mgr;
    FirebaseCustomRemoteModel remoteModel;

    FirebaseModelDownloadConditions downloadConditions;

    String TAG = "MainActivity";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate called!");
        setContentView(R.layout.activity_main);
        //Code of "objectDetection"
        onCreate_objectDetection();
        //End of object detection section

        //  startService(new Intent(this,widgetService.class));

        //   finish();
    }

    void appHead(FragmentActivity activity) {
        // build HeadView
        HeadView.Args headViewArgs = new HeadView.Args()
                .layoutRes(R.layout.app_head_red, R.id.headImageView)
                .onClick(headView ->
                {
//                    Log.i(TAG, "Main Icon clicked!");
//                    // your logic
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                        Log.i(TAG, Build.VERSION.SDK_INT + " is a supported sdk version.");
//                        if (!Settings.canDrawOverlays(MainActivity.this)) {
//                            Log.i(TAG, "Trying to get a permission");
//                            getPermission_objectDetection();
//                        } else {
//                            Log.i(TAG, "Trying to get a screenshot!");
////                            mgr = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
////                            if (mgr == null) {
////                                Log.e(TAG, "Couldn't get media projector manager!");
////                            } else {
////                                Log.d(TAG, "Starting ScreenShot activity!");
////                                startActivityForResult(mgr.createScreenCaptureIntent(), REQUEST_SCREENSHOT);
////                            }
//                        }
//                    } else {
//                        Log.i(TAG, Build.VERSION.SDK_INT + "is unsupported sdk version.");
//                    }

                    return Unit.INSTANCE;
                })
                .onLongClick(headView ->
                {
                    // your logic
                    return Unit.INSTANCE;
                })
                .alpha(0.9f)
                .allowBounce(false)
                .onFinishInflate(headView ->
                {
                    // your logic
                    return Unit.INSTANCE;
                })
                .setupImage(imageView ->
                {
                    // your logic
                    return Unit.INSTANCE;
                })
                .onDismiss(headView ->
                {
                    // your logic
                    return Unit.INSTANCE;
                })
                .dismissOnClick(false)
                .preserveScreenLocation(false);

        // build DismissView
        DismissView.Args dismissViewArgs = new DismissView.Args()
                .alpha(0.5f)
                .scaleRatio(1.0)
                .drawableRes(R.drawable.ic_dismiss)
                .onFinishInflate(dismissView ->
                {
                    // your logic
                    return Unit.INSTANCE;
                })

                .setupImage(imageView ->
                {
                    // your logic
                    return Unit.INSTANCE;
                });

        BadgeView.Args badgeViewArgs = new BadgeView.Args()
                .layoutRes(R.layout.badge_view, R.id.tvCount)
                .position(BadgeView.Position.BOTTOM_END)
                .count("100");

        Head.Builder builder = new Head.Builder(R.drawable.ic_messenger_red)
                .headView(headViewArgs)
                .dismissView(dismissViewArgs)
                .badgeView(badgeViewArgs);


        new AppHead(builder).show(activity);
    }

//Code form "objectDetection" project.
//public class MainActivity extends AppCompatActivity {

    //Methods of "objectDetection" Project
    protected void onCreate_objectDetection() {
        //super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate_objectDetection called");
        btn_start = findViewById(R.id.btnShowHead);
btn_start.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        if(!Settings.canDrawOverlays(MainActivity.this))
        {
            getPermission_objectDetection();
        }
        else {

            mgr=(MediaProjectionManager)getSystemService(MEDIA_PROJECTION_SERVICE);


            startActivityForResult(mgr.createScreenCaptureIntent(),REQUEST_SCREENSHOT);



        }

    }


});


        requestPermission();

        remoteModel = new  FirebaseCustomRemoteModel.Builder("modle512").build();

        downloadConditions = new FirebaseModelDownloadConditions.Builder()
                .requireWifi()
                .build();
        FirebaseModelManager.getInstance().download(remoteModel, downloadConditions).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid)
            {
                Log.d(TAG, "onSuccess of downloading remote model");

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Failed to download remote model: " +e.getMessage());
            }
        });

        Log.d(TAG, "onCreate_objectDetection finished");
    }

    //@RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult called");
        Log.d(TAG, "Request code:" + requestCode);

        if(requestCode==1)
        {
            int MINIMIUM_SDK_VERSION = Build.VERSION_CODES.M;
            if (Build.VERSION.SDK_INT >= MINIMIUM_SDK_VERSION)
            {
                Log.d(TAG,"Android sdk version greater than:" + MINIMIUM_SDK_VERSION);
                if(!Settings.canDrawOverlays(this))
                {
                    Toast.makeText(this,"please generate permission",Toast.LENGTH_LONG).show();

                    return;
                }
            }
        }
        if (requestCode==REQUEST_SCREENSHOT)
        {
            Log.d(TAG, "Got a request code of screenshot!");
            if (resultCode==RESULT_OK) {
                Intent i=
                        new Intent(this, widgetService.class)
                                .putExtra(widgetService.EXTRA_RESULT_CODE, resultCode)
                                .putExtra(widgetService.EXTRA_RESULT_INTENT, data);

                ServiceHelper.INSTANCE.setI(i);

                appHead(this);
        //       startService(i);
                finish();

            }
        }
    }

    private void requestPermission()
    {
        Log.i(TAG, "Try to get permissions...");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[] {
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 47);
        }
    }

    void  getPermission_objectDetection()
    {
        Log.d(TAG, "Trying to get permissions of objectDetection!");
      if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M && !Settings.canDrawOverlays(this))
        {
            Log.d(TAG, "Trying to start activity for getting ACTION_MANAGE_OVERLAY_PERMISSION");
            Intent  intent=new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:"+getPackageName()));
            startActivityForResult(intent,1);
        }
      else
      {
          Log.d(TAG, "Overlay permission exist!.");
      }
    }
}

