/***
 Copyright (c) 2015 CommonsWare, LLC
 Licensed under the Apache License, Version 2.0 (the "License"); you may not
 use this file except in compliance with the License. You may obtain a copy
 of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
 by applicable law or agreed to in writing, software distributed under the
 License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 OF ANY KIND, either express or implied. See the License for the specific
 language governing permissions and limitations under the License.

 Covered in detail in the book _The Busy Coder's Guide to Android Development_
 https://commonsware.com/Android
 */

package com.apphead.sample;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.media.Image;
import android.media.ImageReader;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager;
import com.google.firebase.ml.custom.FirebaseCustomRemoteModel;

import org.tensorflow.lite.task.vision.detector.ObjectDetector;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

public class ImageTransmogrifier implements ImageReader.OnImageAvailableListener {


    private static final String TAG = "ImageTransmogrifier";
    private final int width;
  private final int height;
  private final ImageReader imageReader;
  private final widgetService svc;
  Boolean created=false;

int z;
  private Bitmap latestBitmap=null;
 // LocalModel localModel;
  FirebaseCustomRemoteModel remoteModel;
  Canvas canvas;
 // DownloadConditions downloadConditions;

  private static int MODEL_IMAGE_INPUT_SIZE = 192;
  private Matrix imageTransformMatrix;
  private Bitmap imageBitmapForModel = null;
  private Bitmap rgbBitmapForCameraImage = null;


  Paint pen;

  ObjectDetector.ObjectDetectorOptions optionsBuilder;

  FirebaseCustomRemoteModel customObjectDetectorOptions;
  boolean computing=false;
    MobileNetObjDetector objectDetector2;
  //InputImage imagedetect;
  ObjectDetector objectDetector;

  drawcallback drawcallback;

  boolean swt;
  public void getScreenshotsPeriodacally(int milliseconds){

    try {
      Thread.sleep(milliseconds);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

  }
  ImageTransmogrifier(widgetService svc, drawcallback drawcallback, boolean b) {
    this.svc=svc;

    this.drawcallback=drawcallback;
    this.swt=b;


//     localModel =
//            new LocalModel.Builder()
//                    .setAssetFilePath("xxx.tflite")
//                    // or .setAbsoluteFilePath(absolute file path to model file)
//                    // or .setUri(URI to model file)
//                    .build();


    remoteModel =new  FirebaseCustomRemoteModel.Builder("modle512").build();

if(swt)
{
    FirebaseModelManager.getInstance().isModelDownloaded(remoteModel)
            .addOnSuccessListener(new OnSuccessListener<Boolean>() {
                @Override

                public void onSuccess(Boolean isDownloaded) {

                    if (isDownloaded) {
                        Toast.makeText(svc.getApplicationContext(),remoteModel.getModelName(), Toast.LENGTH_LONG).show();
                        FirebaseModelManager.getInstance().getLatestModelFile(remoteModel).addOnSuccessListener(new OnSuccessListener<File>() {
                            @Override
                            public void onSuccess(File file) {

                                if (file != null) {

                                    try {
                                        objectDetector2 = MobileNetObjDetector.createRemote(file,svc.getAssets());
                                        Toast.makeText(svc.getApplicationContext(),"online", Toast.LENGTH_LONG).show();
                                        created =true;

                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }


                                }
                                else {
                                    try {
                                        Toast.makeText(svc.getApplicationContext(),"offline", Toast.LENGTH_LONG).show();

                                        objectDetector2 = MobileNetObjDetector.createLocal(svc.getAssets());
                                        created =true;

                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                }
                            }
                        });

                    } else {

                        try {
                            objectDetector2 = MobileNetObjDetector.createLocal(svc.getAssets());
                            Toast.makeText(svc.getApplicationContext(),"local", Toast.LENGTH_LONG).show();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        created =true;

                    }
                }
            });

}else {
    Toast.makeText(svc.getApplicationContext(),"offline", Toast.LENGTH_LONG).show();

    try {
        objectDetector2 = MobileNetObjDetector.createLocal(svc.getAssets());
    } catch (IOException e) {
        e.printStackTrace();
    }
    created =true;
}




    Display display=svc.getWindowManager().getDefaultDisplay();
    Point size=new Point();



    display.getRealSize(size);


    int width=size.x;
    int height=size.y;

    while (width*height > (2<<19)) {
      width=width>>1;
      height=height>>1;
    }

    this.width=width;
    this.height=height;

    imageReader= ImageReader.newInstance(width, height,
        PixelFormat.RGBA_8888, 2);


    imageReader.setOnImageAvailableListener(this, svc.getHandler());


  }

  @Override
  public void onImageAvailable(ImageReader reader) {




getScreenshotsPeriodacally(500);
      if(!created)
      {
          return;
      }

    final Image image=imageReader.acquireLatestImage();

    if (image!=null) {

        if(computing)
        {
            return;
        }
        computing=true;

      Image.Plane[] planes = image.getPlanes();
      ByteBuffer buffer = planes[0].getBuffer();
      int pixelStride = planes[0].getPixelStride();
      int rowStride = planes[0].getRowStride();
      int rowPadding = rowStride - pixelStride * width;
      int bitmapWidth = width + rowPadding / pixelStride;




      if (latestBitmap == null ||
              latestBitmap.getWidth() != bitmapWidth ||
              latestBitmap.getHeight() != height) {
        if (latestBitmap != null) {
          latestBitmap.recycle();
        }

        latestBitmap = Bitmap.createBitmap(bitmapWidth,
                height, Bitmap.Config.ARGB_8888);
      }

      latestBitmap.copyPixelsFromBuffer(buffer);

      image.close();

      Bitmap cropped = Bitmap.createBitmap(latestBitmap, 0, 0,
              width, height);
      imageBitmapForModel = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888);

imageBitmapForModel=getResizedBitmap(cropped,512,512);



      rgbBitmapForCameraImage=cropped;



//      imageTransformMatrix = ImageUtils.getTransformationMatrix(width, height,
//            192, 192, svc.windowManager.getDefaultDisplay().getRotation(),false);
//     imageTransformMatrix.invert(new Matrix());
//     imageTransformMatrix=new Matrix();
////

///
  //  new Canvas(imageBitmapForModel).drawBitmap(cropped, imageTransformMatrix, null);
//screenshotHandler(cropped);
    //  computing = true;
    //  preprocessImageForModel(cropped);



        screenshotHandler(imageBitmapForModel);



    }











  }

  private Bitmap getScreenshot()
  {


      return rgbBitmapForCameraImage;
  }
    private Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
       // matrix.postRotate(svc.windowManager.getDefaultDisplay().getRotation());
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }
void screenshotHandler(Bitmap bitmap)
{
    Log.d(TAG, "Screenshot handler called!");


    final List<DetectionResult> results = objectDetector2.detectObjects((bitmap),width,height);


    drawcallback.drawOnscreen(results);
  //Log.i(LOGGING_TAG, results.get(0).toString());

   canvas = new Canvas(rgbBitmapForCameraImage);





//
  if (results.size() > 0) {
//    String title = results.get(0).getTitle();
//
//
//    for (int ix = 1; ix < results.size() - 1; ix++) {
//
//        if (results.get(ix).getConfidence()>0.2)
//        {
//            title += ", ";
//            title += results.get(ix).getTitle();
//
//            if(title.contains("Line"))
//            {
//                PointF p1=new PointF();
//
//                PointF p2=new PointF();
//
//                p1.set(results.get(ix).getLocation().left,results.get(ix).getLocation().top);
//                p2.set(results.get(ix).getLocation().right,results.get(ix).getLocation().bottom);
//
//                drawLineOnScreen(p1,p2,Color.RED);
//            }
//            else {
//                RectF box = results.get(ix).getLocation();
//              // Paint p= new Paint();
//             //  p.setColor(Color.RED);
//              // p.setStyle(Paint.Style.FILL);
//              // p.setStrokeWidth(5);
//              // p.setTextSize(20);
//              //  canvas.drawText(results.get(ix).getTitle().substring(1),box.centerX(),box.centerY(),p);
//                drawRectangle(box,0,0);
//            }
//
//        }
//
//
//
//
//
//    }
//
//      //  Toast.makeText(svc.getApplicationContext(),results.size()+"k",Toast.LENGTH_LONG).show();
//
//  //  canvas.drawBitmap(rgbBitmapForCameraImage,matrix,null);
//   // vv=getResizedBitmap(vv,720,height);
//   //   new Canvas(rgbBitmapForCameraImage).drawBitmap(vv, matrix, null);

    svc.saveBitmapToStorage(rgbBitmapForCameraImage);

    computing=false;

  }
}

    void drawRectangle(RectF rect, float x, float y){
        Paint pen = new Paint();
        pen.setColor(Color.RED);



        pen.setStrokeWidth(1F);
        pen.setStyle(Paint.Style.STROKE);

        canvas.drawRoundRect(rect,x,y,pen);

    }

    void drawLineOnScreen(PointF a, PointF b, int color)
    {
        Paint pen = new Paint();
        pen.setColor(color);



        pen.setStrokeWidth(1F);
        pen.setStyle(Paint.Style.STROKE);

        canvas.drawLine(a.x,a.y,b.x,b.y,pen);
    }
  private void preprocessImageForModel(final Image imageFromCamera) {
   rgbBitmapForCameraImage.setPixels(ImageUtils.convertYUVToARGB(imageFromCamera, z, height),
           0, z, 0, 0, z, height);

    new Canvas(imageBitmapForModel).drawBitmap(rgbBitmapForCameraImage, imageTransformMatrix, null);
  }




  Surface getSurface() {


    return(imageReader.getSurface());
  }

  int getWidth() {
    return(width);
  }

  int getHeight() {
    return(height);
  }



  void close() {
    imageReader.close();
  }
}
