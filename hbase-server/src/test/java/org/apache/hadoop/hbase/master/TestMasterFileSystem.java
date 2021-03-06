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
name|master
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
name|client
operator|.
name|Table
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
name|MediumTests
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
name|Bytes
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
name|FSUtils
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
name|HFileArchiveTestingUtil
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
name|Rule
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
name|junit
operator|.
name|rules
operator|.
name|TestName
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
comment|/**  * Test the master filesystem in a local cluster  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MasterTests
operator|.
name|class
block|,
name|MediumTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestMasterFileSystem
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
name|TestMasterFileSystem
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Rule
specifier|public
name|TestName
name|name
init|=
operator|new
name|TestName
argument_list|()
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
name|TestMasterFileSystem
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setupTest
parameter_list|()
throws|throws
name|Exception
block|{
name|UTIL
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|teardownTest
parameter_list|()
throws|throws
name|Exception
block|{
name|UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFsUriSetProperly
parameter_list|()
throws|throws
name|Exception
block|{
name|HMaster
name|master
init|=
name|UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
decl_stmt|;
name|MasterFileSystem
name|fs
init|=
name|master
operator|.
name|getMasterFileSystem
argument_list|()
decl_stmt|;
name|Path
name|masterRoot
init|=
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|fs
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|Path
name|rootDir
init|=
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|fs
operator|.
name|getFileSystem
argument_list|()
operator|.
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
comment|// make sure the fs and the found root dir have the same scheme
name|LOG
operator|.
name|debug
argument_list|(
literal|"from fs uri:"
operator|+
name|FileSystem
operator|.
name|getDefaultUri
argument_list|(
name|fs
operator|.
name|getFileSystem
argument_list|()
operator|.
name|getConf
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"from configuration uri:"
operator|+
name|FileSystem
operator|.
name|getDefaultUri
argument_list|(
name|fs
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// make sure the set uri matches by forcing it.
name|assertEquals
argument_list|(
name|masterRoot
argument_list|,
name|rootDir
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCheckTempDir
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|MasterFileSystem
name|masterFileSystem
init|=
name|UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getMasterFileSystem
argument_list|()
decl_stmt|;
specifier|final
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|FAM
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"fam"
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
index|[]
name|splitKeys
init|=
operator|new
name|byte
index|[]
index|[]
block|{
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"b"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"c"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"d"
argument_list|)
block|}
decl_stmt|;
name|UTIL
operator|.
name|createTable
argument_list|(
name|tableName
argument_list|,
name|FAM
argument_list|,
name|splitKeys
argument_list|)
expr_stmt|;
comment|// get the current store files for the regions
name|List
argument_list|<
name|HRegion
argument_list|>
name|regions
init|=
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getRegions
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
comment|// make sure we have 4 regions serving this table
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|regions
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// load the table
try|try
init|(
name|Table
name|table
init|=
name|UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
init|)
block|{
name|UTIL
operator|.
name|loadTable
argument_list|(
name|table
argument_list|,
name|FAM
argument_list|)
expr_stmt|;
block|}
comment|// disable the table so that we can manipulate the files
name|UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|disableTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
specifier|final
name|Path
name|tableDir
init|=
name|FSUtils
operator|.
name|getTableDir
argument_list|(
name|masterFileSystem
operator|.
name|getRootDir
argument_list|()
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
specifier|final
name|Path
name|tempDir
init|=
name|masterFileSystem
operator|.
name|getTempDir
argument_list|()
decl_stmt|;
specifier|final
name|Path
name|tempTableDir
init|=
name|FSUtils
operator|.
name|getTableDir
argument_list|(
name|tempDir
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
specifier|final
name|FileSystem
name|fs
init|=
name|masterFileSystem
operator|.
name|getFileSystem
argument_list|()
decl_stmt|;
comment|// move the table to the temporary directory
if|if
condition|(
operator|!
name|fs
operator|.
name|rename
argument_list|(
name|tableDir
argument_list|,
name|tempTableDir
argument_list|)
condition|)
block|{
name|fail
argument_list|()
expr_stmt|;
block|}
name|masterFileSystem
operator|.
name|checkTempDir
argument_list|(
name|tempDir
argument_list|,
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|fs
argument_list|)
expr_stmt|;
comment|// check if the temporary directory exists and is empty
name|assertTrue
argument_list|(
name|fs
operator|.
name|exists
argument_list|(
name|tempDir
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|fs
operator|.
name|listStatus
argument_list|(
name|tempDir
argument_list|)
operator|.
name|length
argument_list|)
expr_stmt|;
comment|// check for the existence of the archive directory
for|for
control|(
name|HRegion
name|region
range|:
name|regions
control|)
block|{
name|Path
name|archiveDir
init|=
name|HFileArchiveTestingUtil
operator|.
name|getRegionArchiveDir
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|region
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|fs
operator|.
name|exists
argument_list|(
name|archiveDir
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|UTIL
operator|.
name|deleteTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

