package org.apache.dubbo.registry.eureka;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.DiscoveryClient;
import org.apache.dubbo.common.Constants;
import org.apache.dubbo.common.URL;


import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EurekaClientCacheWrapper {

    private final DiscoveryClient discoveryClient;

    private final Map<String, Integer> subscribedVipNameRefs = new ConcurrentHashMap<>();

    private final Map<String, Long> servicesCache = new ConcurrentHashMap<>();

    private final Map<String, InstanceServiceMap<String, Long>> instancesCache = new ConcurrentHashMap<>();

    private final boolean isUseSecure = false;

    public EurekaClientCacheWrapper(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    public final List<URL> collectChangedUrls() {
        List<URL> changedUrls = new LinkedList<>();
        for (Map.Entry<String, Integer> vipEntry: subscribedVipNameRefs.entrySet()) {
            List<InstanceInfo> instanceInfos =  discoveryClient.getInstancesByVipAddress(vipEntry.getKey(), isUseSecure);
            for (InstanceInfo instanceInfo : instanceInfos) {
                String instanceId = instanceInfo.getInstanceId();
                InstanceServiceMap<String, Long> serviceMap = instancesCache.computeIfAbsent(instanceId,
                        key -> new InstanceServiceMap<>(0L));
                Long  localDirtyTime = serviceMap.getLastUpdatedTime();
                if (localDirtyTime < instanceInfo.getLastDirtyTimestamp()) {
                    serviceMap.setLastUpdatedTime(instanceInfo.getLastDirtyTimestamp());
                    for (Map.Entry<String, Long> serviceEntry: serviceMap.entrySet()) {
                        String serviceUrl = instanceInfo.getMetadata().get(serviceEntry.getKey());
                        if (null == serviceUrl) {
                            servicesCache.remove(serviceEntry.getKey());
                        } else {
                            URL url = URL.valueOf(serviceUrl);
                            Long providerTimestamp = url.getParameter(Constants.TIMESTAMP_KEY, 0L);
                            Long lastListenedTime = serviceEntry.getValue();
                            if (lastListenedTime < providerTimestamp) {
                                servicesCache.put(serviceEntry.getKey(), providerTimestamp);
                                changedUrls.add(url);
                            }
                        }
                    }
                }
            }
        }
        return changedUrls;
    }

    public void doSubscribe(final URL url, final String appName, final String interfaceCls) {
        subscribedVipNameRefs.compute(appName, (key, val) -> {
            if (val == null) {
                return 1;
            } else {
                return val + 1;
            }
        });

        List<String> appInstanceIds = new LinkedList<>();
        String[] categories = url.getParameter(Constants.CATEGORY_KEY, new String[0]);
        List<InstanceInfo> instanceInfos = discoveryClient.getInstancesByVipAddress(appName, isUseSecure);
        for (InstanceInfo instanceInfo: instanceInfos) {
            String instanceId = instanceInfo.getInstanceId();
            appInstanceIds.add(instanceId);
            InstanceServiceMap<String, Long> instanceServiceMap = instancesCache.computeIfAbsent(instanceId, key -> new InstanceServiceMap<>(0L));
            for (String category: categories) {
                instanceServiceMap.putIfAbsent(interfaceCls + Constants.PATH_SEPARATOR + category, 0L);
            }
        }
    }

    public void doUnsubscribe(final URL url, final String appName, final String interfaceCls) {
        subscribedVipNameRefs.compute(appName, (key, val) -> {
            val = val - 1;
            if (val <= 0) {
                return null;
            }
            return val;
        });
    }
}
