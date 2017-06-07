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
name|assignment
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
name|java
operator|.
name|lang
operator|.
name|Thread
operator|.
name|UncaughtExceptionHandler
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
name|concurrent
operator|.
name|Callable
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
name|ExecutorCompletionService
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
name|Future
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
name|ThreadPoolExecutor
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
name|procedure2
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
name|Threads
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
block|{
name|MasterTests
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
name|TestRegionStates
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
name|TestRegionStates
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|HBaseTestingUtility
name|UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|ThreadPoolExecutor
name|threadPool
decl_stmt|;
specifier|private
specifier|static
name|ExecutorCompletionService
name|executorService
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|threadPool
operator|=
name|Threads
operator|.
name|getBoundedCachedThreadPool
argument_list|(
literal|32
argument_list|,
literal|60L
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|,
name|Threads
operator|.
name|newDaemonThreadFactory
argument_list|(
literal|"ProcedureDispatcher"
argument_list|,
operator|new
name|UncaughtExceptionHandler
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|uncaughtException
parameter_list|(
name|Thread
name|t
parameter_list|,
name|Throwable
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed thread "
operator|+
name|t
operator|.
name|getName
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|executorService
operator|=
operator|new
name|ExecutorCompletionService
argument_list|(
name|threadPool
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|threadPool
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|testSetup
parameter_list|()
block|{   }
annotation|@
name|After
specifier|public
name|void
name|testTearDown
parameter_list|()
throws|throws
name|Exception
block|{
while|while
condition|(
literal|true
condition|)
block|{
name|Future
argument_list|<
name|Object
argument_list|>
name|f
init|=
name|executorService
operator|.
name|poll
argument_list|()
decl_stmt|;
if|if
condition|(
name|f
operator|==
literal|null
condition|)
break|break;
name|f
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|void
name|waitExecutorService
parameter_list|(
specifier|final
name|int
name|count
parameter_list|)
throws|throws
name|Exception
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|count
condition|;
operator|++
name|i
control|)
block|{
name|executorService
operator|.
name|take
argument_list|()
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
block|}
comment|// ==========================================================================
comment|//  Regions related
comment|// ==========================================================================
annotation|@
name|Test
specifier|public
name|void
name|testRegionDoubleCreation
parameter_list|()
throws|throws
name|Exception
block|{
comment|// NOTE: HRegionInfo sort by table first, so we are relying on that
specifier|final
name|TableName
name|TABLE_NAME_A
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testOrderedByTableA"
argument_list|)
decl_stmt|;
specifier|final
name|TableName
name|TABLE_NAME_B
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testOrderedByTableB"
argument_list|)
decl_stmt|;
specifier|final
name|TableName
name|TABLE_NAME_C
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testOrderedByTableC"
argument_list|)
decl_stmt|;
specifier|final
name|RegionStates
name|stateMap
init|=
operator|new
name|RegionStates
argument_list|()
decl_stmt|;
specifier|final
name|int
name|NRUNS
init|=
literal|1000
decl_stmt|;
specifier|final
name|int
name|NSMALL_RUNS
init|=
literal|3
decl_stmt|;
comment|// add some regions for table B
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|NRUNS
condition|;
operator|++
name|i
control|)
block|{
name|addRegionNode
argument_list|(
name|stateMap
argument_list|,
name|TABLE_NAME_B
argument_list|,
name|i
argument_list|)
expr_stmt|;
block|}
comment|// re-add the regions for table B
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|NRUNS
condition|;
operator|++
name|i
control|)
block|{
name|addRegionNode
argument_list|(
name|stateMap
argument_list|,
name|TABLE_NAME_B
argument_list|,
name|i
argument_list|)
expr_stmt|;
block|}
name|waitExecutorService
argument_list|(
name|NRUNS
operator|*
literal|2
argument_list|)
expr_stmt|;
comment|// add two other tables A and C that will be placed before and after table B (sort order)
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|NSMALL_RUNS
condition|;
operator|++
name|i
control|)
block|{
name|addRegionNode
argument_list|(
name|stateMap
argument_list|,
name|TABLE_NAME_A
argument_list|,
name|i
argument_list|)
expr_stmt|;
name|addRegionNode
argument_list|(
name|stateMap
argument_list|,
name|TABLE_NAME_C
argument_list|,
name|i
argument_list|)
expr_stmt|;
block|}
name|waitExecutorService
argument_list|(
name|NSMALL_RUNS
operator|*
literal|2
argument_list|)
expr_stmt|;
comment|// check for the list of regions of the 3 tables
name|checkTableRegions
argument_list|(
name|stateMap
argument_list|,
name|TABLE_NAME_A
argument_list|,
name|NSMALL_RUNS
argument_list|)
expr_stmt|;
name|checkTableRegions
argument_list|(
name|stateMap
argument_list|,
name|TABLE_NAME_B
argument_list|,
name|NRUNS
argument_list|)
expr_stmt|;
name|checkTableRegions
argument_list|(
name|stateMap
argument_list|,
name|TABLE_NAME_C
argument_list|,
name|NSMALL_RUNS
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|checkTableRegions
parameter_list|(
specifier|final
name|RegionStates
name|stateMap
parameter_list|,
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|int
name|nregions
parameter_list|)
block|{
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|hris
init|=
name|stateMap
operator|.
name|getRegionsOfTable
argument_list|(
name|tableName
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|nregions
argument_list|,
name|hris
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|hris
operator|.
name|size
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
name|long
name|a
init|=
name|Bytes
operator|.
name|toLong
argument_list|(
name|hris
operator|.
name|get
argument_list|(
name|i
operator|-
literal|1
argument_list|)
operator|.
name|getStartKey
argument_list|()
argument_list|)
decl_stmt|;
name|long
name|b
init|=
name|Bytes
operator|.
name|toLong
argument_list|(
name|hris
operator|.
name|get
argument_list|(
name|i
operator|+
literal|0
argument_list|)
operator|.
name|getStartKey
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|b
argument_list|,
name|a
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|addRegionNode
parameter_list|(
specifier|final
name|RegionStates
name|stateMap
parameter_list|,
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|long
name|regionId
parameter_list|)
block|{
name|executorService
operator|.
name|submit
argument_list|(
operator|new
name|Callable
argument_list|<
name|Object
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Object
name|call
parameter_list|()
block|{
name|HRegionInfo
name|hri
init|=
operator|new
name|HRegionInfo
argument_list|(
name|tableName
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|regionId
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|regionId
operator|+
literal|1
argument_list|)
argument_list|,
literal|false
argument_list|,
literal|0
argument_list|)
decl_stmt|;
return|return
name|stateMap
operator|.
name|getOrCreateRegionNode
argument_list|(
name|hri
argument_list|)
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
specifier|private
name|Object
name|createRegionNode
parameter_list|(
specifier|final
name|RegionStates
name|stateMap
parameter_list|,
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|long
name|regionId
parameter_list|)
block|{
return|return
name|stateMap
operator|.
name|getOrCreateRegionNode
argument_list|(
name|createRegionInfo
argument_list|(
name|tableName
argument_list|,
name|regionId
argument_list|)
argument_list|)
return|;
block|}
specifier|private
name|HRegionInfo
name|createRegionInfo
parameter_list|(
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|long
name|regionId
parameter_list|)
block|{
return|return
operator|new
name|HRegionInfo
argument_list|(
name|tableName
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|regionId
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|regionId
operator|+
literal|1
argument_list|)
argument_list|,
literal|false
argument_list|,
literal|0
argument_list|)
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testPerf
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|TableName
name|TABLE_NAME
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testPerf"
argument_list|)
decl_stmt|;
specifier|final
name|int
name|NRUNS
init|=
literal|1000000
decl_stmt|;
comment|// 1M
specifier|final
name|RegionStates
name|stateMap
init|=
operator|new
name|RegionStates
argument_list|()
decl_stmt|;
name|long
name|st
init|=
name|System
operator|.
name|currentTimeMillis
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
name|NRUNS
condition|;
operator|++
name|i
control|)
block|{
specifier|final
name|int
name|regionId
init|=
name|i
decl_stmt|;
name|executorService
operator|.
name|submit
argument_list|(
operator|new
name|Callable
argument_list|<
name|Object
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Object
name|call
parameter_list|()
block|{
name|HRegionInfo
name|hri
init|=
name|createRegionInfo
argument_list|(
name|TABLE_NAME
argument_list|,
name|regionId
argument_list|)
decl_stmt|;
return|return
name|stateMap
operator|.
name|getOrCreateRegionNode
argument_list|(
name|hri
argument_list|)
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
name|waitExecutorService
argument_list|(
name|NRUNS
argument_list|)
expr_stmt|;
name|long
name|et
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"PERF STATEMAP INSERT: %s %s/sec"
argument_list|,
name|StringUtils
operator|.
name|humanTimeDiff
argument_list|(
name|et
operator|-
name|st
argument_list|)
argument_list|,
name|StringUtils
operator|.
name|humanSize
argument_list|(
name|NRUNS
operator|/
operator|(
operator|(
name|et
operator|-
name|st
operator|)
operator|/
literal|1000.0f
operator|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|st
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|NRUNS
condition|;
operator|++
name|i
control|)
block|{
specifier|final
name|int
name|regionId
init|=
name|i
decl_stmt|;
name|executorService
operator|.
name|submit
argument_list|(
operator|new
name|Callable
argument_list|<
name|Object
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Object
name|call
parameter_list|()
block|{
name|HRegionInfo
name|hri
init|=
name|createRegionInfo
argument_list|(
name|TABLE_NAME
argument_list|,
name|regionId
argument_list|)
decl_stmt|;
return|return
name|stateMap
operator|.
name|getRegionState
argument_list|(
name|hri
argument_list|)
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
name|waitExecutorService
argument_list|(
name|NRUNS
argument_list|)
expr_stmt|;
name|et
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"PERF STATEMAP GET: %s %s/sec"
argument_list|,
name|StringUtils
operator|.
name|humanTimeDiff
argument_list|(
name|et
operator|-
name|st
argument_list|)
argument_list|,
name|StringUtils
operator|.
name|humanSize
argument_list|(
name|NRUNS
operator|/
operator|(
operator|(
name|et
operator|-
name|st
operator|)
operator|/
literal|1000.0f
operator|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testPerfSingleThread
parameter_list|()
block|{
specifier|final
name|TableName
name|TABLE_NAME
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testPerf"
argument_list|)
decl_stmt|;
specifier|final
name|int
name|NRUNS
init|=
literal|1
operator|*
literal|1000000
decl_stmt|;
comment|// 1M
specifier|final
name|RegionStates
name|stateMap
init|=
operator|new
name|RegionStates
argument_list|()
decl_stmt|;
name|long
name|st
init|=
name|System
operator|.
name|currentTimeMillis
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
name|NRUNS
condition|;
operator|++
name|i
control|)
block|{
name|stateMap
operator|.
name|createRegionNode
argument_list|(
name|createRegionInfo
argument_list|(
name|TABLE_NAME
argument_list|,
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|long
name|et
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"PERF SingleThread: %s %s/sec"
argument_list|,
name|StringUtils
operator|.
name|humanTimeDiff
argument_list|(
name|et
operator|-
name|st
argument_list|)
argument_list|,
name|StringUtils
operator|.
name|humanSize
argument_list|(
name|NRUNS
operator|/
operator|(
operator|(
name|et
operator|-
name|st
operator|)
operator|/
literal|1000.0f
operator|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// ==========================================================================
comment|//  Server related
comment|// ==========================================================================
block|}
end_class

end_unit
