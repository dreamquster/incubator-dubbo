/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.dubbo.config.spring;

import org.apache.dubbo.config.annotation.Service;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.framework.DefaultAopProxyFactory;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.mockito.Mockito.mock;

public class ServiceBeanTest {
    @Test
    public void testGetService() {
        TestService service = mock(TestService.class);
        ServiceBean serviceBean = new ServiceBean(service);

        Service beanService = serviceBean.getService();
        MatcherAssert.assertThat(beanService, not(nullValue()));
    }

    abstract class TestService implements Service {

    }
    interface DemoService {

    }
    class DemoServiceImpl implements DemoService {

    }

    @Test
    public void getServiceClass() {
        AdvisedSupport advisedSupport = new AdvisedSupport();
        advisedSupport.setInterfaces(DemoService.class);
        advisedSupport.setTargetClass(DemoServiceImpl.class);
        DefaultAopProxyFactory aopProxyFactory = new DefaultAopProxyFactory();
        DemoService demoService = (DemoService)aopProxyFactory.createAopProxy(advisedSupport).getProxy();
        ServiceBean serviceBean = new ServiceBean();
        Assert.assertEquals(DemoServiceImpl.class, serviceBean.getServiceClass(demoService));
    }
}