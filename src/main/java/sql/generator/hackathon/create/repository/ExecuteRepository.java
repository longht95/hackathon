package sql.generator.hackathon.create.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ExecuteRepository<T> extends JpaRepository<T, T>{
}
