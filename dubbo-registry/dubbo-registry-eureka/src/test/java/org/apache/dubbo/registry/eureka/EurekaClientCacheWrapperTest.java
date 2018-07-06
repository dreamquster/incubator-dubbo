package org.apache.dubbo.registry.eureka;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.DiscoveryClient;
import org.apache.dubbo.common.URL;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class EurekaClientCacheWrapperTest {

    @Test
    public void subscribeWithOneChangeUrlTest() {
        String vipAddr = "DubboTestProvider";
        String serviceName = "org.apache.test.DemoProvider";
        DiscoveryClient discoveryClient = mock(DiscoveryClient.class);
        List<InstanceInfo> instanceInfos = new ArrayList<>();
        Map<String, String> metadata = new HashMap<>();
        metadata.put(serviceName + "/provider", "eureka://127.0.0.1:6379/service=org.apache.test.DemoProvider");
        InstanceInfo.Builder builder = InstanceInfo.Builder.newBuilder();
        Date now = new Date();
        builder.setAppName(vipAddr)
                .setVIPAddress(vipAddr)
                .setIPAddr("localhost")
                .setInstanceId(serviceName + "632353")
                .setLastDirtyTimestamp(now.getTime())
                .setMetadata(metadata);
        instanceInfos.add(builder.build());
        given(discoveryClient.getInstancesByVipAddress(vipAddr, false)).willReturn(instanceInfos);

        EurekaClientCacheWrapper eurekaClientCacheWrapper = new EurekaClientCacheWrapper(discoveryClient);

        String consumerString = "consumer://192.168.177.1/org.apache.dubbo.demo.DemoService?application=demo-consumer&category=providers,configurators,routers&check=false&dubbo=2.0.2&interface=org.apache.dubbo.demo.DemoService&methods=sayHello&pid=17236&qos.port=33333&side=consumer&timestamp=1530870200927";
        URL url = URL.valueOf(consumerString);
        eurekaClientCacheWrapper.doSubscribe(url, vipAddr, serviceName);
        List<URL> changedUrl = eurekaClientCacheWrapper.collectChangedUrls();

    }

}
