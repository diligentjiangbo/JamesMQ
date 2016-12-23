package bo.jiang;

import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author shumpert.jiang
 */
public class ServiceContext {
  private final Map<String, Service> map = new ConcurrentHashMap<String, Service>();
  private static ServiceContext serviceContext;

  private ServiceContext() {}

  public static ServiceContext getInstance() {
    if (serviceContext == null) {
      synchronized (ServiceContext.class) {
        if (serviceContext == null) {
          serviceContext = new ServiceContext();
        }
      }
    }
    return serviceContext;
  }

  public void put(String str, Service service) {
    map.put(str, service);
  }

  public Service get(String str) {
    return map.get(str);
  }

  public void remove(String str) {
    map.remove(str);
  }

  public Map<String, Service> getMap() {
    return map;
  }
}
