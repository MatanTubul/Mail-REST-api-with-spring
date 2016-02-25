<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Content</title>
</head>
<body>

<table border="1px" bordercolor="black" width=100% align="center">
                <tr>
                    <td bgcolor ="#FF0000"> From</td>
                    <td bgcolor ="#FF0000"> Subject</td>
                    <td bgcolor ="#FF0000"> Message</td>
                </tr>
      <c:if test="${not empty messages}">
		
		
		<c:forEach var="listValue" items="${messages}">
			<tr>
				<td>${listValue.from}</td>
				<td>${listValue.subject}</td>
				<td>${listValue.message}</td>
			</tr>
		</c:forEach>
	</c:if>
	<c:otherwise>
		<h1 align="center" color="red">
			Inbox is Empty!
		</h1>
	</c:otherwise>               
 </table>
</body>
</html>