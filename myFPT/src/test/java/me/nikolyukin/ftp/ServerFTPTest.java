package me.nikolyukin.ftp;

import static me.nikolyukin.ftp.ServerTasks.getFile;
import static me.nikolyukin.ftp.ServerTasks.getList;
import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ServerFTPTest {


    @TempDir
    static Path sharedTempDir;

    private static File root;

    private static List<File> dirs;
    private static List<File> files;

    private static File keysDir;
    private static File configDir;
    private static File srcDir;
    private static File mainDir;

    @BeforeAll
    static void init() throws IOException {
        root = sharedTempDir.toFile();

        keysDir = new File(root, "keys");
        configDir = new File(root, "config");
        srcDir = new File(root, "src");
        mainDir = new File(srcDir, "me.nikolyukin.main");

        dirs = Arrays.asList(keysDir, configDir, srcDir, mainDir);
        dirs.forEach(dir -> assertTrue(dir.mkdir()));

        files = Arrays.asList(new File(keysDir, "Youtrack.txt"), new File(keysDir, "Google.txt"),
            new File(configDir, "local.txt"), new File(configDir, "global.txt"),
            new File(mainDir, "Main.java"), new File(root, "build.gradle"));

        files.forEach(file -> {
            try {
                assertTrue(file.createNewFile());
            } catch (IOException e) {
                assert(false);
            }
        });

        try(var in = new FileOutputStream(files.get(0))) {
            in.write("hello".getBytes());
        }
    }

    private static ServerFTP server = new ServerFTP();
    private static int port = 1999;

    @BeforeAll
    static void startServer() throws IOException {
        server.start(port);
    }

    private Scanner in;
    private PrintWriter out;
    @BeforeEach
    void inti() throws IOException {

        Socket clientSocket = new Socket("localhost", port);
        OutputStream outputStream = clientSocket.getOutputStream();
        out = new PrintWriter(outputStream, true);

        InputStream inputStream = clientSocket.getInputStream();
        in = new Scanner(new BufferedReader(new InputStreamReader(inputStream)));
    }

    @AfterAll
    static void  stopServer() {
        server.stop();
    }


    @Test
    void getListTestNumber() throws IOException {
        out.print("1 " + root.getCanonicalPath());
        out.flush();
        assertEquals(4, in.nextInt());
    }

    @Test
    void getListFullTest() throws IOException {
        out.print("1 " + root.getCanonicalPath());
        out.flush();
        int n = in.nextInt();
        assertEquals(4, n);
        for (int i = 0; i < n; i++) {
            String fileName = in.next();
            int type = in.nextInt();
            var file = new File(root, fileName);
            assertEquals(file.isDirectory(), type != 0);
            assertTrue(files.contains(file) || dirs.contains(file));
        }
    }

    @Test
    void getListFromFile() throws IOException {
        out.print("1 " + files.get(0).getCanonicalPath());
        out.flush();
        assertEquals("-1", in.next());
    }

    @Test
    void getFileFromFile() throws IOException {
        out.print("2 " + files.get(0).getCanonicalPath());
        out.flush();

        assertEquals(5, in.nextInt());
        assertEquals("hello", in.next());
    }

    @Test
    void getFileFromDir() throws IOException {
        out.print("2 " + dirs.get(0).getCanonicalPath());
        out.flush();
        assertEquals("-1", in.next());
    }
}