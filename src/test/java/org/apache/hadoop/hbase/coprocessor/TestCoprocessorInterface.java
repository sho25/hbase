begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|*
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
name|SplitTransaction
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
name|PairOfSameType
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
name|mockito
operator|.
name|Mockito
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
name|TestCoprocessorInterface
extends|extends
name|HBaseTestCase
block|{
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
specifier|static
specifier|final
name|String
name|DIR
init|=
literal|"test/build/data/TestCoprocessorInterface/"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
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
name|KeyValue
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
name|KeyValue
argument_list|>
name|result
parameter_list|,
name|int
name|limit
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
name|limit
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
block|{
return|return
name|delegate
operator|.
name|isFilterDone
argument_list|()
return|;
block|}
block|}
specifier|public
specifier|static
class|class
name|CoprocessorImpl
extends|extends
name|BaseRegionObserver
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
name|boolean
name|preSplitCalled
decl_stmt|;
specifier|private
name|boolean
name|postSplitCalled
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
name|void
name|preSplit
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|e
parameter_list|)
block|{
name|preSplitCalled
operator|=
literal|true
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|postSplit
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|e
parameter_list|,
name|HRegion
name|l
parameter_list|,
name|HRegion
name|r
parameter_list|)
block|{
name|postSplitCalled
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
name|boolean
name|wasSplit
parameter_list|()
block|{
return|return
operator|(
name|preSplitCalled
operator|&&
name|postSplitCalled
operator|)
return|;
block|}
block|}
specifier|public
name|void
name|testCoprocessorInterface
parameter_list|()
throws|throws
name|IOException
block|{
name|byte
index|[]
name|tableName
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testtable"
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
name|initSplit
argument_list|()
decl_stmt|;
name|HRegion
name|region
init|=
name|initHRegion
argument_list|(
name|tableName
argument_list|,
name|getName
argument_list|()
argument_list|,
name|hc
argument_list|,
name|CoprocessorImpl
operator|.
name|class
argument_list|,
name|families
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
literal|3
condition|;
name|i
operator|++
control|)
block|{
name|addContent
argument_list|(
name|region
argument_list|,
name|fam3
argument_list|)
expr_stmt|;
name|region
operator|.
name|flushcache
argument_list|()
expr_stmt|;
block|}
name|region
operator|.
name|compactStores
argument_list|()
expr_stmt|;
name|byte
index|[]
name|splitRow
init|=
name|region
operator|.
name|checkSplit
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|splitRow
argument_list|)
expr_stmt|;
name|HRegion
index|[]
name|regions
init|=
name|split
argument_list|(
name|region
argument_list|,
name|splitRow
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
name|regions
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|regions
index|[
name|i
index|]
operator|=
name|reopenRegion
argument_list|(
name|regions
index|[
name|i
index|]
argument_list|,
name|CoprocessorImpl
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
name|region
operator|.
name|close
argument_list|()
expr_stmt|;
name|region
operator|.
name|getLog
argument_list|()
operator|.
name|closeAndDelete
argument_list|()
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
comment|// HBASE-4197
name|Scan
name|s
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|RegionScanner
name|scanner
init|=
name|regions
index|[
literal|0
index|]
operator|.
name|getCoprocessorHost
argument_list|()
operator|.
name|postScannerOpen
argument_list|(
name|s
argument_list|,
name|regions
index|[
literal|0
index|]
operator|.
name|getScanner
argument_list|(
name|s
argument_list|)
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|scanner
operator|instanceof
name|CustomScanner
argument_list|)
expr_stmt|;
comment|// this would throw an exception before HBASE-4197
name|scanner
operator|.
name|next
argument_list|(
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
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
name|assertTrue
argument_list|(
operator|(
operator|(
name|CoprocessorImpl
operator|)
name|c
operator|)
operator|.
name|wasSplit
argument_list|()
argument_list|)
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
name|regions
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|regions
index|[
name|i
index|]
operator|.
name|close
argument_list|()
expr_stmt|;
name|regions
index|[
name|i
index|]
operator|.
name|getLog
argument_list|()
operator|.
name|closeAndDelete
argument_list|()
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
block|}
block|}
name|HRegion
name|reopenRegion
parameter_list|(
specifier|final
name|HRegion
name|closedRegion
parameter_list|,
name|Class
argument_list|<
name|?
argument_list|>
name|implClass
parameter_list|)
throws|throws
name|IOException
block|{
comment|//HRegionInfo info = new HRegionInfo(tableName, null, null, false);
name|HRegion
name|r
init|=
operator|new
name|HRegion
argument_list|(
name|closedRegion
operator|.
name|getTableDir
argument_list|()
argument_list|,
name|closedRegion
operator|.
name|getLog
argument_list|()
argument_list|,
name|closedRegion
operator|.
name|getFilesystem
argument_list|()
argument_list|,
name|closedRegion
operator|.
name|getConf
argument_list|()
argument_list|,
name|closedRegion
operator|.
name|getRegionInfo
argument_list|()
argument_list|,
name|closedRegion
operator|.
name|getTableDesc
argument_list|()
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|r
operator|.
name|initialize
argument_list|()
expr_stmt|;
comment|// this following piece is a hack. currently a coprocessorHost
comment|// is secretly loaded at OpenRegionHandler. we don't really
comment|// start a region server here, so just manually create cphost
comment|// and set it to region.
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
name|r
operator|.
name|setCoprocessorHost
argument_list|(
name|host
argument_list|)
expr_stmt|;
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
comment|// we need to manually call pre- and postOpen here since the
comment|// above load() is not the real case for CP loading. A CP is
comment|// expected to be loaded by default from 1) configuration; or 2)
comment|// HTableDescriptor. If it's loaded after HRegion initialized,
comment|// the pre- and postOpen() won't be triggered automatically.
comment|// Here we have to call pre and postOpen explicitly.
name|host
operator|.
name|preOpen
argument_list|()
expr_stmt|;
name|host
operator|.
name|postOpen
argument_list|()
expr_stmt|;
return|return
name|r
return|;
block|}
name|HRegion
name|initHRegion
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|,
name|String
name|callingMethod
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|Class
argument_list|<
name|?
argument_list|>
name|implClass
parameter_list|,
name|byte
index|[]
modifier|...
name|families
parameter_list|)
throws|throws
name|IOException
block|{
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
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
name|HRegion
name|r
init|=
name|HRegion
operator|.
name|createHRegion
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
comment|// this following piece is a hack.
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
name|r
operator|.
name|setCoprocessorHost
argument_list|(
name|host
argument_list|)
expr_stmt|;
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
comment|// Here we have to call pre and postOpen explicitly.
name|host
operator|.
name|preOpen
argument_list|()
expr_stmt|;
name|host
operator|.
name|postOpen
argument_list|()
expr_stmt|;
return|return
name|r
return|;
block|}
name|Configuration
name|initSplit
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
literal|"hbase.regionserver.lease.period"
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
return|return
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
return|;
block|}
specifier|private
name|HRegion
index|[]
name|split
parameter_list|(
specifier|final
name|HRegion
name|r
parameter_list|,
specifier|final
name|byte
index|[]
name|splitRow
parameter_list|)
throws|throws
name|IOException
block|{
name|HRegion
index|[]
name|regions
init|=
operator|new
name|HRegion
index|[
literal|2
index|]
decl_stmt|;
name|SplitTransaction
name|st
init|=
operator|new
name|SplitTransaction
argument_list|(
name|r
argument_list|,
name|splitRow
argument_list|)
decl_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
if|if
condition|(
operator|!
name|st
operator|.
name|prepare
argument_list|()
condition|)
block|{
comment|// test fails.
name|assertTrue
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|Server
name|mockServer
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|Server
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|mockServer
operator|.
name|getConfiguration
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|PairOfSameType
argument_list|<
name|HRegion
argument_list|>
name|daughters
init|=
name|st
operator|.
name|execute
argument_list|(
name|mockServer
argument_list|,
literal|null
argument_list|)
decl_stmt|;
for|for
control|(
name|HRegion
name|each_daughter
range|:
name|daughters
control|)
block|{
name|regions
index|[
name|i
index|]
operator|=
name|each_daughter
expr_stmt|;
name|i
operator|++
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Split transaction of "
operator|+
name|r
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
literal|" failed:"
operator|+
name|ioe
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RuntimeException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Failed rollback of failed split of "
operator|+
name|r
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|assertTrue
argument_list|(
name|i
operator|==
literal|2
argument_list|)
expr_stmt|;
return|return
name|regions
return|;
block|}
block|}
end_class

end_unit

