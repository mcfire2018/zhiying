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
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.graphics.Paint;

import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.example.sushiyu.smartshot.VedioShot.VEDIOSHOT_TAG;

public class VedioShot_yuntai extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final String VEDIOSHOT_YUNTAI_TAG = "mcfire_vedio_yuntai";
    private String mDeviceName;
    private String mDeviceAddress;
    public BluetoothLeService mBluetoothLeService;
    public boolean mConnected = false;
    private Intent connect_intent;
    public  boolean connect_status_bit=false;
    private TextView mConnectionState;
    private Handler mHandler;


    private Switch mSwitch;

    private ImageButton img_btn_left;
    private ImageButton img_btn_right;
    private ImageButton img_btn_start;
    private boolean start_press_flag;
    private boolean get_param_success;
    private TextView Tvjiaodu;
    private EditText Etjiaodu_input;
    private int jiaodu_num;
    private SeekBar seekbar_speed;
    private TextView speed_current;
    private Button btn_speed_minus;
    private Button btn_speed_plus;
    private ScheduledExecutorService scheduledExecutor;
    private boolean screen_toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        setContentView(R.layout.activity_vedio_shot_1_yuntai);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        //mConnectionState = (TextView) findViewById(R.id.connection_state);
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
        navigationView.getMenu().add(MainActivity.btn_quanjing, MainActivity.btn_quanjing,
                MainActivity.btn_quanjing, "全景拍摄");
        navigationView.getMenu().add(MainActivity.btn_zidingyi, MainActivity.btn_zidingyi,
                MainActivity.btn_zidingyi, "自定义拍摄");
        mHandler = new Handler();
        timer.schedule(task, 1000, 1000);
        boolean sg;
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        sg = bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        //updateConnectionState(R.string.connecting);
        Log.e(VEDIOSHOT_YUNTAI_TAG, "连接中");

		seekbar_speed = (SeekBar) findViewById(R.id.seekbar_speed_yuntai);
        mSwitch = (Switch) findViewById(R.id.switch1_yuntai);
        btn_speed_plus = (Button) findViewById(R.id.speed_plus_yuntai);
        btn_speed_minus = (Button) findViewById(R.id.speed_minus_yuntai);
        speed_current = (TextView) findViewById(R.id.speed_tv_yuntai);
        btn_speed_minus.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){

                    updateAddOrSubtract(v.getId());    //手指按下时触发不停的发送消息
                }else if(event.getAction() == MotionEvent.ACTION_UP){

                    stopAddOrSubtract();    //手指抬起时停止发送
                }
                return true;
            }
        });

        btn_speed_plus.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    updateAddOrSubtract(v.getId());    //手指按下时触发不停的发送消息
                }else if(event.getAction() == MotionEvent.ACTION_UP){
                    stopAddOrSubtract();    //手指抬起时停止发送
                }
                return true;
            }
        });
	Tvjiaodu = (TextView) findViewById(R.id.jiaodu_input_yuntai);
	Tvjiaodu.setText("00");
	Tvjiaodu.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
	Tvjiaodu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Etjiaodu_input = new EditText(VedioShot_yuntai.this);
                Etjiaodu_input.setInputType(InputType.TYPE_CLASS_NUMBER);
                Etjiaodu_input.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }
                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }
                    @Override
                    public void afterTextChanged(Editable editable) {
                        Log.e(VEDIOSHOT_YUNTAI_TAG, "EtShotTimes afterTextChanged "+editable.toString()+"a");
                        if (editable.toString().equals(""))
                        {
                            jiaodu_num = 0;
                            return;
                        }
                        jiaodu_num = Integer.valueOf(editable.toString(),10);
                        if (jiaodu_num > 720)
                        {
                            jiaodu_num = 720;
                            Etjiaodu_input.setText(""+jiaodu_num);
                        }
                        Log.e(VEDIOSHOT_YUNTAI_TAG, "Etjiaodu_input afterTextChanged "+jiaodu_num);
                    }
                });
                AlertDialog dialog = new AlertDialog.Builder(VedioShot_yuntai.this)
                        .setTitle("请输入角度")
                        .setView(Etjiaodu_input)
                        //.setView(new EditText(DelayShot.this))
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.e(VEDIOSHOT_YUNTAI_TAG, "TvShootTimes onClick");


                                String tx_string;
                                String jiaodu_num_high;
                                String jiaodu_num_low;

                                jiaodu_num_high = String.format("%02x", (jiaodu_num / 256));
                                jiaodu_num_low = String.format("%02x", (jiaodu_num % 256));
                                //tx_string="0093020101"+jiaodu_num_low + jiaodu_num_high+"00";
                                tx_string="00930201"+"01"+jiaodu_num_low + jiaodu_num_high+"00";
                                Tvjiaodu.setText(""+jiaodu_num);
                                if(!connect_status_bit)
                                    return;
                                mBluetoothLeService.txxx(tx_string);
                                Log.e(VEDIOSHOT_YUNTAI_TAG, tx_string);

                            }
                        })
                        .create();
                dialog.show();

            }
        });

        img_btn_left = (ImageButton) findViewById(R.id.imageButton_left_yuntai);
        img_btn_right = (ImageButton) findViewById(R.id.imageButton_right_yuntai);
        img_btn_start = (ImageButton) findViewById(R.id.imageButton_start_yuntai);
        speed_current = (TextView) findViewById(R.id.speed_tv_yuntai);
        img_btn_left.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    img_btn_left.setBackgroundResource(R.drawable.left_press);
                    img_btn_right.setBackgroundResource(R.drawable.right_release);
                    String tx_string;
                    //tx_string="0093010102ffff00";
                    tx_string="00930201"+"04"+"FFFF00";
                    if (!connect_status_bit)
                        return false;
                    mBluetoothLeService.txxx(tx_string);
                    Log.e(VEDIOSHOT_YUNTAI_TAG, tx_string);
                }else if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                    img_btn_left.setBackgroundResource(R.drawable.left_release);
                    img_btn_right.setBackgroundResource(R.drawable.right_release);
                    String tx_string;
					tx_string="00930201"+"04"+"FF0000";
                    //tx_string="0093010102ff0000";
                    if (!connect_status_bit)
                        return false;
                    mBluetoothLeService.txxx(tx_string);
                    Log.e(VEDIOSHOT_YUNTAI_TAG, tx_string);
                }
                return false;
            }
        });

        img_btn_right.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    img_btn_left.setBackgroundResource(R.drawable.left_release);
                    img_btn_right.setBackgroundResource(R.drawable.right_press);
                    String tx_string;
					tx_string="00930201"+"04"+"00FF00";
                    //tx_string="009301010200ff00";
                    if (!connect_status_bit)
                        return false;
                    mBluetoothLeService.txxx(tx_string);
                    Log.e(VEDIOSHOT_YUNTAI_TAG, tx_string);
                }else if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                    img_btn_left.setBackgroundResource(R.drawable.left_release);
                    img_btn_right.setBackgroundResource(R.drawable.right_release);
                    String tx_string;
					tx_string="00930201"+"04"+"000000";
                    //tx_string="0093010102000000";
                    if (!connect_status_bit)
                        return false;
                    mBluetoothLeService.txxx(tx_string);
                    Log.e(VEDIOSHOT_YUNTAI_TAG, tx_string);
                }
                return false;
            }
        });
        //img_btn_start.setBackgroundResource(R.drawable.stop);
        img_btn_start.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {

                    if (start_press_flag) {

                        img_btn_start.setBackgroundResource(R.drawable.start);
                        start_press_flag = false;
                        String tx_string;
						tx_string="00930201"+"05"+"000000";
                        //tx_string="0093010105000000";
                        if (!connect_status_bit)
                            return false;
                        mBluetoothLeService.txxx(tx_string);
                        Log.e(VEDIOSHOT_YUNTAI_TAG, tx_string);
                    }else{

                        img_btn_start.setBackgroundResource(R.drawable.stop);
                        start_press_flag = true;
                        String tx_string;
						tx_string="00930201"+"05"+"FF0000";
                        //tx_string="0093010105ff0000";
                        if (!connect_status_bit)
                            return false;
                        mBluetoothLeService.txxx(tx_string);
                        Log.e(VEDIOSHOT_YUNTAI_TAG, tx_string);
                    }
                }else if(motionEvent.getAction() == MotionEvent.ACTION_UP){

                }
                return false;
            }
        });
        //
		
		seekbar_speed.setMax(99);
		
		seekbar_speed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			private int run_speed;
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress_value, boolean fromUser) {
				run_speed = progress_value;
				speed_current.setText(""+(run_speed+1));
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				//Toast.makeText(ABPointActivity.this, "start slice", Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				//Toast.makeText(ABPointActivity.this, "stop slice", Toast.LENGTH_SHORT).show();

				Log.e(VEDIOSHOT_YUNTAI_TAG, "progress_value = "+run_speed);

				String tx_string;
				if (run_speed >= 100)
				{
					run_speed = 99;
				}
				if (run_speed < 16)
				{
					tx_string="00930201"+"030"+Integer.toHexString(run_speed)+"0000";
					//tx_string="00930101010"+ Integer.toHexString(run_speed)+"0000";
				}
				else
				{
					tx_string="00930201"+"03"+Integer.toHexString(run_speed)+"0000";
					//tx_string="0093010101"+Integer.toHexString(run_speed)+"0000";
				}
				Log.e(VEDIOSHOT_YUNTAI_TAG, "trace "+ tx_string);
				if(!connect_status_bit)
					return;
				mBluetoothLeService.txxx(tx_string);
				Log.e(VEDIOSHOT_YUNTAI_TAG, tx_string);

			}
		});

        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    String tx_string;
                    //tx_string="0093010103ff0000";
                    tx_string="00930201"+"02"+"FF"+"0000";
                    if(!connect_status_bit)
                        return;
                    mBluetoothLeService.txxx(tx_string);
                    Log.e(VEDIOSHOT_YUNTAI_TAG, tx_string);
                }else {
                    String tx_string;
                    //tx_string="0093010103000000";
                    tx_string="00930201"+"02"+"00"+"0000";
                    if(!connect_status_bit)
                        return;
                    mBluetoothLeService.txxx(tx_string);
                    Log.e(VEDIOSHOT_YUNTAI_TAG, tx_string);
                    //Log.e("vedioshot","BBBBB");
                }
            }
        });

    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.e(VEDIOSHOT_YUNTAI_TAG, "VedioShoot onServiceConnected");
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(VEDIOSHOT_YUNTAI_TAG, "Unable to initialize Bluetooth");
                finish();
            }
            {
                boolean result;
                //result = mBluetoothLeService.connect(mDeviceAddress);
                //Log.e(VEDIOSHOT_TAG, "VedioShoot Connect Result "+result);
                //if (result)
                {
                    mConnected = true;
                    connect_status_bit=true;
                    //timer.cancel();
                    Log.e(VEDIOSHOT_YUNTAI_TAG, "VedioShoot Connect!");
                    mBluetoothLeService.txxx("0003020100000000");
					Log.e(VEDIOSHOT_YUNTAI_TAG, "tx 0003020100000000");

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
            connect_intent = intent;
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                Log.e(VEDIOSHOT_YUNTAI_TAG, "ACTION_GATT_CONNECTED");
                mConnected = true;
                connect_status_bit=true;
                //delay(3000);
                //mBluetoothLeService.txxx("0093040100000000");
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Log.e(VEDIOSHOT_YUNTAI_TAG, "ACTION_GATT_DISCONNECTED");
                mConnected = false;
                MainActivity.device_mode = 1;/*防止重连后多个模式重复显示在左侧菜单*/
                //mBluetoothLeService.disconnect();
                unbindService(mServiceConnection);
                mBluetoothLeService = null;
                timer.cancel();
                timer=null;
                Intent intent1 = new Intent(VedioShot_yuntai.this,
                        MainActivity.class);
                startActivity(intent1);
                connect_status_bit=false;

                //show_view(false);
                //invalidateOptionsMenu();
                //clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                Log.e(VEDIOSHOT_YUNTAI_TAG, "ACTION_GATT_SERVICES_DISCOVERED");
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                String str = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                Log.e(VEDIOSHOT_YUNTAI_TAG, "Receive Data : "+str);
                if (str.length() != 16)
                {
                    Log.e(VEDIOSHOT_YUNTAI_TAG, "Receive Data Length not right, str = "+str);
                    return;
                }

                if (str.substring(0,4).equals("0303"))
                {
                    String tmp_str;
                    int jiaodu_num_high;
                    int jiaodu_num_low;
                    tmp_str = str.substring(4,6);
                    jiaodu_num_low = Integer.valueOf(tmp_str,16);
                    tmp_str = str.substring(6,8);
                    jiaodu_num_high = Integer.valueOf(tmp_str,16);
                    Log.e(VEDIOSHOT_YUNTAI_TAG, "jiaodu_num_low" + jiaodu_num_low);
                    Log.e(VEDIOSHOT_YUNTAI_TAG, "jiaodu_num_high" + jiaodu_num_high);
                    Log.e(VEDIOSHOT_YUNTAI_TAG, "jiaodu" + (jiaodu_num_high*256 + jiaodu_num_low));
                    Tvjiaodu.setText(""+(jiaodu_num_high*256 + jiaodu_num_low));
                    tmp_str = str.substring(8,10);
                    if (tmp_str.equals("00"))
                    {
                        Log.e(VEDIOSHOT_YUNTAI_TAG, "00");
                        mSwitch.setChecked(false);
                    }
                    else if(tmp_str.equals("FF"))
                    {
                        Log.e(VEDIOSHOT_YUNTAI_TAG, "FF");
                        mSwitch.setChecked(true);
                    }
                    else
                    {
                        Log.e(VEDIOSHOT_YUNTAI_TAG, "fuck");
                    }
                    /*yuntai speed*/
                    tmp_str = str.substring(10,12);
                    Log.e(VEDIOSHOT_YUNTAI_TAG, "seepd_string = "+tmp_str);
                    //seekbar_speed.setProgress(Integer.valueOf(tmp_str,16)-1);
                    seekbar_speed.setProgress(Integer.valueOf(tmp_str,16));

                    /*Direction*/
                    tmp_str = str.substring(12,14);

                    if (tmp_str.equals("FF"))
                    {
                        img_btn_left.setBackgroundResource(R.drawable.left_press);
                        img_btn_right.setBackgroundResource(R.drawable.right_release);
                    }
                    else if(tmp_str.equals("00"))
                    {
                        img_btn_left.setBackgroundResource(R.drawable.left_release);
                        img_btn_right.setBackgroundResource(R.drawable.right_press);
                    }
                    else
                    {
                        Log.e(VEDIOSHOT_YUNTAI_TAG, "fuck");
                    }

                    tmp_str = str.substring(14,16);
                    if (tmp_str.equals("FF"))
                    {

                        img_btn_start.setBackgroundResource(R.drawable.stop);
                        start_press_flag = true;
                    }
                    else if(tmp_str.equals("00"))
                    {

                        img_btn_start.setBackgroundResource(R.drawable.start);
                        start_press_flag = false;
                    }
                    get_param_success = true;
                    timer.cancel();
                }
                else
                {
                    Log.e(VEDIOSHOT_YUNTAI_TAG, "not equal to 0303");
                    get_param_success = false;
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

    private void updateAddOrSubtract(int viewId) {
        final int vid = viewId;
        scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                Message msg = new Message();
                msg.what = vid;
                handler_speed_inc_dec.sendMessage(msg);
            }
        }, 0, 100, TimeUnit.MILLISECONDS);    //每间隔100ms发送Message
    }

    private void stopAddOrSubtract() {
        int cur_progress;
        String tx_string;

        if (scheduledExecutor != null) {
            scheduledExecutor.shutdownNow();
            scheduledExecutor = null;
        }
        cur_progress = seekbar_speed.getProgress();
        Log.e(VEDIOSHOT_TAG, "post progress_value = "+cur_progress);
        if (cur_progress >= 100)
        {
            cur_progress = 99;
        }
        if (cur_progress < 16)
        {

            tx_string="00930201030"+ Integer.toHexString(cur_progress)+"0000";
        }
        else
        {
            tx_string="0093020103"+Integer.toHexString(cur_progress)+"0000";
        }
        Log.e(VEDIOSHOT_TAG, "trace "+ tx_string);
        if(!MainActivity.connect_status_bit)
        {
            //Toast.makeText(VedioShot.this, R.string.NoConnecting, Toast.LENGTH_SHORT).show();
            return ;
        }
        mBluetoothLeService.txxx(tx_string);
        Log.e(VEDIOSHOT_TAG, tx_string);
    }

    private Handler handler_speed_inc_dec = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            int viewId = msg.what;
            int cur_progress;


            switch (viewId){
                case R.id.speed_minus_yuntai:
                    Log.e(VEDIOSHOT_TAG, "dec");

                    cur_progress = seekbar_speed.getProgress();
                    if (cur_progress > 0)
                    {
                        seekbar_speed.setProgress(cur_progress - 1);
                    }
                    cur_progress = seekbar_speed.getProgress();
                    Log.e(VEDIOSHOT_TAG, "progress_value = "+cur_progress);

                    break;
                case R.id.speed_plus_yuntai:
                    Log.e(VEDIOSHOT_TAG, "inc");

                    cur_progress = seekbar_speed.getProgress();
                    Log.e(VEDIOSHOT_TAG, "pre progress_value = "+cur_progress);
                    if (cur_progress < 99)
                    {
                        seekbar_speed.setProgress(cur_progress + 1);
                    }

                    break;
            }
        }
    };


    Handler handler = new Handler() {
        public void handleMessage(Message msg) {

            if (msg.what == 1) {
                //Log.e("vedioshot", "handleMessage");
                //tvShow.setText(Integer.toString(i++));
                //scanLeDevice(true);
                if (mBluetoothLeService != null) {
                    //Log.e("vedioshot", "mBluetoothLeService != null");
                    if( mConnected==false )
                    {
                        //updateConnectionState(R.string.connecting);
                        //final boolean result = mBluetoothLeService.connect(mDeviceAddress);
                        //Log.e(VEDIOSHOT_TAG, "Connect request result=" + result);
                    }
                }
            }
            super.handleMessage(msg);
        };
    };
    TimerTask task = new TimerTask() {

        @Override
        public void run() {
            //Log.e("vedioshot", "BBB");
            // ÐèÒª×öµÄÊÂ:·¢ËÍÏûÏ¢
            //Log.e("vedioshot", "timer task run");
            Message message = new Message();
            message.what = 1;
            handler.sendMessage(message);
            if (!get_param_success)
            {
                if (connect_status_bit)
                {
                    Log.e(VEDIOSHOT_YUNTAI_TAG, "retry");
                    mBluetoothLeService.txxx("0003020100000000");
					Log.e(VEDIOSHOT_YUNTAI_TAG, "tx 0003020100000000");
                }
            }
        }
    };

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.e(VEDIOSHOT_YUNTAI_TAG, "state changed");
                //mConnectionState.setText(resourceId);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.e(VEDIOSHOT_YUNTAI_TAG, "CCC");
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
        Log.e(VEDIOSHOT_YUNTAI_TAG, "DDD");
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
        Log.e(VEDIOSHOT_YUNTAI_TAG, "EEE");
        if (id == R.id.vedio_shoot) {
            // Handle the camera action
        } else if (id == R.id.delay_shoot) {
            timer.cancel();
            Intent intent1 = new Intent(VedioShot_yuntai.this,
                    DelayShot_yuntai.class);
            startActivity(intent1);

        } else if (id == R.id.abpoint) {
            Log.e(VEDIOSHOT_YUNTAI_TAG, "abpoint");
            Intent intent1 = new Intent(VedioShot_yuntai.this,
                    ABpoint.class);
            startActivity(intent1);
        }else if (id == MainActivity.btn_quanjing) {
            timer.cancel();
            Log.e(VEDIOSHOT_YUNTAI_TAG, "btn_quanjing");
            Intent intent1 = new Intent(VedioShot_yuntai.this,
                    PanoramicShoot_yuntai.class);
            startActivity(intent1);

        }else if (id == MainActivity.btn_zidingyi) {
            timer.cancel();
            Log.e(VEDIOSHOT_YUNTAI_TAG, "btn_zidingyi");
            Intent intent1 = new Intent(VedioShot_yuntai.this,
                    CustomMode_yuntai.class);
            startActivity(intent1);

        }else if (id == R.id.scan) {
            /*任意界面只要按左菜单扫描按钮就会重新扫描*/
            //if (!connect_status_bit)
            {
                Intent intent1 = new Intent(VedioShot_yuntai.this,
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
        Intent intent=new Intent(this,VedioShot_yuntai.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }


    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;

        if( gattServices.size()>0&&mBluetoothLeService.get_connected_status( gattServices )>=4 )
        {
            if(connect_status_bit)
            {
                mConnected = true;
                //show_view( true );
                mBluetoothLeService.enable_JDY_ble(true);
                try {
                    Thread.currentThread();
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mBluetoothLeService.enable_JDY_ble(true);
            }else{
                Log.e(VEDIOSHOT_YUNTAI_TAG, "displayGattServices disconnect");
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
        Log.e(VEDIOSHOT_YUNTAI_TAG, "onResume");
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {

            //final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            //Log.e(VEDIOSHOT_TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        Log.e(VEDIOSHOT_YUNTAI_TAG, "onPause");
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        Log.e(VEDIOSHOT_YUNTAI_TAG, "III");
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
        Log.e(VEDIOSHOT_YUNTAI_TAG, "IntentFilter");
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }


}
