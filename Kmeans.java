package douban;

import com.alibaba.fastjson.JSONObject;
import huffman.Tree;
import lombok.Data;
import lombok.experimental.var;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.Math.max;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

public class Kmeans {
    @Data
    static class Node {
        private String url;
        private Double one;
        private Double two;
        private Double three;
        private Double four;
        private Double five;

        Node(Node a, Node b) {
            this.one = a.one + b.one;
            this.two = a.two + b.two;
            this.three = a.three + b.three;
            this.four = a.four + b.four;
            this.five = a.five + b.five;
        }

        Node() {
        }

        double distance(Node node) {
            return pow(one - node.one, 2)
                    + pow(two - node.two, 2)
                    + pow(three - node.three, 2)
                    + pow(four - node.four, 2)
                    + pow(five - node.five, 2);
        }

        double sqrtDistance(Node node) {
            return sqrt(distance(node));
        }

        String getFeature() {
            return "" + one + two + three + four + five;
        }

        public int hashCode() {
            return getFeature().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Node)) {
                return false;
            }
            return getFeature().equals(((Node) obj).getFeature());
        }

        Node change(int size) {
            this.one = this.one / size;
            this.two = this.two / size;
            this.three = this.three / size;
            this.four = this.four / size;
            this.five = this.five / size;
            return this;
        }
    }

    @Data
    static class Cluster {
        private Node mid;
        private Set<Node> set;

        Cluster(Node mid) {
            this.mid = mid;
            this.set = new HashSet<>();
        }
    }

    private static void compute(int k, List<Node> nodes) {
        Map<Node, Cluster> clusters = new HashMap<>(8);
        while (clusters.size() != k) {
            Node mid = nodes.get((int) (Math.random() * nodes.size()));
            clusters.put(mid, new Cluster(mid));
        }

        boolean change = true;
        int count = 0;
        while (change) {
            System.out.println("第" + (++count) + "次尝试");
            print(clusters);
            change = false;
            nodes.forEach(node -> clusters.get(getClosest(clusters.keySet(), node)).set.add(node));
            Set<Node> newMids = newMids(clusters);
            if (newMids.equals(clusters.keySet())) {
                System.out.println("最终结果");
                System.out.println(JSONObject.toJSONString(clusters.keySet()));
                System.out.println("轮廓系数为" + judge(clusters, nodes.size()));
                continue;
            }
            change = true;
            clusters.clear();
            newMids.forEach(node -> clusters.put(node, new Cluster(node)));
        }
    }

    private static double judge(Map<Node, Cluster> clusters, int count) {
        return clusters.values().stream().mapToDouble(v-> v.set.stream().mapToDouble(o -> {
            double a = v.set.stream().filter(f -> f != o).mapToDouble(o::sqrtDistance).average().getAsDouble();
            double b = clusters.keySet()
                    .stream()
                    .filter(f -> f != o)
                    .mapToDouble(key -> clusters.get(key).set.stream().mapToDouble(o::sqrtDistance).sum())
                    .sum() / (count - v.set.size());
            return (b - a) / max(a, b);
        }).sum()).sum()/count;
    }

    private static void print(Map<Node, Cluster> clusters) {
        clusters.keySet().forEach(System.out::println);
    }

    private static Set<Node> newMids(Map<Node, Cluster> clusters) {
        Set<Node> nodes = new HashSet<>(clusters.size());
        clusters.forEach((k, v) -> nodes.add(v.set.stream().reduce(Node::new).get().change(v.set.size())));
        return nodes;
    }

    private static Node getClosest(Set<Node> mids, Node node) {
        AtomicReference<Node> result = new AtomicReference<>();
        final double[] min = {Double.MAX_VALUE};
        mids.forEach(n -> {
            double distance = n.distance(node);
            if (distance < min[0]) {
                min[0] = distance;
                result.set(n);
            }
        });
        return result.get();
    }

    public static void main(String[] args) throws SQLException {
        var connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/douban?useUnicode=true&characterEncoding=UTF-8&uerSSL=false&serverTimezone=Hongkong", "root", "112233");
        var p = connection.prepareStatement("select one,two,three,four,five,url from datas");
        var set = p.executeQuery();
        List<Node> nodes = new ArrayList<>();
        while (set.next()) {
            Node node = new Node();
            node.one = set.getDouble("one");
            node.two = set.getDouble("two");
            node.three = set.getDouble("three");
            node.four = set.getDouble("four");
            node.five = set.getDouble("five");
            nodes.add(node);
        }
        compute(15, nodes);
    }
}
