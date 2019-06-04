package me.nikolyukin.ftp;

import static me.nikolyukin.ftp.ServerTasks.getList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
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
    static void init() throws FileNotFoundException {
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

//        var in = new FileOutputStream(files.get(0));
//        in.
    }

    @Test
    void getListBasic() throws IOException {
        String res = getList("1 " + root.getCanonicalPath());
        System.out.println(res);
        assertTrue(res.startsWith("4"));
    }

    @Test
    void getListFromFile() throws IOException {
        String res = getList("1 " + files.get(0).getCanonicalPath());
        System.out.println(res);
        assertEquals("-1", res);
    }

    @Test
    void getFile() {
    }
}