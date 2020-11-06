package adapter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DegreeChange {

    private final String degreeId;
    private final Map<Integer, Set<PeriodChange>> periods;

    public DegreeChange(String degreeId) {
        this.degreeId = degreeId;
        this.periods = new HashMap<Integer, Set<PeriodChange>>();
    }

    public void addYear(int year, Set<PeriodChange> periods) {
        this.periods.put(year, periods);
    }

    public Map<Integer, Set<PeriodChange>> getPeriods() {
        return periods;
    }

    public String getDegreeId() {
        return degreeId;
    }

}
