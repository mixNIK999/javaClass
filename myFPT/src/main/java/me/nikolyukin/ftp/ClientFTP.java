package me.nikolyukin.ftp;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ClientFTP {

    private InetSocketAddress address;
    private SocketChannel channel;
    private Selector selector;

    private static final int BUFFER_SIZE = 1024;

    public ClientFTP(InetSocketAddress address) {
        this.address = address;
    }

    public void connect() throws IOException {
        channel = SocketChannel.open();
        channel.configureBlocking(false);
        channel.socket().setTcpNoDelay(true);
        channel.connect(address);
        selector = Selector.open();
        channel.register(selector, SelectionKey.OP_CONNECT);
    }

    public void disconnect() throws IOException {
        if (channel == null) {
            return;
        }
        selector.close();
        channel.close();
        channel = null;
    }

    private String executeQuery(String query) throws IOException {
        while (selector.isOpen()) {
            if (selector.select() > 0) {
                for (var sk: selector.selectedKeys()) {

                    if (!sk.isValid()) {
                        break;
                    }

                    if (sk.isConnectable()) {
                        channel.finishConnect();
                        channel.register(selector, SelectionKey.OP_WRITE);
                        sk.interestOps(SelectionKey.OP_WRITE);
                    }

                    if (sk.isWritable()) {
                        var ch = (SocketChannel) sk.channel();

                        var buffer = ByteBuffer.allocate(BUFFER_SIZE);
                        buffer.put(query.getBytes(UTF_8));
                        buffer.flip();
                        ch.write(buffer);
                        buffer.clear();
                        var selQuery = Selector.open();
                        ch.register(selQuery, SelectionKey.OP_READ);

                        while (true) {
                            if (selQuery.select() > 0) {
                                for (var key: selQuery.selectedKeys()) {
                                    if (key.isReadable()) {
                                        var chResponse = (SocketChannel) key.channel();

                                        var byteStream = new ByteArrayOutputStream();
                                        var bytes = new byte[buffer.capacity()];
                                        while (chResponse.read(buffer) > 0) {
                                            buffer.flip();
                                            buffer.get(bytes);
                                            byteStream.write(bytes);
                                            buffer.clear();
                                        }
                                        chResponse.close();
                                        return new String(byteStream.toByteArray(), UTF_8);
                                    }
                                }
                            }
                        }
                    }
                }
                return null;
            }
        }
        return null;
    }

    public String executeGet(String path) throws IOException {
        return executeQuery("2 " + path);
    }

    public String executeList(String path) throws IOException {
        return executeQuery("1 " + path);
    }
}