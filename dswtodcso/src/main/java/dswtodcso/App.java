package dswtodcso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import OntologyOperations.*;
import gatherDataDSW.*;
import Utils.Utils;


public class App 
{
    /** HashMap with uuid of a question as key and JSONObject with reply as value */
    public static Map<String, JSONObject> DSWReplies = new HashMap<>();

    /** HashMap with uuid for each question path in DSW */
    public static Map<String, JSONObject> pathsReplies = new HashMap<>();

    /** HashMap with the required Level for each attribute path */
    public static Map<String, String> requiredLevels = new HashMap<>();

    /** HashMap with Data properties Type for each class */
    public static Map<String, Map<String, String>> dataPropertiesType = new HashMap<>();

    /** HashMap with Object properties Type for each class */
    public static Map<String, Map<String, String>> objectPropertiesType = new HashMap<>();

    /** JSONObject with vocabularios for data Ranges */
    public static JSONObject dataPropertyTypeValues = new JSONObject();


    public static void main( String[] args )
    {
        //JSON parser object to parse read file
        JSONParser jsonParser = new JSONParser();
        
        //Path for the resources Folder
        String resourcePath = "C:/Users/antot/DSWtoDCSO/dswtodcso/resources";

        try (FileReader readerDSWFile = new FileReader(resourcePath + "/testDSW.json");
            FileReader readerUuidMap = new FileReader(resourcePath + "/DSWUuidMap.json");
            FileReader readerDataPropValues = new FileReader(resourcePath + "/dataPropertyTypeValues.json"))
        {
            //Read JSON file
            Object DSWFile = jsonParser.parse(readerDSWFile);
 
            JSONObject DSWQuestionaire = (JSONObject) DSWFile;

            Object UuidMapFile = jsonParser.parse(readerUuidMap);

            JSONObject UuidMapping = (JSONObject) UuidMapFile;

            JSONObject answerReply = (JSONObject) UuidMapping.get("answerReply");

            Object propertiesValuesFile = jsonParser.parse(readerDataPropValues);

            dataPropertyTypeValues = (JSONObject) propertiesValuesFile;

            gatherDataDSW.initializeDSWReplies(DSWQuestionaire, DSWReplies);
            gatherDataDSW.initializeRequiredLevel(DSWQuestionaire, requiredLevels);
            gatherDataDSW.initializePathsReplies(UuidMapping, pathsReplies);

            // HashMap with the replies for each category gathered from DSW file
            Map<String, Map<String, List<JSONObject>>> replies = new HashMap<>();
            replies.put("DMP", gatherDataDSW.getRepliesFormCategory("DMP", pathsReplies, DSWReplies, requiredLevels, answerReply));
            replies.put("Contact", gatherDataDSW.getRepliesFormCategory("Contact", pathsReplies, DSWReplies, requiredLevels, answerReply));
            replies.put("Contributor", gatherDataDSW.getRepliesFormCategory("Contributor", pathsReplies, DSWReplies, requiredLevels, answerReply));
            replies.put("Project", gatherDataDSW.getRepliesFormCategory("Project", pathsReplies, DSWReplies, requiredLevels, answerReply));
            replies.put("Cost", gatherDataDSW.getRepliesFormCategory("Cost", pathsReplies, DSWReplies, requiredLevels, answerReply));
            replies.put("Dataset", gatherDataDSW.getRepliesFormCategory("Dataset", pathsReplies, DSWReplies, requiredLevels, answerReply));
            

            File ontologyFile = new File(resourcePath + "/dcso.owl");

            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            OWLOntology ontology = OntologyOperations.importOntology(ontologyFile, manager);            
            
            Map<String, OWLClass> classesMap = new HashMap<>(); 
            Set<OWLClass> classes = ontology.getClassesInSignature();            
            for (OWLClass owlClass : classes) {   
                classesMap.put(owlClass.getIRI().getShortForm(), owlClass);
            }

            Utils.getDataAndObjectProperties(ontology, classes, objectPropertiesType, dataPropertiesType);
            
            Utils.createAllIndividuals(ontology, manager, classesMap, replies, objectPropertiesType, dataPropertiesType, dataPropertyTypeValues);

            //Necessary because dmp doesnt have his ObjectProperties in the three mapping
            Utils.createObjectPropertiesForAClassOfInd(ontology, manager, "dmp", classesMap, objectPropertiesType);

            File ontologyOut = new File(resourcePath + "/newOntology.owl");
            OntologyOperations.saveOntology(ontologyOut, ontology, manager);


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    
}
