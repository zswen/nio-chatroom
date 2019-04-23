import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Set;
import java.util.Iterator;

public class NioServer {


    public void start() throws IOException {
        /**
         * 1。 创建Selector
         *
         */
        Selector selector = Selector.open();

        /**
         * 2. 通过ServerSocketChannel 创建channel连接
         */
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

        /**
         * 3. 为channel通道绑定监听端口
         */
        serverSocketChannel.bind(new InetSocketAddress(8000)); //channel处理所有客户端接入

        /**
         * 4. 设置 channel 为非阻塞模式
         */
        serverSocketChannel.configureBlocking(false);

        /**
         * 5. 将channel注册到selector上，监听连接事件
         */
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("服务器启动");

        /**
         * 6. 循环等待接入的连接
         */
        while(true) {
            // 阻塞方法，只有当注册到selector上的serverSocketChannel监听的事件就绪了才会返回，返回值是一共有多少个channel就绪了
            int readyChannels = selector.select();

            if (readyChannels == 0) continue;


            Set<SelectionKey> selectionKeys = selector.selectedKeys(); // 获取所有可用的channel的集合

            Iterator iterator = selectionKeys.iterator();

            while (iterator.hasNext()) {

                SelectionKey selectionKey = (SelectionKey) iterator.next(); // 拿到ready的selectionKey

                iterator.remove();

                /**
                 * 7. 根据就绪状态，分辨调用对应方法处理业务逻辑
                 */

                /**
                 * 如果是 接入事件
                 */
                if (selectionKey.isAcceptable()) {
                    acceptHandler(serverSocketChannel, selector);
                }
                /**
                 * 如果是可读事件
                 */
                if (selectionKey.isReadable()) {
                    readHandler(selectionKey, selector);
                }
            }

        }


    }

    /**
     * 接入事件处理器
     */
    private void acceptHandler(ServerSocketChannel serverSocketChannel, Selector selector) throws IOException {
        /**
         * 创建socketChannel，与服务器端建立连接
         */

        SocketChannel socketChannel = serverSocketChannel.accept();
        /**
         * 将socketChannel 设置为非阻塞模式
         */
        socketChannel.configureBlocking(false);

        /**
         * 把新建的socketChannel注册到selector上，来监听新产生的这个channel的可读事件
         */
        socketChannel.register(selector, SelectionKey.OP_READ);

        /**
         * 给客户端一个回应，说明连接已经建立完了
         */
        socketChannel.write(Charset.forName("UTF-8").encode("你与聊天室其他人都不是朋友关系"));
    }

    /**
     * 可读事件处理器
     */
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
         * 循环读取客户端请求信息
         */
        String request = "";
        while (socketChannel.read(byteBuffer) > 0) {
            /**
             * 切换buffer为读模式
             */
            byteBuffer.flip();

            /**
             * 读取buffer中的内容
             */
            request += Charset.forName("UTF-8").decode(byteBuffer);
        }
        /**
         * 将 channel 再次注册到selector上，监听他的可读事件
         */
        socketChannel.register(selector, SelectionKey.OP_READ);

        /**
         * 将客户端发送的请求信息 广播给其他客户端
         */
        if (request.length() > 0) {

            // 广播给其他客户端
            broadCast(selector, socketChannel, request);
        }
    }


    private void broadCast(Selector selector, SocketChannel sourceChannel, String request) {
        /**
         * 获取所有已介入的客户端channel
         */
        //Set<SelectionKey> selectionKeySet = selector.keys();


        /**
         * 循环向所有channel广播信息
         */
        for (SelectionKey selectionkey : selector.keys()) {
            Channel targetChannel = selectionkey.channel();

            //剔除发消息的客户端
            if (targetChannel instanceof SocketChannel
                    && targetChannel != sourceChannel) {
                try {
                    ((SocketChannel) targetChannel).write(Charset.forName("UTF-8").encode(request));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public static void main(String[] args) throws IOException {
        NioServer nioServer = new NioServer();
        nioServer.start();
    }

}
