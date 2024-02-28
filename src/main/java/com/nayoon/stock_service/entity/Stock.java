package com.nayoon.stock_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "stock")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Stock {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "stock_id")
  private Long id;

  @Column(name = "product_id", updatable = false)
  private Long productId;

  @Column(name = "initial_stock", nullable = false)
  private Integer initialStock;

  @Builder
  public Stock(Long productId, Integer initialStock) {
    this.productId = productId;
    this.initialStock = initialStock;
  }

  public void updateInitialStock(Integer initialStock) {
    this.initialStock = initialStock;
  }

}