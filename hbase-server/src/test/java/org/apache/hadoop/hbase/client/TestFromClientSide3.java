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
name|assertNull
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
name|ArrayList
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
name|java
operator|.
name|util
operator|.
name|Random
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
name|protobuf
operator|.
name|ProtobufUtil
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
name|protobuf
operator|.
name|generated
operator|.
name|AdminProtos
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
name|LargeTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestFromClientSide3
block|{
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|getClass
argument_list|()
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
name|Random
name|random
init|=
operator|new
name|Random
argument_list|()
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
name|byte
index|[]
name|ROW
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testRow"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|ANOTHERROW
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"anotherrow"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|QUALIFIER
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testQualifier"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|VALUE
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testValue"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|byte
index|[]
name|COL_QUAL
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f1"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|byte
index|[]
name|VAL_BYTES
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"v1"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|byte
index|[]
name|ROW_BYTES
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"r1"
argument_list|)
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
name|getConfiguration
argument_list|()
operator|.
name|setBoolean
argument_list|(
literal|"hbase.online.schema.update.enable"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
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
comment|/**    * @throws java.lang.Exception    */
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Nothing to do.
block|}
comment|/**    * @throws java.lang.Exception    */
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Nothing to do.
block|}
specifier|private
name|void
name|randomCFPuts
parameter_list|(
name|HTable
name|table
parameter_list|,
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|int
name|nPuts
parameter_list|)
throws|throws
name|Exception
block|{
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|row
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
name|nPuts
condition|;
name|i
operator|++
control|)
block|{
name|byte
index|[]
name|qualifier
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|random
operator|.
name|nextInt
argument_list|()
argument_list|)
decl_stmt|;
name|byte
index|[]
name|value
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|random
operator|.
name|nextInt
argument_list|()
argument_list|)
decl_stmt|;
name|put
operator|.
name|add
argument_list|(
name|family
argument_list|,
name|qualifier
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|performMultiplePutAndFlush
parameter_list|(
name|HBaseAdmin
name|admin
parameter_list|,
name|HTable
name|table
parameter_list|,
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|int
name|nFlushes
parameter_list|,
name|int
name|nPuts
parameter_list|)
throws|throws
name|Exception
block|{
comment|// connection needed for poll-wait
name|HConnection
name|conn
init|=
name|HConnectionManager
operator|.
name|getConnection
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|HRegionLocation
name|loc
init|=
name|table
operator|.
name|getRegionLocation
argument_list|(
name|row
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|AdminProtos
operator|.
name|AdminService
operator|.
name|BlockingInterface
name|server
init|=
name|conn
operator|.
name|getAdmin
argument_list|(
name|loc
operator|.
name|getServerName
argument_list|()
argument_list|)
decl_stmt|;
name|byte
index|[]
name|regName
init|=
name|loc
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionName
argument_list|()
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
name|nFlushes
condition|;
name|i
operator|++
control|)
block|{
name|randomCFPuts
argument_list|(
name|table
argument_list|,
name|row
argument_list|,
name|family
argument_list|,
name|nPuts
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|sf
init|=
name|ProtobufUtil
operator|.
name|getStoreFiles
argument_list|(
name|server
argument_list|,
name|regName
argument_list|,
name|FAMILY
argument_list|)
decl_stmt|;
name|int
name|sfCount
init|=
name|sf
operator|.
name|size
argument_list|()
decl_stmt|;
comment|// TODO: replace this api with a synchronous flush after HBASE-2949
name|admin
operator|.
name|flush
argument_list|(
name|table
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
comment|// synchronously poll wait for a new storefile to appear (flush happened)
while|while
condition|(
name|ProtobufUtil
operator|.
name|getStoreFiles
argument_list|(
name|server
argument_list|,
name|regName
argument_list|,
name|FAMILY
argument_list|)
operator|.
name|size
argument_list|()
operator|==
name|sfCount
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|40
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|// override the config settings at the CF level and ensure priority
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testAdvancedConfigOverride
parameter_list|()
throws|throws
name|Exception
block|{
comment|/*      * Overall idea: (1) create 3 store files and issue a compaction. config's      * compaction.min == 3, so should work. (2) Increase the compaction.min      * toggle in the HTD to 5 and modify table. If we use the HTD value instead      * of the default config value, adding 3 files and issuing a compaction      * SHOULD NOT work (3) Decrease the compaction.min toggle in the HCD to 2      * and modify table. The CF schema should override the Table schema and now      * cause a minor compaction.      */
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"hbase.hstore.compaction.min"
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|String
name|tableName
init|=
literal|"testAdvancedConfigOverride"
decl_stmt|;
name|TableName
name|TABLE
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|HTable
name|hTable
init|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|TABLE
argument_list|,
name|FAMILY
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|HBaseAdmin
name|admin
init|=
operator|new
name|HBaseAdmin
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|HConnection
name|connection
init|=
name|HConnectionManager
operator|.
name|getConnection
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
comment|// Create 3 store files.
name|byte
index|[]
name|row
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|random
operator|.
name|nextInt
argument_list|()
argument_list|)
decl_stmt|;
name|performMultiplePutAndFlush
argument_list|(
name|admin
argument_list|,
name|hTable
argument_list|,
name|row
argument_list|,
name|FAMILY
argument_list|,
literal|3
argument_list|,
literal|100
argument_list|)
expr_stmt|;
comment|// Verify we have multiple store files.
name|HRegionLocation
name|loc
init|=
name|hTable
operator|.
name|getRegionLocation
argument_list|(
name|row
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|byte
index|[]
name|regionName
init|=
name|loc
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionName
argument_list|()
decl_stmt|;
name|AdminProtos
operator|.
name|AdminService
operator|.
name|BlockingInterface
name|server
init|=
name|connection
operator|.
name|getAdmin
argument_list|(
name|loc
operator|.
name|getServerName
argument_list|()
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|ProtobufUtil
operator|.
name|getStoreFiles
argument_list|(
name|server
argument_list|,
name|regionName
argument_list|,
name|FAMILY
argument_list|)
operator|.
name|size
argument_list|()
operator|>
literal|1
argument_list|)
expr_stmt|;
comment|// Issue a compaction request
name|admin
operator|.
name|compact
argument_list|(
name|TABLE
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
comment|// poll wait for the compactions to happen
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|10
operator|*
literal|1000
operator|/
literal|40
condition|;
operator|++
name|i
control|)
block|{
comment|// The number of store files after compaction should be lesser.
name|loc
operator|=
name|hTable
operator|.
name|getRegionLocation
argument_list|(
name|row
argument_list|,
literal|true
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|loc
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|isOffline
argument_list|()
condition|)
block|{
name|regionName
operator|=
name|loc
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionName
argument_list|()
expr_stmt|;
name|server
operator|=
name|connection
operator|.
name|getAdmin
argument_list|(
name|loc
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|ProtobufUtil
operator|.
name|getStoreFiles
argument_list|(
name|server
argument_list|,
name|regionName
argument_list|,
name|FAMILY
argument_list|)
operator|.
name|size
argument_list|()
operator|<=
literal|1
condition|)
block|{
break|break;
block|}
block|}
name|Thread
operator|.
name|sleep
argument_list|(
literal|40
argument_list|)
expr_stmt|;
block|}
comment|// verify the compactions took place and that we didn't just time out
name|assertTrue
argument_list|(
name|ProtobufUtil
operator|.
name|getStoreFiles
argument_list|(
name|server
argument_list|,
name|regionName
argument_list|,
name|FAMILY
argument_list|)
operator|.
name|size
argument_list|()
operator|<=
literal|1
argument_list|)
expr_stmt|;
comment|// change the compaction.min config option for this table to 5
name|LOG
operator|.
name|info
argument_list|(
literal|"hbase.hstore.compaction.min should now be 5"
argument_list|)
expr_stmt|;
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|hTable
operator|.
name|getTableDescriptor
argument_list|()
argument_list|)
decl_stmt|;
name|htd
operator|.
name|setValue
argument_list|(
literal|"hbase.hstore.compaction.min"
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
literal|5
argument_list|)
argument_list|)
expr_stmt|;
name|admin
operator|.
name|modifyTable
argument_list|(
name|TABLE
argument_list|,
name|htd
argument_list|)
expr_stmt|;
name|Pair
argument_list|<
name|Integer
argument_list|,
name|Integer
argument_list|>
name|st
decl_stmt|;
while|while
condition|(
literal|null
operator|!=
operator|(
name|st
operator|=
name|admin
operator|.
name|getAlterStatus
argument_list|(
name|TABLE
argument_list|)
operator|)
operator|&&
name|st
operator|.
name|getFirst
argument_list|()
operator|>
literal|0
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|st
operator|.
name|getFirst
argument_list|()
operator|+
literal|" regions left to update"
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|40
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"alter status finished"
argument_list|)
expr_stmt|;
comment|// Create 3 more store files.
name|performMultiplePutAndFlush
argument_list|(
name|admin
argument_list|,
name|hTable
argument_list|,
name|row
argument_list|,
name|FAMILY
argument_list|,
literal|3
argument_list|,
literal|10
argument_list|)
expr_stmt|;
comment|// Issue a compaction request
name|admin
operator|.
name|compact
argument_list|(
name|TABLE
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
comment|// This time, the compaction request should not happen
name|Thread
operator|.
name|sleep
argument_list|(
literal|10
operator|*
literal|1000
argument_list|)
expr_stmt|;
name|loc
operator|=
name|hTable
operator|.
name|getRegionLocation
argument_list|(
name|row
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|regionName
operator|=
name|loc
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionName
argument_list|()
expr_stmt|;
name|server
operator|=
name|connection
operator|.
name|getAdmin
argument_list|(
name|loc
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|sfCount
init|=
name|ProtobufUtil
operator|.
name|getStoreFiles
argument_list|(
name|server
argument_list|,
name|regionName
argument_list|,
name|FAMILY
argument_list|)
operator|.
name|size
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|sfCount
operator|>
literal|1
argument_list|)
expr_stmt|;
comment|// change an individual CF's config option to 2& online schema update
name|LOG
operator|.
name|info
argument_list|(
literal|"hbase.hstore.compaction.min should now be 2"
argument_list|)
expr_stmt|;
name|HColumnDescriptor
name|hcd
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|htd
operator|.
name|getFamily
argument_list|(
name|FAMILY
argument_list|)
argument_list|)
decl_stmt|;
name|hcd
operator|.
name|setValue
argument_list|(
literal|"hbase.hstore.compaction.min"
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
name|hcd
argument_list|)
expr_stmt|;
name|admin
operator|.
name|modifyTable
argument_list|(
name|TABLE
argument_list|,
name|htd
argument_list|)
expr_stmt|;
while|while
condition|(
literal|null
operator|!=
operator|(
name|st
operator|=
name|admin
operator|.
name|getAlterStatus
argument_list|(
name|TABLE
argument_list|)
operator|)
operator|&&
name|st
operator|.
name|getFirst
argument_list|()
operator|>
literal|0
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|st
operator|.
name|getFirst
argument_list|()
operator|+
literal|" regions left to update"
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|40
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"alter status finished"
argument_list|)
expr_stmt|;
comment|// Issue a compaction request
name|admin
operator|.
name|compact
argument_list|(
name|TABLE
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
comment|// poll wait for the compactions to happen
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|10
operator|*
literal|1000
operator|/
literal|40
condition|;
operator|++
name|i
control|)
block|{
name|loc
operator|=
name|hTable
operator|.
name|getRegionLocation
argument_list|(
name|row
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|regionName
operator|=
name|loc
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionName
argument_list|()
expr_stmt|;
try|try
block|{
name|server
operator|=
name|connection
operator|.
name|getAdmin
argument_list|(
name|loc
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|ProtobufUtil
operator|.
name|getStoreFiles
argument_list|(
name|server
argument_list|,
name|regionName
argument_list|,
name|FAMILY
argument_list|)
operator|.
name|size
argument_list|()
operator|<
name|sfCount
condition|)
block|{
break|break;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Waiting for region to come online: "
operator|+
name|regionName
argument_list|)
expr_stmt|;
block|}
name|Thread
operator|.
name|sleep
argument_list|(
literal|40
argument_list|)
expr_stmt|;
block|}
comment|// verify the compaction took place and that we didn't just time out
name|assertTrue
argument_list|(
name|ProtobufUtil
operator|.
name|getStoreFiles
argument_list|(
name|server
argument_list|,
name|regionName
argument_list|,
name|FAMILY
argument_list|)
operator|.
name|size
argument_list|()
operator|<
name|sfCount
argument_list|)
expr_stmt|;
comment|// Finally, ensure that we can remove a custom config value after we made it
name|LOG
operator|.
name|info
argument_list|(
literal|"Removing CF config value"
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"hbase.hstore.compaction.min should now be 5"
argument_list|)
expr_stmt|;
name|hcd
operator|=
operator|new
name|HColumnDescriptor
argument_list|(
name|htd
operator|.
name|getFamily
argument_list|(
name|FAMILY
argument_list|)
argument_list|)
expr_stmt|;
name|hcd
operator|.
name|setValue
argument_list|(
literal|"hbase.hstore.compaction.min"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
name|hcd
argument_list|)
expr_stmt|;
name|admin
operator|.
name|modifyTable
argument_list|(
name|TABLE
argument_list|,
name|htd
argument_list|)
expr_stmt|;
while|while
condition|(
literal|null
operator|!=
operator|(
name|st
operator|=
name|admin
operator|.
name|getAlterStatus
argument_list|(
name|TABLE
argument_list|)
operator|)
operator|&&
name|st
operator|.
name|getFirst
argument_list|()
operator|>
literal|0
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|st
operator|.
name|getFirst
argument_list|()
operator|+
literal|" regions left to update"
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|40
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"alter status finished"
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|hTable
operator|.
name|getTableDescriptor
argument_list|()
operator|.
name|getFamily
argument_list|(
name|FAMILY
argument_list|)
operator|.
name|getValue
argument_list|(
literal|"hbase.hstore.compaction.min"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testHTableExistsMethodSingleRegionSingleGet
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Test with a single region table.
name|HTable
name|table
init|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testHTableExistsMethodSingleRegionSingleGet"
argument_list|)
argument_list|,
operator|new
name|byte
index|[]
index|[]
block|{
name|FAMILY
block|}
argument_list|)
decl_stmt|;
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|ROW
argument_list|)
decl_stmt|;
name|put
operator|.
name|add
argument_list|(
name|FAMILY
argument_list|,
name|QUALIFIER
argument_list|,
name|VALUE
argument_list|)
expr_stmt|;
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
name|ROW
argument_list|)
decl_stmt|;
name|boolean
name|exist
init|=
name|table
operator|.
name|exists
argument_list|(
name|get
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|exist
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|exist
operator|=
name|table
operator|.
name|exists
argument_list|(
name|get
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|exist
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testHTableExistsMethodSingleRegionMultipleGets
parameter_list|()
throws|throws
name|Exception
block|{
name|HTable
name|table
init|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testHTableExistsMethodSingleRegionMultipleGets"
argument_list|)
argument_list|,
operator|new
name|byte
index|[]
index|[]
block|{
name|FAMILY
block|}
argument_list|)
decl_stmt|;
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|ROW
argument_list|)
decl_stmt|;
name|put
operator|.
name|add
argument_list|(
name|FAMILY
argument_list|,
name|QUALIFIER
argument_list|,
name|VALUE
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Get
argument_list|>
name|gets
init|=
operator|new
name|ArrayList
argument_list|<
name|Get
argument_list|>
argument_list|()
decl_stmt|;
name|gets
operator|.
name|add
argument_list|(
operator|new
name|Get
argument_list|(
name|ROW
argument_list|)
argument_list|)
expr_stmt|;
name|gets
operator|.
name|add
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|gets
operator|.
name|add
argument_list|(
operator|new
name|Get
argument_list|(
name|ANOTHERROW
argument_list|)
argument_list|)
expr_stmt|;
name|Boolean
index|[]
name|results
init|=
name|table
operator|.
name|exists
argument_list|(
name|gets
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|results
index|[
literal|0
index|]
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|results
index|[
literal|1
index|]
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|results
index|[
literal|2
index|]
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testHTableExistsMethodMultipleRegionsSingleGet
parameter_list|()
throws|throws
name|Exception
block|{
name|HTable
name|table
init|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testHTableExistsMethodMultipleRegionsSingleGet"
argument_list|)
argument_list|,
operator|new
name|byte
index|[]
index|[]
block|{
name|FAMILY
block|}
argument_list|,
literal|1
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|0x00
block|}
argument_list|,
operator|new
name|byte
index|[]
block|{
operator|(
name|byte
operator|)
literal|0xff
block|}
argument_list|,
literal|255
argument_list|)
decl_stmt|;
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|ROW
argument_list|)
decl_stmt|;
name|put
operator|.
name|add
argument_list|(
name|FAMILY
argument_list|,
name|QUALIFIER
argument_list|,
name|VALUE
argument_list|)
expr_stmt|;
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
name|ROW
argument_list|)
decl_stmt|;
name|boolean
name|exist
init|=
name|table
operator|.
name|exists
argument_list|(
name|get
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|exist
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|exist
operator|=
name|table
operator|.
name|exists
argument_list|(
name|get
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|exist
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testHTableExistsMethodMultipleRegionsMultipleGets
parameter_list|()
throws|throws
name|Exception
block|{
name|HTable
name|table
init|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testHTableExistsMethodMultipleRegionsMultipleGets"
argument_list|)
argument_list|,
operator|new
name|byte
index|[]
index|[]
block|{
name|FAMILY
block|}
argument_list|,
literal|1
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|0x00
block|}
argument_list|,
operator|new
name|byte
index|[]
block|{
operator|(
name|byte
operator|)
literal|0xff
block|}
argument_list|,
literal|255
argument_list|)
decl_stmt|;
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|ROW
argument_list|)
decl_stmt|;
name|put
operator|.
name|add
argument_list|(
name|FAMILY
argument_list|,
name|QUALIFIER
argument_list|,
name|VALUE
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Get
argument_list|>
name|gets
init|=
operator|new
name|ArrayList
argument_list|<
name|Get
argument_list|>
argument_list|()
decl_stmt|;
name|gets
operator|.
name|add
argument_list|(
operator|new
name|Get
argument_list|(
name|ANOTHERROW
argument_list|)
argument_list|)
expr_stmt|;
name|gets
operator|.
name|add
argument_list|(
operator|new
name|Get
argument_list|(
name|Bytes
operator|.
name|add
argument_list|(
name|ROW
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|0x00
block|}
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|gets
operator|.
name|add
argument_list|(
operator|new
name|Get
argument_list|(
name|ROW
argument_list|)
argument_list|)
expr_stmt|;
name|gets
operator|.
name|add
argument_list|(
operator|new
name|Get
argument_list|(
name|Bytes
operator|.
name|add
argument_list|(
name|ANOTHERROW
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|0x00
block|}
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Calling exists"
argument_list|)
expr_stmt|;
name|Boolean
index|[]
name|results
init|=
name|table
operator|.
name|exists
argument_list|(
name|gets
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|results
index|[
literal|0
index|]
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|results
index|[
literal|1
index|]
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|results
index|[
literal|2
index|]
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|results
index|[
literal|3
index|]
argument_list|,
literal|false
argument_list|)
expr_stmt|;
comment|// Test with the first region.
name|put
operator|=
operator|new
name|Put
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|0x00
block|}
argument_list|)
expr_stmt|;
name|put
operator|.
name|add
argument_list|(
name|FAMILY
argument_list|,
name|QUALIFIER
argument_list|,
name|VALUE
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|gets
operator|=
operator|new
name|ArrayList
argument_list|<
name|Get
argument_list|>
argument_list|()
expr_stmt|;
name|gets
operator|.
name|add
argument_list|(
operator|new
name|Get
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|0x00
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|gets
operator|.
name|add
argument_list|(
operator|new
name|Get
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|0x00
block|,
literal|0x00
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|results
operator|=
name|table
operator|.
name|exists
argument_list|(
name|gets
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|results
index|[
literal|0
index|]
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|results
index|[
literal|1
index|]
argument_list|,
literal|false
argument_list|)
expr_stmt|;
comment|// Test with the last region
name|put
operator|=
operator|new
name|Put
argument_list|(
operator|new
name|byte
index|[]
block|{
operator|(
name|byte
operator|)
literal|0xff
block|,
operator|(
name|byte
operator|)
literal|0xff
block|}
argument_list|)
expr_stmt|;
name|put
operator|.
name|add
argument_list|(
name|FAMILY
argument_list|,
name|QUALIFIER
argument_list|,
name|VALUE
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|gets
operator|=
operator|new
name|ArrayList
argument_list|<
name|Get
argument_list|>
argument_list|()
expr_stmt|;
name|gets
operator|.
name|add
argument_list|(
operator|new
name|Get
argument_list|(
operator|new
name|byte
index|[]
block|{
operator|(
name|byte
operator|)
literal|0xff
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|gets
operator|.
name|add
argument_list|(
operator|new
name|Get
argument_list|(
operator|new
name|byte
index|[]
block|{
operator|(
name|byte
operator|)
literal|0xff
block|,
operator|(
name|byte
operator|)
literal|0xff
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|gets
operator|.
name|add
argument_list|(
operator|new
name|Get
argument_list|(
operator|new
name|byte
index|[]
block|{
operator|(
name|byte
operator|)
literal|0xff
block|,
operator|(
name|byte
operator|)
literal|0xff
block|,
operator|(
name|byte
operator|)
literal|0xff
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|results
operator|=
name|table
operator|.
name|exists
argument_list|(
name|gets
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|results
index|[
literal|0
index|]
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|results
index|[
literal|1
index|]
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|results
index|[
literal|2
index|]
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetEmptyRow
parameter_list|()
throws|throws
name|Exception
block|{
comment|//Create a table and put in 1 row
name|HBaseAdmin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
decl_stmt|;
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"test"
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|FAMILY
argument_list|)
argument_list|)
expr_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|desc
argument_list|)
expr_stmt|;
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
literal|"test"
argument_list|)
decl_stmt|;
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|ROW_BYTES
argument_list|)
decl_stmt|;
name|put
operator|.
name|add
argument_list|(
name|FAMILY
argument_list|,
name|COL_QUAL
argument_list|,
name|VAL_BYTES
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|table
operator|.
name|flushCommits
argument_list|()
expr_stmt|;
comment|//Try getting the row with an empty row key
name|Result
name|res
init|=
literal|null
decl_stmt|;
try|try
block|{
name|res
operator|=
name|table
operator|.
name|get
argument_list|(
operator|new
name|Get
argument_list|(
operator|new
name|byte
index|[
literal|0
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
comment|// Expected.
block|}
name|assertTrue
argument_list|(
name|res
operator|==
literal|null
argument_list|)
expr_stmt|;
name|res
operator|=
name|table
operator|.
name|get
argument_list|(
operator|new
name|Get
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"r1-not-exist"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|res
operator|.
name|isEmpty
argument_list|()
operator|==
literal|true
argument_list|)
expr_stmt|;
name|res
operator|=
name|table
operator|.
name|get
argument_list|(
operator|new
name|Get
argument_list|(
name|ROW_BYTES
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Arrays
operator|.
name|equals
argument_list|(
name|res
operator|.
name|getValue
argument_list|(
name|FAMILY
argument_list|,
name|COL_QUAL
argument_list|)
argument_list|,
name|VAL_BYTES
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

