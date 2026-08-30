package com.aceli.mock.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "country_limits")
public class CountryLimit {
    @Id
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Country country;

    @Column(name = "max_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal maxAmount;

    protected CountryLimit() {
    }

    public CountryLimit(Country country, BigDecimal maxAmount) {
        this.country = country;
        this.maxAmount = maxAmount;
    }

    public Country getCountry() { return country; }
    public BigDecimal getMaxAmount() { return maxAmount; }
}
