package com.programmingtechie.productservice;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.programmingtechie.productservice.dto.ProductRequest;
import com.programmingtechie.productservice.dto.ProductResponse;
import com.programmingtechie.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

@SpringBootTest
@RequiredArgsConstructor
@Testcontainers
@AutoConfigureMockMvc
class ProductServiceApplicationTests {
	@Container
	static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4.2");

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ProductRepository productRepository;

	@DynamicPropertySource
	static void setProperties(DynamicPropertyRegistry dynamicPropertyRegistry) {
		dynamicPropertyRegistry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
	}

	@Test
	void shouldCreateProduct() throws Exception {
		ProductRequest productRequest = getProductRequest();
		String productRequestString = objectMapper.writeValueAsString(productRequest);
		mockMvc.perform(MockMvcRequestBuilders.post("/api/product")
				.contentType(MediaType.APPLICATION_JSON)
				.content(productRequestString)
		).andExpect(MockMvcResultMatchers.status().isCreated());

		Assertions.assertEquals(1,productRepository.findAll().size());
	}

	@Test
	void shouldGetAllProducts() throws Exception {
		ProductRequest productRequest = getProductRequest();
		String productRequestString = objectMapper.writeValueAsString(productRequest);

		mockMvc.perform(MockMvcRequestBuilders.post("/api/product")
				.contentType(MediaType.APPLICATION_JSON)
				.content(productRequestString)
		);
		mockMvc.perform(MockMvcRequestBuilders.post("/api/product")
				.contentType(MediaType.APPLICATION_JSON)
				.content(productRequestString)
		);

		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/product")
				.contentType(MediaType.APPLICATION_JSON)
		).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

		List<ProductResponse> actual = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<List<ProductResponse>>() {});
		ProductResponse first = actual.get(0);
		ProductResponse second = actual.get(1);

		Assertions.assertEquals("iPhone 13", first.getName());
		Assertions.assertEquals("iPhone 13", second.getName());
	}

	private ProductRequest getProductRequest() {
		return ProductRequest.builder()
				.name("iPhone 13")
				.description("iphone 13 is expensive")
				.price(BigDecimal.valueOf(1200))
				.build();
	}

}
