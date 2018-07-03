package org.apache.dubbo.registry.eureka;

import com.netflix.appinfo.PropertiesInstanceConfig;

import java.util.HashMap;
import java.util.Map;
/**

 * Created by dknight on 2018/6/30.
 */
public class DubboIntanceConfig extends PropertiesInstanceConfig {

    private Map<String, String> metadataMap = new HashMap<>();

    private String appGroupName;

    private String appname;

    private String virtualHostName;

    public void setMetadataMap(Map<String, String> metadataMap) {
        this.metadataMap = metadataMap;
    }

    @Override
    public Map<String, String> getMetadataMap() {
        return metadataMap;
    }


    public void setAppname(String appname) {
        this.appname = appname;
    }

    @Override
    public String getAppname() {
        return this.appname;
    }

    public void setVirtualHostName(String virtualHostName) {
        this.virtualHostName = virtualHostName;
    }

    @Override
    public String getVirtualHostName() {
        return virtualHostName;
    }

    public void setAppGroupName(String appGroupName) {
        this.appGroupName = appGroupName;
    }

    @Override
    public String getAppGroupName() {
        return appGroupName;
    }
}
