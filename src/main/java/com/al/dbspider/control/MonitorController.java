package com.al.dbspider.control;

import com.al.dbspider.base.ExchangeConstant;
import com.al.dbspider.config.MarketQuatationSiteConfig;
import com.al.dbspider.monitor.ExchangeWebsocketManager;
import com.al.dbspider.websocket.BaseWebsocket;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 各种属性信息查询测试,可随意修改
 * TODO 可改为 spring boot endpoint 或spring boot admin
 *
 * @Version 1.0
 * @Since JDK1.8
 * @Author junxiaoyang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 08/01/2018 16:43
 */
@RestController
@RequestMapping(path = "/monitor")
@Slf4j
public class MonitorController {

    @Autowired
    ExchangeWebsocketManager websocketManager;

    @Autowired
    MarketQuatationSiteConfig marketQuatationSiteConfig;

    @Autowired(required = false)
    Map<String, BaseWebsocket> baseWebsockets;

    @GetMapping("/exchange/websocket/close")
    public List<String> closeWebsocket(HttpServletRequest request) {
        ArrayList<String> strings = Lists.newArrayList();
        for (BaseWebsocket baseWebsocket : baseWebsockets.values()) {
            if (baseWebsocket != null) {
                boolean close = baseWebsocket.close(1000, "监控接口主动关闭");
                String format = String.format("%s close %s reason %s,host+add %s", baseWebsocket.getClass().getSimpleName(), close, "监控接口主动关闭", request.getRemoteHost() + "-" + request.getRemoteAddr());
                log.info(format);
                strings.add(format);
                continue;
            }
            log.warn("basewebsocket is null");
        }
        return strings;
    }

    @GetMapping("/exchange/websocket/{name}/close")
    public List<String> closeWebsocket(@PathVariable("name") String name, HttpServletRequest request) {
        ArrayList<String> strings = Lists.newArrayList();
        BaseWebsocket baseWebsocket = baseWebsockets.get(name);
        if (baseWebsocket != null) {
            boolean close = baseWebsocket.close(1000, "监控接口主动关闭");
            String format = String.format("%s close %s reason %s,host+add %s", baseWebsocket.getClass().getSimpleName(), close, "监控接口主动关闭", request.getRemoteHost() + "-" + request.getRemoteAddr());
            log.info(format);
            strings.add(format);
        }
        log.warn("basewebsocket is null");
        return strings;
    }

    @GetMapping("/exchange/websocket/start")
    public List<String> startWebsocket(HttpServletRequest request) {
        ArrayList<String> strings = Lists.newArrayList();
        for (BaseWebsocket baseWebsocket : baseWebsockets.values()) {
            if (baseWebsocket != null) {
                boolean start = baseWebsocket.start();
                String format = String.format("%s start %s ,host+add %s", baseWebsocket.getClass().getSimpleName(), start, request.getRemoteHost() + "-" + request.getRemoteAddr());
                log.info(format);
                strings.add(format);
                continue;
            }
            log.warn("basewebsocket is null");
        }
        return strings;
    }

    @GetMapping("/exchange/websocket/{name}/start")
    public List<String> startWebsocket(@PathVariable("name") String name, HttpServletRequest request) {
        ArrayList<String> strings = Lists.newArrayList();
        BaseWebsocket baseWebsocket = baseWebsockets.get(name);
        if (baseWebsocket != null) {
            boolean start = baseWebsocket.start();
            String format = String.format("%s start %s ,host+add %s", baseWebsocket.getClass().getSimpleName(), start, request.getRemoteHost() + "-" + request.getRemoteAddr());
            log.info(format);
            strings.add(format);
        }
        return strings;
    }

    @GetMapping("/exchange/message/scale")
    public List<String> startWebsocket() {
        ArrayList<String> messageCounters = new ArrayList<>();
        Map<ExchangeConstant, MessageCounter> dataCounters = websocketManager.getDataCounter();
        AtomicLong total = new AtomicLong(0);
        AtomicLong aggtotal = new AtomicLong(0);
        dataCounters.forEach((exchangeConstant, messageCounter) -> {
            messageCounters.add(exchangeConstant + " " + messageCounter.toString());
            total.addAndGet(messageCounter.total());
            aggtotal.addAndGet(messageCounter.aggTotal());
        });
        messageCounters.add("total: " + total + " , " + aggtotal + " , " + (aggtotal.get() == 0 ? "0" : new BigDecimal(total.get()).divide(new BigDecimal(aggtotal.get()), 2, RoundingMode.DOWN)));
        return messageCounters;
    }

    @GetMapping("/status/exchanges")
    public ExchangeStatusVO exchangestatus() {
        ExchangeStatusVO exchangeStatusVO = new ExchangeStatusVO();
        Map<ExchangeConstant, ExchangeStatus> status = websocketManager.getStatus();
        Map<ExchangeConstant, MessageCounter> dataCounters = websocketManager.getDataCounter();
        status.forEach((exchangeConstant, exchangeStatus) -> {
            exchangeStatus.setLastTime(dataCounters.get(exchangeConstant).getLastTime());
        });
        exchangeStatusVO.setStatus(status);
        return exchangeStatusVO;
    }

    @GetMapping("/status/exchanges/{name}")
    public ExchangeStatusVO exchangestatus(@PathVariable(name = "name") String exchangeName) {
        ExchangeConstant exchangeConstant;
        try {
            exchangeConstant = ExchangeConstant.valueOf(exchangeName);
        } catch (IllegalArgumentException e) {
            return null;
        }
        ExchangeStatusVO exchangeStatusVO = new ExchangeStatusVO();
        ExchangeStatus exchangeStatus = websocketManager.getStatus().get(exchangeConstant);
        exchangeStatus.setLastTime(websocketManager.getDataCounter().get(exchangeConstant).getLastTime());
        exchangeStatusVO.addStatus(exchangeConstant, exchangeStatus);
        return exchangeStatusVO;
    }

}
