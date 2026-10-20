package pl.kacper.product_catalog_api.service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Payload;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.metadata.ConstraintDescriptor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.kacper.product_catalog_api.dto.ProductRequestDto;
import pl.kacper.product_catalog_api.exception.NoSuchDbRecordException;
import pl.kacper.product_catalog_api.repository.ProductEntityRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductEntityRepository productEntityRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    public void shouldThrowExceptionForInvalidDto(){

        String[] constraints = {"Digits", "PositiveOrZero", "NotBlank", "Positive"};

        ProductRequestDto productRequestDto = new ProductRequestDto(
                "",
                "",
                new BigDecimal("-1.123"),
                -1450
        );

        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<ProductRequestDto>> constraintViolations = validator.validate(productRequestDto);

        Assertions.assertThat(constraintViolations).hasSize(constraints.length);

        for (ConstraintViolation<ProductRequestDto> constraintViolation : constraintViolations) {
            Map<String, Object> attributes = constraintViolation.getConstraintDescriptor().getAttributes();
            String message = attributes.get("message").toString();
            Assertions.assertThat(message).containsAnyOf(constraints);
        }
    }

    @Test
    public void shouldSuccessfullyValidDto(){

        ProductRequestDto productRequestDto = new ProductRequestDto(
                "CocaCola",
                "CocaCola drink 2L",
                new BigDecimal("9"),
                1450
        );

        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<ProductRequestDto>> constraintViolations = validator.validate(productRequestDto);
        Assertions.assertThat(constraintViolations).isEmpty();
    }

    @Test
    public void shouldThrowExceptionWhenProductNotExists(){
        Long productId = 1L;

        Mockito.when(productEntityRepository.findById(productId)).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> productService.getProduct(productId))
                .isInstanceOf(NoSuchDbRecordException.class);
    }
}
