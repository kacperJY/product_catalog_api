package pl.kacper.product_catalog_api.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;
import pl.kacper.product_catalog_api.model.ProductEntity;

@Repository
public interface ProductEntityRepository extends ListCrudRepository<ProductEntity, Long> {



}
