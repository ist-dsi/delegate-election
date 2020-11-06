package core;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

@Entity
@DiscriminatorValue("Election")
public class ElectionPeriod extends Period {

    @OneToMany(mappedBy = "period", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    protected Set<Vote> votes;

    ElectionPeriod() {
    }

    public ElectionPeriod(LocalDate start, LocalDate end, DegreeYear degreeYear) {
        super(start, end, degreeYear);
        votes = new HashSet<Vote>();
        /*     if (degreeYear.getActivePeriod() != null) {
                 if (degreeYear.getActivePeriod().getCandidates() != null) {
                     super.setCandidates(degreeYear.getActivePeriod().getCandidates());
                 }
             }*/
    }

    // Get the vote from a given student
    public String getVote(String s) {
        for (final Vote v : votes) {
            if (v.getVoter().equals(s)) {
                return v.getVoted();
            }
        }
        return null;
    }

    //Get number of votes on a given student
    public int getNumVotes(String istId) {
        int nVotes = 0;
        for (Vote v : votes) {
            if (v.getVoted().equals(istId)) {
                nVotes++;
            }
        }
        return nVotes;
    }

    public Set<Vote> getVotes() {
        return votes;
    }

    public void vote(Student voter, Student voted) {
        Vote v;
        if (voted == null) {
            v = new Vote(voter.getUsername(), "", this);
        } else {
            v = new Vote(voter.getUsername(), voted.getUsername(), this);
        }
        votes.add(v);
        v.setPeriod(this);
    }

    public void vote(Vote v) {
        votes.add(v);
        v.setPeriod(this);
    }

    @Override
    public PeriodType getType() {
        return PeriodType.Election;
    }
}
