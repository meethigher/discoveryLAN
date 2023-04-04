package top.meethigher.discoverylan;

import lombok.extern.slf4j.Slf4j;
import top.meethigher.Application;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author chenchuancheng github.com/meethigher
 * @since 2023/4/9 21:43
 */
@Slf4j
public class DiscoveryLAN {
    private final static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm");

    private static ThreadPoolExecutor executor;

    public static void run(Application app) throws Exception {
        List<String> allIP = IPCalcFreedom.allIP(app.getIp());
        int thread = ((allIP.size() - 1) / app.getBatch() + 1);
        executor = new ThreadPoolExecutor(thread, thread, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new ThreadFactory() {
            private final AtomicInteger number = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "resolve-" + number.getAndIncrement());
            }
        });
        if (app.isTimer()) {
            new Timer("manager").scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    try {
                        exec(app.getBatch(), thread, app.getIp(), allIP);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }, 0L, app.getDelay() * 60 * 1000L);
        } else {
            exec(app.getBatch(), thread, app.getIp(), allIP);
            executor.shutdown();
        }
    }

    private static void exec(int batch, int thread, String ip, List<String> allIP) throws InterruptedException {
        CountDownLatch resolve = new CountDownLatch(thread);
        int size = allIP.size();
        log.info("自动分配 {} 个线程扫描局域网ip", thread);
        List<String> container = new LinkedList<>();
        for (int i = 0; i < thread; i++) {
            int startIndex = i * batch;
            int endIndex = Math.min(startIndex + batch, size);
            List<String> subList = allIP.subList(startIndex, endIndex);
            executor.execute(new ResolveRunnable(subList, container, resolve));
        }

        resolve.await();
        log.info("{} 个线程扫描完毕", thread);
        LocalDateTime now = LocalDateTime.now();
        String fileName = String.format("%s_%s.txt", dtf.format(now), container.size());
        File file = new File(fileName);
        if (file.exists()) {
            file.delete();
        }

        String[] array = container.toArray(new String[0]);
        IPQuickSort.quickSort(array, 0, array.length - 1);

        try (FileChannel channel = new RandomAccessFile(fileName, "rw").getChannel()) {
            channel.write(StandardCharsets.UTF_8.encode(ip + " 所在局域网内已用IP " + array.length + " 个" + System.getProperty("line.separator")));
            for (String s : array) {
                channel.write(StandardCharsets.UTF_8.encode(s + System.getProperty("line.separator")));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("归档扫描结果=> {}", fileName);
        log.info("============================================================");
    }
}
