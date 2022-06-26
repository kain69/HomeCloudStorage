package com.example.clienthomecloud;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import java.net.Socket;
import java.net.UnknownHostException;
import java.net.InetSocketAddress;

public class MainActivity extends AppCompatActivity {


    private Connection  mConnect  = null;
    private  String     HOST      = "192.168.1.109";
    private  int        PORT      = 3345;
    private  String     LOG_TAG   = "SOCKET";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        onOpenClick();
    }

    private void onOpenClick()
    {
        // Создание подключения
        mConnect = new Connection(HOST, PORT);
        // Открытие сокета в отдельном потоке
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mConnect.openConnection();
                    Log.d(LOG_TAG, "Соединение установлено");
                    Log.d(LOG_TAG, "(mConnect != null) = "
                            + (mConnect != null));
                } catch (Exception e) {
                    Log.e(LOG_TAG, e.getMessage());
                    mConnect = null;
                }
            }
        }).start();
    }

    private void onCloseClick()
    {
        // Закрытие соединения
        mConnect.closeConnection();
        Log.d(LOG_TAG, "Соединение закрыто");
    }
}