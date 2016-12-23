package bo.jiang;

import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;

/**
 * @author shumpert.jiang
 */
public class AsynContext {
  private final Map<String, Channel> map = new ConcurrentHashMap<String, Channel>();
  private static AsynContext context;

  private AsynContext() {}

  public static AsynContext getInstance() {
    if (context == null) {
      synchronized (AsynContext.class) {
        if (context == null) {
          context = new AsynContext();
        }
      }
    }
    return context;
  }

  public void put(String str, Channel channel) {
    map.put(str, channel);
  }

  public Channel get(String str) {
    return map.get(str);
  }

  public void remove(String str) {
    map.remove(str);
  }
}
