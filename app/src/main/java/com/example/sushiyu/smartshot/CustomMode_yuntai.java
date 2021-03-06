package com.example.sushiyu.smartshot;

import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.animation.ValueAnimator;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

//import com.android.graphics.CanvasView;

public class CustomMode_yuntai extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final String CUSTOMMODE_YUNTAI_TAG = "mcfire_custommode_yuntai";
    public static final String TAG = "SuperCircleView";
    public static int max_shot_times_abpoint;/*save after abpoint setting*/
    public static int max_shot_times;/*received from mcu*/
    public BluetoothLeService mBluetoothLeService;
    public static float mViewStartXRec;
    public static float mViewStartYRec;
    public static float mViewStartXPrev;
    public static float mViewStartYPrev;
    public static float mViewCurrentX; //圆弧起始点X
    public static float mViewCurrentY; //圆弧起始点Y
    public static float mViewStopX; //圆弧终点X
    public static float mViewStopY; //圆弧终点Y
    public static boolean ui_init = false; //圆弧起始点X

    public static double AA; //余弦定理A边平方
    public static double BB; //余弦定理B边平方
    public static double CC; //余弦定理C边平方

    public static double A; //余弦定理A边
    public static double B; //余弦定理B边
    public static int mSumAngle;
    public static int mSumAngle_final;
    public static int mSumAngle_curr;
    public static int mViewZeroAngleX;   //view宽的中心点
    public static int mViewZeroAngleY;   //view高的中心点
    private int mAngle; //余弦定理B边
    private int direction = 0x00;
    private int direction_final = 0x0;
    private int Angle_delta;
    private boolean mConnected = false;
    boolean connect_status_bit=false;
    private EditText EtJiaodu;
    private TextView Tvpanoramic_yunxing_time;
    private TextView TvJiaodu;
    private int jiaodu_num;
    private Switch switch_direction;
    private String mDeviceName;
    private String mDeviceAddress;
    private ImageButton custommode_btn_start;
    private boolean custommode_start_press_flag = true;
    private boolean get_param_success;
    private int yunxing_hour;
    private int yunxing_minute;
    private int yunxing_second;
    private boolean screen_toggle;

    private boolean debug = false;
    SuperCircleView mSuperCircleView;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_custommode_yuntai);
        Calendar c = Calendar.getInstance();

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        String month = Integer.toString(c.get(Calendar.MONTH));
        String day = Integer.toString(c.get(Calendar.DAY_OF_MONTH));
        String hour = Integer.toString(c.get(Calendar.HOUR_OF_DAY));
        String min = Integer.toString(c.get(Calendar.MINUTE));
        Log.e(CUSTOMMODE_YUNTAI_TAG, "Calendar获取当前日期"+"2018"+"年"+month+"月"+day+"日"+hour+":"+min);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.getMenu().removeItem(R.id.abpoint);
        navigationView.getMenu().removeItem(R.id.vedio_shoot);
        navigationView.getMenu().add(MainActivity.btn_quanjing, MainActivity.btn_quanjing,
                MainActivity.btn_quanjing, "视频拍摄");
        //navigationView.getMenu().add(MainActivity.btn_zidingyi, MainActivity.btn_zidingyi,
        //        MainActivity.btn_zidingyi, "自定义拍摄");

        if (!debug) {
            boolean sg;
            Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
            sg = bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        }
        //timer.schedule(task, 1000, 1000);

        //this.canvas = (CanvasView)this.findViewById(R.id.canvas);

        textView = (TextView) findViewById(R.id.tv);

        mSuperCircleView = (SuperCircleView) findViewById(R.id.superview);

        mSuperCircleView.setShowSelect(false);


	mViewZeroAngleX = mSuperCircleView.mViewCenterX + mSuperCircleView.mMinRadio + (int)(mSuperCircleView.mRingWidth / 2);
	mViewZeroAngleY = mSuperCircleView.mViewCenterY;// + mMinRadio + (int)(mRingWidth / 2);
	mViewCurrentX = mViewZeroAngleX;
        mViewCurrentY = mViewZeroAngleY;
	Log.e(TAG, "custom init done");
	Log.e(TAG, " "+mViewZeroAngleX+" "+mViewZeroAngleY+" "+mViewCurrentX+" "+mViewCurrentY);
	ui_init = true;        	
        Log.e(CUSTOMMODE_YUNTAI_TAG, "mViewCenterX = "+mSuperCircleView.mViewCenterX);
        Log.e(CUSTOMMODE_YUNTAI_TAG, "mMinRadio = "+mSuperCircleView.mMinRadio);
        Log.e(CUSTOMMODE_YUNTAI_TAG, "mRingWidth = "+mSuperCircleView.mRingWidth);
        Log.e(CUSTOMMODE_YUNTAI_TAG, "mViewCenterY = "+mSuperCircleView.mViewCenterY);
        Log.e(CUSTOMMODE_YUNTAI_TAG, "mMinRadio = "+mSuperCircleView.mMinRadio);
        Log.e(CUSTOMMODE_YUNTAI_TAG, "mRingWidth = "+mSuperCircleView.mRingWidth);
	Log.e(CUSTOMMODE_YUNTAI_TAG, "mViewZeroAngleX = "+mViewZeroAngleX);
        Log.e(CUSTOMMODE_YUNTAI_TAG, "mViewZeroAngleY = "+mViewZeroAngleY);

        mSuperCircleView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                String tx_string;
                float Action_x = motionEvent.getX();
                float Action_y = motionEvent.getY();
                float distance;

                distance = (float) Math.sqrt((Action_x - mSuperCircleView.mViewCenterX) * (Action_x - mSuperCircleView.mViewCenterX) +
                        (Action_y - mSuperCircleView.mViewCenterY) * (Action_y - mSuperCircleView.mViewCenterY));
                //Log.e(CUSTOMMODE_YUNTAI_TAG, "distance " + distance);
		/*
        if (Action_x >= (mViewCenterX - mMinRadio - mRingWidth / 2 - 50)
            && Action_x <= (mViewCenterX + mMinRadio + mRingWidth / 2 + 50)
            && Action_y >= (mViewCenterY - mMinRadio - mRingWidth / 2 - 50)
            && Action_y <= (mViewCenterY + mMinRadio + mRingWidth / 2 + 50)) {
        */
                if ((distance <= (mSuperCircleView.mMinRadio + mSuperCircleView.mRingWidth / 2 + 100)) && distance >= 50) {
			Log.e(TAG, "MotionEvent " + motionEvent.getAction());
			Log.e(TAG, "Action_x " + Action_x);
			Log.e(TAG, "Action_y " + Action_y);

			Log.e(TAG, "mViewCenterX " + mSuperCircleView.mViewCenterX);
			Log.e(TAG, "mMinRadio " + mSuperCircleView.mMinRadio);
			Log.e(TAG, "mRingWidth / 2 " + mSuperCircleView.mRingWidth / 2);

			Log.e(TAG, "mViewCenterY " + mSuperCircleView.mViewCenterY);
			Log.e(TAG, "mMinRadio " + mSuperCircleView.mMinRadio);
			Log.e(TAG, "mRingWidth / 2 " + mSuperCircleView.mRingWidth / 2);

                    //canvas.drawCircle(20, 60, circle);

			mViewStartXPrev = mViewCurrentX;
			mViewStartYPrev = mViewCurrentY;
			mViewCurrentX = (((Action_x - mSuperCircleView.mViewCenterX) * (mSuperCircleView.mMinRadio + mSuperCircleView.mRingWidth / 2)) / distance)
				+ mSuperCircleView.mViewCenterX;
			mViewCurrentY = (((Action_y - mSuperCircleView.mViewCenterY) * (mSuperCircleView.mMinRadio + mSuperCircleView.mRingWidth / 2)) / distance)
				+ mSuperCircleView.mViewCenterY;
			//Log.e(TAG, "mViewCurrentX " + mViewCurrentX);
			//Log.e(TAG, "mViewCurrentY " + mViewCurrentY);
			mSuperCircleView.invalidate();
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            //mViewCurrentX = Action_x;
                            //mViewCurrentY = Action_y;
                            Log.e(TAG, "---------------------------");
                            mViewStartXRec = mViewCurrentX;
                            mViewStartYRec = mViewCurrentY;
                            mSumAngle = 0;
                            direction = 0;
			    direction_final = 0x00; 
                            break;
                        case MotionEvent.ACTION_MOVE:
                            //mViewStopX = Action_x;
                            //mViewStopY = Action_y;
                            Log.e(TAG, "###########################");
                            // cos(<C) = (a*a + b*b -c*c) / (2*a*b)
                            AA = ((mViewCurrentX - mSuperCircleView.mViewCenterX) * (mViewCurrentX - mSuperCircleView.mViewCenterX) +
                                    (mViewCurrentY - mSuperCircleView.mViewCenterY) * (mViewCurrentY - mSuperCircleView.mViewCenterY));
                            //Log.e(TAG, "AA " + AA);
                            BB = ((mViewStartXPrev - mSuperCircleView.mViewCenterX) * (mViewStartXPrev - mSuperCircleView.mViewCenterX) +
                                    (mViewStartYPrev - mSuperCircleView.mViewCenterY) * (mViewStartYPrev - mSuperCircleView.mViewCenterY));
                            //Log.e(TAG, "BB " + BB);
                            CC = ((mViewCurrentX - mViewStartXPrev) * (mViewCurrentX - mViewStartXPrev) +
                                    (mViewCurrentY - mViewStartYPrev) * (mViewCurrentY - mViewStartYPrev));
                            //Log.e(TAG, "CC " + CC);
                            A = Math.sqrt(AA);
                            //Log.e(TAG, "A" + A);
                            B = Math.sqrt(BB);
                            //Log.e(TAG, "B" + B);

                            direction = (((mViewStartXPrev - mSuperCircleView.mViewCenterX) * (mViewCurrentY - mSuperCircleView.mViewCenterY) -
                                    (mViewStartYPrev - mSuperCircleView.mViewCenterY) * (mViewCurrentX - mSuperCircleView.mViewCenterX)) > 0) ? (0x0) : (0xff);

                            Angle_delta = (int) (Math.acos((AA + BB - CC) / (2 * A * B)) * 57.3);
                            //Log.e("allwinnertech", "mAngle = "+mAngle);

                            if (0x0 == direction) {
                                mSumAngle = mSumAngle + Angle_delta;
                            } else {
                                mSumAngle = mSumAngle - Angle_delta;
                            }
			/*Log.e(TAG, " mViewCurrentX " + mViewCurrentX + " mViewCurrentY " + mViewCurrentY +
	                        " mViewStartXRec " + mViewStartXRec + " mViewStartYRec " + mViewStartYRec +
	                        " mAngle " + mAngle + " direction " + direction);*/
                            Log.e("allwinnertech", "mSumAngle " + mSumAngle + " direction " + direction + " direction_final " + direction_final);

                            if (mSumAngle >= 0) {
                                mSumAngle_curr = mSumAngle;
                                Log.e("sushiyu1", "mSumAngle_curr " + mSumAngle_curr);
                            } else {
                                mSumAngle_curr = -mSumAngle;
                                //direction_final = 0xff;
                                Log.e("sushiyu1", "mSumAngle_curr " + mSumAngle_curr);
                            }
                            String str_direction;
                            if (direction == 0xFF) {
                                str_direction = "FF";
                                //textView.setText("逆时针:" + mSumAngle_final);
                                textView.setText("" + mSumAngle_curr);
                            } else {
                                str_direction = "00";
                                //textView.setText("顺时针:" + mSumAngle_final);
                                textView.setText("" + mSumAngle_curr);
                            }

                            tx_string = "0093020401" +
                                    //String.format("%02x", (mSuperCircleView.mAngle % 256)) +
                                    //String.format("%02x", (mSuperCircleView.mAngle / 256)) +
                                    String.format("%02x", (mSumAngle_curr % 256)) +
                                    String.format("%02x", (mSumAngle_curr / 256)) +
                                    str_direction;
                            Log.e("sushiyu1", "mSumAngle 0" + mSumAngle_curr);
                            Log.e("sushiyu1", "String.format 1" + String.format("%02x", (mSumAngle_curr % 256)));
                            Log.e("sushiyu1", "String.format 2" + String.format("%02x", (mSumAngle_curr / 256)));
                            if (!connect_status_bit)
                                return false;
                            Log.e(CUSTOMMODE_YUNTAI_TAG, tx_string);
                            mBluetoothLeService.txxx(tx_string);
                            break;
                        case MotionEvent.ACTION_UP:
                            //textView = ((TextView)findViewById(R.id.tv)).setText("abc");;
                            //textView.setText(""+mAngle);

                            if (mSumAngle >= 360) {
                                mSumAngle = 360;
                            }

                            if (mSumAngle < -360) {
                                mSumAngle = -360;
                            }
                            if (mSumAngle >= 0) {
                                mSumAngle_final = mSumAngle;
                                direction_final = 0x0;
                                Log.e("sushiyu1", "mSumAngle_final " + mSumAngle_final);
                            } else {
                                mSumAngle_final = -mSumAngle;
                                direction_final = 0xff;
                                Log.e("sushiyu1", "mSumAngle_final " + mSumAngle_final);
                            }

                            if (direction_final == 0xFF) {
                                str_direction = "FF";
                                textView.setText("逆时针:" + mSumAngle_final);
                            } else {
                                str_direction = "00";
                                textView.setText("顺时针:" + mSumAngle_final);
                            }
                            mViewStartXRec = 0;
                            mViewStartYRec = 0;
			    direction_final = 0x00; 
                            //mSuperCircleView.setSelect((int) (360 * (20 / 100f)));
                            tx_string = "0093020403000000";
                            Log.e(CUSTOMMODE_YUNTAI_TAG, tx_string);
                            if (!connect_status_bit)
                                return false;
                            mBluetoothLeService.txxx(tx_string);
                            break;
                        default:
                            break;
                    }
                }

                //textView.setText(mSuperCircleView.mAngle);
                return true;
	        }

            });
        //mSuperCircleView.setSelect((int) (360 * (20 / 100f)));
        /*
        ValueAnimator valueAnimator = ValueAnimator.ofInt(0, 100);

        valueAnimator.setTarget(textView);

        valueAnimator.setDuration(2000);

        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

                int i = Integer.valueOf(String.valueOf(animation.getAnimatedValue()));

                textView.setText(i + "");

                Log.e(CUSTOMMODE_YUNTAI_TAG, "select "+i);

                mSuperCircleView.setSelect((int) (360 * (i / 100f)));

            }
        });
        valueAnimator.start();
        */
        custommode_btn_start = (ImageButton) findViewById(R.id.img_btn_custommode_start);
        custommode_btn_start.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {

                    if (custommode_start_press_flag) {
                        custommode_btn_start.setBackgroundResource(R.drawable.stop);
                        custommode_start_press_flag = false;

                        String tx_string;
						tx_string="00930204"+"02"+"FF"+"0000";
                        //tx_string="0093010206ff0000";
                        if(!connect_status_bit)
                            return false;
                        mBluetoothLeService.txxx(tx_string);
                        Log.e(CUSTOMMODE_YUNTAI_TAG, tx_string);

                    }else{
                        custommode_btn_start.setBackgroundResource(R.drawable.start);
                        custommode_start_press_flag = true;

                        String tx_string;
						tx_string="00930204"+"02"+"00"+"0000";
                        //tx_string="0093010206000000";
                        if(!connect_status_bit)
                            return false;
                        mBluetoothLeService.txxx(tx_string);
                        Log.e(CUSTOMMODE_YUNTAI_TAG, tx_string);
                    }
                }else if(motionEvent.getAction() == MotionEvent.ACTION_UP){

                }
                return false;
            }
        });
        //updateConnectionState(R.string.connecting);
        Log.e(TAG, "onCreate end");
        Log.e(CUSTOMMODE_YUNTAI_TAG, "连接中");
    }


    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.e(CUSTOMMODE_YUNTAI_TAG, "delayshot onServiceConnected");
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(CUSTOMMODE_YUNTAI_TAG, "Unable to initialize Bluetooth");
                finish();
            }
            boolean result;
            //result = mBluetoothLeService.connect(mDeviceAddress);
            //Log.e(CUSTOMMODE_YUNTAI_TAG, "delayshot result "+result);
            //if (result)
            {
                mConnected = true;
                connect_status_bit=true;
                //timer.cancel();
                Log.e(CUSTOMMODE_YUNTAI_TAG, "custommode connected!");
                mBluetoothLeService.txxx("0003020300000000");
				Log.e(CUSTOMMODE_YUNTAI_TAG, "tx 0003020300000000");
                delay(20);

            }
            // Automatically connects to the device upon successful start-up initialization.
            /*
            if (mBluetoothLeService.isconnect())
            {
                connect_status_bit=true;
                mConnected = true;
                Log.e(CUSTOMMODE_YUNTAI_TAG, "delayshot connected!");
            }
            else
            {
                boolean result;
                result = mBluetoothLeService.connect(mDeviceAddress);
                Log.e(CUSTOMMODE_YUNTAI_TAG, "delayshot connect "+result);
                if (result)
                {
                    mConnected = true;
                    connect_status_bit=true;
                    timer.cancel();
                    Log.e(CUSTOMMODE_YUNTAI_TAG, "delayshot connected!");
                    mBluetoothLeService.txxx("0003010200000000");
                    Log.e(CUSTOMMODE_YUNTAI_TAG, "0003010200000000");
                }
            }*/
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.e(CUSTOMMODE_YUNTAI_TAG, "delayshot BroadcastReceiver");
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                connect_status_bit=true;
                Log.e(CUSTOMMODE_YUNTAI_TAG, "ACTION_GATT_CONNECTED");
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Log.e(CUSTOMMODE_YUNTAI_TAG, "ACTION_GATT_DISCONNECTED");
                MainActivity.device_mode = 1;/*防止重连后多个模式重复显示在左侧菜单*/
                connect_status_bit=true;
                mConnected = true;
                //mBluetoothLeService.disconnect();
                //unregisterReceiver(mGattUpdateReceiver);
                unbindService(mServiceConnection);
                mBluetoothLeService = null;
                timer.cancel();
                timer=null;
                Intent intent1 = new Intent(CustomMode_yuntai.this,
                        MainActivity.class);
                startActivity(intent1);
                connect_status_bit=false;
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                Log.e(CUSTOMMODE_YUNTAI_TAG, "ACTION_GATT_SERVICES_DISCOVERED");
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                String str = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);

                Log.e(CUSTOMMODE_YUNTAI_TAG, "Receive Data : "+str);
                get_param_success = true;

            }
        }
    };

    // If a given GATT characteristic is selected, check for supported features.  This sample
    // demonstrates 'Read' and 'Notify' features.  See
    // http://d.android.com/reference/android/bluetooth/BluetoothGatt.html for the complete
    // list of supported characteristic features.
    private final ExpandableListView.OnChildClickListener servicesListClickListner =
            new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                            int childPosition, long id) {
                    return false;
                }
            };

    private void clearUI() {
        //mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
        //mDataField.setText(R.string.no_data);
    }

    Timer timer = new Timer();

    public void delay(int ms){
        try {
            Thread.currentThread();
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    Handler handler = new Handler() {
        public void handleMessage(Message msg) {

            if (msg.what == 1) {
                //Log.e("delayshot", "handleMessage");
                //tvShow.setText(Integer.toString(i++));
                //scanLeDevice(true);
                if (mBluetoothLeService != null) {
                    //Log.e("delayshot", "mBluetoothLeService != null");
                    if( mConnected==false )
                    {
                        Log.e(CUSTOMMODE_YUNTAI_TAG,"mConnected==false");
                        //updateConnectionState(R.string.connecting);
                        //final boolean result = mBluetoothLeService.connect(mDeviceAddress);
                        //Log.e(CUSTOMMODE_YUNTAI_TAG, "Connect request result=" + result);
                    }
                }
            }
            super.handleMessage(msg);
        };
    };
    TimerTask task = new TimerTask() {

        @Override
        public void run() {
            //Log.e("delayshot", "BBB");
            // ÐèÒª×öµÄÊÂ:·¢ËÍÏûÏ¢
            //Log.e("delayshot", "timer task run");
            Message message = new Message();
            message.what = 1;
            handler.sendMessage(message);
            if (!get_param_success)
            {
                if (connect_status_bit)
                {
                    //Log.e(CUSTOMMODE_YUNTAI_TAG, "retry");
                    //mBluetoothLeService.txxx("0003020300000000");
					//Log.e(CUSTOMMODE_YUNTAI_TAG, "tx 0003020300000000");
                }
            }
        }
    };

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //mConnectionState.setText(resourceId);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.vedio_shot, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.vedio_shoot) {

            Intent intent1 = new Intent(CustomMode_yuntai.this,
                    VedioShot_yuntai.class);
            startActivity(intent1);
            // Handle the camera action
        } else if (id == R.id.delay_shoot) {
            Log.e(CUSTOMMODE_YUNTAI_TAG, "delay_shoot");
            timer.cancel();
            Intent intent1 = new Intent(CustomMode_yuntai.this,
                    DelayShot_yuntai.class);
            startActivity(intent1);
        } else if (id == R.id.abpoint) {
            Log.e(CUSTOMMODE_YUNTAI_TAG, "nav_slideshow");
            Intent intent1 = new Intent(CustomMode_yuntai.this,
                    ABpoint.class);
            startActivity(intent1);
        }else if (id == MainActivity.btn_quanjing) {
            Log.e(CUSTOMMODE_YUNTAI_TAG, "btn_quanjing");
            Intent intent1 = new Intent(CustomMode_yuntai.this,
                    PanoramicShoot_yuntai.class);
            intent1.putExtra(CustomMode_yuntai.EXTRAS_DEVICE_NAME,
                    mDeviceName);
            intent1.putExtra(MainActivity.EXTRAS_DEVICE_ADDRESS,
                    mDeviceAddress);
            startActivity(intent1);
        }else if (id == MainActivity.btn_zidingyi) {
            Log.e(CUSTOMMODE_YUNTAI_TAG, "btn_zidingyi");

        }else if (id == R.id.scan) {
            /*任意界面只要按左菜单扫描按钮就会重新扫描*/
            //if (!connect_status_bit)
            {
                Intent intent1 = new Intent(CustomMode_yuntai.this,
                        MainActivity.class);
                startActivity(intent1);
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void shiftLanguage(String sta){

        if(sta.equals("zh")){
            Locale.setDefault(Locale.US);
            Configuration config = getBaseContext().getResources().getConfiguration();
            config.locale = Locale.US;
            getBaseContext().getResources().updateConfiguration(config
                    , getBaseContext().getResources().getDisplayMetrics());
            refreshSelf();
        }else{
            Locale.setDefault(Locale.CHINESE);
            Configuration config = getBaseContext().getResources().getConfiguration();
            config.locale = Locale.CHINESE;
            getBaseContext().getResources().updateConfiguration(config
                    , getBaseContext().getResources().getDisplayMetrics());
            refreshSelf();
        }
    }
    //refresh self
    public void refreshSelf(){
        Intent intent=new Intent(this,CustomMode_yuntai.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;

        Log.e(CUSTOMMODE_YUNTAI_TAG, "delayshot displayGattServices");
        if( gattServices.size()>0&&mBluetoothLeService.get_connected_status( gattServices )>=4 )
        {
            if( connect_status_bit )
            {
                mConnected = true;
                mBluetoothLeService.enable_JDY_ble(true);
                try {
                    Thread.currentThread();
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mBluetoothLeService.enable_JDY_ble(true);
            }else{
                Log.e(CUSTOMMODE_YUNTAI_TAG, "delayshot displayGattServices disconnect");
            }
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return false;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        screen_toggle = true;
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //text_screen.append("\n 当前屏幕为横屏");
        } else {
            //text_screen.append("\n 当前屏幕为竖屏");
        }
        super.onConfigurationChanged(newConfig);
        //Log.e("TAG", "onConfigurationChanged");
        //  setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);  //设置横屏
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.e(CUSTOMMODE_YUNTAI_TAG, "delayshot onResume");
        Log.e(TAG, "delayshot onResume");
        if (BluetoothLeService.mConnectionState == BluetoothLeService.STATE_DISCONNECTED &&
                        MainActivity.connect_status_bit == true)
        {
                Log.e(CUSTOMMODE_YUNTAI_TAG, "Connection Lost");
                Log.e(CUSTOMMODE_YUNTAI_TAG, "ACTION_GATT_DISCONNECTED");
                MainActivity.device_mode = 1;/*防止重连后多个模式重复显示在左侧菜单*/
                mConnected = false;
                //mBluetoothLeService.disconnect(); //20181111
                //unregisterReceiver(mGattUpdateReceiver);
                unbindService(mServiceConnection);
                mBluetoothLeService = null;
                timer.cancel();
                timer=null;
                Intent intent1 = new Intent(CustomMode_yuntai.this,
                                MainActivity.class);
                startActivity(intent1);
                connect_status_bit=false;
        }

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {

            //final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            //Log.e(CUSTOMMODE_YUNTAI_TAG, "delayshot onResume connect " + result);
        }
    }

    @Override
    protected void onPause() {
        Log.e(CUSTOMMODE_YUNTAI_TAG, "delayshot onPause");
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        Log.e(CUSTOMMODE_YUNTAI_TAG, "delayshot onDestroy");
        super.onDestroy();
        mBluetoothLeService.disconnect();
        unbindService(mServiceConnection);
        unregisterReceiver(mGattUpdateReceiver);
        mBluetoothLeService = null;
        timer.cancel();
        timer=null;

    }


    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        Log.e(CUSTOMMODE_YUNTAI_TAG, "IntentFilter");
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
}
