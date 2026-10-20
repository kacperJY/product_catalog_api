package pl.kacper.product_catalog_api.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.kacper.product_catalog_api.dto.ProductRequestDto;
import pl.kacper.product_catalog_api.dto.ProductResponseDto;
import pl.kacper.product_catalog_api.service.ProductService;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/products")
    public ResponseEntity<Long> addProduct(@RequestBody @Valid ProductRequestDto productRequestDto) {

        Long productId = productService.addProduct(productRequestDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(productId);
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<ProductResponseDto> getProduct(@PathVariable("productId") Long productId) {

        return ResponseEntity.ok(productService.getProduct(productId));
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductResponseDto>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @PutMapping("/products/{productId}")
    public ResponseEntity<Void> updateProduct(@PathVariable("productId") Long productId, @RequestBody @Valid ProductRequestDto productRequestDto) {
        productService.updateProduct(productId, productRequestDto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("/products/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable("productId") Long productId) {
        productService.deleteProduct(productId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
