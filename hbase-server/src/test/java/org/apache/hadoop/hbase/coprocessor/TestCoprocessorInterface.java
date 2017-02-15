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
name|coprocessor
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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|HBaseTestingUtility
operator|.
name|fam2
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
name|HBaseTestingUtility
operator|.
name|fam3
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
name|assertFalse
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
name|ArrayList
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
name|ConcurrentMap
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
name|fs
operator|.
name|Path
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
name|Coprocessor
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
name|CoprocessorEnvironment
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
name|HBaseTestCase
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
name|InternalScanner
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
name|RegionCoprocessorHost
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
name|RegionScanner
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
name|ScanType
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
name|ScannerContext
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
name|regionserver
operator|.
name|StoreFile
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
name|CoprocessorTests
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

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|CoprocessorTests
operator|.
name|class
block|,
name|SmallTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestCoprocessorInterface
block|{
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
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestCoprocessorInterface
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|TEST_UTIL
init|=
name|HBaseTestingUtility
operator|.
name|createLocalHTU
argument_list|()
decl_stmt|;
specifier|static
specifier|final
name|Path
name|DIR
init|=
name|TEST_UTIL
operator|.
name|getDataTestDir
argument_list|()
decl_stmt|;
specifier|private
specifier|static
class|class
name|CustomScanner
implements|implements
name|RegionScanner
block|{
specifier|private
name|RegionScanner
name|delegate
decl_stmt|;
specifier|public
name|CustomScanner
parameter_list|(
name|RegionScanner
name|delegate
parameter_list|)
block|{
name|this
operator|.
name|delegate
operator|=
name|delegate
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|next
parameter_list|(
name|List
argument_list|<
name|Cell
argument_list|>
name|results
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|delegate
operator|.
name|next
argument_list|(
name|results
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|next
parameter_list|(
name|List
argument_list|<
name|Cell
argument_list|>
name|result
parameter_list|,
name|ScannerContext
name|scannerContext
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|delegate
operator|.
name|next
argument_list|(
name|result
argument_list|,
name|scannerContext
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|nextRaw
parameter_list|(
name|List
argument_list|<
name|Cell
argument_list|>
name|result
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|delegate
operator|.
name|nextRaw
argument_list|(
name|result
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|nextRaw
parameter_list|(
name|List
argument_list|<
name|Cell
argument_list|>
name|result
parameter_list|,
name|ScannerContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|delegate
operator|.
name|nextRaw
argument_list|(
name|result
argument_list|,
name|context
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|delegate
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|HRegionInfo
name|getRegionInfo
parameter_list|()
block|{
return|return
name|delegate
operator|.
name|getRegionInfo
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isFilterDone
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|delegate
operator|.
name|isFilterDone
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|reseek
parameter_list|(
name|byte
index|[]
name|row
parameter_list|)
throws|throws
name|IOException
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getMaxResultSize
parameter_list|()
block|{
return|return
name|delegate
operator|.
name|getMaxResultSize
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getMvccReadPoint
parameter_list|()
block|{
return|return
name|delegate
operator|.
name|getMvccReadPoint
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getBatch
parameter_list|()
block|{
return|return
name|delegate
operator|.
name|getBatch
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|shipped
parameter_list|()
throws|throws
name|IOException
block|{
name|this
operator|.
name|delegate
operator|.
name|shipped
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
class|class
name|CoprocessorImpl
implements|implements
name|RegionObserver
block|{
specifier|private
name|boolean
name|startCalled
decl_stmt|;
specifier|private
name|boolean
name|stopCalled
decl_stmt|;
specifier|private
name|boolean
name|preOpenCalled
decl_stmt|;
specifier|private
name|boolean
name|postOpenCalled
decl_stmt|;
specifier|private
name|boolean
name|preCloseCalled
decl_stmt|;
specifier|private
name|boolean
name|postCloseCalled
decl_stmt|;
specifier|private
name|boolean
name|preCompactCalled
decl_stmt|;
specifier|private
name|boolean
name|postCompactCalled
decl_stmt|;
specifier|private
name|boolean
name|preFlushCalled
decl_stmt|;
specifier|private
name|boolean
name|postFlushCalled
decl_stmt|;
specifier|private
name|ConcurrentMap
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|sharedData
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|start
parameter_list|(
name|CoprocessorEnvironment
name|e
parameter_list|)
block|{
name|sharedData
operator|=
operator|(
operator|(
name|RegionCoprocessorEnvironment
operator|)
name|e
operator|)
operator|.
name|getSharedData
argument_list|()
expr_stmt|;
comment|// using new String here, so that there will be new object on each invocation
name|sharedData
operator|.
name|putIfAbsent
argument_list|(
literal|"test1"
argument_list|,
operator|new
name|Object
argument_list|()
argument_list|)
expr_stmt|;
name|startCalled
operator|=
literal|true
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|(
name|CoprocessorEnvironment
name|e
parameter_list|)
block|{
name|sharedData
operator|=
literal|null
expr_stmt|;
name|stopCalled
operator|=
literal|true
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|preOpen
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|e
parameter_list|)
block|{
name|preOpenCalled
operator|=
literal|true
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|postOpen
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|e
parameter_list|)
block|{
name|postOpenCalled
operator|=
literal|true
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|preClose
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|e
parameter_list|,
name|boolean
name|abortRequested
parameter_list|)
block|{
name|preCloseCalled
operator|=
literal|true
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|postClose
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|e
parameter_list|,
name|boolean
name|abortRequested
parameter_list|)
block|{
name|postCloseCalled
operator|=
literal|true
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|InternalScanner
name|preCompact
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|e
parameter_list|,
name|Store
name|store
parameter_list|,
name|InternalScanner
name|scanner
parameter_list|,
name|ScanType
name|scanType
parameter_list|)
block|{
name|preCompactCalled
operator|=
literal|true
expr_stmt|;
return|return
name|scanner
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|postCompact
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|e
parameter_list|,
name|Store
name|store
parameter_list|,
name|StoreFile
name|resultFile
parameter_list|)
block|{
name|postCompactCalled
operator|=
literal|true
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|preFlush
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|e
parameter_list|)
block|{
name|preFlushCalled
operator|=
literal|true
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|postFlush
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|e
parameter_list|)
block|{
name|postFlushCalled
operator|=
literal|true
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|RegionScanner
name|postScannerOpen
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|e
parameter_list|,
specifier|final
name|Scan
name|scan
parameter_list|,
specifier|final
name|RegionScanner
name|s
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|CustomScanner
argument_list|(
name|s
argument_list|)
return|;
block|}
name|boolean
name|wasStarted
parameter_list|()
block|{
return|return
name|startCalled
return|;
block|}
name|boolean
name|wasStopped
parameter_list|()
block|{
return|return
name|stopCalled
return|;
block|}
name|boolean
name|wasOpened
parameter_list|()
block|{
return|return
operator|(
name|preOpenCalled
operator|&&
name|postOpenCalled
operator|)
return|;
block|}
name|boolean
name|wasClosed
parameter_list|()
block|{
return|return
operator|(
name|preCloseCalled
operator|&&
name|postCloseCalled
operator|)
return|;
block|}
name|boolean
name|wasFlushed
parameter_list|()
block|{
return|return
operator|(
name|preFlushCalled
operator|&&
name|postFlushCalled
operator|)
return|;
block|}
name|boolean
name|wasCompacted
parameter_list|()
block|{
return|return
operator|(
name|preCompactCalled
operator|&&
name|postCompactCalled
operator|)
return|;
block|}
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|getSharedData
parameter_list|()
block|{
return|return
name|sharedData
return|;
block|}
block|}
specifier|public
specifier|static
class|class
name|CoprocessorII
implements|implements
name|RegionObserver
block|{
specifier|private
name|ConcurrentMap
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|sharedData
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|start
parameter_list|(
name|CoprocessorEnvironment
name|e
parameter_list|)
block|{
name|sharedData
operator|=
operator|(
operator|(
name|RegionCoprocessorEnvironment
operator|)
name|e
operator|)
operator|.
name|getSharedData
argument_list|()
expr_stmt|;
name|sharedData
operator|.
name|putIfAbsent
argument_list|(
literal|"test2"
argument_list|,
operator|new
name|Object
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|(
name|CoprocessorEnvironment
name|e
parameter_list|)
block|{
name|sharedData
operator|=
literal|null
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|preGetOp
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|e
parameter_list|,
specifier|final
name|Get
name|get
parameter_list|,
specifier|final
name|List
argument_list|<
name|Cell
argument_list|>
name|results
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
literal|1
operator|/
literal|0
operator|==
literal|1
condition|)
block|{
name|e
operator|.
name|complete
argument_list|()
expr_stmt|;
block|}
block|}
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|getSharedData
parameter_list|()
block|{
return|return
name|sharedData
return|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSharedData
parameter_list|()
throws|throws
name|IOException
block|{
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
name|byte
index|[]
index|[]
name|families
init|=
block|{
name|fam1
block|,
name|fam2
block|,
name|fam3
block|}
decl_stmt|;
name|Configuration
name|hc
init|=
name|initConfig
argument_list|()
decl_stmt|;
name|Region
name|region
init|=
name|initHRegion
argument_list|(
name|tableName
argument_list|,
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|,
name|hc
argument_list|,
operator|new
name|Class
argument_list|<
name|?
argument_list|>
index|[]
block|{}
operator|,
name|families
block|)
function|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|3
condition|;
name|i
operator|++
control|)
block|{
name|HBaseTestCase
operator|.
name|addContent
argument_list|(
name|region
argument_list|,
name|fam3
argument_list|)
expr_stmt|;
name|region
operator|.
name|flush
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
name|region
operator|.
name|compact
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|region
operator|=
name|reopenRegion
argument_list|(
name|region
argument_list|,
name|CoprocessorImpl
operator|.
name|class
argument_list|,
name|CoprocessorII
operator|.
name|class
argument_list|)
expr_stmt|;
name|Coprocessor
name|c
init|=
name|region
operator|.
name|getCoprocessorHost
argument_list|()
operator|.
name|findCoprocessor
argument_list|(
name|CoprocessorImpl
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|Coprocessor
name|c2
init|=
name|region
operator|.
name|getCoprocessorHost
argument_list|()
operator|.
name|findCoprocessor
argument_list|(
name|CoprocessorII
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|Object
name|o
init|=
operator|(
operator|(
name|CoprocessorImpl
operator|)
name|c
operator|)
operator|.
name|getSharedData
argument_list|()
operator|.
name|get
argument_list|(
literal|"test1"
argument_list|)
decl_stmt|;
name|Object
name|o2
init|=
operator|(
operator|(
name|CoprocessorII
operator|)
name|c2
operator|)
operator|.
name|getSharedData
argument_list|()
operator|.
name|get
argument_list|(
literal|"test2"
argument_list|)
decl_stmt|;
name|assertNotNull
parameter_list|(
name|o
parameter_list|)
constructor_decl|;
name|assertNotNull
parameter_list|(
name|o2
parameter_list|)
constructor_decl|;
comment|// to coprocessors get different sharedDatas
name|assertFalse
argument_list|(
operator|(
operator|(
name|CoprocessorImpl
operator|)
name|c
operator|)
operator|.
name|getSharedData
argument_list|()
operator|==
operator|(
operator|(
name|CoprocessorII
operator|)
name|c2
operator|)
operator|.
name|getSharedData
argument_list|()
argument_list|)
expr_stmt|;
name|c
operator|=
name|region
operator|.
name|getCoprocessorHost
argument_list|()
operator|.
name|findCoprocessor
argument_list|(
name|CoprocessorImpl
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|c2
operator|=
name|region
operator|.
name|getCoprocessorHost
argument_list|()
operator|.
name|findCoprocessor
argument_list|(
name|CoprocessorII
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
comment|// make sure that all coprocessor of a class have identical sharedDatas
name|assertTrue
argument_list|(
operator|(
operator|(
name|CoprocessorImpl
operator|)
name|c
operator|)
operator|.
name|getSharedData
argument_list|()
operator|.
name|get
argument_list|(
literal|"test1"
argument_list|)
operator|==
name|o
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
operator|(
operator|(
name|CoprocessorII
operator|)
name|c2
operator|)
operator|.
name|getSharedData
argument_list|()
operator|.
name|get
argument_list|(
literal|"test2"
argument_list|)
operator|==
name|o2
argument_list|)
expr_stmt|;
comment|// now have all Environments fail
try|try
block|{
name|byte
index|[]
name|r
init|=
name|region
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getStartKey
argument_list|()
decl_stmt|;
if|if
condition|(
name|r
operator|==
literal|null
operator|||
name|r
operator|.
name|length
operator|<=
literal|0
condition|)
block|{
comment|// Its the start row.  Can't ask for null.  Ask for minimal key instead.
name|r
operator|=
operator|new
name|byte
index|[]
block|{
literal|0
block|}
expr_stmt|;
block|}
name|Get
name|g
init|=
operator|new
name|Get
argument_list|(
name|r
argument_list|)
decl_stmt|;
name|region
operator|.
name|get
argument_list|(
name|g
argument_list|)
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|DoNotRetryIOException
name|xc
parameter_list|)
block|{     }
name|assertNull
argument_list|(
name|region
operator|.
name|getCoprocessorHost
argument_list|()
operator|.
name|findCoprocessor
argument_list|(
name|CoprocessorII
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|c
operator|=
name|region
operator|.
name|getCoprocessorHost
argument_list|()
operator|.
name|findCoprocessor
argument_list|(
name|CoprocessorImpl
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
operator|(
operator|(
name|CoprocessorImpl
operator|)
name|c
operator|)
operator|.
name|getSharedData
argument_list|()
operator|.
name|get
argument_list|(
literal|"test1"
argument_list|)
operator|==
name|o
argument_list|)
expr_stmt|;
name|c
operator|=
name|c2
operator|=
literal|null
expr_stmt|;
comment|// perform a GC
name|System
operator|.
name|gc
parameter_list|()
constructor_decl|;
comment|// reopen the region
name|region
operator|=
name|reopenRegion
argument_list|(
name|region
argument_list|,
name|CoprocessorImpl
operator|.
name|class
argument_list|,
name|CoprocessorII
operator|.
name|class
argument_list|)
expr_stmt|;
name|c
operator|=
name|region
operator|.
name|getCoprocessorHost
argument_list|()
operator|.
name|findCoprocessor
argument_list|(
name|CoprocessorImpl
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
comment|// CPimpl is unaffected, still the same reference
name|assertTrue
argument_list|(
operator|(
operator|(
name|CoprocessorImpl
operator|)
name|c
operator|)
operator|.
name|getSharedData
argument_list|()
operator|.
name|get
argument_list|(
literal|"test1"
argument_list|)
operator|==
name|o
argument_list|)
expr_stmt|;
name|c2
operator|=
name|region
operator|.
name|getCoprocessorHost
argument_list|()
operator|.
name|findCoprocessor
argument_list|(
name|CoprocessorII
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
comment|// new map and object created, hence the reference is different
comment|// hence the old entry was indeed removed by the GC and new one has been created
name|Object
name|o3
init|=
operator|(
operator|(
name|CoprocessorII
operator|)
name|c2
operator|)
operator|.
name|getSharedData
argument_list|()
operator|.
name|get
argument_list|(
literal|"test2"
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|o3
operator|==
name|o2
argument_list|)
expr_stmt|;
name|HBaseTestingUtility
operator|.
name|closeRegionAndWAL
parameter_list|(
name|region
parameter_list|)
constructor_decl|;
block|}
end_class

begin_function
annotation|@
name|Test
specifier|public
name|void
name|testCoprocessorInterface
parameter_list|()
throws|throws
name|IOException
block|{
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
name|byte
index|[]
index|[]
name|families
init|=
block|{
name|fam1
block|,
name|fam2
block|,
name|fam3
block|}
decl_stmt|;
name|Configuration
name|hc
init|=
name|initConfig
argument_list|()
decl_stmt|;
name|Region
name|region
init|=
name|initHRegion
argument_list|(
name|tableName
argument_list|,
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|,
name|hc
argument_list|,
operator|new
name|Class
argument_list|<
name|?
argument_list|>
index|[]
block|{
name|CoprocessorImpl
operator|.
name|class
block|}
operator|,
name|families
block|)
function|;
end_function

begin_for
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|3
condition|;
name|i
operator|++
control|)
block|{
name|HBaseTestCase
operator|.
name|addContent
argument_list|(
name|region
argument_list|,
name|fam3
argument_list|)
expr_stmt|;
name|region
operator|.
name|flush
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
end_for

begin_expr_stmt
name|region
operator|.
name|compact
argument_list|(
literal|false
argument_list|)
expr_stmt|;
end_expr_stmt

begin_comment
comment|// HBASE-4197
end_comment

begin_decl_stmt
name|Scan
name|s
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
end_decl_stmt

begin_decl_stmt
name|RegionScanner
name|scanner
init|=
name|region
operator|.
name|getCoprocessorHost
argument_list|()
operator|.
name|postScannerOpen
argument_list|(
name|s
argument_list|,
name|region
operator|.
name|getScanner
argument_list|(
name|s
argument_list|)
argument_list|)
decl_stmt|;
end_decl_stmt

begin_expr_stmt
name|assertTrue
argument_list|(
name|scanner
operator|instanceof
name|CustomScanner
argument_list|)
expr_stmt|;
end_expr_stmt

begin_comment
comment|// this would throw an exception before HBASE-4197
end_comment

begin_expr_stmt
name|scanner
operator|.
name|next
argument_list|(
operator|new
name|ArrayList
argument_list|<
name|Cell
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
end_expr_stmt

begin_expr_stmt
name|HBaseTestingUtility
operator|.
name|closeRegionAndWAL
argument_list|(
name|region
argument_list|)
expr_stmt|;
end_expr_stmt

begin_decl_stmt
name|Coprocessor
name|c
init|=
name|region
operator|.
name|getCoprocessorHost
argument_list|()
operator|.
name|findCoprocessor
argument_list|(
name|CoprocessorImpl
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
end_decl_stmt

begin_expr_stmt
name|assertTrue
argument_list|(
literal|"Coprocessor not started"
argument_list|,
operator|(
operator|(
name|CoprocessorImpl
operator|)
name|c
operator|)
operator|.
name|wasStarted
argument_list|()
argument_list|)
expr_stmt|;
end_expr_stmt

begin_expr_stmt
name|assertTrue
argument_list|(
literal|"Coprocessor not stopped"
argument_list|,
operator|(
operator|(
name|CoprocessorImpl
operator|)
name|c
operator|)
operator|.
name|wasStopped
argument_list|()
argument_list|)
expr_stmt|;
end_expr_stmt

begin_expr_stmt
name|assertTrue
argument_list|(
operator|(
operator|(
name|CoprocessorImpl
operator|)
name|c
operator|)
operator|.
name|wasOpened
argument_list|()
argument_list|)
expr_stmt|;
end_expr_stmt

begin_expr_stmt
name|assertTrue
argument_list|(
operator|(
operator|(
name|CoprocessorImpl
operator|)
name|c
operator|)
operator|.
name|wasClosed
argument_list|()
argument_list|)
expr_stmt|;
end_expr_stmt

begin_expr_stmt
name|assertTrue
argument_list|(
operator|(
operator|(
name|CoprocessorImpl
operator|)
name|c
operator|)
operator|.
name|wasFlushed
argument_list|()
argument_list|)
expr_stmt|;
end_expr_stmt

begin_expr_stmt
name|assertTrue
argument_list|(
operator|(
operator|(
name|CoprocessorImpl
operator|)
name|c
operator|)
operator|.
name|wasCompacted
argument_list|()
argument_list|)
expr_stmt|;
end_expr_stmt

begin_expr_stmt
unit|}    Region
name|reopenRegion
argument_list|(
name|final
name|Region
name|closedRegion
argument_list|,
name|Class
argument_list|<
name|?
argument_list|>
operator|...
name|implClasses
argument_list|)
throws|throws
name|IOException
block|{
comment|//HRegionInfo info = new HRegionInfo(tableName, null, null, false);
name|Region
name|r
operator|=
name|HRegion
operator|.
name|openHRegion
argument_list|(
name|closedRegion
argument_list|,
literal|null
argument_list|)
expr_stmt|;
end_expr_stmt

begin_comment
comment|// this following piece is a hack. currently a coprocessorHost
end_comment

begin_comment
comment|// is secretly loaded at OpenRegionHandler. we don't really
end_comment

begin_comment
comment|// start a region server here, so just manually create cphost
end_comment

begin_comment
comment|// and set it to region.
end_comment

begin_decl_stmt
name|Configuration
name|conf
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
end_decl_stmt

begin_decl_stmt
name|RegionCoprocessorHost
name|host
init|=
operator|new
name|RegionCoprocessorHost
argument_list|(
name|r
argument_list|,
literal|null
argument_list|,
name|conf
argument_list|)
decl_stmt|;
end_decl_stmt

begin_expr_stmt
operator|(
operator|(
name|HRegion
operator|)
name|r
operator|)
operator|.
name|setCoprocessorHost
argument_list|(
name|host
argument_list|)
expr_stmt|;
end_expr_stmt

begin_for
for|for
control|(
name|Class
argument_list|<
name|?
argument_list|>
name|implClass
range|:
name|implClasses
control|)
block|{
name|host
operator|.
name|load
argument_list|(
name|implClass
argument_list|,
name|Coprocessor
operator|.
name|PRIORITY_USER
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
end_for

begin_comment
comment|// we need to manually call pre- and postOpen here since the
end_comment

begin_comment
comment|// above load() is not the real case for CP loading. A CP is
end_comment

begin_comment
comment|// expected to be loaded by default from 1) configuration; or 2)
end_comment

begin_comment
comment|// HTableDescriptor. If it's loaded after HRegion initialized,
end_comment

begin_comment
comment|// the pre- and postOpen() won't be triggered automatically.
end_comment

begin_comment
comment|// Here we have to call pre and postOpen explicitly.
end_comment

begin_expr_stmt
name|host
operator|.
name|preOpen
argument_list|()
expr_stmt|;
end_expr_stmt

begin_expr_stmt
name|host
operator|.
name|postOpen
argument_list|()
expr_stmt|;
end_expr_stmt

begin_return
return|return
name|r
return|;
end_return

begin_expr_stmt
unit|}    Region
name|initHRegion
argument_list|(
name|TableName
name|tableName
argument_list|,
name|String
name|callingMethod
argument_list|,
name|Configuration
name|conf
argument_list|,
name|Class
argument_list|<
name|?
argument_list|>
index|[]
name|implClasses
operator|,
name|byte
index|[]
index|[]
name|families
argument_list|)
throws|throws
name|IOException
block|{
name|HTableDescriptor
name|htd
operator|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
end_expr_stmt

begin_for
for|for
control|(
name|byte
index|[]
name|family
range|:
name|families
control|)
block|{
name|htd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|family
argument_list|)
argument_list|)
expr_stmt|;
block|}
end_for

begin_decl_stmt
name|HRegionInfo
name|info
init|=
operator|new
name|HRegionInfo
argument_list|(
name|tableName
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
decl_stmt|;
end_decl_stmt

begin_decl_stmt
name|Path
name|path
init|=
operator|new
name|Path
argument_list|(
name|DIR
operator|+
name|callingMethod
argument_list|)
decl_stmt|;
end_decl_stmt

begin_decl_stmt
name|Region
name|r
init|=
name|HBaseTestingUtility
operator|.
name|createRegionAndWAL
argument_list|(
name|info
argument_list|,
name|path
argument_list|,
name|conf
argument_list|,
name|htd
argument_list|)
decl_stmt|;
end_decl_stmt

begin_comment
comment|// this following piece is a hack.
end_comment

begin_decl_stmt
name|RegionCoprocessorHost
name|host
init|=
operator|new
name|RegionCoprocessorHost
argument_list|(
name|r
argument_list|,
literal|null
argument_list|,
name|conf
argument_list|)
decl_stmt|;
end_decl_stmt

begin_expr_stmt
operator|(
operator|(
name|HRegion
operator|)
name|r
operator|)
operator|.
name|setCoprocessorHost
argument_list|(
name|host
argument_list|)
expr_stmt|;
end_expr_stmt

begin_for
for|for
control|(
name|Class
argument_list|<
name|?
argument_list|>
name|implClass
range|:
name|implClasses
control|)
block|{
name|host
operator|.
name|load
argument_list|(
name|implClass
argument_list|,
name|Coprocessor
operator|.
name|PRIORITY_USER
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|Coprocessor
name|c
init|=
name|host
operator|.
name|findCoprocessor
argument_list|(
name|implClass
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|c
argument_list|)
expr_stmt|;
block|}
end_for

begin_comment
comment|// Here we have to call pre and postOpen explicitly.
end_comment

begin_expr_stmt
name|host
operator|.
name|preOpen
argument_list|()
expr_stmt|;
end_expr_stmt

begin_expr_stmt
name|host
operator|.
name|postOpen
argument_list|()
expr_stmt|;
end_expr_stmt

begin_return
return|return
name|r
return|;
end_return

begin_function
unit|}    private
name|Configuration
name|initConfig
parameter_list|()
block|{
comment|// Always compact if there is more than one store file.
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"hbase.hstore.compactionThreshold"
argument_list|,
literal|2
argument_list|)
expr_stmt|;
comment|// Make lease timeout longer, lease checks less frequent
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"hbase.master.lease.thread.wakefrequency"
argument_list|,
literal|5
operator|*
literal|1000
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_SCANNER_TIMEOUT_PERIOD
argument_list|,
literal|10
operator|*
literal|1000
argument_list|)
expr_stmt|;
comment|// Increase the amount of time between client retries
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setLong
argument_list|(
literal|"hbase.client.pause"
argument_list|,
literal|15
operator|*
literal|1000
argument_list|)
expr_stmt|;
comment|// This size should make it so we always split using the addContent
comment|// below.  After adding all data, the first region is 1.3M
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setLong
argument_list|(
name|HConstants
operator|.
name|HREGION_MAX_FILESIZE
argument_list|,
literal|1024
operator|*
literal|128
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setBoolean
argument_list|(
literal|"hbase.testing.nocluster"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setBoolean
argument_list|(
name|CoprocessorHost
operator|.
name|ABORT_ON_ERROR_KEY
argument_list|,
literal|false
argument_list|)
expr_stmt|;
return|return
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
return|;
block|}
end_function

unit|}
end_unit

