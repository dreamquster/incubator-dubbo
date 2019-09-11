package org.apache.dubbo.rpc.protocol.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * 接口实现类，注解加在实现上
 *
 * @author dreamquster
 * @create 2019/9/11 10:11 AM
 */
@Path("/pure-service")
public class PureServiceImpl implements PureService{

    @GET
    @Path("hello")
    @Override
    public String hello(String name) {
        return "hello " + name;
    }
}
