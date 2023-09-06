package epam.xstack.todoapp;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension.class)
@Transactional
class TodoappApplicationTests {
	@LocalServerPort
	private Integer port;

	@Autowired
	private MockMvc mockMvc;

	private static  MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:latest")
			.withDatabaseName("todoapp_db")
			.withUsername("epamdev")
			.withPassword("epamdev");

	@BeforeAll
	static void beforeAll() {
		mysqlContainer.start();
	}

	@AfterAll
	static void afterAll() {
		mysqlContainer.stop();
	}

	@DynamicPropertySource
	static void configureProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
		registry.add("spring.datasource.username", mysqlContainer::getUsername);
		registry.add("spring.datasource.password", mysqlContainer::getPassword);
	}

	@Test
	void contextLoads() {
	}

	@Test
	@Rollback
	public void testCreateTodoItem() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders
						.post("/api/todos")
						.content("{\"title\":\"Test Task\",\"description\":\"Test Description\"}")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isCreated())
				.andExpect(MockMvcResultMatchers.jsonPath("$.id").exists())
				.andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Test Task"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.description").value("Test Description"));
//				.andDo(print());
	}

	@Test
	@Rollback
	public void testGetTodoItemById() throws Exception {
		MvcResult createResult = mockMvc.perform(MockMvcRequestBuilders
						.post("/api/todos")
						.content("{\"title\":\"Test Task\",\"description\":\"Test Description\"}")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isCreated())
				.andExpect(MockMvcResultMatchers.jsonPath("$.id").exists())
//				.andDo(print())
				.andReturn();

		String responseBody = createResult.getResponse().getContentAsString();
		Integer createdItemId = JsonPath.read(responseBody, "$.id");

		mockMvc.perform(MockMvcRequestBuilders
						.get("/api/todos/{id}", createdItemId)
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.id").value(createdItemId))
				.andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Test Task"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.description").value("Test Description"));
//				.andDo(print());
	}

	@Test
	@Rollback
	public void testUpdateTodoItem() throws Exception {
		MvcResult createResult = mockMvc.perform(MockMvcRequestBuilders
						.post("/api/todos")
						.content("{\"title\":\"Test Task\",\"description\":\"Test Description\"}")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isCreated())
				.andExpect(MockMvcResultMatchers.jsonPath("$.id").exists())
//				.andDo(print())
				.andReturn();

		String responseBody = createResult.getResponse().getContentAsString();
		Integer createdItemId = JsonPath.read(responseBody, "$.id");

		mockMvc.perform(MockMvcRequestBuilders
						.put("/api/todos/{id}", createdItemId)
						.content("{\"title\":\"Updated Task\",\"description\":\"Updated Description\"}")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.id").value(createdItemId))
				.andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Updated Task"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.description").value("Updated Description"));
//				.andDo(print());
	}

	@Test
	@Rollback
	public void testDeleteTodoItem() throws Exception {
		MvcResult createResult = mockMvc.perform(MockMvcRequestBuilders
						.post("/api/todos")
						.content("{\"title\":\"Test Task\",\"description\":\"Test Description\"}")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isCreated())
				.andExpect(MockMvcResultMatchers.jsonPath("$.id").exists())
//				.andDo(print())
				.andReturn();

		String responseBody = createResult.getResponse().getContentAsString();
		Integer createdItemId = JsonPath.read(responseBody, "$.id");

		mockMvc.perform(MockMvcRequestBuilders
						.delete("/api/todos/{id}", createdItemId)
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isNoContent());
//				.andDo(print());
	}

	@Test
	@Rollback
	public void testGetAllTodos() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders
						.post("/api/todos")
						.content("{\"title\":\"Task 1\",\"description\":\"Description 1\"}")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isCreated())
//				.andDo(print())
				.andReturn();

		mockMvc.perform(MockMvcRequestBuilders
						.post("/api/todos")
						.content("{\"title\":\"Task 2\",\"description\":\"Description 2\"}")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isCreated())
//				.andDo(print())
				.andReturn();

		mockMvc.perform(MockMvcRequestBuilders
						.get("/api/todos")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].id").exists())
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].title").value("Task 1"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].description").value("Description 1"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[1].id").exists())
				.andExpect(MockMvcResultMatchers.jsonPath("$[1].title").value("Task 2"))
				.andExpect(MockMvcResultMatchers.jsonPath("$[1].description").value("Description 2"));
//				.andDo(print());
	}
}
