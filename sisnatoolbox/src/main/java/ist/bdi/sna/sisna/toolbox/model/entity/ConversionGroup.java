package ist.bdi.sna.sisna.toolbox.model.entity;

import java.util.Collections;
import java.util.List;

import lombok.Data;

@Data
public class ConversionGroup {
	private String groupName;
	private String sqlOutputFile;
	private String oracleTableName;
	private List<ColumnMapping> mappings;
	private List<CustomValue> customValues = Collections.emptyList();
}
