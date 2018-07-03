package org.apache.dubbo.registry.eureka;

import org.apache.dubbo.common.Constants;
import org.apache.dubbo.common.URL;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class EurekaRegistryTest {

    @Test
    public void initRegistryUrlTest() {
        Map<String, String> params = new HashMap<>();
        params.put(Constants.INTERFACE_KEY, "org.apache.service.EurekaProvider");
        params.put(Constants.APPLICATION_KEY, "eureka-dubbo-test");
        URL url = new URL("eureka", "192.168.99.100",  8761, params);

//        url = url.setServiceInterface("org.apache.service.EurekaProvider");
        EurekaRegistry eurekaRegistry = new EurekaRegistry(url);
    }
}
