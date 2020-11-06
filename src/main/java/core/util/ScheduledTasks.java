package core.util;

import java.time.LocalDate;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import core.Calendar;
import core.CalendarDAO;
import core.Degree;
import core.DegreeDAO;
import core.DegreeYear;
import core.Period;
import core.PeriodDAO;
import core.Student;
import endpoint.AccessTokenHandler;

@Component
public class ScheduledTasks {

    Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);

    @Autowired
    DegreeDAO degreeDAO;

    @Autowired
    CalendarDAO calendarDAO;

    @Autowired
    PeriodDAO periodDAO;

    @Autowired
    AccessTokenHandler accessTokenHandler;

    // Isto assume que a app está sempre a correr.

//    @Scheduled(cron = "0/5 * * * * *")
    @Scheduled(cron = "0 0 1 * * *")   //at 1 am
    @Transactional
    public void updateCurrentPeriods() {
        // Obtem calendario actual
        Calendar calendar = calendarDAO.findFirstByOrderByYearDesc();
        if (calendar == null) {
            return;
        }

        Set<Degree> degrees = calendar.getDegrees();
        if (degrees == null) {
            return;
        }

        for (final Degree degree : degrees) {
            for (final DegreeYear degreeYear : degree.getYears()) {
                try {
                    Set<Student> candidates = null;
                    // Em cada degreeYear, verifica se o currentPeriod ja terminou
                    Period activePeriod = degreeYear.getActivePeriod();
                    if (activePeriod != null) {
                        if (activePeriod.getEnd().isBefore(LocalDate.now())) {
                            // Se terminou, tira esse de activo
                            activePeriod.setInactive();
                            candidates = activePeriod.getCandidates();
                            periodDAO.save(activePeriod);
                        } else {
                            // Se nao terminou, continua para o proximo degreeYear
                            continue;
                        }
                    }
                    // Depois verifica se há algum para entrar em vigor no dia actual, caso haja, coloca-o como activo
                    Period newActivePeriod = degreeYear.getNextPeriod(LocalDate.now());
                    if (newActivePeriod != null && newActivePeriod.getStart().isEqual(LocalDate.now())) {
                        degreeYear.setActivePeriod(newActivePeriod);
                        degreeYear.setStudentsLoaded(true);
                        if (candidates != null) {
                            newActivePeriod.setCandidates(candidates);
                        } else {
                            Period lastPeriod = degreeYear.getLastPeriod(LocalDate.now());
                            if (lastPeriod != null && lastPeriod.getCandidates() != null) {
                                newActivePeriod.setCandidates(lastPeriod.getCandidates());
                            }
                        }
                        periodDAO.save(newActivePeriod);
                    }
                } catch (Exception e) {
                    // Devia guardar excepcao no log
                    // Desta forma, ainda que o processamento de um ano/curso falhe, apenas esse fica por processar
                }
            }
        }
    }

    @Scheduled(cron = "0 30 0 * * *") // at 00:30 am
    @Transactional
    public void retrieveStudents() throws Exception {

        Calendar calendar = calendarDAO.findFirstByOrderByYearDesc();
        if (calendar == null) {
            return;
        }

        Set<Degree> degrees = calendar.getDegrees();
        if (degrees == null) {
            return;
        }

        logger.info("Run retrieveStudents for {}", calendar.getYear());
        
        for (Degree degree : degrees) {
            for (DegreeYear degreeYear : degree.getYears()) {
                try {
                    accessTokenHandler.initStudents(degreeYear, degree);
                } catch (Exception e) {
                    logger.error("Exception while retrieving students", e);
                }
            }
        }
        degreeDAO.save(degrees);

    }

    @Scheduled(cron = "0 59 23 31 8 *") // on 31/08 at 23:59
    @Transactional
    public void createCalendar() {
        final Calendar calendar = new Calendar(LocalDate.now().getYear());
        accessTokenHandler.init(calendar);
        calendarDAO.save(calendar);
    }
}
