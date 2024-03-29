package top.meethigher;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.validators.PositiveInteger;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import static top.meethigher.discoverylan.DiscoveryLAN.run;

/**
 * 启动类
 *
 * @author chenchuancheng
 * @since 2023/4/3 20:46
 */
@Data
public class Application {

    @Parameter(names = "-ip", description = "ip地址,采用斜线记法")
    private String ip = "192.168.110.99/24";

    @Parameter(names = "-b", description = "一个线程测试连接数量", validateWith = PositiveInteger.class)
    private int batch = 10;

    @Parameter(names = "-d", description = "扫描间隔, 单位分钟(添加--timer参数后生效)", validateWith = PositiveInteger.class)
    private int delay = 30;

    @Parameter(names = "-p", description = "指定端口, 多个请使用英文逗号分隔")
    private String port;

    @Parameter(names = "--scan", description = "是否扫描端口")
    private boolean scanPort = false;

    @Parameter(names = "--timer", description = "开启定时扫描任务")
    private boolean timer = false;


    @Parameter(names = "--hostname", description = "开启hostname显示, 受dns服务器影响, 不保证一定拿到")
    private boolean hostname = false;

    @Parameter(names = "--help", help = true)
    private boolean help;

    public static void main(String... args) throws Exception {
        Application app = new Application();
        JCommander jCommander = JCommander.newBuilder()
                .addObject(app)
                .build();
        jCommander.parse(args);
        if (app.isHelp()) {
            jCommander.usage();
            return;
        }
        run(app);
    }

}
