package dswtodcso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
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
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import OntologyOperations.*;


public class App 
{
    /** HashMap with uuid of a question as key and JSONObject with reply as value */
    public static Map<String, JSONObject> DSWReplies = new HashMap<>();

    /** HashMap with uuid for each question path in DSW */
    public static Map<String, JSONObject> pathsReplies = new HashMap<>();

    /** HashMap with the required Level for each attribute path */
    public static Map<String, String> requiredLevels = new HashMap<>();


    public static void main( String[] args )
    {
        //JSON parser object to parse read file
        JSONParser jsonParser = new JSONParser();
         
        try (FileReader readerDSWFile = new FileReader("dswtodcso/resources/testDSW.json");
            FileReader readerUuidMap = new FileReader("dswtodcso/resources/DSWUuidMap.json"))
        {
            //Read JSON file
            Object DSWFile = jsonParser.parse(readerDSWFile);
 
            JSONObject DSWQuestionaire = (JSONObject) DSWFile;

            Object UuidMapFile = jsonParser.parse(readerUuidMap);

            JSONObject UuidMapping = (JSONObject) UuidMapFile;

            gatherDataDSW.initializeDSWReplies(DSWQuestionaire, DSWReplies);

            gatherDataDSW.initializeRequiredLevel(DSWQuestionaire, requiredLevels);

            gatherDataDSW.initializePathsReplies(UuidMapping, pathsReplies);

            Map<String, Map<String, List<JSONObject>>> replies = new HashMap<>();
            replies.put("DMP", gatherDataDSW.getRepliesFormCategory("DMP", pathsReplies, DSWReplies, requiredLevels));
            replies.put("Contact", gatherDataDSW.getRepliesFormCategory("Contact", pathsReplies, DSWReplies, requiredLevels));
            replies.put("DMPStaff", gatherDataDSW.getRepliesFormCategory("DMPStaff", pathsReplies, DSWReplies, requiredLevels));
            replies.put("Project", gatherDataDSW.getRepliesFormCategory("Project", pathsReplies, DSWReplies, requiredLevels));
            replies.put("Cost", gatherDataDSW.getRepliesFormCategory("Cost", pathsReplies, DSWReplies, requiredLevels));
            replies.put("Dataset", gatherDataDSW.getRepliesFormCategory("Dataset", pathsReplies, DSWReplies, requiredLevels));

            // for(String cat : replies.keySet()) {
            //     System.out.println(replies.get(cat));
            //     System.out.println("");
            // }
            

            File ontologyFile = new File("dswtodcso/resources/rda-common-dmp.1.1.2.owl");

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

            createAllIndividuals(ontology, manager, classesMap, replies);
            
            //HashMap<IRI, OWLLiteral> propertyValues = new HashMap<>();
            //propertyValues = getPropertyValues(dataFactory, ontologyIRI, cost);

            //Ontology.createIndividual(ontology, manager, IRI.create(ontologyIRI.toString() + "#costTest"), classesMap.get("Cost"));



            // Set<OWLAxiom> axi = ontology.getAxioms();
            // for (OWLAxiom ax : ontology.getAxioms()) {
                
            //     System.out.println(ax);
            // }

            // for (OWLClass cls : classes) {
            //     System.out.println("+: " + cls.getIRI().getShortForm());

            //     System.out.println(" \tObject Property Domain");
            //     for (OWLObjectPropertyDomainAxiom op : ontology.getAxioms(AxiomType.OBJECT_PROPERTY_DOMAIN)) {
            //         if (op.getDomain().equals(cls)) {
            //             for (OWLObjectProperty oop : op.getObjectPropertiesInSignature()) {
            //                 if (cls.getIRI().getShortForm().equals(oop.getIRI().getShortForm()))
            //                     continue;
            //                 System.out.println("\t\t +: " + oop.getIRI().getShortForm() + "==Object Property");
            //             }
            //         }
            //     }

            //     System.out.println(" \tData Property Domain");
            //     for (OWLDataPropertyDomainAxiom dp : ontology.getAxioms(AxiomType.DATA_PROPERTY_DOMAIN)) {
            //         if (dp.getDomain().equals(cls)) {
            //             for (OWLDataProperty odp : dp.getDataPropertiesInSignature()) {
            //                 System.out.println("\t\t +: " + odp.getIRI().getShortForm() + "==Data Property");
            //             }
            //         }
            //     }
            // }









            File ontologyOut = new File("dswtodcso/resources/newOntology.owl");
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

        for(String cat : replies.keySet()) {

            for(String elem : replies.get(cat).keySet()) {
                
            }

            IRI indIRI = IRI.create(ontologyIRI.toString() + "#");

            System.out.println("Ind: IRI= " + indIRI + " class= " + classesMap.get(cat));
            System.out.println("");

            Ontology.createIndividual(ontology, manager, indIRI, classesMap.get(cat));
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
