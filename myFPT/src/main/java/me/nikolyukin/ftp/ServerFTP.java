package me.nikolyukin.ftp;

import static java.lang.Math.min;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import me.nikolyukin.ftp.ServerTasks.GetTask;
import me.nikolyukin.ftp.ServerTasks.ListTask;
import me.nikolyukin.ftp.ServerTasks.TaskData;

public class ServerFTP {

    private ExecutorService serverTread;
    private AtomicBoolean itsTimeToStop = new AtomicBoolean(true);

    public void start(int port) throws IOException {
        if (itsTimeToStop.get()) {
            serverTread = Executors.newSingleThreadExecutor();
            itsTimeToStop.set(false);
            serverTread.submit(new IOServerTask(port, itsTimeToStop));
        }
    }

    public void stop() {
        if (!itsTimeToStop.getAndSet(true)) {
            serverTread.shutdown();
        }
    }

    private static class IOServerTask implements Runnable {

    private ExecutorService pool;

    private final int port;
    private final int bufferSize = 1024;
    private final int nThreads = 6;

    AtomicBoolean itsTimeToStop;
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
    private ConcurrentLinkedQueue<TaskData> resultQueue = new ConcurrentLinkedQueue<>();

    private IOServerTask(int port, AtomicBoolean itsTimeToStop) throws IOException {
        this.port = port;
        this.itsTimeToStop = itsTimeToStop;

        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress(port));

        selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        pool = Executors.newFixedThreadPool(nThreads);
    }

    @Override
        public void run() {
            while (!itsTimeToStop.get()) {
                if (selector.isOpen()) {
                    int res = 0;
                    try {
                        res = selector.selectNow();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (res > 0) {
                        Set<SelectionKey> selectedKeys = selector.selectedKeys();
                        Iterator<SelectionKey> iter = selectedKeys.iterator();
                        while (iter.hasNext()) {
                            var key = iter.next();
                            try {
                                if (key.isAcceptable()) {
                                    doAccept((ServerSocketChannel) key.channel(), selector);
                                }

                                if (key.isReadable()) {
                                    doRead((SocketChannel) key.channel());
                                }

                                if (key.isWritable()) {
                                    doWrite((SocketChannel) key.channel(),
                                        (String) key.attachment());
                                    key.cancel();
                                    key.channel().close();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            iter.remove();
                        }
                        selectedKeys.clear();
                    }
//                    System.out.println("wow");
                    TaskData taskData = resultQueue.poll();
                    while(taskData != null) {
//                        TaskData taskData = resultQueue.poll();
                        try {
                            taskData.getChannel().register(selector, SelectionKey.OP_WRITE).attach(taskData.getTaskData());
                        } catch (ClosedChannelException e) {
                            e.printStackTrace();
                        }
                        taskData = resultQueue.poll();
                    }
                }
            }
            pool.shutdown();
        }

        private void doWrite(SocketChannel channel, String answer) throws IOException {

//            System.out.println("doWrite");
            byte[] answerBytes = answer.getBytes(UTF_8);
            for (int i = 0; i < answerBytes.length; i+= bufferSize) {
                buffer.clear();
                buffer.put(answerBytes, i, min(answerBytes.length - i, bufferSize));
                buffer.flip();
                channel.write(buffer);
//                System.out.println("doneWrite");
            }
//            System.out.println("doneWrite");
        }

        private void doAccept(ServerSocketChannel serverChannel, Selector selector)
            throws IOException {

//            System.out.println("doAccept");
            SocketChannel clientChannel = serverChannel.accept();
            clientChannel.configureBlocking(false);
            clientChannel.register(selector, SelectionKey.OP_READ);
        }

        private void doRead(SocketChannel channel) throws IOException {

//            System.out.println("doRead");
            buffer.clear();
            channel.read(buffer);
            buffer.flip();

            var bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            String input = new String(bytes, UTF_8);

            TaskData data = new TaskData(input, channel);

            if (input.startsWith("1")) {
//                System.out.println("submit");
                pool.submit(new ListTask(data, resultQueue));
            }

            if (input.startsWith("2")) {
                pool.submit(new GetTask(data, resultQueue));
            }
        }


    }
}
