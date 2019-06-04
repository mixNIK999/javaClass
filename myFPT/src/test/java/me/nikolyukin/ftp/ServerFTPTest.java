package me.nikolyukin.ftp;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class ServerFTPTest {

    @Test
    void start() throws IOException, InterruptedException {
        var server = new ServerFTP();
        server.start(1999);
        Socket clientSocket = new Socket();
        clientSocket.connect(new InetSocketAddress("localhost", 1999));

        OutputStream outputStream = clientSocket.getOutputStream();
        PrintWriter out = new PrintWriter(outputStream, true);
        out.println("1 C:\\Users\\micha\\spbau\\java\\javaClass\\myFPT\\src");
        out.flush();

        InputStream inputStream = clientSocket.getInputStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
        System.out.println(in.read());

        server.stop();
//        TimeUnit.HOURS.sleep(1);
    }
}