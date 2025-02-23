package bdi.ist.sisna.snap.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import bdi.ist.sisna.snap.SnapApplication;
import bdi.ist.sisna.snap.model.DestinationRecord;
import bdi.ist.sisna.snap.service.CopyService;

@Disabled
@ActiveProfiles("h2")
@SpringBootTest(classes = SnapApplication.class)
@Sql({ "classpath:schema.sql", "classpath:data.sql" })
public class CopyServiceTest {

	@Autowired
	private CopyService copyService;

	@Autowired
	private DataSource dataSource;

	@Test
	public void testCreateReader() {
		JdbcPagingItemReader<Map<String, Object>> reader = copyService.createReader(dataSource, 100, "PUBLIC",
				"SOURCE_TABLE", 2024L);
		assertNotNull(reader);
	}

	@Test
	public void testCreateProcessor() throws Exception {
		ItemProcessor<Map<String, Object>, DestinationRecord> processor = copyService.createProcessor(2024L);
		assertNotNull(processor);

		Map<String, Object> input = new HashMap<>();
		input.put("id", 1);
		input.put("name", "Test");

		DestinationRecord result = processor.process(input);
		assertNotNull(result);
		assertEquals(2024L, result.getData().get("year"));
	}

	@Test
	public void testCreateWriter() {
		JdbcBatchItemWriter<DestinationRecord> writer = copyService.createWriter(dataSource, "PUBLIC", "DEST_TABLE", 1L,
				2024L);
		assertNotNull(writer);
	}

	@Test
	public void testGetColumns() {
		String columns = copyService.getColumns("PUBLIC", "SOURCE_TABLE");
		assertEquals("ID, NAME", columns);
	}

	@Test
	public void testGetPlaceholders() {
		String placeholders = copyService.getPlaceholders("ID, NAME");
		assertEquals("?, ?", placeholders);
	}
}