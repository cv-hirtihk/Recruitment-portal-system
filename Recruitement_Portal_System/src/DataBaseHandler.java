/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.Serializable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JOptionPane;


/**
 *
 * @author HIRTIH KUMAR C V
 */

public class DataBaseHandler implements Serializable {
    private Connection connection , connectionStat;
    private PreparedStatement preparedStatement;
    private ResultSet resultset , resultsetStat;
    private ResultSetMetaData MetaData;
    private Statement statement;
    private String sql , dbUsername , dbPassword , dbUserRole , dbUser_id, data[][] , JOptionPane_Message;
    private String dbRequest_id , dbRequest_status , dbPosition_id , dbPosition_status , dbEditDelete_status , dbRecruitement_status;
    private boolean tracker = false;
    private boolean login = false;

    
    protected DataBaseHandler(){
        try{
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        }catch(ClassNotFoundException | InstantiationException | IllegalAccessException error){
            System.out.println("Class Not Found : " + error);
        }
    }

    
    private Connection ConnectToDataBase() throws SQLException{
        connection = (Connection)DriverManager.getConnection("jdbc:mysql://localhost:3306/portalsystem?zeroDateTimeBehavior=convertToNull","root","");
        return connection;
    }
    
    /**
     *
     * @param ArrayOfData
     * @return
     */
    protected String InsertNewAccountData(Object ... ArrayOfData){
        int i = 0 , status ;
        String user_id;
        try{
            connectionStat = ConnectToDataBase();
            if(ArrayOfData[5].equals("Add Resume")){
                sql = "INSERT INTO recruitement_master_table(position_id,job_title,file,user_id,recruitement_status) VALUES(?,?,?,?,?)";
                preparedStatement = connectionStat.prepareStatement(sql);
                for (; i < ArrayOfData.length - 1; i++){
                    System.out.println("index : " + i + " i : " + ArrayOfData[i]);
                    if(i == 0){// Parsing position_count Actual type -> (int), Object to int
                        preparedStatement.setInt(i + 1 , (int) ArrayOfData[i]);
                    }
                    else if(i == 2){
                        preparedStatement.setBytes(i + 1 , (byte[]) ArrayOfData[i]);
                    }
                    else{
                        preparedStatement.setString(i + 1 , String.valueOf(ArrayOfData[i]));
                    }
                }
                status  = preparedStatement.executeUpdate();
                System.out.println("status : " + status);
                if(status > 0){
                    user_id = String.valueOf((ArrayOfData[0]));
                    sql = "SELECT position_id , recruitement_status FROM recruitement_master_table WHERE position_id =" + user_id + "";
                    resultsetStat = getQueryResultSet(sql);
                    while(resultset.next()){
                        dbPosition_id = String.valueOf(resultsetStat.getInt("position_id"));
                        dbRecruitement_status = resultsetStat.getString("recruitement_status");
                    }
                    System.out.println("dbRequest_id : " + dbPosition_id + " dbRecruitement_status : " + dbRecruitement_status);

                    return dbPosition_id + "-" + dbRecruitement_status;
                }
            }
            else if(ArrayOfData[9].equals("Request created") || ArrayOfData[9].equals("Position created")){
                sql = ArrayOfData[9].equals("Request created") ? "INSERT INTO position_request_table(department,job_title,experience,qualifications,skills,job_description,position_count,user_id,request_status)" + "VALUES(?,?,?,?,?,?,?,?,?)" :
                "INSERT INTO position_master_table(department,job_title,experience,qualifications,skills,job_description,position_count,user_id,position_status)" + "VALUES(?,?,?,?,?,?,?,?,?)";
                preparedStatement = connectionStat.prepareStatement(sql);
                if(ArrayOfData[9].equals("Request created") || ArrayOfData[9].equals("Position created")){
                    for (; i < ArrayOfData.length - 1; i++){
                        System.out.println("index : " + i + " i : " + ArrayOfData[i]);
                        if(i == 6){// Parsing position_count Actual type -> (int), Object to int
                            preparedStatement.setInt(i + 1 , (int) ArrayOfData[i]);
                        }
                        else{
                            preparedStatement.setString(i + 1 , String.valueOf(ArrayOfData[i]));
                        }
                    }
                }
                status  = preparedStatement.executeUpdate();// Inserts new account details as mentioned in the sql code

                if(status > 0){ 
                    if(ArrayOfData[9].equals("Request created")){// Returns request_id and request_status from position_request_table
                        user_id = (String) ArrayOfData[7];// Index where the user_id is found
                        sql = "SELECT request_id , request_status FROM position_request_table WHERE user_id ='" + user_id + "'";
                        resultsetStat = getQueryResultSet(sql);
                        while(resultset.next()){
                            dbRequest_id = String.valueOf(resultsetStat.getInt("request_id"));
                            dbRequest_status = resultsetStat.getString("request_status");
                        }   
                        System.out.println("dbRequest_id : " + dbRequest_id + " dbRequest_status : " + dbRequest_status);
                        return dbRequest_id + "-" + dbRequest_status;
                    }
                    else if(ArrayOfData[9].equals("Position created")){// Returns position_id and position_status from position_master_table
                        user_id = (String) ArrayOfData[7];// Index where the user_id is found
                        sql = "SELECT position_id , position_status FROM position_master_table WHERE user_id ='" + user_id + "'";
                        resultsetStat = getQueryResultSet(sql);
                        while(resultset.next()){
                            dbPosition_id = String.valueOf(resultsetStat.getInt("position_id"));
                            dbPosition_status = resultsetStat.getString("position_status");
                        }   
                        System.out.println("dbRequest_id : " + dbPosition_id + " dbRequest_status : " + dbPosition_status);
                        return dbPosition_id + "-" + dbPosition_status;
                    } 
                }
            }
            else{
                return "Not able to insert data";
            }
        }catch(SQLException error){
            System.out.println("SQLException Line 137 : " + error);
            return "SQLException has occured while inserting into database";
        }catch(Exception ex){
            System.out.println("ex Line 142 : " + ex);
        }
        return null;
    }

    protected String CheckForLoginPassword(String userID , String passWord){
        try{     
            sql = "SELECT mailid , password , role , user_id FROM user_details WHERE mailid ='" + userID + "' AND password = '" + passWord + "'";
            resultsetStat = getQueryResultSet(sql);

            while(resultset.next()){
                dbUsername = resultsetStat.getString("mailid");
                dbPassword = resultsetStat.getString("password");
                if(dbUsername.equals(userID) && dbPassword.equals(passWord)){
                    dbUser_id = resultsetStat.getString("user_id");
                    //System.out.println("dbUserRole : " + dbUserRole + " dbUser_id : " + dbUser_id);
                    login = true;
                    break;
                }
            }
            if(login == true){
                return dbUser_id;
            }else{
                return "false";
            }
        }catch(SQLException error){
            return "SQLException occured";
        }
    }
    
    private int rowCount(String sql_row){
        int rowCount = 0;
        try {
            resultset = statement.executeQuery(sql_row);
            resultset.next();
            rowCount = resultset.getInt(1);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "SQLException occured");
        }
        return rowCount;
    }
    
    private int columnCount(String sql_column){
        int columnCount = 0;
        try {
            resultset = statement.executeQuery(sql_column);
            MetaData = resultset.getMetaData();
            columnCount = MetaData.getColumnCount();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "SQLException occured");
        }
        return columnCount;
    }
    
    
    protected String[][] TableData(String SelectSQL , String ... Header) throws SQLException{
        int rowCount = 0 , columnCount= 0;
        int row = 0 , column = 0;
        String sql_row = "" , sql_column = "";
        try{
            connectionStat = ConnectToDataBase();
            statement = connectionStat.createStatement();
            if(SelectSQL.equals("requestView") || SelectSQL.equals("Management Approval")){
                if(SelectSQL.equals("requestView")){
                    sql_row  = "select count(*) from position_request_table";
                    sql_column = "select * from position_request_table";
                }else{
                    sql_row  = "select count(*) from position_request_table";
                    sql_column = "select * from position_request_table WHERE request_status = 'Pending Approval'";
                }
                rowCount = rowCount(sql_row);

                columnCount = columnCount(sql_column);
                data = new String[rowCount][columnCount];
                    try{
                        while (resultset.next()){
                            data[row][column] = resultset.getString("request_id");
                            data[row][++column] = resultset.getString("department");
                            data[row][++column] = resultset.getString("job_title");
                            data[row][++column] = resultset.getString("experience");
                            data[row][++column] = resultset.getString("qualifications");
                            data[row][++column] = resultset.getString("skills");
                            data[row][++column] = resultset.getString("job_description");
                            data[row][++column] = resultset.getString("position_count");
                            data[row][++column] = resultset.getString("user_id");
                            data[row][++column] = resultset.getString("request_status");
                            data[row][++column] = resultset.getString("time_stamp");
                            if(row < rowCount){
                                row += 1;
                                column = 0;
                            }if(row == rowCount && column == columnCount){
                                break;
                            }     
                        }
                    }catch(ArrayIndexOutOfBoundsException error){
                        System.out.println("INDEX ERROR OCCOURED : " + error);
                    }
                }
            else if(SelectSQL.equals("Recruitement View")){
                sql_row = "select count(*) from recruitement_master_table";
                sql_column = "SELECT * from recruitement_master_table";
                rowCount = rowCount(sql_row);
                columnCount = columnCount(sql_column);
                data = new String[rowCount][columnCount];
                
                try{
                    while(resultset.next()){
                        data[row][column] = resultset.getString("sr_no");
                        data[row][++column] = resultset.getString("position_id");
                        data[row][++column] = resultset.getString("job_title");
                        data[row][++column] = String.valueOf(resultset.getBytes("file"));
                        data[row][++column] = resultset.getString("user_id");
                        data[row][++column] = resultset.getString("recruitement_status");
                        data[row][++column] = resultset.getString("time_stamp");
                        if(row < rowCount){
                            row += 1;
                            column = 0;
                        }if(row == rowCount && column == columnCount){
                            break;
                        }  
                    }
                }catch(ArrayIndexOutOfBoundsException error){
                    System.out.println("INDEX ERROR OCCOURED : " + error);
                }
                
            }
            else {
                if(SelectSQL.equals("Delete") || SelectSQL.equals("Edit")){
                    sql_row = "select count(*) from position_master_table";
                    sql_column = "SELECT * from position_master_table";
                    
                }else if(SelectSQL.equals("positionView")){
                    sql_row = "SELECT count(*) from position_master_table";
                    sql_column = "select * from position_master_table WHERE NOT position_status = 'Deleted'";
                    
                }
                    rowCount = rowCount(sql_row);
                    columnCount = columnCount(sql_column);
                    data = new String[rowCount][columnCount];
                try{
                    while (resultset.next()){
                        data[row][column] = resultset.getString("position_id");
                        data[row][++column] = resultset.getString("department");
                        data[row][++column] = resultset.getString("job_title");
                        data[row][++column] = resultset.getString("experience");
                        data[row][++column] = resultset.getString("qualifications");
                        data[row][++column] = resultset.getString("skills");
                        data[row][++column] = resultset.getString("job_description");
                        data[row][++column] = resultset.getString("position_count");
                        data[row][++column] = resultset.getString("user_id");
                        data[row][++column] = resultset.getString("position_status");
                        data[row][++column] = resultset.getString("time_stamp");

                        if(row < rowCount){
                            row += 1;
                            column = 0;
                        }if(row == rowCount && column == columnCount){
                            break;
                        }
                    }
                    System.out.println("DATA UPLOADED TO TABLE");
                }catch(ArrayIndexOutOfBoundsException error){
                    System.out.println("INDEX ERROR OCCOURED : " + error);
                }
            }
        }catch(SQLException error){
            System.out.println("SQLException occoured : " + error);
        }
        return data;
    }

    protected void UpdateDeleteEdit(String sql){
        try {
            connectionStat = ConnectToDataBase();
            statement = connectionStat.createStatement();
            statement.executeUpdate(sql);
        } catch (SQLException ex) {
            System.out.println("Sqlexception in UpdateDeleteEdit : " + ex);
            JOptionPane.showMessageDialog(null, "SQLException has occured");
        }
    }
    
    protected ResultSet getQueryResultSet(String sql){
        
        try {
            connectionStat = ConnectToDataBase();
            statement = connectionStat.createStatement();
            statement.executeQuery(sql);
            resultset = statement.getResultSet();
        } catch (SQLException ex) {
            System.out.println("Sqlexception in getQueryResultSet : " + ex);
            JOptionPane.showMessageDialog(null, "SQLException has occured");
        }
        return resultset;
    }
    protected boolean dbSetDelete(int DeleteSelectedRow) throws SQLException{

        System.out.println("DeleteSelectedRow  : " + DeleteSelectedRow);
        sql = "UPDATE position_master_table SET position_status = 'Deleted' WHERE position_id = "+ DeleteSelectedRow +";";
        
        UpdateDeleteEdit(sql);
        
        sql = "SELECT position_status FROM position_master_table WHERE position_id =" + DeleteSelectedRow + ";";
        resultsetStat = getQueryResultSet(sql);

        while(resultset.next()){
            dbEditDelete_status = resultset.getString("position_status");
            System.out.println("dbEditDelete_status : " + dbEditDelete_status);
        }
        if(dbEditDelete_status.equals("Deleted")){
            JOptionPane_Message = "position_status set to 'Deleted' for the position_id : " + DeleteSelectedRow + "";
            tracker = true;
            JOptionPane.showMessageDialog(null, JOptionPane_Message);
        }
        return tracker;

    }
}
