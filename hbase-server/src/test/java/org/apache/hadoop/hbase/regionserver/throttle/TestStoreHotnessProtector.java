begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *<p>  * http://www.apache.org/licenses/LICENSE-2.0  *<p>  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|regionserver
operator|.
name|throttle
operator|.
name|StoreHotnessProtector
operator|.
name|PARALLEL_PREPARE_PUT_STORE_MULTIPLIER
import|;
end_import

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
name|throttle
operator|.
name|StoreHotnessProtector
operator|.
name|PARALLEL_PUT_STORE_THREADS_LIMIT
import|;
end_import

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
name|throttle
operator|.
name|StoreHotnessProtector
operator|.
name|PARALLEL_PUT_STORE_THREADS_LIMIT_MIN_COLUMN_COUNT
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|mock
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|when
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|CountDownLatch
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
name|AtomicReference
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
name|RegionTooBusyException
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
name|RegionInfo
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
name|Store
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
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
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
name|SmallTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestStoreHotnessProtector
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
name|TestStoreHotnessProtector
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testPreparePutCounter
parameter_list|()
throws|throws
name|Exception
block|{
name|ExecutorService
name|executorService
init|=
name|Executors
operator|.
name|newFixedThreadPool
argument_list|(
literal|10
argument_list|)
decl_stmt|;
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|PARALLEL_PUT_STORE_THREADS_LIMIT_MIN_COLUMN_COUNT
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|PARALLEL_PUT_STORE_THREADS_LIMIT
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|PARALLEL_PREPARE_PUT_STORE_MULTIPLIER
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|Region
name|mockRegion
init|=
name|mock
argument_list|(
name|Region
operator|.
name|class
argument_list|)
decl_stmt|;
name|StoreHotnessProtector
name|storeHotnessProtector
init|=
operator|new
name|StoreHotnessProtector
argument_list|(
name|mockRegion
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|Store
name|mockStore1
init|=
name|mock
argument_list|(
name|Store
operator|.
name|class
argument_list|)
decl_stmt|;
name|RegionInfo
name|mockRegionInfo
init|=
name|mock
argument_list|(
name|RegionInfo
operator|.
name|class
argument_list|)
decl_stmt|;
name|byte
index|[]
name|family
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testF1"
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|mockRegion
operator|.
name|getStore
argument_list|(
name|family
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|mockStore1
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|mockRegion
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|mockRegionInfo
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|mockRegionInfo
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|"test_region_1"
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|mockStore1
operator|.
name|getCurrentParallelPutCount
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|mockStore1
operator|.
name|getColumnFamilyName
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|"test_Family_1"
argument_list|)
expr_stmt|;
specifier|final
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|List
argument_list|<
name|Cell
argument_list|>
argument_list|>
name|familyMaps
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|familyMaps
operator|.
name|put
argument_list|(
name|family
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|(
name|mock
argument_list|(
name|Cell
operator|.
name|class
argument_list|)
argument_list|,
name|mock
argument_list|(
name|Cell
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|AtomicReference
argument_list|<
name|Exception
argument_list|>
name|exception
init|=
operator|new
name|AtomicReference
argument_list|<>
argument_list|()
decl_stmt|;
comment|// PreparePutCounter not access limit
name|int
name|threadCount
init|=
name|conf
operator|.
name|getInt
argument_list|(
name|PARALLEL_PUT_STORE_THREADS_LIMIT
argument_list|,
literal|10
argument_list|)
operator|*
name|conf
operator|.
name|getInt
argument_list|(
name|PARALLEL_PREPARE_PUT_STORE_MULTIPLIER
argument_list|,
literal|3
argument_list|)
decl_stmt|;
name|CountDownLatch
name|countDownLatch
init|=
operator|new
name|CountDownLatch
argument_list|(
name|threadCount
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
name|threadCount
condition|;
name|i
operator|++
control|)
block|{
name|executorService
operator|.
name|execute
argument_list|(
parameter_list|()
lambda|->
block|{
try|try
block|{
name|storeHotnessProtector
operator|.
name|start
argument_list|(
name|familyMaps
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RegionTooBusyException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|exception
operator|.
name|set
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|countDownLatch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
name|countDownLatch
operator|.
name|await
argument_list|(
literal|60
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
comment|//no exception
name|Assert
operator|.
name|assertEquals
argument_list|(
name|exception
operator|.
name|get
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|storeHotnessProtector
operator|.
name|getPreparePutToStoreMap
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|storeHotnessProtector
operator|.
name|getPreparePutToStoreMap
argument_list|()
operator|.
name|get
argument_list|(
name|family
argument_list|)
operator|.
name|get
argument_list|()
argument_list|,
name|threadCount
argument_list|)
expr_stmt|;
comment|// access limit
try|try
block|{
name|storeHotnessProtector
operator|.
name|start
argument_list|(
name|familyMaps
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RegionTooBusyException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|exception
operator|.
name|set
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
name|Assert
operator|.
name|assertEquals
argument_list|(
name|exception
operator|.
name|get
argument_list|()
operator|.
name|getClass
argument_list|()
argument_list|,
name|RegionTooBusyException
operator|.
name|class
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|storeHotnessProtector
operator|.
name|getPreparePutToStoreMap
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
comment|// when access limit, counter will not changed.
name|Assert
operator|.
name|assertEquals
argument_list|(
name|storeHotnessProtector
operator|.
name|getPreparePutToStoreMap
argument_list|()
operator|.
name|get
argument_list|(
name|family
argument_list|)
operator|.
name|get
argument_list|()
argument_list|,
name|threadCount
operator|+
literal|1
argument_list|)
expr_stmt|;
name|storeHotnessProtector
operator|.
name|finish
argument_list|(
name|familyMaps
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|storeHotnessProtector
operator|.
name|getPreparePutToStoreMap
argument_list|()
operator|.
name|get
argument_list|(
name|family
argument_list|)
operator|.
name|get
argument_list|()
argument_list|,
name|threadCount
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

