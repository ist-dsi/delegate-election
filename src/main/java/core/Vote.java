package core;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "vote")
public class Vote implements Serializable {

    @EmbeddedId
    private VotePK votepk;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "period_id", referencedColumnName = "period_id", insertable = false, updatable = false)
    private Period period;

    Vote() {
    }

    public Vote(String voter, String voted, Period p) {
        this.votepk = new VotePK(voter, voted, p.getId());
        this.period = p;
    }

    public void setPeriod(Period p) {
        this.period = p;
    }

    public VotePK getVotePK() {
        return votepk;
    }

    public VotePK getVotepk() {
        return votepk;
    }

    public void setVotepk(VotePK votepk) {
        this.votepk = votepk;
    }

    public Period getPeriod() {
        return period;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Vote)) {
            return false;
        }
        Vote v = (Vote) o;
        if (this.votepk.getVoted().equals(v.votepk.getVoted()) && this.period.getId() == v.period.getId()
                && this.votepk.getVoter().equals(v.votepk.getVoter())) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        //Forcing equals
        return 1;
    }

    public String getVoter() {
        return votepk.getVoter();
    }

    public String getVoted() {
        return votepk.getVoted();
    }

}
