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
name|coprocessor
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
name|*
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
name|Arrays
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
name|Cell
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
name|HRegionInfo
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
name|Delete
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
name|Mutation
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
name|Put
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
name|Result
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
name|ResultScanner
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
name|Scan
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
name|MiniBatchOperationInProgress
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
name|wal
operator|.
name|WALEdit
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
name|wal
operator|.
name|WALKey
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
name|MediumTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestRegionObserverForAddingMutationsFromCoprocessors
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
name|TestRegionObserverForAddingMutationsFromCoprocessors
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|HBaseTestingUtility
name|util
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|dummy
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"dummy"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|row1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"r1"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|row2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"r2"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|row3
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"r3"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|test
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"test"
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
name|TableName
name|tableName
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
name|CoprocessorHost
operator|.
name|WAL_COPROCESSOR_CONF_KEY
argument_list|,
name|TestWALObserver
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|util
operator|=
operator|new
name|HBaseTestingUtility
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|util
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
name|tearDownAfterClass
parameter_list|()
throws|throws
name|Exception
block|{
name|util
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|tableName
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
specifier|private
name|void
name|createTable
parameter_list|(
name|String
name|coprocessor
parameter_list|)
throws|throws
name|IOException
block|{
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
argument_list|)
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|dummy
argument_list|)
argument_list|)
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|test
argument_list|)
argument_list|)
operator|.
name|addCoprocessor
argument_list|(
name|coprocessor
argument_list|)
decl_stmt|;
name|util
operator|.
name|getAdmin
argument_list|()
operator|.
name|createTable
argument_list|(
name|htd
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test various multiput operations.    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testMulti
parameter_list|()
throws|throws
name|Exception
block|{
name|createTable
argument_list|(
name|TestMultiMutationCoprocessor
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
try|try
init|(
name|Table
name|t
init|=
name|util
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
name|t
operator|.
name|put
argument_list|(
operator|new
name|Put
argument_list|(
name|row1
argument_list|)
operator|.
name|addColumn
argument_list|(
name|test
argument_list|,
name|dummy
argument_list|,
name|dummy
argument_list|)
argument_list|)
expr_stmt|;
name|assertRowCount
argument_list|(
name|t
argument_list|,
literal|3
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Tests that added mutations from coprocessors end up in the WAL.    */
annotation|@
name|Test
specifier|public
name|void
name|testCPMutationsAreWrittenToWALEdit
parameter_list|()
throws|throws
name|Exception
block|{
name|createTable
argument_list|(
name|TestMultiMutationCoprocessor
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
try|try
init|(
name|Table
name|t
init|=
name|util
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
name|t
operator|.
name|put
argument_list|(
operator|new
name|Put
argument_list|(
name|row1
argument_list|)
operator|.
name|addColumn
argument_list|(
name|test
argument_list|,
name|dummy
argument_list|,
name|dummy
argument_list|)
argument_list|)
expr_stmt|;
name|assertRowCount
argument_list|(
name|t
argument_list|,
literal|3
argument_list|)
expr_stmt|;
block|}
name|assertNotNull
argument_list|(
name|TestWALObserver
operator|.
name|savedEdit
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|TestWALObserver
operator|.
name|savedEdit
operator|.
name|getCells
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|assertRowCount
parameter_list|(
name|Table
name|t
parameter_list|,
name|int
name|expected
parameter_list|)
throws|throws
name|IOException
block|{
try|try
init|(
name|ResultScanner
name|scanner
init|=
name|t
operator|.
name|getScanner
argument_list|(
operator|new
name|Scan
argument_list|()
argument_list|)
init|)
block|{
name|int
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Result
name|r
range|:
name|scanner
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
name|r
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|i
operator|++
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|i
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDeleteCell
parameter_list|()
throws|throws
name|Exception
block|{
name|createTable
argument_list|(
name|TestDeleteCellCoprocessor
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
try|try
init|(
name|Table
name|t
init|=
name|util
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
name|t
operator|.
name|put
argument_list|(
name|Lists
operator|.
name|newArrayList
argument_list|(
operator|new
name|Put
argument_list|(
name|row1
argument_list|)
operator|.
name|addColumn
argument_list|(
name|test
argument_list|,
name|dummy
argument_list|,
name|dummy
argument_list|)
argument_list|,
operator|new
name|Put
argument_list|(
name|row2
argument_list|)
operator|.
name|addColumn
argument_list|(
name|test
argument_list|,
name|dummy
argument_list|,
name|dummy
argument_list|)
argument_list|,
operator|new
name|Put
argument_list|(
name|row3
argument_list|)
operator|.
name|addColumn
argument_list|(
name|test
argument_list|,
name|dummy
argument_list|,
name|dummy
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertRowCount
argument_list|(
name|t
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|t
operator|.
name|delete
argument_list|(
operator|new
name|Delete
argument_list|(
name|test
argument_list|)
operator|.
name|addColumn
argument_list|(
name|test
argument_list|,
name|dummy
argument_list|)
argument_list|)
expr_stmt|;
comment|// delete non-existing row
name|assertRowCount
argument_list|(
name|t
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDeleteFamily
parameter_list|()
throws|throws
name|Exception
block|{
name|createTable
argument_list|(
name|TestDeleteFamilyCoprocessor
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
try|try
init|(
name|Table
name|t
init|=
name|util
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
name|t
operator|.
name|put
argument_list|(
name|Lists
operator|.
name|newArrayList
argument_list|(
operator|new
name|Put
argument_list|(
name|row1
argument_list|)
operator|.
name|addColumn
argument_list|(
name|test
argument_list|,
name|dummy
argument_list|,
name|dummy
argument_list|)
argument_list|,
operator|new
name|Put
argument_list|(
name|row2
argument_list|)
operator|.
name|addColumn
argument_list|(
name|test
argument_list|,
name|dummy
argument_list|,
name|dummy
argument_list|)
argument_list|,
operator|new
name|Put
argument_list|(
name|row3
argument_list|)
operator|.
name|addColumn
argument_list|(
name|test
argument_list|,
name|dummy
argument_list|,
name|dummy
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertRowCount
argument_list|(
name|t
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|t
operator|.
name|delete
argument_list|(
operator|new
name|Delete
argument_list|(
name|test
argument_list|)
operator|.
name|addFamily
argument_list|(
name|test
argument_list|)
argument_list|)
expr_stmt|;
comment|// delete non-existing row
name|assertRowCount
argument_list|(
name|t
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDeleteRow
parameter_list|()
throws|throws
name|Exception
block|{
name|createTable
argument_list|(
name|TestDeleteRowCoprocessor
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
try|try
init|(
name|Table
name|t
init|=
name|util
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
name|t
operator|.
name|put
argument_list|(
name|Lists
operator|.
name|newArrayList
argument_list|(
operator|new
name|Put
argument_list|(
name|row1
argument_list|)
operator|.
name|addColumn
argument_list|(
name|test
argument_list|,
name|dummy
argument_list|,
name|dummy
argument_list|)
argument_list|,
operator|new
name|Put
argument_list|(
name|row2
argument_list|)
operator|.
name|addColumn
argument_list|(
name|test
argument_list|,
name|dummy
argument_list|,
name|dummy
argument_list|)
argument_list|,
operator|new
name|Put
argument_list|(
name|row3
argument_list|)
operator|.
name|addColumn
argument_list|(
name|test
argument_list|,
name|dummy
argument_list|,
name|dummy
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertRowCount
argument_list|(
name|t
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|t
operator|.
name|delete
argument_list|(
operator|new
name|Delete
argument_list|(
name|test
argument_list|)
operator|.
name|addColumn
argument_list|(
name|test
argument_list|,
name|dummy
argument_list|)
argument_list|)
expr_stmt|;
comment|// delete non-existing row
name|assertRowCount
argument_list|(
name|t
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
class|class
name|TestMultiMutationCoprocessor
implements|implements
name|RegionObserver
block|{
annotation|@
name|Override
specifier|public
name|void
name|preBatchMutate
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|c
parameter_list|,
name|MiniBatchOperationInProgress
argument_list|<
name|Mutation
argument_list|>
name|miniBatchOp
parameter_list|)
throws|throws
name|IOException
block|{
name|Mutation
name|mut
init|=
name|miniBatchOp
operator|.
name|getOperation
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Cell
argument_list|>
name|cells
init|=
name|mut
operator|.
name|getFamilyCellMap
argument_list|()
operator|.
name|get
argument_list|(
name|test
argument_list|)
decl_stmt|;
name|Put
index|[]
name|puts
init|=
operator|new
name|Put
index|[]
block|{
operator|new
name|Put
argument_list|(
name|row1
argument_list|)
operator|.
name|addColumn
argument_list|(
name|test
argument_list|,
name|dummy
argument_list|,
name|cells
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getTimestamp
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cpdummy"
argument_list|)
argument_list|)
block|,
operator|new
name|Put
argument_list|(
name|row2
argument_list|)
operator|.
name|addColumn
argument_list|(
name|test
argument_list|,
name|dummy
argument_list|,
name|cells
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getTimestamp
argument_list|()
argument_list|,
name|dummy
argument_list|)
block|,
operator|new
name|Put
argument_list|(
name|row3
argument_list|)
operator|.
name|addColumn
argument_list|(
name|test
argument_list|,
name|dummy
argument_list|,
name|cells
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getTimestamp
argument_list|()
argument_list|,
name|dummy
argument_list|)
block|,       }
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Putting:"
operator|+
name|puts
argument_list|)
expr_stmt|;
name|miniBatchOp
operator|.
name|addOperationsFromCP
argument_list|(
literal|0
argument_list|,
name|puts
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
class|class
name|TestDeleteCellCoprocessor
implements|implements
name|RegionObserver
block|{
annotation|@
name|Override
specifier|public
name|void
name|preBatchMutate
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|c
parameter_list|,
name|MiniBatchOperationInProgress
argument_list|<
name|Mutation
argument_list|>
name|miniBatchOp
parameter_list|)
throws|throws
name|IOException
block|{
name|Mutation
name|mut
init|=
name|miniBatchOp
operator|.
name|getOperation
argument_list|(
literal|0
argument_list|)
decl_stmt|;
if|if
condition|(
name|mut
operator|instanceof
name|Delete
condition|)
block|{
name|List
argument_list|<
name|Cell
argument_list|>
name|cells
init|=
name|mut
operator|.
name|getFamilyCellMap
argument_list|()
operator|.
name|get
argument_list|(
name|test
argument_list|)
decl_stmt|;
name|Delete
index|[]
name|deletes
init|=
operator|new
name|Delete
index|[]
block|{
comment|// delete only 2 rows
operator|new
name|Delete
argument_list|(
name|row1
argument_list|)
operator|.
name|addColumns
argument_list|(
name|test
argument_list|,
name|dummy
argument_list|,
name|cells
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getTimestamp
argument_list|()
argument_list|)
block|,
operator|new
name|Delete
argument_list|(
name|row2
argument_list|)
operator|.
name|addColumns
argument_list|(
name|test
argument_list|,
name|dummy
argument_list|,
name|cells
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getTimestamp
argument_list|()
argument_list|)
block|,         }
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Deleting:"
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|deletes
argument_list|)
argument_list|)
expr_stmt|;
name|miniBatchOp
operator|.
name|addOperationsFromCP
argument_list|(
literal|0
argument_list|,
name|deletes
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
specifier|static
class|class
name|TestDeleteFamilyCoprocessor
implements|implements
name|RegionObserver
block|{
annotation|@
name|Override
specifier|public
name|void
name|preBatchMutate
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|c
parameter_list|,
name|MiniBatchOperationInProgress
argument_list|<
name|Mutation
argument_list|>
name|miniBatchOp
parameter_list|)
throws|throws
name|IOException
block|{
name|Mutation
name|mut
init|=
name|miniBatchOp
operator|.
name|getOperation
argument_list|(
literal|0
argument_list|)
decl_stmt|;
if|if
condition|(
name|mut
operator|instanceof
name|Delete
condition|)
block|{
name|List
argument_list|<
name|Cell
argument_list|>
name|cells
init|=
name|mut
operator|.
name|getFamilyCellMap
argument_list|()
operator|.
name|get
argument_list|(
name|test
argument_list|)
decl_stmt|;
name|Delete
index|[]
name|deletes
init|=
operator|new
name|Delete
index|[]
block|{
comment|// delete only 2 rows
operator|new
name|Delete
argument_list|(
name|row1
argument_list|)
operator|.
name|addFamily
argument_list|(
name|test
argument_list|,
name|cells
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getTimestamp
argument_list|()
argument_list|)
block|,
operator|new
name|Delete
argument_list|(
name|row2
argument_list|)
operator|.
name|addFamily
argument_list|(
name|test
argument_list|,
name|cells
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getTimestamp
argument_list|()
argument_list|)
block|,         }
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Deleting:"
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|deletes
argument_list|)
argument_list|)
expr_stmt|;
name|miniBatchOp
operator|.
name|addOperationsFromCP
argument_list|(
literal|0
argument_list|,
name|deletes
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
specifier|static
class|class
name|TestDeleteRowCoprocessor
implements|implements
name|RegionObserver
block|{
annotation|@
name|Override
specifier|public
name|void
name|preBatchMutate
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|c
parameter_list|,
name|MiniBatchOperationInProgress
argument_list|<
name|Mutation
argument_list|>
name|miniBatchOp
parameter_list|)
throws|throws
name|IOException
block|{
name|Mutation
name|mut
init|=
name|miniBatchOp
operator|.
name|getOperation
argument_list|(
literal|0
argument_list|)
decl_stmt|;
if|if
condition|(
name|mut
operator|instanceof
name|Delete
condition|)
block|{
name|List
argument_list|<
name|Cell
argument_list|>
name|cells
init|=
name|mut
operator|.
name|getFamilyCellMap
argument_list|()
operator|.
name|get
argument_list|(
name|test
argument_list|)
decl_stmt|;
name|Delete
index|[]
name|deletes
init|=
operator|new
name|Delete
index|[]
block|{
comment|// delete only 2 rows
operator|new
name|Delete
argument_list|(
name|row1
argument_list|,
name|cells
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getTimestamp
argument_list|()
argument_list|)
block|,
operator|new
name|Delete
argument_list|(
name|row2
argument_list|,
name|cells
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getTimestamp
argument_list|()
argument_list|)
block|,         }
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Deleting:"
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|deletes
argument_list|)
argument_list|)
expr_stmt|;
name|miniBatchOp
operator|.
name|addOperationsFromCP
argument_list|(
literal|0
argument_list|,
name|deletes
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
specifier|static
class|class
name|TestWALObserver
implements|implements
name|WALObserver
block|{
specifier|static
name|WALEdit
name|savedEdit
init|=
literal|null
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|postWALWrite
parameter_list|(
name|ObserverContext
argument_list|<
name|?
extends|extends
name|WALCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
name|HRegionInfo
name|info
parameter_list|,
name|WALKey
name|logKey
parameter_list|,
name|WALEdit
name|logEdit
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|info
operator|.
name|getTable
argument_list|()
operator|.
name|equals
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testCPMutationsAreWrittenToWALEdit"
argument_list|)
argument_list|)
condition|)
block|{
name|savedEdit
operator|=
name|logEdit
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

