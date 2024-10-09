package it.bankitalia.sisna.proto.xlsimport.model.importer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.bankitalia.sisna.proto.xlsimport.importer.XImportReader;
import it.bankitalia.sisna.proto.xlsimport.model.entity.SampleEntityImportDto;

@Component
public class SampleEntityXImportReader extends XImportReader<SampleEntityImportDto> {

	public SampleEntityXImportReader(@Autowired SampleEntityXImportMapper mapper) {
		super(mapper);
	}
}