import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import it.bankitalia.poc.sisnareport.SisnaReportApplication;
import it.bankitalia.poc.sisnareport.model.entity.SimpleData;
import it.bankitalia.poc.sisnareport.model.service.SimpleDataExcelReportService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(classes = SisnaReportApplication.class)
public class SisnaReportTest {

	@Autowired
	@Qualifier("excel")
	SimpleDataExcelReportService report;

	@Test
	public void test() throws IOException {
		List<SimpleData> items = new ArrayList<>();
		items.add(SimpleData.builder().age(10).name("pippo1").rate(1.1F).build());
		items.add(SimpleData.builder().age(20).name("pippo2").rate(1.2F).build());
		items.add(SimpleData.builder().age(30).name("pippo3").rate(1.3F).build());
		items.add(SimpleData.builder().age(40).name("pippo4").rate(1.4F).build());

		List<String> template = Arrays.asList("name", "age", "rate");

		report.export(null, template, items);
	}
}
