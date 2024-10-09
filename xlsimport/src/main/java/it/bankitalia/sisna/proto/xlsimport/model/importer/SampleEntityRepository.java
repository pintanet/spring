package it.bankitalia.sisna.proto.xlsimport.model.importer;

import org.springframework.stereotype.Component;

import it.bankitalia.sisna.proto.xlsimport.importer.XImportRepository;
import it.bankitalia.sisna.proto.xlsimport.model.entity.SampleEntityImportDto;

@Component
public class SampleEntityRepository extends XImportRepository<SampleEntityImportDto, Integer> {

}
