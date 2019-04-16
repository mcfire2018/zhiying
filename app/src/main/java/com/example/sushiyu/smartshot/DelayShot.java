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
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.format.Time;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class DelayShot extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final String DELAYSHOT_TAG = "mcfire_delayshot";
    public static int max_shot_times_abpoint;/*save after abpoint setting*/
    public static int max_shot_times;/*received from mcu*/
    private String mDeviceName;
    private String mDeviceAddress;
    public BluetoothLeService mBluetoothLeService;
    private Intent connect_intent;
    private TextView mConnectionState;
    private boolean mConnected = false;
    boolean connect_status_bit=false;
    private Handler mHandler;
    private EditText EtShotTimes;
    private TextView TvZhugeTime;
    private TextView TvBaoguangTime;
    private TextView TvShootTimes;
    private int shoot_times;
    private TextView TvShottimeTotal;
    private TextView TvRemainTimes;
    private Switch switch_direction;
    private Switch switch_dingdian;
    private ImageButton delayshot_btn_left;
    private ImageButton delayshot_btn_right;
    private ImageButton delayshot_btn_start;
    private boolean delayshot_start_press_flag = false;
    private boolean get_param_success;
    private String StrParam[] = new String[8];
    private int zhuge_hour;
    private int zhuge_minute;
    private int zhuge_second;
    private int baoguang_hour;
    private int baoguang_minute;
    private int baoguang_second;
    private boolean screen_toggle;
    private Intent gattServiceIntent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        setContentView(R.layout.activity_delay_shot);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        /*if (DelayShot.max_shot_times == 0)
        {
            DelayShot.max_shot_times = 100;
        }
        Log.e(DELAYSHOT_TAG, "max shot time"+ DelayShot.max_shot_times);
        */
        Calendar c = Calendar.getInstance();

        String month = Integer.toString(c.get(Calendar.MONTH));
        String day = Integer.toString(c.get(Calendar.DAY_OF_MONTH));
        String hour = Integer.toString(c.get(Calendar.HOUR_OF_DAY));
        String min = Integer.toString(c.get(Calendar.MINUTE));
        Log.e(DELAYSHOT_TAG, "Calendar获取当前日期"+"2018"+"年"+month+"月"+day+"日"+hour+":"+min);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        boolean sg;
        gattServiceIntent = new Intent(this, BluetoothLeService.class);
        sg = bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        timer.schedule(task, 1000, 1000);

        TvZhugeTime = (TextView) findViewById(R.id.delayshoot_zhuge_tv);
        TvZhugeTime.setText("00  :  00  :  00");
        TvZhugeTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DateTimepickerDialog datetimedialog =
                        new DateTimepickerDialog(DelayShot.this, zhuge_hour, zhuge_minute, zhuge_second);
                datetimedialog.setOnDateTimeSetListener(new DateTimepickerDialog.OnDateTimeSetListener() {
                    public void OnDateTimeSet(DialogInterface dialog, int g_hour, int g_minute, int g_second) {
                        TvZhugeTime.setText(
                                String.format("%02d", g_hour)+"  :  "+
                                String.format("%02d", g_minute)+"  :  "+
                                String.format("%02d", g_second));
                        zhuge_hour = g_hour;
                        zhuge_minute = g_minute;
                        zhuge_second = g_second;
                        String tx_string;
                        tx_string="0093010201"+
                                String.format("%02x", zhuge_second)+
                                String.format("%02x", zhuge_minute)+
                                String.format("%02x", zhuge_hour);
                        if(!MainActivity.connect_status_bit)
                            return;
                        mBluetoothLeService.txxx(tx_string);
                        Log.e(DELAYSHOT_TAG, tx_string);
                    }
                });
                datetimedialog.show();


            }
        });

        TvBaoguangTime = (TextView) findViewById(R.id.delayshoot_baoguang_tv);
        TvBaoguangTime.setText("00  :  00  :  00");
        TvBaoguangTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DateTimepickerDialog datetimedialog =
                        new DateTimepickerDialog(DelayShot.this,baoguang_hour, baoguang_minute, baoguang_second);
                datetimedialog.setOnDateTimeSetListener(new DateTimepickerDialog.OnDateTimeSetListener() {
                    public void OnDateTimeSet(DialogInterface dialog, int g_hour, int g_minute, int g_second) {
                        TvBaoguangTime.setText(
                                String.format("%02d", g_hour)+"  :  "+
                                String.format("%02d", g_minute)+"  :  "+
                                String.format("%02d", g_second));
                        baoguang_hour = g_hour;
                        baoguang_minute = g_minute;
                        baoguang_second = g_second;
                        String tx_string;
                        tx_string="0093010202"+
                                String.format("%02x", baoguang_second)+
                                String.format("%02x", baoguang_minute)+
                                String.format("%02x", baoguang_hour);
                        if(!MainActivity.connect_status_bit)
                            return;
                        mBluetoothLeService.txxx(tx_string);
                        Log.e(DELAYSHOT_TAG, tx_string);
                    }
                });
                datetimedialog.show();
            }
        });


        TvShootTimes = (TextView) findViewById(R.id.shoot_times);
        TvShootTimes.setText("00");
        TvShootTimes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EtShotTimes = new EditText(DelayShot.this);
                EtShotTimes.setInputType(InputType.TYPE_CLASS_NUMBER);
                EtShotTimes.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }
                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }
                    @Override
                    public void afterTextChanged(Editable editable) {
                        Log.e(DELAYSHOT_TAG, "EtShotTimes afterTextChanged "+editable.toString()+"a");
                        if (editable.toString().equals(""))
                        {
                            shoot_times = 0;
                            return;
                        }
                        shoot_times = Integer.valueOf(editable.toString(),10);
                        if (shoot_times > 65535)
                        {
                            shoot_times = 65535;
                            EtShotTimes.setText(""+shoot_times);
                        }
                        Log.e(DELAYSHOT_TAG, "EtShotTimes afterTextChanged "+shoot_times);
                    }
                });
                AlertDialog dialog = new AlertDialog.Builder(DelayShot.this)
                        .setTitle("拍摄张数")
                        .setView(EtShotTimes)
                        //.setView(new EditText(DelayShot.this))
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.e(DELAYSHOT_TAG, "TvShootTimes onClick");


                                String tx_string;
                                String str_shoot_times_high;
                                String str_shoot_times_low;

                                str_shoot_times_high = String.format("%02x", (shoot_times / 256));
                                str_shoot_times_low = String.format("%02x", (shoot_times % 256));
                                tx_string="0093010203"+str_shoot_times_low + str_shoot_times_high+"00";
                                TvShootTimes.setText(""+shoot_times);
                                if(!MainActivity.connect_status_bit)
                                    return;
                                mBluetoothLeService.txxx(tx_string);
                                Log.e(DELAYSHOT_TAG, tx_string);

                            }
                        })
                        .create();
                dialog.show();

            }
        });

        TvShottimeTotal = (TextView) findViewById(R.id.shottime_total);
        TvRemainTimes = (TextView) findViewById(R.id.shot_times_remain);
        switch_direction = (Switch) findViewById(R.id.switch2_direction);
        switch_dingdian = (Switch) findViewById(R.id.switch3_dingdian);



        switch_direction.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    String tx_string;
                    tx_string="0093010205000000";
                    if(!MainActivity.connect_status_bit)
                        return ;
                    mBluetoothLeService.txxx(tx_string);
                    Log.e(DELAYSHOT_TAG, tx_string);
                }else {
                    String tx_string;
                    tx_string="0093010205ff0000";
                    if(!MainActivity.connect_status_bit)
                        return ;
                    mBluetoothLeService.txxx(tx_string);
                    Log.e(DELAYSHOT_TAG, tx_string);
                }
            }
        });

        switch_dingdian.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    String tx_string;
                    tx_string="0093010204ff0000";
                    if(!MainActivity.connect_status_bit)
                        return ;
                    mBluetoothLeService.txxx(tx_string);
                    Log.e(DELAYSHOT_TAG, tx_string);
                }else {
                    String tx_string;
                    tx_string="0093010204000000";
                    if(!MainActivity.connect_status_bit)
                        return ;
                    mBluetoothLeService.txxx(tx_string);
                    Log.e(DELAYSHOT_TAG, tx_string);
                }
            }
        });


        delayshot_btn_start = (ImageButton) findViewById(R.id.img_btn_delayshot_start);
        delayshot_btn_start.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {

                    if (delayshot_start_press_flag) {
                        delayshot_btn_start.setBackgroundResource(R.drawable.stop);
                        delayshot_start_press_flag = false;

                        String tx_string;
                        tx_string="0093010206ff0000";
                        if(!MainActivity.connect_status_bit)
                            return false;
                        mBluetoothLeService.txxx(tx_string);
                        Log.e(DELAYSHOT_TAG, tx_string);

                    }else{
                        delayshot_btn_start.setBackgroundResource(R.drawable.start);
                        delayshot_start_press_flag = true;

                        String tx_string;
                        tx_string="0093010206000000";
                        if(!MainActivity.connect_status_bit)
                            return false;
                        mBluetoothLeService.txxx(tx_string);
                        Log.e(DELAYSHOT_TAG, tx_string);
                    }
                }else if(motionEvent.getAction() == MotionEvent.ACTION_UP){

                }
                return false;
            }
        });
        //updateConnectionState(R.string.connecting);
        Log.e(DELAYSHOT_TAG, "连接中");
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.e(DELAYSHOT_TAG, "delayshot onServiceConnected");
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(DELAYSHOT_TAG, "Unable to initialize Bluetooth");
                finish();
            }
            boolean result;
            //result = mBluetoothLeService.connect(mDeviceAddress);
            //Log.e(DELAYSHOT_TAG, "delayshot result "+result);
            //if (result)
            {
                mConnected = true;
                //connect_status_bit=true;
                //timer.cancel();
                if (MainActivity.connect_status_bit)
                {
                        String tx_string = "0093050102000000";
                        Log.e(DELAYSHOT_TAG, "abc");
                        if(MainActivity.connect_status_bit)
                        {
                                Log.e(DELAYSHOT_TAG, tx_string);
                                mBluetoothLeService.txxx(tx_string);
                        }
                        Log.e(DELAYSHOT_TAG, "delayshot connected!");
                        mBluetoothLeService.txxx("0003010200000000");
                        //delay(20);
                }
            }
            // Automatically connects to the device upon successful start-up initialization.
            /*
            if (mBluetoothLeService.isconnect())
            {
                connect_status_bit=true;
                mConnected = true;
                Log.e(DELAYSHOT_TAG, "delayshot connected!");
            }
            else
            {
                boolean result;
                result = mBluetoothLeService.connect(mDeviceAddress);
                Log.e(DELAYSHOT_TAG, "delayshot connect "+result);
                if (result)
                {
                    mConnected = true;
                    connect_status_bit=true;
                    timer.cancel();
                    Log.e(DELAYSHOT_TAG, "delayshot connected!");
                    mBluetoothLeService.txxx("0003010200000000");
                    Log.e(DELAYSHOT_TAG, "0003010200000000");
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
            Log.e(DELAYSHOT_TAG, "delayshot BroadcastReceiver");
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                connect_status_bit=true;
                Log.e(DELAYSHOT_TAG, "ACTION_GATT_CONNECTED");
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Log.e(DELAYSHOT_TAG, "ACTION_GATT_DISCONNECTED");
                MainActivity.device_mode = 1;/*防止重连后多个模式重复显示在左侧菜单*/
                connect_status_bit=true;
                mConnected = true;
                mBluetoothLeService.disconnect();//20181111
                //unregisterReceiver(mGattUpdateReceiver);
                //unbindService(mServiceConnection);
                mBluetoothLeService = null;
                timer.cancel();
                timer=null;
                Intent intent1 = new Intent(DelayShot.this,
                        MainActivity.class);
                startActivity(intent1);
                connect_status_bit=false;
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                Log.e(DELAYSHOT_TAG, "ACTION_GATT_SERVICES_DISCOVERED");
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                String str = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                if (str.length() < 4)
                {
                    Log.e(DELAYSHOT_TAG, "Received Data Length Error(<4), Data : "+str);
                    return;
                }
                Log.e(DELAYSHOT_TAG, "Receive Data : "+str);

                if (str.substring(0,6).equals("030BFF") )
                {
                    Log.e(DELAYSHOT_TAG, "Ready To Enter AB Point Setting");
                    Intent intent1 = new Intent(DelayShot.this,
                            ABpoint.class);
                    intent1.putExtra(DelayShot.EXTRAS_DEVICE_NAME,
                            mDeviceName);
                    intent1.putExtra(DelayShot.EXTRAS_DEVICE_ADDRESS,
                            mDeviceAddress);
                    startActivity(intent1);
                    return;
                }
                if (str.substring(0,4).equals("0309"))
                {
                    Log.e(DELAYSHOT_TAG, "Shoot Count(0309) Header Check OK");
					if (str.length() != 14)
					{
                        Log.e(DELAYSHOT_TAG, "Shoot Count(0309) PayLoad Length Check ERR("+str.length()+")");
						return;
					}
                    int remain_times_low = Integer.valueOf(str.substring(4,6),16);
                    int remain_times_high = Integer.valueOf(str.substring(6,8),16);
                    int total_shoot_second = Integer.valueOf(str.substring(8,10),16);
                    int total_shoot_minute = Integer.valueOf(str.substring(10,12),16);
                    int total_shoot_hour = Integer.valueOf(str.substring(12,14),16);
                    TvRemainTimes.setText(""+(remain_times_high*256+remain_times_low));
                    TvShottimeTotal.setText(String.format("%02d", total_shoot_hour)+":"
                            +String.format("%02d", total_shoot_minute)+":"+String.format("%02d", total_shoot_second));

                }

                if (str.substring(0,4).equals("0302"))
                {
                    Log.e(DELAYSHOT_TAG, "DelayShoot Params(0302) Header Check OK");
					if (str.length() != 28)
					{
                        Log.e(DELAYSHOT_TAG, "DelayShoot Params(0302) PayLoad Length Check ERR("+str.length()+")");
						return;
					}

					
                    String tmp_str = str.substring(4,6);
                    /*zhuge second*/
                    zhuge_second = Integer.valueOf(tmp_str,16);
                    //EtSecond.setText(""+tmp_str);
                    Log.e(DELAYSHOT_TAG, "zhuge second = "+tmp_str);
                    /*zhuge minute*/
                    tmp_str = str.substring(6,8);
                    zhuge_minute = Integer.valueOf(tmp_str,16);
                    //EtMinute.setText(""+tmp_str);
                    Log.e(DELAYSHOT_TAG, "zhuge minute = "+tmp_str);
                    /*zhuge hour*/
                    tmp_str = str.substring(8,10);
					zhuge_hour = Integer.valueOf(tmp_str,16);
                    //EtHour.setText(""+tmp_str);
                    Log.e(DELAYSHOT_TAG, "zhuge hour = "+tmp_str);

                    TvZhugeTime.setText(
                            String.format("%02d", zhuge_hour)+"  :  "+
                                    String.format("%02d", zhuge_minute)+"  :  "+
                                    String.format("%02d", zhuge_second));
                    /*baoguang second*/
                    tmp_str = str.substring(10,12);
                    baoguang_second = Integer.valueOf(tmp_str,16);
                    //EtBaoguangSec.setText(""+tmp_str);
                    Log.e(DELAYSHOT_TAG, "baoguang second = "+tmp_str);
                    /*baoguang minute*/
                    tmp_str = str.substring(12,14);
                    baoguang_minute = Integer.valueOf(tmp_str,16);
                    //EtBaoguangMin.setText(""+tmp_str);
                    Log.e(DELAYSHOT_TAG, "baoguang minute = "+tmp_str);
                    /*baoguang hour*/
                    tmp_str = str.substring(14,16);
                    baoguang_hour = Integer.valueOf(tmp_str,16);
                    //EtBaoguangHour.setText(""+tmp_str);
                    Log.e(DELAYSHOT_TAG, "baoguang hour = "+tmp_str);

					TvBaoguangTime.setText(
                                String.format("%02d", baoguang_hour)+"  :  "+
                                String.format("%02d", baoguang_minute)+"  :  "+
                                String.format("%02d", baoguang_second));
                    /*shot maxt time*/
                    tmp_str = str.substring(16,18);
                    //Log.e(DELAYSHOT_TAG, "Integer.valueOf(str.substring(18,20),16) "+Integer.valueOf(str.substring(18,20),16));
                    //Log.e(DELAYSHOT_TAG, "Integer.valueOf(tmp_str,16) "+Integer.valueOf(tmp_str,16));
                    Log.e(DELAYSHOT_TAG, "max shoot time integer = "+((Integer.valueOf(str.substring(18,20),16))*256 +
                            Integer.valueOf(tmp_str,16)));
                    /*StrParam[3] = ((Integer.valueOf(str.substring(18,20),16))*256 +
                            Integer.valueOf(tmp_str,16));*/
                    shoot_times = (Integer.valueOf(str.substring(18,20),16))*256 +
                            Integer.valueOf(tmp_str,16);
                    DelayShot.max_shot_times = shoot_times;
                    Log.e(DELAYSHOT_TAG, "DelayShot.max_shot_times " + DelayShot.max_shot_times);
                    TvShootTimes.setText(""+shoot_times);
                    Log.e(DELAYSHOT_TAG, "shoot_times = "+shoot_times);
                    /*auto back*/
                    tmp_str = str.substring(20,22);
                    Log.e(DELAYSHOT_TAG, "auto back = "+tmp_str);
                    /*direction*/
                    tmp_str = str.substring(24,26);
                    Log.e(DELAYSHOT_TAG, "direction = "+tmp_str);
                    if (tmp_str.equals("FF"))
                    {
                        switch_direction.setChecked(false);
                    }
                    else if (tmp_str.equals("00"))
                    {
                        switch_direction.setChecked(true);
                    }
                    /*start or stop*/
                    tmp_str = str.substring(26,28);
                    if (tmp_str.equals("FF"))
                    {
                        delayshot_btn_start.setBackgroundResource(R.drawable.stop);
                        delayshot_start_press_flag = false;
                    }
                    else if (tmp_str.equals("00"))
                    {
                        delayshot_btn_start.setBackgroundResource(R.drawable.start);
                        delayshot_start_press_flag = true;
                    }
                    get_param_success = true;
                    timer.cancel();

                }
                else
                {
                    get_param_success = false;
                    Log.e(DELAYSHOT_TAG, "DelayShoot Params(0302) Header Check ERR "+str.substring(0,4));
                }

                //displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
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
                        Log.e(DELAYSHOT_TAG,"mConnected==false");
                        //updateConnectionState(R.string.connecting);
                        //final boolean result = mBluetoothLeService.connect(mDeviceAddress);
                        //Log.e(DELAYSHOT_TAG, "Connect request result=" + result);
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
                if (MainActivity.connect_status_bit)
                {
                    Log.e(DELAYSHOT_TAG, "retry");
                    mBluetoothLeService.txxx("0003010200000000");
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

            Intent intent1 = new Intent(DelayShot.this,
                    VedioShot.class);
            intent1.putExtra(VedioShot.EXTRAS_DEVICE_NAME,
                    mDeviceName);
            intent1.putExtra(VedioShot.EXTRAS_DEVICE_ADDRESS,
                    mDeviceAddress);
            startActivity(intent1);
            // Handle the camera action
        } else if (id == R.id.delay_shoot) {

            Log.e(DELAYSHOT_TAG, "nav_gallery");

        } else if (id == R.id.abpoint) {
            Log.e(DELAYSHOT_TAG, "nav_slideshow");
            Intent intent1 = new Intent(DelayShot.this,
                    ABpoint.class);
            intent1.putExtra(VedioShot.EXTRAS_DEVICE_NAME,
                    mDeviceName);
            intent1.putExtra(VedioShot.EXTRAS_DEVICE_ADDRESS,
                    mDeviceAddress);
            startActivity(intent1);

        }else if (id == R.id.scan) {
            /*任意界面只要按左菜单扫描按钮就会重新扫描*/
            //if (!connect_status_bit)
            {
                Intent intent1 = new Intent(DelayShot.this,
                        MainActivity.class);
                intent1.putExtra(VedioShot.EXTRAS_DEVICE_NAME,
                        mDeviceName);
                intent1.putExtra(VedioShot.EXTRAS_DEVICE_ADDRESS,
                        mDeviceAddress);
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
        Intent intent=new Intent(this,DelayShot.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;

        Log.e(DELAYSHOT_TAG, "delayshot displayGattServices");
        if( gattServices.size()>0&&mBluetoothLeService.get_connected_status( gattServices )>=4 )
        {
            if( MainActivity.connect_status_bit )
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
                Log.e(DELAYSHOT_TAG, "delayshot displayGattServices disconnect");
            }
        }

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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return false;
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.e(DELAYSHOT_TAG, "delayshot onResume");

        if (BluetoothLeService.mConnectionState == BluetoothLeService.STATE_DISCONNECTED &&
                        MainActivity.connect_status_bit == true)
        {
                Log.e(DELAYSHOT_TAG, "Connection Lost");
                Log.e(DELAYSHOT_TAG, "ACTION_GATT_DISCONNECTED");
                MainActivity.device_mode = 1;/*防止重连后多个模式重复显示在左侧菜单*/
                mConnected = false;
                //mBluetoothLeService.disconnect(); //20181111
                //unregisterReceiver(mGattUpdateReceiver);
                //unbindService(mServiceConnection);
                mBluetoothLeService = null;
                timer.cancel();
                timer=null;
                Intent intent1 = new Intent(DelayShot.this,
                                MainActivity.class);
                startActivity(intent1);
                connect_status_bit=false;
        }
        
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {

            //final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            //Log.e(DELAYSHOT_TAG, "delayshot onResume connect " + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(DELAYSHOT_TAG, "delayshot onPause");
        Log.e(DELAYSHOT_TAG, "aaa");
        String tx_string = "0093050101000000";
        if(BluetoothLeService.mConnectionState == BluetoothLeService.STATE_CONNECTED)
        {
            Log.e(DELAYSHOT_TAG, "bbb");
            mBluetoothLeService.txxx(tx_string);
            Log.e(DELAYSHOT_TAG, "ccc");
            Log.e(DELAYSHOT_TAG, tx_string);
        }
        Log.e(DELAYSHOT_TAG, "ddd");
        unbindService(mServiceConnection);
        unregisterReceiver(mGattUpdateReceiver);
        Log.e(DELAYSHOT_TAG, "eee");
    }

    @Override
    protected void onDestroy() {
        Log.e(DELAYSHOT_TAG, "delayshot onDestroy");
        super.onDestroy();
        mBluetoothLeService.disconnect();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
        timer.cancel();
        timer = null;
        unregisterReceiver(mGattUpdateReceiver);
    }


    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        Log.e(DELAYSHOT_TAG, "IntentFilter");
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
}
