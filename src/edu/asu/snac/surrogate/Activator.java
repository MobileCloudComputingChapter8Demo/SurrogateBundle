package edu.asu.snac.surrogate;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

@SuppressWarnings("rawtypes")
public class Activator implements BundleActivator {
  Thread jarFileReceiver = null;
  Thread serviceCaller = null;

  // for debug
  Map<Bundle, Set<ServiceReference>> metadata = new HashMap<Bundle, Set<ServiceReference>>();

  /*
   * (non-Javadoc)
   * 
   * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
   */
  public void start(BundleContext context) throws Exception {
    System.out.println("surrogate starting ..");

    context.addServiceListener(new ServiceListener() {
      @Override
      public void serviceChanged(ServiceEvent arg0) {
        switch (arg0.getType()) {
          case ServiceEvent.REGISTERED:
            ServiceReference ref = arg0.getServiceReference();
            Bundle b = ref.getBundle();
            if (metadata.containsKey(b)) {
              if (!metadata.get(b).contains(ref)) {
                metadata.get(b).add(ref);
              }
            }
            break;
          default:
            System.out.println("other ServiceEvent type " + arg0.getType());
        }
      }
    });

    serviceCaller = new Thread(new ServiceCaller(context, metadata));
    serviceCaller.start();
    jarFileReceiver = new Thread(new JarFileReceiver(context, metadata));
    jarFileReceiver.start();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
   */
  public void stop(BundleContext context) throws Exception {
    System.out.println("surrogate stopping ..");
    jarFileReceiver.interrupt();
    serviceCaller.interrupt();
  }

}
