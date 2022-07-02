package com.example.clienthomecloud;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
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
    private static final int SELECT_PICTURE = 1;

    private String selectedImagePath = "";

    EditText TextIP;
    TextView textStatus;
    Button btnConnect, btnDisconnect, btnBrowser;
    static public  Connection mConnect  = null;
    private  String     HOST      = "";
    //private  String     HOST      = "192.168.1.109";
    private  int        PORT      = 3345;
    private  String     LOG_TAG   = "SOCKET";
    Thread   connectThread;
    SharedPreferences.Editor editor;
    static public StatusCode statusCode = new StatusCode();
    static public int status = 2;

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

        UpdateStatus();
        CreateThreadCheckStatus();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        editor = sharedPreferences.edit();
        String HOSTEditor = sharedPreferences.getString("IP", "");
        Log.d("EDITOR", HOSTEditor);
        if(HOSTEditor != "") {
            HOST = HOSTEditor;
            TextIP.setText(HOST);
            mConnect = new Connection(HOST, PORT);
            mConnect.openConnection();
        }
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
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
    }

    public void OpenConection(){
        // Создание подключения
        HOST = TextIP.getText().toString();
        mConnect = new Connection(HOST, PORT);
        // Открытие сокета в отдельном потоке
        mConnect.openConnection();
//        UpdateStatus();
    }

    public void CloseConection(){
        // Закрытие соединения
        if (mConnect != null) {
            mConnect.closeConnection();
            HOST = "";
        } else {
            Log.d(LOG_TAG, "Соединение не существует");
        }
//        UpdateStatus();
    }

    void CreateThreadCheckStatus(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                int oldStatus = status;
                while(true){
                    if(oldStatus != status){
                        UpdateStatus();
                        oldStatus = status;
                    }
                }
            }
        }).start();
    }

    public void UpdateStatus(){
        runOnUiThread(new Runnable(){
            @Override
            public void run() {
                try {
                    textStatus.setText("" + statusCode.listWithCodes.get(status));
                    textStatus.setTextColor(Color.parseColor(
                            status == 1 || status == 5 || status == 6 ?
                                    "#00FF00" : "#FF0000"
                    ));

                    switch (status){
                        case 1: case 5: case 6: case 7: case 8:
                            btnConnect.setVisibility(View.INVISIBLE);
                            btnDisconnect.setVisibility(View.VISIBLE);
                            btnBrowser.setVisibility(View.VISIBLE);
                            btnBrowser.setEnabled(true);
                            btnDisconnect.setEnabled(true);
                            TextIP.setEnabled(false);
                            break;
                        case 2: case 3: case 4:
                            btnConnect.setVisibility(View.VISIBLE);
                            btnDisconnect.setVisibility(View.INVISIBLE);
                            btnBrowser.setVisibility(View.INVISIBLE);
                            btnConnect.setEnabled(true);
                            TextIP.setEnabled(true);
                            TextIP.setText("");
                            break;

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                selectedImagePath = getPath(selectedImageUri);
                try {
                    SendImage();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String getPath(Uri uri) {
        // just some safety built in
        if( uri == null ) {
            // TODO perform some logging or show user feedback
            return null;
        }
        // try to retrieve the image from the media store first
        // this will only work for images selected from gallery
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor;
        if (Build.VERSION.SDK_INT > 23) {
            // Will return "image:x*"
            String wholeID = DocumentsContract.getDocumentId(uri);
            // Split at colon, use second item in the array
            String id = wholeID.split(":")[1];
            // where id is equal to
            String sel = MediaStore.Images.Media._ID + "=?";

            cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection, sel, new String[]{id}, null);
        } else {
            cursor = getContentResolver().query(uri, projection, null, null, null);
        }
        String path = null;
        try {
            int column_index = cursor
                    .getColumnIndex(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            path = cursor.getString(column_index).toString();
            cursor.close();
        } catch (NullPointerException e) {
            Log.d("Test", "Бля");
        }
        return path;
    }

    public void SendImage() throws Exception {
        Connection connection = MainActivity.mConnect;
        connection.sendData(selectedImagePath);
    }

    @Override
    protected void onStop() {
        editor.remove("IP");
        if(HOST.matches ("^(\\d{1,3}\\.){3}\\d{1,3}")) {
            editor.putString("IP", HOST);
        }else {
            editor.putString("IP", "");
        }
        Log.e("EDITOR", HOST);
        editor.commit();

        Log.d("DESTROY", "-Умер. -очень жаль( -Это мое имя Умерчик!!!");
        super.onStop();
        Log.d("DESTROY", "-Умер. -очень жаль( -Это мое имя Умерчик!!!");
        CloseConection();
        finish();
    }
}