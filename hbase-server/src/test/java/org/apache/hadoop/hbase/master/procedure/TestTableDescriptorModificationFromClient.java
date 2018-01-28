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
operator|.
name|procedure
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
name|Set
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
name|InvalidFamilyOperationException
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
name|master
operator|.
name|MasterFileSystem
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
name|LargeTests
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
name|FSTableDescriptors
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
name|Assert
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

begin_comment
comment|/**  * Verify that the HTableDescriptor is updated after  * addColumn(), deleteColumn() and modifyTable() operations.  */
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
name|LargeTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestTableDescriptorModificationFromClient
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
name|TestTableDescriptorModificationFromClient
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
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|TableName
name|TABLE_NAME
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|FAMILY_0
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cf0"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|FAMILY_1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cf1"
argument_list|)
decl_stmt|;
comment|/**    * Start up a mini cluster and put a small table of empty regions into it.    *    * @throws Exception    */
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|beforeAllTests
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
block|}
annotation|@
name|Before
specifier|public
name|void
name|setup
parameter_list|()
block|{
name|TABLE_NAME
operator|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|afterAllTests
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
annotation|@
name|Test
specifier|public
name|void
name|testModifyTable
parameter_list|()
throws|throws
name|IOException
block|{
name|Admin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
comment|// Create a table with one family
name|HTableDescriptor
name|baseHtd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TABLE_NAME
argument_list|)
decl_stmt|;
name|baseHtd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|FAMILY_0
argument_list|)
argument_list|)
expr_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|baseHtd
argument_list|)
expr_stmt|;
name|admin
operator|.
name|disableTable
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
try|try
block|{
comment|// Verify the table descriptor
name|verifyTableDescriptor
argument_list|(
name|TABLE_NAME
argument_list|,
name|FAMILY_0
argument_list|)
expr_stmt|;
comment|// Modify the table adding another family and verify the descriptor
name|HTableDescriptor
name|modifiedHtd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TABLE_NAME
argument_list|)
decl_stmt|;
name|modifiedHtd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|FAMILY_0
argument_list|)
argument_list|)
expr_stmt|;
name|modifiedHtd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|FAMILY_1
argument_list|)
argument_list|)
expr_stmt|;
name|admin
operator|.
name|modifyTable
argument_list|(
name|TABLE_NAME
argument_list|,
name|modifiedHtd
argument_list|)
expr_stmt|;
name|verifyTableDescriptor
argument_list|(
name|TABLE_NAME
argument_list|,
name|FAMILY_0
argument_list|,
name|FAMILY_1
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|admin
operator|.
name|deleteTable
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testAddColumn
parameter_list|()
throws|throws
name|IOException
block|{
name|Admin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
comment|// Create a table with two families
name|HTableDescriptor
name|baseHtd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TABLE_NAME
argument_list|)
decl_stmt|;
name|baseHtd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|FAMILY_0
argument_list|)
argument_list|)
expr_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|baseHtd
argument_list|)
expr_stmt|;
name|admin
operator|.
name|disableTable
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
try|try
block|{
comment|// Verify the table descriptor
name|verifyTableDescriptor
argument_list|(
name|TABLE_NAME
argument_list|,
name|FAMILY_0
argument_list|)
expr_stmt|;
comment|// Modify the table removing one family and verify the descriptor
name|admin
operator|.
name|addColumnFamily
argument_list|(
name|TABLE_NAME
argument_list|,
operator|new
name|HColumnDescriptor
argument_list|(
name|FAMILY_1
argument_list|)
argument_list|)
expr_stmt|;
name|verifyTableDescriptor
argument_list|(
name|TABLE_NAME
argument_list|,
name|FAMILY_0
argument_list|,
name|FAMILY_1
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|admin
operator|.
name|deleteTable
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testAddSameColumnFamilyTwice
parameter_list|()
throws|throws
name|IOException
block|{
name|Admin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
comment|// Create a table with one families
name|HTableDescriptor
name|baseHtd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TABLE_NAME
argument_list|)
decl_stmt|;
name|baseHtd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|FAMILY_0
argument_list|)
argument_list|)
expr_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|baseHtd
argument_list|)
expr_stmt|;
name|admin
operator|.
name|disableTable
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
try|try
block|{
comment|// Verify the table descriptor
name|verifyTableDescriptor
argument_list|(
name|TABLE_NAME
argument_list|,
name|FAMILY_0
argument_list|)
expr_stmt|;
comment|// Modify the table removing one family and verify the descriptor
name|admin
operator|.
name|addColumnFamily
argument_list|(
name|TABLE_NAME
argument_list|,
operator|new
name|HColumnDescriptor
argument_list|(
name|FAMILY_1
argument_list|)
argument_list|)
expr_stmt|;
name|verifyTableDescriptor
argument_list|(
name|TABLE_NAME
argument_list|,
name|FAMILY_0
argument_list|,
name|FAMILY_1
argument_list|)
expr_stmt|;
try|try
block|{
comment|// Add same column family again - expect failure
name|admin
operator|.
name|addColumnFamily
argument_list|(
name|TABLE_NAME
argument_list|,
operator|new
name|HColumnDescriptor
argument_list|(
name|FAMILY_1
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|fail
argument_list|(
literal|"Delete a non-exist column family should fail"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InvalidFamilyOperationException
name|e
parameter_list|)
block|{
comment|// Expected.
block|}
block|}
finally|finally
block|{
name|admin
operator|.
name|deleteTable
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testModifyColumnFamily
parameter_list|()
throws|throws
name|IOException
block|{
name|Admin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
name|HColumnDescriptor
name|cfDescriptor
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|FAMILY_0
argument_list|)
decl_stmt|;
name|int
name|blockSize
init|=
name|cfDescriptor
operator|.
name|getBlocksize
argument_list|()
decl_stmt|;
comment|// Create a table with one families
name|HTableDescriptor
name|baseHtd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TABLE_NAME
argument_list|)
decl_stmt|;
name|baseHtd
operator|.
name|addFamily
argument_list|(
name|cfDescriptor
argument_list|)
expr_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|baseHtd
argument_list|)
expr_stmt|;
name|admin
operator|.
name|disableTable
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
try|try
block|{
comment|// Verify the table descriptor
name|verifyTableDescriptor
argument_list|(
name|TABLE_NAME
argument_list|,
name|FAMILY_0
argument_list|)
expr_stmt|;
name|int
name|newBlockSize
init|=
literal|2
operator|*
name|blockSize
decl_stmt|;
name|cfDescriptor
operator|.
name|setBlocksize
argument_list|(
name|newBlockSize
argument_list|)
expr_stmt|;
comment|// Modify colymn family
name|admin
operator|.
name|modifyColumnFamily
argument_list|(
name|TABLE_NAME
argument_list|,
name|cfDescriptor
argument_list|)
expr_stmt|;
name|HTableDescriptor
name|htd
init|=
name|admin
operator|.
name|getTableDescriptor
argument_list|(
name|TABLE_NAME
argument_list|)
decl_stmt|;
name|HColumnDescriptor
name|hcfd
init|=
name|htd
operator|.
name|getFamily
argument_list|(
name|FAMILY_0
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|hcfd
operator|.
name|getBlocksize
argument_list|()
operator|==
name|newBlockSize
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|admin
operator|.
name|deleteTable
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testModifyNonExistingColumnFamily
parameter_list|()
throws|throws
name|IOException
block|{
name|Admin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
name|HColumnDescriptor
name|cfDescriptor
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|FAMILY_1
argument_list|)
decl_stmt|;
name|int
name|blockSize
init|=
name|cfDescriptor
operator|.
name|getBlocksize
argument_list|()
decl_stmt|;
comment|// Create a table with one families
name|HTableDescriptor
name|baseHtd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TABLE_NAME
argument_list|)
decl_stmt|;
name|baseHtd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|FAMILY_0
argument_list|)
argument_list|)
expr_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|baseHtd
argument_list|)
expr_stmt|;
name|admin
operator|.
name|disableTable
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
try|try
block|{
comment|// Verify the table descriptor
name|verifyTableDescriptor
argument_list|(
name|TABLE_NAME
argument_list|,
name|FAMILY_0
argument_list|)
expr_stmt|;
name|int
name|newBlockSize
init|=
literal|2
operator|*
name|blockSize
decl_stmt|;
name|cfDescriptor
operator|.
name|setBlocksize
argument_list|(
name|newBlockSize
argument_list|)
expr_stmt|;
comment|// Modify a column family that is not in the table.
try|try
block|{
name|admin
operator|.
name|modifyColumnFamily
argument_list|(
name|TABLE_NAME
argument_list|,
name|cfDescriptor
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|fail
argument_list|(
literal|"Modify a non-exist column family should fail"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InvalidFamilyOperationException
name|e
parameter_list|)
block|{
comment|// Expected.
block|}
block|}
finally|finally
block|{
name|admin
operator|.
name|deleteTable
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDeleteColumn
parameter_list|()
throws|throws
name|IOException
block|{
name|Admin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
comment|// Create a table with two families
name|HTableDescriptor
name|baseHtd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TABLE_NAME
argument_list|)
decl_stmt|;
name|baseHtd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|FAMILY_0
argument_list|)
argument_list|)
expr_stmt|;
name|baseHtd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|FAMILY_1
argument_list|)
argument_list|)
expr_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|baseHtd
argument_list|)
expr_stmt|;
name|admin
operator|.
name|disableTable
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
try|try
block|{
comment|// Verify the table descriptor
name|verifyTableDescriptor
argument_list|(
name|TABLE_NAME
argument_list|,
name|FAMILY_0
argument_list|,
name|FAMILY_1
argument_list|)
expr_stmt|;
comment|// Modify the table removing one family and verify the descriptor
name|admin
operator|.
name|deleteColumnFamily
argument_list|(
name|TABLE_NAME
argument_list|,
name|FAMILY_1
argument_list|)
expr_stmt|;
name|verifyTableDescriptor
argument_list|(
name|TABLE_NAME
argument_list|,
name|FAMILY_0
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|admin
operator|.
name|deleteTable
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDeleteSameColumnFamilyTwice
parameter_list|()
throws|throws
name|IOException
block|{
name|Admin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
comment|// Create a table with two families
name|HTableDescriptor
name|baseHtd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TABLE_NAME
argument_list|)
decl_stmt|;
name|baseHtd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|FAMILY_0
argument_list|)
argument_list|)
expr_stmt|;
name|baseHtd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|FAMILY_1
argument_list|)
argument_list|)
expr_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|baseHtd
argument_list|)
expr_stmt|;
name|admin
operator|.
name|disableTable
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
try|try
block|{
comment|// Verify the table descriptor
name|verifyTableDescriptor
argument_list|(
name|TABLE_NAME
argument_list|,
name|FAMILY_0
argument_list|,
name|FAMILY_1
argument_list|)
expr_stmt|;
comment|// Modify the table removing one family and verify the descriptor
name|admin
operator|.
name|deleteColumnFamily
argument_list|(
name|TABLE_NAME
argument_list|,
name|FAMILY_1
argument_list|)
expr_stmt|;
name|verifyTableDescriptor
argument_list|(
name|TABLE_NAME
argument_list|,
name|FAMILY_0
argument_list|)
expr_stmt|;
try|try
block|{
comment|// Delete again - expect failure
name|admin
operator|.
name|deleteColumnFamily
argument_list|(
name|TABLE_NAME
argument_list|,
name|FAMILY_1
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|fail
argument_list|(
literal|"Delete a non-exist column family should fail"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// Expected.
block|}
block|}
finally|finally
block|{
name|admin
operator|.
name|deleteTable
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|verifyTableDescriptor
parameter_list|(
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|byte
index|[]
modifier|...
name|families
parameter_list|)
throws|throws
name|IOException
block|{
name|Admin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
comment|// Verify descriptor from master
name|HTableDescriptor
name|htd
init|=
name|admin
operator|.
name|getTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|verifyTableDescriptor
argument_list|(
name|htd
argument_list|,
name|tableName
argument_list|,
name|families
argument_list|)
expr_stmt|;
comment|// Verify descriptor from HDFS
name|MasterFileSystem
name|mfs
init|=
name|TEST_UTIL
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
name|Path
name|tableDir
init|=
name|FSUtils
operator|.
name|getTableDir
argument_list|(
name|mfs
operator|.
name|getRootDir
argument_list|()
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|TableDescriptor
name|td
init|=
name|FSTableDescriptors
operator|.
name|getTableDescriptorFromFs
argument_list|(
name|mfs
operator|.
name|getFileSystem
argument_list|()
argument_list|,
name|tableDir
argument_list|)
decl_stmt|;
name|verifyTableDescriptor
argument_list|(
name|td
argument_list|,
name|tableName
argument_list|,
name|families
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|verifyTableDescriptor
parameter_list|(
specifier|final
name|TableDescriptor
name|htd
parameter_list|,
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|byte
index|[]
modifier|...
name|families
parameter_list|)
block|{
name|Set
argument_list|<
name|byte
index|[]
argument_list|>
name|htdFamilies
init|=
name|htd
operator|.
name|getColumnFamilyNames
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|tableName
argument_list|,
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|families
operator|.
name|length
argument_list|,
name|htdFamilies
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|byte
index|[]
name|familyName
range|:
name|families
control|)
block|{
name|assertTrue
argument_list|(
literal|"Expected family "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|familyName
argument_list|)
argument_list|,
name|htdFamilies
operator|.
name|contains
argument_list|(
name|familyName
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

