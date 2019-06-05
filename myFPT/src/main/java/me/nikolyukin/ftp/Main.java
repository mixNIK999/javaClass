package me.nikolyukin.ftp;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws IOException {
        var server = new ServerFTP();
        server.start(9999);
        System.out.println("press enter to stop server");
        System.in.read();
        server.stop();
    }

}
