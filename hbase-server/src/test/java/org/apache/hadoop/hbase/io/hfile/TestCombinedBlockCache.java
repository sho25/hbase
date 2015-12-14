begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|io
operator|.
name|hfile
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
name|hbase
operator|.
name|io
operator|.
name|hfile
operator|.
name|CombinedBlockCache
operator|.
name|CombinedCacheStats
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
name|SmallTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestCombinedBlockCache
block|{
annotation|@
name|Test
specifier|public
name|void
name|testCombinedCacheStats
parameter_list|()
block|{
name|CacheStats
name|lruCacheStats
init|=
operator|new
name|CacheStats
argument_list|(
literal|"lruCacheStats"
argument_list|,
literal|2
argument_list|)
decl_stmt|;
name|CacheStats
name|bucketCacheStats
init|=
operator|new
name|CacheStats
argument_list|(
literal|"bucketCacheStats"
argument_list|,
literal|2
argument_list|)
decl_stmt|;
name|CombinedCacheStats
name|stats
init|=
operator|new
name|CombinedCacheStats
argument_list|(
name|lruCacheStats
argument_list|,
name|bucketCacheStats
argument_list|)
decl_stmt|;
name|double
name|delta
init|=
literal|0.01
decl_stmt|;
comment|// period 1:
comment|// lru cache: 1 hit caching, 1 miss caching
comment|// bucket cache: 2 hit non-caching,1 miss non-caching/primary,1 fail insert
name|lruCacheStats
operator|.
name|hit
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|lruCacheStats
operator|.
name|miss
argument_list|(
literal|true
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|bucketCacheStats
operator|.
name|hit
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|bucketCacheStats
operator|.
name|hit
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|bucketCacheStats
operator|.
name|miss
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|5
argument_list|,
name|stats
operator|.
name|getRequestCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|stats
operator|.
name|getRequestCachingCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|stats
operator|.
name|getMissCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|stats
operator|.
name|getPrimaryMissCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|stats
operator|.
name|getMissCachingCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|stats
operator|.
name|getHitCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|stats
operator|.
name|getPrimaryHitCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|stats
operator|.
name|getHitCachingCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0.6
argument_list|,
name|stats
operator|.
name|getHitRatio
argument_list|()
argument_list|,
name|delta
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0.5
argument_list|,
name|stats
operator|.
name|getHitCachingRatio
argument_list|()
argument_list|,
name|delta
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0.4
argument_list|,
name|stats
operator|.
name|getMissRatio
argument_list|()
argument_list|,
name|delta
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0.5
argument_list|,
name|stats
operator|.
name|getMissCachingRatio
argument_list|()
argument_list|,
name|delta
argument_list|)
expr_stmt|;
comment|// lru cache: 2 evicted, 1 evict
comment|// bucket cache: 1 evict
name|lruCacheStats
operator|.
name|evicted
argument_list|(
literal|1000
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|lruCacheStats
operator|.
name|evicted
argument_list|(
literal|1000
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|lruCacheStats
operator|.
name|evict
argument_list|()
expr_stmt|;
name|bucketCacheStats
operator|.
name|evict
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|stats
operator|.
name|getEvictionCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|stats
operator|.
name|getEvictedCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|stats
operator|.
name|getPrimaryEvictedCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1.0
argument_list|,
name|stats
operator|.
name|evictedPerEviction
argument_list|()
argument_list|,
name|delta
argument_list|)
expr_stmt|;
comment|// lru cache:  1 fail insert
name|lruCacheStats
operator|.
name|failInsert
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|stats
operator|.
name|getFailedInserts
argument_list|()
argument_list|)
expr_stmt|;
comment|// rollMetricsPeriod
name|stats
operator|.
name|rollMetricsPeriod
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|stats
operator|.
name|getSumHitCountsPastNPeriods
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|5
argument_list|,
name|stats
operator|.
name|getSumRequestCountsPastNPeriods
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|stats
operator|.
name|getSumHitCachingCountsPastNPeriods
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|stats
operator|.
name|getSumRequestCachingCountsPastNPeriods
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0.6
argument_list|,
name|stats
operator|.
name|getHitRatioPastNPeriods
argument_list|()
argument_list|,
name|delta
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0.5
argument_list|,
name|stats
operator|.
name|getHitCachingRatioPastNPeriods
argument_list|()
argument_list|,
name|delta
argument_list|)
expr_stmt|;
comment|// period 2:
comment|// lru cache: 3 hit caching
name|lruCacheStats
operator|.
name|hit
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|lruCacheStats
operator|.
name|hit
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|lruCacheStats
operator|.
name|hit
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|stats
operator|.
name|rollMetricsPeriod
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|6
argument_list|,
name|stats
operator|.
name|getSumHitCountsPastNPeriods
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|8
argument_list|,
name|stats
operator|.
name|getSumRequestCountsPastNPeriods
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|stats
operator|.
name|getSumHitCachingCountsPastNPeriods
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|5
argument_list|,
name|stats
operator|.
name|getSumRequestCachingCountsPastNPeriods
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0.75
argument_list|,
name|stats
operator|.
name|getHitRatioPastNPeriods
argument_list|()
argument_list|,
name|delta
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0.8
argument_list|,
name|stats
operator|.
name|getHitCachingRatioPastNPeriods
argument_list|()
argument_list|,
name|delta
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

