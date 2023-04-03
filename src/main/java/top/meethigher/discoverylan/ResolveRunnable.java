package top.meethigher.discoverylan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author chenchuancheng github.com/meethigher
 * @since 2023/4/3 23:16
 */
public class ResolveRunnable implements Runnable {

    private final Logger log = LoggerFactory.getLogger(ResolveRunnable.class);


    private final List<String> ipList;

    private final LinkedBlockingQueue<String> queue;

    private final CountDownLatch countDownLatch;

    public ResolveRunnable(List<String> ipList, LinkedBlockingQueue<String> queue, CountDownLatch countDownLatch) {
        this.ipList = ipList;
        this.queue = queue;
        this.countDownLatch = countDownLatch;
    }


    @Override
    public void run() {
        for (String ip : ipList) {
            try {
                InetAddress inetAddress = InetAddress.getByName(ip);
                long startTime = System.currentTimeMillis();
                if (inetAddress.isReachable(2000)) {
                    long endTime = System.currentTimeMillis();
                    String format = String.format("%s 耗时 %s ms", ip, endTime - startTime);
                    queue.add(format);
                    log.info("{} is reachable", format);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        countDownLatch.countDown();
    }
}