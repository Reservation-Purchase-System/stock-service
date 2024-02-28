package com.nayoon.stock_service.service;

import com.nayoon.stock_service.client.PurchaseClient;
import com.nayoon.stock_service.common.exception.CustomException;
import com.nayoon.stock_service.common.exception.ErrorCode;
import com.nayoon.stock_service.entity.Stock;
import com.nayoon.stock_service.repository.StockRepository;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {

  private final StockRepository stockRepository;
  private final PurchaseClient purchaseClient;
  private final RedisService redisService;

  @Transactional
  public Integer createOrUpdate(Long productId, Integer newInitialStock) {
    boolean exists = stockRepository.existsByProductId(productId);

    if (exists) {
      return update(productId, newInitialStock);
    } else {
      Stock stockEntity = Stock.builder()
          .productId(productId)
          .initialStock(newInitialStock)
          .build();
      stockRepository.save(stockEntity);

      redisService.setValue(productId, newInitialStock, Duration.ofMinutes(5));
      return stockEntity.getInitialStock();
    }
  }

  private Integer update(Long productId, Integer newInitialStock) {
    Integer newStock = calculateStock(productId, newInitialStock);
    if (newInitialStock <= newStock) {
      throw new CustomException(ErrorCode.INVALID_NEW_STOCK);
    }

    Stock stockEntity = loadStock(productId);
    stockEntity.updateInitialStock(newInitialStock);
    stockRepository.save(stockEntity);

    redisService.setValue(productId, newStock, Duration.ofMinutes(5));
    return newStock;
  }

  @Transactional
  @Synchronized
  public void increaseStock(Long productId, Integer quantity) {
    Stock stockEntity = loadStock(productId);
    Integer currStock = getStock(productId);

    if (stockEntity.getInitialStock() < currStock + quantity) {
      throw new CustomException(ErrorCode.LIMIT_OF_STOCK);
    }
  }

  @Transactional
  @Synchronized
  public void decreaseStock(Long productId, Integer quantity) {
    if (getStock(productId) < quantity) {
      throw new CustomException(ErrorCode.OUT_OF_STOCK);
    }

    redisService.decrease(productId, quantity);
  }

  private Stock loadStock(Long productId) {
    return stockRepository.findByProductId(productId)
        .orElseThrow(() -> new CustomException(ErrorCode.STOCK_NOT_FOUND));
  }

  public Integer getStock(Long productId) {
    if (redisService.keyExists(productId)) {
      return redisService.getValue(productId);
    }

    Stock stockEntity = loadStock(productId);
    Integer stock = calculateStock(productId, stockEntity.getInitialStock());
    redisService.setValue(productId, stock, Duration.ofMinutes(5));
    return stock;
  }

  private Integer calculateStock(Long productId, Integer newInitialStock) {
    // purchase_service에 결제 프로세스 진입한 주문들의 quantity 합 요청
    Integer quantitySum = purchaseClient.getQuantitySumByProductId(productId);

    if (newInitialStock < quantitySum) {
      throw new CustomException(ErrorCode.INVALID_NEW_STOCK);
    }

    return newInitialStock - quantitySum;
  }

}
