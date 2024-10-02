package it.bankitalia.poc.sisnareport.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimpleData {
	private String name;
	private Integer age;
	private Float rate;
}
