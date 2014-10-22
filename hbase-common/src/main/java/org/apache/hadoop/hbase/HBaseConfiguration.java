begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|InvocationTargetException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Method
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
operator|.
name|Entry
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|conf
operator|.
name|Configuration
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|classification
operator|.
name|InterfaceAudience
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|classification
operator|.
name|InterfaceStability
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|io
operator|.
name|util
operator|.
name|HeapMemorySizeUtil
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|VersionInfo
import|;
end_import

begin_comment
comment|/**  * Adds HBase configuration files to a Configuration  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Stable
specifier|public
class|class
name|HBaseConfiguration
extends|extends
name|Configuration
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|HBaseConfiguration
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Instantinating HBaseConfiguration() is deprecated. Please use    * HBaseConfiguration#create() to construct a plain Configuration    */
annotation|@
name|Deprecated
specifier|public
name|HBaseConfiguration
parameter_list|()
block|{
comment|//TODO:replace with private constructor, HBaseConfiguration should not extend Configuration
name|super
argument_list|()
expr_stmt|;
name|addHbaseResources
argument_list|(
name|this
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
literal|"instantiating HBaseConfiguration() is deprecated. Please use"
operator|+
literal|" HBaseConfiguration#create() to construct a plain Configuration"
argument_list|)
expr_stmt|;
block|}
comment|/**    * Instantiating HBaseConfiguration() is deprecated. Please use    * HBaseConfiguration#create(conf) to construct a plain Configuration    */
annotation|@
name|Deprecated
specifier|public
name|HBaseConfiguration
parameter_list|(
specifier|final
name|Configuration
name|c
parameter_list|)
block|{
comment|//TODO:replace with private constructor
name|this
argument_list|()
expr_stmt|;
name|merge
argument_list|(
name|this
argument_list|,
name|c
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|checkDefaultsVersion
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
if|if
condition|(
name|conf
operator|.
name|getBoolean
argument_list|(
literal|"hbase.defaults.for.version.skip"
argument_list|,
name|Boolean
operator|.
name|FALSE
argument_list|)
condition|)
return|return;
name|String
name|defaultsVersion
init|=
name|conf
operator|.
name|get
argument_list|(
literal|"hbase.defaults.for.version"
argument_list|)
decl_stmt|;
name|String
name|thisVersion
init|=
name|VersionInfo
operator|.
name|getVersion
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|thisVersion
operator|.
name|equals
argument_list|(
name|defaultsVersion
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"hbase-default.xml file seems to be for and old version of HBase ("
operator|+
name|defaultsVersion
operator|+
literal|"), this version is "
operator|+
name|thisVersion
argument_list|)
throw|;
block|}
block|}
specifier|public
specifier|static
name|Configuration
name|addHbaseResources
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|conf
operator|.
name|addResource
argument_list|(
literal|"hbase-default.xml"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|addResource
argument_list|(
literal|"hbase-site.xml"
argument_list|)
expr_stmt|;
name|checkDefaultsVersion
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|HeapMemorySizeUtil
operator|.
name|checkForClusterFreeMemoryLimit
argument_list|(
name|conf
argument_list|)
expr_stmt|;
return|return
name|conf
return|;
block|}
comment|/**    * Creates a Configuration with HBase resources    * @return a Configuration with HBase resources    */
specifier|public
specifier|static
name|Configuration
name|create
parameter_list|()
block|{
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
return|return
name|addHbaseResources
argument_list|(
name|conf
argument_list|)
return|;
block|}
comment|/**    * @param that Configuration to clone.    * @return a Configuration created with the hbase-*.xml files plus    * the given configuration.    */
specifier|public
specifier|static
name|Configuration
name|create
parameter_list|(
specifier|final
name|Configuration
name|that
parameter_list|)
block|{
name|Configuration
name|conf
init|=
name|create
argument_list|()
decl_stmt|;
name|merge
argument_list|(
name|conf
argument_list|,
name|that
argument_list|)
expr_stmt|;
return|return
name|conf
return|;
block|}
comment|/**    * Merge two configurations.    * @param destConf the configuration that will be overwritten with items    *                 from the srcConf    * @param srcConf the source configuration    **/
specifier|public
specifier|static
name|void
name|merge
parameter_list|(
name|Configuration
name|destConf
parameter_list|,
name|Configuration
name|srcConf
parameter_list|)
block|{
for|for
control|(
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|e
range|:
name|srcConf
control|)
block|{
name|destConf
operator|.
name|set
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|,
name|e
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * @return whether to show HBase Configuration in servlet    */
specifier|public
specifier|static
name|boolean
name|isShowConfInServlet
parameter_list|()
block|{
name|boolean
name|isShowConf
init|=
literal|false
decl_stmt|;
try|try
block|{
if|if
condition|(
name|Class
operator|.
name|forName
argument_list|(
literal|"org.apache.hadoop.conf.ConfServlet"
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|isShowConf
operator|=
literal|true
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|LinkageError
name|e
parameter_list|)
block|{
comment|// should we handle it more aggressively in addition to log the error?
name|LOG
operator|.
name|warn
argument_list|(
literal|"Error thrown: "
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|ce
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"ClassNotFound: ConfServlet"
argument_list|)
expr_stmt|;
comment|// ignore
block|}
return|return
name|isShowConf
return|;
block|}
comment|/**    * Get the value of the<code>name</code> property as an<code>int</code>, possibly    * referring to the deprecated name of the configuration property.    * If no such property exists, the provided default value is returned,    * or if the specified value is not a valid<code>int</code>,    * then an error is thrown.    *    * @param name property name.    * @param deprecatedName a deprecatedName for the property to use    * if non-deprecated name is not used    * @param defaultValue default value.    * @throws NumberFormatException when the value is invalid    * @return property value as an<code>int</code>,    *         or<code>defaultValue</code>.    */
comment|// TODO: developer note: This duplicates the functionality of deprecated
comment|// property support in Configuration in Hadoop 2. But since Hadoop-1 does not
comment|// contain these changes, we will do our own as usual. Replace these when H2 is default.
specifier|public
specifier|static
name|int
name|getInt
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|name
parameter_list|,
name|String
name|deprecatedName
parameter_list|,
name|int
name|defaultValue
parameter_list|)
block|{
if|if
condition|(
name|conf
operator|.
name|get
argument_list|(
name|deprecatedName
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Config option \"%s\" is deprecated. Instead, use \"%s\""
argument_list|,
name|deprecatedName
argument_list|,
name|name
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|conf
operator|.
name|getInt
argument_list|(
name|deprecatedName
argument_list|,
name|defaultValue
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|conf
operator|.
name|getInt
argument_list|(
name|name
argument_list|,
name|defaultValue
argument_list|)
return|;
block|}
block|}
comment|/**    * Get the password from the Configuration instance using the    * getPassword method if it exists. If not, then fall back to the    * general get method for configuration elements.    * @param conf configuration instance for accessing the passwords    * @param alias the name of the password element    * @param defPass the default password    * @return String password or default password    * @throws IOException    */
specifier|public
specifier|static
name|String
name|getPassword
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|alias
parameter_list|,
name|String
name|defPass
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|passwd
init|=
literal|null
decl_stmt|;
try|try
block|{
name|Method
name|m
init|=
name|Configuration
operator|.
name|class
operator|.
name|getMethod
argument_list|(
literal|"getPassword"
argument_list|,
name|String
operator|.
name|class
argument_list|)
decl_stmt|;
name|char
index|[]
name|p
init|=
operator|(
name|char
index|[]
operator|)
name|m
operator|.
name|invoke
argument_list|(
name|conf
argument_list|,
name|alias
argument_list|)
decl_stmt|;
if|if
condition|(
name|p
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Config option \"%s\" was found through"
operator|+
literal|" the Configuration getPassword method."
argument_list|,
name|alias
argument_list|)
argument_list|)
expr_stmt|;
name|passwd
operator|=
operator|new
name|String
argument_list|(
name|p
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Config option \"%s\" was not found. Using provided default value"
argument_list|,
name|alias
argument_list|)
argument_list|)
expr_stmt|;
name|passwd
operator|=
name|defPass
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|NoSuchMethodException
name|e
parameter_list|)
block|{
comment|// this is a version of Hadoop where the credential
comment|//provider API doesn't exist yet
name|LOG
operator|.
name|debug
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Credential.getPassword method is not available."
operator|+
literal|" Falling back to configuration."
argument_list|)
argument_list|)
expr_stmt|;
name|passwd
operator|=
name|conf
operator|.
name|get
argument_list|(
name|alias
argument_list|,
name|defPass
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SecurityException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|IllegalAccessException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|InvocationTargetException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
return|return
name|passwd
return|;
block|}
comment|/** For debugging.  Dump configurations to system output as xml format.    * Master and RS configurations can also be dumped using    * http services. e.g. "curl http://master:16010/dump"    */
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
name|HBaseConfiguration
operator|.
name|create
argument_list|()
operator|.
name|writeXml
argument_list|(
name|System
operator|.
name|out
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

