package ist.bdi.sna.sisna.toolbox.datarcv.model.service.xlstosql;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import ist.bdi.sna.sisna.toolbox.datarcv.model.entity.ColumnMapping;
import ist.bdi.sna.sisna.toolbox.datarcv.model.entity.ConversionGroup;
import ist.bdi.sna.sisna.toolbox.datarcv.model.entity.ConverterConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service("xlsToSqlSheet")
@RequiredArgsConstructor
@Slf4j
public class XlsToSqlSheetService {

	// Servizio helper iniettato per le utility condivise
	private final XlsToSqlHelperService helperService;

	public void convert(ConverterConfig runtimeConfig) throws IOException {
		String profileName = runtimeConfig.getProfileName();
		// Usa excelDirectoryPath
		String directoryPath = runtimeConfig.getExcelDirectoryPath();

		log.info("Inizio conversione SHEET per profilo: {} da directory: {}", profileName, directoryPath);

		Path dir = Paths.get(directoryPath);
		if (!Files.isDirectory(dir)) {
			log.error("Directory non trovata o non valida: {}", directoryPath);
			throw new IOException("Directory non trovata: " + directoryPath);
		}

		Map<String, PrintWriter> fileWriters = new HashMap<>();

		// 1. Inizializzazione dei file di output SQL
		for (ConversionGroup group : runtimeConfig.getConversionGroups()) {
			File outputFile = new File(group.getSqlOutputFile());

			// Crea la directory padre SE necessaria (output/schede)
			File parentDir = outputFile.getParentFile();
			if (parentDir != null && !parentDir.exists()) {
				if (!parentDir.mkdirs()) {
					log.error("Impossibile creare la directory di output: {}", parentDir.getAbsolutePath());
					// Potresti voler lanciare un'eccezione qui se la directory è critica.
				} else {
					log.info("Creata directory di output: {}", parentDir.getAbsolutePath());
				}
			}
			fileWriters.put(group.getGroupName(), new PrintWriter(new FileWriter(outputFile, false)));
			log.info("Generazione file SQL per gruppo {}: {}", group.getGroupName(), outputFile.getAbsolutePath());
		}

		// 2. Ciclo sui file .xlsx nella directory
		try (Stream<Path> paths = Files.walk(dir)) {
			paths.filter(Files::isRegularFile).filter(p -> p.toString().toLowerCase().endsWith(".xlsx"))
					.forEach(filePath -> {
						log.info("Processing file: {}", filePath.getFileName());
						try {
							processSingleFile(filePath, runtimeConfig, fileWriters);
						} catch (Exception e) {
							log.error("Errore durante l'elaborazione del file {}: {}", filePath, e.getMessage());
						}
					});
		} finally {
			fileWriters.values().forEach(PrintWriter::close); // Chiusura dei file
		}

		log.info("File SQL generati con successo per il profilo: {}", profileName);
	}

	/**
	 * Processa un singolo file Excel estraendo i dati dalle celle e generando
	 * INSERT.
	 */
	private void processSingleFile(Path filePath, ConverterConfig runtimeConfig, Map<String, PrintWriter> fileWriters)
			throws IOException {
		String fileName = filePath.getFileName().toString();

		try (FileInputStream fis = new FileInputStream(filePath.toFile()); Workbook workbook = new XSSFWorkbook(fis)) {

			FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

			// Assumiamo che la mappatura a cella avvenga sul primo foglio (indice 0)
			Sheet sheet = workbook.getSheetAt(0);

			for (ConversionGroup group : runtimeConfig.getConversionGroups()) {

				String insertStatement = generateCellInsert(sheet, group, evaluator, fileName);

				if (insertStatement != null) {
					fileWriters.get(group.getGroupName()).println(insertStatement + ";");
				}
			}

		} catch (Exception e) {
			log.error("Errore POI/IO durante la lettura del file {}: {}", fileName, e.getMessage());
			throw new IOException("Errore nella lettura del file Excel: " + fileName, e);
		}
	}

	/**
	 * Genera l'istruzione INSERT per la mappatura a celle specifiche.
	 */
	private String generateCellInsert(Sheet sheet, ConversionGroup group, FormulaEvaluator evaluator, String fileName) {
		Map<String, String> excelMappings = new LinkedHashMap<>();

		// 1. Mappatura delle Celle Excel
		for (ColumnMapping mapping : group.getMappings()) {
			// Ignora le mappature a colonna (perché siamo nel servizio SHEET)
			if (mapping.getExcelCell() == null || mapping.getExcelCell().isEmpty()) {
				continue;
			}

			try {
				CellReference cellRef = new CellReference(mapping.getExcelCell());
				Row row = sheet.getRow(cellRef.getRow());
				Cell cell = (row != null) ? row.getCell(cellRef.getCol()) : null;

				// Usa il metodo helper per la formattazione del valore
				String formattedValue = helperService.formatCellValue(cell, mapping.getOracleType(), evaluator);

				excelMappings.put(mapping.getOracleColumn(), formattedValue);
			} catch (Exception e) {
				log.warn("Errore nell'elaborazione della cella Excel {} per il gruppo {}: {}", mapping.getExcelCell(),
						group.getGroupName(), e.getMessage());
				excelMappings.put(mapping.getOracleColumn(), "NULL");
			}
		}

		if (excelMappings.isEmpty())
			return null;

		// 2. Aggiunta dei CustomValues e Costruzione finale
		// Passa il nome del file per l'espressione SpEL
		Map<String, Object> customContext = Map.of("fileName", fileName);

		// Usa il metodo helper per generare la mappa completa
		Map<String, String> columnValueMap = helperService.generateColumnValueMap(group, excelMappings, customContext);

		// 3. Generazione INSERT (USA IL METODO HELPER)
		return helperService.buildInsertStatement(group, columnValueMap);
	}
}