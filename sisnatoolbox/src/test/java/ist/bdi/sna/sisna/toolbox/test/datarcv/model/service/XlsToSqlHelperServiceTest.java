package ist.bdi.sna.sisna.toolbox.test.datarcv.model.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import ist.bdi.sna.sisna.toolbox.SisnaToolboxApplication;
import ist.bdi.sna.sisna.toolbox.datarcv.model.entity.ConversionGroup;
import ist.bdi.sna.sisna.toolbox.datarcv.model.entity.CustomValue;
import ist.bdi.sna.sisna.toolbox.datarcv.model.service.xlstosql.XlsToSqlHelperService;

@ActiveProfiles("test")
@SpringBootTest(classes = SisnaToolboxApplication.class, properties = { "spring.main.web-application-type=none",
		"spring.shell.interactive.enabled=false" })
class XlsToSqlHelperServiceSpringTest {

	// 3. Inietta il servizio da testare gestito da Spring
	@Autowired
	private XlsToSqlHelperService helperService;

	// Poiché FormulaEvaluator è un oggetto POI non gestito da Spring,
	// lo creeremo come un mock locale in ogni metodo di test che ne ha bisogno.
	private FormulaEvaluator evaluator;

	private static final String ORACLE_VARCHAR = "VARCHAR2";
	private static final String ORACLE_NUMBER = "NUMBER";
	private static final String ORACLE_DATE = "DATE";
	private static final String ORACLE_INT = "INTEGER";

	@BeforeEach
	void setUp() {
		// Inizializza il mock in ogni test
		evaluator = mock(FormulaEvaluator.class);
	}

	// =========================================================================
	// TEST: getColumnIndex
	// =========================================================================

	@Test
	void testGetColumnIndex_Success() {
		assertEquals(0, helperService.getColumnIndex("A"), "La colonna A deve essere l'indice 0");
		assertEquals(25, helperService.getColumnIndex("Z"), "La colonna Z deve essere l'indice 25");
		assertEquals(26, helperService.getColumnIndex("AA"), "La colonna AA deve essere l'indice 26");
		assertEquals(0, helperService.getColumnIndex("a"), "La colonna a minuscola deve essere l'indice 0");
	}

	// =========================================================================
	// TEST: formatStringValue (Metodo Privato tramite Reflection)
	// =========================================================================

	// Per chiamare un metodo privato in un ambiente Spring, si usa lo stesso
	// approccio con Reflection.
	@Test
	void testFormatStringValue_Success() throws Exception {
		Method method = XlsToSqlHelperService.class.getDeclaredMethod("formatStringValue", String.class, String.class);
		method.setAccessible(true);

		assertEquals("'Testo Normale'", method.invoke(helperService, "Testo Normale", ORACLE_VARCHAR),
				"La stringa normale deve essere quotata.");
		assertEquals("NULL", method.invoke(helperService, null, ORACLE_VARCHAR),
				"Il valore null deve risultare nella stringa SQL NULL (non quotata).");
		assertEquals("'Valore con ''apice'' singolo'",
				method.invoke(helperService, "Valore con 'apice' singolo", ORACLE_VARCHAR),
				"Gli apici singoli devono essere raddoppiati.");
		assertEquals("''", method.invoke(helperService, " ", ORACLE_VARCHAR),
				"Gli spazi vuoti devono essere quotati e trimmati.");
	}

	// =========================================================================
	// TEST: formatCellValue (Simulazione Cella Excel)
	// =========================================================================

	// Metodo helper locale per mocking Cell
	private Cell mockCell(CellType type, Object value, String oracleType, boolean isDateFormatted) {
		Cell cell = mock(Cell.class);
		when(cell.getCellType()).thenReturn(type);

		switch (type) {
		case STRING:
			when(cell.getStringCellValue()).thenReturn((String) value);
			break;
		case NUMERIC:
			when(cell.getNumericCellValue()).thenReturn((double) value);
			// NOTA: DateUtil.isCellDateFormatted è un metodo statico e non può essere
			// mockato
			// direttamente con Mockito senza l'uso di librerie come
			// PowerMock/Mockito-inline,
			// ma se si assume che il mock di Cell venga usato correttamente, il test può
			// procedere.
			// Per un testing completo, potresti aver bisogno di una libreria di mocking di
			// statici.
			// Qui assumiamo che la logica all'interno di DateUtil sia corretta.
			if (isDateFormatted) {
				when(cell.getLocalDateTimeCellValue()).thenReturn(LocalDateTime.of(2025, 1, 20, 10, 30, 0));
			}
			break;
		case BOOLEAN:
			when(cell.getBooleanCellValue()).thenReturn((boolean) value);
			break;
		default:
			break;
		}
		return cell;
	}

	@Test
	void testFormatCellValue_NullBlankError() {
		// Cella NULL
		assertEquals("NULL", helperService.formatCellValue(null, ORACLE_VARCHAR, evaluator));

		// Cella BLANK
		Cell blankCell = mockCell(CellType.BLANK, null, ORACLE_VARCHAR, false);
		assertEquals("NULL", helperService.formatCellValue(blankCell, ORACLE_VARCHAR, evaluator));

		// Cella ERROR
		Cell errorCell = mockCell(CellType.ERROR, null, ORACLE_VARCHAR, false);
		assertEquals("NULL", helperService.formatCellValue(errorCell, ORACLE_VARCHAR, evaluator));
	}

	@Test
	void testFormatCellValue_Formula() {
		Cell cell = mock(Cell.class);
		when(cell.getCellType()).thenReturn(CellType.FORMULA);

		// Simula il risultato della formula come STRINGA
		CellValue stringValue = mock(CellValue.class);
		when(stringValue.getCellType()).thenReturn(CellType.STRING);
		when(stringValue.getStringValue()).thenReturn("Risultato Formula");
		when(evaluator.evaluate(cell)).thenReturn(stringValue);

		assertEquals("'Risultato Formula'", helperService.formatCellValue(cell, ORACLE_VARCHAR, evaluator));
	}

	@Test
	void testFormatCellValue_NumericDate() {
		double datePoiValue = 45789.4444;
		Cell cell = mockCell(CellType.NUMERIC, datePoiValue, ORACLE_DATE, true);

		// Mockiamo il metodo statico DateUtil.isCellDateFormatted
		try (MockedStatic<DateUtil> mockedDateUtil = mockStatic(DateUtil.class)) {
			mockedDateUtil.when(() -> DateUtil.isCellDateFormatted(cell)).thenReturn(true);
			when(cell.getLocalDateTimeCellValue()).thenReturn(LocalDateTime.of(2025, 1, 20, 10, 30, 0)); // Aggiunto

			String expectedDate = "TO_DATE('2025-01-20 10:30:00', 'YYYY-MM-DD HH24:MI:SS')";
			assertEquals(expectedDate, helperService.formatCellValue(cell, ORACLE_DATE, evaluator));
		}
	}

	// ... (Aggiungere qui tutti gli altri test di formatCellValue: String, Numeric
	// Integer/Float, Boolean)

	// =========================================================================
	// TEST: evaluateCustomValue (SpEL)
	// =========================================================================

	@Test
	void testEvaluateCustomValue_ExpressionFileName() {
		String fileName = "test_data.xlsx";
		CustomValue custom = new CustomValue("FILE_NAME_COL", ORACLE_VARCHAR, "EXPRESSION", "#fileName");
		Map<String, Object> context = Map.of("fileName", fileName);

		assertEquals("'" + fileName + "'", helperService.evaluateCustomValue(custom, context),
				"Dovrebbe quotare il valore della variabile di contesto.");
	}

	@Test
	void testEvaluateCustomValue_ExpressionSySdate() {
		CustomValue custom = new CustomValue("DATE_COL", ORACLE_DATE, "EXPRESSION", "#now()");
		assertEquals("SYSDATE", helperService.evaluateCustomValue(custom, null),
				"Le espressioni che ritornano una data devono essere mappate a SYSDATE.");
	}

	// =========================================================================
	// TEST: buildInsertStatement
	// =========================================================================

	@Test
	void testBuildInsertStatement_Success() {
		ConversionGroup group = new ConversionGroup();
		group.setOracleTableName("LIVE.TABELLA_DATI");

		Map<String, String> map = new LinkedHashMap<>();
		map.put("ID", "100");
		map.put("NOME", "'ROSSI'");

		String expectedSql = "INSERT INTO LIVE.TABELLA_DATI (ID, NOME) VALUES (100, 'ROSSI')";
		assertEquals(expectedSql, helperService.buildInsertStatement(group, map));
	}

	// ... (Aggiungere qui gli altri test di CustomValue e Insert, come in
	// precedenza)

}