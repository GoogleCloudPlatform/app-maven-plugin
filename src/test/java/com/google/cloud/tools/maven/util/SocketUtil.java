package com.google.cloud.tools.maven.util;

import java.io.IOException;
import java.net.ServerSocket;

/** Helper methods to handle sockets. */
public class SocketUtil {

  /**
   * Returns a port that's available.
   *
   * <p><i>Note: the port may become unavailabe by the time the caller tries to use it.</i>
   */
  public static int findPort() throws IOException {
    try (ServerSocket serverSocket = new ServerSocket(0)) {
      serverSocket.setReuseAddress(true);
      return serverSocket.getLocalPort();
    }
  }
}
