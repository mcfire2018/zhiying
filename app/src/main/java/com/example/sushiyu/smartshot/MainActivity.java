package com.example.sushiyu.smartshot;

import android.Manifest;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.w3c.dom.Text;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private static final int REQUEST_CODE_LOCATION_SETTINGS = 2;
    private static final int REQUEST_CODE_ACCESS_COARSE_LOCATION = 1;
    public static final int btn_quanjing = 296955;
    public static final int btn_zidingyi = 296956;
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final String MAINACTIVITY_TAG = "mcfire_main";
    public static int device_mode = 1;  //1-->huagui, 2-->yuntai, 3-->xiaoche
    private String mDeviceName;
    private String mDeviceAddress;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;
    private boolean service_binded;
    /*0 for vedio mode key press
    * 1 for delay mode
    * 2 for ab point mode*/
    private int shoot_mode = 0;
    private int abpoint_ok = 0;

    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    private DeviceListAdapter mDevListAdapter;

    private List<BluetoothDevice> bt_device_test;

    BluetoothDevice device_select;
    ToggleButton tb_on_off;
    TextView btn_searchDev;
    Button btn_aboutUs;
    ListView lv_bleList;
    private DrawerLayout drawerLayout;
    View view1;
    LayoutInflater inflater1;
    ActionBarDrawerToggle toggle;
    public static BluetoothLeService mBluetoothLeService;
    public boolean mConnected = false;
    public boolean mConnecting = false;
    private Intent connect_intent;
    public  static boolean connect_status_bit=false;
    private int wait_receive_mcu_msg_to = 26;
    private int timeout_flag = 0;
    private boolean get_param_success;
    private boolean screen_toggle;
    private ImageButton img_btn_scan;
    private TextView scanning_tv;
    private boolean scanning = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String language = getResources().getConfiguration().locale.getLanguage();
        Log.e(MAINACTIVITY_TAG, "language == "+language);
        shiftLanguage(language);
        setContentView(R.layout.activity_vedio_shot);


        mHandler = new Handler();

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (!mBluetoothAdapter.isEnabled())
        {
            Intent mIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(mIntent, 1);
        }


        //TODO
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//如果 API level 是大于等于 23(Android 6.0) 时
            //判断是否具有权限
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //判断是否需要向用户解释为什么需要申请该权限
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    Toast.makeText(this, "自Android 6.0开始需要打开位置权限才可以搜索到Ble设备", Toast.LENGTH_LONG).show();
                }
                //请求权限
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_CODE_ACCESS_COARSE_LOCATION);
            }

            Boolean haha =isLocationEnable(this);
            if (!haha){
                setLocationService();
            }
        }



        bt_device_test = new ArrayList<BluetoothDevice>();
        //bt_device_test = mBluetoothAdapter.getBondedDevices();

        lv_bleList = (ListView) findViewById(R.id.lv_bleList);
        img_btn_scan = (ImageButton) findViewById(R.id.imageButton_scan);
        scanning_tv = (TextView) findViewById(R.id.scanning_tv);
        if (mDevListAdapter == null)
        {
            Log.e(MAINACTIVITY_TAG, "new a DeviceListAdapter");
            mDevListAdapter = new DeviceListAdapter();
        }

        img_btn_scan.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                /*
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {

                    if (mScanning) {
                        scanLeDevice(false);
                    }else{
                        scanLeDevice(true);
                        lv_bleList.setEnabled(true);
                        lv_bleList.setVisibility(View.VISIBLE);

                    }
                }else if(motionEvent.getAction() == MotionEvent.ACTION_UP){

                }
                */

                if (mScanning || mConnecting)
                {
                    Log.e(MAINACTIVITY_TAG, "img_btn_scan mScanning = "+mScanning);
                    Log.e(MAINACTIVITY_TAG, "img_btn_scan mConnecting = "+mConnecting);
                    /*这里是为了扫描时隐藏扫面按钮，不让用户提前结束*/
                    return false;
                }
                Intent intent1 = new Intent(MainActivity.this,
                        MainActivity.class);
                intent1.putExtra(VedioShot.EXTRAS_DEVICE_NAME,
                        mDeviceName);
                intent1.putExtra(VedioShot.EXTRAS_DEVICE_ADDRESS,
                        mDeviceAddress);
                startActivity(intent1);

                return false;
            }
        });

        lv_bleList.setAdapter(mDevListAdapter);
        lv_bleList.setVisibility(View.VISIBLE);
        lv_bleList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (mDevListAdapter.getCount() > 0) {
                    Log.e(MAINACTIVITY_TAG, "BLE List Item Click");
                    if (mBluetoothLeService != null)
                        mBluetoothLeService.disconnect();//20181111
                    lv_bleList.setVisibility(View.INVISIBLE);
                    BluetoothDevice device_select = mDevListAdapter.getItem(position);
                    if (device_select == null) {
                        Log.e(MAINACTIVITY_TAG, "device == null");
                        return;
                    }
                    Toast.makeText(MainActivity.this, R.string.Connecting, Toast.LENGTH_SHORT).show();

                    mScanning = false;/*这里可能写的不太好，mScanning的赋值没有统一在scanLeDevice中执行*/
                    scanLeDevice(false);
                    mConnecting = true;
                    /*扫面按钮上面文字描述在连接过程中隐藏*/
                    //scanning_tv.setText("");

                    /*
                    Log.e(MAINACTIVITY_TAG, device_select.getName());
                    Log.e(MAINACTIVITY_TAG, device_select.getAddress());
                    Intent intent1 = new Intent(MainActivity.this,
                            VedioShot.class);

                    intent1.putExtra(VedioShot.EXTRAS_DEVICE_NAME,
                            device_select.getName());
                    intent1.putExtra(VedioShot.EXTRAS_DEVICE_ADDRESS,
                            device_select.getAddress());
                    if (mScanning) {
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                        mScanning = false;
                    }
                    Log.e(MAINACTIVITY_TAG, device_select.getName());
                    Log.e(MAINACTIVITY_TAG, device_select.getAddress());
                    startActivity(intent1);
                    */
                    mDeviceAddress = device_select.getAddress();
                    mDeviceName = device_select.getName();
                    //mHandler = new Handler();
                    timer.schedule(task, 10, 100);
                    boolean sg;
                    Intent gattServiceIntent = new Intent(MainActivity.this, BluetoothLeService.class);

                    sg = bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
                    registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
                    //updateConnectionState(R.string.connecting);
                }
            }
        });


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if(device_mode == 2)
        {
            navigationView.getMenu().removeItem(R.id.abpoint);
            navigationView.getMenu().add(btn_quanjing, btn_quanjing, btn_quanjing, "全景拍摄");
            navigationView.getMenu().add(btn_zidingyi, btn_zidingyi, btn_zidingyi, "自定义拍摄");
            navigationView.getMenu().equals(btn_quanjing);

        }

        if (mBluetoothLeService != null)
        {
            Log.e(MAINACTIVITY_TAG, "(mBluetoothLeService != null)");
            //mBluetoothLeService.disconnect();
        }

    }

    Timer timer = new Timer();

    Timer timer_wait_mcu = new Timer();

    public void delay(int ms){
        try {
            Thread.currentThread();
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {

            if (msg.what == 1) {
                //Log.e(MAINACTIVITY_TAG, "handleMessage");
                //tvShow.setText(Integer.toString(i++));
                //scanLeDevice(true);
                if (mBluetoothLeService != null) {
                    //Log.e(MAINACTIVITY_TAG, "mBluetoothLeService != null");
                    if( mConnected==false )
                    {
                        //updateConnectionState(R.string.connecting);
                        //final boolean result = mBluetoothLeService.connect(mDeviceAddress);
                        //Log.e(MAINACTIVITY_TAG, "Connect request result=" + result);
                    }
                }
            }
            super.handleMessage(msg);
        };
    };
    TimerTask task_wait_mcu = new TimerTask() {
        @Override
        public void run() {
            wait_receive_mcu_msg_to--;
            Log.e(MAINACTIVITY_TAG, "task_wait_mcu cnt = "+wait_receive_mcu_msg_to);
            if (wait_receive_mcu_msg_to == 23)
            {
                mBluetoothLeService.gatt_discoverServices();
            }
            if (wait_receive_mcu_msg_to < 19)
            {
                Log.e(MAINACTIVITY_TAG, " task_wait_mcu 0 cnt = "+wait_receive_mcu_msg_to);

                if (mConnected) {
                    if (!get_param_success)
                    {
                        Log.e(MAINACTIVITY_TAG, "tx 0093040100000000");
                        mBluetoothLeService.txxx("0093040100000000");
                    }else
                    {
                        wait_receive_mcu_msg_to = 26;
                        timer.cancel();
                        timer_wait_mcu.cancel();
                    }
                }
                /*x
                Intent intent1 = new Intent(MainActivity.this,
                        VedioShot.class);
                intent1.putExtra(VedioShot.EXTRAS_DEVICE_NAME,
                        mDeviceName);
                intent1.putExtra(VedioShot.EXTRAS_DEVICE_ADDRESS,
                        mDeviceAddress);
                startActivity(intent1);*/
            }
        }
    };

    TimerTask task = new TimerTask() {

        @Override
        public void run() {
            //Log.e(MAINACTIVITY_TAG", "BBB");
            // ÐèÒª×öµÄÊÂ:·¢ËÍÏûÏ¢
            //Log.e(MAINACTIVITY_TAG, "timer task run");
            Message message = new Message();
            message.what = 1;
            handler.sendMessage(message);
        }
    };

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.e(MAINACTIVITY_TAG, "main onServiceConnected");
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(MAINACTIVITY_TAG, "Unable to initialize Bluetooth");
                finish();
            }

            boolean result = false;
            if (!mConnected)
            {
                result = mBluetoothLeService.connect(mDeviceAddress);
                Log.e(MAINACTIVITY_TAG, "main result "+result);
                if (result)
                {
                    mConnected = true;
                    //connect_status_bit=true;
                    return;
                }
                else
                {
                    return;
                }
            }
            Toast.makeText(MainActivity.this, R.string.Connected, Toast.LENGTH_SHORT).show();
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.openDrawer(GravityCompat.START);
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
            connect_intent = intent;
            Log.e(MAINACTIVITY_TAG, "BroadcastReceiver");
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                //connect_status_bit=true;
                Log.e(MAINACTIVITY_TAG, "ACTION_GATT_CONNECTED");
                //delay(3000);
                //Log.e(MAINACTIVITY_TAG, "tx 0093040100000000");
                //delay(1000);
                timer_wait_mcu.schedule(task_wait_mcu, 1, 500);
                wait_receive_mcu_msg_to = 26;
                //mBluetoothLeService.txxx("0093040100000000");


                //drawer.setClickable(false);
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                MainActivity.device_mode = 1;/*防止重连后多个模式重复显示在左侧菜单*/
                mBluetoothLeService.disconnect();
                mBluetoothLeService = null;
                timer.cancel();
                timer_wait_mcu.cancel();
                timer=null;
                //updateConnectionState(R.string.disconnected);
                Intent intent1 = new Intent(MainActivity.this,
                        MainActivity.class);
                startActivity(intent1);
                connect_status_bit=false;
                Log.e(MAINACTIVITY_TAG, "ACTION_GATT_DISCONNECTED");
                //show_view(false);
                //invalidateOptionsMenu();
                //clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                Log.e(MAINACTIVITY_TAG, "ACTION_GATT_SERVICES_DISCOVERED");
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                String str = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                Log.e(MAINACTIVITY_TAG, "Receive Data : "+ str);



                if (str.length() < 6)
                {
                    Log.e(MAINACTIVITY_TAG, "MainActivity str less than 6, return, str = "+str);
                    return;
                }
                if (str.substring(0,6).equals("030BFF") )
                {
                    if (mScanning)
                    {
                        /*扫描过程中被中断，去建立连接，连接完成后，要把Scan按钮的TextView设置回去*/
                        //scanning_tv.setText(R.string.Scanning);
                    }
                    mConnecting = false;
                    Log.e(MAINACTIVITY_TAG, "Ready To Enter AB Point Setting");
                    connect_status_bit=true;
                    Toast.makeText(MainActivity.this, R.string.Connected, Toast.LENGTH_SHORT).show();
                    timer.cancel();
                    timer_wait_mcu.cancel();
                    get_param_success = true;
                    Intent intent1 = new Intent(MainActivity.this,
                            ABpoint.class);
                    intent1.putExtra(MainActivity.EXTRAS_DEVICE_NAME,
                            mDeviceName);
                    intent1.putExtra(MainActivity.EXTRAS_DEVICE_ADDRESS,
                            mDeviceAddress);
                    startActivity(intent1);
                    return;
                }
                else if (str.substring(0,6).equals("030B00") )
                {
                    if (mScanning)
                    {
                        /*扫描过程中被中断，去建立连接，连接完成后，要把Scan按钮的TextView设置回去*/
                        //scanning_tv.setText(R.string.Scanning);
                    }
                    mConnecting = false;
                    Log.e(MAINACTIVITY_TAG, "030B00");
                    connect_status_bit=true;
                    Toast.makeText(MainActivity.this, R.string.Connected, Toast.LENGTH_SHORT).show();
                    timer.cancel();
                    timer_wait_mcu.cancel();
                    get_param_success = true;
                    device_mode = 1;
                    if(str.substring(6,8).equals("02"))
                    {
                        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
                        navigationView.getMenu().removeItem(R.id.abpoint);
                        navigationView.getMenu().add(btn_quanjing, btn_quanjing, btn_quanjing, "全景拍摄");
                        navigationView.getMenu().add(btn_zidingyi, btn_zidingyi, btn_zidingyi, "自定义拍摄");
                        device_mode = 2;
                    }
                    //device_mode = 2;
                    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                    drawer.openDrawer(GravityCompat.START);
                    abpoint_ok = 1;

                }
                /*
                Log.e(MAINACTIVITY_TAG, "shoot_mode = "+shoot_mode);
                if (shoot_mode == 0)
                {

                    Intent intent1 = new Intent(MainActivity.this,
                            VedioShot.class);
                    intent1.putExtra(MainActivity.EXTRAS_DEVICE_NAME,
                            mDeviceName);
                    intent1.putExtra(MainActivity.EXTRAS_DEVICE_ADDRESS,
                            mDeviceAddress);
                    startActivity(intent1);
                }
                else if (shoot_mode == 1)
                {
                    Intent intent1 = new Intent(MainActivity.this,
                            DelayShot.class);
                    intent1.putExtra(MainActivity.EXTRAS_DEVICE_NAME,
                            mDeviceName);
                    intent1.putExtra(MainActivity.EXTRAS_DEVICE_ADDRESS,
                            mDeviceAddress);
                    startActivity(intent1);
                }
                else if (shoot_mode == 2)
                {
                    Intent intent1 = new Intent(MainActivity.this,
                            ABpoint.class);
                    intent1.putExtra(MainActivity.EXTRAS_DEVICE_NAME,
                            mDeviceName);
                    intent1.putExtra(MainActivity.EXTRAS_DEVICE_ADDRESS,
                            mDeviceAddress);
                    startActivity(intent1);
                }
                else
                {
                    Toast.makeText(MainActivity.this,
                            "not a regular mode", Toast.LENGTH_SHORT).show();
                }
                */

                //displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return false;
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

    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;

        Log.e(MAINACTIVITY_TAG, "FFF");
        if( gattServices.size()>0&&mBluetoothLeService.get_connected_status( gattServices )>=4 )
        {
            Log.e(MAINACTIVITY_TAG, "caonima");
            if(mConnected)
            {
                Log.e(MAINACTIVITY_TAG, "connect  aaa");

                //show_view( true );
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
                Log.e(MAINACTIVITY_TAG, "displayGattServices disconnect");
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        Log.e(MAINACTIVITY_TAG, "onCreateOptionsMenu");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Log.e(MAINACTIVITY_TAG, "onOptionsItemSelected");
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Log.e(MAINACTIVITY_TAG, "scanning");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        //if (abpoint_ok == 0)
        if (wait_receive_mcu_msg_to <= 25 && wait_receive_mcu_msg_to >=19)
        {
            /*2018.11.04开始，左菜单按键不需要连接状态下才能按下*/
            return true;
        }
        if (id == R.id.vedio_shoot) {
            timer.cancel();
            //timer_wait_mcu.cancel();
            shoot_mode = 0;
            if (device_mode == 1)
            {
                Intent intent1 = new Intent(MainActivity.this,
                        VedioShot.class);
                intent1.putExtra(MainActivity.EXTRAS_DEVICE_NAME,
                        mDeviceName);
                intent1.putExtra(MainActivity.EXTRAS_DEVICE_ADDRESS,
                        mDeviceAddress);
                startActivity(intent1);
            }
            else if (device_mode == 2)
            {
                Intent intent1 = new Intent(MainActivity.this,
                        VedioShot_yuntai.class);
                intent1.putExtra(MainActivity.EXTRAS_DEVICE_NAME,
                        mDeviceName);
                intent1.putExtra(MainActivity.EXTRAS_DEVICE_ADDRESS,
                        mDeviceAddress);
                startActivity(intent1);
            }
            else
            {

            }


        } else if (id == R.id.delay_shoot) {
            timer.cancel();
            shoot_mode = 1;
            if (device_mode == 1)
            {
                Intent intent1 = new Intent(MainActivity.this,
                        DelayShot.class);
                intent1.putExtra(MainActivity.EXTRAS_DEVICE_NAME,
                        mDeviceName);
                intent1.putExtra(MainActivity.EXTRAS_DEVICE_ADDRESS,
                        mDeviceAddress);
                startActivity(intent1);
            }
            else if (device_mode == 2)
            {
                Intent intent1 = new Intent(MainActivity.this,
                        DelayShot_yuntai.class);
                intent1.putExtra(MainActivity.EXTRAS_DEVICE_NAME,
                        mDeviceName);
                intent1.putExtra(MainActivity.EXTRAS_DEVICE_ADDRESS,
                        mDeviceAddress);
                startActivity(intent1);
            }
            else
            {

            }
        } else if (id == R.id.abpoint) {
            timer.cancel();
            shoot_mode = 2;
            Intent intent1 = new Intent(MainActivity.this,
                    ABpoint.class);
            intent1.putExtra(MainActivity.EXTRAS_DEVICE_NAME,
                    mDeviceName);
            intent1.putExtra(MainActivity.EXTRAS_DEVICE_ADDRESS,
                    mDeviceAddress);
            startActivity(intent1);

        } else if (id == R.id.scan) {
            /*任意界面只要按左菜单扫描按钮就会重新扫描*/
            //if (!connect_status_bit)
            {
                Intent intent1 = new Intent(MainActivity.this,
                    MainActivity.class);
                intent1.putExtra(VedioShot.EXTRAS_DEVICE_NAME,
                        mDeviceName);
                intent1.putExtra(VedioShot.EXTRAS_DEVICE_ADDRESS,
                        mDeviceAddress);
                startActivity(intent1);
            }
        } else if (id == btn_quanjing) {
            timer.cancel();
            Log.e(MAINACTIVITY_TAG, "btn_quanjing");
            Intent intent1 = new Intent(MainActivity.this,
                    PanoramicShoot_yuntai.class);
            intent1.putExtra(MainActivity.EXTRAS_DEVICE_NAME,
                    mDeviceName);
            intent1.putExtra(MainActivity.EXTRAS_DEVICE_ADDRESS,
                    mDeviceAddress);
            startActivity(intent1);

        }else if (id == btn_zidingyi) {
            timer.cancel();
            shoot_mode = 2;
            Log.e(MAINACTIVITY_TAG, "btn_zidingyi");

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void shiftLanguage(String sta){

        if(sta.equals("zh")){
            Locale.setDefault(Locale.CHINESE);
            Configuration config = getBaseContext().getResources().getConfiguration();
            config.locale = Locale.CHINESE;
            getBaseContext().getResources().updateConfiguration(config
                    , getBaseContext().getResources().getDisplayMetrics());
            //refreshSelf();
        }else{
            Locale.setDefault(Locale.US);
            Configuration config = getBaseContext().getResources().getConfiguration();
            config.locale = Locale.US;
            getBaseContext().getResources().updateConfiguration(config
                    , getBaseContext().getResources().getDisplayMetrics());
            //refreshSelf();
        }
    }
    //refresh self
    public void refreshSelf(){
        Intent intent=new Intent(this,MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    Runnable runnable=new Runnable(){
        @Override
        public void run() {
            // TODO Auto-generated method stub
            //要做的事情，这里再次调用此Runnable对象，以实现每两秒实现一次的定时器操作
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            img_btn_scan.setBackgroundResource(R.drawable.start);
            handler.removeCallbacks(runnable);
            Log.e(MAINACTIVITY_TAG, "runnable timeout");
            scanning_tv.setText(R.string.Scan);
            img_btn_scan.setVisibility(View.VISIBLE);
        }

    };

    private void scanLeDevice(final boolean enable) {
        if (mScanning || mConnecting)
        {
            Log.e(MAINACTIVITY_TAG, "scanLeDevice mScanning = "+mScanning);
            Log.e(MAINACTIVITY_TAG, "scanLeDevice mConnecting = "+mConnecting);
            /*这里是为了扫描时隐藏扫面按钮，不让用户提前结束*/
            return;
        }
        if (enable) {
            /*扫描之前先清空所有BLE设备，否则用户会多次连接同一个设备导致APP崩溃*/
            //mDevListAdapter.clear();
            Log.e(MAINACTIVITY_TAG, "start runnable");
            scanning_tv.setText(R.string.Scanning);
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(runnable, SCAN_PERIOD);
            mScanning = true;
            //img_btn_scan.setBackgroundResource(R.drawable.stop);
            img_btn_scan.setVisibility(View.INVISIBLE);
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            img_btn_scan.setVisibility(View.VISIBLE);
            handler.removeCallbacks(runnable);
            scanning_tv.setText(R.string.Scan);
            img_btn_scan.setBackgroundResource(R.drawable.start);
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        //invalidateOptionsMenu();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case 0:
                break;
        }
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, int rssi,
                             byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //if ((device.getName().substring(0,2).equals("MI"))||
                            //(device.getName().substring(0,2).equals("ZY")))
                    if (device.getName() != null)
                    {
                        if (((device.getName().substring(0,3).equals("ZY-"))
                                ||(device.getName().substring(0,3).equals("MLT"))
                                &&(!(device.getAddress().equals(mDeviceAddress)))))
                        {
                            mDevListAdapter.addDevice(device);
                            mDevListAdapter.notifyDataSetChanged();
                        }
                    }
                }
            });
        }
    };

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
    protected void onResume() {//打开APP时扫描设备
        super.onResume();

        scanLeDevice(true);
        lv_bleList.setEnabled(true);
        lv_bleList.setVisibility(View.VISIBLE);
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    @Override
    protected void onPause() {//停止扫描
        super.onPause();
        scanLeDevice(false);
        unregisterReceiver(mGattUpdateReceiver);
        //unbindService(mServiceConnection);
    }

    @Override
    protected void onDestroy() {
        Log.e(MAINACTIVITY_TAG, "MainActivity onDestroy");
        super.onDestroy();
        if (screen_toggle == false) {
            mBluetoothLeService.disconnect();
            unbindService(mServiceConnection);
            mBluetoothLeService = null;
            timer.cancel();
            timer_wait_mcu.cancel();
            timer = null;
            timer_wait_mcu = null;
        }else{
            screen_toggle = false;
        }
    }




    class DeviceListAdapter extends BaseAdapter {

        private List<BluetoothDevice> mBleArray;
        private ViewHolder viewHolder;

        public DeviceListAdapter() {
            mBleArray = new ArrayList<BluetoothDevice>();
        }

        public void addDevice(BluetoothDevice device) {
            if (!mBleArray.contains(device))
            {
                mBleArray.add(device);
            }
        }
        public void clear(){
            mBleArray.clear();
        }

        @Override
        public int getCount() {
            return mBleArray.size();
        }

        @Override
        public BluetoothDevice getItem(int position) {
            return mBleArray.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(MainActivity.this).inflate(
                        R.layout.list_item, null);
                viewHolder = new ViewHolder();
                viewHolder.tv_devName = (TextView) convertView
                        .findViewById(R.id.device_name);
                viewHolder.tv_devAddress = (TextView) convertView
                        .findViewById(R.id.device_address);
                convertView.setTag(viewHolder);
            } else {
                convertView.getTag();
            }

            // add-Parameters
            BluetoothDevice device = mBleArray.get(position);
            String devName = device.getName();
            if (devName != null && devName.length() > 0) {
                viewHolder.tv_devName.setText(devName);
            } else {
                viewHolder.tv_devName.setText("unknow-device");
            }
            viewHolder.tv_devAddress.setText(device.getAddress());

            return convertView;
        }

    }

    class ViewHolder {
        TextView tv_devName, tv_devAddress;
    }


    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        Log.e(MAINACTIVITY_TAG, "IntentFilter");
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    public  String tmp_bin2hex(String bin) {
        char[] digital = "0123456789ABCDEF".toCharArray();
        StringBuffer sb = new StringBuffer("");
        byte[] bs = bin.getBytes();
        int bit;
        for (int i = 0; i < bs.length; i++) {
            bit = (bs[i] & 0x0f0) >> 4;
            sb.append(digital[bit]);
            bit = bs[i] & 0x0f;
            sb.append(digital[bit]);
        }
        return sb.toString();
    }

    //TODO 执行完上面的请求权限后，系统会弹出提示框让用户选择是否允许改权限。选择的结果可以在回到接口中得知：
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_ACCESS_COARSE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //用户允许改权限，0表示允许，-1表示拒绝 PERMISSION_GRANTED = 0， PERMISSION_DENIED = -1
                //permission was granted, yay! Do the contacts-related task you need to do.
                //这里进行授权被允许的处理
            } else {
                //permission denied, boo! Disable the functionality that depends on this permission.
                //这里进行权限被拒绝的处理
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    //判断定位
    public static final boolean isLocationEnable(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean networkProvider = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        boolean gpsProvider = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (networkProvider || gpsProvider) return true;
        return false;
    }

    private void setLocationService() {
        Intent locationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        this.startActivityForResult(locationIntent, REQUEST_CODE_LOCATION_SETTINGS);
    }
}
