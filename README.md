# ThemisForConformance
Extension of Themis for analysis conformance between a set of models. It automatically generates an ontology for those models without ontology related, e.g., ISO norms, based on the set of tests taken as input.


## Prerrequisites
* Define the test cases following this [approach](https://link.springer.com/chapter/10.1007/978-3-030-03667-6_8)
* Store the test cases in an RDF file following the [Verification Test Case ontology](https://w3id.org/def/vtc#). (The online tool [Themis](http://themis.linkeddata.es) can be used to generate this RDF file with the test cases)

## Running the tests

* Download the jar
* Create a CSV where you indicate the ontology/model and the RDF file associated to it, e.g.:
```
URL;Tests
http://iot.linkeddata.es/def/core;testsuite-core.ttl
http://iot.linkeddata.es/def/wot;testsuite-wot.ttl
```


* Execute the command: 
```
java -jar themisForConfomance.jar -i tests.csv
```
> **_NOTE:_**  The tool will automatically create a glossary of terms for each ontology in order to execute the tests that map each term in the test with a term in the ontology. Once it is generated, the tool will ask the user whether the glossary of terms that is created is correct, so that the user can change it if needed.

## Results
This tool will provide: 
* A CSV file with the glossary of terms for each ontology/model
* A CSV file with the tests results for each ontology/model (individual results)
* A CSV file with the tests that are passed by more than one ontology (joint results), indicating (1) The test, (2) the ontologies/models that passes the test

