package top.meethigher;

import com.beust.jcommander.JCommander;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.meethigher.discoverylan.IPCalcFreedom;
import top.meethigher.discoverylan.ResolveRunnable;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 启动类
 *
 * @author chenchuancheng
 * @since 2023/4/3 20:46
 */
public class Application {


    private final static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm");

    private final static Logger log = LoggerFactory.getLogger(Application.class);

    private static ThreadPoolExecutor executor;

    private static LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();


    public static void main(String... args) throws Exception {
        ArgsObj argsObj = new ArgsObj();
        JCommander jCommander = JCommander.newBuilder()
                .addObject(argsObj)
                .build();
        jCommander.parse(args);
        if (argsObj.isHelp()) {
            jCommander.usage();
            return;
        }
        if (argsObj.isTimer()) {
            new Timer().scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    try {
                        start(argsObj);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }, 10 * 1000, 1000 * 60 * 60);
        } else {
            start(argsObj);
        }
    }

    private static void start(ArgsObj argsObj) throws InterruptedException {
        log.info("start");
        List<String> allIP = IPCalcFreedom.allIP(argsObj.getIp());
        int size = allIP.size();
        int thread = ((size - 1) / argsObj.getBatch() + 1);
        executor = new ThreadPoolExecutor(thread, thread, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new ThreadFactory() {

            private final AtomicInteger number = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "resolve-" + number.getAndIncrement());
            }
        });
        CountDownLatch resolve = new CountDownLatch(argsObj.getBatch());
        log.info("自动分配 {} 个线程扫描局域网ip", thread);
        for (int i = 0; i < thread; i++) {
            int startIndex = i * argsObj.getBatch();
            int endIndex = Math.min(startIndex + argsObj.getBatch(), size);
            List<String> subList = allIP.subList(startIndex, endIndex);
            executor.execute(new ResolveRunnable(subList, queue, resolve));
        }

        resolve.await();
        log.info("{} 个线程扫描完毕", thread);
        LocalDateTime now = LocalDateTime.now();
        String fileName = String.format("%s.txt", dtf.format(now));
        File file = new File(fileName);
        if (file.exists()) {
            file.delete();
        }
        try (FileChannel channel = new RandomAccessFile(fileName, "rw").getChannel()) {
            while (!queue.isEmpty()) {
                String take = queue.take() + System.getProperty("line.separator");
                channel.write(StandardCharsets.UTF_8.encode(take));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        log.info("归档扫描结果完毕");
        executor.shutdown();
        log.info("stop");
    }
}
