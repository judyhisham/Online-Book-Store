package com.example.repository;

import com.example.model.Order;
import com.example.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    //بنقول للكود دا المس}ل عن database
    //دي عشان user لما يطلباردير ما يتلخبطش في الكود
    List<Order> findByUserIdOrderByCreatedAtDesc(Integer userId);
    //ده اختراع بيخلينا نقسم الداتا لصفحات
    Page<Order> findAll(Pageable pageable);
    //بتجيب الاوردرات المميزه
    List<Order> findByStatus(OrderStatus status);
    //بترجع رقم (Long) بيقولك فيه كام أوردر في حالة معينة.
    long countByStatus(OrderStatus status);
}
