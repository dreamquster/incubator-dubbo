package org.apache.dubbo.registry.eureka;

import com.netflix.discovery.DefaultEurekaClientConfig;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by houbank on 2018/6/21.
 */
public class EurekaClientConfiguration extends DefaultEurekaClientConfig {

    public static final String DEFAULT_ZONE = "defaultZone";

    public static final String DEFAULT_URL = "http://localhost:8761/eureka/";

    private final Map<String, List<String>> zoneServiceUrls = new HashMap<>();

    public EurekaClientConfiguration() {
        this.zoneServiceUrls.put(DEFAULT_ZONE, Arrays.asList(DEFAULT_URL));
    }

    @Override
    public List<String> getEurekaServerServiceUrls(String myZone) {
        List<String> zoneUrls = zoneServiceUrls.get(myZone);
        if (zoneUrls == null || zoneUrls.isEmpty()) {
            return zoneServiceUrls.get(DEFAULT_ZONE);
        }

        return zoneUrls;
    }

    public void setEurekaServerServiceUrls(String myZone, List<String> serviceUrls) {
        zoneServiceUrls.put(myZone, serviceUrls);
    }
}
