package it.pinta.prove.springboot.data.amlmodel.repo.impl;

import java.io.File;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.pinta.prove.springboot.data.amlmodel.model.AmlModel;
import it.pinta.prove.springboot.data.amlmodel.repo.AmlModelRepo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class AmlModelRepoImpl implements AmlModelRepo {

	@Autowired
	private ObjectMapper mapper;

	@Override
	public AmlModel read(String yamlFile) {
		try {
			return mapper.readValue(new File(yamlFile), AmlModel.class);
		} catch (IOException e) {
			log.error("Exception", e);
			return null;
		}
	}

	@Override
	public void write(String yamlFile, AmlModel model) {
		try {
			mapper.writeValue(new File(yamlFile), model);
		} catch (IOException e) {
			log.error("Exception", e);
		}

	}

}
