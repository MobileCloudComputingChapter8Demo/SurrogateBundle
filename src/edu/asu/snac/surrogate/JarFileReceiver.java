package edu.asu.snac.surrogate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;

@SuppressWarnings("rawtypes")
public class JarFileReceiver implements Runnable {
  public static final String JAR_FILE_STORE_DIR = "./tmp/";
  private static int fileCount = 1;
  public static final int port = 6701;

  private BundleContext context;
  private Map<Bundle, Set<ServiceReference>> metadata;


  public JarFileReceiver(BundleContext context, Map<Bundle, Set<ServiceReference>> metadata) {
    super();
    this.context = context;
    this.metadata = metadata;
    new File(JAR_FILE_STORE_DIR).mkdirs();
  }

  private void installStartBundle(String location) {
    try {
      Bundle b = context.installBundle("file:" + location);
      b.start();
      metadata.put(b, new HashSet<ServiceReference>());
    } catch (BundleException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private String storeJarFile(Socket socket) {
    // get in stream
    InputStream in = null;
    try {
      in = socket.getInputStream();
    } catch (IOException ex) {
      System.err.println("Can't get socket input stream.");
      return null;
    }

    // get out stream
    String path = null;
    OutputStream out = null;
    try {
      path = new File(JAR_FILE_STORE_DIR + fileCount + ".jar").getCanonicalPath();
      out = new FileOutputStream(path);
    } catch (Exception ex) {
      System.err.println("File not found " + path);
      return null;
    }

    // read in, write out
    byte[] bytes = new byte[16 * 1024];
    int count;
    try {
      while ((count = in.read(bytes)) > 0) {
        out.write(bytes, 0, count);
      }
    } catch (IOException e) {
      return null;
    } finally {
      try {
        out.close();
        in.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    // return file location
    return path;
  }

  @SuppressWarnings("resource")
  @Override
  public void run() {
    ServerSocket serverSocket = null;
    try {
      serverSocket = new ServerSocket(port);
    } catch (IOException ex) {
      System.err.println("Can't setup server on port number " + port);
      return;
    }

    do {
      // get one connection
      Socket socket = null;
      try {
        socket = serverSocket.accept();
      } catch (IOException ex) {
        System.err.println("Can't accept client connection. ");
      }

      // process the connection
      String location = null;
      if (socket != null) {
        location = storeJarFile(socket);
      }

      // close the connection
      try {
        socket.close();
      } catch (IOException e) {
        e.printStackTrace();
      }

      // install & start bundle
      if (location != null) {
        installStartBundle(location);
      }
    } while (true);
  }

}
