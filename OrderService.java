package com.example.service;

import com.example.model.*;
import com.example.repository.*;
import com.example.util.Constants;
import com.example.util.EmailUtil;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final CartItemRepository cartItemRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final EmailUtil emailUtil;

    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        PaymentRepository paymentRepository,
                        CartItemRepository cartItemRepository,
                        BookRepository bookRepository,
                        UserRepository userRepository,
                        EmailUtil emailUtil) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.paymentRepository = paymentRepository;
        this.cartItemRepository = cartItemRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.emailUtil = emailUtil;
    }

    @Transactional

    public Order checkout(Integer userId) {
        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        //التاكد من الcsrt is empty
        if (cartItems.isEmpty()) {
            throw new IllegalStateException("Your cart is empty.");
        }


        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (CartItem ci : cartItems) {
            Book book = ci.getBook();
            if (book.getQuantity() < ci.getQuantity()) {//تاكد من المخزون
                throw new IllegalStateException("Insufficient stock for: " + book.getTitle());
            }

            OrderItem oi = new OrderItem();
            oi.setBook(book);
            oi.setQuantity(ci.getQuantity());
            oi.setUnitPrice(book.getPrice());
            //حساب الفلوس
            subtotal = subtotal.add(book.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())));
            orderItems.add(oi);
        }

        BigDecimal tax = subtotal.multiply(BigDecimal.valueOf(Constants.TAX_RATE));
        BigDecimal grandTotal = subtotal.add(tax);

        User managedUser = userRepository.getReferenceById(userId);
        //تخزين detiles in database
        Order order = new Order();
        order.setUser(managedUser);
        order.setTotalPrice(grandTotal);
        order.setStatus(OrderStatus.PENDING);
        order.setEmailSent(false);
        Order savedOrder = orderRepository.save(order);

        for (OrderItem oi : orderItems) {
            oi.setOrder(savedOrder);
            orderItemRepository.save(oi);
            Book book = oi.getBook();
            //ده اللي بيضمن إن لو حد تاني دخل يشتري نفس الكتاب، يلاقي الكمية نقصت.
            book.setQuantity(book.getQuantity() - oi.getQuantity());
            bookRepository.save(book);
        }
        //الدفع

        Payment payment = new Payment();
        payment.setOrder(savedOrder);
        payment.setStatus(PaymentStatus.PENDING);
        paymentRepository.save(payment);
        //مسح الcart بعد ما خلصنا الطلب

        cartItemRepository.deleteByUserId(userId);

        return savedOrder;
    }

    @Transactional
    //تاكد
    public Payment processPayment(Integer orderId, Integer userId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Order not found."));
        //تاكد ان cart بتعته
        if (!order.getUser().getId().equals(userId)) {
            throw new SecurityException("Unauthorized.");
        }
        Payment payment = paymentRepository.findByOrderId(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Payment not found."));
        //نجاح عمليه الدفع
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setTransactionId("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);

        try {
            //تاكيد الدفع بالايمال
            emailUtil.sendOrderConfirmationEmail(order);
            order.setEmailSent(true);
            orderRepository.save(order);
        } catch (Exception ignored) {}
        // لو لا يرجع كل حاجه بالترتيب
        return payment;
    }

    public Optional<Order> findById(Integer id) {
        return orderRepository.findById(id);
    }

    public List<Order> getOrdersByUser(Integer userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    @Transactional
    //تحديث حالة الطلب من قبل الادمن
    public Order updateStatus(Integer orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Order not found."));
        order.setStatus(status);
        return orderRepository.save(order);
    }
    //حساب المعلومات لadmin
    public long getTotalCount() {
        return orderRepository.count();
    }
    // معرفه الLIMET
    public List<Order> getRecentOrders(int limit) {
        return orderRepository.findAll(
            PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"))
        ).getContent();
    }
}
