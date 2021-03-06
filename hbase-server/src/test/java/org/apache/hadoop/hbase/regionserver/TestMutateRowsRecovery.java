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
name|regionserver
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|HBaseTestingUtility
operator|.
name|fam1
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
name|ColumnFamilyDescriptorBuilder
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
name|Durability
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
name|Get
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
name|RowMutations
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
name|client
operator|.
name|TableDescriptorBuilder
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
name|testclassification
operator|.
name|RegionServerTests
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
name|EnvironmentEdgeManager
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

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|RegionServerTests
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
name|TestMutateRowsRecovery
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
name|TestMutateRowsRecovery
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|MiniHBaseCluster
name|cluster
init|=
literal|null
decl_stmt|;
specifier|private
name|Connection
name|connection
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|NB_SERVERS
init|=
literal|3
decl_stmt|;
specifier|static
specifier|final
name|byte
index|[]
name|qual1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qual1"
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|byte
index|[]
name|qual2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qual2"
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|byte
index|[]
name|value1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value1"
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|byte
index|[]
name|value2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value2"
argument_list|)
decl_stmt|;
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
literal|"rowA"
argument_list|)
decl_stmt|;
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
literal|"rowB"
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|HBaseTestingUtility
name|TESTING_UTIL
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
name|before
parameter_list|()
throws|throws
name|Exception
block|{
name|TESTING_UTIL
operator|.
name|startMiniCluster
argument_list|(
name|NB_SERVERS
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|after
parameter_list|()
throws|throws
name|Exception
block|{
name|TESTING_UTIL
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
name|TESTING_UTIL
operator|.
name|ensureSomeNonStoppedRegionServersAvailable
argument_list|(
name|NB_SERVERS
argument_list|)
expr_stmt|;
name|this
operator|.
name|connection
operator|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|TESTING_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|cluster
operator|=
name|TESTING_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|this
operator|.
name|connection
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|connection
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|MutateRowsAndCheckPostKill
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
specifier|final
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
name|Admin
name|admin
init|=
literal|null
decl_stmt|;
name|Table
name|hTable
init|=
literal|null
decl_stmt|;
try|try
block|{
name|admin
operator|=
name|connection
operator|.
name|getAdmin
argument_list|()
expr_stmt|;
name|hTable
operator|=
name|connection
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|TableDescriptorBuilder
operator|.
name|ModifyableTableDescriptor
name|tableDescriptor
init|=
operator|new
name|TableDescriptorBuilder
operator|.
name|ModifyableTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|tableDescriptor
operator|.
name|setColumnFamily
argument_list|(
operator|new
name|ColumnFamilyDescriptorBuilder
operator|.
name|ModifyableColumnFamilyDescriptor
argument_list|(
name|fam1
argument_list|)
argument_list|)
expr_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|tableDescriptor
argument_list|)
expr_stmt|;
comment|// Add a multi
name|RowMutations
name|rm
init|=
operator|new
name|RowMutations
argument_list|(
name|row1
argument_list|)
decl_stmt|;
name|Put
name|p1
init|=
operator|new
name|Put
argument_list|(
name|row1
argument_list|)
decl_stmt|;
name|p1
operator|.
name|addColumn
argument_list|(
name|fam1
argument_list|,
name|qual1
argument_list|,
name|value1
argument_list|)
expr_stmt|;
name|p1
operator|.
name|setDurability
argument_list|(
name|Durability
operator|.
name|SYNC_WAL
argument_list|)
expr_stmt|;
name|rm
operator|.
name|add
argument_list|(
name|p1
argument_list|)
expr_stmt|;
name|hTable
operator|.
name|mutateRow
argument_list|(
name|rm
argument_list|)
expr_stmt|;
comment|// Add a put
name|Put
name|p2
init|=
operator|new
name|Put
argument_list|(
name|row1
argument_list|)
decl_stmt|;
name|p2
operator|.
name|addColumn
argument_list|(
name|fam1
argument_list|,
name|qual2
argument_list|,
name|value2
argument_list|)
expr_stmt|;
name|p2
operator|.
name|setDurability
argument_list|(
name|Durability
operator|.
name|SYNC_WAL
argument_list|)
expr_stmt|;
name|hTable
operator|.
name|put
argument_list|(
name|p2
argument_list|)
expr_stmt|;
name|HRegionServer
name|rs1
init|=
name|TESTING_UTIL
operator|.
name|getRSForFirstRegionInTable
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|long
name|now
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
decl_stmt|;
comment|// Send the RS Load to ensure correct lastflushedseqid for stores
name|rs1
operator|.
name|tryRegionServerReport
argument_list|(
name|now
operator|-
literal|30000
argument_list|,
name|now
argument_list|)
expr_stmt|;
comment|// Kill the RS to trigger wal replay
name|cluster
operator|.
name|killRegionServer
argument_list|(
name|rs1
operator|.
name|serverName
argument_list|)
expr_stmt|;
comment|// Ensure correct data exists
name|Get
name|g1
init|=
operator|new
name|Get
argument_list|(
name|row1
argument_list|)
decl_stmt|;
name|Result
name|result
init|=
name|hTable
operator|.
name|get
argument_list|(
name|g1
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|result
operator|.
name|getValue
argument_list|(
name|fam1
argument_list|,
name|qual1
argument_list|)
operator|!=
literal|null
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|Bytes
operator|.
name|compareTo
argument_list|(
name|result
operator|.
name|getValue
argument_list|(
name|fam1
argument_list|,
name|qual1
argument_list|)
argument_list|,
name|value1
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|result
operator|.
name|getValue
argument_list|(
name|fam1
argument_list|,
name|qual2
argument_list|)
operator|!=
literal|null
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|Bytes
operator|.
name|compareTo
argument_list|(
name|result
operator|.
name|getValue
argument_list|(
name|fam1
argument_list|,
name|qual2
argument_list|)
argument_list|,
name|value2
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|admin
operator|!=
literal|null
condition|)
block|{
name|admin
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|hTable
operator|!=
literal|null
condition|)
block|{
name|hTable
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

