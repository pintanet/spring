package it.pinta.prove.springboot.data.amlmodel.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Flusso {

	private String nome;
	private String sorgente;
	private String condizione;
}
