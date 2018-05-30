package douban;

import lombok.Data;

@Data
public class ProxyInfo {
    private String host;
    private Integer port;

    public ProxyInfo(String s) {
        s = s.split(",")[0];
        host = s.split(":")[0];
        port = Integer.valueOf(s.split(":")[1]);
    }
}
