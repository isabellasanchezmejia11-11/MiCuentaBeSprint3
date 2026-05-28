package com.fabricaescuela.micuenta.domain.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;

public class Budget {

    private Long id;
    private BigDecimal amountLimit;
    private Integer alertPercent;
    private Integer month;
    private Integer year;
    private Long categoryId;
    private Long userId;

    public Budget() {}

    public Budget(Long id, BigDecimal amountLimit, Integer alertPercent, Integer month, Integer year, Long categoryId, Long userId) {
        this.id = id;
        this.amountLimit = amountLimit;
        this.alertPercent = alertPercent;
        this.month = month;
        this.year = year;
        this.categoryId = categoryId;
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getAmountLimit() {
        return amountLimit;
    }

    public void setAmountLimit(BigDecimal amountLimit) {
        this.amountLimit = amountLimit;
    }

    public Integer getAlertPercent() {
        return alertPercent;
    }

    public void setAlertPercent(Integer alertPercent) {
        this.alertPercent = alertPercent;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
