package pl.kacper.product_catalog_api.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opentest4j.AssertionFailedError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import pl.kacper.product_catalog_api.BaseIT;
import pl.kacper.product_catalog_api.dto.ProductResponseDto;
import pl.kacper.product_catalog_api.model.ProductEntity;
import pl.kacper.product_catalog_api.repository.ProductEntityRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

class ConcurrencyCacheIT extends BaseIT {

    private final ProductService productService;

    @MockitoBean
    private ProductEntityRepository productEntityRepositoryMock;

    @Autowired
    public ConcurrencyCacheIT(ProductService productService) {
        this.productService = productService;
    }

    @Test
    public void shouldHandleMultipleReadOnlyFirstProduceValue() {

        Long productId = 1L;

        ProductEntity productEntity = new ProductEntity(
                "CocaCola",
                "CocaCola drink 2L",
                new BigDecimal("9"),
                1450
        );
        ReflectionTestUtils.setField(productEntity, "productId", productId);

        // DELAY - Other threads should be locked until cache update

        final int threadCount = 8;
        CountDownLatch groupThread = new CountDownLatch(threadCount - 1);
        CountDownLatch firstThread = new CountDownLatch(1);

        Mockito.when(productEntityRepositoryMock.findById(productId)).then(_ -> {
            firstThread.countDown();
            try {
                boolean await = groupThread.await(3, TimeUnit.SECONDS);
                if (!await)
                    throw new TimeoutException("Timeout of waiting for notify - Thread-name=%s".formatted(Thread.currentThread().getName()));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Assertions.fail("Main Thread has been Interrupted while waiting - Thread-name=%s".formatted(Thread.currentThread().getName()));
            } catch (TimeoutException e) {
                Assertions.fail(e.getMessage());
            }
            return Optional.of(productEntity);
        });

        Callable<ProductResponseDto> firstTask = () -> productService.getProduct(productId);
        Callable<ProductResponseDto> groupTask = () -> {
            try {
                boolean await = firstThread.await(3, TimeUnit.SECONDS);
                if (!await)
                    throw new TimeoutException("GROUP THREAD: Timeout of waiting for notify - Thread-name=%s".formatted(Thread.currentThread().getName()));
                groupThread.countDown();
                return productService.getProduct(productId);
            } catch (InterruptedException e) {
                groupThread.countDown();
                Thread.currentThread().interrupt();
                Assertions.fail("GROUP THREAD: Thread of group has been interrupted while waiting - Thread-name=%s".formatted(Thread.currentThread().getName()));
            } catch (TimeoutException e) {
                groupThread.countDown();
                Assertions.fail(e.getMessage());
            }
            throw new AssertionFailedError("Method invocation failure - unexpected method branch");
        };

        List<ProductResponseDto> productResponseDtoList = new ArrayList<>();
        List<Callable<ProductResponseDto>> tasks = new ArrayList<>(Collections.nCopies(7, groupTask));
        tasks.add(firstTask);
        try (
                ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
        ) {
            List<Future<ProductResponseDto>> futures = executorService.invokeAll(tasks, 5, TimeUnit.SECONDS);

            for (Future<ProductResponseDto> future : futures) {
                ProductResponseDto productResponseDto = future.get();
                productResponseDtoList.add(productResponseDto);
            }

        } catch (InterruptedException e) {
            Assertions.fail(e.getMessage());
        } catch (ExecutionException e) {
            Assertions.fail("Fatal application failure: " + e.getMessage());
        }

        ProductResponseDto expectedProductDto = new ProductResponseDto(
                productEntity.getProductId(),
                productEntity.getName(),
                productEntity.getDescription(),
                productEntity.getPrice(),
                productEntity.getStockQuantity()
        );
        Assertions.assertThat(productResponseDtoList).allMatch(productDto -> productDto.equals(expectedProductDto));
        Mockito.verify(productEntityRepositoryMock, Mockito.only()).findById(productId);

        // Additional read from cache to verify using cache instead of computing new value
        productService.getProduct(productId);
        productService.getProduct(productId);

        Mockito.verify(productEntityRepositoryMock, Mockito.only()).findById(productId);
    }
}
