package pl.kacper.product_catalog_api.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import pl.kacper.product_catalog_api.BaseIT;
import pl.kacper.product_catalog_api.dto.ProductRequestDto;
import pl.kacper.product_catalog_api.dto.ProductResponseDto;
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
    public void shouldReturn201WhenSuccessfullyCreateProduct() throws Exception {

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
    public void shouldReturn204WhenSuccessfullyDeleteProduct() throws Exception {

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
    public void shouldReturn404WhenProductDoesNotExists() throws Exception {
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/products/-1")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void shouldReturn200WhenSuccessfullyUpdateProduct() throws Exception {

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

    @Test
    public void shouldReturnProductFromCacheIfExists() throws Exception {
        ProductRequestDto productRequestDto = new ProductRequestDto(
                "CocaCola",
                "CocaCola drink 2L",
                new BigDecimal("9"),
                1450
        );

        Long productId = productService.addProduct(productRequestDto); // TX

        // Fetch from database - FIRST TIME
        String productResponseFirst = mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/products/%s".formatted(productId))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn().getResponse().getContentAsString();

        Optional<ProductEntity> byId = productEntityRepository.findById(productId);
        Assertions.assertThat(byId).isNotEmpty();
        ProductEntity productEntity = byId.get();

        // VALIDATE RESPONSE
        ProductResponseDto productResponseDto = objectMapper.readValue(productResponseFirst, ProductResponseDto.class);
        Assertions.assertThat(productEntity.getProductId()).isEqualTo(productResponseDto.productId());
        Assertions.assertThat(productEntity.getName()).isEqualTo(productResponseDto.name());
        Assertions.assertThat(productEntity.getDescription()).isEqualTo(productResponseDto.description());
        Assertions.assertThat(productEntity.getPrice()).isEqualByComparingTo(productResponseDto.price());
        Assertions.assertThat(productEntity.getStockQuantity()).isEqualTo(productResponseDto.stockQuantity());

        productEntityRepository.deleteById(productId); // remove only from database without removing from cache

        Optional<ProductEntity> byIdAfterDelete = productEntityRepository.findById(productId);
        Assertions.assertThat(byIdAfterDelete).isEmpty();

        // Read product from Cache - SECOND TIME
        String productResponseSecond = mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/products/%s".formatted(productId))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn().getResponse().getContentAsString();
        ProductResponseDto productResponseDtoFromCache = objectMapper.readValue(productResponseSecond, ProductResponseDto.class);
        Assertions.assertThat(productEntity.getProductId()).isEqualTo(productResponseDtoFromCache.productId());
        Assertions.assertThat(productEntity.getName()).isEqualTo(productResponseDtoFromCache.name());
        Assertions.assertThat(productEntity.getDescription()).isEqualTo(productResponseDtoFromCache.description());
        Assertions.assertThat(productEntity.getPrice()).isEqualByComparingTo(productResponseDtoFromCache.price());
        Assertions.assertThat(productEntity.getStockQuantity()).isEqualTo(productResponseDtoFromCache.stockQuantity());
    }

    @Test
    public void shouldThrow404WhenFetchProductAfterDeletion() throws Exception {

        ProductRequestDto productRequestDto = new ProductRequestDto(
                "CocaCola",
                "CocaCola drink 2L",
                new BigDecimal("9"),
                1450
        );

        String jsonContent = objectMapper.writeValueAsString(productRequestDto);

        String responseProductId = mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/products")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonContent)
                )
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long productId = Long.valueOf(responseProductId);

        // GET - to add product to Cache
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/products/%s".formatted(productId))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk());

        // Remove Product from database and cache
        mockMvc.perform(
                        MockMvcRequestBuilders.delete("/api/products/%s".formatted(productId))
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isNoContent());


        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/products/" + responseProductId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void shouldFetchNewProductVersionFromCacheAfterUpdate() throws Exception {
        ProductRequestDto productRequestDto = new ProductRequestDto(
                "CocaCola",
                "CocaCola drink 2L",
                new BigDecimal("9"),
                1450
        );

        String jsonContent = objectMapper.writeValueAsString(productRequestDto);

        String responseProductId = mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/products")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonContent)
                )
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long productId = Long.valueOf(responseProductId);

        // GET - to add product to Cache
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/products/%s".formatted(productId))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk());


        ProductRequestDto updatedProductRequestDto = new ProductRequestDto(
                "Fanta",
                "Fanta drink 2L",
                new BigDecimal("6"),
                870
        );

        String updatedJonContent = objectMapper.writeValueAsString(updatedProductRequestDto);

        mockMvc.perform(
                        MockMvcRequestBuilders.put("/api/products/%s".formatted(productId))
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updatedJonContent)
                )
                .andExpect(MockMvcResultMatchers.status().isOk());

        productEntityRepository.deleteById(productId); // remove only from database without removing from cache

        // Read product from Cache
        String productResponse = mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/products/%s".formatted(productId))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn().getResponse().getContentAsString();

        Optional<ProductEntity> byId = productEntityRepository.findById(productId);
        Assertions.assertThat(byId).isEmpty();

        ProductResponseDto productResponseDto = objectMapper.readValue(productResponse, ProductResponseDto.class);
        Assertions.assertThat(productResponseDto.productId()).isEqualTo(productId);
        Assertions.assertThat(productResponseDto.name()).isEqualTo(updatedProductRequestDto.name());
        Assertions.assertThat(productResponseDto.description()).isEqualTo(updatedProductRequestDto.description());
        Assertions.assertThat(productResponseDto.price()).isEqualByComparingTo(updatedProductRequestDto.price());
        Assertions.assertThat(productResponseDto.stockQuantity()).isEqualTo(updatedProductRequestDto.stockQuantity());
    }

    @Test
    public void shouldTwoProductsBeIndependentAsCachedValues() throws Exception {
        ProductRequestDto productRequestDtoOne = new ProductRequestDto(
                "Fanta",
                "Fanta drink 2L",
                new BigDecimal("6"),
                870
        );
        ProductRequestDto productRequestDtoTwo = new ProductRequestDto(
                "CocaCola",
                "CocaCola drink 2L",
                new BigDecimal("9"),
                1450
        );

        String jsonContentProductOne = objectMapper.writeValueAsString(productRequestDtoOne);
        String jsonContentProductTwo = objectMapper.writeValueAsString(productRequestDtoTwo);

        String responseProductOneId = mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/products")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonContentProductOne)
                )
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String responseProductTwoId = mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/products")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonContentProductTwo)
                )
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long productOneId = Long.valueOf(responseProductOneId);
        Long productTwoId = Long.valueOf(responseProductTwoId);

        // ADD TO CACHE
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/products/%s".formatted(productOneId))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk());

        // ADD TO CACHE
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/products/%s".formatted(productTwoId))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk());


        // UPDATE PRODUCT_TWO
        ProductRequestDto updatedProductRequestDtoTwo = new ProductRequestDto(
                "Orange juice",
                "Orange juice 2L",
                new BigDecimal("14"),
                2100
        );
        String updatedJsonContentProductTwo = objectMapper.writeValueAsString(updatedProductRequestDtoTwo);
        mockMvc.perform(
                        MockMvcRequestBuilders.put("/api/products/%s".formatted(productTwoId))
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updatedJsonContentProductTwo)
                )
                .andExpect(MockMvcResultMatchers.status().isOk());

        productEntityRepository.deleteById(productOneId); // remove from database without removing from cache
        productEntityRepository.deleteById(productTwoId); // remove from database without removing from cache

        // READ FROM CACHE
        String responseProductOne = mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/products/%s".formatted(productOneId))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn().getResponse().getContentAsString();

        // READ FROM CACHE
        String responseProductTwo = mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/products/%s".formatted(productTwoId))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn().getResponse().getContentAsString();

        ProductResponseDto productResponseDtoOne = objectMapper.readValue(responseProductOne, ProductResponseDto.class);
        ProductResponseDto productResponseDtoTwo = objectMapper.readValue(responseProductTwo, ProductResponseDto.class);

        // Check response for product_one with original requestDto
        Assertions.assertThat(productResponseDtoOne.productId()).isEqualTo(productOneId);
        Assertions.assertThat(productResponseDtoOne.name()).isEqualTo(productRequestDtoOne.name());
        Assertions.assertThat(productResponseDtoOne.description()).isEqualTo(productRequestDtoOne.description());
        Assertions.assertThat(productResponseDtoOne.price()).isEqualByComparingTo(productRequestDtoOne.price());
        Assertions.assertThat(productResponseDtoOne.stockQuantity()).isEqualTo(productRequestDtoOne.stockQuantity());


        // Check response for product_one with UPDATED requestDto
        Assertions.assertThat(productResponseDtoTwo.productId()).isEqualTo(productTwoId);
        Assertions.assertThat(productResponseDtoTwo.name()).isEqualTo(updatedProductRequestDtoTwo.name());
        Assertions.assertThat(productResponseDtoTwo.description()).isEqualTo(updatedProductRequestDtoTwo.description());
        Assertions.assertThat(productResponseDtoTwo.price()).isEqualByComparingTo(updatedProductRequestDtoTwo.price());
        Assertions.assertThat(productResponseDtoTwo.stockQuantity()).isEqualTo(updatedProductRequestDtoTwo.stockQuantity());
    }
}
