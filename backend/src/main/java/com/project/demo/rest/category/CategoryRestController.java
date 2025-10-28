package com.project.demo.rest.category;

import com.project.demo.logic.entity.product.Product;
import com.project.demo.logic.entity.product.ProductRepository;
import com.project.demo.logic.entity.category.Category;
import com.project.demo.logic.entity.category.CategoryRepository;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import com.project.demo.logic.entity.order.Order;
import com.project.demo.logic.entity.user.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/categories")
public class CategoryRestController {
    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        Pageable pageable = PageRequest.of(page-1, size);
        Page<Category> ordersPage = categoryRepository.findAll(pageable);
        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(ordersPage.getTotalPages());
        meta.setTotalElements(ordersPage.getTotalElements());
        meta.setPageNumber(ordersPage.getNumber() + 1);
        meta.setPageSize(ordersPage.getSize());

        return new GlobalResponseHandler().handleResponse("Product List retrieved successfully",
                ordersPage.getContent(), HttpStatus.OK, meta);
    }

    @PostMapping
    public ResponseEntity<?> addCategory(@RequestBody Category gifList, HttpServletRequest request) {
        Category savedOrder = categoryRepository.save(gifList);
        return new GlobalResponseHandler().handleResponse("Product list created successfully",
                savedOrder, HttpStatus.CREATED, request);
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    public ResponseEntity<?> editCategory(@RequestBody Category gifList, HttpServletRequest request) {
        Category savedOrder = categoryRepository.save(gifList);
        return new GlobalResponseHandler().handleResponse("Product list created successfully",
                savedOrder, HttpStatus.CREATED, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    public ResponseEntity<?> deleteOrder(@PathVariable Long id, HttpServletRequest request) {
        Optional<Category> foundItem = categoryRepository.findById(id);
        if(foundItem.isPresent()) {
            categoryRepository.deleteById(foundItem.get().getId());
            return new GlobalResponseHandler().handleResponse("Product List deleted successfully",
                    foundItem.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Product List " + id + " not found"  ,
                    HttpStatus.NOT_FOUND, request);
        }
    }

    @PostMapping("/{categoryId}/products")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    public ResponseEntity<?> addProductToCategory(@PathVariable Long categoryId, @RequestBody Product product, HttpServletRequest request) {
        Optional<Category> foundCategory = categoryRepository.findById(categoryId);
        if(foundCategory.isPresent()) {
            Category category = foundCategory.get();
            product.setCategory(category);
            Product savedProduct = productRepository.save(product);
            return new GlobalResponseHandler().handleResponse("Product added to Product List successfully",
                    savedProduct, HttpStatus.CREATED, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Product List " + categoryId + " not found",
                    HttpStatus.NOT_FOUND, request);
        }
    }

    @GetMapping("/{categoryId}/products")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getProductsFromCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        Optional<Category> foundCategory = categoryRepository.findById(categoryId);
        if(foundCategory.isPresent()) {
            Pageable pageable = PageRequest.of(page-1, size);
            Page<Product> productsPage = productRepository.findByCategoryId(categoryId, pageable);

            Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
            meta.setTotalPages(productsPage.getTotalPages());
            meta.setTotalElements(productsPage.getTotalElements());
            meta.setPageNumber(productsPage.getNumber() + 1);
            meta.setPageSize(productsPage.getSize());

            return new GlobalResponseHandler().handleResponse("Products from Product List retrieved successfully",
                    productsPage.getContent(), HttpStatus.OK, meta);
        } else {
            return new GlobalResponseHandler().handleResponse("Product List " + categoryId + " not found",
                    HttpStatus.NOT_FOUND, request);
        }
    }

    @DeleteMapping("/{categoryId}/products/{productId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    public ResponseEntity<?> removeProductFromCategory(@PathVariable Long categoryId, @PathVariable Long productId, HttpServletRequest request) {
        Optional<Category> foundCategory = categoryRepository.findById(categoryId);
        Optional<Product> foundProduct = productRepository.findById(productId);

        if(foundCategory.isPresent() && foundProduct.isPresent()) {
            Product product = foundProduct.get();
            if(product.getCategory().getId().equals(categoryId)) {
                productRepository.deleteById(productId);
                return new GlobalResponseHandler().handleResponse("Product removed from Product List successfully",
                        product, HttpStatus.OK, request);
            } else {
                return new GlobalResponseHandler().handleResponse("Product " + productId + " does not belong to Product List " + categoryId,
                        HttpStatus.BAD_REQUEST, request);
            }
        } else {
            return new GlobalResponseHandler().handleResponse("Product List or Product not found",
                    HttpStatus.NOT_FOUND, request);
        }
    }

}
