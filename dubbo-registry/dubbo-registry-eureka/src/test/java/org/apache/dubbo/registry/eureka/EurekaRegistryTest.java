package org.apache.dubbo.registry.eureka;

import org.apache.dubbo.common.Constants;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.utils.ReflectUtils;
import org.apache.dubbo.registry.NotifyListener;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class EurekaRegistryTest {

    private  EurekaRegistry eurekaRegistry;

    @Before
    public void setUp() {
        Map<String, String> params = new HashMap<>();
        params.put(Constants.INTERFACE_KEY, "org.apache.service.EurekaProvider");
        params.put(Constants.APPLICATION_KEY, "eureka-dubbo-test");
        URL url = new URL("eureka", "192.168.99.100",  8761, params);
        eurekaRegistry = new EurekaRegistry(url);
    }

    @Test
    public void initRegistryUrlTest() {

    }

    @Test
    public void fetchChangedUrlsTest() {
        Map<String, String> params = new HashMap<>();
        params.put(Constants.INTERFACE_KEY, "org.apache.service.EurekaProvider");
        params.put(Constants.APPLICATION_KEY, "eureka-dubbo-test");
        URL consumerUrl = new URL("eureka", "192.168.99.100",  8761, params);
        NotifyListener listener = urls -> {};
        eurekaRegistry.doSubscribe(consumerUrl, listener);
    }
}
