package org.apache.dubbo.registry.eureka;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.DiscoveryClient;
import org.apache.dubbo.common.URL;
import org.junit.Assert;
import org.junit.Test;
import rx.internal.util.LinkedArrayList;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class EurekaClientCacheWrapperTest {

    @Test
    public void subscribeWithOneChangeUrlTest() {
        String vipAddr = "DubboTestProvider";
        URL expectedChangeUrl = URL.valueOf("dubbo://192.168.177.1:20880/org.apache.dubbo.demo.DemoService?anyhost=true&application=demo-provider&dubbo=2.0.2&generic=false&interface=org.apache.dubbo.demo.DemoService&methods=sayHello&pid=21808&side=provider&timestamp=1531103335518");
        String serviceName = expectedChangeUrl.getServiceInterface();
        DiscoveryClient discoveryClient = mock(DiscoveryClient.class);
        List<InstanceInfo> instanceInfos = initialInstanceInfos(vipAddr, expectedChangeUrl);
        given(discoveryClient.getInstancesByVipAddress(vipAddr, false)).willReturn(instanceInfos);

        EurekaClientCacheWrapper eurekaClientCacheWrapper = new EurekaClientCacheWrapper(discoveryClient);

        String consumerString = "consumer://192.168.177.1/org.apache.dubbo.demo.DemoService?application=demo-consumer&category=providers,configurators,routers&check=false&dubbo=2.0.2&interface=org.apache.dubbo.demo.DemoService&methods=sayHello&pid=17236&qos.port=33333&side=consumer&timestamp=1530870200927";
        URL url = URL.valueOf(consumerString);
        eurekaClientCacheWrapper.doSubscribe(url, vipAddr, serviceName);
        List<URL> changedUrl = eurekaClientCacheWrapper.collectChangedUrls();
        Assert.assertEquals(1, changedUrl.size());
        Assert.assertEquals(expectedChangeUrl, changedUrl.get(0));
        eurekaClientCacheWrapper.doUnsubscribe(url, vipAddr, serviceName);

        changedUrl = eurekaClientCacheWrapper.collectChangedUrls();
        Assert.assertEquals(0, changedUrl.size());
    }

    private List<InstanceInfo> initialInstanceInfos(String vipAddr, URL expectedChangeUrl) {
        List<InstanceInfo> instanceInfos = new ArrayList<>();
        String serviceName = expectedChangeUrl.getServiceInterface();
        Map<String, String> metadata = new HashMap<>();
        metadata.put(serviceName + "/providers", expectedChangeUrl.toString());
        InstanceInfo.Builder builder = InstanceInfo.Builder.newBuilder();
        Date now = new Date();
        builder.setAppName(vipAddr)
                .setVIPAddress(vipAddr)
                .setIPAddr("localhost")
                .setInstanceId(serviceName + "632353")
                .setLastDirtyTimestamp(now.getTime())
                .setMetadata(metadata);
        instanceInfos.add(builder.build());
        InstanceInfo.Builder notChangedInstanceBuilder = InstanceInfo.Builder.newBuilder()
                .setAppName(vipAddr).setVIPAddress(vipAddr)
                .setIPAddr("192.168.177.1")
                .setInstanceId(serviceName + "35325")
                .setLastDirtyTimestamp(-3)
                .setMetadata(metadata);
        instanceInfos.add(notChangedInstanceBuilder.build());
        return instanceInfos;
    }

    @Test
    public void collectChangedWhenUnregisterServiceTest() {
        String vipAddr = "DubboTestProvider";
        URL registeredUrl = URL.valueOf("dubbo://192.168.177.1:20880/org.apache.dubbo.demo.DemoService?anyhost=true&application=demo-provider&dubbo=2.0.2&generic=false&interface=org.apache.dubbo.demo.DemoService&methods=sayHello&pid=21808&side=provider&timestamp=1531103335518");
        String serviceName = registeredUrl.getServiceInterface();
        DiscoveryClient discoveryClient = mock(DiscoveryClient.class);
        List<InstanceInfo> instanceInfos = initialInstanceInfos(vipAddr, registeredUrl);
        given(discoveryClient.getInstancesByVipAddress(vipAddr, false)).willReturn(instanceInfos);
        EurekaClientCacheWrapper eurekaClientCacheWrapper = new EurekaClientCacheWrapper(discoveryClient);
        String consumerString = "consumer://192.168.177.1/org.apache.dubbo.demo.DemoService?application=demo-consumer&category=providers,configurators,routers&check=false&dubbo=2.0.2&interface=org.apache.dubbo.demo.DemoService&methods=sayHello&pid=17236&qos.port=33333&side=consumer&timestamp=1530870200927";
        URL consumerUrl = URL.valueOf(consumerString);
        eurekaClientCacheWrapper.doSubscribe(consumerUrl, vipAddr, consumerUrl.getServiceInterface());
        List<URL> changedUrl = eurekaClientCacheWrapper.collectChangedUrls();

        // unregister ${serviceName}
        Map<String, String> metadata = instanceInfos.get(0).getMetadata();
        metadata.remove(serviceName + "/providers");
        instanceInfos.get(0).setLastDirtyTimestamp(System.currentTimeMillis());

        changedUrl = eurekaClientCacheWrapper.collectChangedUrls();
        Assert.assertEquals(1, changedUrl.size());
        Assert.assertEquals(registeredUrl, changedUrl.get(0));
    }

}
