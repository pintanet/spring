package it.bankitalia.sisna.proto.xlsimport.model.importer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class SampleEntityService {

	@Autowired
	private SampleEntityXImportReader reader;

	@Autowired
	private SampleEntityXImportValidator validator;

	@Autowired
	private SampleEntityRepository repo;

}
