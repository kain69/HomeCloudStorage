package com.example.clienthomecloud;

import static java.lang.Thread.sleep;

import android.util.Log;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
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
                while (mSocket != null && !mSocket.isClosed()) {

                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * Метод закрытия сокета
     */
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
            } catch (IOException e) {
                Log.e(LOG_TAG, "Ошибка при закрытии сокета :"
                        + e.getMessage());
            } finally {
                mSocket = null;
            }
        }
        else{
            Log.d(LOG_TAG, "Соединение не существует");
        }
        mSocket = null;
    }
    /**
     * Метод отправки данных
     */
    public void sendData(byte[] data) throws Exception {
        // Проверка открытия сокета
        if (mSocket == null || mSocket.isClosed()) {
            throw new Exception("Ошибка отправки данных. " +
                    "Сокет не создан или закрыт");
        }
        // Отправка данных
        try {
            mSocket.getOutputStream().write(data);
            mSocket.getOutputStream().flush();
        } catch (IOException e) {
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
