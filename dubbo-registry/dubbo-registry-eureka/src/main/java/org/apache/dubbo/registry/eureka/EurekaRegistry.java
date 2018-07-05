package org.apache.dubbo.registry.eureka;

import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.appinfo.providers.EurekaConfigBasedInstanceInfoProvider;
import com.netflix.discovery.CacheRefreshedEvent;
import com.netflix.discovery.DiscoveryClient;
import com.netflix.discovery.EurekaEvent;
import com.netflix.discovery.EurekaEventListener;
import com.netflix.discovery.StatusChangeEvent;
import org.apache.dubbo.common.Constants;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.utils.ConcurrentHashSet;
import org.apache.dubbo.common.utils.UrlUtils;
import org.apache.dubbo.registry.NotifyListener;
import org.apache.dubbo.registry.support.FailbackRegistry;
import org.apache.dubbo.rpc.RpcException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Created by dknight on 2018/6/20.
 */
public class EurekaRegistry extends FailbackRegistry{

    public static final String EUREKA_SERVICE_URL = "serviceUrl";

    public static final String ZONE_SEP = ";";
    public static final String URL_SEP = ",";

    public static final String DEFAULT_EUREKA_REG_URL = "http://%s:%d/eureka";

    public static final long MAX_FETCH_PERIOD = 1000*60*3L; // 3 mintues = twice lease period

    private  URL registryUrl;

    private final DiscoveryClient discoveryClient;

    private final ApplicationInfoManager applicationInfoManager;

    private final Set<String> subscribedVipName = new ConcurrentHashSet<>();

    private final Map<String, Long> servicesCache = new ConcurrentHashMap<>();

    private final Map<String, InstanceServiceMap<String, Long>> instancesCache = new ConcurrentHashMap<>();

    private final Map<InstanceServiceKey, InstanceServiceMap<String, Long>> instanceServicesCache = new ConcurrentHashMap<>();

    private final boolean isUseSecure = false;

    private final EurekaEventListener cacheRefreshListener = new EurekaEventListener() {
        @Override
        public void onEvent(EurekaEvent event) {
            if (event instanceof CacheRefreshedEvent) {
                List<URL> changedUrls = collectChangedUrls();
                doNotify(changedUrls);
            } else if (event instanceof StatusChangeEvent) {

            }
        }
    };

    private final List<URL> collectChangedUrls() {
        List<URL> changedUrls = new LinkedList<>();
//        List<String> subscribedVipName = instanceServicesCache.keySet().stream().
//                map(s -> s.getVipAddress()).collect(Collectors.toList());
        for (String vipAddress: subscribedVipName) {
            List<InstanceInfo> instanceInfos =  discoveryClient.getInstancesByVipAddress(vipAddress, isUseSecure);
            for (InstanceInfo instanceInfo : instanceInfos) {
                String instanceId = instanceInfo.getInstanceId();
                InstanceServiceMap<String, Long> serviceMap = instancesCache.get(instanceId);
                if (null == serviceMap) {
                    instancesCache.putIfAbsent(instanceId, new InstanceServiceMap<>(0L));
                    serviceMap = instancesCache.get(instanceId);
                }
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

    private void doNotify(List<URL> changedUrls) {
        for (Map.Entry<URL, Set<NotifyListener>> entry : new HashMap<URL, Set<NotifyListener>>(getSubscribed()).entrySet()) {
            URL consumerUrl = entry.getKey();
            List<URL> changedProviderUrls = new LinkedList<>();
            for (URL providerUrl : changedUrls) {
                if (UrlUtils.isMatch(consumerUrl, providerUrl)) {
                    changedProviderUrls.add(providerUrl);
                }
            }
            for (NotifyListener listener: entry.getValue()) {
                doNotify(consumerUrl, listener , changedUrls);
            }
        }
    }

    public EurekaRegistry(URL url) {
        super(url);

        String applicationName = url.getParameter(Constants.APPLICATION_KEY);
        String group = url.getParameter(Constants.GROUP_KEY, "dubbo");

        String defaultServiceUrl = String.format(DEFAULT_EUREKA_REG_URL, url.getHost(), url.getPort());
        this.registryUrl = url;
        EurekaClientConfiguration clientConfig =  new EurekaClientConfiguration();
        clientConfig.setEurekaServerServiceUrls(EurekaClientConfiguration.DEFAULT_ZONE, Arrays.asList(defaultServiceUrl));
        Map<String, String> metadata = new HashMap<>();
        metadata.put("dubbo-registry", url.toString());
        DubboIntanceConfig eurekaInstanceConfig = new DubboIntanceConfig();
        eurekaInstanceConfig.setAppname(applicationName);
        eurekaInstanceConfig.setVirtualHostName(applicationName);
        eurekaInstanceConfig.setAppGroupName(group);
        eurekaInstanceConfig.setMetadataMap(metadata);


        InstanceInfo instanceInfo = new EurekaConfigBasedInstanceInfoProvider(eurekaInstanceConfig).get();
        applicationInfoManager = new ApplicationInfoManager(eurekaInstanceConfig, instanceInfo);
        discoveryClient = new DiscoveryClient(applicationInfoManager, clientConfig);
        discoveryClient.registerEventListener(cacheRefreshListener);

    }

    private void generateServiceUrls(URL url) {
        String serviceUrlMap = url.getParameter(EUREKA_SERVICE_URL);
        String[] zoneUrls = serviceUrlMap.split(ZONE_SEP);
        for(String zoneUrl : zoneUrls) {
            zoneUrl.indexOf('=');
            String[] eurekaRegistryUrls = zoneUrl.split(URL_SEP);
        }
    }

    @Override
    protected void doRegister(URL url) {
        String key = toCategoryPath(url);
        Map<String, String> metadata = applicationInfoManager.getInfo().getMetadata();
        metadata.put(key, url.toString());
    }

    private String toCategoryPath(URL url) {
        String interfaceCls = url.getServiceInterface();
        String category = url.getParameter(Constants.CONFIGURATORS_CATEGORY, Constants.DEFAULT_CATEGORY);
        return interfaceCls + Constants.PATH_SEPARATOR + category;
    }

    @Override
    protected void doUnregister(URL url) {
        String key = toCategoryPath(url);
        Map<String, String> metadata = applicationInfoManager.getInfo().getMetadata();
        metadata.remove(key);
    }

    @Override
    protected void doSubscribe(URL url, NotifyListener listener) {
        logger.info("configurator listener");
        String appName = url.getParameter(Constants.APPLICATION_KEY);
        String interfaceCls = url.getServiceInterface();
        if (null == appName) {
            throw new RpcException(
                    String.format("applicaton can not be null when consumer " +
                            "subscribe a service[%s] from eureka!", interfaceCls));
        }
        List<String> appInstanceIds = new LinkedList<>();
        if (!subscribedVipName.contains(appName)) {
            subscribedVipName.add(appName);

        }
        String[] categories = url.getParameter(Constants.CONFIGURATORS_CATEGORY, new String[0]);
        List<InstanceInfo> instanceInfos = discoveryClient.getInstancesByVipAddress(appName, isUseSecure);
        for (InstanceInfo instanceInfo: instanceInfos) {
            String instanceId = instanceInfo.getInstanceId();
            appInstanceIds.add(instanceId);
            instancesCache.putIfAbsent(instanceId, new InstanceServiceMap<>(0L));
            InstanceServiceMap<String, Long> instanceServiceMap = instancesCache.get(instanceId);
            for (String category: categories) {
                instanceServiceMap.putIfAbsent(interfaceCls + Constants.PATH_SEPARATOR + category, 0L);
            }
        }



    }

    @Override
    protected void doUnsubscribe(URL url, NotifyListener listener) {

    }
    // todo: judge whether eureka is alive
    @Override
    public boolean isAvailable() {
        return discoveryClient.getLastSuccessfulRegistryFetchTimePeriod() < MAX_FETCH_PERIOD;
    }

}
