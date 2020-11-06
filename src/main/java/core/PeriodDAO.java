package core;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface PeriodDAO extends CrudRepository<Period, Integer> {

    public Period findById(Integer id);

    @Override
    public Iterable<Period> findAll();

}
