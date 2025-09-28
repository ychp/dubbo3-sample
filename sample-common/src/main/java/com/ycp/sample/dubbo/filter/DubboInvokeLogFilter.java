package com.ycp.sample.dubbo.filter;

import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;

import static org.apache.dubbo.common.constants.CommonConstants.CONSUMER_SIDE;
import static org.apache.dubbo.common.constants.CommonConstants.PROVIDER_SIDE;


/**
 * dubbo服务的日志输出
 *
 * @author yingchengpeng
 */
@Slf4j
@Activate(group = {CONSUMER_SIDE, PROVIDER_SIDE})
public class DubboInvokeLogFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        String className = invoker.getInterface().getCanonicalName();
        String methodName = invocation.getMethodName();
        Object[] params = invocation.getArguments();
        Result result;
        long startTime = System.currentTimeMillis();
        try {
            result = invoker.invoke(invocation);
            long costTime = System.currentTimeMillis() - startTime;
            log.info("调用外部DUBBO接口，执行完成 {}#{}， 耗时 {} ms, params: {}, result:{}",
                    className, methodName, costTime, JSONObject.toJSONString(params), result == null ? "" : JSONObject.toJSONString(result.getValue()));
        } catch (Exception e) {
            log.error("调用外部DUBBO接口，执行异常 {}#{}, params: {}, case ", className, methodName, JSONObject.toJSONString(params), e);
            throw e;
        }
        return result;
    }

}
