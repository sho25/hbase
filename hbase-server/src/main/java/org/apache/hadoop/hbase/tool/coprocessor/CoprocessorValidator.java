begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
operator|.
name|tool
operator|.
name|coprocessor
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
name|Method
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URL
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URLClassLoader
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|Files
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|Path
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|Paths
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|AccessController
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|PrivilegedAction
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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
name|java
operator|.
name|util
operator|.
name|Optional
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Collectors
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Stream
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
name|fs
operator|.
name|FileSystem
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
name|HBaseInterfaceAudience
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
name|client
operator|.
name|Admin
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
name|client
operator|.
name|Connection
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
name|client
operator|.
name|ConnectionFactory
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
name|client
operator|.
name|CoprocessorDescriptor
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
name|client
operator|.
name|TableDescriptor
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
name|coprocessor
operator|.
name|CoprocessorHost
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
name|tool
operator|.
name|PreUpgradeValidator
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
name|tool
operator|.
name|coprocessor
operator|.
name|CoprocessorViolation
operator|.
name|Severity
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
name|AbstractHBaseTool
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
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
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|annotations
operator|.
name|VisibleForTesting
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|cli
operator|.
name|CommandLine
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
name|HBaseInterfaceAudience
operator|.
name|TOOLS
argument_list|)
specifier|public
class|class
name|CoprocessorValidator
extends|extends
name|AbstractHBaseTool
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
name|CoprocessorValidator
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|CoprocessorMethods
name|branch1
decl_stmt|;
specifier|private
name|CoprocessorMethods
name|current
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|jars
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|Pattern
argument_list|>
name|tablePatterns
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|classes
decl_stmt|;
specifier|private
name|boolean
name|config
decl_stmt|;
specifier|private
name|boolean
name|dieOnWarnings
decl_stmt|;
specifier|public
name|CoprocessorValidator
parameter_list|()
block|{
name|branch1
operator|=
operator|new
name|Branch1CoprocessorMethods
argument_list|()
expr_stmt|;
name|current
operator|=
operator|new
name|CurrentCoprocessorMethods
argument_list|()
expr_stmt|;
name|jars
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
name|tablePatterns
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
name|classes
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
block|}
comment|/**    * This classloader implementation calls {@link #resolveClass(Class)}    * method for every loaded class. It means that some extra validation will    * take place<a    * href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-12.html#jls-12.3">    * according to JLS</a>.    */
specifier|private
specifier|static
specifier|final
class|class
name|ResolverUrlClassLoader
extends|extends
name|URLClassLoader
block|{
specifier|private
name|ResolverUrlClassLoader
parameter_list|(
name|URL
index|[]
name|urls
parameter_list|,
name|ClassLoader
name|parent
parameter_list|)
block|{
name|super
argument_list|(
name|urls
argument_list|,
name|parent
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Class
argument_list|<
name|?
argument_list|>
name|loadClass
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|ClassNotFoundException
block|{
return|return
name|loadClass
argument_list|(
name|name
argument_list|,
literal|true
argument_list|)
return|;
block|}
block|}
specifier|private
name|ResolverUrlClassLoader
name|createClassLoader
parameter_list|(
name|URL
index|[]
name|urls
parameter_list|)
block|{
return|return
name|createClassLoader
argument_list|(
name|urls
argument_list|,
name|getClass
argument_list|()
operator|.
name|getClassLoader
argument_list|()
argument_list|)
return|;
block|}
specifier|private
name|ResolverUrlClassLoader
name|createClassLoader
parameter_list|(
name|URL
index|[]
name|urls
parameter_list|,
name|ClassLoader
name|parent
parameter_list|)
block|{
return|return
name|AccessController
operator|.
name|doPrivileged
argument_list|(
operator|new
name|PrivilegedAction
argument_list|<
name|ResolverUrlClassLoader
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|ResolverUrlClassLoader
name|run
parameter_list|()
block|{
return|return
operator|new
name|ResolverUrlClassLoader
argument_list|(
name|urls
argument_list|,
name|parent
argument_list|)
return|;
block|}
block|}
argument_list|)
return|;
block|}
specifier|private
name|ResolverUrlClassLoader
name|createClassLoader
parameter_list|(
name|ClassLoader
name|parent
parameter_list|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|fs
operator|.
name|Path
name|path
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|tempPath
init|=
name|Files
operator|.
name|createTempFile
argument_list|(
literal|"hbase-coprocessor-"
argument_list|,
literal|".jar"
argument_list|)
decl_stmt|;
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|fs
operator|.
name|Path
name|destination
init|=
operator|new
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|fs
operator|.
name|Path
argument_list|(
name|tempPath
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Copying coprocessor jar '{}' to '{}'."
argument_list|,
name|path
argument_list|,
name|tempPath
argument_list|)
expr_stmt|;
name|FileSystem
name|fileSystem
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
name|fileSystem
operator|.
name|copyToLocalFile
argument_list|(
name|path
argument_list|,
name|destination
argument_list|)
expr_stmt|;
name|URL
name|url
init|=
name|tempPath
operator|.
name|toUri
argument_list|()
operator|.
name|toURL
argument_list|()
decl_stmt|;
return|return
name|createClassLoader
argument_list|(
operator|new
name|URL
index|[]
block|{
name|url
block|}
argument_list|,
name|parent
argument_list|)
return|;
block|}
specifier|private
name|void
name|validate
parameter_list|(
name|ClassLoader
name|classLoader
parameter_list|,
name|String
name|className
parameter_list|,
name|List
argument_list|<
name|CoprocessorViolation
argument_list|>
name|violations
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Validating class '{}'."
argument_list|,
name|className
argument_list|)
expr_stmt|;
try|try
block|{
name|Class
argument_list|<
name|?
argument_list|>
name|clazz
init|=
name|classLoader
operator|.
name|loadClass
argument_list|(
name|className
argument_list|)
decl_stmt|;
for|for
control|(
name|Method
name|method
range|:
name|clazz
operator|.
name|getDeclaredMethods
argument_list|()
control|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Validating method '{}'."
argument_list|,
name|method
argument_list|)
expr_stmt|;
if|if
condition|(
name|branch1
operator|.
name|hasMethod
argument_list|(
name|method
argument_list|)
operator|&&
operator|!
name|current
operator|.
name|hasMethod
argument_list|(
name|method
argument_list|)
condition|)
block|{
name|CoprocessorViolation
name|violation
init|=
operator|new
name|CoprocessorViolation
argument_list|(
name|className
argument_list|,
name|Severity
operator|.
name|WARNING
argument_list|,
literal|"method '"
operator|+
name|method
operator|+
literal|"' was removed from new coprocessor API, so it won't be called by HBase"
argument_list|)
decl_stmt|;
name|violations
operator|.
name|add
argument_list|(
name|violation
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|e
parameter_list|)
block|{
name|CoprocessorViolation
name|violation
init|=
operator|new
name|CoprocessorViolation
argument_list|(
name|className
argument_list|,
name|Severity
operator|.
name|ERROR
argument_list|,
literal|"no such class"
argument_list|,
name|e
argument_list|)
decl_stmt|;
name|violations
operator|.
name|add
argument_list|(
name|violation
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RuntimeException
decl||
name|Error
name|e
parameter_list|)
block|{
name|CoprocessorViolation
name|violation
init|=
operator|new
name|CoprocessorViolation
argument_list|(
name|className
argument_list|,
name|Severity
operator|.
name|ERROR
argument_list|,
literal|"could not validate class"
argument_list|,
name|e
argument_list|)
decl_stmt|;
name|violations
operator|.
name|add
argument_list|(
name|violation
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|validateClasses
parameter_list|(
name|ClassLoader
name|classLoader
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|classNames
parameter_list|,
name|List
argument_list|<
name|CoprocessorViolation
argument_list|>
name|violations
parameter_list|)
block|{
for|for
control|(
name|String
name|className
range|:
name|classNames
control|)
block|{
name|validate
argument_list|(
name|classLoader
argument_list|,
name|className
argument_list|,
name|violations
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|validateClasses
parameter_list|(
name|ClassLoader
name|classLoader
parameter_list|,
name|String
index|[]
name|classNames
parameter_list|,
name|List
argument_list|<
name|CoprocessorViolation
argument_list|>
name|violations
parameter_list|)
block|{
name|validateClasses
argument_list|(
name|classLoader
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|classNames
argument_list|)
argument_list|,
name|violations
argument_list|)
expr_stmt|;
block|}
annotation|@
name|VisibleForTesting
specifier|protected
name|void
name|validateTables
parameter_list|(
name|ClassLoader
name|classLoader
parameter_list|,
name|Admin
name|admin
parameter_list|,
name|Pattern
name|pattern
parameter_list|,
name|List
argument_list|<
name|CoprocessorViolation
argument_list|>
name|violations
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|TableDescriptor
argument_list|>
name|tableDescriptors
init|=
name|admin
operator|.
name|listTableDescriptors
argument_list|(
name|pattern
argument_list|)
decl_stmt|;
for|for
control|(
name|TableDescriptor
name|tableDescriptor
range|:
name|tableDescriptors
control|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Validating table {}"
argument_list|,
name|tableDescriptor
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|Collection
argument_list|<
name|CoprocessorDescriptor
argument_list|>
name|coprocessorDescriptors
init|=
name|tableDescriptor
operator|.
name|getCoprocessorDescriptors
argument_list|()
decl_stmt|;
for|for
control|(
name|CoprocessorDescriptor
name|coprocessorDescriptor
range|:
name|coprocessorDescriptors
control|)
block|{
name|String
name|className
init|=
name|coprocessorDescriptor
operator|.
name|getClassName
argument_list|()
decl_stmt|;
name|Optional
argument_list|<
name|String
argument_list|>
name|jarPath
init|=
name|coprocessorDescriptor
operator|.
name|getJarPath
argument_list|()
decl_stmt|;
if|if
condition|(
name|jarPath
operator|.
name|isPresent
argument_list|()
condition|)
block|{
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|fs
operator|.
name|Path
name|path
init|=
operator|new
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|fs
operator|.
name|Path
argument_list|(
name|jarPath
operator|.
name|get
argument_list|()
argument_list|)
decl_stmt|;
try|try
init|(
name|ResolverUrlClassLoader
name|cpClassLoader
init|=
name|createClassLoader
argument_list|(
name|classLoader
argument_list|,
name|path
argument_list|)
init|)
block|{
name|validate
argument_list|(
name|cpClassLoader
argument_list|,
name|className
argument_list|,
name|violations
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|CoprocessorViolation
name|violation
init|=
operator|new
name|CoprocessorViolation
argument_list|(
name|className
argument_list|,
name|Severity
operator|.
name|ERROR
argument_list|,
literal|"could not validate jar file '"
operator|+
name|path
operator|+
literal|"'"
argument_list|,
name|e
argument_list|)
decl_stmt|;
name|violations
operator|.
name|add
argument_list|(
name|violation
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|validate
argument_list|(
name|classLoader
argument_list|,
name|className
argument_list|,
name|violations
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
specifier|private
name|void
name|validateTables
parameter_list|(
name|ClassLoader
name|classLoader
parameter_list|,
name|Pattern
name|pattern
parameter_list|,
name|List
argument_list|<
name|CoprocessorViolation
argument_list|>
name|violations
parameter_list|)
throws|throws
name|IOException
block|{
try|try
init|(
name|Connection
name|connection
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|getConf
argument_list|()
argument_list|)
init|;
name|Admin
name|admin
operator|=
name|connection
operator|.
name|getAdmin
argument_list|()
init|)
block|{
name|validateTables
argument_list|(
name|classLoader
argument_list|,
name|admin
argument_list|,
name|pattern
argument_list|,
name|violations
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|void
name|printUsage
parameter_list|()
block|{
name|String
name|header
init|=
literal|"hbase "
operator|+
name|PreUpgradeValidator
operator|.
name|TOOL_NAME
operator|+
literal|" "
operator|+
name|PreUpgradeValidator
operator|.
name|VALIDATE_CP_NAME
operator|+
literal|" [-jar ...] [-class ... | -table ... | -config]"
decl_stmt|;
name|printUsage
argument_list|(
name|header
argument_list|,
literal|"Options:"
argument_list|,
literal|""
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|addOptions
parameter_list|()
block|{
name|addOptNoArg
argument_list|(
literal|"e"
argument_list|,
literal|"Treat warnings as errors."
argument_list|)
expr_stmt|;
name|addOptWithArg
argument_list|(
literal|"jar"
argument_list|,
literal|"Jar file/directory of the coprocessor."
argument_list|)
expr_stmt|;
name|addOptWithArg
argument_list|(
literal|"table"
argument_list|,
literal|"Table coprocessor(s) to check."
argument_list|)
expr_stmt|;
name|addOptWithArg
argument_list|(
literal|"class"
argument_list|,
literal|"Coprocessor class(es) to check."
argument_list|)
expr_stmt|;
name|addOptNoArg
argument_list|(
literal|"config"
argument_list|,
literal|"Obtain coprocessor class(es) from configuration."
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|processOptions
parameter_list|(
name|CommandLine
name|cmd
parameter_list|)
block|{
name|String
index|[]
name|jars
init|=
name|cmd
operator|.
name|getOptionValues
argument_list|(
literal|"jar"
argument_list|)
decl_stmt|;
if|if
condition|(
name|jars
operator|!=
literal|null
condition|)
block|{
name|Collections
operator|.
name|addAll
argument_list|(
name|this
operator|.
name|jars
argument_list|,
name|jars
argument_list|)
expr_stmt|;
block|}
name|String
index|[]
name|tables
init|=
name|cmd
operator|.
name|getOptionValues
argument_list|(
literal|"table"
argument_list|)
decl_stmt|;
if|if
condition|(
name|tables
operator|!=
literal|null
condition|)
block|{
name|Arrays
operator|.
name|stream
argument_list|(
name|tables
argument_list|)
operator|.
name|map
argument_list|(
name|Pattern
operator|::
name|compile
argument_list|)
operator|.
name|forEach
argument_list|(
name|tablePatterns
operator|::
name|add
argument_list|)
expr_stmt|;
block|}
name|String
index|[]
name|classes
init|=
name|cmd
operator|.
name|getOptionValues
argument_list|(
literal|"class"
argument_list|)
decl_stmt|;
if|if
condition|(
name|classes
operator|!=
literal|null
condition|)
block|{
name|Collections
operator|.
name|addAll
argument_list|(
name|this
operator|.
name|classes
argument_list|,
name|classes
argument_list|)
expr_stmt|;
block|}
name|config
operator|=
name|cmd
operator|.
name|hasOption
argument_list|(
literal|"config"
argument_list|)
expr_stmt|;
name|dieOnWarnings
operator|=
name|cmd
operator|.
name|hasOption
argument_list|(
literal|"e"
argument_list|)
expr_stmt|;
block|}
specifier|private
name|List
argument_list|<
name|URL
argument_list|>
name|buildClasspath
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|jars
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|URL
argument_list|>
name|urls
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|jar
range|:
name|jars
control|)
block|{
name|Path
name|jarPath
init|=
name|Paths
operator|.
name|get
argument_list|(
name|jar
argument_list|)
decl_stmt|;
if|if
condition|(
name|Files
operator|.
name|isDirectory
argument_list|(
name|jarPath
argument_list|)
condition|)
block|{
try|try
init|(
name|Stream
argument_list|<
name|Path
argument_list|>
name|stream
init|=
name|Files
operator|.
name|list
argument_list|(
name|jarPath
argument_list|)
init|)
block|{
name|List
argument_list|<
name|Path
argument_list|>
name|files
init|=
name|stream
operator|.
name|filter
argument_list|(
parameter_list|(
name|path
parameter_list|)
lambda|->
name|Files
operator|.
name|isRegularFile
argument_list|(
name|path
argument_list|)
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|Path
name|file
range|:
name|files
control|)
block|{
name|URL
name|url
init|=
name|file
operator|.
name|toUri
argument_list|()
operator|.
name|toURL
argument_list|()
decl_stmt|;
name|urls
operator|.
name|add
argument_list|(
name|url
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
name|URL
name|url
init|=
name|jarPath
operator|.
name|toUri
argument_list|()
operator|.
name|toURL
argument_list|()
decl_stmt|;
name|urls
operator|.
name|add
argument_list|(
name|url
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|urls
return|;
block|}
annotation|@
name|Override
specifier|protected
name|int
name|doWork
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
name|tablePatterns
operator|.
name|isEmpty
argument_list|()
operator|&&
name|classes
operator|.
name|isEmpty
argument_list|()
operator|&&
operator|!
name|config
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Please give at least one -table, -class or -config parameter."
argument_list|)
expr_stmt|;
name|printUsage
argument_list|()
expr_stmt|;
return|return
name|EXIT_FAILURE
return|;
block|}
name|List
argument_list|<
name|URL
argument_list|>
name|urlList
init|=
name|buildClasspath
argument_list|(
name|jars
argument_list|)
decl_stmt|;
name|URL
index|[]
name|urls
init|=
name|urlList
operator|.
name|toArray
argument_list|(
operator|new
name|URL
index|[
name|urlList
operator|.
name|size
argument_list|()
index|]
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Classpath: {}"
argument_list|,
name|urlList
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|CoprocessorViolation
argument_list|>
name|violations
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
try|try
init|(
name|ResolverUrlClassLoader
name|classLoader
init|=
name|createClassLoader
argument_list|(
name|urls
argument_list|)
init|)
block|{
for|for
control|(
name|Pattern
name|tablePattern
range|:
name|tablePatterns
control|)
block|{
name|validateTables
argument_list|(
name|classLoader
argument_list|,
name|tablePattern
argument_list|,
name|violations
argument_list|)
expr_stmt|;
block|}
name|validateClasses
argument_list|(
name|classLoader
argument_list|,
name|classes
argument_list|,
name|violations
argument_list|)
expr_stmt|;
if|if
condition|(
name|config
condition|)
block|{
name|String
index|[]
name|masterCoprocessors
init|=
name|getConf
argument_list|()
operator|.
name|getStrings
argument_list|(
name|CoprocessorHost
operator|.
name|MASTER_COPROCESSOR_CONF_KEY
argument_list|)
decl_stmt|;
if|if
condition|(
name|masterCoprocessors
operator|!=
literal|null
condition|)
block|{
name|validateClasses
argument_list|(
name|classLoader
argument_list|,
name|masterCoprocessors
argument_list|,
name|violations
argument_list|)
expr_stmt|;
block|}
name|String
index|[]
name|regionCoprocessors
init|=
name|getConf
argument_list|()
operator|.
name|getStrings
argument_list|(
name|CoprocessorHost
operator|.
name|REGION_COPROCESSOR_CONF_KEY
argument_list|)
decl_stmt|;
if|if
condition|(
name|regionCoprocessors
operator|!=
literal|null
condition|)
block|{
name|validateClasses
argument_list|(
name|classLoader
argument_list|,
name|regionCoprocessors
argument_list|,
name|violations
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|boolean
name|error
init|=
literal|false
decl_stmt|;
for|for
control|(
name|CoprocessorViolation
name|violation
range|:
name|violations
control|)
block|{
name|String
name|className
init|=
name|violation
operator|.
name|getClassName
argument_list|()
decl_stmt|;
name|String
name|message
init|=
name|violation
operator|.
name|getMessage
argument_list|()
decl_stmt|;
name|Throwable
name|throwable
init|=
name|violation
operator|.
name|getThrowable
argument_list|()
decl_stmt|;
switch|switch
condition|(
name|violation
operator|.
name|getSeverity
argument_list|()
condition|)
block|{
case|case
name|WARNING
case|:
if|if
condition|(
name|throwable
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Warning in class '{}': {}."
argument_list|,
name|className
argument_list|,
name|message
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Warning in class '{}': {}."
argument_list|,
name|className
argument_list|,
name|message
argument_list|,
name|throwable
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|dieOnWarnings
condition|)
block|{
name|error
operator|=
literal|true
expr_stmt|;
block|}
break|break;
case|case
name|ERROR
case|:
if|if
condition|(
name|throwable
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Error in class '{}': {}."
argument_list|,
name|className
argument_list|,
name|message
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Error in class '{}': {}."
argument_list|,
name|className
argument_list|,
name|message
argument_list|,
name|throwable
argument_list|)
expr_stmt|;
block|}
name|error
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
return|return
operator|(
name|error
operator|)
condition|?
name|EXIT_FAILURE
else|:
name|EXIT_SUCCESS
return|;
block|}
block|}
end_class

end_unit

