package com.ulez.clientandroid;

import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    public static final int MESSAGE_NEW_MSG = 1;
    private static final int MESSAGE_STATUS_MSG = 2;
    private static final int SEND_SUCCESS = 3;

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
    private WebSocketClient webSocketClient;

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
                try {
                    connectToServer();
                } catch (URISyntaxException e) {
                    mHandler.obtainMessage(MESSAGE_STATUS_MSG, "连接失败：" + e.getMessage()).sendToTarget();
                    e.printStackTrace();
                }
                break;
            case R.id.bt_clear:
                adapterData.clear();
                adapter.notifyDataSetChanged();
                break;
            case R.id.bt_send:
                sendMsg();
                break;
            case R.id.disconnect:
                webSocketClient.close();
                break;
        }
    }

    private void sendMsg() {
        try {
            webSocketClient.send(etSend.getText().toString());
            mHandler.obtainMessage(SEND_SUCCESS, etSend.getText().toString()).sendToTarget();
        } catch (Exception e) {
            mHandler.obtainMessage(SEND_SUCCESS, "发送失败" + etSend.getText().toString() + e.getMessage()).sendToTarget();
        }
    }

    private void connectToServer() throws URISyntaxException {
        webSocketClient = new WebSocketClient(new URI(etIp.getText().toString()), new Draft_6455()) {

            @Override
            public void onMessage(String message) {
                mHandler.obtainMessage(MESSAGE_NEW_MSG, message).sendToTarget();
            }

            @Override
            public void onOpen(ServerHandshake handshake) {
                mHandler.obtainMessage(MESSAGE_STATUS_MSG, "已连接:" + handshake.getHttpStatusMessage()).sendToTarget();
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                mHandler.obtainMessage(MESSAGE_STATUS_MSG, "已经断开！").sendToTarget();
            }

            @Override
            public void onError(Exception ex) {
                mHandler.obtainMessage(MESSAGE_STATUS_MSG, "报错：" + ex.getMessage()).sendToTarget();
            }
        };
        webSocketClient.connect();
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
