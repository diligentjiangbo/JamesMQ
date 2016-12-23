package bo.jiang;

import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @author shumpert.jiang
 */
public class Service {
  private final BlockingQueue<Message> queue;
  private final List<Channel> channelList;

  public Service() {
    queue = new ArrayBlockingQueue<Message>(3000);
    channelList = new ArrayList<Channel>();
  }

  public BlockingQueue<Message> getQueue() {
    return queue;
  }

  public List<Channel> getChannelList() {
    return channelList;
  }
}
