package org.bme.mit.iir;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.OWLReasonerRuntimeException;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

public class Reasoning {

    public static final String PCSHOP_ONTOLOGY_FNAME = "pc_shop.owl";
    public static final String PCSHOP_BASE_URI = "http://mit.bme.hu/ontologia/iir_labor/pc_shop.owl#";
    public static final IRI ANNOTATION_TYPE_IRI = OWLRDFVocabulary.RDFS_LABEL.getIRI();

    OWLOntologyManager manager;
    OWLOntology ontology;
    OWLReasoner reasoner;
    OWLDataFactory factory;

    public Reasoning(String ontologyFilename) {
        manager = OWLManager.createOWLOntologyManager();

        // Töltsük be az ontológiát egy fizikai címről.
        // [Az ontológiáknak külön van logikai és fizikai címe, a kettő közötti
        // leképezést az URIMapper-ek határozzák meg: manager.addURIMapper(...).
        // Erre ontológiák közötti függőségek (import) kialakításánál van
        // szükség.]
        ontology = null;

        try {
            ontology = manager.loadOntologyFromOntologyDocument(
                    new File(ontologyFilename));
        } catch (Exception e) {
            System.err.println("Error while loading the ontology:\n\t"
                    + e.getMessage());

            System.exit(-1);
        }

        System.out.println("Ontology loaded: " +
                manager.getOntologyDocumentIRI(ontology));

        OWLReasonerFactory reasonerFactory = new org.semanticweb.HermiT.Reasoner.ReasonerFactory();
        reasoner = reasonerFactory.createReasoner(ontology);

        try {
            if (!reasoner.isConsistent()) {
                System.err.println("The ontology isn't consistent!");

                Node<OWLClass> inconsistentClasses = reasoner.getUnsatisfiableClasses();

                System.err.println("The following classes aren't consistent: "
                        + Util.join(inconsistentClasses.getEntities(), ", ") + ".");

                System.exit(-1);
            }
        } catch (OWLReasonerRuntimeException e) {
            System.err.println("A reasoning error occurred: " + e.getMessage());

            System.exit(-1);
        }

        // Példányosítsuk az OWLDataFactory-t, aminek a segítségével létre tudunk
        // hozni URI alapján OWL entitásokat (osztályt, tulajdonságot, stb).
        factory = manager.getOWLDataFactory();
    }

    // Lekérdezi egy osztály leszármazottait a PC-Shop ontológiában.
    // className: a keresett osztály neve
    // Az OWL következtető Set<Set<..>> struktúrákban, azaz halmazok
    // halmazaként adja vissza a lekérdezés eredményét. A belső halmazok
    // egymással ekvivalens, jelentés szempontjából megegyező dolgokat
    // tartalmaznak. Például egy lekérdezés eredménye:
    // { { Kutya, Eb }, { Macska, Cica }, { Ló } }
    // Ez a függvény a fenti halmazt "kilapítva" adja vissza:
    // { Kutya, Eb, Macska, Cica, Ló }
    public Set<OWLClass> getSubClasses(String className, boolean direct) {
        IRI classIRI = IRI.create(PCSHOP_BASE_URI + className);
        // Ellenőrizzük, hogy az osztály szerepel-e az ontológiában.
        // (Ha olyan dologra kérdezünk rá a következtetővel, ami nem szerepel az
        // ontológiában, az nem vezet hibához az OWL nyílt világ feltételezése
        // miatt.)
        if (!ontology.containsClassInSignature(classIRI)) {
            System.out.println("There's no such class in the ontology: \"" +
                    className + "\"");

            return Collections.emptySet();
        }

        OWLClass owlClass = factory.getOWLClass(classIRI);
        NodeSet<OWLClass> subClasses;

        try {
            subClasses = reasoner.getSubClasses(owlClass, direct);
        } catch (OWLReasonerRuntimeException e) {
            System.err.println("An error occurred while reasoning the subclasses: "
                    + e.getMessage());

            return Collections.emptySet();
        }
        // Kiírjuk az eredményt, az ekvivalens osztályokat
        // egyenlőségjellel elválasztva.
        System.out.println("Subclasses of \"" + className + "\":");

        for (Node<OWLClass> subClass : subClasses.getNodes()) {
            System.out.println("  - " + Util.join(subClass.getEntities(), " = "));
        }

        return subClasses.getFlattened();
    }

    // Lekérdezi egy OWL entitás (osztály, tulajdonság vagy egyed) annotációit
    // az ontológiából. (Ehhez nincs szükség a következtetőre.)
    // Az annotációknak sok fajtája van, a lekérdezett típust az
    // ANNOTATION_TYPE_IRI konstans tartalmazza, jelen esetben rdfs:label.
    public Set<String> getClassAnnotations(OWLEntity entity) {
        OWLAnnotationProperty label = factory.getOWLAnnotationProperty(ANNOTATION_TYPE_IRI);

        Set<String> result = new HashSet<String>();

        for (OWLAnnotation annotation : entity.getAnnotations(ontology, label)) {
            if (annotation.getValue() instanceof OWLLiteral) {
                OWLLiteral value = (OWLLiteral) annotation.getValue();
                result.add(value.getLiteral());
            }
        }

        return Collections.unmodifiableSet(result);
    }

    public static void main(String[] args) {
        Reasoning reasoning = new Reasoning(
                PCSHOP_ONTOLOGY_FNAME);

        // Végezzük keresőszó-kiegészítést az "alkatrész" kulcsszóra
        // az osztály leszármazottai szerint!
        final String term = "alkatrész";

        Set<OWLClass> descendants = reasoning.getSubClasses(term, false);
        System.out.println("Query expansion by descendants: ");

        for (OWLClass owlClass : descendants) {
            // Az eredmények közül a beépített OWL entitásokat ki kell szűrnünk.
            // Ezek itt az osztályhierarchia tetejét és alját jelölő
            // "owl:Thing" és "owl:Nothing" lehetnek.
            if (!owlClass.isBuiltIn()) {
                Set<String> labels = reasoning.getClassAnnotations(owlClass);

                System.out.println("\t- "
                        + term + " -> " + owlClass.getIRI().getFragment()
                        + " [" + Util.join(labels, ", ") + "]");
            }
        }
    }
}
