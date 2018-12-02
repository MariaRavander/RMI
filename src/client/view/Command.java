package client.view;

/**
 * Defines all commands that can be performed by a user of the chat application.
 */
public enum Command {
    
    REGISTER,
    
    /**
     * Establish a connection to the server. The first parameter is IP address (or host name),
     * the second is username, the third password.
     */
    LOGIN,
    /**
     * Leave the catalog application.
     */
    LOGOUT,
    
    LIST,
    
    OPEN,
    
    UPLOAD,
    
    DELETE,
    
    UPDATE,
    
    NO_COMMAND
}
