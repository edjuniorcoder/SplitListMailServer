/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.com.edjuniorcoder.util.email;

/**
 *
 * @author jerry
 */
import java.util.Hashtable;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

public class MXRecordChecker {

    public static boolean isValidMX(String email) {
        System.out.println("Email: " + email);
        int atIndex = email.lastIndexOf("@");
        String domain = email.substring(atIndex + 1);
        try {
            Hashtable env = new Hashtable();
            env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
            DirContext ictx = new InitialDirContext(env);
            Attributes attributes = ictx.getAttributes(domain, new String[]{"MX"});

            //InitialDirContext context = new InitialDirContext();
            //Attributes attributes = context.getAttributes("dns:/" + domain, new String[] { "MX" });
            System.out.println("attributes: " + attributes);
            return attributes.get("MX") != null;
        } catch (NamingException e) {
            e.printStackTrace();
            return false;
        }
    }

}
