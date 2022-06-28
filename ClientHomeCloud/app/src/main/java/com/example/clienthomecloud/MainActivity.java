package com.example.clienthomecloud;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    TextView textView;
    private Connection  mConnect  = null;
    private  String     HOST      = "192.168.1.109";
    private  int        PORT      = 3345;
    private  String     LOG_TAG   = "SOCKET";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.StatusView);
    }

    public void onOpenClick(View view)
    {
        // Создание подключения
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