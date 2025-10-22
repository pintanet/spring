package ist.bdi.sna.sisna.toolbox.datarcv.model.service.xlstosql;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.util.CellReference;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;

import ist.bdi.sna.sisna.toolbox.datarcv.model.entity.ConversionGroup;
import ist.bdi.sna.sisna.toolbox.datarcv.model.entity.CustomValue;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class XlsToSqlHelperService {

	// Helper per convertire le lettere di colonna Excel (A, B, C, ...) in un indice
	// numerico (0, 1, 2, ...)
	public int getColumnIndex(String excelColumnLetter) {
		return CellReference.convertColStringToIndex(excelColumnLetter.toUpperCase());
	}

	/**
	 * Formatta il valore della cella Excel in una stringa SQL adatta al tipo
	 * Oracle.
	 */
	public String formatCellValue(Cell cell, String oracleType, FormulaEvaluator evaluator) {
		if (cell == null || cell.getCellType() == CellType.BLANK || cell.getCellType() == CellType.ERROR) {
			return "NULL";
		}

		CellType cellType = cell.getCellType();

		// 1. Gestione Formula
		if (cellType == CellType.FORMULA) {
			try {
				CellValue cellValue = evaluator.evaluate(cell);
				cellType = cellValue.getCellType();

				switch (cellType) {
				case BOOLEAN:
					return cellValue.getBooleanValue() ? "1" : "0";
				case NUMERIC:
					if (DateUtil.isCellDateFormatted(cell)) {
						return "TO_DATE('"
								+ DateUtil.getLocalDateTime(cellValue.getNumberValue(), false)
										.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
								+ "', 'YYYY-MM-DD HH24:MI:SS')";
					}
					return String.valueOf(cellValue.getNumberValue());
				case STRING:
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

		// 2. Gestione Tipi Standard
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

	/**
	 * Formatta e quota un valore stringa per l'SQL.
	 */
	private String formatStringValue(String value, String oracleType) {
		if (value == null)
			return "NULL";

		String escapedValue = value.trim().replace("'", "''");
		// Oracle in genere accetta stringhe quotate indipendentemente dal tipo (CHAR,
		// VARCHAR, CLOB)
		return "'" + escapedValue + "'";
	}

	/**
	 * Valuta un CustomValue in base al suo tipo (CONSTANT o EXPRESSION), includendo
	 * il contesto facoltativo (es. nome del file).
	 */
	public String evaluateCustomValue(CustomValue custom, Map<String, Object> contextVariables) {
		String type = custom.getValueType().toUpperCase();
		String value = custom.getValue();

		if ("CONSTANT".equals(type)) {
			return value;

		} else if ("EXPRESSION".equals(type)) {
			try {
				ExpressionParser parser = new SpelExpressionParser();
				StandardEvaluationContext context = new StandardEvaluationContext();

				// Inietta variabili di contesto aggiuntive (come #fileName)
				if (contextVariables != null) {
					contextVariables.forEach(context::setVariable);
				}

				// Registra metodi statici utili
				context.registerFunction("now", java.time.LocalDateTime.class.getMethod("now"));
				context.registerFunction("uuid", java.util.UUID.class.getMethod("randomUUID"));

				Object result = parser.parseExpression(value).getValue(context);

				if (result instanceof java.time.temporal.TemporalAccessor) {
					return "SYSDATE";
				}

				// Le stringhe da SpEL devono essere quotate
				if (result instanceof String) {
					// Evita la doppia quotatura se è già una costante stringa SpEL ('...')
					if (value.startsWith("'") && value.endsWith("'")) {
						return (String) result;
					}
					return "'" + ((String) result).replace("'", "''") + "'";
				}

				return result != null ? result.toString() : "NULL";

			} catch (Exception e) {
				log.error("Errore nella valutazione dell'espressione SpEL '{}'. Usando NULL.", value, e);
				return "NULL";
			}
		}

		return "NULL";
	}

	/**
	 * Costruisce l'istruzione INSERT finale da una mappa di colonne/valori.
	 */
	public String buildInsertStatement(ConversionGroup group, Map<String, String> columnValueMap) {
		if (columnValueMap.isEmpty()) {
			return null;
		}

		String columns = columnValueMap.keySet().stream().collect(Collectors.joining(", "));
		String values = columnValueMap.values().stream().collect(Collectors.joining(", "));

		String insertSql = String.format("INSERT INTO %s (%s) VALUES (%s)", group.getOracleTableName(), columns,
				values);

		return insertSql;
	}

	/**
	 * Genera la mappa di colonne e valori, inclusi i CustomValues. Utilizzato da
	 * entrambi i servizi di conversione.
	 */
	public Map<String, String> generateColumnValueMap(ConversionGroup group, Map<String, String> excelMappings,
			Map<String, Object> customContext) {
		Map<String, String> columnValueMap = new LinkedHashMap<>();

		// Aggiunge le mappature Excel -> Oracle
		columnValueMap.putAll(excelMappings);

		// Aggiunge i CustomValues
		for (CustomValue custom : group.getCustomValues()) {
			String evaluatedValue = evaluateCustomValue(custom, customContext);
			columnValueMap.put(custom.getOracleColumn(), evaluatedValue);
		}

		return columnValueMap;
	}
}
