package database;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DatabaseConnection {

	private static final String Null = null;
	//Connection con= null;
	static ResultSet rs,rs1,rs2,rs3,rs4,rs5,rs6,rs7,rs8,rs9,rs10,rs11,rs12,rs13;
	//private ConnectionPooling pooling ;
	Connection con = null;
    private static final String DB_URL = "jdbc:mysql://localhost:3306/svlm1"; 
	//static ResultSet rs2;

	//Statement stmt;
	PreparedStatement stmt1=null,stmt2=null,stmt3=null,stmt4=null,stmt5=null,stmt6=null,stmt7=null,stmt8=null,stmt9=null,stmt10=null;
	
	PreparedStatement pstmt,pstmt1,pstmt2,pstmt3,pstmt4,pstmt5,pstmt6,pstmt7,pstmt8,pstmt9,pstmt10,pstmt11,pstmt12,pstmt13;
	
	DatabaseConnection()
	{
		try
		{
			Class.forName("org.postgresql.Driver").newInstance();
			con=DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/nyc","postgres","admin");

			 //pooling = new ConnectionPooling("com.mysql.jdbc.Driver", "jdbc:mysql://localhost:3306/svlm1", "root", "admin",100);

			//this.pooling = new ConnectionPooling("com.mysql.jdbc.Driver""jdbc:mysql://localhost:3306/svlm1","root","root",3);
			//con = pooling.getConnection();
			//this.con.setAutoCommit(false);
			//stmt=con.createStatement();
			if(!con.isClosed())
			{
				System.out.println("Successfully Connected");
			}
		}
		 
		catch(SQLException e)
		{
			
			e.printStackTrace();
		}
		
	/*	catch(InstantiationException e)
		{
			
			e.printStackTrace();

		}
		
		
		catch(IllegalAccessException e)
		{
			
			e.printStackTrace();

		}
		*/
		catch(ClassNotFoundException e)
		{
			
			e.printStackTrace();

		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Connection getConnection()
	{
		return con;
	}


}
	



	
	
	
	
	
	
	


