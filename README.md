# Prometheus AI
COMP 396 Research Project - Supervised by Prof. Vybihal

## Introduction

Prometheus AI models the human brain to control a swarm robotics system.

More information about the general project can be found [here] (https://github.com/seanstappas/prometheus-ai/tree/master/reports)

From June - August 2017, I implemented various features in the Expert System (ES), one of the various layers of Prometheus AI, as well developing the 'TAG' data structure used by the KNN and ES layers.

## Dependencies

* Java 8
* TestNG
* [KNN] (https://github.com/seanstappas/prometheus-ai/tree/master/src/main/java/knn)

## Documentation

The latest JavaDoc for Prometheus AI can be found [here] (http://seanstappas.me/prometheus-ai/)

## Changes To ES

### TAG

For all Tag classes, toString methods was implemented that reflect additional new fields, in order to aid verification of the results of Unit testing.

#### Rule 

New Rule constructors were added. To generate a single Rule from a string, the method first splits the string into tokens. The index where the token matches “->” is found. All elements prior to this index are input Facts, and all after are output Tags. The appropriate Fact and Recommendation constructors are called, to create two arrays of Tags – which form the Rule.

There is now the option to create many Rules from the same string by using the makeRules method. For example, given the string “P1(ARG1,ARG2) P2(ARG3) OR P3(ARG4,ARG5) P4(ARG6,ARG7) -> @P3(ARG4,ARG5,ARG6)”, two Rules are returned "P1(ARG1,ARG2) P2(ARG3) -> @P3(ARG4,ARG5,ARG6)" and "P3(ARG4,ARG5) P4(ARG6,ARG7) -> @P3(ARG4,ARG5,ARG6)".

The confidence values of all outputPredicates of a Rule are set to the product of the confidence values of the input Facts. i.e. “P1(ARG1,ARG2) [0.5] P2(ARG3) [0.5] -> @P4(ARG4,ARG5,ARG6) [0.25] ”. Confidence values can be seen to reflect the certainty of a specific Tag in the system.

#### Predicate Interface

A predicate interface has been created, which is implemented by Fact and Recommendation. Now, Rules have IPredicate [] as output which helps to avoid issues associated with typecasting.

#### Fact

The Fact class is defined by a predicateName and a list of arguments. A Fact object may have no arguments, one arguments or many of them. An arrayList was chosen as the data structure due to speed of iteration and access, as well as the fact that it maintains order (unlike hashsets).

Facts are parsed from a string by splitting it on characters “(“, “)” and “,”, thus turning “P(A1,A2,A3)” into [“P”,”A1”,’A2”,”A3”]. predicateName is set to the first object in the list, and all else have their appropriate argument constructors called. If the argument string contains numeric digits – it is a numeric argument; if it contains “?” or “*” or has a value proceeded by ‘&’ character– it is a variable argument; otherwise it is a string argument.

A new method for matching two facts was created. The method returns a VariableReturn object. With the introduction of inequalities (“>”, “<), negations (“!=”), and variables (“?”, “*”, “&x”), simply using an equals() method does not suffice.

One important thing to keep in mind is that matching in many cases in unidirectional. Meaning that `colour = black` implies `colour != brown`, but `colour != brown` does not imply `colour = black`. This had to be reflected in each method.

The matching method first checks whether the two predicateNames are equal, returning false if not. If the fact from a Rule has more arguments, the method iterates through those arguments at a greater index than this.arguments.size(). If these arguments contain anything other than “*”, the method returns false – since we treat Predicates as “standard format”. Elsewhere, when this has more or equal arguments the method iterates through, comparing each argument with the corresponding one from the other Fact. If any one of the Facts contains “*”, the method will return true. If there is a variable argument, a matching pair is placed in the pairs hashmap. If at the end of the traversal all arguments are matching, the method returns an instance of VariableReturn, with doesMatch == true.

#### Argument

There are three subtypes of the Argument class – NumericArgument, StringArgument, VariableArgument. The first two subtypes possess their own matches method – using simple if, else statements to do logical comparisons. The VariableArgument does not have its own matches method – Fact.matches assumes that any comparison with a variableArgument evaluates as true.

Argument has two fields name and symbol. NumericArgument and VariableAregument also have a value and isNeg field. The symbol field describes the different cases an argument can take (see JavaDoc), and is used in matching methods.

### ES

Think has been extended so that it may generate a new Rule after reaching quiescence. This new Rule takes the Facts present in the ES before a thinkCycle occurs as inputFacts and the Predicates activated by think as outputPredicates – before making a new Rule from these sets and adding the Rule to the ES. This is a Rule that has been “proven” by the think cycles.

With the introduction of Variable Arguments, there becomes a need to replace variables with the actual strings or numbers that they are shown to represent. When Fact.match encounters a Variable Argument during its iteration through the input facts, it puts the name of variable as a key and the argument that replaces it (found in the matching fact) into a hashmap. The hashmap is named pairs, and found in the VariableReturn object. Then, during a think cycle, when a Rule is activated, the arguments of its outputPredicates are searched for the key, and when found, the argument is replaced with its corresponding value. In this way, variable replacement can be done with only one iteration through the Tags of a Rule.

There is a new rest method which for now merged two Rules at a time to generate a new one in this manner: Rule 1 A -> B, Rule 2 B ->C, generates new Rule 3 A -> C. The Rule merger iterates through the whole set of ready rules, comparing every Rule to each other for a merge. This process can then be done for more cycles on the previous set of Rules generated.

The teach method has been created, which allows very basic natural language to be parsed as a Rule. It operates by tokenizing the string and searching the tokens for a small collection adverbs which indicate whether the Predicates following them are input Facts or output Predicates. From there, the tokens can be passed onto the constructor methods for the Predicates, before they themselves are used by a Rule constructor.

### TestIntegration

JUnit testing was extended to include the new features of the Expert System. Full code coverage of multi-line methods was implemented to ensure code stability. testES was extended to also include confidence values while testESCycles and testES&Learn were implemented to test think for a set number of cycles, and to generate rules - as mentioned above.

The KNN team, implemented testKNNandES. This demonstrates the functionality of KNN & ES together in providing an appropriate recommendation to the META layer. If META does not use the TAG module, a toString() method can always be used to provide the recommendation input as a string.

## Possible Future Improvements

There is much room for speed improvements when Fact matching and replacing variables. Currently the matching on a Rule has an average runtime of O(nm) where n is the number of Tag and m is the number of arguments per Tag. Given the average Rule contains 3 Tags with 3 Predicates each, the algorithm may slow down considerably on a larger ruleset. A method of caching each Tag will lead to a quicker runtime.

The teach method is currently very rudimentary. It would be interesting to use Natural Language Processing techniques in order to recognize more complicated string sentences and generate Rules from them.

There is also scope for machine learning within rest in order to generate sensible Rules that can increase both the efficiency and effectiveness of the system. The current method of Rule merging is computationally expensive O(nr^2) and does not necessarily generate useful Rules.
