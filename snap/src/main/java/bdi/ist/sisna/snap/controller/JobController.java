package bdi.ist.sisna.snap.controller;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController
public class JobController {

	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	private Job copyTablesJob;

	@PostMapping("/startJob")
	public String startJob(@RequestParam @Valid @Min(1000) @Max(9999) int year) throws Exception {
		JobParameters jobParameters = new JobParametersBuilder().addLong("year", (long) year)
				.addLong("time", System.currentTimeMillis()).toJobParameters();
		JobExecution jobExecution = jobLauncher.run(copyTablesJob, jobParameters);
		return "Job avviato con successo. Job ID: " + jobExecution.getId() + ", Anno: " + year;
	}
}