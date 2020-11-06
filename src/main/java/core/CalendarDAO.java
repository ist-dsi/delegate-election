package core;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface CalendarDAO extends CrudRepository<Calendar, Integer> {

    public Calendar findByYear(int year);

    @Override
    public Iterable<Calendar> findAll();

    public Calendar findFirstByOrderByYearDesc();

}
