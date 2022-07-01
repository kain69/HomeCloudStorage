package com.example.clienthomecloud;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Stack;
//TODO При повоторном подключении не отправляется картинка (сокет не создается)
// если менять текст для IP кнопки слетают
// Почему-то подключается к несушествующим серверам (не сообщает об ошибке)
public class MainActivity extends AppCompatActivity {

    private static final int BROWSER_IMAGE = 1;
    private static final int OPEN_CONECTION = 2;
    private static final int CLOSE_CONECTION = 3;

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

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextIP = (EditText) findViewById(R.id.TextIP);
        textStatus = (TextView) findViewById(R.id.textStatus);
        btnConnect = (Button) findViewById(R.id.btnConnect);
        btnDisconnect = (Button) findViewById(R.id.btnDisconnect);
        btnBrowser = (Button) findViewById(R.id. btnBrowser);
        btnDisconnect.setVisibility(View.INVISIBLE);
        btnBrowser.setVisibility(View.INVISIBLE);
        CustomTextWatcher textWatcher = new CustomTextWatcher(TextIP, btnConnect, btnDisconnect, btnBrowser);
        TextIP.addTextChangedListener(textWatcher);
        requestMultiplePermissions();
    }

    @RequiresApi(Build.VERSION_CODES.M)
    public void requestMultiplePermissions() {

        String camera_permission = Manifest.permission.CAMERA;
        int hascampermission = checkSelfPermission(camera_permission);

        String storage_permission_group = Manifest.permission.READ_EXTERNAL_STORAGE;
        int hasStoragePermission = checkSelfPermission(storage_permission_group);

        String storage_writepermission_group = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        int hasstroage = checkSelfPermission(storage_permission_group);

        List<String> permissions = new ArrayList<String>();

        if (hasStoragePermission != PackageManager.PERMISSION_GRANTED) {
            permissions.add(storage_permission_group);
        }
        if (hascampermission != PackageManager.PERMISSION_GRANTED) {
            permissions.add(camera_permission);
        }

        if (hasstroage != PackageManager.PERMISSION_GRANTED) {
            permissions.add(storage_writepermission_group);
        }

        if (!permissions.isEmpty()) {
            String[] params = permissions.toArray(new String[permissions.size()]);
            requestPermissions(params, 1);
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            switch (requestCode) {
//                case BROWSER_IMAGE: BrowserImage(); break;
//                case OPEN_CONECTION:  OpenConection(); break;
//                case CLOSE_CONECTION:  CloseConection(); break;
//            }
//        } else {
//            status = 8;
//            statusColor = "#FF0000";
//            UpdateStatus();
//        }
//    }

    public void onOpenClick(View view) {
        int permissionStatus1 = ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET);
        int permissionStatus2 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE);

        if (    permissionStatus1 == PackageManager.PERMISSION_GRANTED &&
                permissionStatus2 == PackageManager.PERMISSION_GRANTED) {
            OpenConection();
        } else {
            ActivityCompat.requestPermissions(this, new String[] {
                        Manifest.permission.INTERNET,
                        Manifest.permission.ACCESS_NETWORK_STATE
                    }, OPEN_CONECTION);
        }
    }

    public void onCloseClick(View view) {
        int permissionStatus1 = ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET);
        int permissionStatus2 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE);

        if (    permissionStatus1 == PackageManager.PERMISSION_GRANTED &&
                permissionStatus2 == PackageManager.PERMISSION_GRANTED) {
            CloseConection();
        } else {
            ActivityCompat.requestPermissions(this, new String[] {
                    Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_NETWORK_STATE
            }, CLOSE_CONECTION);
        }
    }

    public void onBrowserClick(View view){
        int permissionStatus = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
            BrowserImage();
        } else {
            ActivityCompat.requestPermissions(this, new String[] {
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    }, BROWSER_IMAGE);
        }
    }

    public void BrowserImage(){
        Intent intent = new Intent(this, BrowseActivity.class);
        startActivity(intent);
        UpdateStatus();
    }

    public void OpenConection(){
        // Создание подключения
        HOST = TextIP.getText().toString();
        mConnect = new Connection(HOST, PORT);
        // Открытие сокета в отдельном потоке
        mConnect.openConnection();
        btnConnect.setVisibility(View.INVISIBLE);
        btnDisconnect.setVisibility(View.VISIBLE);
        btnBrowser.setVisibility(View.VISIBLE);
        btnBrowser.setEnabled(true);
        btnDisconnect.setEnabled(true);
        UpdateStatus();
    }

    public void CloseConection(){
        // Закрытие соединения
        if (mConnect != null) {
            mConnect.closeConnection();
        } else {
            Log.d(LOG_TAG, "Соединение не существует");
        }
        UpdateStatus();
        btnConnect.setVisibility(View.VISIBLE);
        btnDisconnect.setVisibility(View.INVISIBLE);
        btnBrowser.setVisibility(View.INVISIBLE);
        TextIP.setText("");
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