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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertNull
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|fail
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|File
import|;
end_import

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
name|List
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
name|testclassification
operator|.
name|MiscTests
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
name|testclassification
operator|.
name|SmallTests
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|AfterClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableMap
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MiscTests
operator|.
name|class
block|,
name|SmallTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestHBaseConfiguration
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TestHBaseConfiguration
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|HBaseCommonTestingUtility
name|UTIL
init|=
operator|new
name|HBaseCommonTestingUtility
argument_list|()
decl_stmt|;
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDown
parameter_list|()
throws|throws
name|IOException
block|{
name|UTIL
operator|.
name|cleanupTestDir
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetIntDeprecated
parameter_list|()
block|{
name|int
name|VAL
init|=
literal|1
decl_stmt|,
name|VAL2
init|=
literal|2
decl_stmt|;
name|String
name|NAME
init|=
literal|"foo"
decl_stmt|;
name|String
name|DEPRECATED_NAME
init|=
literal|"foo.deprecated"
decl_stmt|;
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|NAME
argument_list|,
name|VAL
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|VAL
argument_list|,
name|HBaseConfiguration
operator|.
name|getInt
argument_list|(
name|conf
argument_list|,
name|NAME
argument_list|,
name|DEPRECATED_NAME
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|conf
operator|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|DEPRECATED_NAME
argument_list|,
name|VAL
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|VAL
argument_list|,
name|HBaseConfiguration
operator|.
name|getInt
argument_list|(
name|conf
argument_list|,
name|NAME
argument_list|,
name|DEPRECATED_NAME
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|conf
operator|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|DEPRECATED_NAME
argument_list|,
name|VAL
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|NAME
argument_list|,
name|VAL
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|VAL
argument_list|,
name|HBaseConfiguration
operator|.
name|getInt
argument_list|(
name|conf
argument_list|,
name|NAME
argument_list|,
name|DEPRECATED_NAME
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|conf
operator|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|DEPRECATED_NAME
argument_list|,
name|VAL
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|NAME
argument_list|,
name|VAL2
argument_list|)
expr_stmt|;
comment|// deprecated value will override this
name|assertEquals
argument_list|(
name|VAL
argument_list|,
name|HBaseConfiguration
operator|.
name|getInt
argument_list|(
name|conf
argument_list|,
name|NAME
argument_list|,
name|DEPRECATED_NAME
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSubset
parameter_list|()
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
comment|// subset is used in TableMapReduceUtil#initCredentials to support different security
comment|// configurations between source and destination clusters, so we'll use that as an example
name|String
name|prefix
init|=
literal|"hbase.mapred.output."
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"hbase.security.authentication"
argument_list|,
literal|"kerberos"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"hbase.regionserver.kerberos.principal"
argument_list|,
literal|"hbasesource"
argument_list|)
expr_stmt|;
name|HBaseConfiguration
operator|.
name|setWithPrefix
argument_list|(
name|conf
argument_list|,
name|prefix
argument_list|,
name|ImmutableMap
operator|.
name|of
argument_list|(
literal|"hbase.regionserver.kerberos.principal"
argument_list|,
literal|"hbasedest"
argument_list|,
literal|""
argument_list|,
literal|"shouldbemissing"
argument_list|)
operator|.
name|entrySet
argument_list|()
argument_list|)
expr_stmt|;
name|Configuration
name|subsetConf
init|=
name|HBaseConfiguration
operator|.
name|subset
argument_list|(
name|conf
argument_list|,
name|prefix
argument_list|)
decl_stmt|;
name|assertNull
argument_list|(
name|subsetConf
operator|.
name|get
argument_list|(
name|prefix
operator|+
literal|"hbase.regionserver.kerberos.principal"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"hbasedest"
argument_list|,
name|subsetConf
operator|.
name|get
argument_list|(
literal|"hbase.regionserver.kerberos.principal"
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|subsetConf
operator|.
name|get
argument_list|(
literal|"hbase.security.authentication"
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|subsetConf
operator|.
name|get
argument_list|(
literal|""
argument_list|)
argument_list|)
expr_stmt|;
name|Configuration
name|mergedConf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|HBaseConfiguration
operator|.
name|merge
argument_list|(
name|mergedConf
argument_list|,
name|subsetConf
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"hbasedest"
argument_list|,
name|mergedConf
operator|.
name|get
argument_list|(
literal|"hbase.regionserver.kerberos.principal"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"kerberos"
argument_list|,
name|mergedConf
operator|.
name|get
argument_list|(
literal|"hbase.security.authentication"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"shouldbemissing"
argument_list|,
name|mergedConf
operator|.
name|get
argument_list|(
name|prefix
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetPassword
parameter_list|()
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|ReflectiveCredentialProviderClient
operator|.
name|CREDENTIAL_PROVIDER_PATH
argument_list|,
literal|"jceks://file"
operator|+
operator|new
name|File
argument_list|(
name|UTIL
operator|.
name|getDataTestDir
argument_list|()
operator|.
name|toUri
argument_list|()
operator|.
name|getPath
argument_list|()
argument_list|,
literal|"foo.jks"
argument_list|)
operator|.
name|getCanonicalPath
argument_list|()
argument_list|)
expr_stmt|;
name|ReflectiveCredentialProviderClient
name|client
init|=
operator|new
name|ReflectiveCredentialProviderClient
argument_list|()
decl_stmt|;
if|if
condition|(
name|client
operator|.
name|isHadoopCredentialProviderAvailable
argument_list|()
condition|)
block|{
name|char
index|[]
name|keyPass
init|=
block|{
literal|'k'
block|,
literal|'e'
block|,
literal|'y'
block|,
literal|'p'
block|,
literal|'a'
block|,
literal|'s'
block|,
literal|'s'
block|}
decl_stmt|;
name|char
index|[]
name|storePass
init|=
block|{
literal|'s'
block|,
literal|'t'
block|,
literal|'o'
block|,
literal|'r'
block|,
literal|'e'
block|,
literal|'p'
block|,
literal|'a'
block|,
literal|'s'
block|,
literal|'s'
block|}
decl_stmt|;
name|client
operator|.
name|createEntry
argument_list|(
name|conf
argument_list|,
literal|"ssl.keypass.alias"
argument_list|,
name|keyPass
argument_list|)
expr_stmt|;
name|client
operator|.
name|createEntry
argument_list|(
name|conf
argument_list|,
literal|"ssl.storepass.alias"
argument_list|,
name|storePass
argument_list|)
expr_stmt|;
name|String
name|keypass
init|=
name|HBaseConfiguration
operator|.
name|getPassword
argument_list|(
name|conf
argument_list|,
literal|"ssl.keypass.alias"
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|keypass
argument_list|,
operator|new
name|String
argument_list|(
name|keyPass
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|storepass
init|=
name|HBaseConfiguration
operator|.
name|getPassword
argument_list|(
name|conf
argument_list|,
literal|"ssl.storepass.alias"
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|storepass
argument_list|,
operator|new
name|String
argument_list|(
name|storePass
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
class|class
name|ReflectiveCredentialProviderClient
block|{
specifier|public
specifier|static
specifier|final
name|String
name|HADOOP_CRED_PROVIDER_FACTORY_CLASS_NAME
init|=
literal|"org.apache.hadoop.security.alias.JavaKeyStoreProvider$Factory"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HADOOP_CRED_PROVIDER_FACTORY_GET_PROVIDERS_METHOD_NAME
init|=
literal|"getProviders"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HADOOP_CRED_PROVIDER_CLASS_NAME
init|=
literal|"org.apache.hadoop.security.alias.CredentialProvider"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HADOOP_CRED_PROVIDER_GET_CREDENTIAL_ENTRY_METHOD_NAME
init|=
literal|"getCredentialEntry"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HADOOP_CRED_PROVIDER_GET_ALIASES_METHOD_NAME
init|=
literal|"getAliases"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HADOOP_CRED_PROVIDER_CREATE_CREDENTIAL_ENTRY_METHOD_NAME
init|=
literal|"createCredentialEntry"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HADOOP_CRED_PROVIDER_FLUSH_METHOD_NAME
init|=
literal|"flush"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HADOOP_CRED_ENTRY_CLASS_NAME
init|=
literal|"org.apache.hadoop.security.alias.CredentialProvider$CredentialEntry"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HADOOP_CRED_ENTRY_GET_CREDENTIAL_METHOD_NAME
init|=
literal|"getCredential"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|CREDENTIAL_PROVIDER_PATH
init|=
literal|"hadoop.security.credential.provider.path"
decl_stmt|;
specifier|private
specifier|static
name|Object
name|hadoopCredProviderFactory
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
name|Method
name|getProvidersMethod
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
name|Method
name|getAliasesMethod
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
name|Method
name|getCredentialEntryMethod
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
name|Method
name|getCredentialMethod
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
name|Method
name|createCredentialEntryMethod
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
name|Method
name|flushMethod
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
name|Boolean
name|hadoopClassesAvailable
init|=
literal|null
decl_stmt|;
comment|/**      * Determine if we can load the necessary CredentialProvider classes. Only      * loaded the first time, so subsequent invocations of this method should      * return fast.      *      * @return True if the CredentialProvider classes/methods are available,      *         false otherwise.      */
specifier|private
name|boolean
name|isHadoopCredentialProviderAvailable
parameter_list|()
block|{
if|if
condition|(
literal|null
operator|!=
name|hadoopClassesAvailable
condition|)
block|{
comment|// Make sure everything is initialized as expected
if|if
condition|(
name|hadoopClassesAvailable
operator|&&
literal|null
operator|!=
name|getProvidersMethod
operator|&&
literal|null
operator|!=
name|hadoopCredProviderFactory
operator|&&
literal|null
operator|!=
name|getCredentialEntryMethod
operator|&&
literal|null
operator|!=
name|getCredentialMethod
condition|)
block|{
return|return
literal|true
return|;
block|}
else|else
block|{
comment|// Otherwise we failed to load it
return|return
literal|false
return|;
block|}
block|}
name|hadoopClassesAvailable
operator|=
literal|false
expr_stmt|;
comment|// Load Hadoop CredentialProviderFactory
name|Class
argument_list|<
name|?
argument_list|>
name|hadoopCredProviderFactoryClz
init|=
literal|null
decl_stmt|;
try|try
block|{
name|hadoopCredProviderFactoryClz
operator|=
name|Class
operator|.
name|forName
argument_list|(
name|HADOOP_CRED_PROVIDER_FACTORY_CLASS_NAME
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|e
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
comment|// Instantiate Hadoop CredentialProviderFactory
try|try
block|{
name|hadoopCredProviderFactory
operator|=
name|hadoopCredProviderFactoryClz
operator|.
name|getDeclaredConstructor
argument_list|()
operator|.
name|newInstance
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
try|try
block|{
name|getProvidersMethod
operator|=
name|loadMethod
argument_list|(
name|hadoopCredProviderFactoryClz
argument_list|,
name|HADOOP_CRED_PROVIDER_FACTORY_GET_PROVIDERS_METHOD_NAME
argument_list|,
name|Configuration
operator|.
name|class
argument_list|)
expr_stmt|;
comment|// Load Hadoop CredentialProvider
name|Class
argument_list|<
name|?
argument_list|>
name|hadoopCredProviderClz
init|=
literal|null
decl_stmt|;
name|hadoopCredProviderClz
operator|=
name|Class
operator|.
name|forName
argument_list|(
name|HADOOP_CRED_PROVIDER_CLASS_NAME
argument_list|)
expr_stmt|;
name|getCredentialEntryMethod
operator|=
name|loadMethod
argument_list|(
name|hadoopCredProviderClz
argument_list|,
name|HADOOP_CRED_PROVIDER_GET_CREDENTIAL_ENTRY_METHOD_NAME
argument_list|,
name|String
operator|.
name|class
argument_list|)
expr_stmt|;
name|getAliasesMethod
operator|=
name|loadMethod
argument_list|(
name|hadoopCredProviderClz
argument_list|,
name|HADOOP_CRED_PROVIDER_GET_ALIASES_METHOD_NAME
argument_list|)
expr_stmt|;
name|createCredentialEntryMethod
operator|=
name|loadMethod
argument_list|(
name|hadoopCredProviderClz
argument_list|,
name|HADOOP_CRED_PROVIDER_CREATE_CREDENTIAL_ENTRY_METHOD_NAME
argument_list|,
name|String
operator|.
name|class
argument_list|,
name|char
index|[]
operator|.
expr|class
argument_list|)
expr_stmt|;
name|flushMethod
operator|=
name|loadMethod
argument_list|(
name|hadoopCredProviderClz
argument_list|,
name|HADOOP_CRED_PROVIDER_FLUSH_METHOD_NAME
argument_list|)
expr_stmt|;
comment|// Load Hadoop CredentialEntry
name|Class
argument_list|<
name|?
argument_list|>
name|hadoopCredentialEntryClz
init|=
literal|null
decl_stmt|;
try|try
block|{
name|hadoopCredentialEntryClz
operator|=
name|Class
operator|.
name|forName
argument_list|(
name|HADOOP_CRED_ENTRY_CLASS_NAME
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to load class:"
operator|+
name|e
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
name|getCredentialMethod
operator|=
name|loadMethod
argument_list|(
name|hadoopCredentialEntryClz
argument_list|,
name|HADOOP_CRED_ENTRY_GET_CREDENTIAL_METHOD_NAME
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e1
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
name|hadoopClassesAvailable
operator|=
literal|true
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Credential provider classes have been"
operator|+
literal|" loaded and initialized successfully through reflection."
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
specifier|private
name|Method
name|loadMethod
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|clz
parameter_list|,
name|String
name|name
parameter_list|,
name|Class
argument_list|<
name|?
argument_list|>
modifier|...
name|classes
parameter_list|)
throws|throws
name|Exception
block|{
name|Method
name|method
init|=
literal|null
decl_stmt|;
try|try
block|{
name|method
operator|=
name|clz
operator|.
name|getMethod
argument_list|(
name|name
argument_list|,
name|classes
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SecurityException
name|e
parameter_list|)
block|{
name|fail
argument_list|(
literal|"security exception caught for: "
operator|+
name|name
operator|+
literal|" in "
operator|+
name|clz
operator|.
name|getCanonicalName
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
catch|catch
parameter_list|(
name|NoSuchMethodException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to load the "
operator|+
name|name
operator|+
literal|": "
operator|+
name|e
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"no such method: "
operator|+
name|name
operator|+
literal|" in "
operator|+
name|clz
operator|.
name|getCanonicalName
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
return|return
name|method
return|;
block|}
comment|/**      * Wrapper to fetch the configured {@code List<CredentialProvider>}s.      *      * @param conf      *    Configuration with GENERAL_SECURITY_CREDENTIAL_PROVIDER_PATHS defined      * @return List of CredentialProviders, or null if they could not be loaded      */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|protected
name|List
argument_list|<
name|Object
argument_list|>
name|getCredentialProviders
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
comment|// Call CredentialProviderFactory.getProviders(Configuration)
name|Object
name|providersObj
init|=
literal|null
decl_stmt|;
try|try
block|{
name|providersObj
operator|=
name|getProvidersMethod
operator|.
name|invoke
argument_list|(
name|hadoopCredProviderFactory
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to invoke: "
operator|+
name|getProvidersMethod
operator|.
name|getName
argument_list|()
operator|+
literal|": "
operator|+
name|e
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
catch|catch
parameter_list|(
name|IllegalAccessException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to invoke: "
operator|+
name|getProvidersMethod
operator|.
name|getName
argument_list|()
operator|+
literal|": "
operator|+
name|e
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
catch|catch
parameter_list|(
name|InvocationTargetException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to invoke: "
operator|+
name|getProvidersMethod
operator|.
name|getName
argument_list|()
operator|+
literal|": "
operator|+
name|e
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
comment|// Cast the Object to List<Object> (actually List<CredentialProvider>)
try|try
block|{
return|return
operator|(
name|List
argument_list|<
name|Object
argument_list|>
operator|)
name|providersObj
return|;
block|}
catch|catch
parameter_list|(
name|ClassCastException
name|e
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
block|}
comment|/**      * Create a CredentialEntry using the configured Providers.      * If multiple CredentialProviders are configured, the first will be used.      *      * @param conf      *          Configuration for the CredentialProvider      * @param name      *          CredentialEntry name (alias)      * @param credential      *          The credential      */
specifier|public
name|void
name|createEntry
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|name
parameter_list|,
name|char
index|[]
name|credential
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
operator|!
name|isHadoopCredentialProviderAvailable
argument_list|()
condition|)
block|{
return|return;
block|}
name|List
argument_list|<
name|Object
argument_list|>
name|providers
init|=
name|getCredentialProviders
argument_list|(
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
literal|null
operator|==
name|providers
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Could not fetch any CredentialProviders, "
operator|+
literal|"is the implementation available?"
argument_list|)
throw|;
block|}
name|Object
name|provider
init|=
name|providers
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|createEntryInProvider
argument_list|(
name|provider
argument_list|,
name|name
argument_list|,
name|credential
argument_list|)
expr_stmt|;
block|}
comment|/**      * Create a CredentialEntry with the give name and credential in the      * credentialProvider. The credentialProvider argument must be an instance      * of Hadoop      * CredentialProvider.      *      * @param credentialProvider      *          Instance of CredentialProvider      * @param name      *          CredentialEntry name (alias)      * @param credential      *          The credential to store      */
specifier|private
name|void
name|createEntryInProvider
parameter_list|(
name|Object
name|credentialProvider
parameter_list|,
name|String
name|name
parameter_list|,
name|char
index|[]
name|credential
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
operator|!
name|isHadoopCredentialProviderAvailable
argument_list|()
condition|)
block|{
return|return;
block|}
try|try
block|{
name|createCredentialEntryMethod
operator|.
name|invoke
argument_list|(
name|credentialProvider
argument_list|,
name|name
argument_list|,
name|credential
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
return|return;
block|}
catch|catch
parameter_list|(
name|IllegalAccessException
name|e
parameter_list|)
block|{
return|return;
block|}
catch|catch
parameter_list|(
name|InvocationTargetException
name|e
parameter_list|)
block|{
return|return;
block|}
try|try
block|{
name|flushMethod
operator|.
name|invoke
argument_list|(
name|credentialProvider
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
throw|throw
name|e
throw|;
block|}
catch|catch
parameter_list|(
name|IllegalAccessException
name|e
parameter_list|)
block|{
throw|throw
name|e
throw|;
block|}
catch|catch
parameter_list|(
name|InvocationTargetException
name|e
parameter_list|)
block|{
throw|throw
name|e
throw|;
block|}
block|}
block|}
block|}
end_class

end_unit

