package com.example.util;

import com.example.model.Order;
import com.example.model.OrderItem;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.internet.MimeMessage;

@Component
public class EmailUtil {

    private final JavaMailSender mailSender;

    public EmailUtil(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOrderConfirmationEmail(Order order) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("noreply@bookstore.com", "Online Bookstore");
            helper.setTo(order.getUser().getEmail());
            helper.setSubject(Constants.EMAIL_SUBJECT_ORDER);
            helper.setText(buildEmailBody(order), true);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Email send failed: " + e.getMessage());
        }
    }

    private String buildEmailBody(Order order) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style='font-family:Arial,sans-serif;'>");
        sb.append("<h2>Order Confirmation</h2>");
        sb.append("<p>Dear ").append(order.getUser().getName()).append(",</p>");
        sb.append("<p>Thank you for your order! Your order details:</p>");
        sb.append("<p><b>Order #:</b> ").append(order.getId()).append("<br>");
        sb.append("<b>Date:</b> ").append(order.getCreatedAt()).append("<br>");
        sb.append("<b>Status:</b> ").append(order.getStatus()).append("</p>");

        sb.append("<table border='1' cellpadding='6' cellspacing='0' style='border-collapse:collapse;'>");
        sb.append("<tr><th>Book</th><th>Qty</th><th>Price</th><th>Total</th></tr>");
        if (order.getOrderItems() != null) {
            for (OrderItem item : order.getOrderItems()) {
                sb.append("<tr>");
                sb.append("<td>").append(item.getBook().getTitle()).append("</td>");
                sb.append("<td>").append(item.getQuantity()).append("</td>");
                sb.append("<td>$").append(String.format("%.2f", item.getUnitPrice())).append("</td>");
                sb.append("<td>$").append(String.format("%.2f",
                    item.getUnitPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity())))).append("</td>");
                sb.append("</tr>");
            }
        }
        sb.append("</table>");
        sb.append("<p><b>Grand Total: $").append(String.format("%.2f", order.getTotalPrice())).append("</b></p>");
        sb.append("<p>Thank you for shopping with us!</p>");
        sb.append("</body></html>");
        return sb.toString();
    }
}
