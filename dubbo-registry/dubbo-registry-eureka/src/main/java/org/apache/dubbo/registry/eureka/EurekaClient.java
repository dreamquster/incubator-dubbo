package org.apache.dubbo.registry.eureka;

import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.discovery.AbstractDiscoveryClientOptionalArgs;
import com.netflix.discovery.DiscoveryClient;
import com.netflix.discovery.EurekaClientConfig;

public class EurekaClient extends DiscoveryClient {
    public EurekaClient(ApplicationInfoManager applicationInfoManager, EurekaClientConfig config) {
        super(applicationInfoManager, config);
    }

    public EurekaClient(ApplicationInfoManager applicationInfoManager, EurekaClientConfig config, AbstractDiscoveryClientOptionalArgs args) {
        super(applicationInfoManager, config, args);
    }


}
