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
operator|.
name|util
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
name|assertTrue
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|BufferedWriter
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
name|FileInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileOutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileWriter
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
name|List
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
name|JarEntry
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
name|JarOutputStream
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
name|Manifest
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|tools
operator|.
name|JavaCompiler
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|tools
operator|.
name|JavaFileObject
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|tools
operator|.
name|StandardJavaFileManager
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|tools
operator|.
name|ToolProvider
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
name|fs
operator|.
name|Path
import|;
end_import

begin_comment
comment|/**  * Some utilities to help class loader testing  */
end_comment

begin_class
specifier|public
class|class
name|ClassLoaderTestHelper
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
name|ClassLoaderTestHelper
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Jar a list of files into a jar archive.    *    * @param archiveFile the target jar archive    * @param tobejared a list of files to be jared    */
specifier|private
specifier|static
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
literal|4096
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
comment|/**    * Create a test jar for testing purpose for a given class    * name with specified code string: save the class to a file,    * compile it, and jar it up. If the code string passed in is    * null, a bare empty class will be created and used.    *    * @param testDir the folder under which to store the test class and jar    * @param className the test class name    * @param code the optional test class code, which can be null.    * If null, a bare empty class will be used    * @return the test jar file generated    */
specifier|public
specifier|static
name|File
name|buildJar
parameter_list|(
name|String
name|testDir
parameter_list|,
name|String
name|className
parameter_list|,
name|String
name|code
parameter_list|)
throws|throws
name|Exception
block|{
return|return
name|buildJar
argument_list|(
name|testDir
argument_list|,
name|className
argument_list|,
name|code
argument_list|,
name|testDir
argument_list|)
return|;
block|}
comment|/**    * Create a test jar for testing purpose for a given class    * name with specified code string.    *    * @param testDir the folder under which to store the test class    * @param className the test class name    * @param code the optional test class code, which can be null.    * If null, an empty class will be used    * @param folder the folder under which to store the generated jar    * @return the test jar file generated    */
specifier|public
specifier|static
name|File
name|buildJar
parameter_list|(
name|String
name|testDir
parameter_list|,
name|String
name|className
parameter_list|,
name|String
name|code
parameter_list|,
name|String
name|folder
parameter_list|)
throws|throws
name|Exception
block|{
name|String
name|javaCode
init|=
name|code
operator|!=
literal|null
condition|?
name|code
else|:
literal|"public class "
operator|+
name|className
operator|+
literal|" {}"
decl_stmt|;
name|Path
name|srcDir
init|=
operator|new
name|Path
argument_list|(
name|testDir
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
name|File
operator|.
name|separator
operator|+
literal|"target"
operator|+
name|File
operator|.
name|separator
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
literal|"java.class.path"
argument_list|)
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
name|folder
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

unit|} }
end_unit

