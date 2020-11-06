package core;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class VotePK implements Serializable {

    @Column(name = "voter")
    private String voter;

    @Column(name = "voted")
    private String voted;

    @Column(name = "period_id")
    private int period_id;

    VotePK() {
    }

    public VotePK(String voter, String voted, int period_id) {
        this.voted = voted;
        this.voter = voter;
        this.period_id = period_id;
    }

    public String getVoter() {
        return voter;
    }

    public String getVoted() {
        return voted;
    }

    public int getPeriod_id() {
        return period_id;
    }

}
