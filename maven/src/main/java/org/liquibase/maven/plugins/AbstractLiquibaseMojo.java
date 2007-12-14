package org.liquibase.maven.plugins;

import java.io.*;
import java.lang.reflect.Field;
import java.net.*;
import java.sql.*;
import java.util.*;
import liquibase.*;
import liquibase.exception.JDBCException;
import liquibase.exception.LiquibaseException;
import liquibase.migrator.Migrator;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.*;
import org.apache.maven.project.MavenProject;

/**
 * A base class for providing Liquibase {@link liquibase.migrator.Migrator} functionality.
 * @author Peter Murray, Trace Financial Limited
 * @requiresDependencyResolution test
 */
public abstract class AbstractLiquibaseMojo extends AbstractMojo {

  public static final String LOG_SEPARATOR =
          "------------------------------------------------------------------------";

  /** Suffix for fields that are representing a default value for a another field. */
  private static final String DEFAULT_FIELD_SUFFIX = "Default";

  /**
   * Specifies the change log file to use for Liquibase.
   * @parameter expression="${liquibase.changeLogFile}"
   */
  protected String changeLogFile;

  /**
   * The fully qualified name of the driver class to use to connect to the database.
   * @parameter expression="${liquibase.driver}"
   */
  protected String driver;

  /**
   * The Database URL to connect to for executing Liquibase.
   * @parameter expression="${liquibase.url}"
   */
  protected String url;

  /**
   * The database username to use to connect to the specified database.
   * @parameter expression="${liquibase.username}"
   */
  protected String username;

  /**
   * The database password to use to connect to the specified database.
   * @parameter expression="${liquibase.password}"
   */
  protected String password;

  /**
   * Whether or not to perform a drop on the database before executing the change.
   * @parameter expression="${liquibase.dropFirst}" default-value="false"
   */
  protected boolean dropFirst;
  private boolean dropFirstDefault = false;

  /**
   * The Liquibase contexts to execute, which can be "," separated if multiple contexts
   * are required. If no context is specified then ALL contexts will be executed.
   * @parameter expression="${liquibase.contexts}" default-value=""
   */
  protected String contexts;
  private String contextsDefault = "";

  /**
   * Controls the prompting of users as to whether or not they really want to run the
   * changes on a database that is not local to the machine that the user is current
   * executing the plugin on.
   * @parameter expression="${liquibase.promptOnNonLocalDatabase}" default-value="true"
   */
  protected boolean promptOnNonLocalDatabase;
  private boolean promptOnNonLocalDatabaseDefault = true;

  /**
   * The Liquibase properties file used to configure the Liquibase
   * {@link liquibase.migrator.Migrator}.
   * @parameter expression="${liquibase.propertiesFile}"
   */
  protected String propertiesFile;

  /**
   * Flag allowing for the Liquibase properties file to override any settings provided in
   * the Maven plugin configuration. By default if a property is explicity specified it
   * is not overridden if it also appears in the properties file.
   * @parameter expression="${liquibase.propertyFileWillOverride}" default-value="false"
   */
  protected boolean propertyFileWillOverride;

  /**
   * The Maven project that plugin is running under.
   * @parameter expression="${project}"
   * @required
   * @readonly
   */
  protected MavenProject project;

  /**
   * Allows for the maven project artifact to be included in the class loader for
   * obtaining the Liquibase property and DatabaseChangeLog files.
   * @parameter expression="${liquibase.includeArtifact}" default-value="true"
   */
  protected boolean includeArtifact;
  private boolean includeArtifactDefault = true;

  /**
   * Controls the verbosity of the output from invoking the plugin.
   * @parameter expression="${liquibase.verbose}" default-value="false"
   * @description Controls the verbosity of the plugin when executing
   */
  protected boolean verbose;
  private boolean verboseDefault = false;

  public void execute() throws MojoExecutionException, MojoFailureException {
    getLog().info(LOG_SEPARATOR);

    String shouldRunProperty = System.getProperty(Migrator.SHOULD_RUN_SYSTEM_PROPERTY);
    if (shouldRunProperty != null && !Boolean.valueOf(shouldRunProperty).booleanValue()) {
      getLog().warn("Migrator did not run because '" + Migrator.SHOULD_RUN_SYSTEM_PROPERTY
                    + "' system property was set to false");
      return;
    }

    ClassLoader artifactClassLoader = null;
    try {
      artifactClassLoader = getArtifactClassloader();
    }
    catch (MalformedURLException e) {
      throw new MojoExecutionException("Failed to create artifact classloader", e);
    }

    FileOpener mFO = new MavenFileOpener(artifactClassLoader);
    FileOpener fsFO = new FileSystemFileOpener(project.getBasedir().getAbsolutePath());
    CompositeFileOpener cfo = new CompositeFileOpener(mFO, fsFO);

    // Load the properties file if there is one, but only for values that the user has not
    // already specified.
    if (propertiesFile != null) {
      getLog().info("Loading Liquibase settings from properties file, " + propertiesFile);
      try {
        InputStream is = cfo.getResourceAsStream(propertiesFile);
        if (is == null) {
          throw new MojoFailureException("Failed to resolve the properties file.");
        }
        parsePropertiesFile(is);
      }
      catch (IOException e) {
        throw new MojoExecutionException("Failed to resolve properties file", e);
      }
    }

    if (verbose) {
      getLog().info("Settings----------------------------");
      printSettings();
      getLog().info(LOG_SEPARATOR);
    }
    // Check that all the parameters that must be specified have been by the user.
    checkRequiredParametersAreSpecified();

    Connection connection = null;
    try {
      Driver dbDriver = (Driver)Class.forName(driver,
                                              true,
                                              artifactClassLoader).newInstance();

      Properties info = new Properties();
      info.put("user", username);
      info.put("password", password);
      connection = dbDriver.connect(url, info);

      if (connection == null) {
        throw new JDBCException("Connection could not be created to " + url
                                + " with driver " + dbDriver.getClass().getName()
                                + ".  Possibly the wrong driver for the given "
                                + "database URL");
      }

      Migrator migrator = new Migrator(changeLogFile.trim(), cfo);
      migrator.setContexts(contexts);
      migrator.init(connection);

      performMigratorConfiguration(migrator);

      getLog().info("Executing on Database: " + url);

      if (promptOnNonLocalDatabase && !migrator.isSafeToRunMigration()) {
        if (migrator.swingPromptForNonLocalDatabase()) {
          throw new LiquibaseException("Chose not to run against non-production database");
        }
      }

      if (dropFirst) {
        migrator.dropAll();
      }
      performLiquibaseTask(migrator);
    }
    catch (ClassNotFoundException e) {
      releaseConnection(connection);
      throw new MojoFailureException("Missing Class '" + e.getMessage() + "'. Database "
                                     + "driver may not be included in the project "
                                     + "dependencies or with wrong scope.");
    }
    catch (Exception e) {
      releaseConnection(connection);
      throw new MojoFailureException(e.getMessage());
    }
    getLog().info(LOG_SEPARATOR);
    getLog().info("");
  }

  /**
   * Performs some validation after the properties file has been loaded checking that all
   * properties required have been specified.
   * @throws MojoFailureException If any property that is required has not been specified.
   */
  protected void checkRequiredParametersAreSpecified() throws MojoFailureException {
    if (driver == null) {
      throw new MojoFailureException("The driver has not been specified either as a "
                                     + "parameter or in a properties file.");
    } else if (url == null) {
      throw new MojoFailureException("The database URL has not been specified either as "
                                     + "a parameter or in a properties file.");
    } else if (changeLogFile == null) {
      throw new MojoFailureException("A DatabaseChangeLog has not been specified either "
                                     + "as a parameter or in a properties file.");
    }
  }

  /**
   * Prints the settings that have been set of defaulted for the plugin. These will only
   * be shown in verbose mode.
   */
  protected void printSettings() {
    getLog().info("   properties file will override? " + propertyFileWillOverride);
    getLog().info("   changeLogFile: " + changeLogFile);
    getLog().info("   driver: " + driver);
    getLog().info("   url: " + url);
    getLog().info("   username: " + username);
    getLog().info("   password: " + password);
    getLog().info("   prompt on non-local database? " + promptOnNonLocalDatabase);
    getLog().info("   drop first? " + dropFirst);
    getLog().info("   context(s): " + contexts);
  }

  /**
   * Performs extra {@link Migrator} configuration as required by the extending class.
   * By default this method does nothing, but sub classes can override this method to
   * perform extra configuration steps on the {@link Migrator}.
   * @param migrator The {@link Migrator} to perform the extra configuration on.
   */
  protected void performMigratorConfiguration(Migrator migrator) throws MojoExecutionException {
  }

  /**
   * Performs the actual Liquibase task on the database using the fully configured
   * {@link liquibase.migrator.Migrator}.
   * @param migrator The {@link liquibase.migrator.Migrator} that has been fully
   * configured to run the desired database task.
   * @throws MojoExecutionException
   */
  protected abstract void performLiquibaseTask(Migrator migrator) throws MojoExecutionException;


  /**
   * Obtains a {@link ClassLoader} that can load from the Maven project dependencies. If
   * the dependencies have not be resolved (or there are none) then this will just end up
   * delegating to the parent {@link ClassLoader} of this class.
   * @return The ClassLoader that can load the resolved dependencies for the Maven
   * project.
   * @throws java.net.MalformedURLException If any of the dependencies cannot be resolved
   * into a URL.
   */
  protected ClassLoader getArtifactClassloader() throws MalformedURLException {
    if (verbose) {
      getLog().info("Loading artfacts into URLClassLoader");
    }
    Set<URL> urls = new HashSet<URL>();

    Set dependencies = project.getDependencyArtifacts();
    if (dependencies != null || !dependencies.isEmpty()) {
      for (Iterator it = dependencies.iterator(); it.hasNext();) {
        addArtifact(urls, (Artifact)it.next());
      }
    } else {
      getLog().info("there are no resolved artifacts for the Maven project.");
    }

    // Include the artifact for the actual maven project if requested
    if (includeArtifact) {
      addArtifact(urls, project.getArtifact());
    }
    if (verbose) {
      getLog().info(LOG_SEPARATOR);
    }

    URL[] urlArray = urls.toArray(new URL[urls.size()]);
    return new URLClassLoader(urlArray, getClass().getClassLoader());
  }

  /**
   * Adds the artifact file into the set of URLs so it can be used in a URLClassLoader.
   * @param urls The set to add the artifact file URL to.
   * @param artifact The Artifiact to resolve the file for.
   * @throws MalformedURLException If there is a problem creating the URL for the file.
   */
  private void addArtifact(Set<URL> urls, Artifact artifact) throws MalformedURLException {
    File f = artifact.getFile();
    if (f != null) {
      URL fileURL = f.toURI().toURL();
      if (verbose) {
        getLog().info("  artifact: " + fileURL);
      }
      urls.add(fileURL);
    } else {
      getLog().warn("Artifact with no actual file, '" + artifact.getGroupId()
                    + ":" + artifact.getArtifactId() + "'");
    }
  }

  protected void releaseConnection(Connection c) {
    if (c != null) {
      try {
        c.close();
      }
      catch (SQLException e) {
        getLog().error("Failed to close open connection to database.", e);
      }
    }
  }

  /**
   * Parses a properties file and sets the assocaited fields in the plugin.
   * @param propertiesInputStream The input stream which is the Liquibase properties that
   * needs to be parsed.
   * @throws org.apache.maven.plugin.MojoExecutionException If there is a problem parsing the file.
   */
  protected void parsePropertiesFile(InputStream propertiesInputStream) throws MojoExecutionException {
    if (propertiesInputStream == null) {
      throw new MojoExecutionException("Properties file InputStream is null.");
    }
    Properties props = new Properties();
    try {
      props.load(propertiesInputStream);
    }
    catch (IOException e) {
      throw new MojoExecutionException("Could not load the properties Liquibase file", e);
    }

    for (Iterator it = props.keySet().iterator(); it.hasNext();) {
      String key = null;
      try {
        key = (String)it.next();
        Field field = AbstractLiquibaseMojo.class.getDeclaredField(key);

        if (propertyFileWillOverride) {
          getLog().debug("  properties file setting value: " + field.getName());
          setFieldValue(field, props.get(key).toString());
        } else {
          if (!isCurrentFieldValueSpecified(field)) {
            getLog().debug("  properties file setting value: " + field.getName());
            setFieldValue(field, props.get(key).toString());
          }
        }
      }
      catch (Exception e) {
        getLog().warn(e);
        // May need to correct this to make it a more useful exception...
        throw new MojoExecutionException("Unknown parameter: '" + key + "'", e);
      }
    }
  }

  /**
   * This method will check to see if the user has specified a value different to that of
   * the default value. This is not an ideal solution, but should cover most situations
   * in the use of the plugin.
   * @param f The Field to check if a user has specified a value for.
   * @return <code>true</code> if the user has specified a value.
   */
  private boolean isCurrentFieldValueSpecified(Field f) throws IllegalAccessException {
    Object currentValue = f.get(this);
    if (currentValue == null) {
      return false;
    }

    Object defaultValue = getDefaultValue(f);
    if (defaultValue == null) {
      return currentValue != null;
    } else {
      // There is a default value, check to see if the user has selected something other
      // than the default
      return !defaultValue.equals(f.get(this));
    }
  }

  private Object getDefaultValue(Field field) throws IllegalAccessException {
    List<Field> allFields = new ArrayList<Field>();
    allFields.addAll(Arrays.asList(getClass().getDeclaredFields()));
    allFields.addAll(Arrays.asList(AbstractLiquibaseMojo.class.getDeclaredFields()));

    for (Field f : allFields) {
      if (f.getName().equals(field.getName() + DEFAULT_FIELD_SUFFIX)) {
        f.setAccessible(true);
        return f.get(this);
      }
    }
    return null;
  }

  private void setFieldValue(Field field, String value) throws IllegalAccessException {
    if (field.getType().equals(Boolean.class)) {
      field.set(this, Boolean.valueOf(value));
    } else {
      field.set(this, value);
    }
  }
}
