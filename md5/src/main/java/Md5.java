import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

class Md5 {

    private static MessageDigest hashFile(File file) throws IOException, NoSuchAlgorithmException {

        MessageDigest md5 = MessageDigest.getInstance("MD5");

        try (var digestInput = new DigestInputStream(new FileInputStream(file), md5)) {
            while (digestInput.read() != -1) {}
            return md5;
        }
    }

    public static MessageDigest hashAllInOneThread(File root)
        throws IOException, NoSuchAlgorithmException {

        if (root.isFile()) {
            return hashFile(root);
        }

        MessageDigest md5 = MessageDigest.getInstance("MD5");


        md5.update(root.getName().getBytes(StandardCharsets.UTF_8));

        for (var file : Objects.requireNonNull(root.listFiles())) {
            md5.update(hashAllInOneThread(file).digest());
        }
        return md5;
    }

    public static MessageDigest hashAllInForkJoin(File root) {
        return new ForkJoinPool().invoke(new HashTask(root));
    }

    private static class HashTask extends RecursiveTask<MessageDigest> {
        private final File root;

        private HashTask(File root) {
            this.root = root;
        }

        @Override
        protected MessageDigest compute() {
            if (root.isFile()) {
                try {
                    return hashFile(root);
                } catch (IOException | NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            }

            MessageDigest md5;
            try {
                md5 = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("cant do MessageDigest.getInstance(\"MD5\")");
            }

            md5.update(root.getName().getBytes(StandardCharsets.UTF_8));
            List<HashTask> subTasks = new ArrayList<>();
            for (var file : Objects.requireNonNull(root.listFiles())) {
                var task = new HashTask(file);
                subTasks.add(task);
            }

            for (int i = 0; i < subTasks.size(); i++) {
                if (i < subTasks.size() - 1) {
                    subTasks.get(i).fork();
                } else {
                    md5.update(subTasks.get(i).compute().digest());
                }
            }

            for (int i = 0; i < subTasks.size() - 1; i++) {
                md5.update(subTasks.get(i).join().digest());
            }

            return md5;
        }
    }
}
