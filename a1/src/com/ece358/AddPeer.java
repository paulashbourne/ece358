package com.ece358;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class AddPeer {
  private List<PeerInfo> peerInfo;

  public static void main(String[] args) throws IOException {
    AddPeer program = new AddPeer();
    program.start(args);
  }

  public AddPeer() {
    peerInfo = new ArrayList<>();
  }

  private void start(String[] args) {
    ServerSocket serverSocket;
    // TODO(jgulbronson) - add limit to retries
    while (true) {
      try {
        serverSocket = new ServerSocket(new Random().nextInt(1000) + 10_000);
        break;
      } catch (IOException e) {
        // Binding likely failed because the port is in use, try again.
      }
    }

    String address;
    int port;
    try {
      address = InetAddress.getLocalHost().getHostAddress();
      port = serverSocket.getLocalPort();
      System.out.println(String.format("%s %d", address, port));
    } catch (UnknownHostException e) {
      e.printStackTrace();
    }

    if (args.length > 0) {
      Socket socket;
      try {
        socket = new Socket(args[0], Integer.valueOf(args[1]));
        OutputStream socketOutputStream = socket.getOutputStream();
        socketOutputStream.write(
            String.format("ADDPEER %s %s\r\n\r\n", args[0], args[1]).getBytes());
        socketOutputStream.close();
        peerInfo.add(new PeerInfo(args[0], Integer.valueOf(args[1])));
      } catch (IOException e) {
        System.exit(1);
      }
    }

    while (true) {
      try {
        handleSocket(serverSocket.accept());
      } catch (IOException e) {
      }
    }
  }

  private void handleSocket(Socket socket) throws IOException {
    InputStream inputStream = socket.getInputStream();
    byte readData[] = new byte[100];
    int bytesRead;
    ByteArrayOutputStream data = new ByteArrayOutputStream();
    while (true) {
      if ((bytesRead = inputStream.read(readData)) > 0) {
        data.write(Arrays.copyOfRange(readData, 0, bytesRead));
        String message = data.toString(StandardCharsets.US_ASCII.name());
        if (message.endsWith("\r\n\r\n")) {
          handleMessage(message.trim(), socket);
          break;
        }
      }
    }
  }

  private void handleMessage(String message, Socket socket) throws IOException {
    System.out.println(message);
    if (message.startsWith("ADDPEER")) {
      String[] command = message.split(" ");
      if (command.length != 3) {
        socket.getOutputStream().write("FAILURE".getBytes());
      } else {
        PeerInfo newPeerInfo = new PeerInfo(command[1], Integer.valueOf(command[2]));
        peerInfo.add(newPeerInfo);
        System.out.println(
            String.format("Added peer [address=%s, port=%d]", newPeerInfo.getAddress(),
                newPeerInfo.getPort()));
        socket.getOutputStream().write("SUCCESS".getBytes());
      }
    } else {
      socket.getOutputStream().write("FAILURE".getBytes());
    }
    socket.close();
  }
}
