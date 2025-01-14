package com.example.clienthomecloud;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

public class MainActivity extends AppCompatActivity {

    private static final int BROWSER_IMAGE = 1;
    private static final int OPEN_CONECTION = 2;
    private static final int CLOSE_CONECTION = 3;
    private static final int SELECT_PICTURE = 1;

    private String selectedImagePath = "";

    TextView TextIP;
    Spinner spinner;
    TextView textStatus;
    Button btnConnect, btnDisconnect, btnBrowser, btnGetImage, btnListActivity, btnScan;
    ArrayList<String> listPhotos;
    ArrayList<String> ipList;
    ArrayAdapter<String> adapterSpinner;
    static public  Connection mConnect  = null;
    private  String     HOST      = "";
    private  int        PORT      = 3345;
    private  String     LOG_TAG   = "SOCKET";
    SharedPreferences.Editor editor;
    static public StatusCode statusCode = new StatusCode();
    static public int status = 2;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextIP = (TextView) findViewById(R.id.TextIP);
        spinner = (Spinner) findViewById(R.id.spinner);
        textStatus = (TextView) findViewById(R.id.textStatus);
        btnConnect = (Button) findViewById(R.id.btnConnect);
        btnDisconnect = (Button) findViewById(R.id.btnDisconnect);
        btnBrowser = (Button) findViewById(R.id.btnBrowser);
        btnGetImage = (Button) findViewById(R.id.btnGetImage);
        btnListActivity = (Button) findViewById(R.id.btnOpenListActivity);
        btnScan = (Button) findViewById(R.id.btnScan);
        btnDisconnect.setVisibility(View.INVISIBLE);
        btnGetImage.setVisibility(View.INVISIBLE);
        btnBrowser.setVisibility(View.INVISIBLE);
        btnListActivity.setVisibility(View.INVISIBLE);
        btnScan.setVisibility(View.INVISIBLE);
        requestMultiplePermissions();

        ipList = new ArrayList<>();
        adapterSpinner = new ArrayAdapter(this, android.R.layout.simple_spinner_item, ipList);
        // Определяем разметку для использования при выборе элемента
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapterSpinner);
        AdapterView.OnItemSelectedListener itemSelectedListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                Log.d("Test", (String)parent.getItemAtPosition(position));
                // Получаем выбранный объект
                String item = (String)parent.getItemAtPosition(position);
                TextIP.setText(item);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d("Test", "Nothing selected");
            }
        };
        spinner.setOnItemSelectedListener(itemSelectedListener);

        UpdateStatus();
        CreateThreadCheckStatus();
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

    public void onGetImageClick(View view){
            GetImage();
    }

    public void BrowserImage(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
    }

    public void GetImage(){
        listPhotos = mConnect.getData();
    }

    public void OpenConection(){
        // Создание подключения
        Log.d("Test", TextIP.getText().toString());
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
                            status == 1 || status == 5 || status == 6 || status == 10 || status == 11 || status == 13 || status == 14?
                                    "#00FF00" : "#FF0000"
                    ));

                    switch (status){
                        case 1: case 5: case 6: case 7: case 8:
                            btnConnect.setVisibility(View.INVISIBLE);
                            btnDisconnect.setVisibility(View.VISIBLE);
                            btnGetImage.setVisibility(View.VISIBLE);
                            btnBrowser.setVisibility(View.VISIBLE);
                            btnListActivity.setVisibility(View.INVISIBLE);
                            btnScan.setVisibility(View.INVISIBLE);
                            btnListActivity.setEnabled(false);
                            btnScan.setEnabled(false);
                            btnBrowser.setEnabled(true);
                            btnGetImage.setEnabled(true);
                            btnDisconnect.setEnabled(true);
                            TextIP.setEnabled(false);
                            break;
                        case 2: case 3: case 4:
                            btnConnect.setVisibility(View.VISIBLE);
                            btnScan.setVisibility(View.VISIBLE);
                            btnDisconnect.setVisibility(View.INVISIBLE);
                            btnGetImage.setVisibility(View.INVISIBLE);
                            btnBrowser.setVisibility(View.INVISIBLE);
                            btnListActivity.setVisibility(View.INVISIBLE);
                            btnListActivity.setEnabled(false);
                            btnConnect.setEnabled(true);
                            btnScan.setEnabled(true);
                            TextIP.setEnabled(true);
                            break;
                        case 11:
                            if(listPhotos != null && listPhotos.size() > 0){
                                btnListActivity.setVisibility(View.VISIBLE);
                                btnListActivity.setEnabled(true);
                            }
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
            if(requestCode == 123){
                // Запросить и получить фотки;
                Log.d("Test", "Запрос фоток");
                ArrayList<String> selectedPhotos = data.getStringArrayListExtra("SelectedPhotos");
                for (String photo: selectedPhotos) {
                    Log.d("TestImage", photo);
                }
                mConnect.getPhotos(selectedPhotos);
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
            Log.d("Test", "Ошибка курсора");
        }
        return path;
    }

    public void SendImage() throws Exception {
        Connection connection = MainActivity.mConnect;
        connection.sendData(selectedImagePath);
    }

    public void onOpenListActivity(View view) {
        Intent intent = new Intent(this, PhotosListActivity.class);
        intent.putStringArrayListExtra("Photos", listPhotos);
        startActivityForResult(intent, 123);
    }

    public void onScanClick(View view) {
        new ScanIpTask().execute();
    }

    private class ScanIpTask extends AsyncTask<Void, String, Void> {

        /*
        Scan IP 192.168.1.100~192.168.1.110
        you should try different timeout for your network/devices
         */
        static final String subnet = "192.168.1.";
        static final int lower = 2;
        static final int upper = 225;
        static final int timeout = 10;

        @Override
        protected void onPreExecute() {
            ipList.clear();
            status = 13;
            btnScan.setEnabled(false);
            btnConnect.setEnabled(false);
            Log.d("SCAN", "Start Scan");
            adapterSpinner.notifyDataSetChanged();
        }

        @Override
        protected Void doInBackground(Void... params) {

            for (int i = lower; i <= upper; i++) {
                String host = subnet + i;

                try {
                    InetAddress inetAddress = InetAddress.getByName(host);
                    if (inetAddress.isReachable(timeout)) {
                        publishProgress(inetAddress.toString());
                    }

                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            ipList.add(values[0].split("/")[1]);
            Log.d("SCAN", values[0].split("/")[1]);
            adapterSpinner.notifyDataSetChanged();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Toast.makeText(MainActivity.this, "Done", Toast.LENGTH_LONG).show();
            status = 14;
            btnScan.setEnabled(true);
            btnConnect.setEnabled(true);
            Log.d("SCAN", "Done Scan");
        }
    }
}