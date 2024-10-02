package it.pinta.prove.springboot.data.amlmodel.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AmlModel {
	private Integer anno;
	private Integer version;
	private Integer revision;
//	@JsonIgnore
	private LocalDateTime time;

	@Builder.Default
	private List<Flusso> flusso = new ArrayList<Flusso>();

	private List<String> prova;
}
