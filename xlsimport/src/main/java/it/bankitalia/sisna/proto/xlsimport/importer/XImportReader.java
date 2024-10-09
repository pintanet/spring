package it.bankitalia.sisna.proto.xlsimport.importer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;

import it.bankitalia.sisna.proto.xlsimport.exception.XImportReadException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class XImportReader<T> {

	protected XImportMapper<T> xImportMapper;

	public List<T> read(String file) throws XImportReadException {

		List<T> out = new ArrayList<>();

		try (Workbook workbook = WorkbookFactory.create(new File(file))) {

			Sheet sheet = workbook.getSheetAt(0);

			int index = 0;
			for (Row row : sheet) {

				if (index == 0) {
					xImportMapper.init(row);
				} else {
					T item = xImportMapper.map(row);
					out.add(item);
				}
				index++;
			}

		} catch (Throwable e) {
			throw new XImportReadException(e);
		}

		return out;

	}

}
