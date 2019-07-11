package com.ulez.clientandroid;

import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {


    public static final int MESSAGE_NEW_MSG = 1;

    private Socket socket;
    private MyHandler mHandler;
    private EditText etIp;
    private TextView tvMsg;
    private String TAG = "MainActivity";
    private ListView listView;
    private ArrayList<String> adapterData;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etIp = findViewById(R.id.et_ip);
        tvMsg = findViewById(R.id.tv_msg);
        listView = findViewById(R.id.lv);


        listView = findViewById(R.id.lv);
        adapterData = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, adapterData);
        listView.setAdapter(adapter);

        mHandler = new MyHandler(this);


        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String[] paras = etIp.getText().toString().split(":");
                    socket = new Socket(paras[0], Integer.parseInt(paras[1]));
                    InputStream inputStream = socket.getInputStream();
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = inputStream.read(buffer)) != -1) {
                        String data = new String(buffer, 0, len);
                        mHandler.obtainMessage(MESSAGE_NEW_MSG, data).sendToTarget();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private static class MyHandler extends WeakHandler<MainActivity> {
        public MyHandler(MainActivity context) {
            super(context);
        }

        @Override
        public void handle(MainActivity activity, Message msg) {
            switch (msg.what) {
                case MESSAGE_NEW_MSG:
                    activity.addNewMsg((String) msg.obj);
                    break;
            }
        }
    }

    private void addNewMsg(String str) {
        Log.e(TAG, str);
        adapterData.add(str);
        adapter.notifyDataSetChanged();
    }
}
