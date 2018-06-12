import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.*;

import static java.lang.Double.compare;
import static java.lang.Math.abs;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

public class DBScan {
    class Draw extends JFrame {
        double minLa;
        double maxLa;
        double minLo;
        double maxLo;
        Predicate<Node> filter;

        Draw(double minLa, double maxLa, double minLo, double maxLo, Predicate<Node> filter) {
            this.minLa = minLa;
            this.maxLa = maxLa;
            this.minLo = minLo;
            this.maxLo = maxLo;
            this.filter = filter;
        }

        int latitude(double l) {
            return (int) Math.round((l - minLa) * (400) / (maxLa - minLa)) + 100;
        }

        int longitude(double l) {
            return (int) Math.round((l - minLo) * (400) / (maxLo - minLo)) + 100;
        }

        void init() {
            this.setBounds(100, 100, 800, 600);
            this.setVisible(true);
        }

        @Override
        public void paint(Graphics g) {
            System.out.println("正在开始绘画");
            g.setColor(Color.WHITE);
            g.fillRect(0,0,this.getWidth(),this.getHeight());
            nodeList.stream().filter(filter).forEach(node -> {
                g.setColor(colorMap.get(node.color));
                int x = latitude(abs(node.latitude));
                int y = longitude(abs(node.longitude));
                g.fillOval(x, y, 7, 7);
            });
            System.out.println("绘画结束");
        }

        public void save(String path) throws IOException {
            BufferedImage image = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_RGB);
            this.paint(image.createGraphics());
            ImageIO.write(image, "jpeg", new File(path));
        }
    }

    static Map<Integer, Color> colorMap = new HashMap<>();

    static {
        colorMap.put(0, Color.GRAY);
        colorMap.put(1, Color.RED);
        colorMap.put(2, Color.BLUE);
        colorMap.put(3, Color.CYAN);
        colorMap.put(4, Color.GREEN);
        colorMap.put(5, Color.YELLOW);
        colorMap.put(6, Color.PINK);
        colorMap.put(7, Color.BLACK);
        colorMap.put(8, Color.ORANGE);
    }

    static class Node {
        int color = -1;
        double latitude;
        double longitude;
        int id;
        int track_id;
        Set<Node> nearSet;
        boolean visit = false;

        double dist(Node node) {
            return sqrt(pow(this.latitude - node.latitude, 2) + pow(this.longitude - node.longitude, 2));
        }

        @Override
        public String toString() {
            return id + "  " + color;
        }
    }

    private double e;
    private int minPts;
    private List<Node> nodeList;

    DBScan(double e, int minPts) {
        this.e = e;
        this.minPts = minPts;
    }

    void read() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/wajue?useUnicode=true&characterEncoding=UTF-8&uerSSL=false&serverTimezone=Hongkong"
                , "root"
                , "112233");
        ResultSet set = connection.prepareStatement("select id,latitude,longitude,track_id from go_track_trackspoints")
                .executeQuery();
        nodeList = new ArrayList<>(set.getRow());
        while (set.next()) {
            Node node = new Node();
            node.id = set.getInt("id");
            node.latitude = set.getDouble("latitude");
            node.longitude = set.getDouble("longitude");
            node.track_id = set.getInt("track_id");
            nodeList.add(node);
        }
    }

    void pretreatment() {
        nodeList.forEach(i -> i.nearSet = nodeList
                .stream()
                .filter(j -> i != j && compare(i.dist(j), e) <= 0)
                .collect(Collectors.toSet()));
    }

    void cluster() {
        AtomicInteger color = new AtomicInteger(1);
        nodeList.forEach(node -> {
            if (node.visit) {
                return;
            }
            node.visit = true;
            if (node.nearSet.size() < minPts) {
                node.color = 0;
            } else {
                expandCluster(node, color.getAndIncrement());
            }
        });
        System.out.println("类别数量" + (color.get() - 1));
    }

    void draw() throws IOException {
        Draw d1 = new Draw(27.2836741, 27.60317466, 48.516397, 48.63291841, n -> n.longitude < -40);
        d1.init();
        d1.save("/home/wjx/desktop/1.jpg");
        Draw d2 = new Draw(10.29284478, 11.01549612, 36.49335713, 37.53588639, n -> n.longitude >= -40);
        d2.init();
        d2.save("/home/wjx/desktop/2.jpg");
    }

    private void expandCluster(Node node, int color) {
        Set<Node> set = node.nearSet;
        node.color = color;
        while (!set.isEmpty()) {
            Set<Node> next = new HashSet<>();
            set.forEach(n -> {
                if (!n.visit) {
                    n.visit = true;
                    if (n.nearSet.size() >= minPts) {
                        next.addAll(n.nearSet);
                    }
                }
                if (n.color <= 0) {
                    n.color = color;
                }
            });
            set = next;
        }
    }

    public static void main(String[] args) throws SQLException, ClassNotFoundException, IOException, AWTException {
        DBScan scan = new DBScan(0.005, 4);
        System.out.println("开始读取数据");
        scan.read();
        System.out.println("读取数据完毕");
        System.out.println("正在预处理数据");
        scan.pretreatment();
        System.out.println("预处理完毕");
        System.out.println("正在聚类");
        scan.cluster();
        System.out.println("聚类结束");
        scan.draw();
    }
}
