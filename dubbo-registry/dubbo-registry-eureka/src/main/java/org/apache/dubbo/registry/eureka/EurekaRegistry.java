package org.apache.dubbo.registry.eureka;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.registry.NotifyListener;
import org.apache.dubbo.registry.support.FailbackRegistry;

/**
 * Created by dknight on 2018/6/20.
 */
public class EurekaRegistry extends FailbackRegistry{

    private  URL registryUrl;

    public EurekaRegistry(URL url) {
        super(url);
        if (url.isAnyHost()) {
            throw new IllegalStateException("registry address == null");
        }


        this.registryUrl = url;
    }

    @Override
    protected void doRegister(URL url) {

    }

    @Override
    protected void doUnregister(URL url) {

    }

    @Override
    protected void doSubscribe(URL url, NotifyListener listener) {

    }

    @Override
    protected void doUnsubscribe(URL url, NotifyListener listener) {

    }

    @Override
    public boolean isAvailable() {
        return false;
    }
}
