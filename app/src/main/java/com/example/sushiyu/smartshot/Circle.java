package com.example.sushiyu.smartshot;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

public class Circle extends View {

    private Paint paint;
    private int circleColor;
    private int circleProgressColor;
    private int textColor;
    private float textSize;
    private float roundWidth;
    private int max;
    private int progress;
    private boolean textIsDisplayable;
    private int style;
    public static final int STROKE = 0;
    public static final int FILL = 1;


    public Circle(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint p = new Paint();
        canvas.drawCircle(100, 100, 90, p);



        /*
        Paint p = new Paint();
        p.setColor(Color.RED);// 设置红色

        canvas.drawText("画圆：", 10, 20, p);// 画文本
        canvas.drawCircle(60, 20, 10, p);// 小圆
        p.setAntiAlias(true);// 设置画笔的锯齿效果。 true是去除，大家一看效果就明白了
        canvas.drawCircle(120, 20, 20, p);// 大圆

        canvas.drawText("画线及弧线：", 10, 60, p);
        p.setColor(Color.GREEN);// 设置绿色
        canvas.drawLine(60, 40, 100, 40, p);// 画线
        canvas.drawLine(110, 40, 190, 80, p);// 斜线
        */
    }


}