package ist.bdi.sisna.cli.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ColumnMapping {
	private String excelColumn;
	private String oracleColumn;
	private String oracleType;
}