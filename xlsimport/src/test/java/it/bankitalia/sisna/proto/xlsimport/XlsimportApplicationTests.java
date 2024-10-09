package it.bankitalia.sisna.proto.xlsimport;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import it.bankitalia.sisna.proto.xlsimport.exception.XImportReadException;
import it.bankitalia.sisna.proto.xlsimport.model.entity.SampleEntityImportDto;
import it.bankitalia.sisna.proto.xlsimport.model.importer.SampleEntityXImportReader;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(classes = XlsimportApplication.class)
class XlsimportApplicationTests {

	@Autowired
	SampleEntityXImportReader sampleEntityXImportReader;

	@Test
	void contextLoads() {
	}

	@Test
	void loadExcel() throws XImportReadException {
		String file = "src/test/resources/prova1.xlsx";

		List<SampleEntityImportDto> out = sampleEntityXImportReader.read(file);

		out.stream().forEach(s -> log.info(s.toString()));
	}

}
