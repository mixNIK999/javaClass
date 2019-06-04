import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class ServerFTP {

//    private ExecutorService serverPull;
//    private task
    public static void runServer(int port) throws IOException {
        new ServerTask(port).run();
    }

    private static class ServerTask implements Runnable {

    private ExecutorService pool;

    private final int port;
    private final int bufferSize = 1024;
    private final int nThreads = 6;

    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
    private BlockingQueue<TaskData> resultQueue = new LinkedBlockingQueue<>();

    public ServerTask(int port) throws IOException {
        this.port = port;
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(port));
        serverSocketChannel.configureBlocking(false);

        selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        pool = Executors.newFixedThreadPool(nThreads);
    }

    @Override
        public void run() {
            while (true) {
                if (selector.isOpen()) {
                    Set<SelectionKey> selectedKeys = selector.selectedKeys();
                    for (var key : selectedKeys) {
                        try {
                            if (key.isAcceptable()) {
                                doAccept((ServerSocketChannel) key.channel(), selector);
                            }

                            if (key.isReadable()) {
                                doRead((SocketChannel) key.channel());
                            }

                            if (key.isWritable()) {
                                doWrite((SocketChannel) key.channel(), (String) key.attachment());
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    selectedKeys.clear();
                }
            }
        }

        private void doWrite(SocketChannel channel, String answer) throws IOException {
            ByteBuffer writeBuff = ByteBuffer.allocate(bufferSize);
            writeBuff.clear();
            writeBuff.put(answer.getBytes(UTF_8));
            channel.write(writeBuff);
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
            channel.read(buffer);
            buffer.flip();
            String input = new String(buffer.array(), UTF_8);

            TaskData data = new TaskData(input, channel);

            if (input.startsWith("1")) {
                pool.submit(new ListTask(data, resultQueue));
            }

            if (input.startsWith("2")) {
                pool.submit(new GetTask(data, resultQueue));
            }
        }

        private void answerGetAndWriteInChannel(String input, SocketChannel channel) {
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

        private void writeFileAnswer(SocketChannel channel, byte[] data) throws IOException {
            ByteBuffer writeBuff = ByteBuffer.allocate(bufferSize);
            writeBuff.clear();
            writeBuff.putInt(data.length);
            writeBuff.put(data);
            channel.write(writeBuff);
        }

        private void writeListAnswer(SocketChannel channel, String answer) throws IOException {
            ByteBuffer writeBuff = ByteBuffer.allocate(bufferSize);
            writeBuff.clear();
            writeBuff.put(answer.getBytes(UTF_8));
            channel.write(writeBuff);
        }

        private static class TaskData {
            private String taskData;
            private SocketChannel channel;

            private TaskData(String taskData, SocketChannel channel) {
                this.taskData = taskData;
                this.channel = channel;
            }
        }

        private static class ListTask implements Runnable {
            private final TaskData input;
            private BlockingQueue<TaskData> resultQueue;

            private ListTask(TaskData input, BlockingQueue<TaskData> resultQueue) {
                this.input = input;
                this.resultQueue = resultQueue;
            }

            @Override
            public void run() {
                resultQueue.add(new TaskData(getList(input.taskData), input.channel));
            }

            private static String getList(String input) {
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
        }

        private static class GetTask implements Runnable {
            private final TaskData input;
            private BlockingQueue<TaskData> resultQueue;

            private GetTask(TaskData input, BlockingQueue<TaskData> resultQueue) {
                this.input = input;
                this.resultQueue = resultQueue;
            }

            @Override
            public void run() {
                resultQueue.add(new TaskData(new String(getFile(input.taskData), UTF_8), input.channel));
            }

            private static byte[] getFile(String fileRequest) {
                var in = new Scanner(fileRequest);
                var errString = "-1".getBytes(UTF_8);
                if (in.nextInt() != 2) {
                    return errString;
                }

                String path = in.next();
                try {
                    return Files.readAllBytes(Paths.get(path)) ;
                } catch (IOException e) {
                    return errString;
                }
            }
        }
    }
}
