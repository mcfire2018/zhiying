package com.example.sushiyu.smartshot;

import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class ABpoint extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final String ABPOINT_TAG = "mcfire_abpoint";
    private String mDeviceName;
    private String mDeviceAddress;
    public BluetoothLeService mBluetoothLeService;
    private Intent connect_intent;
    private TextView mConnectionState;
    private boolean mConnected = false;
    boolean connect_status_bit=false;
    private ImageButton img_btn_left_abpoint;
    private ImageButton img_btn_right_abpoint;
    private ImageButton img_btn_start_abpoint;
    private Button btn_reset;
    private boolean start_press_flag;
    private boolean abpoint_set_flag = false;
    private TextView abpoint_set;
    private TextView tv_notification;
    private boolean screen_toggle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_abpoint);
        Log.e(ABPOINT_TAG, "abpoint activity");
        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
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
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        sg = bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        img_btn_left_abpoint = (ImageButton) findViewById(R.id.imageButton_left_abpoint);
        img_btn_right_abpoint = (ImageButton) findViewById(R.id.imageButton_right_abpoint);
        img_btn_start_abpoint = (ImageButton) findViewById(R.id.imageButton_start_abpoint);
        btn_reset = (Button) findViewById(R.id.btn_reset);
        abpoint_set = (TextView) findViewById(R.id.tv_abpoint_set);
        tv_notification = (TextView) findViewById(R.id.tv_abpoint_notification);

        img_btn_left_abpoint.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    img_btn_left_abpoint.setBackgroundResource(R.drawable.left_press);
                    img_btn_right_abpoint.setBackgroundResource(R.drawable.right_release);
                    String tx_string;
                    tx_string="009301030101ff00";
                    Log.e(ABPOINT_TAG, tx_string);
                    if (!MainActivity.connect_status_bit)
                        return false;
                    mBluetoothLeService.txxx(tx_string);
                }else if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                    img_btn_left_abpoint.setBackgroundResource(R.drawable.left_press);
                    img_btn_right_abpoint.setBackgroundResource(R.drawable.right_release);
                    String tx_string;
                    tx_string="0093010301010000";
                    Log.e(ABPOINT_TAG, tx_string);
                    if (!MainActivity.connect_status_bit)
                        return false;
                    mBluetoothLeService.txxx(tx_string);
                }
                return false;
            }
        });

        img_btn_right_abpoint.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    img_btn_left_abpoint.setBackgroundResource(R.drawable.left_release);
                    img_btn_right_abpoint.setBackgroundResource(R.drawable.right_press);
                    String tx_string;
                    tx_string="009301030102ff00";
                    Log.e(ABPOINT_TAG, tx_string);
                    if (!MainActivity.connect_status_bit)
                        return false;
                    mBluetoothLeService.txxx(tx_string);
                }else if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                    img_btn_left_abpoint.setBackgroundResource(R.drawable.left_release);
                    img_btn_right_abpoint.setBackgroundResource(R.drawable.right_press);
                    String tx_string;
                    tx_string="0093010301020000";
                    Log.e(ABPOINT_TAG, tx_string);
                    if (!MainActivity.connect_status_bit)
                        return false;
                    mBluetoothLeService.txxx(tx_string);

                }
                return false;
            }
        });
        //img_btn_start.setBackgroundResource(R.drawable.stop);
        img_btn_start_abpoint.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    if (abpoint_set_flag)
                    {
                        /*Log.e(ABPOINT_TAG, "skip");
                        abpoint_set_flag = false;
                        Intent intent1 = new Intent(ABpoint.this,
                                VedioShot.class);
                        intent1.putExtra(VedioShot.EXTRAS_DEVICE_NAME,
                                mDeviceName);
                        intent1.putExtra(VedioShot.EXTRAS_DEVICE_ADDRESS,
                                mDeviceAddress);
                        startActivity(intent1);*/
                    }
                    else
                    {
                        abpoint_set_flag = true;
                    }
                    if (start_press_flag) {
                        img_btn_start_abpoint.setBackgroundResource(R.drawable.start);
                        start_press_flag = false;
                        abpoint_set.setText(R.string.a_point_setting);
                        String tx_string;
                        tx_string="0093010302000000";
                        Log.e(ABPOINT_TAG, tx_string);
                        if (!MainActivity.connect_status_bit)
                            return false;

                        mBluetoothLeService.txxx(tx_string);
                    }else{
                        img_btn_start_abpoint.setBackgroundResource(R.drawable.stop);
                        start_press_flag = true;
                        abpoint_set.setText(R.string.b_point_setting);
                        tv_notification.setText(R.string.abpoint_notification_right);
                        String tx_string;
                        tx_string="0093010302000000";
                        Log.e(ABPOINT_TAG, tx_string);
                        if (!MainActivity.connect_status_bit)
                            return false;
                        mBluetoothLeService.txxx(tx_string);
                    }
                }else if(motionEvent.getAction() == MotionEvent.ACTION_UP){

                }
                return false;
            }
        });

        btn_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abpoint_set_flag = false;
                Log.e(ABPOINT_TAG, "reset button press");
                String tx_string = "0093010304000000";
                abpoint_set.setText(R.string.a_point_setting);
                if (!MainActivity.connect_status_bit)
                    return;
                mBluetoothLeService.txxx(tx_string);
            }
        });
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

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.e(ABPOINT_TAG, "ABPoint onServiceConnected");
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(ABPOINT_TAG, "Unable to initialize Bluetooth");
                finish();
            }
            Log.e(ABPOINT_TAG, "ABPoint AAA!");
            // Automatically connects to the device upon successful start-up initialization.
            /*if (mBluetoothLeService.isconnect())
            {
                connect_status_bit=true;
                mConnected = true;
                Log.e(ABPOINT_TAG, "ABPoint connected!");
            }
            else*/
            {
                boolean result;
                Log.e(ABPOINT_TAG, "ABPoint BBB!");
                //result = mBluetoothLeService.connect(mDeviceAddress);
                //Log.e(ABPOINT_TAG, "ABPoint connect "+result);
                //if (result)
                {
                    mConnected = true;
                    connect_status_bit=true;
                    timer.cancel();
                }
            }
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
            Log.e(ABPOINT_TAG, "ABPoint BroadcastReceiver");
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {

            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                MainActivity.device_mode = 1;/*防止重连后多个模式重复显示在左侧菜单*/
                connect_status_bit=true;
                mConnected = true;
                mBluetoothLeService.disconnect();//20181111
                unbindService(mServiceConnection);
                mBluetoothLeService = null;
                timer.cancel();
                timer=null;
                Intent intent1 = new Intent(ABpoint.this,
                        MainActivity.class);
                startActivity(intent1);
                connect_status_bit=false;
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                Log.e(ABPOINT_TAG, "ABPoint service discovered");
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                Log.e(ABPOINT_TAG, "ABPoint received data");
                String str = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                Log.e(ABPOINT_TAG, "PPP"+ str);
                //030CFF 030A1234

                if (str.length() < 6)
                {
                    Log.e(ABPOINT_TAG, "ABPoint str less than 6, return, str = "+str);
                    return;
                }
                if (str.substring(0,6).equals("030CFF"))
                {
                    Log.e(ABPOINT_TAG, "skip");
                    /*if (str.substring(6,10).equals("030A"))
                    {
                        String shot_maxtime = str.substring(12,14) + str.substring(10,12);
                        DelayShot.max_shot_times_abpoint = Integer.valueOf(shot_maxtime,16);
                        Log.e(ABPOINT_TAG, "max shot time abpoint"+ DelayShot.max_shot_times_abpoint);
                    }*/
                    if (abpoint_set_flag)
                    {
                        Log.e(ABPOINT_TAG, "AAA");
                        abpoint_set_flag = false;

                        Intent intent1 = new Intent(ABpoint.this,
                                VedioShot.class);
                        intent1.putExtra(VedioShot.EXTRAS_DEVICE_NAME,
                                mDeviceName);
                        intent1.putExtra(VedioShot.EXTRAS_DEVICE_ADDRESS,
                                mDeviceAddress);
                        startActivity(intent1);
                    }

                }
                else if (str.substring(0, 6).equals("030C00"))
                {
                    Log.e(ABPOINT_TAG, "BBB");
                    img_btn_start_abpoint.setVisibility(View.VISIBLE);
                    tv_notification.setText(R.string.abpoint_notification_error);
                }
                else
                {
                    Log.e(ABPOINT_TAG, "CCC");
                }


            }
        }
    };

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
            Intent intent1 = new Intent(ABpoint.this,
                    VedioShot.class);
            intent1.putExtra(VedioShot.EXTRAS_DEVICE_NAME,
                    mDeviceName);
            intent1.putExtra(VedioShot.EXTRAS_DEVICE_ADDRESS,
                    mDeviceAddress);
            startActivity(intent1);
        } else if (id == R.id.delay_shoot) {
            Intent intent1 = new Intent(ABpoint.this,
                    DelayShot.class);
            intent1.putExtra(VedioShot.EXTRAS_DEVICE_NAME,
                    mDeviceName);
            intent1.putExtra(VedioShot.EXTRAS_DEVICE_ADDRESS,
                    mDeviceAddress);
            startActivity(intent1);

        } else if (id == R.id.abpoint) {

        } else if (id == R.id.scan) {
            /*任意界面只要按左菜单扫描按钮就会重新扫描*/
            //if (!connect_status_bit)
            {
                Intent intent1 = new Intent(ABpoint.this,
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
        Intent intent=new Intent(this,ABpoint.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;

        Log.e(ABPOINT_TAG, "abpoint displayGattServices");
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
                //updateConnectionState(R.string.connected);
            }else{
                Log.e(ABPOINT_TAG, "abpoint displayGattServices disconnect");
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
        Log.e(ABPOINT_TAG, "abpoint onResume");
        if (BluetoothLeService.mConnectionState == BluetoothLeService.STATE_DISCONNECTED)
        {
                Log.e(ABPOINT_TAG, "Connection Lost");
                Log.e(ABPOINT_TAG, "ACTION_GATT_DISCONNECTED");
                MainActivity.device_mode = 1;/*防止重连后多个模式重复显示在左侧菜单*/
                mConnected = false;
                //mBluetoothLeService.disconnect(); //20181111
                //unregisterReceiver(mGattUpdateReceiver);
                unbindService(mServiceConnection);
                mBluetoothLeService = null;
                timer.cancel();
                timer=null;
                Intent intent1 = new Intent(ABpoint.this,
                                MainActivity.class);
                startActivity(intent1);
                connect_status_bit=false;
        }
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {

            //final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            //Log.e(ABPOINT_TAG, "abpoint onResume connect " + result);
        }
    }

    @Override
    protected void onPause() {
        Log.e(ABPOINT_TAG, "abpoint onPause");
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        Log.e(ABPOINT_TAG, "abpoint onDestroy");
        super.onDestroy();
        mBluetoothLeService.disconnect();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
        timer.cancel();
        timer = null;
    }


    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        Log.e(ABPOINT_TAG, "IntentFilter");
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

}
