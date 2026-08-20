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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.ArrayList;
import java.util.List;

@Service
public class CartService {

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
    // FIND USER
    // ==========================================

    private User findUser(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found with email: " + email
                        )
                );
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
    public CartResponse addToCart(
            String email,
            AddCartRequest request
    ) {

        User user = findUser(email);

        Cart cart = getOrCreateCart(user);


        if (request.getQuantity() <= 0) {

            throw new RuntimeException(
                    "Quantity must be greater than zero"
            );
        }


        Product product =
                productRepository
                        .findByName(request.getProductName())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Product not found: "
                                                + request.getProductName()
                                )
                        );


        // Check stock

        if (product.getStock() <= 0) {

            throw new RuntimeException(
                    "Product is out of stock"
            );
        }


        CartItem cartItem =
                cartItemRepository
                        .findByCartIdAndProductId(
                                cart.getId(),
                                product.getId()
                        )
                        .orElse(null);


        if (cartItem != null) {

            int newQuantity =
                    cartItem.getQuantity()
                            + request.getQuantity();


            if (newQuantity > product.getStock()) {

                throw new RuntimeException(
                        "Only "
                                + product.getStock()
                                + " items available"
                );
            }


            cartItem.setQuantity(
                    newQuantity
            );

            /*
             * Always get price from Product.
             * Never trust frontend price.
             */
            cartItem.setPrice(
                    calculateFinalPrice(product)
            );

            cartItemRepository.save(
                    cartItem
            );

        } else {

            CartItem newItem =
                    new CartItem();

            newItem.setCart(cart);

            newItem.setProduct(product);

            newItem.setQuantity(
                    request.getQuantity()
            );

            newItem.setPrice(
                    calculateFinalPrice(product)
            );

            cart.getItems().add(
                    newItem
            );

            cartItemRepository.save(
                    newItem
            );
        }


        cartRepository.save(cart);


        return buildCartResponse(
                cart
        );
    }


    // ==========================================
    // CALCULATE PRODUCT FINAL PRICE
    // ==========================================

    private double calculateFinalPrice(
            Product product
    ) {

        double price =
                product.getPrice();

        double discount =
                product.getDiscount();


        /*
         * Assuming discount is percentage.
         *
         * Example:
         * price = 40000
         * discount = 10
         *
         * final = 36000
         */

        if (discount > 0) {

            price =
                    price -
                            (price * discount / 100.0);
        }


        return price;
    }


    // ==========================================
    // GET CART
    // ==========================================

    @Transactional(readOnly = true)
    public CartResponse getCart(
            String email
    ) {

        User user = findUser(email);

        Cart cart = getOrCreateCart(user);

        return buildCartResponse(
                cart
        );
    }


    // ==========================================
    // BUILD CART RESPONSE
    // ==========================================

    private CartResponse buildCartResponse(
            Cart cart
    ) {

        List<CartItemResponse> responseItems =
                new ArrayList<>();

        int totalItems = 0;

        double totalAmount = 0;


        for (CartItem item : cart.getItems()) {

            Product product =
                    item.getProduct();


            double finalPrice =
                    calculateFinalPrice(
                            product
                    );


            /*
             * Keep cart price synchronized
             * with current product price.
             */
            item.setPrice(
                    finalPrice
            );


            double subtotal =
                    finalPrice *
                            item.getQuantity();


            CartItemResponse response =
                    new CartItemResponse();


            response.setId(
                    item.getId()
            );

            response.setProductId(
                    product.getId()
            );

            response.setProductName(
                    product.getName()
            );

            response.setDescription(
                    product.getDescription()
            );

            response.setPrice(
                    product.getPrice()
            );

            response.setDiscount(
                    product.getDiscount()
            );

            response.setFinalPrice(
                    finalPrice
            );

            response.setQuantity(
                    item.getQuantity()
            );

            response.setSubtotal(
                    subtotal
            );

            response.setStock(
                    product.getStock()
            );

            response.setImage(
                    product.getImage()
            );

            response.setBrand(
                    product.getBrand()
            );


            responseItems.add(
                    response
            );


            totalItems +=
                    item.getQuantity();


            totalAmount +=
                    subtotal;
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
    public CartResponse updateQuantity(
            String email,
            Long cartItemId,
            UpdateCartRequest request
    ) {

        User user = findUser(email);

        Cart cart = getOrCreateCart(user);


        CartItem cartItem =
                cartItemRepository
                        .findById(cartItemId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Cart item not found"
                                )
                        );


        // Security check
        if (
                !cartItem.getCart()
                        .getId()
                        .equals(cart.getId())
        ) {

            throw new RuntimeException(
                    "You cannot modify this cart item"
            );
        }


        Product product =
                cartItem.getProduct();


        if (request.getQuantity() <= 0) {

            cart.getItems().remove(
                    cartItem
            );

            cartItemRepository.delete(
                    cartItem
            );

        } else {

            if (
                    request.getQuantity()
                            > product.getStock()
            ) {

                throw new RuntimeException(
                        "Only "
                                + product.getStock()
                                + " items available"
                );
            }


            cartItem.setQuantity(
                    request.getQuantity()
            );

            cartItem.setPrice(
                    calculateFinalPrice(
                            product
                    )
            );

            cartItemRepository.save(
                    cartItem
            );
        }


        cartRepository.save(cart);


        return buildCartResponse(
                cart
        );
    }


    // ==========================================
    // REMOVE ITEM
    // ==========================================

    @Transactional
    public CartResponse removeItem(
            String email,
            Long cartItemId
    ) {

        User user = findUser(email);

        Cart cart =
                getOrCreateCart(user);


        CartItem cartItem =
                cartItemRepository
                        .findById(cartItemId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Cart item not found"
                                )
                        );


        if (
                !cartItem.getCart()
                        .getId()
                        .equals(cart.getId())
        ) {

            throw new RuntimeException(
                    "You cannot remove this cart item"
            );
        }


        cart.getItems().remove(
                cartItem
        );

        cartItemRepository.delete(
                cartItem
        );

        cartRepository.save(cart);


        return buildCartResponse(
                cart
        );
    }


    // ==========================================
    // CLEAR CART
    // ==========================================

    @Transactional
    public CartResponse clearCart(
            String email
    ) {

        User user = findUser(email);

        Cart cart =
                getOrCreateCart(user);


        /*
         * Because Cart has:
         *
         * cascade = ALL
         * orphanRemoval = true
         *
         * removing items from the list will
         * delete them from database.
         */

        cart.getItems().clear();

        cartRepository.save(cart);


        return buildCartResponse(
                cart
        );
    }
}