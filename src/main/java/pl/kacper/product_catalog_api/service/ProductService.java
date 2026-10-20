package pl.kacper.product_catalog_api.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.kacper.product_catalog_api.dto.ProductRequestDto;
import pl.kacper.product_catalog_api.dto.ProductResponseDto;
import pl.kacper.product_catalog_api.exception.NoSuchDbRecordException;
import pl.kacper.product_catalog_api.model.ProductEntity;
import pl.kacper.product_catalog_api.repository.ProductEntityRepository;

import java.util.List;

@Service
public class ProductService {

    private final ProductEntityRepository productEntityRepository;

    public ProductService(ProductEntityRepository productEntityRepository) {
        this.productEntityRepository = productEntityRepository;
    }

    @Transactional
    public Long addProduct(ProductRequestDto productRequestDto) {

        ProductEntity productEntity = new ProductEntity(
                productRequestDto.name(),
                productRequestDto.description(),
                productRequestDto.price(),
                productRequestDto.stockQuantity()
        );

        productEntityRepository.save(productEntity);

        return productEntity.getProductId();
    }

    @Transactional(readOnly = true)
    public ProductResponseDto getProduct(Long productId) {

        ProductEntity productEntity = productEntityRepository.findById(productId)
                .orElseThrow(() -> new NoSuchDbRecordException("There is no product with ID=" + productId));

        return new ProductResponseDto(
                productEntity.getProductId(),
                productEntity.getName(),
                productEntity.getDescription(),
                productEntity.getPrice(),
                productEntity.getStockQuantity()
        );
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDto> getAllProducts() {
        return productEntityRepository.findAll()
                .stream()
                .map(productEntity -> new ProductResponseDto(
                        productEntity.getProductId(),
                        productEntity.getName(),
                        productEntity.getDescription(),
                        productEntity.getPrice(),
                        productEntity.getStockQuantity()))
                .toList();
    }

    @Transactional
    public void updateProduct(Long productId, ProductRequestDto productRequestDto){
        ProductEntity productEntity = productEntityRepository.findById(productId)
                .orElseThrow(() -> new NoSuchDbRecordException("There is no product with ID=" + productId));

        productEntity.setName(productRequestDto.name());
        productEntity.setDescription(productRequestDto.description());
        productEntity.setPrice(productRequestDto.price());
        productEntity.setStockQuantity(productRequestDto.stockQuantity());
    }

    @Transactional
    public void deleteProduct(Long productId){
        ProductEntity productEntity = productEntityRepository.findById(productId)
                .orElseThrow(() -> new NoSuchDbRecordException("There is no product with ID=" + productId));

        productEntityRepository.delete(productEntity);
    }
}
