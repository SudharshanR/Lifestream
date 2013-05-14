package Hibernate;

import java.util.Properties;
import java.util.Random;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

import poke.Model.ImageTable;




public class HibernateTest {
	private static ServiceRegistry serviceRegistry;
	
	/*public static void main(String args[])
	{
		
		HibernateTest t=new HibernateTest();
		t.persistImage("lllllllllnnnnnnnnkkkk");
		
		
	}*/

public void persistImage(String tag,byte[] bArayy)
{
	System.out.println("-----------------------> In persist Image");
	ImageTable img=new ImageTable();
	   Random randomGenerator = new Random();
	   
	img.setUserId(""+randomGenerator.nextInt(1000));	
	
	img.setImage(bArayy);
	
	
	   Properties properties = new Properties();
	
	   properties.setProperty("hbm2ddl.auto", "create");
	    properties.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
	    properties.setProperty("hibernate.connection.url", "jdbc:postgresql://127.0.0.1:5432/nyc");
	    properties.setProperty("hibernate.connection.username", "postgres");
	    properties.setProperty("hibernate.connection.password", "admin");
	   // properties.setProperty("hibernate.show_sql", "com.mysql.jdbc.Driver");
	    properties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");

	  
	
	    AnnotationConfiguration  newconfiguration=new AnnotationConfiguration ().setProperties(properties);
	
	    newconfiguration.addAnnotatedClass(poke.Model.ImageTable.class);
   
	serviceRegistry = new ServiceRegistryBuilder().applySettings(newconfiguration.getProperties()).buildServiceRegistry();	
	SessionFactory sessionFactory = newconfiguration.buildSessionFactory(serviceRegistry);

	
	
	
	Session session=sessionFactory.openSession();
	session.beginTransaction();
	session.save(img);
	
	session.getTransaction().commit();
}	

}
