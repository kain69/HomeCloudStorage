package com.example.clienthomecloud;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Stack;

public class MainActivity extends AppCompatActivity {

    private  TextView   textView;
    EditText TextIP;
    Button btnConnect, btnDisconnect;
    private  Connection mConnect  = null;
    private  String     HOST      = "";
//    private  String     HOST      = "192.168.1.109";
    private  int        PORT      = 3345;
    private  String     LOG_TAG   = "SOCKET";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.StatusView);

        TextIP = (EditText) findViewById(R.id.TextIP);
        btnConnect = (Button) findViewById(R.id.btnConnect);
        btnDisconnect = (Button) findViewById(R.id.btnDisconnect);
        btnConnect.setEnabled(false);
        btnDisconnect.setEnabled(false);
        CustomTextWatcher textWatcher = new CustomTextWatcher(TextIP, btnConnect, btnDisconnect);
        TextIP.addTextChangedListener(textWatcher);
    }

    public void onOpenClick(View view)
    {
        // Создание подключения
        HOST = TextIP.getText().toString();
        mConnect = new Connection(HOST, PORT);
        textView.setText("Соединение установлено");
        // Открытие сокета в отдельном потоке
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mConnect.openConnection();
                } catch (Exception e) {
                    Log.e(LOG_TAG, e.getMessage());
                    mConnect = null;
                }
            }
        }).start();
    }

    public void onCloseClick(View view)
    {
        // Закрытие соединения
        if(mConnect != null){
            mConnect.closeConnection();
            textView.setText("Соединение закрыто");
        }
        else{
            Log.d(LOG_TAG, "Соединение не существует");
        }
    }

}