package it.bankitalia.sisna.proto.xlsimport.importer;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;

import it.bankitalia.sisna.proto.xlsimport.annotaions.XImport;
import it.bankitalia.sisna.proto.xlsimport.exception.XImportFormatException;
import it.bankitalia.sisna.proto.xlsimport.exception.XImportMappingException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class XImportMapper<T> {

	final private Map<String, Short> mapping = new HashMap<>();

	private Class<T> clazz;

	@SuppressWarnings("unchecked")
	public XImportMapper() {
		this.clazz = ((Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0]);
	}

	public void init(Row header) throws XImportFormatException {
		short xImportCount = 0;

		for (Field field : clazz.getDeclaredFields()) {

			if (field.isAnnotationPresent(XImport.class)) {

				xImportCount++;

				Short captionIndex = findIndexByCaption(header, field);

				if (captionIndex != -1)
					mapping.put(field.getName(), captionIndex);
			}
		}

		if (mapping.keySet().size() != xImportCount)
			throw new XImportFormatException("Wrong File Format");
	}

	public T map(Row row) throws XImportMappingException {

		T out = null;

		String property = "";
		Short index = -1;

		try {
			out = clazz.getDeclaredConstructor().newInstance();

			for (Entry<String, Short> map : mapping.entrySet()) {

				property = map.getKey();
				index = map.getValue();

				PropertyAccessor accessor = PropertyAccessorFactory.forBeanPropertyAccess(out);

				Class<?> type = accessor.getPropertyType(property);

				Cell cell = row.getCell(index);

				if (type.equals(String.class)) {
					accessor.setPropertyValue(property, cell.getStringCellValue());
				} else if (type.equals(Integer.class) || type.equals(Short.class) || type.equals(Number.class)
						|| type.equals(Float.class) || type.equals(Double.class)) {
					accessor.setPropertyValue(property, cell.getNumericCellValue());
				} else if (type.equals(Boolean.class)) {
					accessor.setPropertyValue(property, cell.getBooleanCellValue());
				} else if (type.equals(Date.class)) {
					accessor.setPropertyValue(property, cell.getDateCellValue());
				} else if (type.equals(LocalDateTime.class)) {
					accessor.setPropertyValue(property, cell.getLocalDateTimeCellValue());
				} else {
					throw new IllegalArgumentException("Cell value not assignable");
				}

			}

		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new XImportMappingException(String.format("Mapping error: field %s - index %d", property, index), e);
		}

		return out;
	}

	private short findIndexByCaption(Row header, Field field) {
		short out = -1;

		XImport annotation = field.getAnnotation(XImport.class);
		String caption = annotation.caption().isBlank() ? field.getName() : annotation.caption();

		boolean isFound = false;
		short iStart = header.getFirstCellNum();
		short iStop = header.getLastCellNum();

		for (short i = iStart; i < iStop; i++) {

			try {
				isFound = header.getCell(i).getStringCellValue().equals(caption);
			} catch (Exception ex) {
				isFound = false;
			}

			if (isFound) {
				out = i;
				break;
			}
		}

		return out;
	}
}
