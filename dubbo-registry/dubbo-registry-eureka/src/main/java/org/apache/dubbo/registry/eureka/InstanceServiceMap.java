package org.apache.dubbo.registry.eureka;

import java.util.concurrent.ConcurrentHashMap;

public class InstanceServiceMap<K, V> extends ConcurrentHashMap<K, V> {
    private volatile long lastUpdatedTime;

    public InstanceServiceMap(long lastUpdatedTime) {
        super();
        this.lastUpdatedTime = lastUpdatedTime;
    }

    public long getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    public void setLastUpdatedTime(long lastUpdatedTime) {
        this.lastUpdatedTime = lastUpdatedTime;
    }
}
