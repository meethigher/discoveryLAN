package top.meethigher;

import com.beust.jcommander.Parameter;

/**
 * 参数
 *
 * @author chenchuancheng
 * @since 2023/4/3 20:51
 */
public class ArgsObj {

    @Parameter(names = "-ip", description = "ip地址,采用斜线记法")
    private String ip = "192.168.110.99/24";


    @Parameter(names = "-b", description = "一个线程测试连接数量")
    private int batch = 10;

    @Parameter(names = "--timer", description = "每隔一小时扫描一次")
    private boolean timer;

    @Parameter(names = "--help", help = true)
    private boolean help;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getBatch() {
        return batch;
    }

    public void setBatch(int batch) {
        this.batch = batch;
    }

    public boolean isHelp() {
        return help;
    }

    public void setHelp(boolean help) {
        this.help = help;
    }

    public boolean isTimer() {
        return timer;
    }

    public void setTimer(boolean timer) {
        this.timer = timer;
    }

}
