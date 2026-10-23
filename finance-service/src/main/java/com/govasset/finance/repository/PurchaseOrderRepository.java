package com.govasset.finance.repository;

import com.govasset.finance.model.PurchaseOrder;
import com.govasset.finance.model.PurchaseOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, String> {
    Optional<PurchaseOrder> findByPoNumber(String poNumber);
    List<PurchaseOrder> findByDepartment(String department);
    List<PurchaseOrder> findByStatus(PurchaseOrderStatus status);
}
