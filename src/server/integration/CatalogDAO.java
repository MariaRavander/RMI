package server.integration;

import common.Credentials;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import server.model.File;
import common.FileDTO;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This data access object (DAO) encapsulates all database calls in the bank application. No code
 * outside this class shall have any knowledge about the database.
 */
public class CatalogDAO {
    private static final String TABLE_NAME = "FILE";
    private static final String FILENAME_COLUMN_NAME = "FILENAME";
    private static final String OWNER_COLUMN_NAME = "USERNAME";
    private static final String FILESIZE_COLUMN_NAME = "FILESIZE";
    private static final String FILEPERMISSION_COLUMN_NAME = "FILEPERMISSION";
    private PreparedStatement createFileStmt;
    private PreparedStatement findFileStmt;
    private PreparedStatement findAllFilesStmt;
    private PreparedStatement deleteFileStmt;
    private PreparedStatement updateFileStmt;
    private PreparedStatement getPasswordStmt;
    private PreparedStatement userExistsStmt;
    private PreparedStatement registerStmt;
            
            
    /**
     * Constructs a new DAO object connected to the specified database.
     *
     * @param dbms       Database management system vendor. Currently supported type is "derby"
     *                   
     * @param datasource Database name.
     * @throws server.integration.CatalogDBException
     */
    public CatalogDAO(String dbms, String datasource) throws CatalogDBException {
        try {
            Connection connection = createDatasource(dbms, datasource);
            if(connection != null) 
            prepareStatements(connection);
        } catch (ClassNotFoundException | SQLException exception) {
            throw new CatalogDBException("Could not connect to datasource.", exception);
        }
    }
    
    private Connection createDatasource(String dbms, String datasource) throws
            ClassNotFoundException, SQLException, CatalogDBException {
        Connection connection = connectToCatalogDB(dbms, datasource);
        if (!fileTableExists(connection)) {
            Statement statement = connection.createStatement();
            statement.executeUpdate("CREATE TABLE " + TABLE_NAME
                                  + " (" + FILENAME_COLUMN_NAME + " VARCHAR(32) PRIMARY KEY, "
                                    + FILESIZE_COLUMN_NAME + " INT," + OWNER_COLUMN_NAME + " VARCHAR(32)," + FILEPERMISSION_COLUMN_NAME + " VARCHAR(32))");  
        }
        if(!accountTableExists(connection)){
            Statement statement = connection.createStatement();
            statement.executeUpdate("CREATE TABLE ACCOUNT (NAME VARCHAR(32) PRIMARY KEY, PASSWORD VARCHAR(32))");
        }

        return connection;
    }
    
    public String getPassword(String username) throws CatalogDBException{
        ResultSet result = null;
        try{
            getPasswordStmt.setString(1, username);
            result = getPasswordStmt.executeQuery();
            if(result.next()){
                return result.getString("PASSWORD");
            }
        } catch (SQLException ex) {
            Logger.getLogger(CatalogDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * 
     * @param fileName
     * @return
     * @throws CatalogDBException 
     */
    public File findFileByName(String fileName) throws CatalogDBException {
        String failureMsg = "Could not search for specified account.";
        ResultSet result = null;
        try {
            findFileStmt.setString(1, fileName);
            result = findFileStmt.executeQuery();
            if (result.next()) {
                return new File(fileName, result.getInt(FILESIZE_COLUMN_NAME), 
                        result.getString(OWNER_COLUMN_NAME), result.getString(FILEPERMISSION_COLUMN_NAME));
            }
        } catch (SQLException sqle) {
            throw new CatalogDBException(failureMsg, sqle);
        } finally {
            try {
                result.close();
            } catch (Exception e) {
                throw new CatalogDBException(failureMsg, e);
            }
        }
        return null;
    }

    /**
     * Retrieves all existing files.
     *
     * @return A list with all existing files. The list is empty if there are no files.
     * @throws BankDBException If failed to search for account.
     */
    public List<File> findAllFiles() throws CatalogDBException {
        String failureMsg = "Could not list accounts.";
        List<File> files = new ArrayList<>();
        try (ResultSet result = findAllFilesStmt.executeQuery()) {
            while (result.next()) {
                files.add(new File(result.getString(FILENAME_COLUMN_NAME), result.getInt(FILESIZE_COLUMN_NAME), 
                        result.getString(OWNER_COLUMN_NAME), result.getString(FILEPERMISSION_COLUMN_NAME)));
            }
        } catch (SQLException sqle) {
            throw new CatalogDBException(failureMsg, sqle);
        }

        return files;
    }

    /**
     * Creates a new file.
     *
     * @param file The file to create.
     * @throws BankDBException If failed to create the specified file.
     */
    public void createFile(File file) throws CatalogDBException {
        String failureMsg = "Could not create the file: " + file;
        try {
            createFileStmt.setString(1, file.getFileName());
            createFileStmt.setInt(2, file.getFileSize());
            createFileStmt.setString(3, file.getFileOwner());
            createFileStmt.setString(4, file.getFilePermission());
            int rows = createFileStmt.executeUpdate();
            if (rows != 1) {
                throw new CatalogDBException(failureMsg);
            }
        } catch (SQLException sqle) {
            throw new CatalogDBException(failureMsg, sqle);
        }
    }

    /**
     * Deletes the specified file.
     *
     * @param file The file to delete.
     * @throws server.integration.CatalogDBException
     */
    public void deleteFile(FileDTO file) throws CatalogDBException {
        try {
            deleteFileStmt.setString(1, file.getFileName());
            deleteFileStmt.executeUpdate();
        } catch (SQLException sqle) {
            throw new CatalogDBException("Could not delete the file: " + file, sqle);
        }
    }
    
    /**
     * 
     * @param file
     * @throws CatalogDBException 
     */
    public void updateFile(String filename, int newSize) throws CatalogDBException {
        try {
            updateFileStmt.setInt(1, newSize);
            updateFileStmt.setString(2, filename);
            updateFileStmt.executeUpdate();
        } catch (SQLException sqle) {
            throw new CatalogDBException("Could not update the file: " + filename, sqle);
        }
    }

    private boolean fileTableExists(Connection connection) throws SQLException {
        int tableNameColumn = 3;
        DatabaseMetaData dbm = connection.getMetaData();
        try (ResultSet rs = dbm.getTables(null, null, null, null)) {
            for (; rs.next();) {
                if (rs.getString(tableNameColumn).equals(TABLE_NAME)) {
                    return true;
                }
            }
            return false;
        }
    }
    
    private boolean accountTableExists(Connection connection) throws SQLException {
        int tableNameColumn = 3;
        DatabaseMetaData dbm = connection.getMetaData();
        try (ResultSet rs = dbm.getTables(null, null, null, null)) {
            for (; rs.next();) {
                if (rs.getString(tableNameColumn).equals("ACCOUNT")) {
                    return true;
                }
            }
            return false;
        }
    }

    private Connection connectToCatalogDB(String dbms, String datasource)
            throws ClassNotFoundException, SQLException, CatalogDBException {
        if (dbms.equalsIgnoreCase("derby")) {
            Class.forName("org.apache.derby.jdbc.ClientXADataSource");
            return DriverManager.getConnection(
                    "jdbc:derby://localhost:1527/" + datasource, "Maria", "Maria");
        } else {
            throw new CatalogDBException("Unable to create datasource, unknown dbms.");
        }
    }
    
    public boolean userExists (String username) throws CatalogDBException {
        String failureMsg = "Could not determine whether user exists or not.";
        ResultSet result;
        try {
            userExistsStmt.setString(1, username);
            result = userExistsStmt.executeQuery();
             return result.next();
        } catch (SQLException sqle) {
            throw new CatalogDBException(failureMsg, sqle);
        }
    }
    
    public void register(Credentials credentials) throws CatalogDBException {
         String failureMsg = "Could not register with username: " + credentials.getUsername();
        try {
            registerStmt.setString(1, credentials.getUsername());
            registerStmt.setString(2, credentials.getPassword());
            int rows = registerStmt.executeUpdate();
            if (rows != 1) {
                throw new CatalogDBException(failureMsg);
            }
        } catch (SQLException sqle) {
            throw new CatalogDBException(failureMsg, sqle);
        }
    }
    

    private void prepareStatements(Connection connection) throws SQLException {
        createFileStmt = connection.prepareStatement("INSERT INTO "
                                                        + TABLE_NAME + " VALUES (?, ?, ?, ?)");
        findFileStmt = connection.prepareStatement("SELECT * from "
                                                      + TABLE_NAME + " WHERE FILENAME = ?");
        findAllFilesStmt = connection.prepareStatement("SELECT * from "
                                                          + TABLE_NAME);
        deleteFileStmt = connection.prepareStatement("DELETE FROM "
                                                        + TABLE_NAME
                                                        + " WHERE FILENAME = ?"); 
        updateFileStmt = connection.prepareStatement("UPDATE " + TABLE_NAME + " SET FILESIZE = ? WHERE FILENAME = ? ");
        getPasswordStmt = connection.prepareStatement("SELECT PASSWORD from ACCOUNT WHERE NAME = ?");
        userExistsStmt = connection.prepareStatement("SELECT * FROM ACCOUNT WHERE NAME = ?");
        registerStmt = connection.prepareStatement("INSERT INTO ACCOUNT VALUES(?, ?)");
    }

}
