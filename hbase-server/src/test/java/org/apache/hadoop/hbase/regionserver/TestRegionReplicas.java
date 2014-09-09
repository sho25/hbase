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
name|regionserver
operator|.
name|TestRegionServerNoMaster
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
name|Random
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ExecutorService
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|Executors
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeUnit
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicBoolean
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicReference
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
name|HConstants
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
name|TestMetaTableAccessor
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
name|Consistency
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
name|HTable
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
name|RequestConverter
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
name|ClientProtos
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
name|Threads
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
name|util
operator|.
name|StringUtils
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

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ServiceException
import|;
end_import

begin_comment
comment|/**  * Tests for region replicas. Sad that we cannot isolate these without bringing up a whole  * cluster. See {@link TestRegionServerNoMaster}.  */
end_comment

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
name|TestRegionReplicas
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
name|TestRegionReplicas
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|NB_SERVERS
init|=
literal|1
decl_stmt|;
specifier|private
specifier|static
name|HTable
name|table
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|row
init|=
literal|"TestRegionReplicas"
operator|.
name|getBytes
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|HRegionInfo
name|hriPrimary
decl_stmt|;
specifier|private
specifier|static
name|HRegionInfo
name|hriSecondary
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|HTU
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|f
init|=
name|HConstants
operator|.
name|CATALOG_FAMILY
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
name|HTU
operator|.
name|startMiniCluster
argument_list|(
name|NB_SERVERS
argument_list|)
expr_stmt|;
specifier|final
name|byte
index|[]
name|tableName
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|TestRegionReplicas
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|)
decl_stmt|;
comment|// Create table then get the single region for our new table.
name|table
operator|=
name|HTU
operator|.
name|createTable
argument_list|(
name|tableName
argument_list|,
name|f
argument_list|)
expr_stmt|;
name|hriPrimary
operator|=
name|table
operator|.
name|getRegionLocation
argument_list|(
name|row
argument_list|,
literal|false
argument_list|)
operator|.
name|getRegionInfo
argument_list|()
expr_stmt|;
comment|// mock a secondary region info to open
name|hriSecondary
operator|=
operator|new
name|HRegionInfo
argument_list|(
name|hriPrimary
operator|.
name|getTable
argument_list|()
argument_list|,
name|hriPrimary
operator|.
name|getStartKey
argument_list|()
argument_list|,
name|hriPrimary
operator|.
name|getEndKey
argument_list|()
argument_list|,
name|hriPrimary
operator|.
name|isSplit
argument_list|()
argument_list|,
name|hriPrimary
operator|.
name|getRegionId
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
comment|// No master
name|TestRegionServerNoMaster
operator|.
name|stopMasterAndAssignMeta
argument_list|(
name|HTU
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|afterClass
parameter_list|()
throws|throws
name|Exception
block|{
name|HRegionServer
operator|.
name|TEST_SKIP_REPORTING_TRANSITION
operator|=
literal|false
expr_stmt|;
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
name|HTU
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
specifier|private
name|HRegionServer
name|getRS
parameter_list|()
block|{
return|return
name|HTU
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getRegionServer
argument_list|(
literal|0
argument_list|)
return|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testOpenRegionReplica
parameter_list|()
throws|throws
name|Exception
block|{
name|openRegion
argument_list|(
name|HTU
argument_list|,
name|getRS
argument_list|()
argument_list|,
name|hriSecondary
argument_list|)
expr_stmt|;
try|try
block|{
comment|//load some data to primary
name|HTU
operator|.
name|loadNumericRows
argument_list|(
name|table
argument_list|,
name|f
argument_list|,
literal|0
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
comment|// assert that we can read back from primary
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1000
argument_list|,
name|HTU
operator|.
name|countRows
argument_list|(
name|table
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|HTU
operator|.
name|deleteNumericRows
argument_list|(
name|table
argument_list|,
name|f
argument_list|,
literal|0
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
name|closeRegion
argument_list|(
name|HTU
argument_list|,
name|getRS
argument_list|()
argument_list|,
name|hriSecondary
argument_list|)
expr_stmt|;
block|}
block|}
comment|/** Tests that the meta location is saved for secondary regions */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testRegionReplicaUpdatesMetaLocation
parameter_list|()
throws|throws
name|Exception
block|{
name|openRegion
argument_list|(
name|HTU
argument_list|,
name|getRS
argument_list|()
argument_list|,
name|hriSecondary
argument_list|)
expr_stmt|;
name|Table
name|meta
init|=
literal|null
decl_stmt|;
try|try
block|{
name|meta
operator|=
operator|new
name|HTable
argument_list|(
name|HTU
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|TableName
operator|.
name|META_TABLE_NAME
argument_list|)
expr_stmt|;
name|TestMetaTableAccessor
operator|.
name|assertMetaLocation
argument_list|(
name|meta
argument_list|,
name|hriPrimary
operator|.
name|getRegionName
argument_list|()
argument_list|,
name|getRS
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|,
operator|-
literal|1
argument_list|,
literal|1
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|meta
operator|!=
literal|null
condition|)
name|meta
operator|.
name|close
argument_list|()
expr_stmt|;
name|closeRegion
argument_list|(
name|HTU
argument_list|,
name|getRS
argument_list|()
argument_list|,
name|hriSecondary
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testRegionReplicaGets
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
comment|//load some data to primary
name|HTU
operator|.
name|loadNumericRows
argument_list|(
name|table
argument_list|,
name|f
argument_list|,
literal|0
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
comment|// assert that we can read back from primary
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1000
argument_list|,
name|HTU
operator|.
name|countRows
argument_list|(
name|table
argument_list|)
argument_list|)
expr_stmt|;
comment|// flush so that region replica can read
name|getRS
argument_list|()
operator|.
name|getRegionByEncodedName
argument_list|(
name|hriPrimary
operator|.
name|getEncodedName
argument_list|()
argument_list|)
operator|.
name|flushcache
argument_list|()
expr_stmt|;
name|openRegion
argument_list|(
name|HTU
argument_list|,
name|getRS
argument_list|()
argument_list|,
name|hriSecondary
argument_list|)
expr_stmt|;
comment|// first try directly against region
name|HRegion
name|region
init|=
name|getRS
argument_list|()
operator|.
name|getFromOnlineRegions
argument_list|(
name|hriSecondary
operator|.
name|getEncodedName
argument_list|()
argument_list|)
decl_stmt|;
name|assertGet
argument_list|(
name|region
argument_list|,
literal|42
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|assertGetRpc
argument_list|(
name|hriSecondary
argument_list|,
literal|42
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|HTU
operator|.
name|deleteNumericRows
argument_list|(
name|table
argument_list|,
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
literal|0
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
name|closeRegion
argument_list|(
name|HTU
argument_list|,
name|getRS
argument_list|()
argument_list|,
name|hriSecondary
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testGetOnTargetRegionReplica
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
comment|//load some data to primary
name|HTU
operator|.
name|loadNumericRows
argument_list|(
name|table
argument_list|,
name|f
argument_list|,
literal|0
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
comment|// assert that we can read back from primary
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1000
argument_list|,
name|HTU
operator|.
name|countRows
argument_list|(
name|table
argument_list|)
argument_list|)
expr_stmt|;
comment|// flush so that region replica can read
name|getRS
argument_list|()
operator|.
name|getRegionByEncodedName
argument_list|(
name|hriPrimary
operator|.
name|getEncodedName
argument_list|()
argument_list|)
operator|.
name|flushcache
argument_list|()
expr_stmt|;
name|openRegion
argument_list|(
name|HTU
argument_list|,
name|getRS
argument_list|()
argument_list|,
name|hriSecondary
argument_list|)
expr_stmt|;
comment|// try directly Get against region replica
name|byte
index|[]
name|row
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
literal|42
argument_list|)
argument_list|)
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
name|setConsistency
argument_list|(
name|Consistency
operator|.
name|TIMELINE
argument_list|)
expr_stmt|;
name|get
operator|.
name|setReplicaId
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|Result
name|result
init|=
name|table
operator|.
name|get
argument_list|(
name|get
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertArrayEquals
argument_list|(
name|row
argument_list|,
name|result
operator|.
name|getValue
argument_list|(
name|f
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|HTU
operator|.
name|deleteNumericRows
argument_list|(
name|table
argument_list|,
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
literal|0
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
name|closeRegion
argument_list|(
name|HTU
argument_list|,
name|getRS
argument_list|()
argument_list|,
name|hriSecondary
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|assertGet
parameter_list|(
name|HRegion
name|region
parameter_list|,
name|int
name|value
parameter_list|,
name|boolean
name|expect
parameter_list|)
throws|throws
name|IOException
block|{
name|byte
index|[]
name|row
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
name|value
argument_list|)
argument_list|)
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
name|Result
name|result
init|=
name|region
operator|.
name|get
argument_list|(
name|get
argument_list|)
decl_stmt|;
if|if
condition|(
name|expect
condition|)
block|{
name|Assert
operator|.
name|assertArrayEquals
argument_list|(
name|row
argument_list|,
name|result
operator|.
name|getValue
argument_list|(
name|f
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|result
operator|.
name|isEmpty
argument_list|()
expr_stmt|;
block|}
block|}
comment|// build a mock rpc
specifier|private
name|void
name|assertGetRpc
parameter_list|(
name|HRegionInfo
name|info
parameter_list|,
name|int
name|value
parameter_list|,
name|boolean
name|expect
parameter_list|)
throws|throws
name|IOException
throws|,
name|ServiceException
block|{
name|byte
index|[]
name|row
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
name|value
argument_list|)
argument_list|)
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
name|ClientProtos
operator|.
name|GetRequest
name|getReq
init|=
name|RequestConverter
operator|.
name|buildGetRequest
argument_list|(
name|info
operator|.
name|getRegionName
argument_list|()
argument_list|,
name|get
argument_list|)
decl_stmt|;
name|ClientProtos
operator|.
name|GetResponse
name|getResp
init|=
name|getRS
argument_list|()
operator|.
name|getRSRpcServices
argument_list|()
operator|.
name|get
argument_list|(
literal|null
argument_list|,
name|getReq
argument_list|)
decl_stmt|;
name|Result
name|result
init|=
name|ProtobufUtil
operator|.
name|toResult
argument_list|(
name|getResp
operator|.
name|getResult
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|expect
condition|)
block|{
name|Assert
operator|.
name|assertArrayEquals
argument_list|(
name|row
argument_list|,
name|result
operator|.
name|getValue
argument_list|(
name|f
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|result
operator|.
name|isEmpty
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|restartRegionServer
parameter_list|()
throws|throws
name|Exception
block|{
name|afterClass
argument_list|()
expr_stmt|;
name|before
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|300000
argument_list|)
specifier|public
name|void
name|testRefreshStoreFiles
parameter_list|()
throws|throws
name|Exception
block|{
comment|// enable store file refreshing
specifier|final
name|int
name|refreshPeriod
init|=
literal|2000
decl_stmt|;
comment|// 2 sec
name|HTU
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"hbase.hstore.compactionThreshold"
argument_list|,
literal|100
argument_list|)
expr_stmt|;
name|HTU
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
name|StorefileRefresherChore
operator|.
name|REGIONSERVER_STOREFILE_REFRESH_PERIOD
argument_list|,
name|refreshPeriod
argument_list|)
expr_stmt|;
comment|// restart the region server so that it starts the refresher chore
name|restartRegionServer
argument_list|()
expr_stmt|;
try|try
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Opening the secondary region "
operator|+
name|hriSecondary
operator|.
name|getEncodedName
argument_list|()
argument_list|)
expr_stmt|;
name|openRegion
argument_list|(
name|HTU
argument_list|,
name|getRS
argument_list|()
argument_list|,
name|hriSecondary
argument_list|)
expr_stmt|;
comment|//load some data to primary
name|LOG
operator|.
name|info
argument_list|(
literal|"Loading data to primary region"
argument_list|)
expr_stmt|;
name|HTU
operator|.
name|loadNumericRows
argument_list|(
name|table
argument_list|,
name|f
argument_list|,
literal|0
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
comment|// assert that we can read back from primary
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1000
argument_list|,
name|HTU
operator|.
name|countRows
argument_list|(
name|table
argument_list|)
argument_list|)
expr_stmt|;
comment|// flush so that region replica can read
name|LOG
operator|.
name|info
argument_list|(
literal|"Flushing primary region"
argument_list|)
expr_stmt|;
name|getRS
argument_list|()
operator|.
name|getRegionByEncodedName
argument_list|(
name|hriPrimary
operator|.
name|getEncodedName
argument_list|()
argument_list|)
operator|.
name|flushcache
argument_list|()
expr_stmt|;
comment|// ensure that chore is run
name|LOG
operator|.
name|info
argument_list|(
literal|"Sleeping for "
operator|+
operator|(
literal|4
operator|*
name|refreshPeriod
operator|)
argument_list|)
expr_stmt|;
name|Threads
operator|.
name|sleep
argument_list|(
literal|4
operator|*
name|refreshPeriod
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Checking results from secondary region replica"
argument_list|)
expr_stmt|;
name|HRegion
name|secondaryRegion
init|=
name|getRS
argument_list|()
operator|.
name|getFromOnlineRegions
argument_list|(
name|hriSecondary
operator|.
name|getEncodedName
argument_list|()
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|secondaryRegion
operator|.
name|getStore
argument_list|(
name|f
argument_list|)
operator|.
name|getStorefilesCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertGet
argument_list|(
name|secondaryRegion
argument_list|,
literal|42
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|assertGetRpc
argument_list|(
name|hriSecondary
argument_list|,
literal|42
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|assertGetRpc
argument_list|(
name|hriSecondary
argument_list|,
literal|1042
argument_list|,
literal|false
argument_list|)
expr_stmt|;
comment|//load some data to primary
name|HTU
operator|.
name|loadNumericRows
argument_list|(
name|table
argument_list|,
name|f
argument_list|,
literal|1000
argument_list|,
literal|1100
argument_list|)
expr_stmt|;
name|getRS
argument_list|()
operator|.
name|getRegionByEncodedName
argument_list|(
name|hriPrimary
operator|.
name|getEncodedName
argument_list|()
argument_list|)
operator|.
name|flushcache
argument_list|()
expr_stmt|;
name|HTU
operator|.
name|loadNumericRows
argument_list|(
name|table
argument_list|,
name|f
argument_list|,
literal|2000
argument_list|,
literal|2100
argument_list|)
expr_stmt|;
name|getRS
argument_list|()
operator|.
name|getRegionByEncodedName
argument_list|(
name|hriPrimary
operator|.
name|getEncodedName
argument_list|()
argument_list|)
operator|.
name|flushcache
argument_list|()
expr_stmt|;
comment|// ensure that chore is run
name|Threads
operator|.
name|sleep
argument_list|(
literal|4
operator|*
name|refreshPeriod
argument_list|)
expr_stmt|;
name|assertGetRpc
argument_list|(
name|hriSecondary
argument_list|,
literal|42
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|assertGetRpc
argument_list|(
name|hriSecondary
argument_list|,
literal|1042
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|assertGetRpc
argument_list|(
name|hriSecondary
argument_list|,
literal|2042
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// ensure that we are see the 3 store files
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|secondaryRegion
operator|.
name|getStore
argument_list|(
name|f
argument_list|)
operator|.
name|getStorefilesCount
argument_list|()
argument_list|)
expr_stmt|;
comment|// force compaction
name|HTU
operator|.
name|compact
argument_list|(
name|table
operator|.
name|getName
argument_list|()
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|long
name|wakeUpTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|+
literal|4
operator|*
name|refreshPeriod
decl_stmt|;
while|while
condition|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|<
name|wakeUpTime
condition|)
block|{
name|assertGetRpc
argument_list|(
name|hriSecondary
argument_list|,
literal|42
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|assertGetRpc
argument_list|(
name|hriSecondary
argument_list|,
literal|1042
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|assertGetRpc
argument_list|(
name|hriSecondary
argument_list|,
literal|2042
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|Threads
operator|.
name|sleep
argument_list|(
literal|10
argument_list|)
expr_stmt|;
block|}
comment|// ensure that we see the compacted file only
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|secondaryRegion
operator|.
name|getStore
argument_list|(
name|f
argument_list|)
operator|.
name|getStorefilesCount
argument_list|()
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|HTU
operator|.
name|deleteNumericRows
argument_list|(
name|table
argument_list|,
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
literal|0
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
name|closeRegion
argument_list|(
name|HTU
argument_list|,
name|getRS
argument_list|()
argument_list|,
name|hriSecondary
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|300000
argument_list|)
specifier|public
name|void
name|testFlushAndCompactionsInPrimary
parameter_list|()
throws|throws
name|Exception
block|{
name|long
name|runtime
init|=
literal|30
operator|*
literal|1000
decl_stmt|;
comment|// enable store file refreshing
specifier|final
name|int
name|refreshPeriod
init|=
literal|100
decl_stmt|;
comment|// 100ms refresh is a lot
name|HTU
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"hbase.hstore.compactionThreshold"
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|HTU
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
name|StorefileRefresherChore
operator|.
name|REGIONSERVER_STOREFILE_REFRESH_PERIOD
argument_list|,
name|refreshPeriod
argument_list|)
expr_stmt|;
comment|// restart the region server so that it starts the refresher chore
name|restartRegionServer
argument_list|()
expr_stmt|;
specifier|final
name|int
name|startKey
init|=
literal|0
decl_stmt|,
name|endKey
init|=
literal|1000
decl_stmt|;
try|try
block|{
name|openRegion
argument_list|(
name|HTU
argument_list|,
name|getRS
argument_list|()
argument_list|,
name|hriSecondary
argument_list|)
expr_stmt|;
comment|//load some data to primary so that reader won't fail
name|HTU
operator|.
name|loadNumericRows
argument_list|(
name|table
argument_list|,
name|f
argument_list|,
name|startKey
argument_list|,
name|endKey
argument_list|)
expr_stmt|;
name|TestRegionServerNoMaster
operator|.
name|flushRegion
argument_list|(
name|HTU
argument_list|,
name|hriPrimary
argument_list|)
expr_stmt|;
comment|// ensure that chore is run
name|Threads
operator|.
name|sleep
argument_list|(
literal|2
operator|*
name|refreshPeriod
argument_list|)
expr_stmt|;
specifier|final
name|AtomicBoolean
name|running
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|true
argument_list|)
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|final
name|AtomicReference
argument_list|<
name|Exception
argument_list|>
index|[]
name|exceptions
init|=
operator|new
name|AtomicReference
index|[
literal|3
index|]
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
name|exceptions
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|exceptions
index|[
name|i
index|]
operator|=
operator|new
name|AtomicReference
argument_list|<
name|Exception
argument_list|>
argument_list|()
expr_stmt|;
block|}
name|Runnable
name|writer
init|=
operator|new
name|Runnable
argument_list|()
block|{
name|int
name|key
init|=
name|startKey
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
while|while
condition|(
name|running
operator|.
name|get
argument_list|()
condition|)
block|{
name|byte
index|[]
name|data
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
name|key
argument_list|)
argument_list|)
decl_stmt|;
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|data
argument_list|)
decl_stmt|;
name|put
operator|.
name|add
argument_list|(
name|f
argument_list|,
literal|null
argument_list|,
name|data
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|key
operator|++
expr_stmt|;
if|if
condition|(
name|key
operator|==
name|endKey
condition|)
name|key
operator|=
name|startKey
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
name|ex
argument_list|)
expr_stmt|;
name|exceptions
index|[
literal|0
index|]
operator|.
name|compareAndSet
argument_list|(
literal|null
argument_list|,
name|ex
argument_list|)
expr_stmt|;
block|}
block|}
block|}
decl_stmt|;
name|Runnable
name|flusherCompactor
init|=
operator|new
name|Runnable
argument_list|()
block|{
name|Random
name|random
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
while|while
condition|(
name|running
operator|.
name|get
argument_list|()
condition|)
block|{
comment|// flush or compact
if|if
condition|(
name|random
operator|.
name|nextBoolean
argument_list|()
condition|)
block|{
name|TestRegionServerNoMaster
operator|.
name|flushRegion
argument_list|(
name|HTU
argument_list|,
name|hriPrimary
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|HTU
operator|.
name|compact
argument_list|(
name|table
operator|.
name|getName
argument_list|()
argument_list|,
name|random
operator|.
name|nextBoolean
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
name|ex
argument_list|)
expr_stmt|;
name|exceptions
index|[
literal|1
index|]
operator|.
name|compareAndSet
argument_list|(
literal|null
argument_list|,
name|ex
argument_list|)
expr_stmt|;
block|}
block|}
block|}
decl_stmt|;
name|Runnable
name|reader
init|=
operator|new
name|Runnable
argument_list|()
block|{
name|Random
name|random
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
while|while
condition|(
name|running
operator|.
name|get
argument_list|()
condition|)
block|{
comment|// whether to do a close and open
if|if
condition|(
name|random
operator|.
name|nextInt
argument_list|(
literal|10
argument_list|)
operator|==
literal|0
condition|)
block|{
try|try
block|{
name|closeRegion
argument_list|(
name|HTU
argument_list|,
name|getRS
argument_list|()
argument_list|,
name|hriSecondary
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed closing the region "
operator|+
name|hriSecondary
operator|+
literal|" "
operator|+
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|ex
argument_list|)
argument_list|)
expr_stmt|;
name|exceptions
index|[
literal|2
index|]
operator|.
name|compareAndSet
argument_list|(
literal|null
argument_list|,
name|ex
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|openRegion
argument_list|(
name|HTU
argument_list|,
name|getRS
argument_list|()
argument_list|,
name|hriSecondary
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed opening the region "
operator|+
name|hriSecondary
operator|+
literal|" "
operator|+
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|ex
argument_list|)
argument_list|)
expr_stmt|;
name|exceptions
index|[
literal|2
index|]
operator|.
name|compareAndSet
argument_list|(
literal|null
argument_list|,
name|ex
argument_list|)
expr_stmt|;
block|}
block|}
name|int
name|key
init|=
name|random
operator|.
name|nextInt
argument_list|(
name|endKey
operator|-
name|startKey
argument_list|)
operator|+
name|startKey
decl_stmt|;
name|assertGetRpc
argument_list|(
name|hriSecondary
argument_list|,
name|key
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed getting the value in the region "
operator|+
name|hriSecondary
operator|+
literal|" "
operator|+
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|ex
argument_list|)
argument_list|)
expr_stmt|;
name|exceptions
index|[
literal|2
index|]
operator|.
name|compareAndSet
argument_list|(
literal|null
argument_list|,
name|ex
argument_list|)
expr_stmt|;
block|}
block|}
block|}
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting writer and reader"
argument_list|)
expr_stmt|;
name|ExecutorService
name|executor
init|=
name|Executors
operator|.
name|newFixedThreadPool
argument_list|(
literal|3
argument_list|)
decl_stmt|;
name|executor
operator|.
name|submit
argument_list|(
name|writer
argument_list|)
expr_stmt|;
name|executor
operator|.
name|submit
argument_list|(
name|flusherCompactor
argument_list|)
expr_stmt|;
name|executor
operator|.
name|submit
argument_list|(
name|reader
argument_list|)
expr_stmt|;
comment|// wait for threads
name|Threads
operator|.
name|sleep
argument_list|(
name|runtime
argument_list|)
expr_stmt|;
name|running
operator|.
name|set
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|executor
operator|.
name|shutdown
argument_list|()
expr_stmt|;
name|executor
operator|.
name|awaitTermination
argument_list|(
literal|30
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
for|for
control|(
name|AtomicReference
argument_list|<
name|Exception
argument_list|>
name|exRef
range|:
name|exceptions
control|)
block|{
name|Assert
operator|.
name|assertNull
argument_list|(
name|exRef
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|HTU
operator|.
name|deleteNumericRows
argument_list|(
name|table
argument_list|,
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
name|startKey
argument_list|,
name|endKey
argument_list|)
expr_stmt|;
name|closeRegion
argument_list|(
name|HTU
argument_list|,
name|getRS
argument_list|()
argument_list|,
name|hriSecondary
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

