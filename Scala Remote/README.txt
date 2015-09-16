
Instructions:
There are 2 .scala files->BitcoinRemote.scala and BitcoinLocal.scala

Step1.Upload BitcoinRemote.scala and BitcoinLocal.scala on different machines and change the hostname in the application.conf files of both remote and local to IP address of server. Place build.sbt for both directories to run using sbt. Also, rename both remoteApplication.conf, localApplication.conf to application.conf and place them to corresponding machines. Make sure to follow the directory structure as below:
-> "Scala Remote" or "Scala Local"/src/main/scala/remote/(BitcoinRemote.scala or BitcoinLocal.scala)
			   		    	 /resources/application.conf

Step2.To execute giving number of zeroes as input(n),start the server as follows
 
      sbt "run n"  

Step3. To accomodate additional workers by executing with IP address(of server) as input for any other local machine on the network, execute BitcoinLocal.scala as follows

    sbt "run <IP of server>"    
