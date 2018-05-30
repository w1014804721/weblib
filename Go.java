package douban;

import com.alibaba.fastjson.JSONObject;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static douban.Cookies.agent;
import static douban.Cookies.cookies;
import static douban.Cookies.host;
import static douban.Cookies.port;
import static douban.Cookies.proxy;
import static java.lang.String.valueOf;

public class Go {

    static String listUrl = "https://movie.douban.com/j/new_search_subjects";
    static BlockingQueue<String> urlPool = new ArrayBlockingQueue<>(100);
    static BlockingQueue<Node> dataPool = new ArrayBlockingQueue<>(100);
    static AtomicInteger start = new AtomicInteger(3899);

    private static void get() {
        while (true) {
            System.out.println("正在爬取电影信息");
            try {
                Node node = Node.get(urlPool.take());
                if (node != null)
                    dataPool.put(node);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            System.out.println("爬取电影信息成功");
        }
    }

    private static void getList() {
        while (true) {
            System.out.println("正在爬取列表");
            List<String> list = null;
            try {
                org.jsoup.Connection.Response response = Jsoup.connect(listUrl)
                        //           .cookies(cookies)
                        .userAgent(agent)
                        .proxy(host, port)
                        .ignoreContentType(true)
                        .data("sort", "T")
                        .data("range", "0,100")
                        .data("start", valueOf(start.get()))
                        .ignoreHttpErrors(true)
                        .execute();
                if (response.statusCode() != 200) {
                    ProxyInfo info = proxy.take();
                    host = info.getHost();
                    port = info.getPort();
                    System.out.println("切换代理1。剩余代理数量"+proxy.size());
                    continue;
                }
                String body = response.body();
                list = JSONObject.parseObject(body).getJSONArray("data")
                        .stream().map(a -> {
                            JSONObject obj = (JSONObject) a;
                            return obj.getString("url");
                        }).collect(Collectors.toList());
            } catch (Exception e) {
                e.printStackTrace();
                ProxyInfo info = null;
                try {
                    info = proxy.take();
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                host = info.getHost();
                port = info.getPort();
                System.out.println("切换代理1。剩余代理数量"+proxy.size());
                continue;
            }
            list.forEach(a -> {
                try {
                    urlPool.put(a);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            start.set(start.get() + list.size());
            System.out.println("获取列表");
        }
    }

    public static void main(String[] args) throws ClassNotFoundException, SQLException, InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        new Thread(() -> {
            while (true) {
                String s;
                try {
                    s = Jsoup.connect("https://proxyapi.mimvp.com/api/fetchopen.php?orderid=860010922826112501&num=20&http_type=1,2&check_success_count=100&result_fields=1,2")
                            .execute()
                            .body();
                    Arrays.stream(s.split("\n")).forEach(sp -> {
                        try {
                            proxy.put(new ProxyInfo(sp));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    });
                    if (host == null) {
                        ProxyInfo info = proxy.take();
                        host = info.getHost();
                        port = info.getPort();
                        countDownLatch.countDown();
                    }
                    Thread.sleep(60 * 1000);
                    System.out.println("更新代理池。剩余代理"+proxy.size());

                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
            }
        }).start();
        countDownLatch.await();
        new Thread(Go::getList).start();
        new Thread(Go::get).start();
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/douban?useUnicode=true&characterEncoding=UTF-8&uerSSL=false&serverTimezone=Hongkong", "root", "112233");
        final PreparedStatement[] p = {connection.prepareStatement("insert into datas values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)")};
        new Thread(() -> {
            int count = 0;
            int all = 0;
            while (true) {
                try {
                    Node node = dataPool.take();
                    all++;
                    System.out.println("正在插入第" + all + "条记录");
                    p[0].setString(1, node.getName());
                    p[0].setString(2, node.getDirector());
                    p[0].setString(3, node.getWriter());
                    p[0].setString(4, node.getActor());
                    p[0].setString(5, node.getType());
                    p[0].setString(6, node.getArea());
                    p[0].setString(7, node.getTime());
                    p[0].setDouble(8, node.getOne());
                    p[0].setDouble(9, node.getTwo());
                    p[0].setDouble(10, node.getThree());
                    p[0].setDouble(11, node.getFour());
                    p[0].setDouble(12, node.getFive());
                    p[0].setDouble(13, node.getMark());
                    p[0].setInt(14, node.getComment());
                    p[0].setInt(15, node.getMarkCount());
                    p[0].setString(16, node.getUrl());
                    p[0].addBatch();
                    count++;
                    System.out.println(node);
                    if (count >= 20) {
                        p[0].executeBatch();
                        p[0] = connection.prepareStatement("insert into datas values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
                        count = 0;
                        System.out.println("start:"+start.get());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
