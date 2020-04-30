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

            Object propertiesValuesFile = jsonParser.parse(readerDataPropValues);

            dataPropertyTypeValues = (JSONObject) propertiesValuesFile;

            gatherDataDSW.initializeDSWReplies(DSWQuestionaire, DSWReplies);

            gatherDataDSW.initializeRequiredLevel(DSWQuestionaire, requiredLevels);

            gatherDataDSW.initializePathsReplies(UuidMapping, pathsReplies);

            Map<String, Map<String, List<JSONObject>>> replies = new HashMap<>();
            replies.put("DMP", gatherDataDSW.getRepliesFormCategory("DMP", pathsReplies, DSWReplies, requiredLevels));
            replies.put("Contact", gatherDataDSW.getRepliesFormCategory("Contact", pathsReplies, DSWReplies, requiredLevels));
            replies.put("Contributor", gatherDataDSW.getRepliesFormCategory("Contributor", pathsReplies, DSWReplies, requiredLevels));
            replies.put("Project", gatherDataDSW.getRepliesFormCategory("Project", pathsReplies, DSWReplies, requiredLevels));
            replies.put("Cost", gatherDataDSW.getRepliesFormCategory("Cost", pathsReplies, DSWReplies, requiredLevels));
            replies.put("Dataset", gatherDataDSW.getRepliesFormCategory("Dataset", pathsReplies, DSWReplies, requiredLevels));

            // for(String cat : replies.keySet()) {
            //     System.out.println(replies.get(cat));
            //     System.out.println("");
            // }
            
            

            File ontologyFile = new File("C:/Users/antot/DSWtoDCSO/dswtodcso/resources/rda-common-dmp.2.0.0.owl");

            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            OWLOntology ontology = Ontology.importOntology(ontologyFile, manager);
            OWLDataFactory dataFactory = ontology.getOWLOntologyManager().getOWLDataFactory();

            IRI ontologyIRI = ontology.getOntologyID().getOntologyIRI().get();
            
            //System.out.println(ontology);
            
            Map<String, OWLClass> classesMap = new HashMap<>(); 
            
            Set<OWLClass> classes = ontology.getClassesInSignature();

            for (OWLClass owlClass : classes) {   
                classesMap.put(owlClass.getIRI().getShortForm(), owlClass);
            }

            
            
            //HashMap<IRI, OWLLiteral> propertyValues = new HashMap<>();
            //propertyValues = getPropertyValues(dataFactory, ontologyIRI, cost);

            //Ontology.createIndividual(ontology, manager, IRI.create(ontologyIRI.toString() + "#costTest"), classesMap.get("Cost"));



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
     * 
     * @param ontology
     * @param manager
     * @param propertyClass
     * @param elem
     * @param ind
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
                        OWLLiteral value = getDataPropertyValue(ontology, dataPropertiesType.get(propertyClass).get(attribute), elem.get(attribute).toString());
                        propertyValues.put(IRI.create(ontologyIRI.toString() + "#" + attribute), value);
                    }
                } 
            }
        }

        Ontology.addDataPropertyToIndividual(ontology, manager, ind, propertyValues);
    }

    /**
     * 
     * @param ontology
     * @param propertyType
     * @param value
     * @return
     */
    public static OWLLiteral getDataPropertyValue(OWLOntology ontology, String propertyType, String value) {
        OWLLiteral dataPropertyValue = null;
        OWLDataFactory dataFactory = ontology.getOWLOntologyManager().getOWLDataFactory();

        switch (propertyType) {
            case "xsd:boolean":
                Boolean booleanValue = Boolean.parseBoolean(value);
                dataPropertyValue = dataFactory.getOWLLiteral(booleanValue);
                break;

            case "xsd:double":
                try {
                    double doubleValue = Double.parseDouble(value);
                    dataPropertyValue = dataFactory.getOWLLiteral(doubleValue);
                } catch (NumberFormatException e) {
                    System.out.println("Value " + value + " is not in tha correct format for an Double");
                }
                
                break;
                
            case "xsd:float":
                try {
                    float floatValue = Float.parseFloat(value);
                    dataPropertyValue = dataFactory.getOWLLiteral(floatValue);
                } catch (NumberFormatException e) {
                    System.out.println("Value " + value + " is not in tha correct format for an Float");
                }

                break;

            case "xsd:integer":
                try {
                    int intValue = Integer.parseInt(value);
                    dataPropertyValue = dataFactory.getOWLLiteral(intValue);
                } catch (NumberFormatException e) {
                    System.out.println("Value " + value + " is not in tha correct format for an Integer");
                }
                
                break;

            case "xsd:string":
                dataPropertyValue = dataFactory.getOWLLiteral(value);
                break;

            case "xsd:dateTime":
                OWLDatatype dateType = new OWL2DatatypeImpl(OWL2Datatype.XSD_DATE_TIME);
                dataPropertyValue = dataFactory.getOWLLiteral(value, dateType);
                break;

            case "xsd:anyURI":
                OWLDatatype uriType = new OWL2DatatypeImpl(OWL2Datatype.XSD_ANY_URI);
                dataPropertyValue = dataFactory.getOWLLiteral(value, uriType);
                break;

            case "<https://raw.githubusercontent.com/RDA-DMP-Common/RDA-DMP-Common-Standard/master/ontologies/dcso/rda-common-dmp.2.0.0.owl#yesNoUnknownVocabulary>":
                JSONArray typesYesNoUnk = (JSONArray) dataPropertyTypeValues.get("yesNoUnknownVocabulary");

                if (!containsCaseInsensitive(typesYesNoUnk, value)) {
                    System.out.println(value + " Should Be Yes/No/Unknown");
                } else {
                    dataPropertyValue = dataFactory.getOWLLiteral(value);
                }
                
                
                break;
            
            case "<https://raw.githubusercontent.com/RDA-DMP-Common/RDA-DMP-Common-Standard/master/ontologies/dcso/rda-common-dmp.2.0.0.owl#iso639-3_values>":                        
                JSONArray typesISO639 = (JSONArray) dataPropertyTypeValues.get("iso639-3_values");

                if (!containsCaseInsensitive(typesISO639, value)) {
                    System.out.println(value + " Should Be Complient with ISO639-3");
                } else {
                    dataPropertyValue = dataFactory.getOWLLiteral(value);
                }
                
                break;

            case "<https://raw.githubusercontent.com/RDA-DMP-Common/RDA-DMP-Common-Standard/master/ontologies/dcso/rda-common-dmp.2.0.0.owl#role_values>":
                JSONArray typesRole = (JSONArray) dataPropertyTypeValues.get("role_values");

                if (!containsCaseInsensitive(typesRole, value)) {
                    System.out.println(value + " Should Be Complient with role Values");
                } else {
                    dataPropertyValue = dataFactory.getOWLLiteral(value);
                }

                break;

            case "<https://raw.githubusercontent.com/RDA-DMP-Common/RDA-DMP-Common-Standard/master/ontologies/dcso/rda-common-dmp.2.0.0.owl#iso-4217-code>":
                JSONArray typesISO4217 = (JSONArray) dataPropertyTypeValues.get("iso-4217-code");

                if (!containsCaseInsensitive(typesISO4217, value)) {
                    System.out.println(value + " Should Be Complient with ISO4217");
                } else {
                    dataPropertyValue = dataFactory.getOWLLiteral(value);
                }
                break;

            case "<https://raw.githubusercontent.com/RDA-DMP-Common/RDA-DMP-Common-Standard/master/ontologies/dcso/rda-common-dmp.2.0.0.owl#fundingStatusVocabulary>":
                JSONArray typesFundingSt = (JSONArray) dataPropertyTypeValues.get("fundingStatusVocabulary");

                if (!containsCaseInsensitive(typesFundingSt, value)) {
                    System.out.println(value + " Should Be Complient with fundingStatusVocabulary");
                } else {
                    dataPropertyValue = dataFactory.getOWLLiteral(value);
                }
                break;

            case "<https://raw.githubusercontent.com/RDA-DMP-Common/RDA-DMP-Common-Standard/master/ontologies/dcso/rda-common-dmp.2.0.0.owl#dataAccessVocabulary>":
                JSONArray typesDataAccess = (JSONArray) dataPropertyTypeValues.get("dataAccessVocabulary");

                if (!containsCaseInsensitive(typesDataAccess, value)) {
                    System.out.println(value + " Should Be Complient with dataAccessVocabulary");
                } else {
                    dataPropertyValue = dataFactory.getOWLLiteral(value);
                }
                break;
                
            case "<https://raw.githubusercontent.com/RDA-DMP-Common/RDA-DMP-Common-Standard/master/ontologies/dcso/rda-common-dmp.2.0.0.owl#certified_with_values>":
                JSONArray typesCertified = (JSONArray) dataPropertyTypeValues.get("certified_with_values");

                if (!containsCaseInsensitive(typesCertified, value)) {
                    System.out.println(value + " Should Be Complient with certifiedWith_values");
                } else {
                    dataPropertyValue = dataFactory.getOWLLiteral(value);
                }
                break;

            case "<https://raw.githubusercontent.com/RDA-DMP-Common/RDA-DMP-Common-Standard/master/ontologies/dcso/rda-common-dmp.2.0.0.owl#iso-3166-1-alpha2>":
                JSONArray typesISO3166 = (JSONArray) dataPropertyTypeValues.get("iso-3166-1-alpha2");

                if (!containsCaseInsensitive(typesISO3166, value)) {
                    System.out.println(value + " Should Be Complient with iso-3166-1-alpha2");
                } else {
                    dataPropertyValue = dataFactory.getOWLLiteral(value);
                }
                break;

            case "<https://raw.githubusercontent.com/RDA-DMP-Common/RDA-DMP-Common-Standard/master/ontologies/dcso/rda-common-dmp.2.0.0.owl#pid_system_values>":
                JSONArray typesPid = (JSONArray) dataPropertyTypeValues.get("pid_system_values");

                if (!containsCaseInsensitive(typesPid, value)) {
                    System.out.println(value + " Should Be Complient with PID System values");
                } else {
                    dataPropertyValue = dataFactory.getOWLLiteral(value);
                }
                break;


            default:
                System.out.println(propertyType);
                break;
            }

        return dataPropertyValue;
    }


    /**
     * 
     * @param obj
     * @param value
     * @return
     */
    public static Boolean containsCaseInsensitive(JSONArray obj, String value) {
        for (int i = 0; i < obj.size(); i++) {    
            String temp = (String) obj.get(i);
            temp = temp.trim().replaceAll("\\s+", "");
            if (temp.equalsIgnoreCase(value.trim().replaceAll("\\s+", ""))) {
                return true;
            }
        }
        return false;
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



    /**
     * 
     * @param dataFactory
     * @param ontologyIRI
     * @param values
     * @return
     */
    public static HashMap<IRI, OWLLiteral> getPropertyValues(OWLDataFactory dataFactory, IRI ontologyIRI, 
    Map<String, List<JSONObject>> values) {
        
        HashMap<IRI, OWLLiteral> propertyValues = new HashMap<>();
        JSONObject propValue;

        for (String prop : values.keySet()) {
            
            IRI valueIRI = IRI.create(ontologyIRI.toString() + "#" + prop);
            propValue = values.get(prop).get(0);
            if (propValue.size() == 1) {
                OWLLiteral literal = dataFactory.getOWLLiteral(propValue.get(prop).toString());
                propertyValues.put(valueIRI, literal);
            }
            
        }
        
        return propertyValues;
    }

}
