package edu.ualberta.med.biobank.server.logging;

import java.util.Date;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;

import edu.ualberta.med.biobank.model.Log;
import edu.ualberta.med.biobank.server.BiobankThreadVariable;
import edu.ualberta.med.biobank.server.LocalInfo;

/**
 * A custom Apache Log4J Appender will be responsible for formatting and
 * inserting Log4J messages into the configurable RDBMS.
 * 
 * <br>
 * <br>
 * Features include: <br>
 * --Inserts Logs Messages into the database in near real time <br>
 * --Uses a configurable buffer to perform batch processing <br>
 * --Spawns threads to execute the batch inserts to maximise performance <br>
 * --Prepares all data for RDBMS by escaping quotes.
 * 
 * See use in log4j.xml
 * 
 * Copy from CLM JDBCAppender
 * 
 */
public class BiobankJDBCAppender extends AppenderSkeleton {

    public static final String MYSQL_DIALECT = "org.hibernate.dialect.MySQLDialect";

    private String application = null;
    private String dbUrl = null;
    private String dbDriverClass = null;
    private String dbUser = null;
    private String dbPwd = null;

    public BiobankJDBCAppender() {

    }

    public BiobankJDBCAppender(Layout layout) {
        super();
        setLayout(layout);
    }

    @Override
    public void append(LoggingEvent le) {
        LocalInfo userInfo = BiobankThreadVariable.get();
        if (null == userInfo) {
            userInfo = new LocalInfo();
        }

        // Determine Log Type
        String msg = "";
        if (le.getMessage() != null) {
            msg = le.getMessage().toString();
        }
        Log log = populateObjectStateLogMesage(msg);
        java.util.Date d = new java.util.Date();
        d.setTime(System.currentTimeMillis());
        log.setCreatedAt(new Date());
        log.setUsername(userInfo.getUsername());

        JDBCLogExecutor exe = new JDBCLogExecutor(log, getJDBCProperties());
        // execute the batch insert
        new Thread(exe).start();
    }

    /**
     * Method parses the string to populate the log object
     */
    private Log populateObjectStateLogMesage(String objectAttributeMessage) {

        Log log = new Log();

        StringTokenizer stringTokenizer = new StringTokenizer(
            objectAttributeMessage, "&");
        while (stringTokenizer.hasMoreElements()) {
            String messagetemp = (String) stringTokenizer.nextElement();
            if (messagetemp.indexOf("=") <= 0)
                continue;
            String attributeName = messagetemp.substring(0,
                messagetemp.indexOf("="));
            String value = messagetemp.substring(messagetemp.indexOf("=") + 1);
            for (LogProperty logProperties : LogProperty.values()) {
                boolean set = logProperties.setLogValueIfInString(log,
                    attributeName, value);
                if (set) {
                    break;
                }
            }
        }
        return log;
    }

    /**
     * This method returns the Hibernate Properties.
     * 
     * @return Properties
     */
    private Properties getJDBCProperties() {
        Properties props = new Properties();
        props.setProperty("hibernate.connection.driver_class",
            getDbDriverClass());
        props.setProperty("hibernate.connection.url", getDbUrl());
        props.setProperty("hibernate.connection.username", getDbUser());
        props.setProperty("hibernate.connection.password", getDbPwd());

        if (getDbUrl().indexOf(":mysql") > -1) {
            props.setProperty("hibernate.dialect", MYSQL_DIALECT);
        }
        return props;
    }

    @Override
    public boolean requiresLayout() {
        return true;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String value) {
        application = value;
    }

    /**
     * @return Returns the dbDriverClass.
     */
    public String getDbDriverClass() {
        return dbDriverClass;
    }

    /**
     * @param dbDriverClass The dbDriverClass to set.
     */
    public void setDbDriverClass(String dbDriverClass) {
        this.dbDriverClass = dbDriverClass;
    }

    /**
     * @return Returns the dbPwd.
     */
    public String getDbPwd() {
        return dbPwd;
    }

    /**
     * @param dbPwd The dbPwd to set.
     */
    public void setDbPwd(String dbPwd) {
        this.dbPwd = dbPwd;
    }

    /**
     * @return Returns the dbUrl.
     */
    public String getDbUrl() {
        return dbUrl;
    }

    /**
     * @param dbUrl The dbUrl to set.
     */
    public void setDbUrl(String dbUrl) {
        this.dbUrl = dbUrl;
    }

    /**
     * @return Returns the dbUser.
     */
    public String getDbUser() {
        return dbUser;
    }

    /**
     * @param dbUser The dbUser to set.
     */
    public void setDbUser(String dbUser) {
        this.dbUser = dbUser;
    }

    @Override
    public void close() {

    }

}