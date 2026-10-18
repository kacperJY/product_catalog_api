package pl.kacper.product_catalog_api;

import org.springframework.boot.SpringApplication;

public class TestProductCatalogApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(ProductCatalogApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
