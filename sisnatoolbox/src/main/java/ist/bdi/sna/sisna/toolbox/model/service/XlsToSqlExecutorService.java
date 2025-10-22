package ist.bdi.sna.sisna.toolbox.model.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ist.bdi.sna.sisna.toolbox.model.entity.ConverterConfig;

@Service
public class XlsToSqlExecutorService {

	@Autowired
	private XlsToSqlRowService xlsToSqlRowService; // Per profile-type: ROW

	@Autowired
	private XlsToSqlSheetService xlsToSqlSheetService; // Per profile-type: SHEET

	public void execute(ConverterConfig config, boolean dryRun, String username, String password) throws IOException {

		if ("SHEET".equalsIgnoreCase(config.getProfileType())) {
			xlsToSqlSheetService.convert(config);
		} else {
			xlsToSqlRowService.convert(config);
		}
	}
}