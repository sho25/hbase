begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|assertFalse
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
name|assertNotNull
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
name|fs
operator|.
name|Path
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
name|HBaseCommonTestingUtility
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
name|HBaseConfiguration
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
name|SmallTests
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
name|io
operator|.
name|IOUtils
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

begin_comment
comment|/**  * Test TestCoprocessorClassLoader. More tests are in TestClassLoading  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
name|SmallTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestCoprocessorClassLoader
block|{
specifier|private
specifier|static
specifier|final
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HBaseCommonTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseCommonTestingUtility
argument_list|()
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testCleanupOldJars
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|className
init|=
literal|"TestCleanupOldJars"
decl_stmt|;
name|String
name|folder
init|=
name|TEST_UTIL
operator|.
name|getDataTestDir
argument_list|()
operator|.
name|toString
argument_list|()
decl_stmt|;
name|File
name|jarFile
init|=
name|ClassLoaderTestHelper
operator|.
name|buildJar
argument_list|(
name|folder
argument_list|,
name|className
argument_list|,
literal|null
argument_list|,
name|ClassLoaderTestHelper
operator|.
name|localDirPath
argument_list|(
name|conf
argument_list|)
argument_list|)
decl_stmt|;
name|File
name|tmpJarFile
init|=
operator|new
name|File
argument_list|(
name|jarFile
operator|.
name|getParent
argument_list|()
argument_list|,
literal|"/tmp/"
operator|+
name|className
operator|+
literal|".test.jar"
argument_list|)
decl_stmt|;
if|if
condition|(
name|tmpJarFile
operator|.
name|exists
argument_list|()
condition|)
name|tmpJarFile
operator|.
name|delete
argument_list|()
expr_stmt|;
name|assertFalse
argument_list|(
literal|"tmp jar file should not exist"
argument_list|,
name|tmpJarFile
operator|.
name|exists
argument_list|()
argument_list|)
expr_stmt|;
name|IOUtils
operator|.
name|copyBytes
argument_list|(
operator|new
name|FileInputStream
argument_list|(
name|jarFile
argument_list|)
argument_list|,
operator|new
name|FileOutputStream
argument_list|(
name|tmpJarFile
argument_list|)
argument_list|,
name|conf
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"tmp jar file should be created"
argument_list|,
name|tmpJarFile
operator|.
name|exists
argument_list|()
argument_list|)
expr_stmt|;
name|Path
name|path
init|=
operator|new
name|Path
argument_list|(
name|jarFile
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
decl_stmt|;
name|ClassLoader
name|parent
init|=
name|TestCoprocessorClassLoader
operator|.
name|class
operator|.
name|getClassLoader
argument_list|()
decl_stmt|;
name|CoprocessorClassLoader
operator|.
name|parentDirLockSet
operator|.
name|clear
argument_list|()
expr_stmt|;
comment|// So that clean up can be triggered
name|ClassLoader
name|classLoader
init|=
name|CoprocessorClassLoader
operator|.
name|getClassLoader
argument_list|(
name|path
argument_list|,
name|parent
argument_list|,
literal|"111"
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
literal|"Classloader should be created"
argument_list|,
name|classLoader
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"tmp jar file should be removed"
argument_list|,
name|tmpJarFile
operator|.
name|exists
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testLibJarName
parameter_list|()
throws|throws
name|Exception
block|{
name|checkingLibJarName
argument_list|(
literal|"TestLibJarName.jar"
argument_list|,
literal|"/lib/"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRelativeLibJarName
parameter_list|()
throws|throws
name|Exception
block|{
name|checkingLibJarName
argument_list|(
literal|"TestRelativeLibJarName.jar"
argument_list|,
literal|"lib/"
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test to make sure the lib jar file extracted from a coprocessor jar have    * the right name.  Otherwise, some existing jar could be override if there are    * naming conflicts.    */
specifier|private
name|void
name|checkingLibJarName
parameter_list|(
name|String
name|jarName
parameter_list|,
name|String
name|libPrefix
parameter_list|)
throws|throws
name|Exception
block|{
name|File
name|tmpFolder
init|=
operator|new
name|File
argument_list|(
name|ClassLoaderTestHelper
operator|.
name|localDirPath
argument_list|(
name|conf
argument_list|)
argument_list|,
literal|"tmp"
argument_list|)
decl_stmt|;
if|if
condition|(
name|tmpFolder
operator|.
name|exists
argument_list|()
condition|)
block|{
comment|// Clean up the tmp folder
for|for
control|(
name|File
name|f
range|:
name|tmpFolder
operator|.
name|listFiles
argument_list|()
control|)
block|{
name|f
operator|.
name|delete
argument_list|()
expr_stmt|;
block|}
block|}
name|String
name|className
init|=
literal|"CheckingLibJarName"
decl_stmt|;
name|String
name|folder
init|=
name|TEST_UTIL
operator|.
name|getDataTestDir
argument_list|()
operator|.
name|toString
argument_list|()
decl_stmt|;
name|File
name|innerJarFile
init|=
name|ClassLoaderTestHelper
operator|.
name|buildJar
argument_list|(
name|folder
argument_list|,
name|className
argument_list|,
literal|null
argument_list|,
name|ClassLoaderTestHelper
operator|.
name|localDirPath
argument_list|(
name|conf
argument_list|)
argument_list|)
decl_stmt|;
name|File
name|targetJarFile
init|=
operator|new
name|File
argument_list|(
name|innerJarFile
operator|.
name|getParent
argument_list|()
argument_list|,
name|jarName
argument_list|)
decl_stmt|;
name|ClassLoaderTestHelper
operator|.
name|addJarFilesToJar
argument_list|(
name|targetJarFile
argument_list|,
name|libPrefix
argument_list|,
name|innerJarFile
argument_list|)
expr_stmt|;
name|Path
name|path
init|=
operator|new
name|Path
argument_list|(
name|targetJarFile
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
decl_stmt|;
name|ClassLoader
name|parent
init|=
name|TestCoprocessorClassLoader
operator|.
name|class
operator|.
name|getClassLoader
argument_list|()
decl_stmt|;
name|ClassLoader
name|classLoader
init|=
name|CoprocessorClassLoader
operator|.
name|getClassLoader
argument_list|(
name|path
argument_list|,
name|parent
argument_list|,
literal|"112"
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
literal|"Classloader should be created"
argument_list|,
name|classLoader
argument_list|)
expr_stmt|;
name|String
name|fileToLookFor
init|=
literal|"."
operator|+
name|className
operator|+
literal|".jar"
decl_stmt|;
for|for
control|(
name|String
name|f
range|:
name|tmpFolder
operator|.
name|list
argument_list|()
control|)
block|{
if|if
condition|(
name|f
operator|.
name|endsWith
argument_list|(
name|fileToLookFor
argument_list|)
operator|&&
name|f
operator|.
name|contains
argument_list|(
name|jarName
argument_list|)
condition|)
block|{
comment|// Cool, found it;
return|return;
block|}
block|}
name|fail
argument_list|(
literal|"Could not find the expected lib jar file"
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

