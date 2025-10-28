package com.project.demo.rest.product;


import com.project.demo.logic.entity.product.Product;
import com.project.demo.logic.entity.product.ProductRepository;
import com.project.demo.logic.entity.category.Category;
import com.project.demo.logic.entity.category.CategoryRepository;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/products")
public class ProductRestController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        Pageable pageable = PageRequest.of(page-1, size);
        Page<Product> productsPage = productRepository.findAll(pageable);

        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(productsPage.getTotalPages());
        meta.setTotalElements(productsPage.getTotalElements());
        meta.setPageNumber(productsPage.getNumber() + 1);
        meta.setPageSize(productsPage.getSize());

        return new GlobalResponseHandler().handleResponse("Products retrieved successfully",
                productsPage.getContent(), HttpStatus.OK, meta);
    }

    @GetMapping("/category/{categoryId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getProductsByCategoryId(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        Pageable pageable = PageRequest.of(page-1, size);
        Page<Product> productsPage = productRepository.findByCategoryId(categoryId, pageable);

        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(productsPage.getTotalPages());
        meta.setTotalElements(productsPage.getTotalElements());
        meta.setPageNumber(productsPage.getNumber() + 1);
        meta.setPageSize(productsPage.getSize());

        return new GlobalResponseHandler().handleResponse("Products retrieved successfully",
                productsPage.getContent(), HttpStatus.OK, meta);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getProductById(@PathVariable Long id, HttpServletRequest request) {
        Optional<Product> foundProduct = productRepository.findById(id);
        if(foundProduct.isPresent()) {
            return new GlobalResponseHandler().handleResponse("Product retrieved successfully",
                    foundProduct.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Product " + id + " not found",
                    HttpStatus.NOT_FOUND, request);
        }
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> addProduct(@RequestBody Product product, HttpServletRequest request) {
        if (product.getCategory() != null && product.getCategory().getId() != null) {
            Optional<Category> category = categoryRepository.findById(product.getCategory().getId());
            if (category.isPresent()) {
                product.setCategory(category.get());
                Product savedProduct = productRepository.save(product);
                return new GlobalResponseHandler().handleResponse("Product created successfully",
                        savedProduct, HttpStatus.CREATED, request);
            } else {
                return new GlobalResponseHandler().handleResponse("Category not found",
                        HttpStatus.BAD_REQUEST, request);
            }
        } else {
            return new GlobalResponseHandler().handleResponse("Category is required",
                    HttpStatus.BAD_REQUEST, request);
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody Product product, HttpServletRequest request) {
        Optional<Product> existingProduct = productRepository.findById(id);
        if (existingProduct.isPresent()) {
            Product productToUpdate = existingProduct.get();
            productToUpdate.setName(product.getName());
            productToUpdate.setDescription(product.getDescription());
            productToUpdate.setPrice(product.getPrice());
           productToUpdate.setCategory(product.getCategory());

            if (product.getCategory() != null && product.getCategory().getId() != null) {
                Optional<Category> category = categoryRepository.findById(product.getCategory().getId());
                if (category.isPresent()) {
                    productToUpdate.setCategory(category.get());
                } else {
                    return new GlobalResponseHandler().handleResponse("Category not found",
                            HttpStatus.BAD_REQUEST, request);
                }
            }

            Product savedProduct = productRepository.save(productToUpdate);
            return new GlobalResponseHandler().handleResponse("Product updated successfully",
                    savedProduct, HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Product " + id + " not found",
                    HttpStatus.NOT_FOUND, request);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id, HttpServletRequest request) {
        Optional<Product> foundProduct = productRepository.findById(id);
        if(foundProduct.isPresent()) {
            productRepository.deleteById(foundProduct.get().getId());
            return new GlobalResponseHandler().handleResponse("Product deleted successfully",
                    foundProduct.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Product " + id + " not found",
                    HttpStatus.NOT_FOUND, request);
        }
    }
}