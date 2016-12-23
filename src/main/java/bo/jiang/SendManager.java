package bo.jiang;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author shumpert.jiang
 */
public class SendManager implements Runnable{
  private Logger logger = LogManager.getLogger(SendManager.class);
  private final ExecutorService executorService = Executors.newCachedThreadPool();
  private final Set<String> serviceIdMap = new HashSet<String>();
  private ServiceContext serviceContext = ServiceContext.getInstance();
  public void run() {
    logger.info("线程开始");
    while (true) {
      logger.debug("循环取服务");
      Map<String, Service> map = serviceContext.getMap();
      Set<Map.Entry<String, Service>> set = map.entrySet();
      for (Map.Entry<String, Service> entry : set) {

        String serviceId = entry.getKey();
        if (!serviceIdMap.contains(serviceId)) {
          executorService.execute(new SendWorker(serviceId));
          serviceIdMap.add(serviceId);
        }
      }

      try {
        TimeUnit.MILLISECONDS.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
}
