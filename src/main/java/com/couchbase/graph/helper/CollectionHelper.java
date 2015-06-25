/*
 * Copyright 2015 Couchbase, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.couchbase.graph.helper;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Just some helper methods for lists
 * 
 * 
 * @author David Maier <david.maier at couchbase.com>
 */
public class CollectionHelper {
    
   /**
    * Checks if the given list contains a specific object value by comparing the
    * string version.
    * 
    * This is necessary becuase we sometimes don't want to work with exactly 
    * the same instance on the client side but with a temp. instance which
    * is fetched on demand but reperesents the same object
    * 
    * @param list
    * @param obj
    * @return 
    */
   public static boolean contains(List list, Object obj)
   {
       for (Object entry : list) {
           
           if (entry.toString().equals(obj.toString())) return true;
       }
       
       return false;
   }
   
    /**
     * Sort a map recursivly
     * 
     * @param in
     * @return 
     */
    public static TreeMap<String, Object> sortMap(Map<String, Object> in)
    {
        TreeMap<String, Object> out = new TreeMap<>(new StringComparator());
        
        for (Map.Entry<String, Object> entry : in.entrySet()) {
           
            String key = entry.getKey();
            Object value = entry.getValue();
            
            if (value instanceof Map)
            {
                out.put(key, sortMap((Map)value));
            }
            else
            {
                out.put(key, value);
            }
        }
        
        return out;
    }
   
}
