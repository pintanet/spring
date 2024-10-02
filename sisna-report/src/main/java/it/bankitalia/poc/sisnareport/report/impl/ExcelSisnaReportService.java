//https://denitiawan.medium.com/create-rest-api-for-export-data-to-excel-and-pdf-using-springboot-38a2ee6c73a0
package it.bankitalia.poc.sisnareport.report.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.poi.hpsf.Date;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import it.bankitalia.poc.sisnareport.report.SisnaReportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component("excel")
public abstract class ExcelSisnaReportService<T> implements SisnaReportService {

	protected XSSFWorkbook workbook;
	protected XSSFSheet sheet;

	@Override
	public void export(HttpServletResponse response, Object template, Object data) throws IOException {
		newReportExcel();

		// response writer to excel
//		response = initResponseForExportExcel(response, "UserExcel");
//		ServletOutputStream outputStream = response.getOutputStream();
		FileOutputStream outputStream = new FileOutputStream(new File("prova.xls"));

		// write sheet, title & header
//		String[] headers = new String[] { "No", "username", "Password", "Roles", "Permission", "Active", "Bloked",
//				"Created By", "Created Date", "Update By", "Update Date" };
		String[] headers = ((List<String>) template).toArray(new String[0]);
		writeTableHeaderExcel("Sheet User", "Report User", headers);

		// write content row
		writeTableData(template, data);

		workbook.write(outputStream);
		workbook.close();
		outputStream.close();
	}

	protected void writeTableData(Object config, Object data) {
		List<T> items = (List<T>) data;
		List<String> fields = (List<String>) config;

		// font style content
		CellStyle style = getFontContentExcel();

		// starting write on row
		int startRow = 2;

		// write content
		for (T item : items) {
			Row row = sheet.createRow(startRow++);
			int columnCount = 0;
			for (String field : fields) {
				Object value = null;
				try {
					String getter = String.format("get%s", StringUtils.capitalize(field));
					Method method = item.getClass().getMethod(getter);
					value = method.invoke(item);
					log.info("Field {} = {}", getter, value);
				} catch (Exception ex) {
					value = "";
					log.error("Errore", ex);
				} finally {
					createCell(row, columnCount++, value, style);
				}

			}
		}
	}

	protected void newReportExcel() {
		workbook = new XSSFWorkbook();
	}

	protected HttpServletResponse initResponseForExportExcel(HttpServletResponse response, String fileName) {
		response.setContentType("application/octet-stream");
		DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd:hh:mm:ss");
		String currentDateTime = dateFormatter.format(new Date());

		String headerKey = "Content-Disposition";
		String headerValue = "attachment; filename=" + fileName + "_" + currentDateTime + ".xlsx";
		response.setHeader(headerKey, headerValue);
		return response;
	}

	protected void writeTableHeaderExcel(String sheetName, String titleName, String[] headers) {

		// sheet
		sheet = workbook.createSheet(sheetName);
		org.apache.poi.ss.usermodel.Row row = sheet.createRow(0);
		CellStyle style = workbook.createCellStyle();
		XSSFFont font = workbook.createFont();
		font.setBold(true);
		font.setFontHeight(20);
		style.setFont(font);
		style.setAlignment(HorizontalAlignment.CENTER);

//		// title
//		createCell(row, 0, titleName, style);
//		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, headers.length - 1));
//		font.setFontHeightInPoints((short) 10);

		// header
		row = sheet.createRow(1);
		font.setBold(true);
		font.setFontHeight(16);
		style.setFont(font);
		for (int i = 0; i < headers.length; i++) {
			createCell(row, i, headers[i], style);
		}
	}

	protected void createCell(org.apache.poi.ss.usermodel.Row row, int columnCount, Object value, CellStyle style) {
		sheet.autoSizeColumn(columnCount);
		org.apache.poi.ss.usermodel.Cell cell = row.createCell(columnCount);
		if (value instanceof Integer) {
			cell.setCellValue((Integer) value);
		} else if (value instanceof Float) {
			cell.setCellValue((Float) value);
		} else if (value instanceof Double) {
			cell.setCellValue((Double) value);
		} else if (value instanceof Boolean) {
			cell.setCellValue((Boolean) value);
		} else if (value instanceof Long) {
			cell.setCellValue((Long) value);
		} else {
			cell.setCellValue((String) value);
		}
		log.info("Value {}", value);
		cell.setCellStyle(style);
	}

	protected CellStyle getFontContentExcel() {
		CellStyle style = workbook.createCellStyle();
		XSSFFont font = workbook.createFont();
		font.setFontHeight(14);
		style.setFont(font);
		return style;
	}
}
