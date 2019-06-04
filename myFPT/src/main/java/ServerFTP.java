import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerFTP {

    private ExecutorService serverTread = Executors.newSingleThreadExecutor();

    public void runServer(int port) throws IOException {
        serverTread.submit(new ServerTask(port));
    }

    private static class ServerTask implements Runnable {

    private ExecutorService pool;

    private final int port;
    private final int bufferSize = 1024;
    private final int nThreads = 6;

    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
    private ConcurrentLinkedQueue<TaskData> resultQueue = new ConcurrentLinkedQueue<>();

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
                                key.cancel();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    selectedKeys.clear();

                    while(!resultQueue.isEmpty()) {
                        TaskData taskData = resultQueue.poll();
                        try {
                            taskData.channel.register(selector, SelectionKey.OP_WRITE).attach(taskData.taskData);
                        } catch (ClosedChannelException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        private void doWrite(SocketChannel channel, String answer) throws IOException {
            buffer.clear();
            buffer.put(answer.getBytes(UTF_8));
            buffer.flip();
            channel.write(buffer);
        }

        private void doAccept(ServerSocketChannel serverChannel, Selector selector)
            throws IOException {

            SocketChannel clientChannel = serverChannel.accept();
            clientChannel.configureBlocking(false);
            clientChannel.register(selector, SelectionKey.OP_READ);
        }

        private void doRead(SocketChannel channel) throws IOException {

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
            private ConcurrentLinkedQueue<TaskData> resultQueue;

            private ListTask(TaskData input, ConcurrentLinkedQueue<TaskData> resultQueue) {
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
            private ConcurrentLinkedQueue<TaskData> resultQueue;

            private GetTask(TaskData input, ConcurrentLinkedQueue<TaskData> resultQueue) {
                this.input = input;
                this.resultQueue = resultQueue;
            }

            @Override
            public void run() {
                resultQueue.add(new TaskData(getFile(input.taskData), input.channel));
            }

            private static String getFile(String fileRequest) {
                var in = new Scanner(fileRequest);
                var errString = "-1";
                if (in.nextInt() != 2) {
                    return errString;
                }

                try {
                    String path = in.next();
                    var bytes = Files.readAllBytes(Paths.get(path));
                    return bytes.length + new String(bytes, UTF_8) ;
                } catch (IOException e) {
                    return errString;
                }
            }
        }
    }
}
