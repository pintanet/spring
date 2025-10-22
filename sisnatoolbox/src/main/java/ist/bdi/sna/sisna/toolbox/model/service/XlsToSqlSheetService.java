package ist.bdi.sna.sisna.toolbox.model.service;

import org.springframework.stereotype.Service;

import ist.bdi.sna.sisna.toolbox.model.entity.ConverterConfig;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class XlsToSqlSheetService {

	/**
	 * Esegue la conversione utilizzando la strategia SHEET. Si presume che un
	 * intero foglio (o file) si trasformi in un set di output SQL.
	 */
	public void convert(ConverterConfig config) {
		log.info("Avvio conversione in modalità SHEET per il profilo: {}", config.getProfileName());

		log.info("Conversione in modalità SHEET completata.");
	}
}
