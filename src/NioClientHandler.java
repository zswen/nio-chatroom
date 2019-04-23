import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

/**
 * 客户端线程类，专门接受服务器端响应信息
 */

public class NioClientHandler implements Runnable{

    private Selector selector;

    public NioClientHandler(Selector selector) {
        this.selector = selector;
    }

    @Override
    public void run() {
        try {
            while(true) {

                int readyChannels = selector.select();

                if (readyChannels == 0) continue;

                Set<SelectionKey> selectionKeys = selector.selectedKeys(); // 获取所有可用的channel的集合

                Iterator iterator = selectionKeys.iterator();

                while (iterator.hasNext()) {

                    SelectionKey selectionKey = (SelectionKey) iterator.next(); // 拿到ready的selectionKey

                    iterator.remove();


                    /**
                     * 如果是可读事件
                     */
                    if (selectionKey.isReadable()) {
                        readHandler(selectionKey, selector);
                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readHandler(SelectionKey selectionKey, Selector selector) throws IOException {
        /**
         * 从 selectionKey 里面获取已经就绪的channel
         */
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        /**
         * 创建 buffer
         */
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        /**
         * 循环读取服务器端请求信息
         */
        String  response = "";
        while (socketChannel.read(byteBuffer) > 0) {

            byteBuffer.flip();


            response += Charset.forName("UTF-8").decode(byteBuffer);
        }

        socketChannel.register(selector, SelectionKey.OP_READ);

        if (response.length() > 0) {
            // 服务器端响应信息打印到本地
            System.out.println(response);
        }
    }

}
