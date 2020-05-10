package Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

import OntologyOperations.OntologyOperations;
import uk.ac.manchester.cs.owl.owlapi.OWL2DatatypeImpl;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class Utils {

    /**
     * Get all the Data and Object properties for each classa and fills the dataPropertiesType and objectPropertiesType HashMaps
     * @param ontology Ontology
     * @param classes Set of all classes in the ontology
     * @param objectPropertiesType HashMap with Object properties Type for each class
     * @param dataPropertiesType HashMap with Data properties Type for each class
     */
    public static void getDataAndObjectProperties(OWLOntology ontology, Set<OWLClass> classes, 
    Map<String, Map<String, String>> objectPropertiesType, Map<String, Map<String, String>> dataPropertiesType) {
        for (OWLClass cls : classes) {
                
            //System.out.println("+: " + cls.getIRI().getShortForm());
            //System.out.println(" \tObject Property Domain");

            Map <String, String> objectPropertyType = new HashMap<>();

            for (OWLObjectPropertyDomainAxiom op : ontology.getAxioms(AxiomType.OBJECT_PROPERTY_DOMAIN)) {
                if (op.getDomain().equals(cls)) {
                    for (OWLObjectProperty oop : op.getObjectPropertiesInSignature()) {
                        if (cls.getIRI().getShortForm().equals(oop.getIRI().getShortForm()))
                            continue;
                        //System.out.println("\t\t +: " + oop.getIRI().getShortForm() + "==Object Property");
                        Set <OWLObjectPropertyRangeAxiom> sgdp = ontology.getObjectPropertyRangeAxioms(oop);

                        for (OWLObjectPropertyRangeAxiom a : sgdp ) {
                            objectPropertyType.put(oop.getIRI().getShortForm(), a.getRange().toString());
                        }

                    }
                    objectPropertiesType.put(cls.getIRI().getShortForm(), objectPropertyType);
                }
            }

            Map <String, String> dataPropertyType = new HashMap<>();

            //System.out.println(" \tData Property Domain");
            
            for (OWLDataPropertyDomainAxiom dp : ontology.getAxioms(AxiomType.DATA_PROPERTY_DOMAIN)) {
                if (dp.getDomain().equals(cls)) {
                    for (OWLDataProperty odp : dp.getDataPropertiesInSignature()) {
                        //System.out.println("\t\t +: " + odp.getIRI().getShortForm() + "==Data Property" );
                        Set <OWLDataPropertyRangeAxiom> sgdp = ontology.getDataPropertyRangeAxioms(odp);

                        for (OWLDataPropertyRangeAxiom a : sgdp ) {
                            dataPropertyType.put(odp.getIRI().getShortForm(), a.getRange().toString());
                        }
                        
                    }
                    dataPropertiesType.put(cls.getIRI().getShortForm(), dataPropertyType);
                }
            }
        }
    }

    /**
     * Create all individuals for all classes
     * @param ontology OWLOntology
     * @param manager OWLOntologyManager
     * @param classesMap HashMap with Name of Class as Key and OWLClass as Value
     * @param replies HashMap with the replies for each category
     * @param objectPropertiesType HashMap with Object properties Type for each class
     * @param dataPropertiesType HashMap with Data properties Type for each class
     * @param dataPropertyTypeValues JSONObject with vocabularios for data Ranges
     */
    public static void createAllIndividuals(OWLOntology ontology, OWLOntologyManager manager, Map<String, OWLClass> classesMap, 
    Map<String, Map<String, List<JSONObject>>> replies, Map<String, Map<String, String>> objectPropertiesType, 
    Map<String, Map<String, String>> dataPropertiesType, JSONObject dataPropertyTypeValues) {

        IRI ontologyIRI = ontology.getOntologyID().getOntologyIRI().get();

        int id = 0;
        String indName;

        for(String cat : replies.keySet()) {
            id = 0;
            for(String elem : replies.get(cat).keySet()) {
                if (replies.get(cat).get(elem) instanceof List) {
                    int quantity = replies.get(cat).get(elem).size();
                    OWLClass indClass = classesMap.get(elem);

                    if (indClass != null) {
                        for(int i = 0; i < quantity; ++i) {
                            indName = elem + id;
                            IRI indIRI = IRI.create(ontologyIRI.toString() + "#" + indName);
                            id++;
    
                            OWLIndividual ind = OntologyOperations.createIndividual(ontology, manager, indIRI, indClass);

                            createDatapropertiesForInd(ontology, manager, elem, replies.get(cat).get(elem).get(i), ind, objectPropertiesType, dataPropertiesType, dataPropertyTypeValues);

                            createIndInsideElem(ontology, manager, classesMap, replies.get(cat).get(elem).get(i), indName, indClass.getIRI().getShortForm(), objectPropertiesType, dataPropertiesType, dataPropertyTypeValues);
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
     * @param objectPropertiesType HashMap with Object properties Type for each class
     * @param dataPropertiesType HashMap with Data properties Type for each class
     * @param dataPropertyTypeValues JSONObject with vocabularios for data Ranges
     */
    public static void createDatapropertiesForInd(OWLOntology ontology, OWLOntologyManager manager, String propertyClass, 
    JSONObject elem, OWLIndividual ind, Map<String, Map<String, String>> objectPropertiesType, 
    Map<String, Map<String, String>> dataPropertiesType, JSONObject dataPropertyTypeValues) {
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
        OntologyOperations.addDataPropertyToIndividual(ontology, manager, ind, propertyValues);
    }



    /**
     * Creates the Object Properties between two individuals
     * @param ontology OWLOntology
     * @param manager OWLOntologyManager
     * @param firstIndClass Name of the class of the first individual
     * @param firstInd OWLIndividual of the first Individual
     * @param secondIndClass Name of the class of the second individual
     * @param secondInd OWLIndividual of the second Individual
     * @param objectPropertiesType HashMap with Object properties Type for each class
     */
    public static void createObjectPropertiesBetweenInds(OWLOntology ontology, OWLOntologyManager manager, String firstIndClass, 
    OWLIndividual firstInd, String secondIndClass, OWLIndividual secondInd, Map<String, Map<String, String>> objectPropertiesType) {
        IRI ontologyIRI = ontology.getOntologyID().getOntologyIRI().get();

        String firstIndClassIRIString = "<" + ontologyIRI + "#" + firstIndClass + ">";
        String secondIndClassIRIString = "<" + ontologyIRI + "#" + secondIndClass + ">";

        Map<String, String> objectPropertiesFirstInd = objectPropertiesType.get(firstIndClass);
        Map<String, String> objectPropertiesSecondInd = objectPropertiesType.get(secondIndClass);

        if (objectPropertiesFirstInd != null) {
            // For each Object property of the first Individual
            for(String objectProperty : objectPropertiesFirstInd.keySet()) {
                if (objectPropertiesFirstInd.get(objectProperty).equals(secondIndClassIRIString)) {
                    OntologyOperations.addObjectPropertyToIndividual(ontology, manager, firstInd, secondInd, IRI.create(ontologyIRI + "#" + objectProperty));
                }
            }
        }
        
        if (objectPropertiesSecondInd != null) {
            // For each Object property of the second Individual
            for(String objectProperty : objectPropertiesSecondInd.keySet()) {
                if (objectPropertiesSecondInd.get(objectProperty).equals(firstIndClassIRIString) ) {
                    OntologyOperations.addObjectPropertyToIndividual(ontology, manager, secondInd, firstInd, IRI.create(ontologyIRI + "#" + objectProperty));
                }
            }
        }   
    }

    /**
     * Creates all the Object Properties for individuals of a given class
     * @param ontology OWLOntology
     * @param manager OWLOntologyManager
     * @param indClassName Name of the class
     * @param classesMap HashMap with Name of Class as Key and OWLClass as Value
     * @param objectPropertiesType HashMap with Object properties Type for each class
     */
    public static void createObjectPropertiesForAClassOfInd(OWLOntology ontology, OWLOntologyManager manager, 
    String indClassName, Map<String, OWLClass> classesMap, Map<String, Map<String, String>> objectPropertiesType) {

        OWLClass indClass = classesMap.get(indClassName);

        Set<OWLNamedIndividual> individuals = OntologyOperations.getIndividualsOfClass(ontology, indClass);
        Set<OWLNamedIndividual> individualsOtherClass = null;

        Map<String, String> objectPropertiesClass = objectPropertiesType.get(indClassName);

        String otherClassStr = "";
        String otherClassName = "";

        OWLClass otherClass = null;

        if (objectPropertiesClass != null) {
            for(OWLIndividual ind : individuals) {
                for(String objectProperty : objectPropertiesClass.keySet()) {
                    otherClassStr = objectPropertiesClass.get(objectProperty).replace("<", "").replace(">", "");
                    otherClassName = otherClassStr.substring(otherClassStr.lastIndexOf("#") + 1);
                    otherClass = classesMap.get(otherClassName);
                    
                    individualsOtherClass = OntologyOperations.getIndividualsOfClass(ontology, otherClass);
                    for(OWLIndividual indOther : individualsOtherClass) {
                        createObjectPropertiesBetweenInds(ontology, manager, indClassName, ind, otherClassName, indOther, objectPropertiesType);
                    }
                }
            }
        }
    }


    

     /**
      * Creates the individuals recursively
      * @param ontology OWLOntology
      * @param manager OWLOntologyManager
      * @param classesMap HashMap with Name of Class as Key and OWLClass as Value
      * @param elem JSONObject with the individual information
      * @param parentName String with Parent Element Name
      * @param parentClass String with Parent Element Class
      * @param objectPropertiesType HashMap with Object properties Type for each class
      * @param dataPropertiesType HashMap with Data properties Type for each class
      * @param dataPropertyTypeValues JSONObject with vocabularios for data Ranges
      */
    public static void createIndInsideElem(OWLOntology ontology, OWLOntologyManager manager, Map<String, OWLClass> classesMap, 
    JSONObject elem, String parentName, String parentClass, Map<String, Map<String, String>> objectPropertiesType, 
    Map<String, Map<String, String>> dataPropertiesType, JSONObject dataPropertyTypeValues) {
        OWLDataFactory dataFactory = ontology.getOWLOntologyManager().getOWLDataFactory();
        IRI ontologyIRI = ontology.getOntologyID().getOntologyIRI().get();
        Set<String> attributes = elem.keySet();
        int id = 0;
        String indName;

        OWLIndividual parentInd = OntologyOperations.getIndividual(ontology, IRI.create(ontologyIRI.toString() + "#" + parentName));

        for(String attribute : attributes) {
            id = 0;
            if (elem.get(attribute) instanceof List) {
                List<JSONObject> attributeList = (List<JSONObject>) elem.get(attribute);

                int quantity = attributeList.size();
                OWLClass indClass = classesMap.get(attribute);

                
                if (indClass != null) {
                    for(int i = 0; i < quantity; ++i) {
                        indName = parentName + attribute + id;
                        IRI indIRI = IRI.create(ontologyIRI.toString() + "#" + indName);
                        id++;

                        OWLIndividual ind = OntologyOperations.createIndividual(ontology, manager, indIRI, indClass);

                        createDatapropertiesForInd(ontology, manager, attribute, attributeList.get(i), ind, objectPropertiesType, dataPropertiesType, dataPropertyTypeValues);




                        createObjectPropertiesBetweenInds(ontology, manager, parentClass, parentInd, indClass.getIRI().getShortForm(), ind, objectPropertiesType);
        



                        createIndInsideElem(ontology, manager, classesMap, attributeList.get(i), indName, indClass.getIRI().getShortForm(), objectPropertiesType, dataPropertiesType, dataPropertyTypeValues);
                    }
                }else {
                    if (attribute == "keywords") {
                        IRI indIRI = IRI.create(ontologyIRI.toString() + "#" + parentName);
                        OWLIndividual ind = dataFactory.getOWLNamedIndividual(indIRI);
                        
                        for(int i = 0; i < quantity; ++i) {
                            createDatapropertiesForInd(ontology, manager, attribute, attributeList.get(i), ind, objectPropertiesType, dataPropertiesType, dataPropertyTypeValues);
                        }
                    }
                }
            }
        }
    }


    /**
     * Check if String is in JSONArray with Case Insensitive
     * @param obj JSONArray of strings
     * @param value String to check
     * @return Boolean if JSONArray contains String
     */
    public static Boolean containsCaseInsensitive(JSONArray obj, String value) {
        String temp = null;
        for (int i = 0; i < obj.size(); i++) {    
            temp = (String) obj.get(i);
            temp = temp.trim().replaceAll("\\s+", "");
            if (temp.equalsIgnoreCase(value.trim().replaceAll("\\s+", ""))) {
                return true;
            }
        }
        return false;
    }
    

    /**
     * Parses the String value to the corresponding type
     * @param ontology OWLOntology
     * @param propertyType String with property Type
     * @param value String with the value to be parsed
     * @param dataPropertyTypeValues JSONObject with vocabularios for data Ranges 
     * @return
     */
    public static OWLLiteral getDataPropertyValue(OWLOntology ontology, String propertyType, String value, JSONObject dataPropertyTypeValues) {
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

            case "<https://w3id.org/dcso#yesNoUnknownVocabulary>":
                JSONArray typesYesNoUnk = (JSONArray) dataPropertyTypeValues.get("yesNoUnknownVocabulary");

                if (!containsCaseInsensitive(typesYesNoUnk, value)) {
                    System.out.println(value + " Should Be Yes/No/Unknown");
                } else {
                    dataPropertyValue = dataFactory.getOWLLiteral(value);
                }
                break;
            
            case "<https://w3id.org/dcso#iso639-3_values>":                        
                JSONArray typesISO639 = (JSONArray) dataPropertyTypeValues.get("iso639-3_values");

                if (!containsCaseInsensitive(typesISO639, value)) {
                    System.out.println(value + " Should Be Complient with ISO639-3");
                } else {
                    dataPropertyValue = dataFactory.getOWLLiteral(value);
                }
                break;

            case "<https://w3id.org/dcso#role_values>":
                JSONArray typesRole = (JSONArray) dataPropertyTypeValues.get("role_values");

                if (!containsCaseInsensitive(typesRole, value)) {
                    System.out.println(value + " Should Be Complient with role Values");
                } else {
                    dataPropertyValue = dataFactory.getOWLLiteral(value);
                }
                break;

            case "<https://w3id.org/dcso#iso-4217-code>":
                JSONArray typesISO4217 = (JSONArray) dataPropertyTypeValues.get("iso-4217-code");

                if (!containsCaseInsensitive(typesISO4217, value)) {
                    System.out.println(value + " Should Be Complient with ISO4217");
                } else {
                    dataPropertyValue = dataFactory.getOWLLiteral(value);
                }
                break;

            case "<https://w3id.org/dcso#fundingStatusVocabulary>":
                JSONArray typesFundingSt = (JSONArray) dataPropertyTypeValues.get("fundingStatusVocabulary");

                if (!containsCaseInsensitive(typesFundingSt, value)) {
                    System.out.println(value + " Should Be Complient with fundingStatusVocabulary");
                } else {
                    dataPropertyValue = dataFactory.getOWLLiteral(value);
                }
                break;

            case "<https://w3id.org/dcso#dataAccessVocabulary>":
                JSONArray typesDataAccess = (JSONArray) dataPropertyTypeValues.get("dataAccessVocabulary");

                if (!containsCaseInsensitive(typesDataAccess, value)) {
                    System.out.println(value + " Should Be Complient with dataAccessVocabulary");
                } else {
                    dataPropertyValue = dataFactory.getOWLLiteral(value);
                }
                break;
                
            case "<https://w3id.org/dcso#certified_with_values>":
                JSONArray typesCertified = (JSONArray) dataPropertyTypeValues.get("certified_with_values");

                if (!containsCaseInsensitive(typesCertified, value)) {
                    System.out.println(value + " Should Be Complient with certifiedWith_values");
                } else {
                    dataPropertyValue = dataFactory.getOWLLiteral(value);
                }
                break;

            case "<https://w3id.org/dcso#iso-3166-1-alpha2>":
                JSONArray typesISO3166 = (JSONArray) dataPropertyTypeValues.get("iso-3166-1-alpha2");

                if (!containsCaseInsensitive(typesISO3166, value)) {
                    System.out.println(value + " Should Be Complient with iso-3166-1-alpha2");
                } else {
                    dataPropertyValue = dataFactory.getOWLLiteral(value);
                }
                break;

            case "<https://w3id.org/dcso#pid_system_values>":
                JSONArray typesPid = (JSONArray) dataPropertyTypeValues.get("pid_system_values");

                if (!containsCaseInsensitive(typesPid, value)) {
                    System.out.println(value + " Should Be Complient with PID System values");
                } else {
                    dataPropertyValue = dataFactory.getOWLLiteral(value);
                }
                break;


            default:
                System.out.println("Doesnt have a case for this type " + propertyType);
                break;
            }

        return dataPropertyValue;
    }


}