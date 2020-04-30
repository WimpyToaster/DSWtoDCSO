package OntologyOperations;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;

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
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

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
        
        OWLIndividual ind = dataFactory.getOWLNamedIndividual(indIRI);

        OWLAxiom axiom = dataFactory.getOWLClassAssertionAxiom(classInd, ind);
        AddAxiom addAxion = new AddAxiom(ontology, axiom);
        manager.applyChange(addAxion);
        
        return ind;
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

}