package com.tony.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import io.seata.saga.engine.AsyncCallback;
import io.seata.saga.engine.StateMachineEngine;
import io.seata.saga.proctrl.ProcessContext;
import io.seata.saga.statelang.domain.ExecutionStatus;
import io.seata.saga.statelang.domain.StateMachineInstance;

@RestController
public class SagaController {

  private static final Logger LOGGER = LoggerFactory.getLogger(SagaController.class);
  private static final String SUCCESS = "SUCCESS";
  private static final String FAIL = "FAIL";
  @Autowired
  private StateMachineEngine stateMachineEngine;
  private static volatile Object lock = new Object();
  private static AsyncCallback CALL_BACK = new AsyncCallback() {
    @Override
    public void onFinished(ProcessContext context, StateMachineInstance stateMachineInstance) {
      synchronized (lock) {
        lock.notifyAll();
      }
    }

    @Override
    public void onError(ProcessContext context, StateMachineInstance stateMachineInstance, Exception exp) {
      synchronized (lock) {
        lock.notifyAll();
      }
    }
  };

  private static void waittingForFinish(StateMachineInstance inst) {
    synchronized (lock) {
      if (ExecutionStatus.RU.equals(inst.getStatus())) {
        try {
          lock.wait();
        } catch (InterruptedException e) {
          LOGGER.error(e.getMessage(), e);
        }
      }
    }
  }

  @PostMapping(value = "/create", produces = "application/json")
  public String create(int count, BigDecimal amount) {
    String businessKey = String.valueOf(System.currentTimeMillis());
    Map<String, Object> startParams = new HashMap<>(3);
    startParams.put("businessKey", businessKey);
    startParams.put("count", count);
    startParams.put("amount", amount);
    // 同步执行
//    StateMachineInstance inst = stateMachineEngine.startWithBusinessKey("reduceInventoryAndBalance", null, businessKey,
//        startParams);
    // 异步执行
    businessKey = String.valueOf(System.currentTimeMillis());
    StateMachineInstance inst = stateMachineEngine.startWithBusinessKeyAsync("reduceInventoryAndBalance", null,
        businessKey, startParams, CALL_BACK);
    waittingForFinish(inst);
    if (ExecutionStatus.SU.equals(inst.getStatus())) {
      LOGGER.info("订单创建成功！saga transaction succeed, XID: {}", inst.getId());
      return SUCCESS;
    }
    LOGGER.info("订单创建失败！saga transaction failed, XID: {}", inst.getId());
    return FAIL;
  }

}
