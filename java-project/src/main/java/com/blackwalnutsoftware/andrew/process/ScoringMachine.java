package com.blackwalnutsoftware.andrew.process;

import java.util.List;

public interface ScoringMachine {

   public List<ThoughtScore> scoreAndRank(List<Evaluation> evualationResults);

}
