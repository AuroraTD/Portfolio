import java.util.*;

public class Sleeper {


public static void rideTime () {

Random generator = new Random ( );
int milliseconds = (generator.nextInt(5) +1 ) * 1000;

System.out.println ("Riding for " + milliseconds/1000 + " seconds");

try {
            Thread.currentThread().sleep( milliseconds);
                        }
                        catch( InterruptedException e ){
                        }

}

public static void walkAround () {

Random generator = new Random ( );
int milliseconds = (generator.nextInt(10) +1 ) * 1000;

System.out.println ("Walking around for  " + milliseconds/1000 + " seconds");

try {
            Thread.currentThread().sleep( milliseconds);
                        }
                        catch( InterruptedException e ){
                        }

}


public static void main (String [] args) {

// example of usage

Sleeper.rideTime();
System.out.println ("Ride over");
Sleeper.walkAround();
System.out.println ("Completed walking around ...");
}
}
