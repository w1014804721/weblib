package douban;

import lombok.Data;
import lombok.experimental.var;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.stream.IntStream;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

public class Matrix {
    @Data
    static class Value {
        private Double e;
        private Double d;

        Value(double[] data) {
            this.e = Arrays.stream(data).average().getAsDouble();
            this.d = Arrays.stream(data)
                    .map(a -> pow(a - e, 2))
                    .reduce((a, b) -> a + b)
                    .getAsDouble();
        }
    }

    public static void main(String[] args) throws SQLException {
        var connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/douban?useUnicode=true&characterEncoding=UTF-8&uerSSL=false&serverTimezone=Hongkong", "root", "112233");
        var p = connection.prepareStatement("select one,two,three,four,five,mark,comment,markCount from datas");
        var set = p.executeQuery();
        double[][] data = new double[8][4681];
        var count = 0;
        String[] s = {"one", "two", "three", "four", "five", "mark", "comment", "markCount"};
        while (set.next()) {
            for (int i = 0; i < 6; i++) {
                data[i][count] = set.getDouble(s[i]);
            }
            data[6][count] = (double) set.getInt(s[6]);
            data[7][count++] = (double) set.getInt(s[7]);
        }
        Value[] value = new Value[8];
        IntStream.range(0, value.length).forEach(i -> value[i] = new Value(data[i]));
        double[][] result = new double[8][8];
        IntStream.range(0, result.length).forEach(i -> IntStream.rangeClosed(0, i).forEach(j -> {
            result[i][j] = result(i, j, data, value);
            result[j][i] = result[i][j];
        }));
        IntStream.range(0, result.length).forEach(i -> IntStream.range(0, result[i].length).forEach(j -> {
            System.out.println(s[i]+" "+s[j]+" "+result[i][j]);
        }));
    }

    private static double result(int i, int j, double[][] data, Value[] value) {
        Value iv = value[i];
        Value jv = value[j];
        double[] ii = Arrays.stream(data[i]).map(a -> a - iv.e).toArray();
        double[] jj = Arrays.stream(data[j]).map(a -> a - jv.e).toArray();
        return IntStream.range(0, ii.length)
                .mapToDouble(index -> ii[index] * jj[index])
                .reduce((a, b) -> a + b)
                .getAsDouble() / sqrt(iv.d) / sqrt(jv.d);
    }
}
