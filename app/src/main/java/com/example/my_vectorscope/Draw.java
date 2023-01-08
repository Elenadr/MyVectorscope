package com.example.my_vectorscope;


import android.graphics.Color;
import android.graphics.Paint;

import android.util.AttributeSet;
import android.content.Context;
import android.graphics.Canvas;

import static com.example.my_vectorscope.MainActivity.cuadratura_final;
import static com.example.my_vectorscope.MainActivity.fase_final;

import java.util.Objects;


public class Draw extends androidx.appcompat.widget.AppCompatImageView {
    private Paint drawPaint;

    public Draw(Context context) {
        super(context);
        setupDrawing();
    }
    public Draw(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupDrawing();
    }

    public Draw(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setupDrawing();

    }
    private void setupDrawing(){

        drawPaint = new Paint();
        drawPaint.setARGB(255, 255, 0,0);
        drawPaint.setStrokeWidth(4);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
    }


    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT);
        canvas.save();
        canvas.rotate(237,this.getWidth()/2f,this.getHeight()/2f);
        canvas.translate(-12f,15f);
        if(cuadratura_final == null &  fase_final == null) {

            float[] fils_barras = {0f,0.3216f,-0.5959f, -0.2744f,0.2744f,0.5959f,-0.3216f,-0.0f,-0.1602f,0f,-0.0233f,0f,0f};
            float[] cols_barras = {0f,-0.3114f,-0.2115f, -0.5229f,0.5229f,0.2115f,0.3214f,0.0f,-0.0118f,0f,0.2020f,0f,0f};

            for (int i = 0; i < fils_barras.length; i++) {
                if (i < fils_barras.length-1){
                    canvas.drawLine(this.getWidth()/2f+fils_barras[i+1]*610,this.getHeight()/2f+cols_barras[i+1]*610,this.getWidth()/2f+fils_barras[i]*610,this.getWidth()/2f+cols_barras[i]*610,drawPaint);
                }
            }
        }else{
            for (int i = 0; i < Objects.requireNonNull(cuadratura_final).length; i++) {
                if (i < cuadratura_final.length-1){
                    canvas.drawLine(this.getWidth()/2f+cuadratura_final[i+1]*610,this.getHeight()/2f+fase_final[i+1]*610,this.getWidth()/2f+cuadratura_final[i]*610,this.getWidth()/2f+fase_final[i]*610,drawPaint);
                }
            }
        }
        canvas.restore();

        super.onDraw(canvas);
        //you need to call postInvalidate so that the system knows that it  should redraw your custom ImageView
        this.postInvalidate();
    }

}