package adapter;

import java.time.LocalDate;

import core.Period;
import core.Period.PeriodType;

public class PeriodChange {
    private PeriodType periodType;
    private int periodId;
    private LocalDate start;
    private LocalDate end;

    public PeriodChange(PeriodType periodType, int periodId, LocalDate start, LocalDate end) {
        super();
        this.periodType = periodType;
        this.periodId = periodId;
        this.start = start;
        this.end = end;
    }

    public PeriodType getPeriodType() {
        return periodType;
    }

    public void setPeriodType(PeriodType periodType) {
        this.periodType = periodType;
    }

    public int getPeriodId() {
        return periodId;
    }

    public void setPeriodId(int periodId) {
        this.periodId = periodId;
    }

    public LocalDate getStart() {
        return start;
    }

    public void setStart(LocalDate start) {
        this.start = start;
    }

    public LocalDate getEnd() {
        return end;
    }

    public void setEnd(LocalDate end) {
        this.end = end;
    }

}