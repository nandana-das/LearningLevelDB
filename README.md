
This repository contains a Maven project for a library management system that uses LevelDB for caching. The project includes the following files:

**pom.xml**:The Maven configuration file that includes dependencies for LevelDB, MySQL, and JSON serialization.
**LibraryManager.java**: The main class that handles storing data from a MySQL database to LevelDB, retrieving book details, and closing resources.
**LRUCache.java**: A class that implements an LRU cache using LevelDB.



**How to use**

Replace the placeholders in the main method of 'LibraryManager.java' with your actual database URL, username, password, and cache directory.
Run the main method to store data from the database to LevelDB, retrieve book details, and close resources.


**Dependencies**

The project uses the following dependencies:

'LevelDB': A fast key-value storage library.
'MySQL Connector/J': A driver that implements the JDBC API for connectivity to the MySQL database.
'Google Gson': A library for serializing and deserializing Java objects to and from JSON.
'Jackson Databind': A library for handling JSON data binding.


**Building**

To build the project, use the following command:

'mvn clean install'

This will compile the project, run any tests, and install the project in your local Maven repository.
