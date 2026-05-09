package com.example.service;

import com.example.model.Book;
import com.example.model.CartItem;
import com.example.model.User;
import com.example.repository.BookRepository;
import com.example.repository.CartItemRepository;
import com.example.repository.UserRepository;
import com.example.util.Constants;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    //constractor
    //بدل ما نقول new Repository()، إحنا بنخلي Spring هو اللي يجهزهم ويديهم لنا جاهزين.
    public CartService(CartItemRepository cartItemRepository,
                       BookRepository bookRepository,
                       UserRepository userRepository) {
        this.cartItemRepository = cartItemRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    public List<CartItem> getCartItems(Integer userId) {
        return cartItemRepository.findByUserId(userId);
    }

    @Transactional
    // لو حصلت مشكلة في نص العملية، كل حاجة ترجع زي ما كانت
    public CartItem addToCart(Integer userId, Integer bookId, int quantity) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found."));
        //الكتاب خارج الstoce  اصلا
        if (book.getQuantity() <= 0) {
            throw new IllegalStateException(Constants.MSG_OUT_OF_STOCK);
        }
        //بنروح لجدول السلة في datauser وبنسأله: "يا جدول، هل user ده (userId) عنده الكتاب ده (bookId) في سلته؟
        Optional<CartItem> existing = cartItemRepository.findByUserIdAndBookId(userId, bookId);
        if (existing.isPresent()){
            CartItem item = existing.get();
        //بنبداء بminmum و عشان لما user يطلب بزياده عن stock يقف maxmum
            int newQty = Math.min(item.getQuantity() + quantity, Constants.MAX_CART_ITEMS_PER_BOOK);
            //updet
            item.setQuantity(newQty);
            return cartItemRepository.save(item);
        } else {
        //او الكتاب مش موجود
            User user = userRepository.getReferenceById(userId);
            CartItem item = new CartItem();
            item.setUser(user);
            item.setBook(book);
            item.setQuantity(Math.min(quantity, Constants.MAX_CART_ITEMS_PER_BOOK));
            return cartItemRepository.save(item);
        }
    }

    @Transactional
    //لازم نتأكد إن الـ ID اللي مبعوت لنا ده بتاع كتاب موجود فعلاً في السلة في الداتا بيز.
    public CartItem updateQuantity(Integer cartItemId, Integer userId, int quantity) {
        CartItem item = cartItemRepository.findById(cartItemId)
            .orElseThrow(() -> new IllegalArgumentException("Cart item not found."));
        //بيقارن الـ ID بتاع صاحب السلة بالـ ID بتاع الشخص اللي باعت
        if (!item.getUser().getId().equals(userId)) {
            throw new SecurityException("Unauthorized.");
        }
        if (quantity <= 0) {
            cartItemRepository.delete(item);
            return null;
        }
        item.setQuantity(Math.min(quantity, Constants.MAX_CART_ITEMS_PER_BOOK));
        return cartItemRepository.save(item);
    }

    @Transactional
    public void removeItem(Integer cartItemId, Integer userId) {
        CartItem item = cartItemRepository.findById(cartItemId)
            .orElseThrow(() -> new IllegalArgumentException("Cart item not found."));
        if (!item.getUser().getId().equals(userId)) {
            throw new SecurityException("Unauthorized.");
        }
        //امسح يا عمنا
        cartItemRepository.delete(item);
    }
//مسح الcart كلاها
    @Transactional
    public void clearCart(Integer userId) {
        cartItemRepository.deleteByUserId(userId);
    }
//حساب الفلوس
    public double getCartTotal(Integer userId) {
        Double total = cartItemRepository.getCartTotal(userId);
        return total != null ? total : 0.0;
    }
//counter
    public long getCartCount(Integer userId) {
        return cartItemRepository.countByUserId(userId);
    }
}
