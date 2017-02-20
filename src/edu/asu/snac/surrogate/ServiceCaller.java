package edu.asu.snac.surrogate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Set;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

@SuppressWarnings("rawtypes")
public class ServiceCaller implements Runnable {
  public static final int port = 6702;

  private BundleContext context;
  @SuppressWarnings("unused")
  private Map<Bundle, Set<ServiceReference>> metadata; // for debug

  public ServiceCaller(BundleContext context, Map<Bundle, Set<ServiceReference>> metadata) {
    super();
    this.context = context;
    this.metadata = metadata;
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

      try {
        // process the connection
        if (socket != null) {
          processRemoteCall(socket);
        }
        // close the connection
        socket.close();
      } catch (IOException | NoSuchMethodException | SecurityException | IllegalAccessException
          | IllegalArgumentException | InvocationTargetException e) {
        e.printStackTrace();
      }
    } while (true);
  }

  private String invokeLocalService(String input) throws NoSuchMethodException, SecurityException,
      IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    // 1 get object
    Object service = context.getService(context.getServiceReference(Util.getServiceName(input)));
    // 2 get method
    Method method = service.getClass().getMethod(Util.getMethodName(input),
        Util.getInvokationParamTypes(input));
    // 3 invoke
    Object ret = method.invoke(service, Util.getInvokationParams(input));
    // 4 return
    return ret.toString();
  }

  private void processRemoteCall(Socket fromClientSocket)
      throws IOException, NoSuchMethodException, SecurityException, IllegalAccessException,
      IllegalArgumentException, InvocationTargetException {
    // get stream
    PrintWriter pw = new PrintWriter(fromClientSocket.getOutputStream(), true);
    BufferedReader br =
        new BufferedReader(new InputStreamReader(fromClientSocket.getInputStream()));

    // process string
    String rev = br.readLine();
    if (rev != null) {
      System.out.println("receive: " + rev);
      String snd = invokeLocalService(rev);
      System.out.println("reply: " + snd);
      pw.println(snd);
    }

    // close
    pw.close();
    br.close();
  }

}
