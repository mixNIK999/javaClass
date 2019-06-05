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

/**
 * Вспомогательные функции и классы для обработки серверных запросов.
 */
public class ServerTasks {

    /**
     * Функция для обработки запроса list — листинг файлов в директории на сервере.
     *
     * @param input строка в формате <1: Int> <path: String>
     * path — путь к директории
     *
     * @return строка ответа в формате <size: Int> (<name: String> <is_dir: Boolean>)*
     * size — количество файлов и папок в директории
     * name — название файла или папки
     * is_dir — флаг, принимающий значение True для директорий
     * Если директории не существует, сервер посылает ответ с size = -1
     */
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

    /**
     * Функция для обработки запроса get — скачивание файла с сервера.
     *
     * @param input строка в формате: <2: Int> <path: String>
     *     path — путь к файлу
     * @return строка в формате <size: Long> <content: Bytes>
     * size — размер файла
     * content — его содержимое
     * Если файла не существует, сервер посылает ответ с size = -1
     */
    public static String getFile(String input) {
        var in = new Scanner(input);
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

    static class TaskData {
        private String taskData;
        private SocketChannel channel;

        String getTaskData() {
            return taskData;
        }

        SocketChannel getChannel() {
            return channel;
        }

        TaskData(String taskData, SocketChannel channel) {
            this.taskData = taskData;
            this.channel = channel;
        }
    }

    static class ListTask implements Runnable {
        private final TaskData input;
        private ConcurrentLinkedQueue<TaskData> resultQueue;

        ListTask(TaskData input, ConcurrentLinkedQueue<TaskData> resultQueue) {
            this.input = input;
            this.resultQueue = resultQueue;
        }

        @Override
        public void run() {
            var task = new TaskData(getList(input.taskData), input.channel);
            resultQueue.add(task);
        }

    }

    static class GetTask implements Runnable {
        private final TaskData input;
        private ConcurrentLinkedQueue<TaskData> resultQueue;

        GetTask(TaskData input, ConcurrentLinkedQueue<TaskData> resultQueue) {
            this.input = input;
            this.resultQueue = resultQueue;
        }

        @Override
        public void run() {
            resultQueue.offer(new TaskData(getFile(input.taskData), input.channel));
        }

    }
}
