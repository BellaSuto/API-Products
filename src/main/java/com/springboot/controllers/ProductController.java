package com.springboot.controllers;

import com.springboot.dto.ProductRecordDto;
import com.springboot.models.ProductModel;
import com.springboot.repository.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @PostMapping
    public ResponseEntity<ProductModel> saveProduct(@RequestBody @Valid ProductRecordDto productRecordDto) {
        var productModel = new ProductModel();
        BeanUtils.copyProperties(productRecordDto, productModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(productRepository.save(productModel));
    }

    @GetMapping
    public ResponseEntity<List<ProductModel>> getAllProducts() {
        List<ProductModel> productList = productRepository.findAll();
        if (!productList.isEmpty()){
            for (ProductModel product: productList){
                UUID id = product.getIdProduct();
                product.add(linkTo(methodOn(ProductController.class).getProduct(id)).withSelfRel());
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(productList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getProduct(@PathVariable("id") UUID id) {
        Optional<ProductModel> optProduct = productRepository.findById(id);
        if (optProduct.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found");
        }
        optProduct.get().add(linkTo(methodOn(ProductController.class).getAllProducts()).withRel("Product List"));

        return ResponseEntity.status(HttpStatus.OK).body(optProduct.get());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> putProduct(@PathVariable("id") UUID id, @RequestBody ProductRecordDto productRecordDto) {
        Optional<ProductModel> optProduct = productRepository.findById(id);
        if (optProduct.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found");
        }
        var productModel = optProduct.get();
        BeanUtils.copyProperties(productRecordDto, productModel);

        return ResponseEntity.status(HttpStatus.OK).body(productRepository.save(productModel));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteProduct(@PathVariable("id")UUID id){
        Optional<ProductModel> optProduct = productRepository.findById(id);
        if (optProduct.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found");
        }
        productRepository.deleteById(id);
        return ResponseEntity.status(HttpStatus.OK).body("Product deleted successfully.");
    }
}
