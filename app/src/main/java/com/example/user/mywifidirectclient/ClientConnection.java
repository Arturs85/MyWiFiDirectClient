package com.example.user.mywifidirectclient;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.wifi.p2p.WifiP2pInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;

/**
 * Created by user on 2017.07.02..
 */

class ClientConnection extends Thread {
    String TAG = "ClientConnection";
    DataOutputStream dao;
    private Handler mHandler;
    WifiP2pInfo connectionInfo;
    private Bitmap bitmap;
    int jpgQuality = 20;
volatile boolean isRunning = true;
    SendingThread sendingThread;
    DatagramSocket clientSocket; //recieving socket
   DatagramSocket sendingSocket;
    ArrayList<Byte>dataToSend;
   boolean dataUpdated=false;
    ClientConnection(Handler handler, WifiP2pInfo info) {
        connectionInfo = info;
        mHandler = handler;
initializeSendingList(2);
        sendingThread = new SendingThread();
        sendingThread.start();
    }

    public Bitmap getBitmap() {

        return bitmap;
    }
void initializeSendingList(int size){
    dataToSend=new ArrayList<>();
for(int i=0;i<=size;i++){
    dataToSend.add(new Byte((byte)0));
}
}

    public synchronized void sendMessageToMain(String msg, int incomingMsg) {
        Log.e(TAG, "sending message to main act: " + msg);

        Bundle messageBundle = new Bundle();
        messageBundle.putString("msg", msg);
        messageBundle.putInt(null, incomingMsg);
        Message message = new Message();
        message.setData(messageBundle);
        mHandler.sendMessage(message);

    }

    @Override
    public void run() {
        recieveDatagram();
    }

    void recieveDatagram() {
        String host = connectionInfo.groupOwnerAddress.getHostAddress();

        int port = 8888;// intent.getExtras().getInt(EXTRAS_GROUP_OWNER_PORT);
        int SOCKET_TIMEOUT = 1000;
//        DatagramSocket clientSocket = null;
       try {
//
           clientSocket = new DatagramSocket(port);
           Log.d(TAG, "Opened client socket  ");

       }
        catch (SocketException e) {
            e.printStackTrace();
            Log.e(TAG,"socket creating err");
        }
           try {

            InetAddress address = InetAddress.getByName("192.168.49.255");
            // clientSocket.joinGroup(address);

            Log.d(TAG, "Client socket is bound:  " + clientSocket.isBound());//socket.isConnected());
            byte[] buf = new byte[100024];
            byte[] sizeBuf = new byte[20];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            DatagramPacket packetSize = new DatagramPacket(sizeBuf, sizeBuf.length);


            while (isRunning) {
                //  clientSocket.receive(packetSize);
                // byte[] sizeBytes = packetSize.getLength()
                clientSocket.receive(packet);
                byte[] data = packet.getData();
                Log.d(TAG, "data recieved : " + data.length + "getLength = " + packet.getLength());

                bitmap = BitmapFactory.decodeByteArray(data, 0, packet.getLength()); //length??

                if (bitmap != null) {
                    //  Log.d(TAG, "Read from the stream: ");
                    // bitmap = recievedBitmap;
//recievedBitmap.recycle();
                    sendMessageToMain("kadrs", packet.getLength());
                } else {
                    Log.d(TAG, "null recieved");
                    // break;
                }
            }

        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        } finally {
            if (clientSocket != null) {

                try {
                    clientSocket.close();
                } catch (Exception e) {
                    // Give up
                    e.printStackTrace();
                }

            }
        }


    }

    void readTCP() {

        String host = connectionInfo.groupOwnerAddress.getHostAddress();
        //    Socket socket = new Socket();
        int port = 8888;// intent.getExtras().getInt(EXTRAS_GROUP_OWNER_PORT);
        int SOCKET_TIMEOUT = 1000;
        DatagramSocket clientSocket = null;

        try {
            Log.d(TAG, "Opening client socket - ");
//            socket.bind(null);
            //          socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);
            clientSocket = new DatagramSocket(8888);
            InetAddress address = InetAddress.getByName("192.168.49.255");
            // clientSocket.joinGroup(address);

            Log.d(TAG, "Client socket - " + clientSocket.toString());//socket.isConnected());
            DataInputStream input;
            byte[] buf = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            clientSocket.receive(packet);
            byte[] data = packet.getData();
            Log.d(TAG, "data recieved : " + new String(data));

/*            input = (new DataInputStream(socket.getInputStream()));
                while (!Thread.currentThread().isInterrupted()) {
                    int lenght = input.readInt();
                    if (lenght > 0) {
                        byte[] messageImage = new byte[lenght];
                        input.readFully(messageImage);
                        Bitmap recievedBitmap = BitmapFactory.decodeByteArray(messageImage, 0, messageImage.length);
                        //Bitmap recievedBitmap = BitmapFactory.decodeStream(input);
                        //input.reset();
                        if (recievedBitmap != null) {
                            Log.d(TAG, "Read from the stream: ");
                            bitmap = recievedBitmap;
//recievedBitmap.recycle();
                            sendMessageToMain("kadrs", lenght);
                        } else {
                            Log.d(TAG, "null recieved");
                            // break;
                        }
                    }

                }
                input.close();*/
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        } finally {
            if (clientSocket != null) {
                    try {
                        clientSocket.close();
                    } catch (Exception e) {
                        // Give up
                        e.printStackTrace();
                    }

            }
        }


    }

    class SendingThread extends Thread {

        @Override
        public void run(){
    sendDatagram();
}
        void sendDatagram() {
            try {
                Log.e(TAG, "sending thread started");

                /**
                 * Create a server socket and wait for client connections. This
                 * call blocks until a connection is accepted from a client
                 */
                //ServerSocket serverSocket = new ServerSocket(8888);
                //Socket client = serverSocket.accept();

                InetAddress inetAddress = InetAddress.getByName("192.168.49.1");
                try {
//
                    sendingSocket = new DatagramSocket(8889);
                    Log.d(TAG, "Opened client sending socket  ");

                }
                catch (SocketException e) {
                    e.printStackTrace();
                    Log.e(TAG,"socket creating err");
                }
                //DatagramSocket datagramSocket = new DatagramSocket(8888);
                // sendMessageToMain("SocketInitialized", 0); //to start preview callbacks
                int old = jpgQuality;

                while (isRunning) {
                    synchronized (this) {
                        if (dataUpdated) {

//Byte[] data = (Byte[])dataToSend.toArray();
                            byte[] dataRaw = new byte[dataToSend.size()];
                           int i = 0;
                            for (Byte b:dataToSend) {
                                dataRaw[i++]=b;
                            }
                          //  byte[] data = dataToSend;//((Integer) jpgQuality).toString().getBytes();
                            //   byte[] size = (Integer.toString(data.length)).getBytes();
                            //    Log.e(TAG, "size array length: " + size.length);

                            // DatagramPacket packetSize = new DatagramPacket(size, size.length, inetAddress, 8888);
//datagramSocket.send(packetSize);
                            DatagramPacket packet = new DatagramPacket(dataRaw, dataRaw.length, inetAddress, 8889);
                            sendingSocket.send(packet);
                            Log.e(TAG, "sending package, length: " + dataRaw.length);
dataUpdated=false;                        }
                    }
                }


                /**
                 * If this code is reached, a client has connected and transferred data
                 * Save the input stream from the client as a JPEG file
                 */
                // dao = new DataOutputStream (client.getOutputStream());
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());

            }

        }

    }
}
