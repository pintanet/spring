package it.bankitalia.sisna.proto.xlsimport.model.entity;

import it.bankitalia.sisna.proto.xlsimport.annotaions.XImport;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SampleEntityImportDto {

	@NotBlank
	@XImport
	private Integer id;

	@NotBlank
	@XImport(caption = "Chiave")
	private String key;

	@Email
	@XImport
	private String email;

	@Digits(fraction = 2, integer = 2)
	@XImport
	private Double perc;
}
