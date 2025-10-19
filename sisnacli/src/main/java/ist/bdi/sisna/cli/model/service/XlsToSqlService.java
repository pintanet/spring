package ist.bdi.sisna.cli.model.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;

import ist.bdi.sisna.cli.model.entity.ColumnMapping;
import ist.bdi.sisna.cli.model.entity.ConversionGroup;
import ist.bdi.sisna.cli.model.entity.ConverterConfig;
import ist.bdi.sisna.cli.model.entity.CustomValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service("xlsToSql")
@RequiredArgsConstructor
@Slf4j
public class XlsToSqlService {

	// Helper per convertire le lettere di colonna Excel (A, B, C, ...) in un indice
	// numerico (0, 1, 2, ...)
	private static int getColumnIndex(String excelColumnLetter) {
		// Uso della classe Jave POI per la conversione.
		return CellReference.convertColStringToIndex(excelColumnLetter.toUpperCase());
	}

	public void convert(ConverterConfig runtimeConfig) throws IOException {
		String profileName = runtimeConfig.getProfileName();
		log.info("Inizio conversione per profilo: {}", profileName);

		int startRow = runtimeConfig.isHasHeaderRow() ? 1 : 0;
		log.info("La generazione delle INSERT inizierà dalla riga {} (Header: {}).", startRow,
				runtimeConfig.isHasHeaderRow());

		Map<String, PrintWriter> fileWriters = new HashMap<>();

		try (FileInputStream fis = new FileInputStream(runtimeConfig.getExcelFilePath());
				Workbook workbook = new XSSFWorkbook(fis)) {

			FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

			Sheet sheet = workbook.getSheetAt(0);

			// Inizializzazione dei file di output SQL
			for (ConversionGroup group : runtimeConfig.getConversionGroups()) {
				File outputFile = new File(group.getSqlOutputFile());
				outputFile.getParentFile().mkdirs();
				// Utilizza FileWriter per sovrascrivere o creare il file
				fileWriters.put(group.getGroupName(), new PrintWriter(new FileWriter(outputFile, false)));
				log.info("Generazione file SQL: {}", group.getSqlOutputFile());
			}

			// Ciclo sulle righe a partire da startRow
			for (int r = startRow; r <= sheet.getLastRowNum(); r++) {
				Row row = sheet.getRow(r);
				if (row == null)
					continue;

				for (ConversionGroup group : runtimeConfig.getConversionGroups()) {
					// AGGIORNA: Passa l'evaluator a generateInsert
					String insertStatement = generateInsert(row, group, evaluator);
					if (insertStatement != null) {
						fileWriters.get(group.getGroupName()).println(insertStatement + ";");
					}
				}
			}

			log.info("File SQL generati con successo per il profilo: {}", profileName);

		} catch (FileNotFoundException e) {
			log.error("File Excel non trovato al percorso: {}", runtimeConfig.getExcelFilePath());
			throw e;
		} finally {
			fileWriters.values().forEach(PrintWriter::close);
		}
	}

	// --------------------------------------------------------------------------
	// METODI DI SUPPORTO
	// --------------------------------------------------------------------------

	/**
	 * Genera una singola istruzione INSERT per la riga e il gruppo specificati.
	 */
	public String generateInsert(Row row, ConversionGroup group, FormulaEvaluator evaluator) { // 1. Definisci liste
																								// per i nomi delle
																								// colonne e i valori
		Map<String, String> columnValueMap = new LinkedHashMap<>();

		// 2. Mappa le colonne Excel
		for (ColumnMapping mapping : group.getMappings()) {
			try {
				int colIndex = getColumnIndex(mapping.getExcelColumn());
				Cell cell = row.getCell(colIndex);
				String formattedValue = formatCellValue(cell, mapping.getOracleType(), evaluator);

				columnValueMap.put(mapping.getOracleColumn(), formattedValue);
			} catch (Exception e) {
				log.warn("Errore nell'elaborazione della colonna Excel {} (riga {}) per il gruppo {}: {}",
						mapping.getExcelColumn(), row.getRowNum() + 1, group.getGroupName(), e.getMessage());
				// Se una colonna richiesta fallisce, potresti voler saltare l'intera riga.
				// Per ora, registriamo un valore NULL.
				columnValueMap.put(mapping.getOracleColumn(), "NULL");
			}
		}

		// 3. Aggiungi i valori personalizzati
		for (CustomValue custom : group.getCustomValues()) {
			String evaluatedValue = evaluateCustomValue(custom);
			columnValueMap.put(custom.getOracleColumn(), evaluatedValue);
		}

		// 4. Costruisci l'istruzione INSERT

		// Nomi delle colonne (JOINED_COLUMNS)
		String columns = columnValueMap.keySet().stream().collect(Collectors.joining(", "));

		// Valori delle colonne (JOINED_VALUES)
		String values = columnValueMap.values().stream().collect(Collectors.joining(", "));

		// Formato SQL Oracle: INSERT INTO TABELLA (COLONNE) VALUES (VALORI)
		String insertSql = String.format("INSERT INTO %s (%s) VALUES (%s)", group.getOracleTableName(), columns,
				values);

		return insertSql;
	}

	/**
	 * Formatta il valore della cella Excel in una stringa SQL adatta al tipo
	 * Oracle. Ora accetta FormulaEvaluator per gestire le formule.
	 */
	private String formatCellValue(Cell cell, String oracleType, FormulaEvaluator evaluator) {
		if (cell == null || cell.getCellType() == CellType.BLANK || cell.getCellType() == CellType.ERROR) {
			return "NULL";
		}

		CellType cellType = cell.getCellType();

		// *************************************************************
		// CORREZIONE DEL BLOCCO FORMULA
		// *************************************************************
		if (cellType == CellType.FORMULA) {
			try {
				// Valuta la formula per ottenere il risultato come CellValue
				CellValue cellValue = evaluator.evaluate(cell);
				cellType = cellValue.getCellType(); // Aggiorna il tipo al risultato della formula

				// Per il resto della logica, lavoriamo con il CellValue e il suo tipo
				// Se il CellValue ha un tipo diverso (es. NUMERIC), useremo i suoi valori.

				// Gestione dei tipi del CellValue
				switch (cellType) {
				case BOOLEAN:
					return cellValue.getBooleanValue() ? "1" : "0";
				case NUMERIC:
					// Se la formula produce una data
					if (DateUtil.isCellDateFormatted(cell)) {
						return "TO_DATE('"
								+ DateUtil.getLocalDateTime(cellValue.getNumberValue(), false)
										.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
								+ "', 'YYYY-MM-DD HH24:MI:SS')";
					}
					// Altrimenti, un numero normale
					return String.valueOf(cellValue.getNumberValue());
				case STRING:
					// Continua con la logica standard per le stringhe
					return formatStringValue(cellValue.getStringValue(), oracleType);
				default:
					return "NULL";
				}
			} catch (Exception e) {
				log.warn("Impossibile valutare la formula a riga {}, colonna {}. Usando NULL.", cell.getRowIndex() + 1,
						cell.getColumnIndex() + 1);
				return "NULL";
			}
		}
		// *************************************************************
		// FINE CORREZIONE
		// *************************************************************

		// Logica standard per i tipi non-formula

		switch (cellType) {
		case STRING:
			return formatStringValue(cell.getStringCellValue(), oracleType);

		case NUMERIC:
			if (DateUtil.isCellDateFormatted(cell)) {
				// DATE/TIMESTAMP
				return "TO_DATE('"
						+ cell.getLocalDateTimeCellValue()
								.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
						+ "', 'YYYY-MM-DD HH24:MI:SS')";
			}
			// NUMBER, FLOAT, INTEGER
			double numValue = cell.getNumericCellValue();
			if (oracleType.toUpperCase().contains("NUMBER") || oracleType.toUpperCase().contains("INT")
					|| oracleType.toUpperCase().contains("FLOAT")) {
				if (numValue == Math.floor(numValue)) {
					return String.valueOf((long) numValue);
				}
			}
			return String.valueOf(numValue);

		case BOOLEAN:
			return cell.getBooleanCellValue() ? "1" : "0";

		default:
			return "NULL";
		}
	}

	// Metodo helper estratto per chiarezza e riutilizzo
	private String formatStringValue(String value, String oracleType) {
		if (value == null)
			return "NULL";

		String typeUpper = oracleType.toUpperCase();
		String escapedValue = value.trim().replace("'", "''");

		if (typeUpper.contains("CHAR") || typeUpper.contains("TEXT") || typeUpper.contains("CLOB")) {
			return "'" + escapedValue + "'";
		}
		return "'" + escapedValue + "'";
	}

	/**
	 * Valuta un CustomValue in base al suo tipo (CONSTANT o EXPRESSION).
	 */
	public String evaluateCustomValue(CustomValue custom) {
		String type = custom.getValueType().toUpperCase();
		String value = custom.getValue();

		if ("CONSTANT".equals(type)) {
			return value;

		} else if ("EXPRESSION".equals(type)) {
			try {
				ExpressionParser parser = new SpelExpressionParser();
				StandardEvaluationContext context = new StandardEvaluationContext();

				// Registrare metodi statici utili se servono (es. LocalDateTime.now())
				context.registerFunction("now", java.time.LocalDateTime.class.getMethod("now"));
				context.registerFunction("uuid", java.util.UUID.class.getMethod("randomUUID"));

				Object result = parser.parseExpression(value).getValue(context);

				if (result instanceof java.time.temporal.TemporalAccessor) {
					return "SYSDATE";
				}

				return result != null ? result.toString() : "NULL";

			} catch (Exception e) {
				log.error("Errore nella valutazione dell'espressione SpEL '{}'. Usando NULL.", value, e);
				return "NULL";
			}
		}

		return "NULL";
	}
}