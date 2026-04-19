package org.shippin.util;

import org.shippin.domain.User;
import org.shippin.domain.enums.Role;

//USER        0
//POWER_USER  1
//ADMIN       2

public class AuthService {

    public static boolean hasAccess(User user, Role requiredRole) 
    {
    	 if (user == null) 
    	 {
             System.out.println("AuthService Error: user je null");
             return false;
         }
    	 
    	 boolean result = user.getRole().ordinal() >= requiredRole.ordinal();
    	 
    	 if (result) 
    	 {
             System.out.println("AuthService Prístup povolený");
         } 
    	 else 
    	 {
             System.out.println("AuthService Prístup zamietnutý: nedostatočné práva");
         }

         return result;
    }
}