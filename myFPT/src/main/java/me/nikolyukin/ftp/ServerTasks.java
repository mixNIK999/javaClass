package me.nikolyukin.ftp;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ServerTasks {

    public static String getList(String input) {
        var in = new Scanner(input);
        var errString = "-1";

        if (!in.hasNextInt() || in.nextInt() != 1 || !in.hasNext()) {
            return errString;
        }

        File dir = new File(in.next());
        if (dir.exists() && dir.isDirectory()) {
            var list = Objects.requireNonNull(dir.listFiles());
            var answer = new StringBuilder(list.length + " ");
            for (var file : list) {
                answer.append(file.getName()).append(" ").append((file.isDirectory()) ? "1 " : "0 ");
            }
            return answer.toString();
        }
        return errString;
    }

    public static String getFile(String fileRequest) {
        var in = new Scanner(fileRequest);
        var errString = "-1";

        if (in.nextInt() != 2) {
            return errString;
        }

        try {
            String path = in.next();
            var bytes = Files.readAllBytes(Paths.get(path));
            return bytes.length + " " + new String(bytes, UTF_8) ;
        } catch (IOException e) {
            return errString;
        }
    }

    public static class TaskData {
        private String taskData;
        private SocketChannel channel;

        public String getTaskData() {
            return taskData;
        }

        public SocketChannel getChannel() {
            return channel;
        }

        public TaskData(String taskData, SocketChannel channel) {
            this.taskData = taskData;
            this.channel = channel;
        }
    }

    public static class ListTask implements Runnable {
        private final TaskData input;
        private ConcurrentLinkedQueue<TaskData> resultQueue;

        public ListTask(TaskData input, ConcurrentLinkedQueue<TaskData> resultQueue) {
            this.input = input;
            this.resultQueue = resultQueue;
        }

        @Override
        public void run() {
            resultQueue.add(new TaskData(getList(input.taskData), input.channel));
        }

    }

    public static class GetTask implements Runnable {
        private final TaskData input;
        private ConcurrentLinkedQueue<TaskData> resultQueue;

        public GetTask(TaskData input, ConcurrentLinkedQueue<TaskData> resultQueue) {
            this.input = input;
            this.resultQueue = resultQueue;
        }

        @Override
        public void run() {
            resultQueue.add(new TaskData(getFile(input.taskData), input.channel));
        }

    }
}
