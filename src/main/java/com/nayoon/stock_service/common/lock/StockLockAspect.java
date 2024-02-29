package com.nayoon.stock_service.common.lock;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class StockLockAspect {

  private final RedissonClient redissonClient;

  @Around("@annotation(com.nayoon.stock_service.common.lock.StockLock)")
  public Object applyStockLock(ProceedingJoinPoint joinPoint) throws Throwable {
    Long productId = (Long) joinPoint.getArgs()[0];

    RLock lock = redissonClient.getLock(String.format("stock:productId:%d", productId));
    try {
      boolean available = lock.tryLock(100, 10, TimeUnit.SECONDS);
      if (!available) {
        throw new RuntimeException("Failed to acquire lock for product: " + productId);
      }
      return joinPoint.proceed();
    } finally {
      lock.unlock();
    }
  }

}
