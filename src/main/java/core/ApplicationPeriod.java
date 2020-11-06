package core;

import java.time.LocalDate;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("Application")
public class ApplicationPeriod extends Period {

    ApplicationPeriod() {
        super();
    }

    public ApplicationPeriod(LocalDate start, LocalDate end, DegreeYear degreeYear) {
        super(start, end, degreeYear);
    }

    public int getCandidateCount() {
        return candidates.size();
    }

    @Override
    public PeriodType getType() {
        return PeriodType.Application;
    }

}
