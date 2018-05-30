package douban;

import lombok.Data;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static douban.Cookies.agent;
import static douban.Cookies.host;
import static douban.Cookies.port;
import static douban.Cookies.proxy;

@Data
public class Node {
    static Pattern directorPattern = Pattern.compile("<span ><span class='pl'>导演</span>: <span class='attrs'>(.*?)</span></span><br/>");
    static Pattern directorInnerPattern = Pattern.compile("<a href=\".*\" rel=\"v:directedBy\">(.*?)</a>");
    static Pattern writerPattern = Pattern.compile("<span ><span class='pl'>编剧</span>: <span class='attrs'>(.*?)</span></span><br/>");
    static Pattern writerInnerPattern = Pattern.compile("<a href=\".*\">(.*?)</a>");
    static Pattern actorPattern = Pattern.compile("<span class=\"actor\"><span class='pl'>主演</span>: <span class='attrs'>(.*?)</span></span><br/>");
    static Pattern actorInnerPattern = Pattern.compile("<a href=\".*\" rel=\"v:starring\">(.*?)</a>");
    static Pattern typePattern = Pattern.compile("<span class=\"pl\">类型:</span>(.*?)<br/>");
    static Pattern typeInnerPattern = Pattern.compile("<span property=\"v:genre\">(.*?)</span>");
    static Pattern areaPattern = Pattern.compile("<span class=\"pl\">制片国家/地区:</span> (.*?)<br/>");
    static Pattern namePattern = Pattern.compile("<span property=\"v:itemreviewed\">(.*?)</span>");
    static Pattern timePattern = Pattern.compile("<span class=\"year\">\\((.*?)\\)</span>");
    static Pattern fivePattern = Pattern.compile("<span class=\"stars5 starstop\" title=\"力荐\">\\s*5星\\s*</span>\\s*<div class=\"power\" style=\"width:\\d*px\"></div>\\s*<span class=\"rating_per\">(.*?)%</span>");
    static Pattern fourPattern = Pattern.compile("<span class=\"stars4 starstop\" title=\"推荐\">\\s*4星\\s*</span>\\s*<div class=\"power\" style=\"width:\\d*px\"></div>\\s*<span class=\"rating_per\">(.*?)%</span>");
    static Pattern threePattern = Pattern.compile("<span class=\"stars3 starstop\" title=\"还行\">\\s*3星\\s*</span>\\s*<div class=\"power\" style=\"width:\\d*px\"></div>\\s*<span class=\"rating_per\">(.*?)%</span>");
    static Pattern twoPattern = Pattern.compile("<span class=\"stars2 starstop\" title=\"较差\">\\s*2星\\s*</span>\\s*<div class=\"power\" style=\"width:\\d*px\"></div>\\s*<span class=\"rating_per\">(.*?)%</span>");
    static Pattern onePattern = Pattern.compile("<span class=\"stars1 starstop\" title=\"很差\">\\s*1星\\s*</span>\\s*<div class=\"power\" style=\"width:\\d*px\"></div>\\s*<span class=\"rating_per\">(.*?)%</span>");
    static Pattern markPattern = Pattern.compile("<strong class=\"ll rating_num\" property=\"v:average\">(.*?)</strong>");
    static Pattern markCountPattern = Pattern.compile("<span property=\"v:votes\">(\\d*?)</span>");
    static Pattern commentPattern = Pattern.compile("\\?status=P\">全部 (\\d*?) 条</a>");


    private String name;
    private String director;
    private String writer;
    private String actor;
    private String type;
    private String area;
    private String time;
    private Double one;
    private Double two;
    private Double three;
    private Double four;
    private Double five;
    private Double mark;
    private Integer comment;
    private Integer markCount;
    private String url;

    public static Node get(String url) {
        try {
            Connection.Response response = Jsoup.connect(url)
                    .userAgent(agent)
                    .proxy(host, port)
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .execute();
            if (response.statusCode() != 200) {
                ProxyInfo info = proxy.take();
                host = info.getHost();
                port = info.getPort();
                System.out.println("切换代理3。剩余代理数量"+proxy.size());
                return null;
            }
            String html = response.body();
            Node node = new Node();
            System.out.println(url);
            node.director = get(html, directorPattern, directorInnerPattern);
            node.writer = get(html, writerPattern, writerInnerPattern);
            node.actor = get(html, actorPattern, actorInnerPattern);
            node.type = get(html, typePattern, typeInnerPattern);
            node.area = get(html, areaPattern);
            node.time = get(html, timePattern);
            node.one = Double.parseDouble(get(html, onePattern));
            node.two = Double.parseDouble(get(html, twoPattern));
            node.three = Double.parseDouble(get(html, threePattern));
            node.four = Double.parseDouble(get(html, fourPattern));
            node.five = Double.parseDouble(get(html, fivePattern));
            node.mark = Double.parseDouble(get(html, markPattern));
            node.comment = Integer.parseInt(get(html, commentPattern));
            node.markCount = Integer.parseInt(get(html, markCountPattern));
            node.name = get(html, namePattern);
            node.url = url;
            return node;
        } catch (Exception e) {
            ProxyInfo info = null;
            try {
                info = proxy.take();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            host = info.getHost();
            port = info.getPort();
            System.out.println("切换代理2。剩余代理数量"+proxy.size());
            e.printStackTrace();
            return null;
        }
    }

    private static String get(String html, Pattern pattern, Pattern innerPattern) {
        StringBuilder sb = new StringBuilder();
        String s = get(html, pattern);
        if (s == null)
            return sb.toString();
        Matcher matcher = innerPattern.matcher(s);
        while (matcher.find()) {
            sb.append(matcher.group(1)).append(" ");
        }
        return sb.toString().trim();
    }

    private static String get(String html, Pattern pattern) {
        Matcher matcher = pattern.matcher(html);
        while (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
