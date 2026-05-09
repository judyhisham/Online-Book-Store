package com.example.repository;

import com.example.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository // 1. بنعرف Spring إن ده المسئول عن الداتا بيز (أمين المخزن)
public interface PaymentRepository extends JpaRepository<Payment, Integer> { // 2. ورثنا كل العمليات الجاهزة

    // 3. ميثود بتدور على الدفع باستخدام رقم الأوردر
    Optional<Payment> findByOrderId(Integer orderId);
}
