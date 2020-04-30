package Utils;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

import uk.ac.manchester.cs.owl.owlapi.OWL2DatatypeImpl;

public class Utils {


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
     * 
     * @param ontology
     * @param propertyType
     * @param value
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


}