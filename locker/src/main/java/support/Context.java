package support;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * Created by wangqi on 2017/9/16 上午11:23.
 */
public class Context {
    private Map<String, String> properties = new HashMap<>();

    {
        String conf = FileUtil.readFile("classpath:application.properties");
        if( null!=conf && conf.contains("=") ){
            String[] lines = conf.split("\n");
            IntStream.range(0, lines.length).forEach( o -> {
                String[] oneConf = lines[o].split("=");
                properties.put(oneConf[0],oneConf[1]);
            });
        }
    }

    public String getOneConf(String key) {
        return properties.get(key);
    }
}
