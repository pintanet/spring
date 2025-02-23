package bdi.ist.sisna.snap.test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import bdi.ist.sisna.snap.controller.JobController;

@ActiveProfiles("h2")
@WebMvcTest(JobController.class)
@Import(JobControllerTest.TestConfig.class)
public class JobControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JobLauncher jobLauncher;

	@Test
	public void testStartJob_Success() throws Exception {
		when(jobLauncher.run(any(Job.class), any(JobParameters.class))).thenReturn(new JobExecution(123L)); // Simuliamo
																											// l'esecuzione
																											// del job

		mockMvc.perform(MockMvcRequestBuilders.post("/startJob").param("year", "2024")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)).andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().string(
						org.hamcrest.Matchers.containsString("Job avviato con successo. Job ID: 123, Anno: 2024")));
	}

	@Test
	public void testStartJob_InvalidYear_TooLow() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.post("/startJob").param("year", "999")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED))
				.andExpect(MockMvcResultMatchers.status().isBadRequest()); // Aspettiamo un errore 400
	}

	@Test
	public void testStartJob_InvalidYear_TooHigh() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.post("/startJob").param("year", "10000")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED))
				.andExpect(MockMvcResultMatchers.status().isBadRequest()); // Aspettiamo un errore 400
	}

	@Test
	public void testStartJob_MissingYear() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.post("/startJob").contentType(MediaType.APPLICATION_FORM_URLENCODED))
				.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}

	@TestConfiguration // Classe di configurazione di test
	static class TestConfig {

		@Bean
		public JobLauncher jobLauncher() {
			return Mockito.mock(JobLauncher.class);
		}

		@Bean
		public Job copyTablesJob() {
			return Mockito.mock(Job.class);
		}
	}
}