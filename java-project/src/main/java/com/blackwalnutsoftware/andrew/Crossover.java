package com.blackwalnutsoftware.andrew;

import java.util.List;

/**
 * The Interface Crossover.
 * <p/>
 * Crossover implementations create a next generation of Thoughts based on characteristics
 * of the parents.  Different implementations of Crossover may do this in different ways.
 */
public interface Crossover {
   
   /**
    * Crossover.
    *
    * @param t1 the t 1
    * @param t2 the t 2
    * @return the thought
    */
   public Thought crossover(Thought t1, Thought t2);

   /**
    * Creates the crossovers.
    *
    * @param currentThoughts the current thoughts
    * @return the list
    */
   public List<Thought> createCrossovers(List<Thought> currentThoughts);
}
