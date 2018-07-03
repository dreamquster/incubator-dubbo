package org.apache.dubbo.registry.eureka;

import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.EurekaInstanceConfig;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.appinfo.MyDataCenterInstanceConfig;
import com.netflix.appinfo.providers.EurekaConfigBasedInstanceInfoProvider;
import com.netflix.discovery.DefaultEurekaClientConfig;
import com.netflix.discovery.DiscoveryClient;
import com.netflix.discovery.EurekaClientConfig;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by dknight on 2018/6/24.
 */
public class EurekaClientTest {

    private DiscoveryClient discoveryClient;

    @Before
    public void setUp() {
        EurekaClientConfig clientConfig =  new DefaultEurekaClientConfig();
        EurekaInstanceConfig eurekaInstanceConfig = new MyDataCenterInstanceConfig();

        InstanceInfo instanceInfo = new EurekaConfigBasedInstanceInfoProvider(eurekaInstanceConfig).get();
        ApplicationInfoManager applicationInfoManager = new ApplicationInfoManager(eurekaInstanceConfig, instanceInfo);
        discoveryClient = new DiscoveryClient(applicationInfoManager, clientConfig);
    }

    @Test
    public void regexQueryServicesTest() {
        discoveryClient.getApplications();
    }

}
