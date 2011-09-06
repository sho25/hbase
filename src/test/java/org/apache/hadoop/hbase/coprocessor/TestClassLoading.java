begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2011 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|coprocessor
package|;
end_package

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
name|HBaseTestingUtility
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
name|HColumnDescriptor
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
name|HTableDescriptor
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
name|MiniHBaseCluster
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
name|CoprocessorEnvironment
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
name|Coprocessor
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
name|HBaseAdmin
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
name|regionserver
operator|.
name|HRegion
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
name|hdfs
operator|.
name|MiniDFSCluster
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
name|fs
operator|.
name|Path
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|tools
operator|.
name|*
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|*
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|*
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|jar
operator|.
name|*
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|*
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
name|assertTrue
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
name|assertFalse
import|;
end_import

begin_comment
comment|/**  * Test coprocessors class loading.  */
end_comment

begin_class
specifier|public
class|class
name|TestClassLoading
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
name|TestClassLoading
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|static
name|MiniDFSCluster
name|cluster
decl_stmt|;
specifier|static
specifier|final
name|int
name|BUFFER_SIZE
init|=
literal|4096
decl_stmt|;
specifier|static
specifier|final
name|String
name|tableName
init|=
literal|"TestClassLoading"
decl_stmt|;
specifier|static
specifier|final
name|String
name|cpName1
init|=
literal|"TestCP1"
decl_stmt|;
specifier|static
specifier|final
name|String
name|cpName2
init|=
literal|"TestCP2"
decl_stmt|;
specifier|static
specifier|final
name|String
name|cpName3
init|=
literal|"TestCP3"
decl_stmt|;
specifier|static
specifier|final
name|String
name|cpName4
init|=
literal|"TestCP4"
decl_stmt|;
specifier|static
specifier|final
name|String
name|cpName5
init|=
literal|"TestCP5"
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUpBeforeClass
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|conf
operator|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
name|cluster
operator|=
name|TEST_UTIL
operator|.
name|getDFSCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDownAfterClass
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
comment|// generate jar file
specifier|private
name|boolean
name|createJarArchive
parameter_list|(
name|File
name|archiveFile
parameter_list|,
name|File
index|[]
name|tobeJared
parameter_list|)
block|{
try|try
block|{
name|byte
name|buffer
index|[]
init|=
operator|new
name|byte
index|[
name|BUFFER_SIZE
index|]
decl_stmt|;
comment|// Open archive file
name|FileOutputStream
name|stream
init|=
operator|new
name|FileOutputStream
argument_list|(
name|archiveFile
argument_list|)
decl_stmt|;
name|JarOutputStream
name|out
init|=
operator|new
name|JarOutputStream
argument_list|(
name|stream
argument_list|,
operator|new
name|Manifest
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|tobeJared
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|tobeJared
index|[
name|i
index|]
operator|==
literal|null
operator|||
operator|!
name|tobeJared
index|[
name|i
index|]
operator|.
name|exists
argument_list|()
operator|||
name|tobeJared
index|[
name|i
index|]
operator|.
name|isDirectory
argument_list|()
condition|)
block|{
continue|continue;
block|}
comment|// Add archive entry
name|JarEntry
name|jarAdd
init|=
operator|new
name|JarEntry
argument_list|(
name|tobeJared
index|[
name|i
index|]
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|jarAdd
operator|.
name|setTime
argument_list|(
name|tobeJared
index|[
name|i
index|]
operator|.
name|lastModified
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|putNextEntry
argument_list|(
name|jarAdd
argument_list|)
expr_stmt|;
comment|// Write file to archive
name|FileInputStream
name|in
init|=
operator|new
name|FileInputStream
argument_list|(
name|tobeJared
index|[
name|i
index|]
argument_list|)
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|int
name|nRead
init|=
name|in
operator|.
name|read
argument_list|(
name|buffer
argument_list|,
literal|0
argument_list|,
name|buffer
operator|.
name|length
argument_list|)
decl_stmt|;
if|if
condition|(
name|nRead
operator|<=
literal|0
condition|)
break|break;
name|out
operator|.
name|write
argument_list|(
name|buffer
argument_list|,
literal|0
argument_list|,
name|nRead
argument_list|)
expr_stmt|;
block|}
name|in
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|out
operator|.
name|close
argument_list|()
expr_stmt|;
name|stream
operator|.
name|close
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Adding classes to jar file completed"
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Error: "
operator|+
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
block|}
specifier|private
name|File
name|buildCoprocessorJar
parameter_list|(
name|String
name|className
parameter_list|)
throws|throws
name|Exception
block|{
comment|// compose a java source file.
name|String
name|javaCode
init|=
literal|"import org.apache.hadoop.hbase.coprocessor.*;"
operator|+
literal|"public class "
operator|+
name|className
operator|+
literal|" extends BaseRegionObserver {}"
decl_stmt|;
name|Path
name|baseDir
init|=
name|HBaseTestingUtility
operator|.
name|getTestDir
argument_list|()
decl_stmt|;
name|Path
name|srcDir
init|=
operator|new
name|Path
argument_list|(
name|HBaseTestingUtility
operator|.
name|getTestDir
argument_list|()
argument_list|,
literal|"src"
argument_list|)
decl_stmt|;
name|File
name|srcDirPath
init|=
operator|new
name|File
argument_list|(
name|srcDir
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|srcDirPath
operator|.
name|mkdirs
argument_list|()
expr_stmt|;
name|File
name|sourceCodeFile
init|=
operator|new
name|File
argument_list|(
name|srcDir
operator|.
name|toString
argument_list|()
argument_list|,
name|className
operator|+
literal|".java"
argument_list|)
decl_stmt|;
name|BufferedWriter
name|bw
init|=
operator|new
name|BufferedWriter
argument_list|(
operator|new
name|FileWriter
argument_list|(
name|sourceCodeFile
argument_list|)
argument_list|)
decl_stmt|;
name|bw
operator|.
name|write
argument_list|(
name|javaCode
argument_list|)
expr_stmt|;
name|bw
operator|.
name|close
argument_list|()
expr_stmt|;
comment|// compile it by JavaCompiler
name|JavaCompiler
name|compiler
init|=
name|ToolProvider
operator|.
name|getSystemJavaCompiler
argument_list|()
decl_stmt|;
name|ArrayList
argument_list|<
name|String
argument_list|>
name|srcFileNames
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|srcFileNames
operator|.
name|add
argument_list|(
name|sourceCodeFile
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|StandardJavaFileManager
name|fm
init|=
name|compiler
operator|.
name|getStandardFileManager
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|Iterable
argument_list|<
name|?
extends|extends
name|JavaFileObject
argument_list|>
name|cu
init|=
name|fm
operator|.
name|getJavaFileObjects
argument_list|(
name|sourceCodeFile
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|options
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|options
operator|.
name|add
argument_list|(
literal|"-classpath"
argument_list|)
expr_stmt|;
comment|// only add hbase classes to classpath. This is a little bit tricky: assume
comment|// the classpath is {hbaseSrc}/target/classes.
name|String
name|currentDir
init|=
operator|new
name|File
argument_list|(
literal|"."
argument_list|)
operator|.
name|getAbsolutePath
argument_list|()
decl_stmt|;
name|String
name|classpath
init|=
name|currentDir
operator|+
name|Path
operator|.
name|SEPARATOR
operator|+
literal|"target"
operator|+
name|Path
operator|.
name|SEPARATOR
operator|+
literal|"classes"
operator|+
name|System
operator|.
name|getProperty
argument_list|(
literal|"path.separator"
argument_list|)
operator|+
name|System
operator|.
name|getProperty
argument_list|(
literal|"surefire.test.class.path"
argument_list|)
decl_stmt|;
name|options
operator|.
name|add
argument_list|(
name|classpath
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Setting classpath to: "
operator|+
name|classpath
argument_list|)
expr_stmt|;
name|JavaCompiler
operator|.
name|CompilationTask
name|task
init|=
name|compiler
operator|.
name|getTask
argument_list|(
literal|null
argument_list|,
name|fm
argument_list|,
literal|null
argument_list|,
name|options
argument_list|,
literal|null
argument_list|,
name|cu
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Compile file "
operator|+
name|sourceCodeFile
operator|+
literal|" failed."
argument_list|,
name|task
operator|.
name|call
argument_list|()
argument_list|)
expr_stmt|;
comment|// build a jar file by the classes files
name|String
name|jarFileName
init|=
name|className
operator|+
literal|".jar"
decl_stmt|;
name|File
name|jarFile
init|=
operator|new
name|File
argument_list|(
name|baseDir
operator|.
name|toString
argument_list|()
argument_list|,
name|jarFileName
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|createJarArchive
argument_list|(
name|jarFile
argument_list|,
operator|new
name|File
index|[]
block|{
operator|new
name|File
argument_list|(
name|srcDir
operator|.
name|toString
argument_list|()
argument_list|,
name|className
operator|+
literal|".class"
argument_list|)
block|}
block|)
block|)
block|{
name|assertTrue
argument_list|(
literal|"Build jar file failed."
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
end_class

begin_return
return|return
name|jarFile
return|;
end_return

begin_function
unit|}    @
name|Test
comment|// HBASE-3516: Test CP Class loading from HDFS
specifier|public
name|void
name|testClassLoadingFromHDFS
parameter_list|()
throws|throws
name|Exception
block|{
name|FileSystem
name|fs
init|=
name|cluster
operator|.
name|getFileSystem
argument_list|()
decl_stmt|;
name|File
name|jarFile1
init|=
name|buildCoprocessorJar
argument_list|(
name|cpName1
argument_list|)
decl_stmt|;
name|File
name|jarFile2
init|=
name|buildCoprocessorJar
argument_list|(
name|cpName2
argument_list|)
decl_stmt|;
comment|// copy the jars into dfs
name|fs
operator|.
name|copyFromLocalFile
argument_list|(
operator|new
name|Path
argument_list|(
name|jarFile1
operator|.
name|getPath
argument_list|()
argument_list|)
argument_list|,
operator|new
name|Path
argument_list|(
name|fs
operator|.
name|getUri
argument_list|()
operator|.
name|toString
argument_list|()
operator|+
name|Path
operator|.
name|SEPARATOR
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|jarFileOnHDFS1
init|=
name|fs
operator|.
name|getUri
argument_list|()
operator|.
name|toString
argument_list|()
operator|+
name|Path
operator|.
name|SEPARATOR
operator|+
name|jarFile1
operator|.
name|getName
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Copy jar file to HDFS failed."
argument_list|,
name|fs
operator|.
name|exists
argument_list|(
operator|new
name|Path
argument_list|(
name|jarFileOnHDFS1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Copied jar file to HDFS: "
operator|+
name|jarFileOnHDFS1
argument_list|)
expr_stmt|;
name|fs
operator|.
name|copyFromLocalFile
argument_list|(
operator|new
name|Path
argument_list|(
name|jarFile2
operator|.
name|getPath
argument_list|()
argument_list|)
argument_list|,
operator|new
name|Path
argument_list|(
name|fs
operator|.
name|getUri
argument_list|()
operator|.
name|toString
argument_list|()
operator|+
name|Path
operator|.
name|SEPARATOR
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|jarFileOnHDFS2
init|=
name|fs
operator|.
name|getUri
argument_list|()
operator|.
name|toString
argument_list|()
operator|+
name|Path
operator|.
name|SEPARATOR
operator|+
name|jarFile2
operator|.
name|getName
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Copy jar file to HDFS failed."
argument_list|,
name|fs
operator|.
name|exists
argument_list|(
operator|new
name|Path
argument_list|(
name|jarFileOnHDFS2
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Copied jar file to HDFS: "
operator|+
name|jarFileOnHDFS2
argument_list|)
expr_stmt|;
comment|// create a table that references the coprocessors
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
literal|"test"
argument_list|)
argument_list|)
expr_stmt|;
comment|// without configuration values
name|htd
operator|.
name|setValue
argument_list|(
literal|"COPROCESSOR$1"
argument_list|,
name|jarFileOnHDFS1
operator|.
name|toString
argument_list|()
operator|+
literal|"|"
operator|+
name|cpName1
operator|+
literal|"|"
operator|+
name|Coprocessor
operator|.
name|PRIORITY_USER
argument_list|)
expr_stmt|;
comment|// with configuration values
name|htd
operator|.
name|setValue
argument_list|(
literal|"COPROCESSOR$2"
argument_list|,
name|jarFileOnHDFS2
operator|.
name|toString
argument_list|()
operator|+
literal|"|"
operator|+
name|cpName2
operator|+
literal|"|"
operator|+
name|Coprocessor
operator|.
name|PRIORITY_USER
operator|+
literal|"|k1=v1,k2=v2,k3=v3"
argument_list|)
expr_stmt|;
name|HBaseAdmin
name|admin
init|=
operator|new
name|HBaseAdmin
argument_list|(
name|this
operator|.
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
name|admin
operator|.
name|tableExists
argument_list|(
name|tableName
argument_list|)
condition|)
block|{
name|admin
operator|.
name|disableTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|admin
operator|.
name|deleteTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
name|admin
operator|.
name|createTable
argument_list|(
name|htd
argument_list|)
expr_stmt|;
comment|// verify that the coprocessors were loaded
name|boolean
name|found1
init|=
literal|false
decl_stmt|,
name|found2
init|=
literal|false
decl_stmt|,
name|found2_k1
init|=
literal|false
decl_stmt|,
name|found2_k2
init|=
literal|false
decl_stmt|,
name|found2_k3
init|=
literal|false
decl_stmt|;
name|MiniHBaseCluster
name|hbase
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
decl_stmt|;
for|for
control|(
name|HRegion
name|region
range|:
name|hbase
operator|.
name|getRegionServer
argument_list|(
literal|0
argument_list|)
operator|.
name|getOnlineRegionsLocalContext
argument_list|()
control|)
block|{
if|if
condition|(
name|region
operator|.
name|getRegionNameAsString
argument_list|()
operator|.
name|startsWith
argument_list|(
name|tableName
argument_list|)
condition|)
block|{
name|CoprocessorEnvironment
name|env
decl_stmt|;
name|env
operator|=
name|region
operator|.
name|getCoprocessorHost
argument_list|()
operator|.
name|findCoprocessorEnvironment
argument_list|(
name|cpName1
argument_list|)
expr_stmt|;
if|if
condition|(
name|env
operator|!=
literal|null
condition|)
block|{
name|found1
operator|=
literal|true
expr_stmt|;
block|}
name|env
operator|=
name|region
operator|.
name|getCoprocessorHost
argument_list|()
operator|.
name|findCoprocessorEnvironment
argument_list|(
name|cpName2
argument_list|)
expr_stmt|;
if|if
condition|(
name|env
operator|!=
literal|null
condition|)
block|{
name|found2
operator|=
literal|true
expr_stmt|;
name|Configuration
name|conf
init|=
name|env
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|found2_k1
operator|=
name|conf
operator|.
name|get
argument_list|(
literal|"k1"
argument_list|)
operator|!=
literal|null
expr_stmt|;
name|found2_k2
operator|=
name|conf
operator|.
name|get
argument_list|(
literal|"k2"
argument_list|)
operator|!=
literal|null
expr_stmt|;
name|found2_k3
operator|=
name|conf
operator|.
name|get
argument_list|(
literal|"k3"
argument_list|)
operator|!=
literal|null
expr_stmt|;
block|}
block|}
block|}
name|assertTrue
argument_list|(
literal|"Class "
operator|+
name|cpName1
operator|+
literal|" was missing on a region"
argument_list|,
name|found1
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Class "
operator|+
name|cpName2
operator|+
literal|" was missing on a region"
argument_list|,
name|found2
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Configuration key 'k1' was missing on a region"
argument_list|,
name|found2_k1
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Configuration key 'k2' was missing on a region"
argument_list|,
name|found2_k2
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Configuration key 'k3' was missing on a region"
argument_list|,
name|found2_k3
argument_list|)
expr_stmt|;
block|}
end_function

begin_function
annotation|@
name|Test
comment|// HBASE-3516: Test CP Class loading from local file system
specifier|public
name|void
name|testClassLoadingFromLocalFS
parameter_list|()
throws|throws
name|Exception
block|{
name|File
name|jarFile
init|=
name|buildCoprocessorJar
argument_list|(
name|cpName3
argument_list|)
decl_stmt|;
comment|// create a table that references the jar
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|cpName3
argument_list|)
decl_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
literal|"test"
argument_list|)
argument_list|)
expr_stmt|;
name|htd
operator|.
name|setValue
argument_list|(
literal|"COPROCESSOR$1"
argument_list|,
name|jarFile
operator|.
name|toString
argument_list|()
operator|+
literal|"|"
operator|+
name|cpName3
operator|+
literal|"|"
operator|+
name|Coprocessor
operator|.
name|PRIORITY_USER
argument_list|)
expr_stmt|;
name|HBaseAdmin
name|admin
init|=
operator|new
name|HBaseAdmin
argument_list|(
name|this
operator|.
name|conf
argument_list|)
decl_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|htd
argument_list|)
expr_stmt|;
comment|// verify that the coprocessor was loaded
name|boolean
name|found
init|=
literal|false
decl_stmt|;
name|MiniHBaseCluster
name|hbase
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
decl_stmt|;
for|for
control|(
name|HRegion
name|region
range|:
name|hbase
operator|.
name|getRegionServer
argument_list|(
literal|0
argument_list|)
operator|.
name|getOnlineRegionsLocalContext
argument_list|()
control|)
block|{
if|if
condition|(
name|region
operator|.
name|getRegionNameAsString
argument_list|()
operator|.
name|startsWith
argument_list|(
name|cpName3
argument_list|)
condition|)
block|{
name|found
operator|=
operator|(
name|region
operator|.
name|getCoprocessorHost
argument_list|()
operator|.
name|findCoprocessor
argument_list|(
name|cpName3
argument_list|)
operator|!=
literal|null
operator|)
expr_stmt|;
block|}
block|}
name|assertTrue
argument_list|(
literal|"Class "
operator|+
name|cpName3
operator|+
literal|" was missing on a region"
argument_list|,
name|found
argument_list|)
expr_stmt|;
block|}
end_function

begin_function
annotation|@
name|Test
comment|// HBase-3810: Registering a Coprocessor at HTableDescriptor should be
comment|// less strict
specifier|public
name|void
name|testHBase3810
parameter_list|()
throws|throws
name|Exception
block|{
comment|// allowed value pattern: [path] | class name | [priority] | [key values]
name|File
name|jarFile1
init|=
name|buildCoprocessorJar
argument_list|(
name|cpName1
argument_list|)
decl_stmt|;
name|File
name|jarFile2
init|=
name|buildCoprocessorJar
argument_list|(
name|cpName2
argument_list|)
decl_stmt|;
name|File
name|jarFile4
init|=
name|buildCoprocessorJar
argument_list|(
name|cpName4
argument_list|)
decl_stmt|;
name|File
name|jarFile5
init|=
name|buildCoprocessorJar
argument_list|(
name|cpName5
argument_list|)
decl_stmt|;
name|String
name|cpKey1
init|=
literal|"COPROCESSOR$1"
decl_stmt|;
name|String
name|cpKey2
init|=
literal|" Coprocessor$2 "
decl_stmt|;
name|String
name|cpKey3
init|=
literal|" coprocessor$03 "
decl_stmt|;
name|String
name|cpValue1
init|=
name|jarFile1
operator|.
name|toString
argument_list|()
operator|+
literal|"|"
operator|+
name|cpName1
operator|+
literal|"|"
operator|+
name|Coprocessor
operator|.
name|PRIORITY_USER
decl_stmt|;
name|String
name|cpValue2
init|=
name|jarFile2
operator|.
name|toString
argument_list|()
operator|+
literal|" | "
operator|+
name|cpName2
operator|+
literal|" | "
decl_stmt|;
comment|// load from default class loader
name|String
name|cpValue3
init|=
literal|" | org.apache.hadoop.hbase.coprocessor.SimpleRegionObserver | | k=v "
decl_stmt|;
comment|// create a table that references the jar
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
literal|"test"
argument_list|)
argument_list|)
expr_stmt|;
comment|// add 3 coprocessors by setting htd attributes directly.
name|htd
operator|.
name|setValue
argument_list|(
name|cpKey1
argument_list|,
name|cpValue1
argument_list|)
expr_stmt|;
name|htd
operator|.
name|setValue
argument_list|(
name|cpKey2
argument_list|,
name|cpValue2
argument_list|)
expr_stmt|;
name|htd
operator|.
name|setValue
argument_list|(
name|cpKey3
argument_list|,
name|cpValue3
argument_list|)
expr_stmt|;
comment|// add 2 coprocessor by using new htd.addCoprocessor() api
name|htd
operator|.
name|addCoprocessor
argument_list|(
name|cpName4
argument_list|,
operator|new
name|Path
argument_list|(
name|jarFile4
operator|.
name|getPath
argument_list|()
argument_list|)
argument_list|,
name|Coprocessor
operator|.
name|PRIORITY_USER
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|kvs
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|kvs
operator|.
name|put
argument_list|(
literal|"k1"
argument_list|,
literal|"v1"
argument_list|)
expr_stmt|;
name|kvs
operator|.
name|put
argument_list|(
literal|"k2"
argument_list|,
literal|"v2"
argument_list|)
expr_stmt|;
name|kvs
operator|.
name|put
argument_list|(
literal|"k3"
argument_list|,
literal|"v3"
argument_list|)
expr_stmt|;
name|htd
operator|.
name|addCoprocessor
argument_list|(
name|cpName5
argument_list|,
operator|new
name|Path
argument_list|(
name|jarFile5
operator|.
name|getPath
argument_list|()
argument_list|)
argument_list|,
name|Coprocessor
operator|.
name|PRIORITY_USER
argument_list|,
name|kvs
argument_list|)
expr_stmt|;
name|HBaseAdmin
name|admin
init|=
operator|new
name|HBaseAdmin
argument_list|(
name|this
operator|.
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
name|admin
operator|.
name|tableExists
argument_list|(
name|tableName
argument_list|)
condition|)
block|{
name|admin
operator|.
name|disableTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|admin
operator|.
name|deleteTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
name|admin
operator|.
name|createTable
argument_list|(
name|htd
argument_list|)
expr_stmt|;
comment|// verify that the coprocessor was loaded
name|boolean
name|found_2
init|=
literal|false
decl_stmt|,
name|found_1
init|=
literal|false
decl_stmt|,
name|found_3
init|=
literal|false
decl_stmt|,
name|found_4
init|=
literal|false
decl_stmt|,
name|found_5
init|=
literal|false
decl_stmt|;
name|boolean
name|found5_k1
init|=
literal|false
decl_stmt|,
name|found5_k2
init|=
literal|false
decl_stmt|,
name|found5_k3
init|=
literal|false
decl_stmt|,
name|found5_k4
init|=
literal|false
decl_stmt|;
name|MiniHBaseCluster
name|hbase
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
decl_stmt|;
for|for
control|(
name|HRegion
name|region
range|:
name|hbase
operator|.
name|getRegionServer
argument_list|(
literal|0
argument_list|)
operator|.
name|getOnlineRegionsLocalContext
argument_list|()
control|)
block|{
if|if
condition|(
name|region
operator|.
name|getRegionNameAsString
argument_list|()
operator|.
name|startsWith
argument_list|(
name|tableName
argument_list|)
condition|)
block|{
name|found_1
operator|=
name|found_1
operator|||
operator|(
name|region
operator|.
name|getCoprocessorHost
argument_list|()
operator|.
name|findCoprocessor
argument_list|(
name|cpName1
argument_list|)
operator|!=
literal|null
operator|)
expr_stmt|;
name|found_2
operator|=
name|found_2
operator|||
operator|(
name|region
operator|.
name|getCoprocessorHost
argument_list|()
operator|.
name|findCoprocessor
argument_list|(
name|cpName2
argument_list|)
operator|!=
literal|null
operator|)
expr_stmt|;
name|found_3
operator|=
name|found_3
operator|||
operator|(
name|region
operator|.
name|getCoprocessorHost
argument_list|()
operator|.
name|findCoprocessor
argument_list|(
literal|"SimpleRegionObserver"
argument_list|)
operator|!=
literal|null
operator|)
expr_stmt|;
name|found_4
operator|=
name|found_4
operator|||
operator|(
name|region
operator|.
name|getCoprocessorHost
argument_list|()
operator|.
name|findCoprocessor
argument_list|(
name|cpName4
argument_list|)
operator|!=
literal|null
operator|)
expr_stmt|;
name|CoprocessorEnvironment
name|env
init|=
name|region
operator|.
name|getCoprocessorHost
argument_list|()
operator|.
name|findCoprocessorEnvironment
argument_list|(
name|cpName5
argument_list|)
decl_stmt|;
if|if
condition|(
name|env
operator|!=
literal|null
condition|)
block|{
name|found_5
operator|=
literal|true
expr_stmt|;
name|Configuration
name|conf
init|=
name|env
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|found5_k1
operator|=
name|conf
operator|.
name|get
argument_list|(
literal|"k1"
argument_list|)
operator|!=
literal|null
expr_stmt|;
name|found5_k2
operator|=
name|conf
operator|.
name|get
argument_list|(
literal|"k2"
argument_list|)
operator|!=
literal|null
expr_stmt|;
name|found5_k3
operator|=
name|conf
operator|.
name|get
argument_list|(
literal|"k3"
argument_list|)
operator|!=
literal|null
expr_stmt|;
block|}
block|}
block|}
name|assertTrue
argument_list|(
literal|"Class "
operator|+
name|cpName1
operator|+
literal|" was missing on a region"
argument_list|,
name|found_1
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Class "
operator|+
name|cpName2
operator|+
literal|" was missing on a region"
argument_list|,
name|found_2
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Class SimpleRegionObserver was missing on a region"
argument_list|,
name|found_3
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Class "
operator|+
name|cpName4
operator|+
literal|" was missing on a region"
argument_list|,
name|found_4
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Class "
operator|+
name|cpName5
operator|+
literal|" was missing on a region"
argument_list|,
name|found_5
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Configuration key 'k1' was missing on a region"
argument_list|,
name|found5_k1
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Configuration key 'k2' was missing on a region"
argument_list|,
name|found5_k2
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Configuration key 'k3' was missing on a region"
argument_list|,
name|found5_k3
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"Configuration key 'k4' wasn't configured"
argument_list|,
name|found5_k4
argument_list|)
expr_stmt|;
block|}
end_function

unit|}
end_unit

