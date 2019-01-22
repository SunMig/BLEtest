package com.example.lenovo.bletest;

import android.Manifest;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.List;
import java.util.TimerTask;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends AppCompatActivity {
    private static String TAG="BLE";
    private TextView tv1;
    private Button bt1,bt2;
    private String sdPath;
    private String fileName="ble_wifi";
    BluetoothAdapter bluetoothAdapter;
    BluetoothManager bluetoothManager;
    private WifiManager wifiManager;
    private LocationManager locationManager;
    BluetoothLeScanner bleScanner;
    ScanSettings.Builder scanSettingsBuilder;
    private StringBuilder sb=new StringBuilder();
    private boolean isRun=false;
    public static final int REQUEST_ENABLE_BT=1;
    private String[] needed_permission;
    private String MAC_36="C2:00:89:00:00:B7";
    private String MAC_38="C2:00:89:00:00:BC";
    private String MAC_46="C2:00:89:00:00:7C";
    private String MAC_40="C2:00:89:00:00:C1";
    private String MAC_51="6c:e8:73:91:96:ac";//wifi的一个AP
    private String MAC_47="6c:e8:73:91:96:d0";
    //用于存储蓝牙数据
    private HashMap<String, Integer> iBeaconMap=new HashMap<>();
    private int count=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationManager=(LocationManager)getSystemService(LOCATION_SERVICE);
        initView();
        final wifiScanThread thread=new wifiScanThread();
        wifiManager=(WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);
        if(!wifiManager.isWifiEnabled()){
            wifiManager.setWifiEnabled(true);
            Log.i(TAG,"wifi打开！");
        }
        //获取蓝牙适配器
//        bluetoothManager=(BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
//        if(bluetoothManager!=null){
//            bluetoothAdapter=bluetoothManager.getAdapter();
//            if(!bluetoothAdapter.isEnabled()){
//                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
//            }
//        }
//        /*
//            扫描蓝牙数据的流程：
//            1.获取蓝牙适配器bluetoothAdapter,先获得bluetoothmanager的蓝牙服务，然后获取适配器（bluetoothManager.getAdapter()）
//            2.获取适配器之后，初始化蓝牙操作，包括蓝牙扫描（bluetoothAdapter.getBluetoothLeScanner()，
//              以及蓝牙扫描的频率ScanSettings.Builder等
//            3.开启蓝牙扫描，bleScanner.startScan(null,scanSettingsBuilder.build(),scancallback)
//         */
        //动态获取权限
        requestApplicationPermission();
        //thread.start();
        //开启WiFi扫描
        //myScanning();
        //初始化蓝牙
//        initblueth();
//        //开始蓝牙扫描
//        bleScanner.startScan(null,scanSettingsBuilder.build(),scancallback);
        bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isRun=true;
                count++;
                fileName=fileName+"_"+count;
                thread.start();
            }
        });

        bt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isRun=false;
                fileName="ble_wifi";
                tv1.setText(""+0.00);

            }
        });

    }
    private void myScanning(){
        wifiManager.startScan();
        List<android.net.wifi.ScanResult> list=wifiManager.getScanResults();
        for(int j=0;j<list.size();j++){
            //获取蓝牙MAC
            String apmac=list.get(j).BSSID;
            int aplevel=list.get(j).level;
            if(apmac.equals(MAC_47)){
                sb.append(apmac.concat("\t").concat(String.valueOf(aplevel)).concat("\n"));
            }
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv1.setText(sb.toString());
                //sb.delete(0,sb.length());
            }
        });
        //tv1.setText(sb.toString());
        Log.i(TAG,sb.toString());
        if(isRun){
            WriteFileSdcard(sb);
            Log.i(TAG+" writing: ",sb.toString());
            sb.delete(0,sb.length());
        }
    }
    //初始化控件
    private void initView() {
        tv1=(TextView)findViewById(R.id.text);
        bt1=(Button)findViewById(R.id.bt1);
        bt2=(Button)findViewById(R.id.bt2);
    }
    //初始化蓝牙操作
    private void initblueth() {
        bleScanner=bluetoothAdapter.getBluetoothLeScanner();
        scanSettingsBuilder=new ScanSettings.Builder();
        scanSettingsBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
    }
    private void requestApplicationPermission() {
        needed_permission = new String[]{
                Manifest.permission.CHANGE_NETWORK_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.READ_LOGS,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        };
        boolean permission_ok = true;
        for (String permission : needed_permission) {
            if (ContextCompat.checkSelfPermission(this,
                    permission) != PackageManager.PERMISSION_GRANTED) {
                permission_ok = false;
//                mTextView.append(String.valueOf(permission_ok)+"\n");
            }
        }
        if (!permission_ok) {
            ActivityCompat.requestPermissions(this, needed_permission, 1);
        }
    }

    private ScanCallback scancallback=new ScanCallback() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice device=result.getDevice();
            if(device!=null){
                int rssi=result.getRssi();
                String Mac=device.getAddress();
//                if(Mac.equals(MAC_36)){
//                    sb.append(MAC_36.concat("\t").concat(String.valueOf(rssi).concat("\t")));
//                }
//                if(Mac.equals(MAC_38)){
//                    sb.append(MAC_38.concat("\t").concat(String.valueOf(rssi).concat("\t")));
//                }
//                if(Mac.equals(MAC_46)){
//                    sb.append(MAC_46.concat("\t").concat(String.valueOf(rssi).concat("\t")));
//                }
                if(Mac.equals(MAC_40)){
                    sb.append(MAC_40.concat("\t").concat(String.valueOf(rssi).concat("\n")));
                }
            }
            Log.i(TAG,sb.toString());
            //sb.delete(0,sb.length());
            //文件写入
            if(isRun){
//                Log.i(TAG,"writing");
                WriteFileSdcard(sb);
                Log.i(TAG+" writing: ",sb.toString());
                sb.delete(0,sb.length());
            }
        }
    };

    @TargetApi(Build.VERSION_CODES.N)
    private void WriteFileSdcard(StringBuilder message) {
        try{
            //创建文件夹
            sdPath = Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator;
            File file = new File(sdPath+FileName.str+File.separator);
            if(!file.exists()){
                file.mkdir();
            }
            //创建文件并写入
            File file1=new File(sdPath+FileName.str+File.separator+fileName+".txt");
            if (!file1.exists()) {
                file1.createNewFile();
            }
            //参数true必须添加，要不然写入数据为空，文件也是空的
            FileOutputStream fos=new FileOutputStream(file1,true);
            BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(fos));
            bw.write(message.toString());
            bw.flush();
            bw.close();

        }catch (IOException e){
            e.printStackTrace();
        }
    }
    private class wifiScanThread extends Thread{

        @Override
        public void run() {
            super.run();
            while(isRun){
                myScanning();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}
