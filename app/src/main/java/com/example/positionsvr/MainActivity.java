package com.example.positionsvr;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

public class MainActivity extends AppCompatActivity {

    LocationClient mlc = null;
    private boolean dw1 = true;
    private Button but;
    private ProgressBar bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        but = (Button)findViewById(R.id.button);
        bar = (ProgressBar)findViewById(R.id.progressBar) ;
        bar.setVisibility(View.GONE);
        onPermision();

    }

    public void onPosition(View v) {
        if (dw1) {
            bar.setVisibility(View.VISIBLE);
            initLocation();
            but.setEnabled(false);
            but.setText("正在定位,请稍后...");
        } else {
            Toast.makeText(MainActivity.this, "权限不足",Toast.LENGTH_SHORT).show();
        }

        /*String JD = "110.35762";
        String WD = "21.291565";
        openDlg(JD, WD, "深圳市南山区");*/
    }

    private void initLocation() {
        //findview
        //客户端配置对象

        LocationClientOption lco = new LocationClientOption();

        lco.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        lco.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        lco.setScanSpan(0);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        lco.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        lco.setOpenGps(true);//可选，默认false,设置是否使用gps
        lco.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS
        lco.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        lco.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        lco.setIgnoreKillProcess(true);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        //初始化定位客户端
        mlc = new LocationClient(getApplicationContext());
        //给定位客户端注册自定义监听器
        mlc.registerLocationListener(mListener);
        //设置一些配置选项
        mlc.setLocOption(lco);
        //启动定位客户端
        mlc.start();
    }

    private BDAbstractLocationListener mListener = new BDAbstractLocationListener() {

        //接受位置信息
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {

            if (bdLocation != null) {
                int type = bdLocation.getLocType();
                String JD = Double.toString(bdLocation.getLongitude());
                String WD = Double.toString(bdLocation.getLatitude());
                String ADR = bdLocation.getCity()+bdLocation.getDistrict()+bdLocation.getStreet();
                mlc.stop();

                Log.d("type=", Integer.toString(type));
                Log.d("hhhhh=", JD+"-"+WD+"-"+ADR);
                if (("4.9E-324").equals(JD)) {
                    Toast.makeText(MainActivity.this, "定位失败，error="+Integer.toString(type),Toast.LENGTH_SHORT).show();
                } else {
                    openDlg(JD, WD, ADR);
                }
                //Toast.makeText(MainActivity.this,"JD="+ JD + ",WD="+WD+",ADR="+ADR , Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "定位失败",Toast.LENGTH_SHORT).show();
            }

            bar.setVisibility(View.GONE);
            but.setEnabled(true);
            but.setText("定位");

        }

    };

    private void openDlg(final String wd, final String jd, String adr) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("当前位置");
        builder.setMessage(adr);
        builder.setPositiveButton("确定",  new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                Intent intent=new Intent(MainActivity.this,positionList.class);//从SpendingActivity页面跳转至ExpenseProcesActivity页面
                intent.putExtra("latitude", wd);//纬度
                intent.putExtra("lontitude", jd);//经度

                MainActivity.this.startActivity(intent);

            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Toast.makeText(MainActivity.this, "no",Toast.LENGTH_SHORT).show();
            }
        });

        builder.show();
    }

    private void onPermision() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(),"没有权限,请手动开启定位权限",Toast.LENGTH_SHORT).show();
            // 申请一个（或多个）权限，并提供用于回调返回的获取码（用户定义）
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }else{
            dw1 = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //判断请求码
        switch (requestCode){
            case 1:
                //如果同意，就拨打
                if (grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){

                }else{
                    Toast.makeText(this,"请添加权限",Toast.LENGTH_SHORT).show();
                    dw1 = false;
                }
                break;
        }
    }

}
