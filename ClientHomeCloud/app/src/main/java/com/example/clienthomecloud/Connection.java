package com.example.clienthomecloud;

import static java.lang.Thread.sleep;

import android.os.Environment;
import android.util.Log;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.Socket;
import java.net.UnknownHostException;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import android.widget.TextView;

public class Connection {
    private  Socket  mSocket = null;
    private BufferedReader   br  = null;
    private DataOutputStream oos = null;
    private DataInputStream  ois = null;
    private  String  mHost   = null;
    private  int     mPort   = 0;

    public static final String LOG_TAG = "SOCKET";

    public Connection() {}

    public Connection (final String host, final int port) {
        this.mHost = host;
        this.mPort = port;
    }

    // Метод открытия сокета
    public void openConnection() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mSocket != null) {
                        Log.d(LOG_TAG, "Соединение уже установленно");
                    } else {
                        mSocket = new Socket(mHost, mPort);
                        br = new BufferedReader(new InputStreamReader(System.in));
                        oos = new DataOutputStream(mSocket.getOutputStream());
                        ois = new DataInputStream(mSocket.getInputStream());
                        Log.d(LOG_TAG, "Client connected to socket.");
                        // проверяем живой ли канал и работаем если живой
                        Log.d(LOG_TAG, "Iam Connected");
                        MainActivity.status = 1;
                    }
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                    MainActivity.status = 3;
                } catch (IOException e) {
                    MainActivity.status = 3;
                    e.printStackTrace();
                } catch (Exception e) {
                    Log.e(LOG_TAG, e.getMessage());
                }
            }
        }).start();
    }

    public void closeConnection() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mSocket != null && !mSocket.isClosed()) {
                        oos.writeUTF("quit");
                        System.out.println("Client kill connections");
                        oos.flush();
                        ois.close();
                        oos.close();
                        mSocket.close();
                        MainActivity.status = 2;
                    }
                    else{
                        Log.d(LOG_TAG, "Соединение не существует");
                        MainActivity.status = 4;
                    }
                    mSocket = null;
                } catch (IOException e) {
                    MainActivity.status = 4;
                    Log.e(LOG_TAG, "Ошибка при закрытии сокета :"
                            + e.getMessage());
                } catch (Exception e) {
                    Log.e(LOG_TAG, e.getMessage());
                } finally {
                    mSocket = null;
                }
            }
        }).start();
    }

    public void sendData(String urlImage) throws Exception {
        MainActivity.status = 5;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Проверка открытия сокета
                    if (mSocket == null || mSocket.isClosed()) {
                        MainActivity.status = 4;
                        throw new Exception("Ошибка отправки данных. " +
                                "Сокет не создан или закрыт");
                    }
                    // Отправка данных
                    oos.writeUTF("image"); // Ало, сервер, лови картинку
                    Log.d("TEST", "Я начал отправку");
                    oos.flush();
                    Log.d("TEST", "Вот кст путь " + urlImage);
                    File file = new File(urlImage);
                    Log.d("TEST", "Я получил файл");
                    FileInputStream inF = new FileInputStream(file);
                    Log.d("TEST", "Я закинул файл в поток");
                    byte[] bytes = new byte[5*1024];
                    int count;
                    long lenght = file.length();
                    String[] temp = urlImage.split("/");
                    String fileName = temp[temp.length - 1];
                    oos.writeUTF(fileName);
                    oos.writeLong(lenght);
                    Log.d("TEST", "Я отправил размер файла");
                    while ((count = inF.read(bytes)) > -1) {
                        oos.write(bytes, 0, count);
                    }
                    Log.d("TEST", "Я отправил файл");
                    mSocket.getOutputStream().flush();
                    MainActivity.status = 6;

                } catch (IOException e) {
                    MainActivity.status = 7;
                    Log.e(LOG_TAG,"Ошибка отправки данных : "
                            + e.getMessage());
                } catch (Exception e) {
                    Log.e(LOG_TAG, e.getMessage());
                }
            }
        }).start();
    }
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        closeConnection();
    }

    public ArrayList<String> getData() {
        MainActivity.status = 12;
        ArrayList<String> listPhotos = new ArrayList<String>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Проверка открытия сокета
                    if (mSocket == null || mSocket.isClosed()) {
                        MainActivity.status = 4;
                        throw new Exception("Ошибка олучения данных. " +
                                "Сокет не создан или закрыт");
                    }
                    oos.writeUTF("Allimage"); // Ало, сервер, лови картинку
                    Log.d("TEST", "Запрос фоток");
                    oos.flush();
                    int lenght = ois.readInt();
                    Log.d("TEST", String.valueOf(lenght));
                    for (int i = 0; i < lenght; i++){
                        listPhotos.add(ois.readUTF());
                        Log.d("IMAGESERVER", listPhotos.get(listPhotos.size() - 1));
                    }
                    oos.flush();
                    MainActivity.status = 11;

                } catch (IOException e) {
                    MainActivity.status = 7;
                    Log.e(LOG_TAG,"Ошибка отправки данных : "
                            + e.getMessage());
                } catch (Exception e) {
                    Log.e(LOG_TAG, e.getMessage());
                }
            }
        }).start();
        return listPhotos;
    }

    public void getPhotos(ArrayList<String> selectedPhotos) {
        MainActivity.status = 12;
        ArrayList<String> SelectedPhotos = selectedPhotos;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Проверка открытия сокета
                    if (mSocket == null || mSocket.isClosed()) {
                        MainActivity.status = 4;
                        throw new Exception("Ошибка олучения данных. " +
                                "Сокет не создан или закрыт");
                    }
                    oos.writeUTF("SelectedImage"); // Ало, сервер, лови картинку
                    oos.writeInt(SelectedPhotos.size());
                    Log.d("TEST", "Дай мне выбранные фотки");
                    for (int i = 0; i < SelectedPhotos.size(); i++){
                        oos.writeUTF(SelectedPhotos.get(i));
                    }
                    oos.flush();
                    for (int i = 0; i < SelectedPhotos.size(); i++){
                        String name = ois.readUTF();
                        long lenght = ois.readLong();
                        Log.d("Dir", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + name);
                        FileOutputStream outFile = new FileOutputStream(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + name);
                        byte[] bytes = new byte[5*1024];

                        int count, total=0;
                        while ((count = ois.read(bytes)) > -1) {
                            total+=count;
                            outFile.write(bytes, 0, count);
                            if (total==lenght) break;
                        }
                        outFile.close();
                    }
                    oos.flush();
                    MainActivity.status = 11;

                } catch (IOException e) {
                    MainActivity.status = 7;
                    Log.e(LOG_TAG,"Ошибка отправки данных : "
                            + e.getMessage());
                } catch (Exception e) {
                    Log.e(LOG_TAG, e.getMessage());
                }
            }
        }).start();
    }
}