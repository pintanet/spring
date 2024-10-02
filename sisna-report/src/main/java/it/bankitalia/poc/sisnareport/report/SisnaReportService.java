package it.bankitalia.poc.sisnareport.report;

import java.io.IOException;

import jakarta.servlet.http.HttpServletResponse;

public interface SisnaReportService {
	void export(HttpServletResponse response, Object config, Object data) throws IOException;
}
