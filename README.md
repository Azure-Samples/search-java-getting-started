---
services: Azure Search, Java
platforms: Azure
author: azure
---

# Getting Started with Azure Search using JAVA

This is a sample of how to interact with Azure Search using Java.  Not only does it execute most of the common API requests against Azure Search, but it also implements some of the best practices such as handling retries, etc.  

## Running this sample

To run this sample, you will need to have an Azure Search service and add your Search Service name as well as API key for your Search service tot he App.java file located under \src\main\java\com\microsoft\azure\search\samples\console.  After that, you should be able to simply load it and run it.  One thing to note is that you will need to have JDK 1.7 or higher.   

When you run the sample, it will do the following:
1.	Create an Index - createIndex(indexClient, true);
2.	Upload documents to an Index - indexData(indexClient);
3.	Perform a simple search query - searchSimple(indexClient);
4.	Do a more complex search - searchAllFeatures(indexClient);
5.	Do an item lookup - lookup(indexClient);
6.	Perform a suggest query (used for type ahead) - suggest(indexClient);
