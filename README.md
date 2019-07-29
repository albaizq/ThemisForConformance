# ThemisForConformance
Version of Themis for analysis conformance between a set of models.


## Prerrequisites
* Define the test cases following this [approach](https://link.springer.com/chapter/10.1007/978-3-030-03667-6_8)
* Store the test cases in an RDF file following the [Verification Test Case ontology](https://w3id.org/def/vtc#). (The tool [Themis](http://themis.linkeddata.es) can be used to generate this RDF file with the test cases)

## Running the tests

* Download the jar
* Create a CSV where you indicate the ontology/model and the path of the RDF file associated to it.
* Execute the command: 
```
java -jar coverageTest-jar-with-dependencies.jar -i tests.csv
```

## Results
This tool will provide: 
* A CSV file with the glossary of terms for each ontology/model
* A CSV file with the tests results for each ontology/model
* A CSV file with the tests that are passed by more than one ontology, indicating (1) The test, (2) the ontologies/models that passes the test

