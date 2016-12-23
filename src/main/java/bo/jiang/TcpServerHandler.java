package bo.jiang;

import com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.SynchronousQueue;

/**
 * @author shumpert.jiang
 */
public class TcpServerHandler extends ChannelInboundHandlerAdapter {
  private static Logger logger = LogManager.getLogger(TcpServerHandler.class);
  private static Gson gson = new Gson();

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    logger.info("消息来了");
    ByteBuf byteBuf = (ByteBuf)msg;
    //获取协议字段
    MsgType msgType = MsgType.getByIndex(byteBuf.readByte());
    //解析请求
    byte[] bytes = new byte[byteBuf.readableBytes()];
    byteBuf.readBytes(bytes);
    String str = new String(bytes);
    Message message = gson.fromJson(str, Message.class);;
    //获取服务上下文
    ServiceContext serviceContext = ServiceContext.getInstance();
    SyncContext syncContext = SyncContext.getInstance();
    AsynContext asynContext = AsynContext.getInstance();
    String serviceId = message.getHeader().getServiceId();
    String uuid = message.getHeader().getUuid();
    Service service = serviceContext.get(serviceId);
    if (service == null) {
      service = new Service();
      serviceContext.put(serviceId, service);
    }
    switch (msgType) {
      case SERVICE:
        service.getChannelList().add(ctx.channel());
        sendReply(ctx.channel(), message, ReturnCode.SUCCESS);
        logger.info("服务注册成功{}", serviceId);
        break;
      case REQUEST:
        service.getQueue().put(message);
        asynContext.put(uuid, ctx.channel());
        logger.info("收到服务请求{}，消息id为{}", serviceId, uuid);
//        SynchronousQueue<Message> syncQueue = new SynchronousQueue<Message>();
//        syncContext.put(uuid, syncQueue);
//        Message returnMsg = syncQueue.take();
//        sendReply(ctx.channel(), returnMsg, null);
        break;
      case RESPONSE:
//        syncContext.get(uuid).offer(message);
        Channel returnChannel = asynContext.get(uuid);
        if (returnChannel != null) {
          sendReply(returnChannel, message, ReturnCode.SUCCESS);
          logger.info("服务回包{}，消息id为{}", serviceId, uuid);
        }
        break;
    }
    ReferenceCountUtil.release(msg);

  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    logger.error("异常", cause);
  }

  public void sendReply(Channel channel, Message message, ReturnCode returnCode) {
    //如果返回码没有设置就设置
    if (returnCode != null) {
      message.getHeader().setReturnCode(returnCode);
    }
    String str = gson.toJson(message);
    ByteBuf byteBuf = Unpooled.buffer();
    byteBuf.writeByte(MsgType.RESPONSE.getIndex());
    byteBuf.writeBytes(str.getBytes());
    channel.writeAndFlush(byteBuf);
    logger.info("返回消息{}", message.getHeader().getUuid());
  }
}
