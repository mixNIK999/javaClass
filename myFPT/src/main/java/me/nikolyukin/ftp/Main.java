package me.nikolyukin.ftp;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        var server = new ServerFTP();
        server.runServer(9999);
    }

}
