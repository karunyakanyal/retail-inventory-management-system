package com.hcl.rims.repo;

import com.hcl.rims.model.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {
    Optional<SalesOrder> findByOrderNumber(String orderNumber);
    List<SalesOrder> findByStatus(SalesOrder.OrderStatus status);
    List<SalesOrder> findByCustomerId(Long customerId);
}
