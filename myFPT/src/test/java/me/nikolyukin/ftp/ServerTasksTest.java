package me.nikolyukin.ftp;

import static me.nikolyukin.ftp.ServerTasks.getFile;
import static me.nikolyukin.ftp.ServerTasks.getList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.hamcrest.Matchers.*;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ServerTasksTest {


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

    @Test
    void getListTestNumber() throws IOException {
        String res = getList("1 " + root.getCanonicalPath());
        assertTrue(res.startsWith("4"));
    }

    @Test
    void getListFullTest() throws IOException {
        String res = getList("1 " + root.getCanonicalPath());
        var in = new Scanner(res);
        assertEquals(4, in.nextInt());
        while (in.hasNext()) {
            String fileName = in.next();
            int type = in.nextInt();
            var file = new File(root, fileName);
            assertEquals(file.isDirectory(), type != 0);
            assertTrue(files.contains(file) || dirs.contains(file));
        }
    }

    @Test
    void getListFromFile() throws IOException {
        String res = getList("1 " + files.get(0).getCanonicalPath());
        assertEquals("-1", res);
    }

    @Test
    void getFileFromFile() throws IOException {
        String res = getFile("2 " + files.get(0).getCanonicalPath());
        var in = new Scanner(res);
        assertEquals(5, in.nextInt());
        assertEquals("hello", in.next());
    }

    @Test
    void getFileFromDir() throws IOException {
        String res = getFile("2 " + dirs.get(0).getCanonicalPath());
        assertEquals("-1", res);
    }
}