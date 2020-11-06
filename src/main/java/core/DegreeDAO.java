package core;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface DegreeDAO extends CrudRepository<Degree, CalendarDegreePK> {

    @Query("select u from Degree u where u.id = ?1 and u.calendarDegreePK.calendarYear = ?2")
    public Degree findByIdAndYear(String id, int calendarYear);

    @Override
    public Iterable<Degree> findAll();

    @Query("select u from Degree u where u.acronym = ?1 and u.calendarDegreePK.calendarYear = ?2")
    public Degree findByAcronymAndYear(String acronym, int year);

}
