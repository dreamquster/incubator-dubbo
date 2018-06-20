package org.apache.dubbo.registry.eureka;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.registry.Registry;
import org.apache.dubbo.registry.support.AbstractRegistryFactory;

/**
 * Created by dknight on 2018/6/20.
 */
public class EurekaRegistryFactory extends AbstractRegistryFactory {
    @Override
    protected Registry createRegistry(URL url) {
        return new EurekaRegistry(url);
    }
}
