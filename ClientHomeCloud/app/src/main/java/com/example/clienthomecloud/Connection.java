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
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.io.IOException;
import java.net.Socket;
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

    public Connection (final String host, final int port)
    {
        this.mHost = host;
        this.mPort = port;
    }

    // Метод открытия сокета
    public void openConnection() throws Exception
    {
        if(mSocket != null) {
            Log.d(LOG_TAG, "Соединение уже установленно");
        }
        else {
            try {
                mSocket = new Socket(mHost, mPort);
                br = new BufferedReader(new InputStreamReader(System.in));
                oos = new DataOutputStream(mSocket.getOutputStream());
                ois = new DataInputStream(mSocket.getInputStream());
                Log.d(LOG_TAG, "Client connected to socket.");
                // проверяем живой ли канал и работаем если живой
                Log.d(LOG_TAG, "Iam Connected");
                MainActivity.status = 1;
                MainActivity.statusColor = "#00FF00";

                while (mSocket != null && !mSocket.isClosed()) {

                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
                MainActivity.status = 3;
                MainActivity.statusColor = "#FF0000";
            } catch (IOException e) {
                MainActivity.status = 3;
                MainActivity.statusColor = "#FF0000";
                e.printStackTrace();
            }
        }
    }

    public void closeConnection()
    {
        if (mSocket != null && !mSocket.isClosed()) {
            try {
                oos.writeUTF("quit");
                System.out.println("Client kill connections");
                oos.flush();
                ois.close();
                oos.close();
                mSocket.close();
                MainActivity.status = 2;
                MainActivity.statusColor = "#00FF00";
            } catch (IOException e) {
                MainActivity.status = 4;
                MainActivity.statusColor = "#FF0000";
                Log.e(LOG_TAG, "Ошибка при закрытии сокета :"
                        + e.getMessage());
            } finally {
                mSocket = null;
            }
        }
        else{
            Log.d(LOG_TAG, "Соединение не существует");
            MainActivity.status = 4;
            MainActivity.statusColor = "#FF0000";
        }
        mSocket = null;
    }

    public void sendData(String urlImage) throws Exception {

        // Проверка открытия сокета
        if (mSocket == null || mSocket.isClosed()) {
            MainActivity.status = 4;
            MainActivity.statusColor = "#FF0000";
            throw new Exception("Ошибка отправки данных. " +
                    "Сокет не создан или закрыт");
        }
        // Отправка данных
        try {
            MainActivity.status = 5;
            MainActivity.statusColor = "#00FF00";
            oos.writeUTF("image"); // Ало, сервер, лови картинку
            oos.flush();
            File file = new File(urlImage);
            FileInputStream inF = new FileInputStream(file);
            byte[] bytes = new byte[5*1024];
            int count;
            long lenght = file.length();
            oos.writeLong(lenght);
            while ((count = inF.read(bytes)) > -1) {
                oos.write(bytes, 0, count);
            }
            mSocket.getOutputStream().flush();
            MainActivity.status = 5;
            MainActivity.statusColor = "#00FF00";
        } catch (IOException e) {
            MainActivity.status = 7;
            MainActivity.statusColor = "#FF0000";
            throw new Exception("Ошибка отправки данных : "
                    + e.getMessage());
        }
    }
    @Override
    protected void finalize() throws Throwable
    {
        super.finalize();
        closeConnection();
    }

}