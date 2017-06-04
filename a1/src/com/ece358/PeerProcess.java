package com.ece358;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class PeerProcess {
  private List<Peer> peers;
  private HashMap<Integer, String> localContentMappings;
  private HashMap<Integer, Peer> peerContentMappings;
  private Integer globalContentCounter;

  public PeerProcess() {
    peers = new LinkedList<>();
    localContentMappings = new LinkedHashMap<>();
    peerContentMappings = new LinkedHashMap<>();
    globalContentCounter = 0;
  }

  public void start(String[] args) throws SocketException {
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
    Enumeration interfaces = NetworkInterface.getNetworkInterfaces();
    while (interfaces.hasMoreElements()) {
      NetworkInterface networkInterface = (NetworkInterface) interfaces.nextElement();
      if (networkInterface.isLoopback()) {
        continue;
      }
      Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
      while (inetAddresses.hasMoreElements()) {
        InetAddress inetAddress = inetAddresses.nextElement();
        if (inetAddress.isLoopbackAddress()) {
          continue;
        }

        address = inetAddress.getHostAddress();
      }
    }
    port = serverSocket.getLocalPort();
    System.out.println(String.format("%s %d", address, port));

    if (args.length > 0) {
      try {
        Request request = new AddPeerRequest(address, port);
        AddPeerResponse response =
            (AddPeerResponse) Utils.sendAndGetResponse(args[0], Integer.valueOf(args[1]), request);
        System.out.println(
            String.format("Added peer [address=%s, port=%s]", args[0], args[1]));

        for (Peer peer : response.peers) {
          if (peer.getAddress().equals(address) && peer.getPort().equals(port)) {
            continue;
          }

          request = new AddPeerRequest(address, port);
          response = (AddPeerResponse) Utils.sendAndGetResponse(peer.getAddress(), peer.getPort(),
              request);
          if (response.success) {
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
    } else if (untypedRequest instanceof AllKeysRequest) {
      return new AllKeysResponse(true, localContentMappings.keySet());
    } else {
      System.err.println("Command not recognized");
      throw new RuntimeException();
    }
  }

  private void rebalance() {

  }
}
