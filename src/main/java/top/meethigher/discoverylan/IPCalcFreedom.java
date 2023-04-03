package top.meethigher.discoverylan;

import java.util.LinkedList;
import java.util.List;

/**
 * @author chenchuancheng
 * @see <a href="https://meethigher.top/blog/2023/centos-firewall/">Centos7防火墙基础</a>
 * @since 2023/4/3 20:55
 */
public class IPCalcFreedom {

    /**
     * @param ipAddress 斜线记法地址
     * @return 地址范围
     */
    private static IPRange resolveIP(String ipAddress) {
        String[] split = ipAddress.split("[./]");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < split.length - 1; i++) {
            sb.append(complement(Integer.toBinaryString(Integer.parseInt(split[i]))));
        }
        int netIndex = Integer.parseInt(split[4]);
        String ipBinary = sb.toString();
        String netBinary = ipBinary.substring(0, netIndex);
        String hostBinary = ipBinary.substring(netIndex);
        String maxHostBinary = getHostBinary(hostBinary, true);
        String minHostBinary = getHostBinary(hostBinary, false);
        String minIPBinary = netBinary + minHostBinary;
        String maxIPBinary = netBinary + maxHostBinary;
        String minIPDecimal = ipBinaryToDotDecimal(minIPBinary);
        String maxIPDecimal = ipBinaryToDotDecimal(maxIPBinary);
        return new IPRange(minIPDecimal, maxIPDecimal);
    }

    public static List<String> allIP(String ipAddress) {
        List<String> list = new LinkedList<>();
        IPRange ipRange = resolveIP(ipAddress);
        String tempIP = ipRange.getStartIP();
        list.add(tempIP);
        do {
            tempIP = getNextIpAddress(tempIP);
            list.add(tempIP);
        } while (!ipRange.getEndIP().equals(tempIP));
        return list;
    }


    /**
     * 获取下一个ip地址
     *
     * @param ipAddress ip地址
     * @return 下一个ip地址
     */
    private static String getNextIpAddress(String ipAddress) {
        String[] ipAddressInArray = ipAddress.split("\\.");
        int[] ipAddressIntArray = new int[4];
        for (int i = 0; i < 4; i++) {
            ipAddressIntArray[i] = Integer.parseInt(ipAddressInArray[i]);
        }
        ipAddressIntArray[3]++;
        for (int i = 3; i > 0; i--) {
            if (ipAddressIntArray[i] > 255) {
                ipAddressIntArray[i] = 0;
                ipAddressIntArray[i - 1]++;
            }
        }
        StringBuilder nextIpAddressStringBuilder = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            nextIpAddressStringBuilder.append(ipAddressIntArray[i]);
            if (i != 3) {
                nextIpAddressStringBuilder.append(".");
            }
        }
        return nextIpAddressStringBuilder.toString();
    }


    /**
     * 将二进制ip地址转换为点分十进制
     */
    private static String ipBinaryToDotDecimal(String binary) {
        String[] strArray = new String[4];
        for (int i = 0; i < 4; i++) {
            int startIndex = i * 8;
            int endIndex = startIndex + 8;
            strArray[i] = String.valueOf(Integer.parseInt(binary.substring(startIndex, endIndex), 2));
        }
        return String.join(".", strArray);
    }

    /**
     * 获取主机号二进制的最大值或者最小值
     * 按照要求，除掉全为1或者全为0，用于预留地址
     */
    private static String getHostBinary(String binary, boolean max) {
        String target, replace;
        if (max) {
            target = "1";
            replace = "0";
        } else {
            target = "0";
            replace = "1";
        }
        String external = binary.replaceAll("[01]", target);
        return external.substring(0, external.length() - 1) + replace;
    }


    /**
     * 二进制补0操作
     */
    private static String complement(String ipBinary) {
        int length = ipBinary.length();
        if (length == 8) {
            return ipBinary;
        } else {
            return String.format("%8s", ipBinary).replace(' ', '0');
        }
    }


    public static class IPRange {

        private String startIP;

        private String endIP;

        public IPRange(String startIP, String endIP) {
            this.startIP = startIP;
            this.endIP = endIP;
        }

        public String getStartIP() {
            return startIP;
        }

        public void setStartIP(String startIP) {
            this.startIP = startIP;
        }

        public String getEndIP() {
            return endIP;
        }

        public void setEndIP(String endIP) {
            this.endIP = endIP;
        }
    }


}
