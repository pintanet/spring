package bdi.ist.sisna.snap.service;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.sql.DataSource;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import bdi.ist.sisna.snap.model.DestinationRecord;

@Service
public class CopyService {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public JdbcPagingItemReader<Map<String, Object>> createReader(DataSource sourceDataSource, int pageSize,
			String sourceSchema, String sourceTable, Long year) {
		JdbcPagingItemReader<Map<String, Object>> reader = new JdbcPagingItemReader<>();
		reader.setDataSource(sourceDataSource);
		reader.setPageSize(pageSize);

		SqlPagingQueryProviderFactoryBean queryProviderFactoryBean = new SqlPagingQueryProviderFactoryBean();
		queryProviderFactoryBean.setDataSource(sourceDataSource);
		queryProviderFactoryBean.setSelectClause("*");
		queryProviderFactoryBean.setFromClause(sourceSchema + "." + sourceTable);

		queryProviderFactoryBean.setSortKeys(Map.of("idSna", Order.ASCENDING));

		reader.setRowMapper(new RowMapper<Map<String, Object>>() {
			@Override
			public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
				Map<String, Object> row = new HashMap<>();
				// Mappa i risultati del ResultSet nella mappa
				for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
					row.put(rs.getMetaData().getColumnName(i), rs.getObject(i));
				}
				return row;
			}
		});

		try {
			reader.setQueryProvider(queryProviderFactoryBean.getObject());
		} catch (Exception e) {
			// Gestione dell'eccezione
		}

		return reader;
	}

	public ItemProcessor<Map<String, Object>, DestinationRecord> createProcessor(Long jobId, Long year) {
		return item -> {
			DestinationRecord record = new DestinationRecord();
			record.setData(item);
			record.getData().put("job_id", jobId);
//			record.getData().put("year", year);
			return record;
		};
	}

	public JdbcBatchItemWriter<DestinationRecord> createWriter(DataSource destinationDataSource,
			String destinationSchema, String destinationTable, Long jobId, Long year) {
		JdbcBatchItemWriter<DestinationRecord> writer = new JdbcBatchItemWriter<>();
		writer.setDataSource(destinationDataSource);

		String columns = getColumns(destinationSchema, destinationTable);
		String placeholders = getPlaceholders(columns);
		List<String> keys = Arrays.asList(columns.replaceAll(" ", "").toUpperCase().split(","));

//		writer.setSql("INSERT INTO " + destinationSchema + "." + destinationTable + " (" + columns
//				+ ", job_id) VALUES (" + placeholders + ", ?)");

		writer.setSql("INSERT INTO " + destinationSchema + "." + destinationTable + " (" + columns + ") VALUES ("
				+ placeholders + ")");

		writer.setItemPreparedStatementSetter((item, ps) -> {
			Map<String, Object> data = item.getData();

			for (Entry<String, Object> entry : data.entrySet()) {
				int iPar = keys.indexOf(entry.getKey().toUpperCase()) + 1;
				ps.setObject(iPar, entry.getValue());
			}
		});

		return writer;
	}

	public String getColumns(String schema, String table) {
		return jdbcTemplate.query("SELECT * FROM " + schema + "." + table + " WHERE 1=0", rs -> {
			ResultSetMetaData metaData = rs.getMetaData();
			try {
				return IntStream.rangeClosed(1, metaData.getColumnCount()).mapToObj(i -> {
					try {
						return metaData.getColumnName(i);
					} catch (SQLException e) {
						// Gestisci l'eccezione, ad esempio loggandola o restituendo un valore di
						// fallback
						e.printStackTrace(); // Stampa la stack trace per il debug
						return "UNKNOWN_COLUMN"; // Valore di fallback
					}
				}).collect(Collectors.joining(", "));
			} catch (SQLException e) {
				// Gestisci l'eccezione esterna
				e.printStackTrace();
				return ""; // Restituisci una stringa vuota o un valore di fallback
			}
		});
	}

	public String getPlaceholders(String columns) {
		return IntStream.range(0, columns.split(",").length).mapToObj(i -> "?").collect(Collectors.joining(", "));
	}
}