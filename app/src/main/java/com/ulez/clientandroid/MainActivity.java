package com.ulez.clientandroid;

import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    public static final int MESSAGE_NEW_MSG = 1;
    private static final int MESSAGE_STATUS_MSG = 2;
    private static final int SEND_SUCCESS = 3;

    private Socket socket;
    private MyHandler mHandler;
    private EditText etIp;
    private EditText etSend;
    private TextView tvMsg;
    private TextView tvStatus;
    private String TAG = "MainActivity";
    private ListView listView;
    private Button disconnect;
    private ArrayList<String> adapterData;
    private ArrayAdapter<String> adapter;
    private PrintWriter writer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etIp = findViewById(R.id.et_ip);
        etSend = findViewById(R.id.et_send);
        tvMsg = findViewById(R.id.tv_msg);
        tvStatus = findViewById(R.id.tv_status);
        listView = findViewById(R.id.lv);
        disconnect = findViewById(R.id.disconnect);


        listView = findViewById(R.id.lv);
        adapterData = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, adapterData);
        listView.setAdapter(adapter);

        mHandler = new MyHandler(this);
        findViewById(R.id.bt).setOnClickListener(this);
        findViewById(R.id.bt_clear).setOnClickListener(this);
        findViewById(R.id.bt_send).setOnClickListener(this);
        disconnect.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt:
                connectToServer();
                break;
            case R.id.bt_clear:
                adapterData.clear();
                adapter.notifyDataSetChanged();
                break;
            case R.id.bt_send:
                sendMsg();
                break;
            case R.id.disconnect:
                closeSocket();
                break;
        }
    }

    private void closeSocket() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMsg() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    writer.print(etSend.getText().toString());
                    writer.flush();
                    Log.e(TAG, "writer.println" + etSend.getText().toString());
                    mHandler.obtainMessage(SEND_SUCCESS, etSend.getText().toString()).sendToTarget();
                } catch (Exception e) {
                    Log.e(TAG, "SEND_ERROR" + e.getMessage());
//                    mHandler.obtainMessage(SEND_ERROR, msg).sendToTarget();
                }
            }
        }).start();
    }

    private void connectToServer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                InputStream inputStream = null;
                try {
                    String[] paras = etIp.getText().toString().split(":");
                    socket = new Socket(paras[0], Integer.parseInt(paras[1]));
                    inputStream = socket.getInputStream();
                    mHandler.obtainMessage(MESSAGE_STATUS_MSG, "已连接至:" + socket.getRemoteSocketAddress().toString()).sendToTarget();

                    writer = new PrintWriter(socket.getOutputStream(), true);
                } catch (IOException e) {
                    mHandler.obtainMessage(MESSAGE_STATUS_MSG, "连接失败！").sendToTarget();
                    Log.e(TAG, e.getMessage());
                    e.printStackTrace();
                }
                byte[] buffer = new byte[1024];
                int len;
                try {
                    while ((len = inputStream.read(buffer)) != -1) {
                        String data = new String(buffer, 0, len);
                        mHandler.obtainMessage(MESSAGE_NEW_MSG, data).sendToTarget();
                    }
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                } finally {
                    mHandler.obtainMessage(MESSAGE_STATUS_MSG, "已经断开！").sendToTarget();
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
                    activity.addNewMsg("收到:" + msg.obj);
                    break;
                case MESSAGE_STATUS_MSG:
                    activity.tvStatus.setText((String) msg.obj);
                    break;
                case SEND_SUCCESS:
                    activity.adapterData.add("发送:" + msg.obj);
                    activity.adapter.notifyDataSetChanged();
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
