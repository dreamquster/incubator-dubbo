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
import org.apache.dubbo.common.utils.UrlUtils;
import org.apache.dubbo.registry.NotifyListener;
import org.apache.dubbo.registry.support.FailbackRegistry;
import org.apache.dubbo.rpc.RpcException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

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

    private final boolean isUseSecure = false;

    private final EurekaClientCacheWrapper eurekaClientCacheWrapper;

    private final EurekaEventListener cacheRefreshListener = new EurekaEventListener() {
        @Override
        public void onEvent(EurekaEvent event) {
            if (event instanceof CacheRefreshedEvent) {
                List<URL> changedUrls = eurekaClientCacheWrapper.collectChangedUrls();
                doNotify(changedUrls);
            } else if (event instanceof StatusChangeEvent) {

            }
        }
    };



    private void doNotify(List<URL> changedUrls) {
        if (changedUrls.isEmpty()) {
            return;
        }
        for (Map.Entry<URL, Set<NotifyListener>> entry : new HashMap<URL, Set<NotifyListener>>(getSubscribed()).entrySet()) {
            URL consumerUrl = entry.getKey();
            List<URL> changedProviderUrls = new LinkedList<>();
            for (URL providerUrl : changedUrls) {
                if (UrlUtils.isMatch(consumerUrl, providerUrl)) {
                    changedProviderUrls.add(providerUrl);
                }
            }
            if (!changedProviderUrls.isEmpty()) {
                for (NotifyListener listener: entry.getValue()) {
                    doNotify(consumerUrl, listener , changedProviderUrls);
                }
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
        eurekaClientCacheWrapper = new EurekaClientCacheWrapper(discoveryClient);

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
        if (null == url.getParameter(Constants.TIMESTAMP_KEY)) {
            url.addParameter(Constants.TIMESTAMP_KEY, System.currentTimeMillis());
        }
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
        String appName = url.getParameter(Constants.PROVIDER_APP_KEY);
        String interfaceCls = url.getServiceInterface();
        if (null == appName && Constants.PROVIDER.equals(url.getProtocol())) {
            appName = registryUrl.getParameter(Constants.APPLICATION_KEY);
        }

        if (null == appName) {
            throw new RpcException(
                    String.format("applicaton can not be null when consumer " +
                            "subscribe a service[%s] from eureka!", interfaceCls));
        }

        eurekaClientCacheWrapper.doSubscribe(url, appName, interfaceCls);
        List<URL> changedUrls = eurekaClientCacheWrapper.collectChangedUrls(true);
        doNotify(changedUrls);
    }

    @Override
    protected void doUnsubscribe(URL url, NotifyListener listener) {
        String appName = url.getParameter(Constants.PROVIDER_APP_KEY);
        String interfaceCls = url.getServiceInterface();
        if (null == appName) {
            throw new RpcException(
                    String.format("applicaton can not be null when consumer " +
                            "unsubscribe a service[%s] from eureka!", interfaceCls));
        }
        eurekaClientCacheWrapper.doUnsubscribe(url, appName, interfaceCls);
    }
    // todo: judge whether eureka is alive
    @Override
    public boolean isAvailable() {
        return discoveryClient.getLastSuccessfulRegistryFetchTimePeriod() < MAX_FETCH_PERIOD;
    }

}
