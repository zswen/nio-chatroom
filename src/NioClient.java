import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Scanner;

public class NioClient {
    private void start() throws IOException {
        /**
         * 连接服务器端
         */
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 8000));
        System.out.println("客户端启动");

        /**
         * 新开一个线程，专门负责来接受服务器端的响应数据
         */
        Selector selector = Selector.open();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);
        new Thread(new NioClientHandler(selector)).start();

        /**
         * 向服务器端发送数据
         */
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String request = scanner.nextLine();
            if (request != null && request.length() > 0) {
                socketChannel.write(Charset.forName("UTF-8").encode(request)); // 我们还可以改造，然后加入 username
            }
        }
        /**
         *接受服务器响应
         */
    }

    public static void main(String[] args) throws IOException {
        new NioClient().start();
    }
}
