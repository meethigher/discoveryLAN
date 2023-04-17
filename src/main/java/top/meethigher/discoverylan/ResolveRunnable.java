package top.meethigher.discoverylan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author chenchuancheng github.com/meethigher
 * @since 2023/4/3 23:16
 */
public class ResolveRunnable implements Runnable {

    private final Logger log = LoggerFactory.getLogger(ResolveRunnable.class);


    private final List<String> ipList;

    private final List<String> container;

    private final CountDownLatch countDownLatch;

    private final boolean scanPort;

    public ResolveRunnable(List<String> ipList, List<String> container, CountDownLatch countDownLatch, boolean scanPort) {
        this.ipList = ipList;
        this.container = container;
        this.countDownLatch = countDownLatch;
        this.scanPort = scanPort;
    }


    @Override
    public void run() {
        for (String ip : ipList) {
            try {
                InetAddress inetAddress = InetAddress.getByName(ip);
                long startTime = System.currentTimeMillis();
                if (inetAddress.isReachable(1000)) {
                    long endTime = System.currentTimeMillis();
                    String format = String.format("%s_耗时 %s ms", ip, endTime - startTime);
                    log.info("{} is reachable", format);
                    if (scanPort) {
                        List<Integer> connectedPort = new LinkedList<>();
                        for (int port : DiscoveryLAN.ports) {
                            try {
                                Socket socket = new Socket();
                                socket.connect(new InetSocketAddress(ip, port), 1000);
                                socket.close();
                                connectedPort.add(port);
                            } catch (Exception ignore) {
                            }
                        }
                        format = format + " 开放的端口 " + connectedPort;
                    }
                    container.add(format);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        countDownLatch.countDown();
    }
}
