package bdi.ist.sisna.snap.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import bdi.ist.sisna.snap.model.DestinationRecord;
import bdi.ist.sisna.snap.model.TableConfig;
import bdi.ist.sisna.snap.repository.TableConfigRepository;
import bdi.ist.sisna.snap.service.CopyService;
import bdi.ist.sisna.snap.service.JobCompletionNotificationListener;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

	@Autowired
	private DataSource dataSource;

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Autowired
	private TableConfigRepository tableConfigRepository;

	@Autowired
	private CopyService copyService;

	@Autowired
	private JobRepository jobRepository;

	@Value("${batch.chunk.size}")
	private int chunkSize;

	@Value("${batch.partition.grid-size}")
	private int gridSize;

	@Value("${batch.reader.page-size}")
	private int pageSize;

	@Bean
	public Job copyTablesJob(JobCompletionNotificationListener listener) {
		return new JobBuilder("copyTablesJob", jobRepository).listener(listener).flow(partitionStep()).end().build();
	}

	@Bean
	public Step partitionStep() {
		return new StepBuilder("partitionStep", jobRepository).partitioner("copyStep", partitioner()).step(copyStep())
				.gridSize(gridSize) // Configura il livello di parallelizzazione
				.build();
	}

	@Bean
	public CustomPartitioner partitioner() {
		return new CustomPartitioner(tableConfigRepository.findAll());
	}

	@Bean
	@StepScope
	public JdbcPagingItemReader<Map<String, Object>> reader(
			@Value("#{stepExecutionContext['sourceSchema']}") String sourceSchema,
			@Value("#{stepExecutionContext['sourceTable']}") String sourceTable,
			@Value("#{jobParameters['year']}") Long year) {
		return copyService.createReader(dataSource, pageSize, sourceSchema, sourceTable, year);
	}

	@Bean
	@StepScope
	public ItemProcessor<Map<String, Object>, DestinationRecord> processor(
			@Value("#{jobParameters['year']}") Long year) {
		return copyService.createProcessor(year);
	}

	@Bean
	@StepScope
	public JdbcBatchItemWriter<DestinationRecord> writer(
			@Value("#{stepExecutionContext['destinationSchema']}") String destinationSchema,
			@Value("#{stepExecutionContext['destinationTable']}") String destinationTable,
			@Value("#{jobParameters['jobId']}") Long jobId, @Value("#{jobParameters['year']}") Long year) {
		return copyService.createWriter(dataSource, destinationSchema, destinationTable, jobId, year);
	}

	@Bean
	public Step copyStep() {
		return new StepBuilder("copyStep", jobRepository).<Map<String, Object>, DestinationRecord>chunk(chunkSize) // Dimensione
																													// dei
																													// chunk
				.reader(reader(null, null, null)).processor(processor(null)).writer(writer(null, null, null, null))
				.transactionManager(transactionManager).build();
	}

	public static class CustomPartitioner implements org.springframework.batch.core.partition.support.Partitioner {
		private final List<TableConfig> tableConfigs;

		public CustomPartitioner(List<TableConfig> tableConfigs) {
			this.tableConfigs = tableConfigs;
		}

		@Override
		public Map<String, org.springframework.batch.item.ExecutionContext> partition(int gridSize) {
			Map<String, org.springframework.batch.item.ExecutionContext> map = new HashMap<>(gridSize);
			int i = 0;
			for (TableConfig config : tableConfigs) {
				org.springframework.batch.item.ExecutionContext context = new org.springframework.batch.item.ExecutionContext();
				context.putString("sourceSchema", config.getSourceSchema());
				context.putString("sourceTable", config.getSourceTable());
				context.putString("destinationSchema", config.getDestinationSchema());
				context.putString("destinationTable", config.getDestinationTable());
				map.put("partition" + i, context);
				i++;
			}
			return map;
		}
	}
}