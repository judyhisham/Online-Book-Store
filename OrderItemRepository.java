package com.example.repository;

import com.example.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository // 1. بنقول لـ Spring: "يا عم Spring، ده المسؤول عن الـ Database"
public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> { // 2. بنورث القوى الخارقة

    // 3. الميثود السحرية اللي بتجيب الداتا بمجرد اسمها
    List<OrderItem> findByOrderId(Integer orderId);
}
