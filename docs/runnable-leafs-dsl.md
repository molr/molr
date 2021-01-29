# Java embedded DSL for RunnableLeafsMissions

This document briefly describes the available features for the embedded java DSL for creating 
complex java missions which can be executed by molr. There are a few concepts which are important
to understand in order to profit from the full capacities of the language. We will come to them in 
a minute. However, lets start with an example, as usual...

### Hello World

The probably simplest snippet to construct a mission for the RunnableLeafsMole would look like this:
```java
RunnableLeafsMission helloWorldMission = 
new RunnableLeafsMissionSupport() {
{
    root("Hello World").as(root -> {
        root.leaf("Print 'Hello Molr'").run(() -> System.out.println("Hello Molr"));
    });
}
}.build();
``` 

This example shows already all the most important concepts which which shall 
be explained in the following sections.    

#### The RunnableLeafsMissionSupport
The most convenient way to describe a mission, is to inherit from the RunnableLeafsMissionSupport class.
The methods which are then inherited from this class are the starting point for describing
the structure of the mission. The most concise and readible form to use these methods is to all put them 
into an initializer block. Note: This is the reason for the additional bracket `{` at the start of this class.
To finally construct a mission from the `RunnableLeafsMissionSupport` its `build()` method has to be called.

Here a short snippet to illustrate all this:
```java
RunnableLeafsMission someMission = 
new RunnableLeafsMissionSupport() {
{ /* Notice this additional bracket pair - java initializer block */
    
    /* mission description goes here */

}
}.build(); /* Call the build method again at the end to create the mission */ 
```

NOTE: This is just for illustrative purpose as an empty mission description is not allowed. 
At least a minimal description as in the hello world example is required. 
We get to the details in a minute.   

#### The basic building blocks of the mission
The basic structure of RunnableLeafsMission is a tree structure which consists of the following building blocks:

##### branches

A **branch** can be seen as kind of a grouping of other content. Branches can contain other branches (and thus can be arbitrarily nested) 
and/or leafs. The central purporse of a branch is to determine, if its children shall be run sequentially or in parallel. 
A mission needs at least one branch, the **root branch**, which is defined using the following syntax: 
```java
root("Name of the mission").as( root -> {
    /* content of the branchs goes here */
}
);
```
* The `root()` method requires one parameter: The name of the branch (which in this case corresponds to the name of the mission).
* In the `as(..)` part of the clause, the actual content of the branch is defined. This has to be done by calling methods

* As the name 'RunnableLeafsMission' already suggests, **leaf**s are the parts which actually do something - They are nothing 
else than java runnables (with some variations).

To refer to a branche or a leaf, we use in this context the general term **block**. 


#### Mission livecycle






































