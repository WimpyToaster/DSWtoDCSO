package dswtodcso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

import OntologyOperations.*;
import Utils.Utils;
import uk.ac.manchester.cs.owl.owlapi.OWL2DatatypeImpl;


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

    public static JSONObject dataPropertyTypeValues = new JSONObject();


    public static void main( String[] args )
    {
        //JSON parser object to parse read file
        JSONParser jsonParser = new JSONParser();
         
        try (FileReader readerDSWFile = new FileReader("C:/Users/antot/DSWtoDCSO/dswtodcso/resources/testDSW.json");
            FileReader readerUuidMap = new FileReader("C:/Users/antot/DSWtoDCSO/dswtodcso/resources/DSWUuidMap.json");
            FileReader readerDataPropValues = new FileReader("C:/Users/antot/DSWtoDCSO/dswtodcso/resources/dataPropertyTypeValues.json"))
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

            Map<String, Map<String, List<JSONObject>>> replies = new HashMap<>();
            replies.put("DMP", gatherDataDSW.getRepliesFormCategory("DMP", pathsReplies, DSWReplies, requiredLevels, answerReply));
            replies.put("Contact", gatherDataDSW.getRepliesFormCategory("Contact", pathsReplies, DSWReplies, requiredLevels, answerReply));
            replies.put("Contributor", gatherDataDSW.getRepliesFormCategory("Contributor", pathsReplies, DSWReplies, requiredLevels, answerReply));
            replies.put("Project", gatherDataDSW.getRepliesFormCategory("Project", pathsReplies, DSWReplies, requiredLevels, answerReply));
            replies.put("Cost", gatherDataDSW.getRepliesFormCategory("Cost", pathsReplies, DSWReplies, requiredLevels, answerReply));
            replies.put("Dataset", gatherDataDSW.getRepliesFormCategory("Dataset", pathsReplies, DSWReplies, requiredLevels, answerReply));
            

            File ontologyFile = new File("C:/Users/antot/DSWtoDCSO/dswtodcso/resources/rda-common-dmp.2.0.0.owl");

            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            OWLOntology ontology = Ontology.importOntology(ontologyFile, manager);
            OWLDataFactory dataFactory = ontology.getOWLOntologyManager().getOWLDataFactory();
            
            
            Map<String, OWLClass> classesMap = new HashMap<>(); 
            
            Set<OWLClass> classes = ontology.getClassesInSignature();

            for (OWLClass owlClass : classes) {   
                classesMap.put(owlClass.getIRI().getShortForm(), owlClass);
            }


            // Set<OWLAxiom> axi = ontology.getAxioms();
            // for (OWLAxiom ax : ontology.getAxioms()) {
                
            //     System.out.println(ax);
            // }

            for (OWLClass cls : classes) {
                
                //System.out.println("+: " + cls.getIRI().getShortForm());

                //System.out.println(" \tObject Property Domain");
                for (OWLObjectPropertyDomainAxiom op : ontology.getAxioms(AxiomType.OBJECT_PROPERTY_DOMAIN)) {
                    if (op.getDomain().equals(cls)) {
                        for (OWLObjectProperty oop : op.getObjectPropertiesInSignature()) {
                            if (cls.getIRI().getShortForm().equals(oop.getIRI().getShortForm()))
                                continue;
                            //System.out.println("\t\t +: " + oop.getIRI().getShortForm() + "==Object Property");
                        }
                    }
                }

                Map <String, String> propertyType = new HashMap<>();

                //System.out.println(" \tData Property Domain");
                
                for (OWLDataPropertyDomainAxiom dp : ontology.getAxioms(AxiomType.DATA_PROPERTY_DOMAIN)) {
                    if (dp.getDomain().equals(cls)) {
                        for (OWLDataProperty odp : dp.getDataPropertiesInSignature()) {
                            //System.out.println("\t\t +: " + odp.getIRI().getShortForm() + "==Data Property" );
                            Set <OWLDataPropertyRangeAxiom> sgdp = ontology.getDataPropertyRangeAxioms(odp);

                            for (OWLDataPropertyRangeAxiom a : sgdp ) {
                                //System.out.println("The data properties range for " + odp.getIRI() +" is: " + a.getRange());
                                propertyType.put(odp.getIRI().getShortForm(), a.getRange().toString());
                            }
                            
                        }
                        dataPropertiesType.put(cls.getIRI().getShortForm(), propertyType);
                    }
                }
            }

            //System.out.println(dataPropertiesType);

            createAllIndividuals(ontology, manager, classesMap, replies);

            File ontologyOut = new File("C:/Users/antot/DSWtoDCSO/dswtodcso/resources/newOntology.owl");
            Ontology.saveOntology(ontologyOut, ontology, manager);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create all individuals for all classes
     * @param ontology
     * @param manager
     * @param classesMap
     * @param replies
     */
    public static void createAllIndividuals(OWLOntology ontology, OWLOntologyManager manager, Map<String, OWLClass> classesMap, 
    Map<String, Map<String, List<JSONObject>>> replies) {

        IRI ontologyIRI = ontology.getOntologyID().getOntologyIRI().get();

        int id = 0;
        String indName;

        for(String cat : replies.keySet()) {
            for(String elem : replies.get(cat).keySet()) {
                if (replies.get(cat).get(elem) instanceof List) {
                    int quantity = replies.get(cat).get(elem).size();
                    OWLClass indClass = classesMap.get(elem);

                    if (indClass != null) {
                        for(int i = 0; i < quantity; ++i) {
                            indName = elem + id;
                            IRI indIRI = IRI.create(ontologyIRI.toString() + "#" + indName);
                            id++;
    
                            OWLIndividual ind = Ontology.createIndividual(ontology, manager, indIRI, indClass);

                            createDatapropertiesForInd(ontology, manager, elem, replies.get(cat).get(elem).get(i), ind);

                            createIndInsideElem(ontology, manager, classesMap, replies.get(cat).get(elem).get(i), indName);
                        }
                    }
                }
            }
        }
    }

    /**
     * Creates the DataProperty for a given individual
     * @param ontology OWLOntology
     * @param manager OWLOntologyManager
     * @param propertyClass Name of the class of the individual
     * @param elem JSONObject with the individual data
     * @param ind OWLIndividual 
     */
    public static void createDatapropertiesForInd(OWLOntology ontology, OWLOntologyManager manager, String propertyClass, 
    JSONObject elem, OWLIndividual ind) {
        IRI ontologyIRI = ontology.getOntologyID().getOntologyIRI().get();
        Set<String> attributes = elem.keySet();

        HashMap<IRI, OWLLiteral> propertyValues = new HashMap<>();

        for(String attribute : attributes) {

            if (!(elem.get(attribute) instanceof List)) {
                if (dataPropertiesType.get(propertyClass) != null) {
                    if (dataPropertiesType.get(propertyClass).get(attribute) != null) {
                        OWLLiteral value = Utils.getDataPropertyValue(ontology, dataPropertiesType.get(propertyClass).get(attribute), elem.get(attribute).toString(), dataPropertyTypeValues);
                        propertyValues.put(IRI.create(ontologyIRI.toString() + "#" + attribute), value);
                    }
                } 
            }
        }

        Ontology.addDataPropertyToIndividual(ontology, manager, ind, propertyValues);
    }

    


    

    /**
     * Creates the individuals recursively
     * @param ontology
     * @param manager
     * @param classesMap
     * @param elem
     * @param id
     */
    public static void createIndInsideElem(OWLOntology ontology, OWLOntologyManager manager, Map<String, OWLClass> classesMap, 
    JSONObject elem, String parentName) {
        IRI ontologyIRI = ontology.getOntologyID().getOntologyIRI().get();
        Set<String> attributes = elem.keySet();
        int id = 0;
        String indName;

        for(String attribute : attributes) {

            if (elem.get(attribute) instanceof List) {
                List<JSONObject> attributeList = (List<JSONObject>) elem.get(attribute);

                int quantity = attributeList.size();
                OWLClass indClass = classesMap.get(attribute);

                
                if (indClass != null) {
                    for(int i = 0; i < quantity; ++i) {
                        indName = parentName + attribute + id;
                        IRI indIRI = IRI.create(ontologyIRI.toString() + "#" + indName);
                        id++;

                        OWLIndividual ind = Ontology.createIndividual(ontology, manager, indIRI, indClass);

                        createDatapropertiesForInd(ontology, manager, attribute, attributeList.get(i), ind);
        
                        createIndInsideElem(ontology, manager, classesMap, attributeList.get(i), indName);
                    }
                }
            }
        }
    }
}
