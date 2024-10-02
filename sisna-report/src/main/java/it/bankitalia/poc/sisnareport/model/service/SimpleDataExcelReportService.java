package it.bankitalia.poc.sisnareport.model.service;

import org.springframework.stereotype.Component;

import it.bankitalia.poc.sisnareport.model.entity.SimpleData;
import it.bankitalia.poc.sisnareport.report.impl.ExcelSisnaReportService;

@Component("excel")
public class SimpleDataExcelReportService extends ExcelSisnaReportService<SimpleData> {

}
