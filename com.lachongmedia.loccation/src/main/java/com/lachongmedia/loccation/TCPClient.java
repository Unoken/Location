package com.lachongmedia.loccation;

/**
 * Created by Unoken on 22/12/13.
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import android.app.AlertDialog;
import android.util.Log;

public class TCPClient {
    private String serverMessage;
    public static final String SERVERIP = "118.70.171.240";
    public static final int SERVERPORT = 5555;
    private OnMessageReceived mMessageListener = null;

    PrintWriter out;
    BufferedReader in;

    Socket socket;

    public TCPClient(OnMessageReceived listener) {
        mMessageListener = listener;
    }

    public TCPClient() {
    }

    public boolean isConnected() {
        if (socket == null) {
            return false;
        }
        return socket.isConnected();
    }

    public int sendMessage(String message) {
        if (out == null) {
            return -2;
        }
        if (out.checkError()) {
            return -1;
        }
        out.println(message);
        out.flush();
        return 0;
    }

    public void run() {

        try {
            // InetAddress serverAddr = InetAddress.getByName(SERVERIP);
            Log.e("TCP Client", "C: Connecting...");
            socket = new Socket(SERVERIP, SERVERPORT);

            try {
                out = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream())), true);
                // in = new BufferedReader(new InputStreamReader(
                // socket.getInputStream()));

            } catch (Exception e) {
                // System.out.println("Exception: " + e.getMessage());
                Log.e("TCP", "S: Error", e);

            } finally {
            }

        } catch (Exception e) {
            // System.out.println("Exception: " + e.getMessage());
            Log.e("TCP", "C: Error", e);

        }

    }

    public void close() {
        try {
            if (out != null) {
                out.close();
            }
            // in.close();
            if (socket != null) {
                socket.close();
            }
        } catch (Exception ex) {

        }
    }

    public interface OnMessageReceived {
        public void messageReceived(String message);
    }
}

