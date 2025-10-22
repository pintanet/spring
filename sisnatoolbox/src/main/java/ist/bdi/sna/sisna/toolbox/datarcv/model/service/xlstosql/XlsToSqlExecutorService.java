package ist.bdi.sna.sisna.toolbox.datarcv.model.service.xlstosql;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ist.bdi.sna.sisna.toolbox.datarcv.model.entity.ConverterConfig;

@Service
public class XlsToSqlExecutorService {

	@Autowired
	private XlsToSqlRowService xlsToSqlRowService;

	@Autowired
	private XlsToSqlSheetService xlsToSqlSheetService;

	public void execute(ConverterConfig config, boolean dryRun, String username, String password) throws IOException {

		if ("SHEET".equalsIgnoreCase(config.getProfileType())) {
			xlsToSqlSheetService.convert(config);
		} else if ("ROW".equalsIgnoreCase(config.getProfileType())) {
			xlsToSqlRowService.convert(config);
		} else {
			throw new IllegalArgumentException("Profile type non riconosciuto: " + config.getProfileType()
					+ ". Valori accettati sono 'ROW' o 'SHEET'.");
		}
	}
}