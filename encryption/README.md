# Project-3 encryption example
Example of storing encrypted password in database and verifying against encrypted password

### To run this example: 
1. Clone this repository.
2. Open IntelliJ -> Open -> Choose the project you just cloned (The root path must contain the pom.xml!) -> The IntelliJ will load automatically. 
3. Go to [UpdateSecurePassword.java](src/main/java/UpdateSecurePassword.java), make sure to change your mysql username and password. 
4. Run [UpdateSecurePassword.java](src/main/java/UpdateSecurePassword.java) on your local machine, it will update the passwords in your existing moviedb customers table from plain text to encrypted string. 
5. Go to [VerifyPassword.java](src/main/java/VerifyPassword.java), also change your mysql username and password. This program will verify if the email and password are valid.


To run it on AWS under command line:
1. Clone this repository using.
2. `cd cs122b-project3-encryption-example`
3. change your mysql username and password in [UpdateSecurePassword.java](src/main/java/UpdateSecurePassword.java) and [VerifyPassword.java](src/main/java/VerifyPassword.java)
4. `mvn compile`
5. Have a backup of the "customers" table, run the following:
<br>`create table customers_backup(`
   <br>`id integer auto_increment primary key,`
   <br>`firstName varchar(50) not null,`
   <br>`lastName varchar(50) not null,`
   <br>`ccId varchar(20) not null,`
   <br>`address varchar(200) not null,`
   <br>`email varchar(50) not null,`
   <br>`password varchar(20) not null,`
   <br>`foreign key(ccId) references creditcards(id));`
<br>`insert into customers_backup select * from customers;`
6. to run `UpdateSecurePassword`:
   <br>`mvn exec:java -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="UpdateSecurePassword"`
7. to run `VerifyPassword`:
   <br>`mvn exec:java -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="VerifyPassword"`
8. When execute java program using maven in command line, if the program doesn't exist after it finishes, you can just kill it.
9. To recover the data in the "customers" table, run the following:
   <br>`update customers C1 set password = (select password from customers_backup C2 where C2.id = C1.id);`
