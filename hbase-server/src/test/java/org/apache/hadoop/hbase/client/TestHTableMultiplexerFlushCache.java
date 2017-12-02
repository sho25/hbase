begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|client
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
name|HRegionLocation
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
name|ServerName
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
name|Waiter
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
name|HRegionServer
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
name|ClientTests
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
name|Pair
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

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|LargeTests
operator|.
name|class
block|,
name|ClientTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestHTableMultiplexerFlushCache
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
name|TestHTableMultiplexerFlushCache
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
name|byte
index|[]
name|FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testFamily"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|QUALIFIER1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testQualifier_1"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|QUALIFIER2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testQualifier_2"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|VALUE1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testValue1"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|VALUE2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testValue2"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|int
name|SLAVES
init|=
literal|3
decl_stmt|;
specifier|private
specifier|static
name|int
name|PER_REGIONSERVER_QUEUE_SIZE
init|=
literal|100000
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
comment|/**    * @throws java.lang.Exception    */
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
name|SLAVES
argument_list|)
expr_stmt|;
block|}
comment|/**    * @throws java.lang.Exception    */
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
specifier|private
specifier|static
name|void
name|checkExistence
parameter_list|(
specifier|final
name|Table
name|htable
parameter_list|,
specifier|final
name|byte
index|[]
name|row
parameter_list|,
specifier|final
name|byte
index|[]
name|family
parameter_list|,
specifier|final
name|byte
index|[]
name|quality
parameter_list|,
specifier|final
name|byte
index|[]
name|value
parameter_list|)
throws|throws
name|Exception
block|{
comment|// verify that the Get returns the correct result
name|TEST_UTIL
operator|.
name|waitFor
argument_list|(
literal|30000
argument_list|,
operator|new
name|Waiter
operator|.
name|Predicate
argument_list|<
name|Exception
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|evaluate
parameter_list|()
throws|throws
name|Exception
block|{
name|Result
name|r
decl_stmt|;
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|get
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|quality
argument_list|)
expr_stmt|;
name|r
operator|=
name|htable
operator|.
name|get
argument_list|(
name|get
argument_list|)
expr_stmt|;
return|return
name|r
operator|!=
literal|null
operator|&&
name|r
operator|.
name|getValue
argument_list|(
name|family
argument_list|,
name|quality
argument_list|)
operator|!=
literal|null
operator|&&
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|value
argument_list|)
operator|.
name|equals
argument_list|(
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|r
operator|.
name|getValue
argument_list|(
name|family
argument_list|,
name|quality
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testOnRegionChange
parameter_list|()
throws|throws
name|Exception
block|{
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
name|int
name|NUM_REGIONS
init|=
literal|10
decl_stmt|;
name|Table
name|htable
init|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|tableName
argument_list|,
operator|new
name|byte
index|[]
index|[]
block|{
name|FAMILY
block|}
argument_list|,
literal|3
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"aaaaa"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"zzzzz"
argument_list|)
argument_list|,
name|NUM_REGIONS
argument_list|)
decl_stmt|;
name|HTableMultiplexer
name|multiplexer
init|=
operator|new
name|HTableMultiplexer
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|PER_REGIONSERVER_QUEUE_SIZE
argument_list|)
decl_stmt|;
try|try
init|(
name|RegionLocator
name|r
init|=
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getRegionLocator
argument_list|(
name|tableName
argument_list|)
init|)
block|{
name|byte
index|[]
index|[]
name|startRows
init|=
name|r
operator|.
name|getStartKeys
argument_list|()
decl_stmt|;
name|byte
index|[]
name|row
init|=
name|startRows
index|[
literal|1
index|]
decl_stmt|;
name|assertTrue
argument_list|(
literal|"2nd region should not start with empty row"
argument_list|,
name|row
operator|!=
literal|null
operator|&&
name|row
operator|.
name|length
operator|>
literal|0
argument_list|)
expr_stmt|;
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|row
argument_list|)
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|QUALIFIER1
argument_list|,
name|VALUE1
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"multiplexer.put returns"
argument_list|,
name|multiplexer
operator|.
name|put
argument_list|(
name|tableName
argument_list|,
name|put
argument_list|)
argument_list|)
expr_stmt|;
name|checkExistence
argument_list|(
name|htable
argument_list|,
name|row
argument_list|,
name|FAMILY
argument_list|,
name|QUALIFIER1
argument_list|,
name|VALUE1
argument_list|)
expr_stmt|;
comment|// Now let's shutdown the regionserver and let regions moved to other servers.
name|HRegionLocation
name|loc
init|=
name|r
operator|.
name|getRegionLocation
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|MiniHBaseCluster
name|hbaseCluster
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
decl_stmt|;
name|hbaseCluster
operator|.
name|stopRegionServer
argument_list|(
name|loc
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|waitUntilAllRegionsAssigned
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
comment|// put with multiplexer.
name|put
operator|=
operator|new
name|Put
argument_list|(
name|row
argument_list|)
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|QUALIFIER2
argument_list|,
name|VALUE2
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"multiplexer.put returns"
argument_list|,
name|multiplexer
operator|.
name|put
argument_list|(
name|tableName
argument_list|,
name|put
argument_list|)
argument_list|)
expr_stmt|;
name|checkExistence
argument_list|(
name|htable
argument_list|,
name|row
argument_list|,
name|FAMILY
argument_list|,
name|QUALIFIER2
argument_list|,
name|VALUE2
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testOnRegionMove
parameter_list|()
throws|throws
name|Exception
block|{
comment|// This test is doing near exactly the same thing that testOnRegionChange but avoiding the
comment|// potential to get a ConnectionClosingException. By moving the region, we can be certain that
comment|// the connection is still valid and that the implementation is correctly handling an invalid
comment|// Region cache (and not just tearing down the entire connection).
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
name|int
name|NUM_REGIONS
init|=
literal|10
decl_stmt|;
name|Table
name|htable
init|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|tableName
argument_list|,
operator|new
name|byte
index|[]
index|[]
block|{
name|FAMILY
block|}
argument_list|,
literal|3
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"aaaaa"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"zzzzz"
argument_list|)
argument_list|,
name|NUM_REGIONS
argument_list|)
decl_stmt|;
name|HTableMultiplexer
name|multiplexer
init|=
operator|new
name|HTableMultiplexer
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|PER_REGIONSERVER_QUEUE_SIZE
argument_list|)
decl_stmt|;
specifier|final
name|RegionLocator
name|regionLocator
init|=
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getRegionLocator
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|Pair
argument_list|<
name|byte
index|[]
index|[]
argument_list|,
name|byte
index|[]
index|[]
argument_list|>
name|startEndRows
init|=
name|regionLocator
operator|.
name|getStartEndKeys
argument_list|()
decl_stmt|;
name|byte
index|[]
name|row
init|=
name|startEndRows
operator|.
name|getFirst
argument_list|()
index|[
literal|1
index|]
decl_stmt|;
name|assertTrue
argument_list|(
literal|"2nd region should not start with empty row"
argument_list|,
name|row
operator|!=
literal|null
operator|&&
name|row
operator|.
name|length
operator|>
literal|0
argument_list|)
expr_stmt|;
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|row
argument_list|)
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|QUALIFIER1
argument_list|,
name|VALUE1
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"multiplexer.put returns"
argument_list|,
name|multiplexer
operator|.
name|put
argument_list|(
name|tableName
argument_list|,
name|put
argument_list|)
argument_list|)
expr_stmt|;
name|checkExistence
argument_list|(
name|htable
argument_list|,
name|row
argument_list|,
name|FAMILY
argument_list|,
name|QUALIFIER1
argument_list|,
name|VALUE1
argument_list|)
expr_stmt|;
specifier|final
name|HRegionLocation
name|loc
init|=
name|regionLocator
operator|.
name|getRegionLocation
argument_list|(
name|row
argument_list|)
decl_stmt|;
specifier|final
name|MiniHBaseCluster
name|hbaseCluster
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
decl_stmt|;
comment|// The current server for the region we're writing to
specifier|final
name|ServerName
name|originalServer
init|=
name|loc
operator|.
name|getServerName
argument_list|()
decl_stmt|;
name|ServerName
name|newServer
init|=
literal|null
decl_stmt|;
comment|// Find a new server to move that region to
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|SLAVES
condition|;
name|i
operator|++
control|)
block|{
name|HRegionServer
name|rs
init|=
name|hbaseCluster
operator|.
name|getRegionServer
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|rs
operator|.
name|getServerName
argument_list|()
operator|.
name|equals
argument_list|(
name|originalServer
operator|.
name|getServerName
argument_list|()
argument_list|)
condition|)
block|{
name|newServer
operator|=
name|rs
operator|.
name|getServerName
argument_list|()
expr_stmt|;
break|break;
block|}
block|}
name|assertNotNull
argument_list|(
literal|"Did not find a new RegionServer to use"
argument_list|,
name|newServer
argument_list|)
expr_stmt|;
comment|// Move the region
name|LOG
operator|.
name|info
argument_list|(
literal|"Moving "
operator|+
name|loc
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getEncodedName
argument_list|()
operator|+
literal|" from "
operator|+
name|originalServer
operator|+
literal|" to "
operator|+
name|newServer
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|move
argument_list|(
name|loc
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|newServer
operator|.
name|getServerName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|waitUntilAllRegionsAssigned
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
comment|// Send a new Put
name|put
operator|=
operator|new
name|Put
argument_list|(
name|row
argument_list|)
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|QUALIFIER2
argument_list|,
name|VALUE2
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"multiplexer.put returns"
argument_list|,
name|multiplexer
operator|.
name|put
argument_list|(
name|tableName
argument_list|,
name|put
argument_list|)
argument_list|)
expr_stmt|;
comment|// We should see the update make it to the new server eventually
name|checkExistence
argument_list|(
name|htable
argument_list|,
name|row
argument_list|,
name|FAMILY
argument_list|,
name|QUALIFIER2
argument_list|,
name|VALUE2
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

