package com.aceli.mock.repository;

import com.aceli.mock.domain.Country;
import com.aceli.mock.domain.CountryLimit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CountryLimitRepository extends JpaRepository<CountryLimit, Country> {
}
