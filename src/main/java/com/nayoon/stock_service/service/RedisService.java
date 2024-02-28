package com.nayoon.stock_service.service;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisService {

  private static final String PREFIX = "stock:product:";
  private final RedisTemplate<String, Integer> redisTemplate;

  public void setValue(Long productId, Integer stock, Duration expiration) {
    String key = getKey(productId);
    redisTemplate.opsForValue().set(key, stock, expiration);
  }

  public Long increase(Long productId, Integer stock) {
    String key = getKey(productId);
    return redisTemplate.opsForValue().increment(key, stock);
  }

  public Long decrease(Long productId, Integer stock) {
    String key = getKey(productId);
    return redisTemplate.opsForValue().decrement(key, stock);
  }

  public Integer getValue(Long productId) {
    String key = getKey(productId);
    return redisTemplate.opsForValue().get(key);
  }

  public boolean keyExists(Long productId) {
    String key = getKey(productId);
    return Boolean.TRUE.equals(redisTemplate.hasKey(key));
  }

  private String getKey(Long productId) {
    return PREFIX + productId;
  }

}
