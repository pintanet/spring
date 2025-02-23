package bdi.ist.sisna.snap.service;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

@Component
public class JobCompletionNotificationListener implements JobExecutionListener {

	@Override
	public void beforeJob(JobExecution jobExecution) {
		// Logica da eseguire prima dell'avvio del job (opzionale)
	}

	@Override
	public void afterJob(JobExecution jobExecution) {
		if (jobExecution.getStatus().isUnsuccessful()) {
			System.out.println("JOB COMPLETATO!");
			// Qui puoi aggiungere la logica di notifica, come inviare email o aggiornare un
			// database.
		} else if (jobExecution.getStatus().isUnsuccessful()) {
			System.out.println("JOB FALLITO!");
			// Gestione dell'errore
		}
	}
}