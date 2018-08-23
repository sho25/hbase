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
operator|.
name|throttle
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
name|master
operator|.
name|LoadBalancer
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
name|DefaultStoreEngine
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
name|regionserver
operator|.
name|HStore
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
name|Region
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
name|StoreEngine
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
name|StripeStoreEngine
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
name|JVMClusterUtil
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
name|TestFlushWithThroughputController
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
name|TestFlushWithThroughputController
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
name|TestFlushWithThroughputController
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|double
name|EPSILON
init|=
literal|1.3E
operator|-
literal|6
decl_stmt|;
specifier|private
name|HBaseTestingUtility
name|hbtu
decl_stmt|;
annotation|@
name|Rule
specifier|public
name|TestName
name|testName
init|=
operator|new
name|TestName
argument_list|()
decl_stmt|;
specifier|private
name|TableName
name|tableName
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|family
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|qualifier
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"q"
argument_list|)
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
block|{
name|hbtu
operator|=
operator|new
name|HBaseTestingUtility
argument_list|()
expr_stmt|;
name|tableName
operator|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"Table-"
operator|+
name|testName
operator|.
name|getMethodName
argument_list|()
argument_list|)
expr_stmt|;
name|hbtu
operator|.
name|getConfiguration
argument_list|()
operator|.
name|set
argument_list|(
name|FlushThroughputControllerFactory
operator|.
name|HBASE_FLUSH_THROUGHPUT_CONTROLLER_KEY
argument_list|,
name|PressureAwareFlushThroughputController
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|hbtu
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
specifier|private
name|HStore
name|getStoreWithName
parameter_list|(
name|TableName
name|tableName
parameter_list|)
block|{
name|MiniHBaseCluster
name|cluster
init|=
name|hbtu
operator|.
name|getMiniHBaseCluster
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|JVMClusterUtil
operator|.
name|RegionServerThread
argument_list|>
name|rsts
init|=
name|cluster
operator|.
name|getRegionServerThreads
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
name|cluster
operator|.
name|getRegionServerThreads
argument_list|()
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|HRegionServer
name|hrs
init|=
name|rsts
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getRegionServer
argument_list|()
decl_stmt|;
for|for
control|(
name|Region
name|region
range|:
name|hrs
operator|.
name|getRegions
argument_list|(
name|tableName
argument_list|)
control|)
block|{
return|return
operator|(
operator|(
name|HRegion
operator|)
name|region
operator|)
operator|.
name|getStores
argument_list|()
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
specifier|private
name|void
name|setMaxMinThroughputs
parameter_list|(
name|long
name|max
parameter_list|,
name|long
name|min
parameter_list|)
block|{
name|Configuration
name|conf
init|=
name|hbtu
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
name|PressureAwareFlushThroughputController
operator|.
name|HBASE_HSTORE_FLUSH_MAX_THROUGHPUT_LOWER_BOUND
argument_list|,
name|min
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
name|PressureAwareFlushThroughputController
operator|.
name|HBASE_HSTORE_FLUSH_MAX_THROUGHPUT_UPPER_BOUND
argument_list|,
name|max
argument_list|)
expr_stmt|;
block|}
comment|/**    * Writes Puts to the table and flushes few times.    * @return {@link Pair} of (throughput, duration).    */
specifier|private
name|Pair
argument_list|<
name|Double
argument_list|,
name|Long
argument_list|>
name|generateAndFlushData
parameter_list|(
name|Table
name|table
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Internally, throughput is controlled after every cell write, so keep value size less for
comment|// better control.
specifier|final
name|int
name|NUM_FLUSHES
init|=
literal|3
decl_stmt|,
name|NUM_PUTS
init|=
literal|50
decl_stmt|,
name|VALUE_SIZE
init|=
literal|200
operator|*
literal|1024
decl_stmt|;
name|Random
name|rand
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
name|long
name|duration
init|=
literal|0
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
name|NUM_FLUSHES
condition|;
name|i
operator|++
control|)
block|{
comment|// Write about 10M (10 times of throughput rate) per iteration.
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|NUM_PUTS
condition|;
name|j
operator|++
control|)
block|{
name|byte
index|[]
name|value
init|=
operator|new
name|byte
index|[
name|VALUE_SIZE
index|]
decl_stmt|;
name|rand
operator|.
name|nextBytes
argument_list|(
name|value
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
operator|*
literal|10
operator|+
name|j
argument_list|)
argument_list|)
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|qualifier
argument_list|,
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|long
name|startTime
init|=
name|System
operator|.
name|nanoTime
argument_list|()
decl_stmt|;
name|hbtu
operator|.
name|getAdmin
argument_list|()
operator|.
name|flush
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|duration
operator|+=
name|System
operator|.
name|nanoTime
argument_list|()
operator|-
name|startTime
expr_stmt|;
block|}
name|HStore
name|store
init|=
name|getStoreWithName
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|NUM_FLUSHES
argument_list|,
name|store
operator|.
name|getStorefilesCount
argument_list|()
argument_list|)
expr_stmt|;
name|double
name|throughput
init|=
operator|(
name|double
operator|)
name|store
operator|.
name|getStorefilesSize
argument_list|()
operator|/
name|TimeUnit
operator|.
name|NANOSECONDS
operator|.
name|toSeconds
argument_list|(
name|duration
argument_list|)
decl_stmt|;
return|return
operator|new
name|Pair
argument_list|<>
argument_list|(
name|throughput
argument_list|,
name|duration
argument_list|)
return|;
block|}
specifier|private
name|long
name|testFlushWithThroughputLimit
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|long
name|throughputLimit
init|=
literal|1024
operator|*
literal|1024
decl_stmt|;
name|setMaxMinThroughputs
argument_list|(
name|throughputLimit
argument_list|,
name|throughputLimit
argument_list|)
expr_stmt|;
name|Configuration
name|conf
init|=
name|hbtu
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
name|PressureAwareFlushThroughputController
operator|.
name|HBASE_HSTORE_FLUSH_THROUGHPUT_CONTROL_CHECK_INTERVAL
argument_list|,
name|throughputLimit
argument_list|)
expr_stmt|;
name|hbtu
operator|.
name|startMiniCluster
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|Table
name|table
init|=
name|hbtu
operator|.
name|createTable
argument_list|(
name|tableName
argument_list|,
name|family
argument_list|)
decl_stmt|;
name|Pair
argument_list|<
name|Double
argument_list|,
name|Long
argument_list|>
name|result
init|=
name|generateAndFlushData
argument_list|(
name|table
argument_list|)
decl_stmt|;
name|hbtu
operator|.
name|deleteTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Throughput is: "
operator|+
operator|(
name|result
operator|.
name|getFirst
argument_list|()
operator|/
literal|1024
operator|/
literal|1024
operator|)
operator|+
literal|" MB/s"
argument_list|)
expr_stmt|;
comment|// confirm that the speed limit work properly(not too fast, and also not too slow)
comment|// 20% is the max acceptable error rate.
name|assertTrue
argument_list|(
name|result
operator|.
name|getFirst
argument_list|()
operator|<
name|throughputLimit
operator|*
literal|1.2
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|result
operator|.
name|getFirst
argument_list|()
operator|>
name|throughputLimit
operator|*
literal|0.8
argument_list|)
expr_stmt|;
return|return
name|result
operator|.
name|getSecond
argument_list|()
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFlushControl
parameter_list|()
throws|throws
name|Exception
block|{
name|testFlushWithThroughputLimit
argument_list|()
expr_stmt|;
block|}
comment|/**    * Test the tuning task of {@link PressureAwareFlushThroughputController}    */
annotation|@
name|Test
specifier|public
name|void
name|testFlushThroughputTuning
parameter_list|()
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
name|hbtu
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|setMaxMinThroughputs
argument_list|(
literal|20L
operator|*
literal|1024
operator|*
literal|1024
argument_list|,
literal|10L
operator|*
literal|1024
operator|*
literal|1024
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|StoreEngine
operator|.
name|STORE_ENGINE_CLASS_KEY
argument_list|,
name|DefaultStoreEngine
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|PressureAwareFlushThroughputController
operator|.
name|HBASE_HSTORE_FLUSH_THROUGHPUT_TUNE_PERIOD
argument_list|,
literal|3000
argument_list|)
expr_stmt|;
name|hbtu
operator|.
name|startMiniCluster
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|Connection
name|conn
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|hbtu
operator|.
name|getAdmin
argument_list|()
operator|.
name|createTable
argument_list|(
name|TableDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|tableName
argument_list|)
operator|.
name|setColumnFamily
argument_list|(
name|ColumnFamilyDescriptorBuilder
operator|.
name|of
argument_list|(
name|family
argument_list|)
argument_list|)
operator|.
name|setCompactionEnabled
argument_list|(
literal|false
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|hbtu
operator|.
name|waitTableAvailable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|HRegionServer
name|regionServer
init|=
name|hbtu
operator|.
name|getRSForFirstRegionInTable
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|PressureAwareFlushThroughputController
name|throughputController
init|=
operator|(
name|PressureAwareFlushThroughputController
operator|)
name|regionServer
operator|.
name|getFlushThroughputController
argument_list|()
decl_stmt|;
for|for
control|(
name|HRegion
name|region
range|:
name|regionServer
operator|.
name|getRegions
argument_list|()
control|)
block|{
name|region
operator|.
name|flush
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|0.0
argument_list|,
name|regionServer
operator|.
name|getFlushPressure
argument_list|()
argument_list|,
name|EPSILON
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|5000
argument_list|)
expr_stmt|;
name|boolean
name|tablesOnMaster
init|=
name|LoadBalancer
operator|.
name|isTablesOnMaster
argument_list|(
name|hbtu
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|tablesOnMaster
condition|)
block|{
comment|// If no tables on the master, this math is off and I'm not sure what it is supposed to be
comment|// when meta is on the regionserver and not on the master.
name|assertEquals
argument_list|(
literal|10L
operator|*
literal|1024
operator|*
literal|1024
argument_list|,
name|throughputController
operator|.
name|getMaxThroughput
argument_list|()
argument_list|,
name|EPSILON
argument_list|)
expr_stmt|;
block|}
name|Table
name|table
init|=
name|conn
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|Random
name|rand
init|=
operator|new
name|Random
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
literal|10
condition|;
name|i
operator|++
control|)
block|{
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
literal|10
condition|;
name|j
operator|++
control|)
block|{
name|byte
index|[]
name|value
init|=
operator|new
name|byte
index|[
literal|256
operator|*
literal|1024
index|]
decl_stmt|;
name|rand
operator|.
name|nextBytes
argument_list|(
name|value
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
operator|*
literal|10
operator|+
name|j
argument_list|)
argument_list|)
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|qualifier
argument_list|,
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|Thread
operator|.
name|sleep
argument_list|(
literal|5000
argument_list|)
expr_stmt|;
name|double
name|expectedThroughPut
init|=
literal|10L
operator|*
literal|1024
operator|*
literal|1024
operator|*
operator|(
literal|1
operator|+
name|regionServer
operator|.
name|getFlushPressure
argument_list|()
operator|)
decl_stmt|;
name|assertEquals
argument_list|(
name|expectedThroughPut
argument_list|,
name|throughputController
operator|.
name|getMaxThroughput
argument_list|()
argument_list|,
name|EPSILON
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|FlushThroughputControllerFactory
operator|.
name|HBASE_FLUSH_THROUGHPUT_CONTROLLER_KEY
argument_list|,
name|NoLimitThroughputController
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|regionServer
operator|.
name|onConfigurationChange
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|throughputController
operator|.
name|isStopped
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|regionServer
operator|.
name|getFlushThroughputController
argument_list|()
operator|instanceof
name|NoLimitThroughputController
argument_list|)
expr_stmt|;
name|conn
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|/**    * Test the logic for striped store.    */
annotation|@
name|Test
specifier|public
name|void
name|testFlushControlForStripedStore
parameter_list|()
throws|throws
name|Exception
block|{
name|hbtu
operator|.
name|getConfiguration
argument_list|()
operator|.
name|set
argument_list|(
name|StoreEngine
operator|.
name|STORE_ENGINE_CLASS_KEY
argument_list|,
name|StripeStoreEngine
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|testFlushWithThroughputLimit
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

