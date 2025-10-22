package ist.bdi.sna.sisna.toolbox.datarcv.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomValue {
	private String oracleColumn;
	private String oracleType;
	private String valueType; // "CONSTANT" o "EXPRESSION"
	private String value;
}