package com.example.sushiyu.smartshot;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

//import com.example.sushiyu.smartshot.R;

/**
 * Created by gjy on 16/7/4.
 */

public class SuperCircleView extends View {

    private final String TAG = "SuperCircleView";

    private int mViewWidth; //view的宽
    private int mViewHeight;    //view的高
    private int mViewCenterX;   //view宽的中心点
    private int mViewCenterY;   //view高的中心点
    private int mMinRadio; //最里面白色圆的半径
    private float mRingWidth; //圆环的宽度
    private int mSelect;    //分成多少段
    private int mSelectAngle;   //每个圆环之间的间隔
    private int mMinCircleColor;    //最里面圆的颜色
    private int mMaxCircleColor;    //最外面圆的颜色
    private int mRingNormalColor;    //默认圆环的颜色
    private Paint mPaint;
    private int color[] = new int[3];   //渐变颜色
    private float mViewStartXRec;
	private float mViewStartYRec;
    private float mViewStartX; //圆弧起始点X
    private float mViewStartY; //圆弧起始点Y
    private float mViewStopX; //圆弧终点X
    private float mViewStopY; //圆弧终点Y

    private double AA; //余弦定理A边平方
    private double BB; //余弦定理B边平方
    private double CC; //余弦定理C边平方

    private double A; //余弦定理A边
    private double B; //余弦定理B边
    private int mStartAngle; //余弦定理B边

    private int mViewZeroAngleX;   //view宽的中心点
    private int mViewZeroAngleY;   //view高的中心点


    private float mRingAngleWidth;  //每一段的角度

    private RectF mRectF; //圆环的矩形区域
    private int mSelectRing = 0;        //要显示几段彩色

    private boolean isShowSelect = false;   //是否显示断

    private int loop = 0;
    private int count_p = 0;
    private int count_n = 0;
    public static int mAngle; //余弦定理B边
    public static int direction = 0x00;
    public static int direction_init = 0xAA;
    private int direction_rec_0;
    private int direction_rec_1;
    private int direction_rec_2;
    private int direction_rec_3;
    private int direction_init_cnt;
	TextView textView;

    public SuperCircleView(Context context) {
        this(context, null);
    }

    public SuperCircleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SuperCircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SuperCircleView);
        mMinRadio = a.getInteger(R.styleable.SuperCircleView_min_circle_radio, 400);

        mRingWidth = a.getFloat(R.styleable.SuperCircleView_ring_width, 40);

        mSelect = a.getInteger(R.styleable.SuperCircleView_select, 7);
        mSelectAngle = a.getInteger(R.styleable.SuperCircleView_selec_angle, 3);

        mMinCircleColor = a.getColor(R.styleable.SuperCircleView_circle_color,
                context.getResources().getColor(R.color.white));
        mMaxCircleColor = a.getColor(R.styleable.SuperCircleView_max_circle_color,
                context.getResources().getColor(R.color.huise2));
        mRingNormalColor = a.getColor(R.styleable.SuperCircleView_ring_normal_color,
                context.getResources().getColor(R.color.huise));

        isShowSelect = a.getBoolean(R.styleable.SuperCircleView_is_show_select, false);
        mSelectRing = a.getInt(R.styleable.SuperCircleView_ring_color_select, 0);
        a.recycle();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setAntiAlias(true);
        this.setWillNotDraw(false);
        //color[0] = Color.parseColor("#8EE484");
        //color[1] = Color.parseColor("#97C0EF");
        //color[2] = Color.parseColor("#8EE484");
		color[0] = Color.parseColor("#000000");
        color[1] = Color.parseColor("#000000");
        color[2] = Color.parseColor("#000000");
		
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        mViewWidth = getMeasuredWidth();
        mViewHeight = getMeasuredHeight();
        mViewCenterX = mViewWidth / 2;
        mViewCenterY = mViewHeight / 2;
		mViewZeroAngleX = mViewCenterX + mMinRadio + (int)(mRingWidth / 2);
		mViewZeroAngleY = mViewCenterY;// + mMinRadio + (int)(mRingWidth / 2);
        mViewStartX = mViewZeroAngleX;
        mViewStartY = mViewZeroAngleY;
        Log.e(TAG, "mViewCenterX = "+mViewCenterX);
        Log.e(TAG, "mMinRadio = "+mMinRadio);
        Log.e(TAG, "mRingWidth = "+mRingWidth);
        Log.e(TAG, "mViewCenterY = "+mViewCenterY);
        Log.e(TAG, "mMinRadio = "+mMinRadio);
        Log.e(TAG, "mRingWidth = "+mRingWidth);
		Log.e(TAG, "mViewZeroAngleX = "+mViewZeroAngleX);
        Log.e(TAG, "mViewZeroAngleY = "+mViewZeroAngleY);
        mRectF = new RectF(mViewCenterX - mMinRadio - mRingWidth / 2,
                mViewCenterY - mMinRadio - mRingWidth / 2,
                mViewCenterX + mMinRadio + mRingWidth / 2,
                mViewCenterY + mMinRadio + mRingWidth / 2);
        mRingAngleWidth = (360 - mSelect * mSelectAngle) / mSelect;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        /**
         * 显示彩色断大于总共的段数是错误的
         */
        Log.e(TAG, "onDraw isShowSelect " + isShowSelect);

        if (isShowSelect && mSelectRing > mSelect) {
            return;
        }
        mPaint.setColor(mMaxCircleColor);
        canvas.drawCircle(mViewCenterX, mViewCenterY, mMinRadio + mRingWidth + 20, mPaint);
        mPaint.setColor(mMinCircleColor);
        canvas.drawCircle(mViewCenterX, mViewCenterY, mMinRadio, mPaint);
        //画默认圆环
        drawNormalRing(canvas);
        //画彩色圆环
        drawColorRing(canvas);

    }

    @Override
     public boolean onTouchEvent(MotionEvent event) {

        float Action_x = event.getX();
        float Action_y = event.getY();
		float distance;

		distance = (float)Math.sqrt((Action_x - mViewCenterX) * (Action_x - mViewCenterX) + 
			(Action_y - mViewCenterY) * (Action_y - mViewCenterY));
		Log.e(TAG, "distance " + distance);
		/*
        if (Action_x >= (mViewCenterX - mMinRadio - mRingWidth / 2 - 50)
            && Action_x <= (mViewCenterX + mMinRadio + mRingWidth / 2 + 50)
            && Action_y >= (mViewCenterY - mMinRadio - mRingWidth / 2 - 50)
            && Action_y <= (mViewCenterY + mMinRadio + mRingWidth / 2 + 50)) {
        */
        if ((distance <= (mMinRadio + mRingWidth / 2 + 100)) && distance >= 50){
			  
            Log.e(TAG, "MotionEvent " + event.getAction());
            Log.e(TAG, "Action_x " + Action_x);
            Log.e(TAG, "Action_y " + Action_y);

			Log.e(TAG, "mViewCenterX " + mViewCenterX);
            Log.e(TAG, "mMinRadio " + mMinRadio);
            Log.e(TAG, "mRingWidth / 2 " + mRingWidth / 2);

			Log.e(TAG, "mViewCenterY " + mViewCenterY);
            Log.e(TAG, "mMinRadio " + mMinRadio);
            Log.e(TAG, "mRingWidth / 2 " + mRingWidth / 2);
			
			
			//canvas.drawCircle(20, 60, circle);
			mViewStartX = (((Action_x - mViewCenterX) * (mMinRadio + mRingWidth / 2)) / distance)
                    + mViewCenterX;
	        mViewStartY = (((Action_y - mViewCenterY) * (mMinRadio + mRingWidth / 2)) / distance)
                    + mViewCenterY;
			Log.e(TAG, "mViewStartX " + mViewStartX);
			Log.e(TAG, "mViewStartY " + mViewStartY);
			this.invalidate();
        	
	        switch(event.getAction())
	        {
	            case MotionEvent.ACTION_DOWN:
	                //mViewStartX = Action_x;
	                //mViewStartY = Action_y;
	                mViewStartXRec = mViewStartX;
					mViewStartYRec = mViewStartY;
                    mAngle = 0;
                    direction = 0;
                    direction_init = 0xAA;
                    direction_init_cnt = 0;
                    direction_rec_0 = 0;
                    direction_rec_1 = 0;
                    direction_rec_2 = 0;
                    direction_rec_3 = 0;
                    //direction_array = null;
					Log.e(TAG, "--------------------------");
					// cos(<C) = (a*a + b*b -c*c) / (2*a*b)
	                AA = ((mViewStartX - mViewCenterX) * (mViewStartX - mViewCenterX) +
                            (mViewStartY - mViewCenterY) * (mViewStartY - mViewCenterY));
	                Log.e(TAG, "AA " + AA);
	                BB = ((mViewZeroAngleX - mViewCenterX) * (mViewZeroAngleX - mViewCenterX) +
                            (mViewZeroAngleY - mViewCenterY) * (mViewZeroAngleY - mViewCenterY));
	                Log.e(TAG, "BB " + BB);
	                CC = ((mViewStartX - mViewZeroAngleX) * (mViewStartX - mViewZeroAngleX) +
                            (mViewStartY - mViewZeroAngleY) * (mViewStartY - mViewZeroAngleY));
	                Log.e(TAG, "CC " + CC);
	                A = Math.sqrt(AA);
	                Log.e(TAG, "A" + A);
	                B = Math.sqrt(BB);
	                Log.e(TAG, "B" + B);
	                mStartAngle = (360 - (int)(Math.acos((AA+BB-CC)/(2*A*B)) * 57.3));
	                //mStartAngle = (int)(Math.acos((AA+BB-CC)/(2*A*B)) * 57.3);
	                Log.e(TAG, " mViewStartX " + mViewStartX + " mViewStartY " + mViewStartY +
	                        " mViewZeroAngleX " + mViewZeroAngleX + " mViewZeroAngleY " + mViewZeroAngleY +
	                        " mStartAngle " + mStartAngle);
	                //setSelect(mAngle);
	                break;
	            case MotionEvent.ACTION_UP:
					//textView = ((TextView)findViewById(R.id.tv)).setText("abc");;
	                //textView.setText(""+mAngle);
					mViewStartXRec = 0;
					mViewStartYRec = 0;
					mAngle = 0;
					mStartAngle = 0;
	                //setSelect(mAngle);
	                //mSuperCircleView.setSelect((int) (360 * (20 / 100f)));
	                break;
	            case MotionEvent.ACTION_MOVE:
					//mViewStopX = Action_x;
	                //mViewStopY = Action_y;
					Log.e(TAG, "###########################");
	                // cos(<C) = (a*a + b*b -c*c) / (2*a*b)
	                AA = ((mViewStartX - mViewCenterX) * (mViewStartX - mViewCenterX) +
                            (mViewStartY - mViewCenterY) * (mViewStartY - mViewCenterY));
	                Log.e(TAG, "AA " + AA);
	                BB = ((mViewStartXRec - mViewCenterX) * (mViewStartXRec - mViewCenterX) +
                            (mViewStartYRec - mViewCenterY) * (mViewStartYRec - mViewCenterY));
	                Log.e(TAG, "BB " + BB);
	                CC = ((mViewStartX - mViewStartXRec) * (mViewStartX - mViewStartXRec) +
                            (mViewStartY - mViewStartYRec) * (mViewStartY - mViewStartYRec));
	                Log.e(TAG, "CC " + CC);
	                A = Math.sqrt(AA);
	                Log.e(TAG, "A" + A);
	                B = Math.sqrt(BB);
	                Log.e(TAG, "B" + B);
	                
					direction = (((mViewStartXRec - mViewCenterX) * (mViewStartY - mViewCenterY) - 
						(mViewStartYRec - mViewCenterY) * (mViewStartX - mViewCenterX)) > 0) ? (0x0):(0xff);


                    if (direction_init_cnt < 4)
                    {
                        Log.e(TAG, "direction_init_cnt" + direction_init_cnt);
                        if (direction_init_cnt == 0)
                        {
                            direction_rec_0 = direction;
                            direction_init_cnt++;
                        }
                        else if (direction_init_cnt == 1)
                        {
                            direction_rec_1 = direction;
                            direction_init_cnt++;
                        }
                        else if (direction_init_cnt == 2)
                        {
                            direction_rec_2 = direction;
                            direction_init_cnt++;
                        }
                        else if (direction_init_cnt == 3)
                        {
                            direction_rec_3 = direction;
                            direction_init_cnt++;
                        }
                        else
                        {

                        }
                    }
                    else
                    {
                        Log.e(TAG, "direction_rec_0 = "+direction_rec_0);
                        Log.e(TAG, "direction_rec_1 = "+direction_rec_1);
                        Log.e(TAG, "direction_rec_2 = "+direction_rec_2);
                        Log.e(TAG, "direction_rec_3 = "+direction_rec_3);
                        if (direction_init == 0xAA)
                        {
                            if ((direction_rec_0 + direction_rec_1 +
                                    direction_rec_2 + direction_rec_3) >=510)
                            {
                                direction_init = 0xff;
                            }
                            else
                            {
                                direction_init = 0x00;
                            }
                            //direction_init = direction;
                        }
                    }
					if (direction_init == direction)
					{
						mAngle = (int)(Math.acos((AA+BB-CC)/(2*A*B)) * 57.3);
						Log.e("allwinnertech", "0'c");
					}
					else
					{
						mAngle = 360 - ((int)(Math.acos((AA+BB-CC)/(2*A*B)) * 57.3));
						Log.e("allwinnertech", "360'c");
					}
					/*Log.e(TAG, " mViewStartX " + mViewStartX + " mViewStartY " + mViewStartY +
	                        " mViewStartXRec " + mViewStartXRec + " mViewStartYRec " + mViewStartYRec +
	                        " mAngle " + mAngle + " direction " + direction);*/
					Log.e("allwinnertech", "mAngle " + mAngle + " direction " + direction + " direction_init " + direction_init);
	                break;
	            default:
	                break;
	        }
	        
		}
		else
	    {
	    	Log.e(TAG, "Filter MotionEvent " + event.getAction());
	        Log.e(TAG, "Filter Action_x " + Action_x);
	        Log.e(TAG, "Filter Action_y " + Action_y);
	    }
        return true;
    }


    /**
     * 画彩色圆环
     *
     * @param canvas
     */
    private void drawColorRing(Canvas canvas) {
        Paint ringColorPaint = new Paint(mPaint);
        ringColorPaint.setStyle(Paint.Style.STROKE);
        ringColorPaint.setStrokeWidth(mRingWidth);
        Log.e(TAG, "drawColorRing");
        ringColorPaint.setShader(new SweepGradient(mViewCenterX, mViewCenterX, color, null));

		canvas.drawCircle(mViewStartX, mViewStartY, 6, ringColorPaint);
		
		/*
        if (!isShowSelect) {
            canvas.drawArc(mRectF, mStartAngle, mSelectRing, false, ringColorPaint);
            return;
        }
        if (mSelect == mSelectRing && mSelectRing != 0 && mSelect != 0) {
            canvas.drawArc(mRectF, mStartAngle, 360, false, ringColorPaint);
        } else {
            Log.d(TAG, (mRingAngleWidth * mSelectRing + mSelectAngle + mSelectRing) + "");
            canvas.drawArc(mRectF, mStartAngle, mRingAngleWidth * mSelectRing + mSelectAngle * mSelectRing, false, ringColorPaint);
        }
        ringColorPaint.setShader(null);
        ringColorPaint.setColor(mMaxCircleColor);
        for (int i = 0; i < mSelectRing; i++) {
            canvas.drawArc(mRectF, mStartAngle + (i * mRingAngleWidth + (i) * mSelectAngle), mSelectAngle, false, ringColorPaint);
        }
        */
    }

    /**
     * 画默认圆环
     *
     * @param canvas
     */
    private void drawNormalRing(Canvas canvas) {
        Paint ringNormalPaint = new Paint(mPaint);
        ringNormalPaint.setStyle(Paint.Style.STROKE);
        ringNormalPaint.setStrokeWidth(mRingWidth);
        ringNormalPaint.setColor(mRingNormalColor);
        canvas.drawArc(mRectF, 90, 360, false, ringNormalPaint);
        Log.e(TAG, "drawNormalRing isShowSelect " + isShowSelect);
        if (!isShowSelect) {
            return;
        }
        ringNormalPaint.setColor(mMaxCircleColor);
        Log.e(TAG, "mSelect = "+mSelect);
        for (int i = 0; i < mSelect; i++) {
            canvas.drawArc(mRectF, 90 + (i * mRingAngleWidth + (i) * mSelectAngle), mSelectAngle, false, ringNormalPaint);
        }
    }

    /**
     * 显示几段
     *
     * @param i
     */
    public void setSelect(int i) {
        Log.e(TAG, "setSelect");
        this.mSelectRing = i;
        this.invalidate();
    }

    /**
     * 断的总数
     *
     * @param i
     */
    public void setSelectCount(int i) {
        this.mSelect = i;
    }


    /**
     * 是否显示断
     *
     * @param b
     */
    public void setShowSelect(boolean b) {
        this.isShowSelect = b;
        Log.e(TAG, "setShowSelect");
    }


    public void setColor(int[] color) {
        this.color = color;
    }
}
