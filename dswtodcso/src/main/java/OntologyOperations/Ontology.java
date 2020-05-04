package OntologyOperations;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.FunctionalSyntaxDocumentFormat;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;

public class Ontology {

    public static OWLOntology importOntology(File file, OWLOntologyManager manager) {
        try {
            OWLOntology ontology;
            ontology = manager.loadOntologyFromOntologyDocument(file);

            return ontology;
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void saveOntology(File fileout, OWLOntology ontology, OWLOntologyManager manager) {
        try {
            manager.saveOntology(ontology, new FileOutputStream(fileout));
        } catch (OWLOntologyStorageException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static OWLIndividual createIndividual(OWLOntology ontology, OWLOntologyManager manager, IRI indIRI, OWLClass classInd) {
        OWLDataFactory dataFactory = ontology.getOWLOntologyManager().getOWLDataFactory();
        OWLIndividual ind = getIndividual(ontology, indIRI);

        OWLAxiom axiom = dataFactory.getOWLClassAssertionAxiom(classInd, ind);
        AddAxiom addAxion = new AddAxiom(ontology, axiom);
        manager.applyChange(addAxion);
        
        return ind;
    }

    public static OWLIndividual getIndividual(OWLOntology ontology, IRI indIRI) {
        OWLDataFactory dataFactory = ontology.getOWLOntologyManager().getOWLDataFactory();
        return dataFactory.getOWLNamedIndividual(indIRI);
    }

    public static void addDataPropertyToIndividual(OWLOntology ontology, OWLOntologyManager manager, OWLIndividual ind, 
    HashMap<IRI, OWLLiteral> propertyValues) {

        OWLDataFactory dataFactory = ontology.getOWLOntologyManager().getOWLDataFactory();
        OWLLiteral dataPropertyValue;
        OWLDataProperty property;
        OWLDataPropertyAssertionAxiom assertion;
        AddAxiom addAxiomChange;
        
        for (IRI iri : propertyValues.keySet()) {
            if (propertyValues.get(iri) != null) {
                property = dataFactory.getOWLDataProperty(iri);
                dataPropertyValue = propertyValues.get(iri);
                assertion = dataFactory.getOWLDataPropertyAssertionAxiom(property, ind, dataPropertyValue);
                addAxiomChange = new AddAxiom(ontology, assertion);
                manager.applyChange(addAxiomChange);
            }
        }
    }

    public static void addObjectPropertyToIndividual(OWLOntology ontology, OWLOntologyManager manager, OWLIndividual firstInd, 
    OWLIndividual secondInd, IRI propertyIRI) {

        OWLDataFactory dataFactory = ontology.getOWLOntologyManager().getOWLDataFactory();
        OWLObjectProperty property;
        OWLObjectPropertyAssertionAxiom assertion;
        AddAxiom addAxiomChange;
        
        property = dataFactory.getOWLObjectProperty(propertyIRI);
        assertion = dataFactory.getOWLObjectPropertyAssertionAxiom(property, firstInd, secondInd);
        addAxiomChange = new AddAxiom(ontology, assertion);
        manager.applyChange(addAxiomChange);
    }

    public static Set<OWLNamedIndividual> getIndividualsOfClass(OWLOntology ontology, OWLClass indClass) {
        OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
        OWLReasoner reasoner = reasonerFactory.createReasoner(ontology);
        NodeSet<OWLNamedIndividual> individuals = reasoner.getInstances(indClass, true);

        return individuals.getFlattened();
    }
}