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
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
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
name|io
operator|.
name|util
operator|.
name|MemorySizeUtil
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
name|SmallTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestRegionServerAccounting
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
name|TestRegionServerAccounting
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testOnheapMemstoreHigherWaterMarkLimits
parameter_list|()
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
name|setFloat
argument_list|(
name|MemorySizeUtil
operator|.
name|MEMSTORE_SIZE_KEY
argument_list|,
literal|0.2f
argument_list|)
expr_stmt|;
comment|// try for default cases
name|RegionServerAccounting
name|regionServerAccounting
init|=
operator|new
name|RegionServerAccounting
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|MemStoreSize
name|memstoreSize
init|=
operator|new
name|MemStoreSize
argument_list|(
operator|(
literal|3L
operator|*
literal|1024L
operator|*
literal|1024L
operator|*
literal|1024L
operator|)
argument_list|,
operator|(
literal|1L
operator|*
literal|1024L
operator|*
literal|1024L
operator|*
literal|1024L
operator|)
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|regionServerAccounting
operator|.
name|incGlobalMemStoreSize
argument_list|(
name|memstoreSize
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|FlushType
operator|.
name|ABOVE_ONHEAP_HIGHER_MARK
argument_list|,
name|regionServerAccounting
operator|.
name|isAboveHighWaterMark
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testOnheapMemstoreLowerWaterMarkLimits
parameter_list|()
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
name|setFloat
argument_list|(
name|MemorySizeUtil
operator|.
name|MEMSTORE_SIZE_KEY
argument_list|,
literal|0.2f
argument_list|)
expr_stmt|;
comment|// try for default cases
name|RegionServerAccounting
name|regionServerAccounting
init|=
operator|new
name|RegionServerAccounting
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|MemStoreSize
name|memstoreSize
init|=
operator|new
name|MemStoreSize
argument_list|(
operator|(
literal|3L
operator|*
literal|1024L
operator|*
literal|1024L
operator|*
literal|1024L
operator|)
argument_list|,
operator|(
literal|1L
operator|*
literal|1024L
operator|*
literal|1024L
operator|*
literal|1024L
operator|)
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|regionServerAccounting
operator|.
name|incGlobalMemStoreSize
argument_list|(
name|memstoreSize
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|FlushType
operator|.
name|ABOVE_ONHEAP_LOWER_MARK
argument_list|,
name|regionServerAccounting
operator|.
name|isAboveLowWaterMark
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testOffheapMemstoreHigherWaterMarkLimitsDueToDataSize
parameter_list|()
block|{
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
comment|// setting 1G as offheap data size
name|conf
operator|.
name|setLong
argument_list|(
name|MemorySizeUtil
operator|.
name|OFFHEAP_MEMSTORE_SIZE_KEY
argument_list|,
operator|(
literal|1L
operator|*
literal|1024L
operator|)
argument_list|)
expr_stmt|;
comment|// try for default cases
name|RegionServerAccounting
name|regionServerAccounting
init|=
operator|new
name|RegionServerAccounting
argument_list|(
name|conf
argument_list|)
decl_stmt|;
comment|// this will breach offheap limit as data size is higher and not due to heap size
name|MemStoreSize
name|memstoreSize
init|=
operator|new
name|MemStoreSize
argument_list|(
operator|(
literal|3L
operator|*
literal|1024L
operator|*
literal|1024L
operator|*
literal|1024L
operator|)
argument_list|,
literal|0
argument_list|,
operator|(
literal|1L
operator|*
literal|1024L
operator|*
literal|1024L
operator|*
literal|1024L
operator|)
argument_list|)
decl_stmt|;
name|regionServerAccounting
operator|.
name|incGlobalMemStoreSize
argument_list|(
name|memstoreSize
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|FlushType
operator|.
name|ABOVE_OFFHEAP_HIGHER_MARK
argument_list|,
name|regionServerAccounting
operator|.
name|isAboveHighWaterMark
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testOffheapMemstoreHigherWaterMarkLimitsDueToHeapSize
parameter_list|()
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
name|setFloat
argument_list|(
name|MemorySizeUtil
operator|.
name|MEMSTORE_SIZE_KEY
argument_list|,
literal|0.2f
argument_list|)
expr_stmt|;
comment|// setting 1G as offheap data size
name|conf
operator|.
name|setLong
argument_list|(
name|MemorySizeUtil
operator|.
name|OFFHEAP_MEMSTORE_SIZE_KEY
argument_list|,
operator|(
literal|1L
operator|*
literal|1024L
operator|)
argument_list|)
expr_stmt|;
comment|// try for default cases
name|RegionServerAccounting
name|regionServerAccounting
init|=
operator|new
name|RegionServerAccounting
argument_list|(
name|conf
argument_list|)
decl_stmt|;
comment|// this will breach higher limit as heap size is higher and not due to offheap size
name|MemStoreSize
name|memstoreSize
init|=
operator|new
name|MemStoreSize
argument_list|(
operator|(
literal|3L
operator|*
literal|1024L
operator|*
literal|1024L
operator|)
argument_list|,
operator|(
literal|2L
operator|*
literal|1024L
operator|*
literal|1024L
operator|*
literal|1024L
operator|)
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|regionServerAccounting
operator|.
name|incGlobalMemStoreSize
argument_list|(
name|memstoreSize
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|FlushType
operator|.
name|ABOVE_ONHEAP_HIGHER_MARK
argument_list|,
name|regionServerAccounting
operator|.
name|isAboveHighWaterMark
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testOffheapMemstoreLowerWaterMarkLimitsDueToDataSize
parameter_list|()
block|{
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
comment|// setting 1G as offheap data size
name|conf
operator|.
name|setLong
argument_list|(
name|MemorySizeUtil
operator|.
name|OFFHEAP_MEMSTORE_SIZE_KEY
argument_list|,
operator|(
literal|1L
operator|*
literal|1024L
operator|)
argument_list|)
expr_stmt|;
comment|// try for default cases
name|RegionServerAccounting
name|regionServerAccounting
init|=
operator|new
name|RegionServerAccounting
argument_list|(
name|conf
argument_list|)
decl_stmt|;
comment|// this will breach offheap limit as data size is higher and not due to heap size
name|MemStoreSize
name|memstoreSize
init|=
operator|new
name|MemStoreSize
argument_list|(
operator|(
literal|3L
operator|*
literal|1024L
operator|*
literal|1024L
operator|*
literal|1024L
operator|)
argument_list|,
literal|0
argument_list|,
operator|(
literal|1L
operator|*
literal|1024L
operator|*
literal|1024L
operator|*
literal|1024L
operator|)
argument_list|)
decl_stmt|;
name|regionServerAccounting
operator|.
name|incGlobalMemStoreSize
argument_list|(
name|memstoreSize
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|FlushType
operator|.
name|ABOVE_OFFHEAP_LOWER_MARK
argument_list|,
name|regionServerAccounting
operator|.
name|isAboveLowWaterMark
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testOffheapMemstoreLowerWaterMarkLimitsDueToHeapSize
parameter_list|()
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
name|setFloat
argument_list|(
name|MemorySizeUtil
operator|.
name|MEMSTORE_SIZE_KEY
argument_list|,
literal|0.2f
argument_list|)
expr_stmt|;
comment|// setting 1G as offheap data size
name|conf
operator|.
name|setLong
argument_list|(
name|MemorySizeUtil
operator|.
name|OFFHEAP_MEMSTORE_SIZE_KEY
argument_list|,
operator|(
literal|1L
operator|*
literal|1024L
operator|)
argument_list|)
expr_stmt|;
comment|// try for default cases
name|RegionServerAccounting
name|regionServerAccounting
init|=
operator|new
name|RegionServerAccounting
argument_list|(
name|conf
argument_list|)
decl_stmt|;
comment|// this will breach higher limit as heap size is higher and not due to offheap size
name|MemStoreSize
name|memstoreSize
init|=
operator|new
name|MemStoreSize
argument_list|(
operator|(
literal|3L
operator|*
literal|1024L
operator|*
literal|1024L
operator|)
argument_list|,
operator|(
literal|2L
operator|*
literal|1024L
operator|*
literal|1024L
operator|*
literal|1024L
operator|)
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|regionServerAccounting
operator|.
name|incGlobalMemStoreSize
argument_list|(
name|memstoreSize
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|FlushType
operator|.
name|ABOVE_ONHEAP_LOWER_MARK
argument_list|,
name|regionServerAccounting
operator|.
name|isAboveLowWaterMark
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

