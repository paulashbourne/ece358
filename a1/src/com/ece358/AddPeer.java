package com.ece358;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
        socketOutputStream.write(new AddPeerRequest(address, port).toBytes());
        AddPeerResponse response =
            (AddPeerResponse) Utils.readResponseFromInputStream(socketInputStream);
        System.out.println(
            String.format("Added peer [address=%s, port=%s]", args[0], args[1]));
        socketOutputStream.close();

        for (Peer peer : response.peers) {
          if (peer.getAddress().equals(address) && peer.getPort().equals(port)) {
            continue;
          }

          socket = new Socket(peer.getAddress(), peer.getPort());
          socketOutputStream = socket.getOutputStream();
          socketOutputStream.write(new AddPeerRequest(address, port).toBytes());
          AddPeerResponse addPeerResponse = (AddPeerResponse) getResponse(socket);
          if (addPeerResponse.success) {
            System.out.println(
                String.format("Added peer [address=%s, port=%d]", peer.getAddress(),
                    peer.getPort()));
          }
        }

        peers.add(new Peer(args[0], Integer.valueOf(args[1])));
      } catch (IOException e) {
        System.err.println(e);
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

  private Response getResponse(Socket socket) throws IOException {
    Response response = Utils.readResponseFromInputStream(socket.getInputStream());
    return response;
  }

  private void handleSocket(Socket socket) throws IOException {
    Request request = Utils.readRequestFromInputStream(socket.getInputStream());
    Response response = handleRequest(request);
    socket.getOutputStream().write(response.toBytes());
    socket.close();
  }

  public Response handleRequest(Request untypedRequest) throws IOException {
    if (untypedRequest instanceof AddPeerRequest) {
      AddPeerRequest request = (AddPeerRequest) untypedRequest;
      Peer newPeer = new Peer(request.address, request.port);
      Response response = new AddPeerResponse(true, peers);
      peers.add(newPeer);
      System.out.println(
          String.format("Added peer [address=%s, port=%d]", newPeer.getAddress(),
              newPeer.getPort()));
      return response;
    } else {
      System.err.println("Command not recognized");
      throw new RuntimeException();
    }
  }
}
