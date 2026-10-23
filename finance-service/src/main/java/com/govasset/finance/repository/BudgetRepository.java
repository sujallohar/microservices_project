package com.govasset.finance.repository;

import com.govasset.finance.model.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, String> {
    Optional<Budget> findByDepartmentAndFiscalYear(String department, String fiscalYear);
}
