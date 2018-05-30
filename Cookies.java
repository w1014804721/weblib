package douban;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class Cookies {
    static Map<String, String> cookies = new HashMap<>();
    static String agent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3359.181 Safari/537.36";
    static String s = "bid=zYVn4czjD9A; ap=1; ll=\"118220\"; _vwo_uuid_v2=D609DC0BC6DB0170B4DC00F7C4643970B|31beced344c97bae2bac2a61f79078f8; viewed=\"1436131\"; gr_user_id=bf5c6b4e-c67f-4825-91e3-9032ca3f22e8; __utma=223695111.1532081468.1525674643.1527332753.1527335676.7; __utmz=223695111.1527335676.7.7.utmcsr=baidu|utmccn=(organic)|utmcmd=organic; __utma=30149280.1256598932.1527318225.1527433855.1527496130.7; __utmc=30149280; __utmz=30149280.1527496130.7.7.utmcsr=baidu|utmccn=(organic)|utmcmd=organic; ct=y; ps=y; _pk_ref.100001.4cf6=%5B%22%22%2C%22%22%2C1527576188%2C%22http%3A%2F%2Flocalhost%3A63343%2Face%2Ftest.html%3F_ijt%3Dadg0kvlt634nn2aqmncus5gl2g%22%5D; _pk_ses.100001.4cf6=*; push_noty_num=0; push_doumail_num=0; dbcl2=\"141372384:8JFWd2Clh2c\"; ck=lygw; _pk_id.100001.4cf6=30de7af15b3b3bbc.1525674643.11.1527579750.1527571387.; report=ref=%2F&from=mv_a_pst";
    static volatile String host;
    static volatile Integer port;
    static BlockingQueue<ProxyInfo> proxy = new LinkedBlockingDeque<>();
    static {
        Arrays.stream(s.split("; ")).forEach(a -> cookies.put(a.split("=")[0], a.split("=")[1]));
    }
}
