package bo.jiang;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author shumpert.jiang
 */
public class JamesServer {
  private static Logger logger = LogManager.getLogger(JamesServer.class);

  public static void main(String[] args) {
    new Thread(new SendManager()).start();
    startNetty();
  }

  private static void startNetty() {
    EventLoopGroup work = new NioEventLoopGroup();
    EventLoopGroup boss = new NioEventLoopGroup();
    ServerBootstrap bootstrap = new ServerBootstrap();
    try {
      bootstrap.group(boss, work)
          .channel(NioServerSocketChannel.class)
          .childHandler(new ChannelInitializer<SocketChannel>() {
            protected void initChannel(SocketChannel ch) throws Exception {
              ch.pipeline()
                  .addLast(new TcpDecoder(65535))
                  .addLast(new TcpEncoder())
                  .addLast(new TcpServerHandler());
            }
          });
      Channel channel = bootstrap.bind(6789).sync().channel();
      channel.closeFuture().sync();
    } catch (InterruptedException e) {
      logger.info("中断", e);
    }
  }
}
