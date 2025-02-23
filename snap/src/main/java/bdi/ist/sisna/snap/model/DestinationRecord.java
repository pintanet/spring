package bdi.ist.sisna.snap.model;

import java.util.Map;

import lombok.Data;

@Data
public class DestinationRecord {
	private Map<String, Object> data;
	private Long jobId;
}
