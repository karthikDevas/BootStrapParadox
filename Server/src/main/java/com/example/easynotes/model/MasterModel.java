package com.example.easynotes.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Calendar;

public class MasterModel
{
    public static void doItemCatelogueAction(String arg2, int arg3)
    {
        try
        {
            // create a mysql database connection
            String myDriver = "org.gjt.mm.mysql.Driver";
            String myUrl = "jdbc:mysql://localhost/dunzo";
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl, "root", "");
            // the mysql insert statement
            String query = " Insert into dunzo.Items values(default, ?, ?)";

            // create the mysql insert preparedstatement
            PreparedStatement preparedStmt = conn.prepareStatement(query);
            preparedStmt.setString (1, arg2);
            preparedStmt.setInt    (2, arg3);

            // execute the preparedstatement
            preparedStmt.execute();

            conn.close();
        }
        catch (Exception e)
        {
            System.err.println("Got an exception!");
            System.err.println(e.getMessage());
        }

    }


    public static void doStoreUpdateAction(String storeName, String doorNo, String street, String city, String state, int rating, String GSTIN)
    {
        try
        {
            // create a mysql database connection
            String myDriver = "org.gjt.mm.mysql.Driver";
            String myUrl = "jdbc:mysql://localhost/dunzo";
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl, "root", "");
            // the mysql insert statement
            String query = " Insert into dunzo.Stores values(default, ?, ?, ?, ?, ?, ?)";

            // create the mysql insert preparedstatement
            PreparedStatement preparedStmt = conn.prepareStatement(query);
            preparedStmt.setString (1, storeName);
            preparedStmt.setString (2, doorNo);
            preparedStmt.setString (3, street);
            preparedStmt.setString (4, city);
            preparedStmt.setString (5, state);
            preparedStmt.setInt    (6, rating);
            preparedStmt.setString (7, GSTIN);

            // execute the preparedstatement
            preparedStmt.execute();

            conn.close();
        }
        catch (Exception e)
        {
            System.err.println("Got an exception!");
            System.err.println(e.getMessage());
        }

    }
}
