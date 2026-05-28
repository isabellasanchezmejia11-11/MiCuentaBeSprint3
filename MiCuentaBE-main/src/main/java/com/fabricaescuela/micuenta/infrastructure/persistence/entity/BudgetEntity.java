package com.fabricaescuela.micuenta.infrastructure.persistence.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;


@Entity
@Table(name = "budgets", uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_budgets_user_category_month_year",
                columnNames = { "user_id", "category_id", "month_value", "year_value" }
        )
})
public class BudgetEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "amount_limit", nullable = false, precision = 15, scale = 2)
    private BigDecimal amountLimit;

    @Column(name = "alert_percent")
    private Integer alertPercent;

   
    @Column(name = "month_value", nullable = false)
    private Integer month;

    
    @Column(name = "year_value", nullable = false)
    private Integer year;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    public BudgetEntity() {
    }

    public BudgetEntity(Long id, BigDecimal amountLimit, Integer alertPercent, Integer month, Integer year, Long categoryId, Long userId) {
        this.id = id;
        this.amountLimit = amountLimit;
        this.alertPercent = alertPercent;
        this.month = month;
        this.year = year;
        this.categoryId = categoryId;
        this.userId = userId;
    }

    // GETTERS
    public Long getId() {
        return id;
    }

    public BigDecimal getAmountLimit() {
        return amountLimit;
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

    public Integer getYear() {
        return year;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public Long getUserId() {
        return userId;
    }

    // SETTERS
    public void setId(Long id) {
        this.id = id;
    }

    public void setAmountLimit(BigDecimal amountLimit) {
        this.amountLimit = amountLimit;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}