package com.hcl.rims.repo;

import com.hcl.rims.model.OrderLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderLineRepository extends JpaRepository<OrderLine, Long> {
    List<OrderLine> findBySalesOrderId(Long salesOrderId);
}
