package io.virinchi.glowup.service;

import io.virinchi.glowup.dto.AddCartRequest;
import io.virinchi.glowup.dto.CartItemResponse;
import io.virinchi.glowup.dto.CartResponse;
import io.virinchi.glowup.dto.UpdateCartRequest;
import io.virinchi.glowup.entity.Cart;
import io.virinchi.glowup.entity.CartItem;
import io.virinchi.glowup.entity.Product;
import io.virinchi.glowup.entity.User;
import io.virinchi.glowup.repository.CartItemRepository;
import io.virinchi.glowup.repository.CartRepository;
import io.virinchi.glowup.repository.ProductRepository;
import io.virinchi.glowup.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CartService {

    private static final Logger log = LoggerFactory.getLogger(CartService.class);

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartService(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            ProductRepository productRepository,
            UserRepository userRepository
    ) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    // ==========================================
    // FIND OR CREATE USER
    // ==========================================
    private User findUser(String email) {
        String cleanEmail = email.trim().toLowerCase();
        return userRepository.findByEmail(cleanEmail)
                .orElseGet(() -> {
                    log.info("Auto-creating user profile for email: {}", cleanEmail);
                    User user = new User();
                    user.setEmail(cleanEmail);
                    user.setFullName(cleanEmail.split("@")[0]);
                    user.setPassword("glowup_guest_session");
                    user.setRole(cleanEmail.contains("admin") ? "ADMIN" : "CUSTOMER");
                    return userRepository.save(user);
                });
    }

    // ==========================================
    // GET OR CREATE CART
    // ==========================================
    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    cart.setUser(user);
                    return cartRepository.save(cart);
                });
    }

    // ==========================================
    // ADD TO CART
    // ==========================================
    @Transactional
    public CartResponse addToCart(String email, AddCartRequest request) {
        User user = findUser(email);
        Cart cart = getOrCreateCart(user);

        int quantity = request.getQuantity() > 0 ? request.getQuantity() : 1;
        String prodName = request.getProductName() != null ? request.getProductName().trim() : "Product";

        // Find or auto-create Product in DB
        Product product = productRepository.findByNameIgnoreCase(prodName)
                .orElse(null);

        if (product == null) {
            product = productRepository.findByName(prodName).orElse(null);
        }

        if (product == null) {
            log.info("Product not yet in DB, creating entry for: {}", prodName);
            product = new Product();
            product.setName(prodName);
            product.setPrice(request.getPrice() != null && request.getPrice() > 0 ? request.getPrice() : 1200.0);
            product.setStock(100);
            product.setRating(5.0);
            product.setImage(request.getImage() != null ? request.getImage() : "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=800&q=80");
            product = productRepository.save(product);
        }

        CartItem cartItem = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), product.getId())
                .orElse(null);

        if (cartItem != null) {
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItem.setPrice(calculateFinalPrice(product));
            cartItemRepository.save(cartItem);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            newItem.setPrice(calculateFinalPrice(product));
            cart.getItems().add(newItem);
            cartItemRepository.save(newItem);
        }

        cartRepository.save(cart);
        return buildCartResponse(cart);
    }

    private double calculateFinalPrice(Product product) {
        double price = product.getPrice();
        double discount = product.getDiscount();
        if (discount > 0) {
            price = price - (price * discount / 100.0);
        }
        return price;
    }

    // ==========================================
    // GET CART
    // ==========================================
    @Transactional(readOnly = true)
    public CartResponse getCart(String email) {
        User user = findUser(email);
        Cart cart = getOrCreateCart(user);
        return buildCartResponse(cart);
    }

    // ==========================================
    // BUILD CART RESPONSE
    // ==========================================
    private CartResponse buildCartResponse(Cart cart) {
        List<CartItemResponse> responseItems = new ArrayList<>();
        int totalItems = 0;
        double totalAmount = 0.0;

        if (cart.getItems() != null) {
            for (CartItem item : cart.getItems()) {
                Product product = item.getProduct();
                if (product == null) continue;

                double finalPrice = calculateFinalPrice(product);
                item.setPrice(finalPrice);
                double subtotal = finalPrice * item.getQuantity();

                CartItemResponse response = new CartItemResponse();
                response.setId(item.getId());
                response.setProductId(product.getId());
                response.setProductName(product.getName());
                response.setDescription(product.getDescription());
                response.setPrice(product.getPrice());
                response.setDiscount(product.getDiscount());
                response.setFinalPrice(finalPrice);
                response.setQuantity(item.getQuantity());
                response.setSubtotal(subtotal);
                response.setStock(product.getStock());
                response.setImage(product.getImage());
                response.setBrand(product.getBrand());

                responseItems.add(response);
                totalItems += item.getQuantity();
                totalAmount += subtotal;
            }
        }

        return new CartResponse(
                cart.getId(),
                responseItems,
                totalItems,
                totalAmount
        );
    }

    // ==========================================
    // UPDATE QUANTITY
    // ==========================================
    @Transactional
    public CartResponse updateQuantity(String email, Long cartItemId, UpdateCartRequest request) {
        User user = findUser(email);
        Cart cart = getOrCreateCart(user);

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("Unauthorized cart modification");
        }

        Product product = cartItem.getProduct();

        if (request.getQuantity() <= 0) {
            cart.getItems().remove(cartItem);
            cartItemRepository.delete(cartItem);
        } else {
            cartItem.setQuantity(request.getQuantity());
            if (product != null) {
                cartItem.setPrice(calculateFinalPrice(product));
            }
            cartItemRepository.save(cartItem);
        }

        cartRepository.save(cart);
        return buildCartResponse(cart);
    }

    // ==========================================
    // REMOVE ITEM
    // ==========================================
    @Transactional
    public CartResponse removeItem(String email, Long cartItemId) {
        User user = findUser(email);
        Cart cart = getOrCreateCart(user);

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("Unauthorized cart removal");
        }

        cart.getItems().remove(cartItem);
        cartItemRepository.delete(cartItem);
        cartRepository.save(cart);

        return buildCartResponse(cart);
    }

    // ==========================================
    // CLEAR CART
    // ==========================================
    @Transactional
    public CartResponse clearCart(String email) {
        User user = findUser(email);
        Cart cart = getOrCreateCart(user);

        cart.getItems().clear();
        cartRepository.save(cart);

        return buildCartResponse(cart);
    }
}