<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Welcome to mail API</title>
</head>
<body>
<h1 align="center"> Welcome to Rest Mail API </h1>
<h2 align="center">The following  manual describe  the API and cover all available command</h2>
<table border="1px" bordercolor="black" width=100% align="center">
 	<tr>
         <td bgcolor ="#FF0000"> Command</td>
         <td bgcolor ="#FF0000"> Parameters</td>
         <td bgcolor ="#FF0000"> Description</td>
    </tr>
	<tr>
		<td>setup</td>
		<td>/username/password/host/</td>
		<td>This command configure the host server which we should connect in order to connect to the mail box by predefine username and password</td>
	</tr>
	<tr>
		<td>run</td>
		<td>/username/password/</td>
		<td>This command execute the service and connect to the mail server by imap protocol, in order to run the service setup command should be executed</td>
	</tr>
	<tr>
		<td>getstatus</td>
		<td>/username/password/</td>
		<td>This command return the current status of the service</td>
	</tr>
	<tr>
		<td>stop</td>
		<td>/username/password/</td>
		<td>This command stop the service and change is status to on hold</td>
	</tr>
	<tr>
		<td>content</td>
		<td>/username/password/</td>
		<td>This command preview all the mail messages that was retrieved after the service was executed</td>
	</tr>

</table>
</body>
</html>