package com.javacodegeeks.snippets.enterprise;

public class Mail {
	
	private String from;
	private String subject;
	private String bodyMessage;
	
	public Mail(String from,String sub,String msg){
		this.from = from;
		this.subject = sub;
		this.bodyMessage = msg;
	}

	public String getFrom(){
		return this.from;
	}
	
	public String getSubject(){
		return this.subject;
	}
	
	public String getMessage(){
		return this.bodyMessage;
	}
	
	
}
