package com.example.clienthomecloud;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Set;
import java.util.Stack;
//TODO При повоторном подключении не отправляется картинка (сокет не создается)
// если менять текст для IP кнопки слетают
// Почему-то подключается к несушествующим серверам (не сообщает об ошибке)
public class MainActivity extends AppCompatActivity {

    EditText TextIP;
    TextView textStatus;
    Button btnConnect, btnDisconnect, btnBrowser;
    static public  Connection mConnect  = null;
    private  String     HOST      = "";
    //private  String     HOST      = "192.168.1.109";
    private  int        PORT      = 3345;
    private  String     LOG_TAG   = "SOCKET";
    Thread   connectThread;
    static public StatusCode statusCode = new StatusCode();
    static public int status = 1;
    static public String statusColor = "#FFFFFF";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextIP = (EditText) findViewById(R.id.TextIP);
        textStatus = (TextView) findViewById(R.id.textStatus);
        btnConnect = (Button) findViewById(R.id.btnConnect);
        btnDisconnect = (Button) findViewById(R.id.btnDisconnect);
        btnBrowser = (Button) findViewById(R.id. btnBrowser);
        btnConnect.setEnabled(true);
        btnDisconnect.setEnabled(true);
        btnBrowser.setEnabled(true);
        CustomTextWatcher textWatcher = new CustomTextWatcher(TextIP, btnConnect, btnDisconnect, btnBrowser);
        TextIP.addTextChangedListener(textWatcher);

        //Можно поторгать твою картинку?
        if(ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE
            },1);

        }
    }

    @Override // Можно потрогать твою картинку?
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==1){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED){

            }
            else{
                Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void onOpenClick(View view)
    {
        // Создание подключения
        HOST = TextIP.getText().toString();
        mConnect = new Connection(HOST, PORT);
        // Открытие сокета в отдельном потоке
        connectThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mConnect.openConnection();
                } catch (Exception e) {
                    Log.e(LOG_TAG, e.getMessage());
                    mConnect = null;
                }
            }
        });
        connectThread.start();
        if(connectThread.isAlive()){
            btnDisconnect.setEnabled(true);
            btnConnect.setEnabled(false);
            btnBrowser.setEnabled(true);
        }
        UpdateStatus();
    }

    public void onCloseClick(View view)
    {
        // Закрытие соединения
        if(mConnect != null){
            mConnect.closeConnection();
            connectThread.interrupt();
            TextIP.setText("");
        }
        else{
            Log.d(LOG_TAG, "Соединение не существует");
        }
        UpdateStatus();
    }

    public void onBrowserClick(View view){
        Intent intent = new Intent(this, BrowseActivity.class);
        startActivity(intent);
        UpdateStatus();
    }

    public void UpdateStatus(){
        runOnUiThread(new Runnable(){
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                    textStatus.setText("" + statusCode.listWithCodes.get(status));
                    textStatus.setTextColor(Color.parseColor(statusColor));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}