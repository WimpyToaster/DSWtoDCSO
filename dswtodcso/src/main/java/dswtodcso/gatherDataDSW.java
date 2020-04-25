package dswtodcso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class gatherDataDSW {

    /**
	 * Returns the reply values for a given category and DSWFile
	 * @param category What Category to get values (from DMP, Contact, DMPStaff, Project, Cost and Dataset)
	 */
    public static Map<String, List<JSONObject>> getRepliesFormCategory(String category, Map<String, 
    JSONObject> pathsReplies, Map<String, JSONObject> DSWReplies, Map<String, String> requiredLevels) {
        
        Map<String, List<JSONObject>> replyValues = new HashMap<>();
        
        // JSONArray with JSONObjects with paths for each property
        JSONObject obj = pathsReplies.get(category);
        
        Set<String> attributes = obj.keySet();

        for (String attribute : attributes) {
            if (obj.get(attribute) instanceof String) {
                getValueFromPath(attribute, obj.get(attribute).toString(), "", replyValues, DSWReplies);                
            }
            else {
                getValueFromObject(attribute, (JSONObject) obj.get(attribute), "", replyValues, DSWReplies, requiredLevels);
            }
        }

        //System.out.println(replyValues);
        return replyValues;
    }

    
     /**
     * Update the Map with the List of JSONObjects with the values from the DSW Replies
     * @param attributeName The Attribute Name of the Object
     * @param obj The JSONObject with the paths to the values in DSW Replies
     * @param previousPath The Path from the previous level in the Mapping
     * @param valuesMap Map that will be updated
     */
    public static void getValueFromObject(String attributeName, JSONObject obj, String previousPath, 
    Map<String, List<JSONObject>> valuesMap, Map<String, JSONObject> DSWReplies, Map<String, String> requiredLevels) { 
        String initialPath;
        Boolean noQuantity = false;

        if (previousPath.isEmpty()) {
            if (obj.containsKey("quantity")) {
                if (obj.get("quantity").toString().isEmpty()) {
                    noQuantity = true;
                }
                initialPath = obj.get("quantity").toString();
            } else {
                initialPath = "";
            }
            
        }
        else{
            initialPath = previousPath + "." + obj.get("quantity").toString();
        }

        Set<String> propertyAttributes = obj.keySet();

        List<JSONObject> valuesMapList = new ArrayList<>();

        int quantity = 0;

        if (!initialPath.isEmpty()) {
            quantity = Integer.parseInt(DSWReplies.get(initialPath).get("value").toString());
        }
        
        if (noQuantity) {
            JSONObject valuesJSON = new JSONObject();
            
            for(String attribute : propertyAttributes) {
                if (!attribute.equals("quantity")) {
                    if (obj.get(attribute) instanceof String) {
                        updateJSONValueFromPath(attribute, obj.get(attribute).toString(), initialPath, valuesJSON, DSWReplies, requiredLevels);               
                    }
                    else {
                        updateJSONValueFromObject(attribute, (JSONObject) obj.get(attribute), initialPath, valuesJSON, DSWReplies, requiredLevels);
                    }
                }
            }

            valuesMapList.add(valuesJSON);

            valuesMap.put(attributeName, valuesMapList);
        } else {

            for (int j = 0; j < quantity; j++) {
                JSONObject valuesJSON = new JSONObject();
                
                for(String attribute : propertyAttributes) {
                    if (!attribute.equals("quantity")) {
                        if (obj.get(attribute) instanceof String) {
                            updateJSONValueFromPath(attribute, obj.get(attribute).toString(), initialPath + "." + j, valuesJSON, DSWReplies, requiredLevels);               
                        }
                        else {
                            updateJSONValueFromObject(attribute, (JSONObject) obj.get(attribute), initialPath + "." + j, valuesJSON, DSWReplies, requiredLevels);
                        }
                    }
                }
    
                valuesMapList.add(valuesJSON);
    
                valuesMap.put(attributeName, valuesMapList);
            }
        }
    }

    /**
     * Update the JSONObject with the values from the DSW Replies for a given Attribute
     * @param attributeName The Attribute Name of the Object
     * @param obj The JSONObject with the paths to the values in DSW Replies
     * @param previousPath The Path from the previous level in the Mapping
     * @param values JSONObject that will be updated
     */
    public static void updateJSONValueFromObject(String attributeName, JSONObject obj, String previousPath, 
    JSONObject values, Map<String, JSONObject> DSWReplies, Map<String, String> requiredLevels) { 
        String initialPath;
        Boolean noQuantity = false;

        if (previousPath.isEmpty()) {
            if (obj.containsKey("quantity")) {
                if (obj.get("quantity").toString().isEmpty()) {
                    noQuantity = true;
                }
                initialPath = obj.get("quantity").toString();
            } else {
                initialPath = "";
            }
        }
        else{
            initialPath = previousPath + "." + obj.get("quantity").toString();
        }

        Set<String> propertyAttributes = obj.keySet();

        List<JSONObject> valuesMapList = new ArrayList<>();

        int quantity = 0;
        
        if (!initialPath.isEmpty()) {
            quantity = Integer.parseInt(DSWReplies.get(initialPath).get("value").toString());
        }

        if (noQuantity) {
            JSONObject valuesJSON = new JSONObject();
            
            for(String attribute : propertyAttributes) {
                if (!attribute.equals("quantity")) {
                    if (obj.get(attribute) instanceof String) {
                        updateJSONValueFromPath(attribute, obj.get(attribute).toString(), initialPath, valuesJSON, DSWReplies, requiredLevels);               
                    }
                    else {
                        getValueFromObject(attribute, (JSONObject) obj.get(attribute), initialPath, valuesJSON, DSWReplies, requiredLevels);
                    }
                }
            }

            valuesMapList.add(valuesJSON);

            values.put(attributeName, valuesMapList);
        } else {

            for (int j = 0; j < quantity; j++) {
                JSONObject valuesJSON = new JSONObject();
                
                for(String attribute : propertyAttributes) {
                    if (!attribute.equals("quantity")) {
                        if (obj.get(attribute) instanceof String) {
                            updateJSONValueFromPath(attribute, obj.get(attribute).toString(), initialPath + "." + j, valuesJSON, DSWReplies, requiredLevels);               
                        }
                        else {
                            getValueFromObject(attribute, (JSONObject) obj.get(attribute), initialPath + "." + j, valuesJSON, DSWReplies, requiredLevels);
                        }
                    }
                }
    
                valuesMapList.add(valuesJSON);
    
                values.put(attributeName, valuesMapList);
            }
        }
    }

    /**
     * Update the JSONObject with the value from the DSW Replies for a given Attribute
     * @param attributeName The Attribute Name of the Object
     * @param attributePath The path of the Attribute to get the Reply
     * @param previousPath The Path from the previous level in the Mapping
     * @param valuesJSON JSONObject that will be updated
     */
    public static void updateJSONValueFromPath(String attributeName, String attributePath, String previousPath, JSONObject valuesJSON,
    Map<String, JSONObject> DSWReplies, Map<String, String> requiredLevels) {
        JSONObject value;

        if (previousPath.isEmpty()) {
            value = (JSONObject) DSWReplies.get(attributePath);
        }
        else {
            value = (JSONObject) DSWReplies.get(previousPath + "." + attributePath);
        }
        
        if (value == null) {
            if (requiredLevels.get(attributePath) != null) {
                if (!requiredLevels.get(attributePath).isEmpty()) {
                    System.out.println("The attribute " + attributeName + " is required and is missing");
                }
            }
            valuesJSON.put(attributeName, "");
        } else {
            valuesJSON.put(attributeName, value.get("value").toString());
        }
        
    }

    /**
     * Update the Map with the List of JSONObjects with the values from the DSW Replies
     * @param attributeName The Attribute Name of the Object
     * @param attributePath The path of the Attribute to get the Reply
     * @param previousPath The Path from the previous level in the Mapping
     * @param valuesList JSONObject that will be updated
     */
    public static void getValueFromPath(String attributeName, String attributePath, String previousPath, Map<String, List<JSONObject>> values,
    Map<String, JSONObject> DSWReplies) {
        JSONObject value;

        if (previousPath.isEmpty()) {
            value = (JSONObject) DSWReplies.get(attributePath);
        }
        else {
            value = (JSONObject) DSWReplies.get(previousPath + "." + attributePath);
        }

        JSONObject valuesMap = new JSONObject();
        valuesMap.put(attributeName, value.get("value").toString());

        List<JSONObject> valuesMapList = new ArrayList<>();
        valuesMapList.add(valuesMap);

        values.put(attributeName, valuesMapList);

    }

        /**
	 * Populates the HashMap with the questionnaire replies
	 * @param jsonObj The JSON Object with the replies to the questionnaire
	 */
    public static void initializeDSWReplies(JSONObject jsonObj, Map<String, JSONObject> DSWReplies) {
        JSONArray replies = (JSONArray) jsonObj.get("questionnaireReplies");
        
        for (int i = 0; i < replies.size(); i++) {    
            JSONObject reply = (JSONObject) replies.get(i);       
            String path = reply.get("path").toString();
            JSONObject value = (JSONObject) reply.get("value");
            DSWReplies.put(path, value);
        }
    }

    /**
	 * Populates the HashMap with the required level for each question
	 * @param jsonObj The JSON Object with the paths for the replies to the questionnaire
	 */
    public static void initializeRequiredLevel(JSONObject jsonObj, Map<String, String> requiredLevels) {
        JSONObject knowledgeModel = (JSONObject) jsonObj.get("knowledgeModel");
        JSONObject entities = (JSONObject) knowledgeModel.get("entities");
        JSONObject questions = (JSONObject) entities.get("questions");

        Set<String> paths = questions.keySet();
        
        String requiredLevel = "";

        for (String path : paths) {
            JSONObject question = (JSONObject) questions.get(path);
            
            if (question.get("requiredLevel") == null ) {
                requiredLevel = "";
            }
            else {
                requiredLevel = question.get("requiredLevel").toString();
            }

            requiredLevels.put(path, requiredLevel);
        }
    }

    /**
	 * Populates the HashMap with the paths to the questionnaire replies
	 * @param jsonObj The JSON Object with the paths for the replies to the questionnaire
	 */
    public static void initializePathsReplies(JSONObject jsonObj, Map<String, JSONObject> pathsReplies) {
        JSONArray paths = (JSONArray) jsonObj.get("questions");
        
        for (int i = 0; i < paths.size(); i++) {    
            JSONObject path = (JSONObject) paths.get(i);       
            String key = path.get("question").toString();
            JSONObject value = (JSONObject) path.get("uuid");
            pathsReplies.put(key, value);
        }
    }

    
}