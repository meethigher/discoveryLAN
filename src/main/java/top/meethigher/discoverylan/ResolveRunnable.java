package top.meethigher.discoverylan;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
@Slf4j
@RequiredArgsConstructor
public class ResolveRunnable implements Runnable {

    private final List<String> resolveIpList;

    private final List<String> localIpList;

    private final List<String> container;

    private final CountDownLatch countDownLatch;

    private final boolean scanPort;

    private final boolean showHostname;

    private final String template;


    @Override
    public void run() {
        for (String ip : resolveIpList) {
            String hostname = null;
            List<String> connectedPort = null;
            try {
                InetAddress inetAddress = InetAddress.getByName(ip);
                if (inetAddress.isReachable(2000)) {
                    if (showHostname) {
                        if (localIpList.contains(ip)) {
                            hostname = "本机";
                        } else {
                            //该操作通过ping -a ip 一样可以实现
                            String name = inetAddress.getHostName();
                            hostname = name.isEmpty() ? inetAddress.getCanonicalHostName() : name;
                        }
                    }
                    if (scanPort) {
                        connectedPort = new LinkedList<>();
                        for (int port : DiscoveryLAN.ports) {
                            try {
                                Socket socket = new Socket();
                                socket.connect(new InetSocketAddress(ip, port), 2000);
                                socket.close();
                                connectedPort.add(String.valueOf(port));
                            } catch (Exception ignore) {
                            }
                        }
                    }
                    String result;
                    if (showHostname && scanPort) {
                        result = String.format(template, ip, hostname, String.join(",", connectedPort));
                    } else if (showHostname) {
                        result = String.format(template, ip, hostname, null);
                    } else if (scanPort) {
                        result = String.format(template, ip, null, String.join(",", connectedPort));
                    } else {
                        result = String.format(template, ip, null, null);
                    }
                    log.info("{}", result);
                    container.add(result);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        countDownLatch.countDown();
    }
}
