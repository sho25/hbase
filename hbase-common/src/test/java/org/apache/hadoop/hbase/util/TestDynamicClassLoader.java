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
name|HBaseClassTestRule
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
name|Before
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|ClassRule
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

begin_comment
comment|/**  * Test TestDynamicClassLoader  */
end_comment

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
name|TestDynamicClassLoader
block|{
annotation|@
name|ClassRule
specifier|public
specifier|static
specifier|final
name|HBaseClassTestRule
name|CLASS_RULE
init|=
name|HBaseClassTestRule
operator|.
name|forClass
argument_list|(
name|TestDynamicClassLoader
operator|.
name|class
argument_list|)
decl_stmt|;
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
name|TestDynamicClassLoader
operator|.
name|class
argument_list|)
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
specifier|private
name|Configuration
name|conf
decl_stmt|;
static|static
block|{
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|set
argument_list|(
literal|"hbase.dynamic.jars.dir"
argument_list|,
name|TEST_UTIL
operator|.
name|getDataTestDir
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|initializeConfiguration
parameter_list|()
block|{
name|conf
operator|=
operator|new
name|Configuration
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testLoadClassFromLocalPath
parameter_list|()
throws|throws
name|Exception
block|{
name|ClassLoader
name|parent
init|=
name|TestDynamicClassLoader
operator|.
name|class
operator|.
name|getClassLoader
argument_list|()
decl_stmt|;
name|DynamicClassLoader
name|classLoader
init|=
operator|new
name|DynamicClassLoader
argument_list|(
name|conf
argument_list|,
name|parent
argument_list|)
decl_stmt|;
name|String
name|className
init|=
literal|"TestLoadClassFromLocalPath"
decl_stmt|;
name|deleteClass
argument_list|(
name|className
argument_list|)
expr_stmt|;
try|try
block|{
name|classLoader
operator|.
name|loadClass
argument_list|(
name|className
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Should not be able to load class "
operator|+
name|className
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|cnfe
parameter_list|)
block|{
comment|// expected, move on
block|}
try|try
block|{
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
expr_stmt|;
name|classLoader
operator|.
name|loadClass
argument_list|(
name|className
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|cnfe
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Should be able to load class "
operator|+
name|className
argument_list|,
name|cnfe
argument_list|)
expr_stmt|;
name|fail
argument_list|(
name|cnfe
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testLoadClassFromAnotherPath
parameter_list|()
throws|throws
name|Exception
block|{
name|ClassLoader
name|parent
init|=
name|TestDynamicClassLoader
operator|.
name|class
operator|.
name|getClassLoader
argument_list|()
decl_stmt|;
name|DynamicClassLoader
name|classLoader
init|=
operator|new
name|DynamicClassLoader
argument_list|(
name|conf
argument_list|,
name|parent
argument_list|)
decl_stmt|;
name|String
name|className
init|=
literal|"TestLoadClassFromAnotherPath"
decl_stmt|;
name|deleteClass
argument_list|(
name|className
argument_list|)
expr_stmt|;
try|try
block|{
name|classLoader
operator|.
name|loadClass
argument_list|(
name|className
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Should not be able to load class "
operator|+
name|className
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|cnfe
parameter_list|)
block|{
comment|// expected, move on
block|}
try|try
block|{
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
name|ClassLoaderTestHelper
operator|.
name|buildJar
argument_list|(
name|folder
argument_list|,
name|className
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|classLoader
operator|.
name|loadClass
argument_list|(
name|className
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|cnfe
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Should be able to load class "
operator|+
name|className
argument_list|,
name|cnfe
argument_list|)
expr_stmt|;
name|fail
argument_list|(
name|cnfe
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testLoadClassFromLocalPathWithDynamicDirOff
parameter_list|()
throws|throws
name|Exception
block|{
name|conf
operator|.
name|setBoolean
argument_list|(
literal|"hbase.use.dynamic.jars"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|ClassLoader
name|parent
init|=
name|TestDynamicClassLoader
operator|.
name|class
operator|.
name|getClassLoader
argument_list|()
decl_stmt|;
name|DynamicClassLoader
name|classLoader
init|=
operator|new
name|DynamicClassLoader
argument_list|(
name|conf
argument_list|,
name|parent
argument_list|)
decl_stmt|;
name|String
name|className
init|=
literal|"TestLoadClassFromLocalPath"
decl_stmt|;
name|deleteClass
argument_list|(
name|className
argument_list|)
expr_stmt|;
try|try
block|{
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
expr_stmt|;
name|classLoader
operator|.
name|loadClass
argument_list|(
name|className
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Should not be able to load class "
operator|+
name|className
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|cnfe
parameter_list|)
block|{
comment|// expected, move on
block|}
block|}
specifier|private
name|void
name|deleteClass
parameter_list|(
name|String
name|className
parameter_list|)
throws|throws
name|Exception
block|{
name|String
name|jarFileName
init|=
name|className
operator|+
literal|".jar"
decl_stmt|;
name|File
name|file
init|=
operator|new
name|File
argument_list|(
name|TEST_UTIL
operator|.
name|getDataTestDir
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
name|jarFileName
argument_list|)
decl_stmt|;
name|file
operator|.
name|delete
argument_list|()
expr_stmt|;
name|assertFalse
argument_list|(
literal|"Should be deleted: "
operator|+
name|file
operator|.
name|getPath
argument_list|()
argument_list|,
name|file
operator|.
name|exists
argument_list|()
argument_list|)
expr_stmt|;
name|file
operator|=
operator|new
name|File
argument_list|(
name|conf
operator|.
name|get
argument_list|(
literal|"hbase.dynamic.jars.dir"
argument_list|)
argument_list|,
name|jarFileName
argument_list|)
expr_stmt|;
name|file
operator|.
name|delete
argument_list|()
expr_stmt|;
name|assertFalse
argument_list|(
literal|"Should be deleted: "
operator|+
name|file
operator|.
name|getPath
argument_list|()
argument_list|,
name|file
operator|.
name|exists
argument_list|()
argument_list|)
expr_stmt|;
name|file
operator|=
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
name|jarFileName
argument_list|)
expr_stmt|;
name|file
operator|.
name|delete
argument_list|()
expr_stmt|;
name|assertFalse
argument_list|(
literal|"Should be deleted: "
operator|+
name|file
operator|.
name|getPath
argument_list|()
argument_list|,
name|file
operator|.
name|exists
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

