package com.example.positionsvr;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class positionList extends AppCompatActivity {

    private ListView listView;
    private String lat, lon;
    private int idx = 1;
    private ArrayList<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();
    private LoadMoreListView mListView;
    private ArrayAdapter<HashMap<String, String>> adapter;
    private int pageNum = 0;
    private HashMap<String, String> DataItem;
    private Handler handler;
    private boolean loadStatus = true;
    private ProgressBar bar1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_position_list);

        mListView = (LoadMoreListView) findViewById(R.id.listview);

        bar1 = (ProgressBar)findViewById(R.id.progressBar1) ;

        View view = LayoutInflater.from(positionList.this).inflate(R.layout.position_head, null);
        mListView.addHeaderView(view);
        mListView.setVisibility(View.VISIBLE);

        /*if (Build.VERSION.SDK_INT >= 11) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedClosableObjects().penaltyLog().penaltyDeath().build());
        }*/

        getParam();

        Runnable runnable=new Runnable()
        {
            public void run()
            {
                try
                {
                    Thread.sleep(2000);
                    //xmlwebdata解析网络中xml中的数据
                    ArrayList<HashMap<String, String>> tmpList = addData(loadData(pageNum++));
                    //发送消息，并把persons结合对象传递过去
                    handler.sendMessage(handler.obtainMessage(0, tmpList));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        try
        {
            //开启线程
            new Thread(runnable).start();
            //handler与线程之间的通信及数据处理
            handler=new Handler()
            {
                public void handleMessage(Message msg)
                {
                    if(msg.what==0 && msg.obj != null)
                    {
                        //msg.obj是获取handler发送信息传来的数据
                        ArrayList<HashMap<String, String>> tmp = (ArrayList<HashMap<String, String>>) msg.obj;
                        dataList.addAll(tmp);
                        //给ListView绑定数据
                        init();
                        bar1.setVisibility(View.GONE);
                    } else if (msg.what==1) {
                        Toast.makeText(positionList.this, "数据加载完成",Toast.LENGTH_SHORT).show();
                        mListView.setLoadCompleted();
                    }
                }
            };
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void init() {
        //getListData();

        SimpleAdapter sim_aAdapter = new SimpleAdapter(this, dataList, R.layout.list_item,
                new String[]{"idx", "mobile", "addr", "dateTime"}, new int[]{R.id.idx, R.id.mobile, R.id.addr, R.id.dateTime});
        mListView.setAdapter(sim_aAdapter);
        mListView.setOnLoadMoreListener(new LoadMoreListView.OnLoadMoreListener() {
            @Override
            public void onloadMore() {
                if (loadStatus) {
                    loadMore();
                } else {
                    mListView.setLoadCompleted();
                    Toast.makeText(positionList.this, "没有更多数据了",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void getParam() {
        //接收传递过来的参数
        final Intent intent = getIntent();
        lat = intent.getStringExtra("latitude"); //纬度
        lon = intent.getStringExtra("lontitude"); //经度

        Toast.makeText(positionList.this, lat + "-" + lon, Toast.LENGTH_SHORT).show();
    }

    private ArrayList<HashMap<String, String>> loadData(int page) {

        String requestUrlWithPageNum = "https://api.map.baidu.com/place/v2/search?query=商铺&location=" + lon + "," + lat + "&radius=2000&output=xml&ak=FUYMcaAculpjR5GRNEF1YwHAoK9E5BCy&page_size=50&page_num=" + pageNum;
        Log.d("url=", "url=" + requestUrlWithPageNum);
        ArrayList<HashMap<String, String>> rulData = new ArrayList<HashMap<String, String>>();
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(requestUrlWithPageNum);
            try {
                Thread.sleep(100);
                urlConnection = (HttpURLConnection) url.openConnection();
                //设置请求方法 、 超时
                urlConnection.setRequestMethod("GET");
                urlConnection.setReadTimeout(5000);
                urlConnection.setConnectTimeout(5000);

                int responseCode = urlConnection.getResponseCode();
                if(responseCode == 200){
                    InputStream inputStream = urlConnection.getInputStream();
                    rulData = updateDataList(inputStream);
                    urlConnection.disconnect();
                }
                Log.d("responseCode=", Integer.toString(responseCode));
                return rulData;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private ArrayList<HashMap<String, String>> addData(ArrayList<HashMap<String, String>> list) {
        ArrayList<HashMap<String,String>> retList = new ArrayList<HashMap<String, String>>();
        if (list != null) {
            retList.addAll(list);
            while (true) {
                if (retList.size() >= 10) {
                    break;
                } else if (!loadStatus) {
                    //发送消息，并把persons结合对象传递过去
                    handler.sendEmptyMessage(1);
                    break;
                } else {
                    retList.addAll(loadData(pageNum++));
                }
            }
        }
        return retList;
    }

    private ArrayList<HashMap<String, String>> updateDataList(InputStream inputStream) throws Exception {
        ArrayList<HashMap<String,String>> list = new ArrayList<HashMap<String, String>>();
        List<Person> weatherlists = PULLService.getPersons(inputStream);
        if (weatherlists.size() <= 0) {
            loadStatus = false;
            return list;
        }
        for (Person channel : weatherlists) {
            Log.d("kwwl", channel.toString());
            String[] str = channel.toString().split("#");

            HashMap<String, String> hashMap = new HashMap<String, String>();
            hashMap.put("idx", Integer.toString(idx));
            //hashMap.put("mobile", str[0]);
            hashMap.put("addr", str[1]);
            hashMap.put("dateTime", str[2]);
            if (str[0] != null) {
                String[] ms = str[0].split(",");
                for (String m : ms) {
                    if (m.length() == 11) {
                        hashMap.put("mobile", m);
                        list.add(hashMap);
                        idx++;
                        break;
                    }
                }

            }
        }
        return list;
    }

    int i = 0;

    public void loadMore() {
        new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                dataList.addAll(addData(loadData(pageNum++)));
                adapter = new ArrayAdapter<HashMap<String, String>>(positionList.this, R.layout.list_item, dataList);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                        mListView.setLoadCompleted();
                    }
                });
            }
        }.start();

    }

    public void onImport(View v) {
        String content = "序号,手机号,位置,时间\r\n";
        for (HashMap<String, String> data : dataList ) {
            content += data.get("idx") + "," + data.get("mobile") + "," + data.get("addr") + "," + data.get("dateTime")+"\r\n";
        }
        Log.d("positionFile=", content);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        Date date = new Date(System.currentTimeMillis());
        String fileName = simpleDateFormat.format(date);

        Context context = positionList.this;
        String path = Environment.getExternalStorageDirectory().getPath().toString();
        FileUtils f = new FileUtils();
        f.writeTxtToFile(content, path, "/dw"+fileName+".csv");
        //WriteFile("abcdw"+fileName+".csv", content);
        Log.e("tishi=", "文件保存完成-"+fileName+".csv, path="+path);
        Toast.makeText(positionList.this, "文件保存完成-"+fileName+".csv",Toast.LENGTH_SHORT).show();
    }

    private void WriteFile(String fileName, String fileData) {
        try {
            File fs = new File(Environment.getDataDirectory().getPath()+"/" + fileName);
            FileOutputStream outputStream =new FileOutputStream(fs);
            outputStream.write(fileData.getBytes());
            outputStream.flush();
            outputStream.close();
            Toast.makeText(getBaseContext(), "File created successfully", Toast.LENGTH_LONG).show();
            Log.e("tag", "Successful");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}