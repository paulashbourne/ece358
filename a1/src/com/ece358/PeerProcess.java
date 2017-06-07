package com.ece358;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.*;

public class PeerProcess {
  private Peer me;
  private Set<Peer> peers;
  private HashMap<Integer, String> localContentMappings;
  private HashMap<Integer, Peer> peerContentMappings;
  private Integer globalContentCounter;

  public PeerProcess() {
    me = null;
    peers = new HashSet<>();
    localContentMappings = new LinkedHashMap<>();
    peerContentMappings = new LinkedHashMap<>();
    globalContentCounter = 0;
  }

  private static int getMinContentPerPeer(int numContent, int numPeers) {
    return (int) Math.floor((double)numContent / numPeers);
  }

  private static int getMaxContentPerPeer(int numContent, int numPeers) {
    return (int) Math.ceil((double)numContent / numPeers);
  }

  private HashMap<Peer, Integer> getContentCountByPeer() {
    HashMap<Peer, Integer> result = new HashMap<>();
    result.put(me, 0);
    for (Peer peer : peers) {
      result.put(peer, 0);
    }
    for (Peer peer : peerContentMappings.values()) {
      result.put(peer, result.get(peer) + 1);
    }
    return result;
  }

  /*
   * Finds a peer who has space to host more data
   */
  private Peer findPeerWithSpace(int maxContent, HashMap<Peer, Integer> contentCountByPeer) {
    for (Peer peer : peers) {
      if (contentCountByPeer.get(peer) < maxContent) {
        return peer;
      }
    }
    return null;
  }

  /*
   * Finds a peer who has space to host more data
   */
  private Peer findPeerWithExtraContent(int minContent, HashMap<Peer, Integer> contentCountByPeer) {
    for (Peer peer : peers) {
      if (contentCountByPeer.get(peer) > minContent) {
        return peer;
      }
    }
    return null;
  }

  /*
   *  Steals content from another peer until I meet the minimum
   */
  private void stealContent() throws IOException {
    int minContent = getMinContentPerPeer(peerContentMappings.size(), peers.size() + 1);
    HashMap<Peer, Integer> contentCountByPeer = getContentCountByPeer();
    while (contentCountByPeer.get(me) < minContent) {
      Peer victim = findPeerWithExtraContent(minContent, contentCountByPeer);
      // Pick a key from this peer
      Integer key = null;
      for (Map.Entry<Integer, Peer> entry : peerContentMappings.entrySet()) {
        Peer peer = entry.getValue();
        if (peer.equals(victim)) {
          key = entry.getKey();
          break;
        }
      }
      // Grab the data from the victim (no propagation)
      RemoveContentRequest request = new RemoveContentRequest(key, false);
      RemoveContentResponse response = (RemoveContentResponse) forwardRequest(victim, request);
      // Store the data locally
      localContentMappings.put(key, response.content);
      peerContentMappings.put(key, me);
      // Notify network of new mapping
      notifyContentMappingChange(me, key, true);
      // Refresh content counts
      contentCountByPeer = getContentCountByPeer();
    }
  }

  /*
   *  Forwards a request to another peer in the network
   */
  private Response forwardRequest(Peer dest, Request request) throws IOException {
    return Utils.sendAndGetResponse(dest.getAddress(), dest.getPort(), request);
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
    me = new Peer(address, port);
    System.out.println(String.format("%s %d", address, port));

    if (args.length == 2) {
      try {
        Request request = new AddPeerRequest(address, port);
        Peer destPeer = new Peer(args[0], Integer.valueOf(args[1]));
        AddPeerResponse response = (AddPeerResponse) forwardRequest(destPeer, request);
        globalContentCounter = response.counter;
        peerContentMappings = (HashMap<Integer, Peer>) response.peerContentMapping;
        //System.out.println(
        //    String.format("Added peer [address=%s, port=%s]", args[0], args[1]));

        for (Peer peer : response.peers) {
          if (peer.getAddress().equals(address) && peer.getPort().equals(port)) {
            // This is me
            me = peer;

          } else {
            // Ping the peer, let them know I'm here
            request = new AddPeerRequest(address, port);
            response = (AddPeerResponse) Utils.sendAndGetResponse(peer.getAddress(), peer.getPort(), request);
            //if (response.success) {
            //  System.out.println(
            //      String.format("Added peer [address=%s, port=%d]", peer.getAddress(), peer.getPort()));
            //}

            peers.add(peer);
          }
        }

        peers.add(destPeer);

        // Balance content
        stealContent();
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
    if (request instanceof RemovePeerRequest) {
      RemovePeerRequest removeRequest = (RemovePeerRequest) request;
      if (removeRequest.address.equals(me.getAddress()) && removeRequest.port.equals(
          me.getPort())) {
        // I am the peer being removed - kill this process
        System.exit(0);
      }
    }
  }

  private Response handleAddPeerRequest(AddPeerRequest request) {
    Peer newPeer = new Peer(request.address, request.port);
    Response response = new AddPeerResponse(true, peers, globalContentCounter, peerContentMappings);
    peers.add(newPeer);
    return response;
  }

  private Response handleRemovePeerRequest(RemovePeerRequest request) throws IOException {
    if (request.address.equals(me.getAddress()) && request.port.equals(me.getPort())) {
      // I am the peer being removed

      // Notify each of my peers that I am no longer in the network
      for (Peer peer : peers) {
        // Notify by forwarding the same request
        RemovePeerResponse response = (RemovePeerResponse)
          Utils.sendAndGetResponse(peer.getAddress(), peer.getPort(), request);
        if (response.success) {
          //System.out.println("Successfully notified peer");
        }
      }

      // Distribute my content to peers
      // If no more peers, then the content dies
      if (peers.size() > 0) {
        int maxContent = getMaxContentPerPeer(peerContentMappings.size(), peers.size());
        HashMap<Peer, Integer> contentCountByPeer = getContentCountByPeer();
        for (Map.Entry<Integer, String> entry : localContentMappings.entrySet()) {
          Integer key = entry.getKey();
          String content = entry.getValue();
          // Find a peer to send this data to
          Peer dest = findPeerWithSpace(maxContent, contentCountByPeer);
          // Update local mapping
          peerContentMappings.put(key, dest);
          // Forward the data - tell the peer to propagate the mapping change to the rest of
          // the network. Won't cause deadlock because I have already removed myself from
          // their mapping
          AddContentRequest forward = new AddContentRequest(dest.getAddress(), dest.getPort(), content, true);
          forwardRequest(dest, forward);
        }
      }

      // Reply and exit (exit handled by socket handler)
      return new RemovePeerResponse(true);

    } else {
      // Remove this peer from my list of peers
      Iterator<Peer> iterator = peers.iterator();
      while (iterator.hasNext()) {
        Peer peer = iterator.next();
        if (request.address.equals(peer.getAddress()) && request.port.equals(peer.getPort())) {
          iterator.remove();
          break;
        }
      }
      return new RemovePeerResponse(true);
    }
  }

  private Response handleAddContentRequest(AddContentRequest request) throws IOException {
    String content = request.content;
    // Prepare response
    AddContentResponse response = new AddContentResponse(true, globalContentCounter);

    if (!request.propagate) {
      // Accept the content and immediately return without updating anything else
      localContentMappings.put(globalContentCounter, content);
      peerContentMappings.put(globalContentCounter, me);
      return response;
    }

    // Check if I have space for more content
    int maxContent = getMaxContentPerPeer(
        peerContentMappings.size() + 1, // We're add new content, so +1
        peers.size() + 1 // peers.size() does not include me
    );
    HashMap<Peer, Integer> contentCountByPeer = getContentCountByPeer();
    if (contentCountByPeer.get(me) >= maxContent) {
      // Forward the data to a peer
      // Find an eligible peer
      Peer dest = findPeerWithSpace(maxContent, contentCountByPeer);
      // Forward the request - tell the peer to accept it without propogating changes
      // to the mappings (I'll take care of it)
      AddContentRequest forward = new AddContentRequest(dest.getAddress(), dest.getPort(), request.content, false);
      forwardRequest(dest, forward);
      // Update mapping
      peerContentMappings.put(globalContentCounter, dest);
      notifyContentMappingChange(dest, globalContentCounter, true);
    } else {
      localContentMappings.put(globalContentCounter, content);
      peerContentMappings.put(globalContentCounter, me);
      notifyContentMappingChange(me, globalContentCounter, true);
    }

    // Increment global content counter
    // Global counter for each peer
    globalContentCounter++;
    notifyCounterChange();

    return response;
  }

  private Response handleUpdateCounterRequest(UpdateCounterRequest request) {
    globalContentCounter = request.counter;
    return new UpdateCounterResponse(true);
  }

  private Response handleUpdateContentMappingRequest(UpdateContentMappingRequest request) {
    if (request.add) {
      peerContentMappings.put(request.key, new Peer(request.address, request.port));
    } else if (peerContentMappings.containsKey(request.key)) {
      peerContentMappings.remove(request.key);
    } else {
      return new UpdateContentMappingResponse(false);
    }

    return new UpdateContentMappingResponse(true);
  }

  private Response handleLookupContentRequest(LookupContentRequest request) throws IOException {
    Integer key = Integer.valueOf(request.key);
    if (localContentMappings.containsKey(key)) {
      return new LookupContentResponse(true, localContentMappings.get(key));
    } else if (!peerContentMappings.containsKey(key)) {
      return new LookupContentResponse(false, "");
    } else {
      Peer peer = peerContentMappings.get(key);
      LookupContentResponse response = (LookupContentResponse) forwardRequest(peer, request);
      if (response.success) {
        return new LookupContentResponse(true, response.content);
      }

      return new LookupContentResponse(false, "");
    }
  }

  private Response handleRemoveContentRequest(RemoveContentRequest request) throws IOException {
    int key = request.key;
    if (localContentMappings.containsKey(key)) {
      String content = localContentMappings.get(key);
      localContentMappings.remove(key);
      peerContentMappings.remove(key);
      if (request.propagate) {
        notifyContentMappingChange(me, key, false);
        // Steal content from another peer if I'm below the minimum
        stealContent();
      }

      return new RemoveContentResponse(true, content);
    } else if (peerContentMappings.containsKey(key)) {
      // Forward the request to the appropriate peer
      Peer peer = peerContentMappings.get(key);
      // Can't propogate due to deadlock - this process has to update mappings
      RemoveContentRequest forward = new RemoveContentRequest(key, false);
      RemoveContentResponse response = (RemoveContentResponse) forwardRequest(peer, forward);
      peerContentMappings.remove(key);
      notifyContentMappingChange(peer, key, false);
      return response;
    } else {
      return new RemoveContentResponse(false, "");
    }
  }

  private Response handleAllKeysRequest(AllKeysRequest request) {
    return new AllKeysResponse(true, localContentMappings.keySet());
  }

  public Response handleRequest(Request untypedRequest) throws IOException {

    if (untypedRequest instanceof AddPeerRequest) {
      /* ADD PEER REQUEST */
      return handleAddPeerRequest((AddPeerRequest) untypedRequest);

    } else if (untypedRequest instanceof AddContentRequest) {
      /* ADD CONTENT REQUEST */
      return handleAddContentRequest((AddContentRequest) untypedRequest);

    } else if (untypedRequest instanceof AllKeysRequest) {
      /* ALL KEYS REQUEST */
      return handleAllKeysRequest((AllKeysRequest) untypedRequest);

    } else if (untypedRequest instanceof RemovePeerRequest) {
      /* REMOVE PEER REQUEST */
      return handleRemovePeerRequest((RemovePeerRequest) untypedRequest);

    } else if (untypedRequest instanceof UpdateCounterRequest) {
      /* UPDATE COUNTER REQUEST */
      return handleUpdateCounterRequest((UpdateCounterRequest) untypedRequest);

    } else if (untypedRequest instanceof UpdateContentMappingRequest) {
      /* UPDATE CONTENT REQUEST */
      return handleUpdateContentMappingRequest((UpdateContentMappingRequest) untypedRequest);

    } else if (untypedRequest instanceof LookupContentRequest) {
      /* LOOKUP CONTENT REQUEST */
      return handleLookupContentRequest((LookupContentRequest) untypedRequest);

    } else if (untypedRequest instanceof RemoveContentRequest) {
      /* REMOVE CONTENT REQUEST */
      return handleRemoveContentRequest((RemoveContentRequest) untypedRequest);

    } else {
      System.err.println("Command not recognized");
      throw new RuntimeException();
    }
  }

  private void notifyCounterChange() throws IOException {
    // TODO: Source peer not needed here
    UpdateCounterRequest request = new UpdateCounterRequest(me.getAddress(), me.getPort(), globalContentCounter);
    for (Peer peer : peers) {
      forwardRequest(peer, request);
    }
  }

  private void notifyContentMappingChange(Peer owner, Integer key, boolean add) throws IOException {
    UpdateContentMappingRequest request =
      new UpdateContentMappingRequest(owner.getAddress(), owner.getPort(), key, add);
    // Forward to each peer
    for (Peer peer : peers) {
      forwardRequest(peer, request);
    }
  }
}
