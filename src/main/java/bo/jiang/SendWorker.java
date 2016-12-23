package bo.jiang;

import com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author shumpert.jiang
 */
public class SendWorker implements Runnable {
  private Logger logger = LogManager.getLogger(SendWorker.class);
  private Gson gson = new Gson();
  private String serviceId;
  private Service service;

  public SendWorker(String serviceId) {
    this.serviceId = serviceId;
    this.service = ServiceContext.getInstance().get(serviceId);
  }

  public void run() {
    List<Channel> list = service.getChannelList();
    BlockingQueue<Message> queue = service.getQueue();
    int index = 0;
    while (true) {
      try {
        logger.info("等待请求来");
        Message message = queue.take();
        logger.info("取到请求");
        String str = gson.toJson(message);
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeByte(MsgType.REQUEST.getIndex());
        byteBuf.writeBytes(str.getBytes());

        if (list.size() != 0) {
          Channel channel = null;
          while (!(channel=(list.get(index))).isActive()) {
            list.remove(index);
            index = ++index % list.size();
          }
          channel.writeAndFlush(byteBuf);
          index = ++index % list.size();
        }

      } catch (InterruptedException e) {
        logger.error("异常", e);
      }
    }

  }

}
