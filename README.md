# Infection-Visualization
W.I.P.

#Terminology
Coachees is sometimes mistakenly used interchangeably with neighbors. Since we are modelling infections that travel along both "coached by" and "coaches" relationships, the adjacency list for each key in the hashmap userGraph actually represents the neighbors (coachees and coaches for a given coach). 

#Usage
Run the jar in /out/artifacts.

Options: `[<number of nodes to generate> <base version float> <max degree per node>] [<infection type ("total" or "limited")> <target number affected> <new version float>]`

#### Example

`java -jar KaInfection.jar 100 0f 5 limited 30 1f`

Generates 100 user nodes with a base version of 0f. Performs a limited infection with a target of 30 user nodes with version 1f.
