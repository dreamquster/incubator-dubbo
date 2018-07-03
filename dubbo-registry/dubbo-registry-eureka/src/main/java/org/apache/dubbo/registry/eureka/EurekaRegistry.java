package org.apache.dubbo.registry.eureka;

import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.EurekaInstanceConfig;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.appinfo.MyDataCenterInstanceConfig;
import com.netflix.appinfo.providers.EurekaConfigBasedInstanceInfoProvider;
import com.netflix.discovery.DefaultEurekaClientConfig;
import com.netflix.discovery.DiscoveryClient;
import com.netflix.discovery.EurekaClientConfig;
import com.netflix.eureka.EurekaServerConfig;
import com.netflix.eureka.registry.PeerAwareInstanceRegistryImpl;
import org.apache.dubbo.common.Constants;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.registry.NotifyListener;
import org.apache.dubbo.registry.support.FailbackRegistry;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dknight on 2018/6/20.
 */
public class EurekaRegistry extends FailbackRegistry{

    public static final String EUREKA_SERVICE_URL = "serviceUrl";

    public static final String ZONE_SEP = ";";
    public static final String URL_SEP = ",";

    public static final String DEFAULT_EUREKA_REG_URL = "http://%s:%d/eureka";

    private  URL registryUrl;

    private PeerAwareInstanceRegistryImpl peerAwareInstanceRegistry;

    private final DiscoveryClient discoveryClient;

    public EurekaRegistry(URL url) {
        super(url);
        if (url.isAnyHost()) {
            throw new IllegalStateException("registry address == null");
        }


        String applicationName = url.getParameter(Constants.APPLICATION_KEY);
        String group = url.getParameter(Constants.GROUP_KEY, "dubbo");

        String defaultServiceUrl = String.format(DEFAULT_EUREKA_REG_URL, url.getHost(), url.getPort());
        this.registryUrl = url;
        EurekaClientConfiguration clientConfig =  new EurekaClientConfiguration();
        clientConfig.setEurekaServerServiceUrls(EurekaClientConfiguration.DEFAULT_ZONE, Arrays.asList(defaultServiceUrl));
        Map<String, String> metadata = new HashMap<>();
        metadata.put("package.servicename", "serviceUrl");
        metadata.put("dubbo-registry", url.toFullString());
        DubboIntanceConfig eurekaInstanceConfig = new DubboIntanceConfig();
        eurekaInstanceConfig.setAppname(applicationName);
        eurekaInstanceConfig.setVirtualHostName(applicationName);
        eurekaInstanceConfig.setAppGroupName(group);
        eurekaInstanceConfig.setMetadataMap(metadata);



        InstanceInfo instanceInfo = new EurekaConfigBasedInstanceInfoProvider(eurekaInstanceConfig).get();
        ApplicationInfoManager applicationInfoManager = new ApplicationInfoManager(eurekaInstanceConfig, instanceInfo);
        discoveryClient = new DiscoveryClient(applicationInfoManager, clientConfig);

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
        logger.info("todo:doRegister from FailbackRegistry");
    }

    @Override
    protected void doUnregister(URL url) {
        logger.info("todo:doUnregister from FailbackRegistry");
    }

    @Override
    protected void doSubscribe(URL url, NotifyListener listener) {
        logger.info("configurator listener");
    }

    @Override
    protected void doUnsubscribe(URL url, NotifyListener listener) {

    }

    @Override
    public boolean isAvailable() {
        return false;
    }
}
