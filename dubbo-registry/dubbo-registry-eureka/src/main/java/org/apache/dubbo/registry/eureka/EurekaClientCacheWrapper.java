package org.apache.dubbo.registry.eureka;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.DiscoveryClient;
import org.apache.dubbo.common.Constants;
import org.apache.dubbo.common.URL;


import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class EurekaClientCacheWrapper {

    private final DiscoveryClient discoveryClient;

    private final Map<String, Set<URL>> subscribedVipUrls = new ConcurrentHashMap<>();

    private final Map<String, InstanceServiceMap<String, URL>> instancesCache = new ConcurrentHashMap<>();

    private final boolean isUseSecure = false;

    private static final URL NONE = new URL(null, null, 0);

    public EurekaClientCacheWrapper(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    public final List<URL> collectChangedUrls() {
        return collectChangedUrls(false);
    }

    public final List<URL> collectChangedUrls(final boolean emptyRefech) {
        List<URL> changedUrls = new LinkedList<>();
        for (Map.Entry<String, Set<URL>> vipEntry: subscribedVipUrls.entrySet()) {
            List<InstanceInfo> instanceInfos =  discoveryClient.getApplication(vipEntry.getKey()).getInstances();
            if (instanceInfos.isEmpty() && emptyRefech) {
                instanceInfos = discoveryClient.getApplication(vipEntry.getKey()).getInstancesAsIsFromEureka();
            }
            for (InstanceInfo instanceInfo : instanceInfos) {
                String instanceId = instanceInfo.getInstanceId();
                InstanceServiceMap<String, URL> serviceMap = instancesCache.computeIfAbsent(instanceId,
                        key -> new InstanceServiceMap<>(0L));
                Long  localDirtyTime = serviceMap.getLastUpdatedTime();
                if (localDirtyTime < instanceInfo.getLastDirtyTimestamp()) {
                    serviceMap.setLastUpdatedTime(instanceInfo.getLastDirtyTimestamp());
                    for (URL listenedUrl : vipEntry.getValue()) {
                        changedUrls.addAll(differLocalFromInstanceMeta(instanceInfo, serviceMap, listenedUrl));
                    }
                }
            }
        }
        return changedUrls;
    }

    private final List<URL> differLocalFromInstanceMeta(InstanceInfo instanceInfo, InstanceServiceMap<String, URL> serviceMap, URL listenedUrl) {
        List<URL> changedUrls = new LinkedList<>();
        String[] categories = listenedUrl.getParameter(Constants.CATEGORY_KEY, new String[0]);
        String interfaceCls = listenedUrl.getServiceInterface();
        for (String category: categories) {
            String serviceCategroy = interfaceCls + Constants.PATH_SEPARATOR + category;
            URL localUrl = serviceMap.get(serviceCategroy);
            String serviceUrlStr = instanceInfo.getMetadata().get(serviceCategroy);
            if (serviceUrlStr != null) {
                URL remoteServiceUrl = URL.valueOf(serviceUrlStr);
                Long providerTimestamp = remoteServiceUrl.getParameter(Constants.TIMESTAMP_KEY, 0L);
                if (localUrl == null) {
                    // register url
                    serviceMap.put(serviceCategroy, remoteServiceUrl);
                    changedUrls.add(remoteServiceUrl);
                } else {
                    long lastListenedTime = localUrl.getParameter(Constants.TIMESTAMP_KEY, 0L);
                    if (lastListenedTime < providerTimestamp) {
                        // remoteUrl timestamp is newer than local
                        serviceMap.put(serviceCategroy, remoteServiceUrl);
                        changedUrls.add(remoteServiceUrl);
                    }
                }
            } else {
                // unregister url
                if (localUrl != null) {
                    serviceMap.remove(serviceCategroy);
                    changedUrls.add(localUrl);
                }
            }

        }
        return changedUrls;
    }

    public void doSubscribe(final URL url, final String appName, final String interfaceCls) {
        subscribedVipUrls.compute(appName, (key, val) -> {
            if (val == null) {
                val = new HashSet<>();
            }
            val.add(url);
            return val;
        });

        List<InstanceInfo> instanceInfos = discoveryClient.getApplication(appName).getInstances();
        for (InstanceInfo instanceInfo: instanceInfos) {
            String instanceId = instanceInfo.getInstanceId();
            instancesCache.computeIfAbsent(instanceId, key -> new InstanceServiceMap<>(0L));
        }
    }

    public void doUnsubscribe(final URL url, final String appName, final String interfaceCls) {
        subscribedVipUrls.compute(appName, (key, val) -> {
            val.remove(url);
            if (val.isEmpty()) {
                return null;
            }
            return val;
        });
    }
}
