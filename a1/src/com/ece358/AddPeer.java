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
  private List<Peer> peers;

  public static void main(String[] args) throws IOException {
    AddPeer program = new AddPeer();
    program.start(args);
  }

  public AddPeer() {
    peers = new ArrayList<>();
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

    String address = null;
    Integer port = null;
    try {
      address = InetAddress.getLocalHost().getHostAddress();
      port = serverSocket.getLocalPort();
      System.out.println(String.format("%s %d", address, port));
    } catch (UnknownHostException e) {
      System.exit(1);
    }

    if (args.length > 0) {
      Socket socket;
      try {
        socket = new Socket(args[0], Integer.valueOf(args[1]));
        OutputStream socketOutputStream = socket.getOutputStream();
        InputStream socketInputStream = socket.getInputStream();
        socketOutputStream.write(new Request(address, port).toWire());
        AddPeer.Response response =
            AddPeer.Response.Parser.fromWire(readMessageFromInputStream(socketInputStream));
        socketOutputStream.close();

        for (Peer peer : response.peers) {
          System.out.println(String.format("Trying to connect to %s:%s", peer.getAddress(), peer.getPort()));
          socket = new Socket(peer.getAddress(), peer.getPort());
          socketOutputStream = socket.getOutputStream();
          socketOutputStream.write(new Request(address, port).toWire());
        }

        peers.add(new Peer(args[0], Integer.valueOf(args[1])));
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

  private String readMessageFromInputStream(InputStream inputStream) throws IOException {
    byte readData[] = new byte[100];
    int bytesRead;
    ByteArrayOutputStream data = new ByteArrayOutputStream();
    while (true) {
      if ((bytesRead = inputStream.read(readData)) > 0) {
        data.write(Arrays.copyOfRange(readData, 0, bytesRead));
        String message = data.toString(StandardCharsets.US_ASCII.name());
        if (message.endsWith("\r\n\r\n")) {
          return message;
        }
      }
    }
  }

  private void handleSocket(Socket socket) throws IOException {
    String message = readMessageFromInputStream(socket.getInputStream());
    handleMessage(message, socket);
  }

  private void handleMessage(String message, Socket socket) throws IOException {
    if (message.startsWith("ADDPEER")) {
      Request request = Request.Parser.fromWire(message);
      Peer newPeer = new Peer(request.address, request.port);
      byte[] response = new Response(true, peers).toWire();
      peers.add(newPeer);
      System.out.println(
          String.format("Added peer [address=%s, port=%d]", newPeer.getAddress(),
              newPeer.getPort()));
      socket.getOutputStream().write(response);
    } else {
      socket.getOutputStream().write("FAILURE".getBytes(StandardCharsets.US_ASCII));
    }
    socket.close();
  }

  public static class Request {
    public final String address;
    public final Integer port;

    public Request(String address, Integer port) {
      this.address = address;
      this.port = port;
    }

    byte[] toWire() {
      return String.format("ADDPEER %s:%s\r\n\r\n", address, port).getBytes(StandardCharsets.US_ASCII);
    }

    public static class Parser {
      public static Request fromWire(String s) {
        s = s.trim().split(" ")[1];
        String address = s.split(":")[0];
        String port = s.split(":")[1];
        return new Request(address, Integer.valueOf(port));
      }
    }
  }

  public static class Response {
    private final boolean success;
    private List<Peer> peers;

    public Response(boolean success, List<Peer> peers) {
      this.success = success;
      this.peers = peers;
    }

    @Override public String toString() {
      return String.format("Response [success=%s]", success);
    }

    byte[] toWire() {
      if (!success) {
        return "FAILURE\r\n\r\n".getBytes();
      }
      StringBuilder sb = new StringBuilder()
          .append("SUCCESS");

      if (peers != null && peers.size() > 0) {
        sb.append("\n");
      }

      for (Peer peer : peers) {
        sb.append(String.format("%s:%s\n", peer.getAddress(), peer.getPort()));
      }
      return sb.append("\r\n\r\n").toString().getBytes(StandardCharsets.US_ASCII);
    }

    public static class Parser {
      public static Response fromWire(String s) {
        if (s.startsWith("FAILURE")) {
          return new Response(false, null);
        } else if (s.equals("SUCCESS\r\n\r\n")) {
          return new Response(true, new ArrayList<>());
        }

        s = s.trim();
        s = s.substring(s.indexOf("\n") + 1, s.length());
        List<Peer> peers = new ArrayList<>();
        for (String l : s.split("\n")) {
          String address = l.split(":")[0];
          String port = l.split(":")[1];
          peers.add(new Peer(address, Integer.valueOf(port)));
        }

        return new Response(true, peers);
      }
    }
  }
}
