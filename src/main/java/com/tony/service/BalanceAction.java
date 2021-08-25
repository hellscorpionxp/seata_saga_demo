/*
 *  Copyright 1999-2019 Seata.io Group.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.tony.service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("balanceAction")
public class BalanceAction {

  private static final Logger LOGGER = LoggerFactory.getLogger(BalanceAction.class);
  private static final Random R = new Random();

  public boolean reduce(String businessKey, BigDecimal amount, Map<String, Object> params) {
    if ((params != null) && "true".equals(params.get("throwException"))) {
      throw new RuntimeException("reduce balance failed");
    }
    // 模拟异常回滚
    if (R.nextBoolean()) {
      throw new RuntimeException("This is mock Exception, HaHaHa!");
    }
    LOGGER.info("reduce balance succeed, amount: " + amount + ", businessKey:" + businessKey);
    return true;
  }

  public boolean compensateReduce(String businessKey, Map<String, Object> params) {
    if ((params != null) && "true".equals(params.get("throwException"))) {
      throw new RuntimeException("compensate reduce balance failed");
    }
    LOGGER.info("compensate reduce balance succeed, businessKey:" + businessKey);
    return true;
  }
}