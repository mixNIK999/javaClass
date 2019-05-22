import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class ServerFTP {

//    private ExecutorService pool;
//    private ExecutorService serverPull;
//    private task
    public static void runServer(int port) throws IOException {
        new ServerTask(port).run();
    }

    private static class ServerTask implements Runnable {

    private final int port;
    private final int bufferSize = 1024;
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private ByteBuffer buffer = ByteBuffer.allocate(bufferSize);

    public ServerTask(int port) throws IOException {
        this.port = port;
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(port));
        serverSocketChannel.configureBlocking(false);

        selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    @Override
        public void run() {
            try {
                while (true) {
                    if (selector.isOpen()) {
                        Set<SelectionKey> selectedKeys = selector.selectedKeys();
                        for (var key : selectedKeys) {
                            if (key.isAcceptable()) {
                                doAccept((ServerSocketChannel) key.channel(), selector);
                            }

                            if (key.isReadable()) {
                                doRead((SocketChannel) key.channel());
                            }

                        }
                        selectedKeys.clear();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void doAccept(ServerSocketChannel serverChannel, Selector selector)
            throws IOException {

            SocketChannel clientChannel = serverChannel.accept();
            clientChannel.configureBlocking(false);
            clientChannel.register(selector, SelectionKey.OP_READ);
        }

        private void doRead(SocketChannel channel) throws IOException {
            var stringBuilder = new StringBuilder();

            buffer.clear();
            int readed = 0;
            while ((readed = channel.read(buffer)) > 0) {
                buffer.flip();
                byte[] inputBytes = new byte[buffer.limit()];
                buffer.get(inputBytes);
                stringBuilder.append(new String(inputBytes));
            }
            String inputStream = stringBuilder.toString();
            if (inputStream.startsWith("1")) {
                byte[] answer = getList(inputStream).getBytes(UTF_8);

                for (int i = 0; i < answer.length; i += bufferSize) {
                    buffer.clear();
                    buffer.put(answer, i, bufferSize);
                    buffer.flip();
                    channel.write(buffer);
                }
            }

            if (inputStream.startsWith("2")) {
                answerGet(inputStream, channel);
            }
        }

        private String getList(String input) {
            var in = new Scanner(input);
            if (in.nextInt() != 1) {
                return "-1";
            }

            File dir = new File(in.next());
            if (dir.exists() && dir.isDirectory()) {
                var list = Objects.requireNonNull(dir.listFiles());
                var answer = new StringBuilder(list.length + " ");
                for (var file : list) {
                    answer.append(file.getName()).append(" ").append((file.isDirectory()) ? "1" : "0");
                }
                return answer.toString();
            }
            return "-1";
        }

        private void answerGet(String input, SocketChannel channel) {
            var in = new Scanner(input);
            buffer.clear();
            if (in.nextInt() != 2) {
                buffer.put("-1".getBytes(UTF_8));
                return;
            }

            File file = new File(in.next());
            if (file.exists() && file.isFile() && file.canRead()) {
                try (var reader = new BufferedInputStream(new FileInputStream(file))) {
                    buffer.clear();
                    buffer.put(Long.valueOf(file.length()).toString().getBytes(UTF_8));
                    buffer.flip();
                    channel.write(buffer);

                    int len;
                    byte[] bytes = new byte[bufferSize];

                    while ((len = reader.read(bytes, 0, bufferSize)) > 0) {
                        buffer.clear();
                        buffer.put(bytes, 0, len);
                        buffer.flip();
                        channel.write(buffer);
                    }
                } catch (IOException e) {
                    buffer.put("-1".getBytes(UTF_8));
                    return;
                }

            }
            buffer.put("-1".getBytes(UTF_8));
        }
    }
}
