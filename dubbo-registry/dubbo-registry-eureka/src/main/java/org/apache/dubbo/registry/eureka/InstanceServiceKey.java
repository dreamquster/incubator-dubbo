package org.apache.dubbo.registry.eureka;

public class InstanceServiceKey implements Comparable<InstanceServiceKey> {
    private final String vipAddress;

    private final String instanceId;

    private final String serviceConfigName;

    private final String compareKey;

    private Long lastUpdatedTime;

    public InstanceServiceKey(String vipAddress, String instanceId, String serviceConfigName) {
        this.vipAddress = vipAddress;
        this.instanceId = instanceId;
        this.serviceConfigName = serviceConfigName;
        this.compareKey =  vipAddress + instanceId + serviceConfigName;
    }

    public String getVipAddress() {
        return vipAddress;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public Long getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    public String getServiceConfigName() {
        return serviceConfigName;
    }

    public String getCompareKey() {
        return compareKey;
    }

    @Override
    public int compareTo(InstanceServiceKey o) {
        return this.getCompareKey().compareTo(o.getCompareKey());
    }
}
