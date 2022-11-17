package com.blackwalnutsoftware.andrew;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The Class TrainingParameters.
 * <P>
 * Training Parameters manages sets of values to be used in combination for
 * the training of thoughts.  The values for each parameter are to be randomly
 * combined to generate corresponding goal attributes.
 */
public class TrainingParameters {

   Map<String, List<Object>> _trainingParametersMap;

   public TrainingParameters(Map<String, List<Object>> trainingParametersMap) {
      _trainingParametersMap = trainingParametersMap;
   }

   public Set<String> getGoalAttributes() {
      return _trainingParametersMap.keySet();
   }

   public Object getRandomValue(String key) {
      List<Object> values = _trainingParametersMap.get(key);
      Collections.shuffle(values);
      return values.get(0);
   }

   // TODO: add a method to add a name and values. ex: add(String key, Object... values[])
}
