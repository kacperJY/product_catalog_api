package pl.kacper.product_catalog_api.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import pl.kacper.product_catalog_api.BaseIT;
import pl.kacper.product_catalog_api.dto.ProductRequestDto;
import pl.kacper.product_catalog_api.model.ProductEntity;
import pl.kacper.product_catalog_api.repository.ProductEntityRepository;

import java.math.BigDecimal;
import java.util.Optional;

public class ProductServiceIT extends BaseIT {

    private final ProductService productService;
    private final ProductEntityRepository productEntityRepository;

    @Autowired
    public ProductServiceIT(ProductService productService, ProductEntityRepository productEntityRepository) {
        this.productService = productService;
        this.productEntityRepository = productEntityRepository;
    }

    @Test
    public void shouldReturn201WhenSuccessfullyCreateProduct() throws Exception{

        ProductRequestDto productRequestDto = new ProductRequestDto(
                "CocaCola",
                "CocaCola drink 2L",
                new BigDecimal("9"),
                1450
        );

        String jsonContent = objectMapper.writeValueAsString(productRequestDto);

        String productId = mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/products")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonContent)
                )
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Optional<ProductEntity> byId = productEntityRepository.findById(Long.valueOf(productId));
        Assertions.assertThat(byId).isNotEmpty();
        ProductEntity product = byId.get();
        Assertions.assertThat(product.getName()).isEqualTo(productRequestDto.name());
        Assertions.assertThat(product.getDescription()).isEqualTo(productRequestDto.description());
        Assertions.assertThat(product.getPrice()).isEqualByComparingTo(productRequestDto.price());
        Assertions.assertThat(product.getStockQuantity()).isEqualTo(productRequestDto.stockQuantity());
    }

    @Test
    public void shouldReturn204WhenSuccessfullyDeleteProduct() throws Exception{

        ProductRequestDto productRequestDto = new ProductRequestDto(
                "CocaCola",
                "CocaCola drink 2L",
                new BigDecimal("9"),
                1450
        );

        Long productId = productService.addProduct(productRequestDto); // TX

        mockMvc.perform(
                        MockMvcRequestBuilders.delete("/api/products/%s".formatted(productId))
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        Optional<ProductEntity> byId = productEntityRepository.findById(productId);
        Assertions.assertThat(byId).isEmpty();
    }

    @Test
    public void shouldReturn404WhenProductDoesNotExists() throws Exception{
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/products/-1")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void shouldReturn200WhenSuccessfullyUpdateProduct() throws Exception{

        ProductRequestDto productRequestDto = new ProductRequestDto(
                "CocaCola",
                "CocaCola drink 2L",
                new BigDecimal("9"),
                1450
        );
        Long productId = productService.addProduct(productRequestDto); // TX



        ProductRequestDto updatedProductRequestDto = new ProductRequestDto(
                "Sprite",
                "Sprite drink 2L",
                new BigDecimal("7"),
                875
        );
        String jsonContent = objectMapper.writeValueAsString(updatedProductRequestDto);

        mockMvc.perform(
                        MockMvcRequestBuilders.put("/api/products/%s".formatted(productId))
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonContent)
                )
                .andExpect(MockMvcResultMatchers.status().isOk());

        Optional<ProductEntity> byId = productEntityRepository.findById(productId);
        Assertions.assertThat(byId).isNotEmpty();
        ProductEntity product = byId.get();
        Assertions.assertThat(product.getName()).isEqualTo(updatedProductRequestDto.name());
        Assertions.assertThat(product.getDescription()).isEqualTo(updatedProductRequestDto.description());
        Assertions.assertThat(product.getPrice()).isEqualByComparingTo(updatedProductRequestDto.price());
        Assertions.assertThat(product.getStockQuantity()).isEqualTo(updatedProductRequestDto.stockQuantity());
    }

}
