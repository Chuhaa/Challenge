package net.contal.demo;

import java.util.Random;

public abstract class AccountNumberUtil {


    /**
     * This function should generate random integer number and return
     *
     * @return random 8 digits integer
     */
    public static int generateAccountNumber() {
        //Create random number in 8 digits
        Random random = new Random();
        return 10000000 + random.nextInt(90000000);
    }

}
