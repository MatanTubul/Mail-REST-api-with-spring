package com.javacodegeeks.snippets.enterprise;

import java.awt.List;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.mail.*;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;


@Controller
@RequestMapping()
public class RestController{
	private final String layout = "ViewLayout";
	private final String contentLayout = "ContentLayout";
	private static enum Status{
		RUNNING,ONHOLD,FINISHED,ERROR
	}
	Status status;
	
	 @RequestMapping(value = "/", method = RequestMethod.GET)
     public String welcome() {
           return "index";
     }
	
	//setup method - get username , password,host mail server
	@RequestMapping(value = "/setup/{username}/{password}/{host}/", method = RequestMethod.GET)
	public String Setup(@PathVariable("username") String username,@PathVariable("password") String pass,@PathVariable("host") String host ,ModelMap model) throws IOException {
		model.addAttribute("tag","Setup configuration");
		OutputStream output = null;
		Properties prop = null;
		File myfile = new File(username+".properties");
		try{
			if(!checkIfFileExists(myfile)){
				myfile.createNewFile();
				output = new FileOutputStream(myfile,false);
				prop = new Properties();
				prop.setProperty("username", username);
				prop.setProperty("password", pass);
				prop.setProperty("host", host);
				prop.setProperty("status", status.FINISHED.toString());
				prop.store(output, null);	
			}
			else{
					prop = loadProperties(username+".properties");
					if(prop == null){
				 		model.addAttribute("msg","Error: Status is not available: can not read properties file!");
				 		return layout;
				 	}
					if(!authenticatePassword(prop,pass)){
						model.addAttribute("msg","Error: Get status failed, password or username is inccorrect");
						return layout;
					}
					updateConfigfile(username+".properties","username",username);
					updateConfigfile(username+".properties","password",pass);
					updateConfigfile(username+".properties","host",host);
					updateConfigfile(username+".properties","status",status.FINISHED.toString());
				}
			
			
		}
		catch (IOException io) {
			io.printStackTrace();
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
		
		model.addAttribute("msg","Configuration successfully updated!");
		return layout;
	}
	//run service method
	@RequestMapping(value = "/run/{username}/{password}/", method = RequestMethod.GET)
	public String runService(@PathVariable("username") String username,@PathVariable("password") String pass,ModelMap model){
		
		model.addAttribute("tag","Running service procedure");
		File myfile = new File(username+".properties");
		if(!checkIfFileExists(myfile)){
			model.addAttribute("msg","Error: Run Failed, configuration for the user ="+" "+username+" "+"not found!");
			return layout;
		}
		else{
			Properties prop = loadProperties(username+".properties");
			if(prop == null){
		 		model.addAttribute("msg","Error: Status is not available: can not read properties file!");
		 		return layout;
		 	}
		 	if(!authenticatePassword(prop,pass)){
				model.addAttribute("msg","Error: Get status failed, password or username is inccorrect");
				return layout;
			}
		   
		    String stat = prop.getProperty("status");
		    if(stat.equals(status.FINISHED.toString()) || stat.equals(status.ONHOLD.toString()))
		    {
		    	updateConfigfile(username+".properties","status",status.RUNNING.toString());
		    	prop =  loadProperties(username+".properties");
		    	model.addAttribute("msg","begin reading mails");
		    	
		    	//begin to receiving mails from inbox
		    		Properties protocol = new Properties();
		    		protocol.setProperty("mail.store.protocol", "imaps"); //define which protocol to connect with
		    		try{
		    			Session session = Session.getInstance(protocol, null);
		                Store store = session.getStore();
		                String host = prop.getProperty("host");
		                String email = prop.getProperty("username");
		                String password = prop.getProperty("password");
		                store.connect(host, email, password);
		                Folder inbox = store.getFolder("INBOX");
		                inbox.open(Folder.READ_ONLY);
		                int messageCount = inbox.getMessageCount();
		                Message[] messages = inbox.getMessages();
		                
		                //creating output data file which holding our messages and accessible only with the correct password.
		                File data = new File(username+".txt");
		                BufferedWriter writer = new BufferedWriter(new FileWriter(data,false));
		                for(int i=0;i<messageCount && prop.getProperty("status").equals(status.RUNNING.toString()) ;i++ ){
		       		        writer.write(messages[i].getFrom()[0].toString());
		        		    writer.newLine();
		        		    writer.write(messages[i].getSubject().toString());
		        		    writer.newLine();
		        		    writer.write(messages[i].getContent().toString());
		        		    writer.newLine();
		                	prop =  loadProperties(username+".properties");
		                }
		                //update service status to finished
		                updateConfigfile(username+".properties","status",status.FINISHED.toString());
		                writer.close();
		                inbox.close(true);
		                store.close();
		                model.addAttribute("msg",messageCount+" "+"Messages succesfully saved! ");
		    			
		    		}catch (Exception e){
		    			model.addAttribute("msg","Exception:"+e.getMessage());
		    			updateConfigfile(username+".properties","status",status.ERROR.toString());
		    		}
		    	
		    }else{
		    	model.addAttribute("msg","Service status is:"+prop.getProperty("status"));
		    	
		    }

		}
		return layout;
	}
	//get status - function that get user name and password and return the service status for the predefine user
		@RequestMapping(value = "/getstatus/{username}/{password}/", method = RequestMethod.GET)
		public String getServiceSatus(@PathVariable("username") String username,@PathVariable("password") String pass,ModelMap model) {
				model.addAttribute("tag","Get Status");
			 	Properties prop = loadProperties(username+".properties");
			 	if(prop == null){
			 		model.addAttribute("msg","Error: Status is not available: can not read properties file!");
			 		return layout;
			 	}
			 	if(!authenticatePassword(prop,pass)){
					model.addAttribute("msg","Error: Get status failed, password or username is inccorrect");
					return layout;
				}
			    String status = prop.getProperty("status");
			    model.addAttribute("msg", "Service status is:"+" "+status);
			    
				return layout;
		}
		
		/*
		 * Stop Service function, get user name and password and stop the service for the predefined user.
		 */
		@RequestMapping(value = "/stop/{username}/{password}/", method = RequestMethod.GET)
		public String stopService( @PathVariable("username") String username,@PathVariable("password") String pass,ModelMap model) {
			model.addAttribute("tag","Stop Service");
			String message = "";
			Properties prop = loadProperties(username+".properties");
			if(prop == null){
		 		model.addAttribute("msg","Error: Can not stop service, properties file does not exist or username is inccorrect!");
		 		return layout;
		 	}
			if(!authenticatePassword(prop,pass)){
				model.addAttribute("msg","Error: Can not stop service, password or username is inccorrect");
				return layout;
			}
		    String stat = prop.getProperty("status");
		    if(stat.equals(status.RUNNING.toString()))
		    {
		    	updateConfigfile(username+".properties","status",status.ONHOLD.toString());
		    	message = "Service was stoped";
		    }else{
		    	message = "service is not running status is = "+ " "+prop.getProperty("status");
		    }
		    
		    model.addAttribute("msg",message);
			return layout;
		}
		
		/*
		 * getContent- provide the user the ability to get all is messages that was received in the runService
		 * function and display it on the web browser.
		 */
		@RequestMapping(value = "/content/{username}/{password}/", method = RequestMethod.GET)
		public ModelAndView getContent(@PathVariable("username") String username,@PathVariable("password") String pass,ModelMap model){
			model.addAttribute("tag","Get Content");
			ArrayList<Mail> messages = null;
			Properties prop = loadProperties(username+".properties");
			if(prop == null){
		 		model.addAttribute("msg","Error: Can not get content, properties file does not exist or username is inccorrect!");
		 		return new ModelAndView(layout,"model",model);
		 	}
			if(!authenticatePassword(prop,pass)){
				model.addAttribute("msg","Error: Can not get content, password or username is inccorrect");
				return new ModelAndView(layout,"model",model);
			}
			messages = readData(username+".txt",model);
			if(messages == null){
				model.addAttribute("msg","Error: Can not get content, messages object is not available");
			}else{
				ModelAndView mymodel = new ModelAndView(contentLayout);
				mymodel.addObject("messages",messages);
				return mymodel;
			}
			return new ModelAndView(layout,"model",model);
			
		}
		//function that check an existence of configuration file or data file
		public boolean checkIfFileExists(File filename){
			if(!filename.exists()){
				return false;
			}
			return true;
		}
		
		//function that updating properties configuration file
		public void updateConfigfile(String filename ,String key,String val){
			FileInputStream in = null;
			Properties props = new Properties();
			try {
				in = new FileInputStream(filename);
				props.load(in);
				in.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			catch (IOException e1) {
				e1.printStackTrace();
			}
			FileOutputStream out = null;
			try {
				out = new FileOutputStream(filename);
				props.setProperty(key, val);
				props.store(out, null);
				out.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		//function that load configuration properties file
		public Properties loadProperties(String filename){
			Properties prop = new Properties();
		    InputStream input = null;
		    try {
				input = new FileInputStream(filename);
				prop.load(input);	
				
			} catch (IOException ex) {
				ex.printStackTrace();
			} finally {
				if (input != null) {
					try {
						input.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				else{
					return null;
				}
			}
		    return prop;
			
		}
		//function that check if the password that provided fit to the user configuration file
		public boolean authenticatePassword(Properties prop,String pass){
			if(prop.getProperty("password") == null)
			{
				return true;
			}
			if(!(prop.getProperty("password").equals(pass))){
				return false;
			}
			return true;
		}
		//function that read all messages from data txt file which holding our incoming mails
		public ArrayList<Mail> readData(String filename,ModelMap model)
		{
			String line;
			ArrayList<Mail> mails = new ArrayList<Mail>();
			File file = new File(filename); 
			try (
			    InputStream fis = new FileInputStream(file);
			    InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
			    BufferedReader br = new BufferedReader(isr);
			) {
			    while ((line = br.readLine()) != null) {
			    	String from = line;
			    	String subject = br.readLine();
			    	String message = br.readLine();
			    	Mail mail = new  Mail(from,subject,message);
			    	mails.add(mail);
			        
			    }
			} catch (IOException e) {
				model.addAttribute("msg","Error: Can not get content, Exception:"+e.getMessage());
				return null;
			}
		   
		    return mails;
		}
	
	}

