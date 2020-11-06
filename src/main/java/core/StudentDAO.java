package core;

import java.util.Set;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface StudentDAO extends CrudRepository<Student, StudentPK> {

    @Query("select u from Student u where u.studentpk.username = ?1 and u.studentpk.degreeYear.degree.id = ?2 and u.studentpk.degreeYear.degree.calendar.year = ?3")
    public Student findByUsernameAndDegreeAndCalendarYear(String username, String dy, int year);

    @Query("select u from Student u where u.studentpk.username = ?1 and u.studentpk.degreeYear.degree.calendar.year = ?2")
    public Iterable<Student> findByUsername(String username, int year);

    @Query("select u from Student u where u.studentpk.degreeYear.degree.calendar.year = ?2 and u.name = ?1")
    public Set<Student> findByName(String name, int year);

    @Override
    public Iterable<Student> findAll();

}
