package com.servlet;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class JavaServlet extends HttpServlet {
	private static final int NOT_EXISTING_FUNCTION_CODE_ERROR = 984;
	private static final int NOT_EXISTING_TABLE_CODE_ERROR = 942;

	private static final String nextLineChar = "\r\n";
	private static final String CLASS_NAME = "oracle.jdbc.driver.OracleDriver";
	private static final String CONNECTION_ARGUMENT = "jdbc:oracle:thin:testuser/testpass@localhost";
	private static final String CREATE_FUNCTION_FILE_NAME = "CREATE_FUNCTION.txt";

	private static final String REMOVE_RECORD = "DELETE testTable WHERE id = %s";
	private static final String ADD_RECORD = "INSERT INTO testTable VALUES (getMinID, '%s')";
	private static final String PRINT_RECORDS = "SELECT * FROM testTable ORDER BY id";
	private static final String CREATE_TABLE = "CREATE TABLE testTable(id INTEGER PRIMARY KEY, name VARCHAR2(50))";

	private static final String PRINT = "print";
	private static final String ADD = "add";
	private static final String DELETE = "delete";
	private static final String ARG = "arg";
	private static final String REQUEST = "request";

	public JavaServlet() throws IOException
	{
		try {
			Class.forName(CLASS_NAME);
		}
		catch (ClassNotFoundException e) {}
	}

	private String readCreateFunctionQuery(String fileName)
	{
		InputStream in = this.getClass().getResourceAsStream(fileName);
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		StringBuilder out = new StringBuilder();
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				out.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return out.toString();
	}

	private void removeArgument(String arg)
	{
		Connection con = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			con = DriverManager.getConnection(CONNECTION_ARGUMENT);
			stmt = con.createStatement();
			rs = stmt.executeQuery(String.format(REMOVE_RECORD, arg));
		} catch (SQLException e) {
			e.printStackTrace();

			if(e.getErrorCode() == NOT_EXISTING_TABLE_CODE_ERROR)
				createView(CREATE_TABLE);
		} finally {
			try {
				if(rs != null)
					rs.close();
				stmt.close();
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			catch (NullPointerException e) {
				e.printStackTrace();
			}
		}
	}


	private void createView(String src)
	{
		Connection con = null;
		Statement stmt = null;
		ResultSet rs = null;

		try {									
			con = DriverManager.getConnection(CONNECTION_ARGUMENT);
			stmt = con.createStatement();
			rs = stmt.executeQuery(src);
		}  catch (SQLException e2) {
			e2.printStackTrace();
		} finally {
			try {
				if(rs != null)
					rs.close();
				stmt.close();
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			catch (NullPointerException e) {
				e.printStackTrace();
			}
		}
	}


	private void addArgument(String arg)
	{
		Connection con = null;
		Statement stmt = null;
		ResultSet rs = null;

		try {
			con = DriverManager.getConnection(CONNECTION_ARGUMENT);
			stmt = con.createStatement();
			rs = stmt.executeQuery(String.format(ADD_RECORD, arg));
		}  catch (SQLException e) {
			e.printStackTrace();

			if(e.getErrorCode() == NOT_EXISTING_FUNCTION_CODE_ERROR)
				createView(readCreateFunctionQuery(CREATE_FUNCTION_FILE_NAME));

			if(e.getErrorCode() == NOT_EXISTING_TABLE_CODE_ERROR)
				createView(CREATE_TABLE);

			addArgument(arg);

		} finally {
			try {
				if(rs != null)
					rs.close();
				stmt.close();
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			catch (NullPointerException e) {
				e.printStackTrace();
			}
		}
	}

	private ArrayList<String> printList()
	{
		ArrayList<String> lines = new ArrayList<String>();
		boolean ifNeedToReprint = false;

		Connection con = null;
		Statement stmt = null;
		ResultSet rs = null;

		try {
			con = DriverManager.getConnection(CONNECTION_ARGUMENT);
			stmt = con.createStatement();
			rs = stmt.executeQuery(PRINT_RECORDS);

			lines.add("ID \t\t VALUE");
			lines.add("-- \t\t\t --");

			while(rs.next()) {
				String line = rs.getInt(1) + "\t\t\t" + rs.getString(2);
				lines.add(line);
			}
		} catch (SQLException e) {
			e.printStackTrace();

			if(e.getErrorCode() == NOT_EXISTING_TABLE_CODE_ERROR)
				createView(CREATE_TABLE);

			ifNeedToReprint = true;

		} finally {
			try {
				if(rs != null)
					rs.close();
				stmt.close();
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			catch (NullPointerException e) {
				e.printStackTrace();
			}
		}

		if(ifNeedToReprint)
			return printList();
		else
			return lines;
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {    	
		String requestStr = request.getParameter(REQUEST);

		if(requestStr != null)
		{
			if(requestStr.equals(PRINT))
				printList(); 
			else if(requestStr.equals(ADD))
			{
				requestStr = request.getParameter(ARG);
				addArgument(requestStr);
			}	
			else if(requestStr.equals(DELETE))
			{
				requestStr = request.getParameter(ARG);
				removeArgument(requestStr);
			}	
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		try {        	
			int length = request.getContentLength();
			byte[] input = new byte[length];
			ServletInputStream sin = request.getInputStream();
			int c, count = 0 ;
			while ((c = sin.read(input, count, input.length-count)) != -1) {
				count +=c;
			}
			sin.close();

			String recievedString = new String(input);
			response.setStatus(HttpServletResponse.SC_OK);
			OutputStreamWriter writer = new OutputStreamWriter(response.getOutputStream());


			if(recievedString.equals(PRINT))
			{
				ArrayList<String> linijki = printList();

				for(String linijka : linijki)
					writer.write(linijka + nextLineChar);
			}
			else 
			{
				try{					
					Integer doubledValue = Integer.parseInt(recievedString) * 2;
					writer.write(doubledValue.toString());
				} catch(NumberFormatException e){}
			}

			writer.flush();
			writer.close();

		} catch (IOException e) {

			try{
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().print(e.getMessage());
				response.getWriter().close();
			} catch (IOException ioe) {}
		}   
	}

}