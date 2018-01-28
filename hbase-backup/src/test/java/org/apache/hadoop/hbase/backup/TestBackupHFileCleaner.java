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
name|backup
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
name|assertTrue
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
name|HashMap
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
name|Map
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
name|FileStatus
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
name|TableName
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
name|backup
operator|.
name|impl
operator|.
name|BackupSystemTable
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
name|testclassification
operator|.
name|MasterTests
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
name|After
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
name|Before
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
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

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MasterTests
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
name|TestBackupHFileCleaner
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
name|TestBackupHFileCleaner
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
name|TestBackupHFileCleaner
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
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"backup.hfile.cleaner"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|String
name|famName
init|=
literal|"fam"
decl_stmt|;
specifier|static
name|FileSystem
name|fs
init|=
literal|null
decl_stmt|;
name|Path
name|root
decl_stmt|;
comment|/**    * @throws Exception if starting the mini cluster or getting the filesystem fails    */
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
name|conf
operator|.
name|setBoolean
argument_list|(
name|BackupRestoreConstants
operator|.
name|BACKUP_ENABLE_KEY
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniZKCluster
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|fs
operator|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
comment|/**    * @throws Exception if closing the filesystem or shutting down the mini cluster fails    */
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
if|if
condition|(
name|fs
operator|!=
literal|null
condition|)
block|{
name|fs
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|setup
parameter_list|()
throws|throws
name|IOException
block|{
name|root
operator|=
name|TEST_UTIL
operator|.
name|getDataTestDirOnTestFS
argument_list|()
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|cleanup
parameter_list|()
block|{
try|try
block|{
name|fs
operator|.
name|delete
argument_list|(
name|root
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to delete files recursively from path "
operator|+
name|root
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetDeletableFiles
parameter_list|()
throws|throws
name|IOException
block|{
comment|// 1. Create a file
name|Path
name|file
init|=
operator|new
name|Path
argument_list|(
name|root
argument_list|,
literal|"testIsFileDeletableWithNoHFileRefs"
argument_list|)
decl_stmt|;
name|fs
operator|.
name|createNewFile
argument_list|(
name|file
argument_list|)
expr_stmt|;
comment|// 2. Assert file is successfully created
name|assertTrue
argument_list|(
literal|"Test file not created!"
argument_list|,
name|fs
operator|.
name|exists
argument_list|(
name|file
argument_list|)
argument_list|)
expr_stmt|;
name|BackupHFileCleaner
name|cleaner
init|=
operator|new
name|BackupHFileCleaner
argument_list|()
decl_stmt|;
name|cleaner
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|cleaner
operator|.
name|setCheckForFullyBackedUpTables
argument_list|(
literal|false
argument_list|)
expr_stmt|;
comment|// 3. Assert that file as is should be deletable
name|List
argument_list|<
name|FileStatus
argument_list|>
name|stats
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|FileStatus
name|stat
init|=
name|fs
operator|.
name|getFileStatus
argument_list|(
name|file
argument_list|)
decl_stmt|;
name|stats
operator|.
name|add
argument_list|(
name|stat
argument_list|)
expr_stmt|;
name|Iterable
argument_list|<
name|FileStatus
argument_list|>
name|deletable
init|=
name|cleaner
operator|.
name|getDeletableFiles
argument_list|(
name|stats
argument_list|)
decl_stmt|;
name|deletable
operator|=
name|cleaner
operator|.
name|getDeletableFiles
argument_list|(
name|stats
argument_list|)
expr_stmt|;
name|boolean
name|found
init|=
literal|false
decl_stmt|;
for|for
control|(
name|FileStatus
name|stat1
range|:
name|deletable
control|)
block|{
if|if
condition|(
name|stat
operator|.
name|equals
argument_list|(
name|stat1
argument_list|)
condition|)
block|{
name|found
operator|=
literal|true
expr_stmt|;
block|}
block|}
name|assertTrue
argument_list|(
literal|"Cleaner should allow to delete this file as there is no hfile reference "
operator|+
literal|"for it."
argument_list|,
name|found
argument_list|)
expr_stmt|;
comment|// 4. Add the file as bulk load
name|List
argument_list|<
name|Path
argument_list|>
name|list
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|list
operator|.
name|add
argument_list|(
name|file
argument_list|)
expr_stmt|;
try|try
init|(
name|Connection
name|conn
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf
argument_list|)
init|;
name|BackupSystemTable
name|sysTbl
operator|=
operator|new
name|BackupSystemTable
argument_list|(
name|conn
argument_list|)
init|)
block|{
name|List
argument_list|<
name|TableName
argument_list|>
name|sTableList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|sTableList
operator|.
name|add
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|List
argument_list|<
name|Path
argument_list|>
argument_list|>
index|[]
name|maps
init|=
operator|new
name|Map
index|[
literal|1
index|]
decl_stmt|;
name|maps
index|[
literal|0
index|]
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
name|maps
index|[
literal|0
index|]
operator|.
name|put
argument_list|(
name|famName
operator|.
name|getBytes
argument_list|()
argument_list|,
name|list
argument_list|)
expr_stmt|;
name|sysTbl
operator|.
name|writeBulkLoadedFiles
argument_list|(
name|sTableList
argument_list|,
name|maps
argument_list|,
literal|"1"
argument_list|)
expr_stmt|;
block|}
comment|// 5. Assert file should not be deletable
name|deletable
operator|=
name|cleaner
operator|.
name|getDeletableFiles
argument_list|(
name|stats
argument_list|)
expr_stmt|;
name|deletable
operator|=
name|cleaner
operator|.
name|getDeletableFiles
argument_list|(
name|stats
argument_list|)
expr_stmt|;
name|found
operator|=
literal|false
expr_stmt|;
for|for
control|(
name|FileStatus
name|stat1
range|:
name|deletable
control|)
block|{
if|if
condition|(
name|stat
operator|.
name|equals
argument_list|(
name|stat1
argument_list|)
condition|)
block|{
name|found
operator|=
literal|true
expr_stmt|;
block|}
block|}
name|assertFalse
argument_list|(
literal|"Cleaner should not allow to delete this file as there is a hfile reference "
operator|+
literal|"for it."
argument_list|,
name|found
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

