package bdi.ist.sisna.snap.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import bdi.ist.sisna.snap.model.TableConfig;

@Repository
public interface TableConfigRepository extends JpaRepository<TableConfig, Long> {
}