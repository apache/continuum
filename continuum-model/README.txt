
-------------------------------------
Running JPOX tools on Continuum model
-------------------------------------

1) To enhance Continuum model classes:
   
   1-1) Open a terminal/command prompt window.
   
   1-2) Change directory to 'continuum-model' directory.
   
   1-3) Run the following command
   
        >  mvn clean compile jpox:enhance
        
        
2) To generate DB schema for Continuum model to a file:

    2-1)  Follow (1-1) and (1-2) above
    
    2-2)  Run the following command
    
          >  mvn jpox:schema -Pddl