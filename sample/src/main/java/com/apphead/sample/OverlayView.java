package com.apphead.sample;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import java.util.LinkedList;
import java.util.List;


public class OverlayView extends View {
    private static int INPUT_SIZE = 512;
Canvas canvas=null;
    private final Paint paint;
    private final List<DrawCallback> callbacks = new LinkedList();
    private List<DetectionResult> results;
    private List<Integer> colors;
    private float resultsViewHeight;

    public OverlayView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                20, getResources().getDisplayMetrics()));
        resultsViewHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                112, getResources().getDisplayMetrics());
    }

    public void addCallback(final DrawCallback callback) {
        callbacks.add(callback);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }


    @Override
    public synchronized void onDraw(final Canvas canvas) {
        for (final DrawCallback callback : callbacks) {
            callback.drawCallback(canvas);
        }




//        if (results != null) {
//            for (int i = 0; i < results.size(); i++) {
//                if (results.get(i).getConfidence() > 0.2) {
//                    RectF box = results.get(i).getLocation();
//                //    String title = results.get(i).getTitle() + String.format(" %2.2f", results.get(i).getConfidence()*100) + "%";
//                    paint.setColor(Color.RED);
//                    paint.setStyle(Paint.Style.STROKE);
//                    canvas.drawRect(box, paint);
//                    paint.setStrokeWidth(2.0f);
//                    paint.setStyle(Paint.Style.FILL_AND_STROKE);
//                   // canvas.drawText(title, box.left, box.top, paint);
//                }
//            }
//        }

        if (results != null)
        {
            if (results.size() > 0) {
                this.canvas=canvas;

                String title = results.get(0).getTitle();


                for (int ix = 1; ix < results.size() - 1; ix++) {

                    if (results.get(ix).getConfidence()>0.2)
                    {
                        title += ", ";
                        title += results.get(ix).getTitle();

                        if(title.contains("Line"))
                        {
                            PointF p1=new PointF();

                            PointF p2=new PointF();

                            p1.set(results.get(ix).getLocation().left,results.get(ix).getLocation().top);
                            p2.set(results.get(ix).getLocation().right,results.get(ix).getLocation().bottom);

                            drawLineOnScreen(p1,p2, Color.RED);
                        }
                        else {
                            RectF box = results.get(ix).getLocation();
                            // Paint p= new Paint();
                            //  p.setColor(Color.RED);
                            // p.setStyle(Paint.Style.FILL);
                            // p.setStrokeWidth(5);
                            // p.setTextSize(20);
                            //  canvas.drawText(results.get(ix).getTitle().substring(1),box.centerX(),box.centerY(),p);
                            drawRectangle(box,0,0);
                        }

                    }





                }

                //  Toast.makeText(svc.getApplicationContext(),results.size()+"k",Toast.LENGTH_LONG).show();

                //  canvas.drawBitmap(rgbBitmapForCameraImage,matrix,null);
                // vv=getResizedBitmap(vv,720,height);
                //   new Canvas(rgbBitmapForCameraImage).drawBitmap(vv, matrix, null);

                // svc.saveBitmapToStorage(rgbBitmapForCameraImage);

                //computing=false;

            }

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
    public void setResults(final List<DetectionResult> results) {
        this.results = results;
        postInvalidate();
    }

    public interface DrawCallback {
        void drawCallback(final Canvas canvas);
    }

    private RectF reCalcSize(RectF rect) {
        int padding = 5;
        float overlayViewHeight = getHeight() - resultsViewHeight;
        float sizeMultiplier = Math.min((float) getWidth() / (float) INPUT_SIZE,
                overlayViewHeight / (float) INPUT_SIZE);

        float offsetX = (getWidth() - INPUT_SIZE * sizeMultiplier) / 2;
        float offsetY = (overlayViewHeight - INPUT_SIZE * sizeMultiplier) / 2 + resultsViewHeight;

        float left = Math.max(padding, sizeMultiplier * rect.left + offsetX);
        float top = Math.max(offsetY + padding, sizeMultiplier * rect.top + offsetY);

        float right = Math.min(rect.right * sizeMultiplier, getWidth() - padding);
        float bottom = Math.min(rect.bottom * sizeMultiplier + offsetY, getHeight() - padding);

        RectF newRect = new RectF(left, top, right, bottom);
        return newRect;
    }

}