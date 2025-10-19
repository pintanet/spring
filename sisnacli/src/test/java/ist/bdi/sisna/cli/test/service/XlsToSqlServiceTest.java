package ist.bdi.sisna.cli.test.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ist.bdi.sisna.cli.model.entity.ColumnMapping;
import ist.bdi.sisna.cli.model.entity.ConversionGroup;
import ist.bdi.sisna.cli.model.entity.CustomValue;
import ist.bdi.sisna.cli.model.service.XlsToSqlService;

class XlsToSqlServiceTest {

	private XlsToSqlService service;
	private Workbook workbook;
	private Sheet sheet;
	private FormulaEvaluator evaluator;

	@BeforeEach
	void setUp() {
		service = new XlsToSqlService();
		workbook = new XSSFWorkbook();
		sheet = workbook.createSheet("TestSheet");
		evaluator = workbook.getCreationHelper().createFormulaEvaluator();
	}

	@Test
	void testGenerateInsert_withSimpleValues() {
		// Create test row
		Row row = sheet.createRow(0);
		row.createCell(0).setCellValue("Mario");
		row.createCell(1).setCellValue(42);

		// Mapping: A -> NOME, B -> ETA
		ColumnMapping nameMapping = new ColumnMapping();
		nameMapping.setExcelColumn("A");
		nameMapping.setOracleColumn("NOME");
		nameMapping.setOracleType("VARCHAR");

		ColumnMapping ageMapping = new ColumnMapping();
		ageMapping.setExcelColumn("B");
		ageMapping.setOracleColumn("ETA");
		ageMapping.setOracleType("NUMBER");

		ConversionGroup group = new ConversionGroup();
		group.setGroupName("testGroup");
		group.setOracleTableName("UTENTI");
		group.setMappings(Arrays.asList(nameMapping, ageMapping));
		group.setCustomValues(Collections.emptyList());

		String insert = service.generateInsert(row, group, evaluator);

		assertEquals("INSERT INTO UTENTI (NOME, ETA) VALUES ('Mario', 42)", insert);
	}

	@Test
	void testGenerateInsert_withCustomValue() {
		// Create test row
		Row row = sheet.createRow(0);
		row.createCell(0).setCellValue("Mario");

		ColumnMapping nameMapping = new ColumnMapping();
		nameMapping.setExcelColumn("A");
		nameMapping.setOracleColumn("NOME");
		nameMapping.setOracleType("VARCHAR");

		CustomValue custom = new CustomValue();
		custom.setOracleColumn("STATO");
		custom.setValueType("CONSTANT");
		custom.setValue("'ATTIVO'"); // già formattato per SQL

		ConversionGroup group = new ConversionGroup();
		group.setGroupName("testGroup");
		group.setOracleTableName("UTENTI");
		group.setMappings(List.of(nameMapping));
		group.setCustomValues(List.of(custom));

		String insert = service.generateInsert(row, group, evaluator);

		assertEquals("INSERT INTO UTENTI (NOME, STATO) VALUES ('Mario', 'ATTIVO')", insert);
	}

	@Test
	void testEvaluateCustomValue_constant() {
		CustomValue custom = new CustomValue();
		custom.setValueType("CONSTANT");
		custom.setValue("'PROVA'");

		String result = service.evaluateCustomValue(custom);
		assertEquals("'PROVA'", result);
	}

	@Test
	void testEvaluateCustomValue_expression_valid() {
		CustomValue custom = new CustomValue();
		custom.setValueType("EXPRESSION");
		custom.setValue("3 + 4");

		String result = service.evaluateCustomValue(custom);
		assertEquals("7", result);
	}

	@Test
	void testEvaluateCustomValue_expression_invalid() {
		CustomValue custom = new CustomValue();
		custom.setValueType("EXPRESSION");
		custom.setValue("thisIsNotValid()");

		String result = service.evaluateCustomValue(custom);
		assertEquals("NULL", result);
	}
}
