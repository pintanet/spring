package ist.bdi.sna.sisna.toolbox.datarcv.model.service.xlstosql;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import ist.bdi.sna.sisna.toolbox.datarcv.model.entity.ColumnMapping;
import ist.bdi.sna.sisna.toolbox.datarcv.model.entity.ConversionGroup;
import ist.bdi.sna.sisna.toolbox.datarcv.model.entity.ConverterConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service("xlsToSqlRow")
@RequiredArgsConstructor
@Slf4j
public class XlsToSqlRowService {

	// Servizio helper iniettato per le utility condivise
	private final XlsToSqlHelperService helperService;

	public void convert(ConverterConfig runtimeConfig) throws IOException {
		String profileName = runtimeConfig.getProfileName();
		log.info("Inizio conversione ROW per profilo: {}", profileName);

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
				log.info("Generazione file SQL per gruppo {}: {}", group.getGroupName(), group.getSqlOutputFile());
			}

			// Ciclo sulle righe a partire da startRow
			for (int r = startRow; r <= sheet.getLastRowNum(); r++) {
				Row row = sheet.getRow(r);
				if (row == null)
					continue;

				for (ConversionGroup group : runtimeConfig.getConversionGroups()) {
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

	/**
	 * Genera una singola istruzione INSERT per la riga basata sul mapping per
	 * colonna.
	 */
	public String generateInsert(Row row, ConversionGroup group, FormulaEvaluator evaluator) {
		Map<String, String> excelMappings = new LinkedHashMap<>();

		// 1. Mappatura delle Colonne Excel
		for (ColumnMapping mapping : group.getMappings()) {
			// Ignora le mappature a cella singola (perché siamo nel servizio ROW)
			if (mapping.getExcelColumn() == null)
				continue;

			try {
				int colIndex = helperService.getColumnIndex(mapping.getExcelColumn());
				Cell cell = row.getCell(colIndex);

				// Usa il metodo helper per la formattazione del valore
				String formattedValue = helperService.formatCellValue(cell, mapping.getOracleType(), evaluator);

				excelMappings.put(mapping.getOracleColumn(), formattedValue);
			} catch (Exception e) {
				log.warn("Errore nell'elaborazione della colonna Excel {} (riga {}) per il gruppo {}: {}",
						mapping.getExcelColumn(), row.getRowNum() + 1, group.getGroupName(), e.getMessage());
				excelMappings.put(mapping.getOracleColumn(), "NULL");
			}
		}

		// Se non ci sono mappature valide, non generare nulla
		if (excelMappings.isEmpty())
			return null;

		// 2. Aggiunta dei CustomValues e Costruzione finale (nessun contesto aggiuntivo
		// richiesto)
		Map<String, String> columnValueMap = helperService.generateColumnValueMap(group, excelMappings, null);

		// 3. Generazione INSERT (USA IL METODO HELPER)
		return helperService.buildInsertStatement(group, columnValueMap);
	}
}