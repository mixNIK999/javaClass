import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("need args");
            return;
        }
        File file = new File(args[0]);
        if (!file.exists()) {
            System.out.println("cant find " + args[0]);
            return;
        }

        try {
            long startOneThread = System.currentTimeMillis();

            MessageDigest oneRes = Md5.hashAllInOneThread(file);

            long finishOneThread = System.currentTimeMillis();

            System.out.println("One thread finished in " + (finishOneThread - startOneThread) + " ms and res = " + oneRes.toString());
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        long startForkJoin = System.currentTimeMillis();

        MessageDigest forkJoinRes = Md5.hashAllInForkJoin(file);

        long finishForkJoin = System.currentTimeMillis();

        System.out.println("One thread finished in " + (finishForkJoin - startForkJoin) + " ms and res = " + forkJoinRes.toString());
    }
}
