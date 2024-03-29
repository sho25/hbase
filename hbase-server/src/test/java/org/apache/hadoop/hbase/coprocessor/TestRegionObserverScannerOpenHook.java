begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|Collections
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
name|Optional
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
name|FileSystem
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
name|Admin
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
name|CoprocessorDescriptorBuilder
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
name|TableDescriptor
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
name|filter
operator|.
name|FilterBase
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
name|ChunkCreator
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
name|FlushLifeCycleTracker
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
name|MemStoreLABImpl
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
name|RegionServerServices
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
name|StoreScanner
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
name|compactions
operator|.
name|CompactionContext
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
name|compactions
operator|.
name|CompactionLifeCycleTracker
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
name|compactions
operator|.
name|CompactionRequest
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
name|throttle
operator|.
name|ThroughputController
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
name|security
operator|.
name|User
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
name|wal
operator|.
name|WAL
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

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|CoprocessorTests
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
name|TestRegionObserverScannerOpenHook
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
name|TestRegionObserverScannerOpenHook
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|HBaseTestingUtility
name|UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|static
specifier|final
name|Path
name|DIR
init|=
name|UTIL
operator|.
name|getDataTestDir
argument_list|()
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
specifier|public
specifier|static
class|class
name|NoDataFilter
extends|extends
name|FilterBase
block|{
annotation|@
name|Override
specifier|public
name|ReturnCode
name|filterCell
parameter_list|(
specifier|final
name|Cell
name|ignored
parameter_list|)
block|{
return|return
name|ReturnCode
operator|.
name|SKIP
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|filterAllRemaining
parameter_list|()
throws|throws
name|IOException
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|filterRow
parameter_list|()
throws|throws
name|IOException
block|{
return|return
literal|true
return|;
block|}
block|}
comment|/**    * Do the default logic in {@link RegionObserver} interface.    */
specifier|public
specifier|static
class|class
name|EmptyRegionObsever
implements|implements
name|RegionCoprocessor
implements|,
name|RegionObserver
block|{
annotation|@
name|Override
specifier|public
name|Optional
argument_list|<
name|RegionObserver
argument_list|>
name|getRegionObserver
parameter_list|()
block|{
return|return
name|Optional
operator|.
name|of
argument_list|(
name|this
argument_list|)
return|;
block|}
block|}
comment|/**    * Don't return any data from a scan by creating a custom {@link StoreScanner}.    */
specifier|public
specifier|static
class|class
name|NoDataFromScan
implements|implements
name|RegionCoprocessor
implements|,
name|RegionObserver
block|{
annotation|@
name|Override
specifier|public
name|Optional
argument_list|<
name|RegionObserver
argument_list|>
name|getRegionObserver
parameter_list|()
block|{
return|return
name|Optional
operator|.
name|of
argument_list|(
name|this
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|preGetOp
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|c
parameter_list|,
name|Get
name|get
parameter_list|,
name|List
argument_list|<
name|Cell
argument_list|>
name|result
parameter_list|)
throws|throws
name|IOException
block|{
name|c
operator|.
name|bypass
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|preScannerOpen
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|c
parameter_list|,
name|Scan
name|scan
parameter_list|)
throws|throws
name|IOException
block|{
name|scan
operator|.
name|setFilter
argument_list|(
operator|new
name|NoDataFilter
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
specifier|final
name|InternalScanner
name|NO_DATA
init|=
operator|new
name|InternalScanner
argument_list|()
block|{
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
literal|false
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
block|{}
block|}
decl_stmt|;
comment|/**    * Don't allow any data in a flush by creating a custom {@link StoreScanner}.    */
specifier|public
specifier|static
class|class
name|NoDataFromFlush
implements|implements
name|RegionCoprocessor
implements|,
name|RegionObserver
block|{
annotation|@
name|Override
specifier|public
name|Optional
argument_list|<
name|RegionObserver
argument_list|>
name|getRegionObserver
parameter_list|()
block|{
return|return
name|Optional
operator|.
name|of
argument_list|(
name|this
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|InternalScanner
name|preFlush
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|c
parameter_list|,
name|Store
name|store
parameter_list|,
name|InternalScanner
name|scanner
parameter_list|,
name|FlushLifeCycleTracker
name|tracker
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|NO_DATA
return|;
block|}
block|}
comment|/**    * Don't allow any data to be written out in the compaction by creating a custom    * {@link StoreScanner}.    */
specifier|public
specifier|static
class|class
name|NoDataFromCompaction
implements|implements
name|RegionCoprocessor
implements|,
name|RegionObserver
block|{
annotation|@
name|Override
specifier|public
name|Optional
argument_list|<
name|RegionObserver
argument_list|>
name|getRegionObserver
parameter_list|()
block|{
return|return
name|Optional
operator|.
name|of
argument_list|(
name|this
argument_list|)
return|;
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
name|c
parameter_list|,
name|Store
name|store
parameter_list|,
name|InternalScanner
name|scanner
parameter_list|,
name|ScanType
name|scanType
parameter_list|,
name|CompactionLifeCycleTracker
name|tracker
parameter_list|,
name|CompactionRequest
name|request
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|NO_DATA
return|;
block|}
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
name|byte
index|[]
modifier|...
name|families
parameter_list|)
throws|throws
name|IOException
block|{
name|TableDescriptorBuilder
operator|.
name|ModifyableTableDescriptor
name|tableDescriptor
init|=
operator|new
name|TableDescriptorBuilder
operator|.
name|ModifyableTableDescriptor
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableName
argument_list|)
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
name|tableDescriptor
operator|.
name|setColumnFamily
argument_list|(
operator|new
name|ColumnFamilyDescriptorBuilder
operator|.
name|ModifyableColumnFamilyDescriptor
argument_list|(
name|family
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|ChunkCreator
operator|.
name|initialize
argument_list|(
name|MemStoreLABImpl
operator|.
name|CHUNK_SIZE_DEFAULT
argument_list|,
literal|false
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|HRegionInfo
name|info
init|=
operator|new
name|HRegionInfo
argument_list|(
name|tableDescriptor
operator|.
name|getTableName
argument_list|()
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
name|WAL
name|wal
init|=
name|HBaseTestingUtility
operator|.
name|createWal
argument_list|(
name|conf
argument_list|,
name|path
argument_list|,
name|info
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
name|tableDescriptor
argument_list|,
name|wal
argument_list|)
decl_stmt|;
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
return|return
name|r
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRegionObserverScanTimeStacking
parameter_list|()
throws|throws
name|Exception
block|{
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
name|byte
index|[]
name|TABLE
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|byte
index|[]
name|A
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"A"
argument_list|)
decl_stmt|;
name|byte
index|[]
index|[]
name|FAMILIES
init|=
operator|new
name|byte
index|[]
index|[]
block|{
name|A
block|}
decl_stmt|;
comment|// Use new HTU to not overlap with the DFS cluster started in #CompactionStacking
name|Configuration
name|conf
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|HRegion
name|region
init|=
name|initHRegion
argument_list|(
name|TABLE
argument_list|,
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|conf
argument_list|,
name|FAMILIES
argument_list|)
decl_stmt|;
name|RegionCoprocessorHost
name|h
init|=
name|region
operator|.
name|getCoprocessorHost
argument_list|()
decl_stmt|;
name|h
operator|.
name|load
argument_list|(
name|NoDataFromScan
operator|.
name|class
argument_list|,
name|Coprocessor
operator|.
name|PRIORITY_HIGHEST
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|h
operator|.
name|load
argument_list|(
name|EmptyRegionObsever
operator|.
name|class
argument_list|,
name|Coprocessor
operator|.
name|PRIORITY_USER
argument_list|,
name|conf
argument_list|)
expr_stmt|;
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
name|addColumn
argument_list|(
name|A
argument_list|,
name|A
argument_list|,
name|A
argument_list|)
expr_stmt|;
name|region
operator|.
name|put
argument_list|(
name|put
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
name|Result
name|r
init|=
name|region
operator|.
name|get
argument_list|(
name|get
argument_list|)
decl_stmt|;
name|assertNull
argument_list|(
literal|"Got an unexpected number of rows - no data should be returned with the NoDataFromScan coprocessor. Found: "
operator|+
name|r
argument_list|,
name|r
operator|.
name|listCells
argument_list|()
argument_list|)
expr_stmt|;
name|HBaseTestingUtility
operator|.
name|closeRegionAndWAL
argument_list|(
name|region
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRegionObserverFlushTimeStacking
parameter_list|()
throws|throws
name|Exception
block|{
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
name|byte
index|[]
name|TABLE
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|byte
index|[]
name|A
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"A"
argument_list|)
decl_stmt|;
name|byte
index|[]
index|[]
name|FAMILIES
init|=
operator|new
name|byte
index|[]
index|[]
block|{
name|A
block|}
decl_stmt|;
comment|// Use new HTU to not overlap with the DFS cluster started in #CompactionStacking
name|Configuration
name|conf
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|HRegion
name|region
init|=
name|initHRegion
argument_list|(
name|TABLE
argument_list|,
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|conf
argument_list|,
name|FAMILIES
argument_list|)
decl_stmt|;
name|RegionCoprocessorHost
name|h
init|=
name|region
operator|.
name|getCoprocessorHost
argument_list|()
decl_stmt|;
name|h
operator|.
name|load
argument_list|(
name|NoDataFromFlush
operator|.
name|class
argument_list|,
name|Coprocessor
operator|.
name|PRIORITY_HIGHEST
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|h
operator|.
name|load
argument_list|(
name|EmptyRegionObsever
operator|.
name|class
argument_list|,
name|Coprocessor
operator|.
name|PRIORITY_USER
argument_list|,
name|conf
argument_list|)
expr_stmt|;
comment|// put a row and flush it to disk
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
name|addColumn
argument_list|(
name|A
argument_list|,
name|A
argument_list|,
name|A
argument_list|)
expr_stmt|;
name|region
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|region
operator|.
name|flush
argument_list|(
literal|true
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
name|Result
name|r
init|=
name|region
operator|.
name|get
argument_list|(
name|get
argument_list|)
decl_stmt|;
name|assertNull
argument_list|(
literal|"Got an unexpected number of rows - no data should be returned with the NoDataFromScan coprocessor. Found: "
operator|+
name|r
argument_list|,
name|r
operator|.
name|listCells
argument_list|()
argument_list|)
expr_stmt|;
name|HBaseTestingUtility
operator|.
name|closeRegionAndWAL
argument_list|(
name|region
argument_list|)
expr_stmt|;
block|}
comment|/*    * Custom HRegion which uses CountDownLatch to signal the completion of compaction    */
specifier|public
specifier|static
class|class
name|CompactionCompletionNotifyingRegion
extends|extends
name|HRegion
block|{
specifier|private
specifier|static
specifier|volatile
name|CountDownLatch
name|compactionStateChangeLatch
init|=
literal|null
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
specifier|public
name|CompactionCompletionNotifyingRegion
parameter_list|(
name|Path
name|tableDir
parameter_list|,
name|WAL
name|log
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|Configuration
name|confParam
parameter_list|,
name|RegionInfo
name|info
parameter_list|,
name|TableDescriptor
name|htd
parameter_list|,
name|RegionServerServices
name|rsServices
parameter_list|)
block|{
name|super
argument_list|(
name|tableDir
argument_list|,
name|log
argument_list|,
name|fs
argument_list|,
name|confParam
argument_list|,
name|info
argument_list|,
name|htd
argument_list|,
name|rsServices
argument_list|)
expr_stmt|;
block|}
specifier|public
name|CountDownLatch
name|getCompactionStateChangeLatch
parameter_list|()
block|{
if|if
condition|(
name|compactionStateChangeLatch
operator|==
literal|null
condition|)
name|compactionStateChangeLatch
operator|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
expr_stmt|;
return|return
name|compactionStateChangeLatch
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|compact
parameter_list|(
name|CompactionContext
name|compaction
parameter_list|,
name|HStore
name|store
parameter_list|,
name|ThroughputController
name|throughputController
parameter_list|)
throws|throws
name|IOException
block|{
name|boolean
name|ret
init|=
name|super
operator|.
name|compact
argument_list|(
name|compaction
argument_list|,
name|store
argument_list|,
name|throughputController
argument_list|)
decl_stmt|;
if|if
condition|(
name|ret
condition|)
name|compactionStateChangeLatch
operator|.
name|countDown
argument_list|()
expr_stmt|;
return|return
name|ret
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|compact
parameter_list|(
name|CompactionContext
name|compaction
parameter_list|,
name|HStore
name|store
parameter_list|,
name|ThroughputController
name|throughputController
parameter_list|,
name|User
name|user
parameter_list|)
throws|throws
name|IOException
block|{
name|boolean
name|ret
init|=
name|super
operator|.
name|compact
argument_list|(
name|compaction
argument_list|,
name|store
argument_list|,
name|throughputController
argument_list|,
name|user
argument_list|)
decl_stmt|;
if|if
condition|(
name|ret
condition|)
name|compactionStateChangeLatch
operator|.
name|countDown
argument_list|()
expr_stmt|;
return|return
name|ret
return|;
block|}
block|}
comment|/**    * Unfortunately, the easiest way to test this is to spin up a mini-cluster since we want to do    * the usual compaction mechanism on the region, rather than going through the backdoor to the    * region    */
annotation|@
name|Test
specifier|public
name|void
name|testRegionObserverCompactionTimeStacking
parameter_list|()
throws|throws
name|Exception
block|{
comment|// setup a mini cluster so we can do a real compaction on a region
name|Configuration
name|conf
init|=
name|UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|conf
operator|.
name|setClass
argument_list|(
name|HConstants
operator|.
name|REGION_IMPL
argument_list|,
name|CompactionCompletionNotifyingRegion
operator|.
name|class
argument_list|,
name|HRegion
operator|.
name|class
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.hstore.compaction.min"
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
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
name|byte
index|[]
name|A
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"A"
argument_list|)
decl_stmt|;
name|TableDescriptorBuilder
operator|.
name|ModifyableTableDescriptor
name|tableDescriptor
init|=
operator|new
name|TableDescriptorBuilder
operator|.
name|ModifyableTableDescriptor
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|tableDescriptor
operator|.
name|setColumnFamily
argument_list|(
operator|new
name|ColumnFamilyDescriptorBuilder
operator|.
name|ModifyableColumnFamilyDescriptor
argument_list|(
name|A
argument_list|)
argument_list|)
expr_stmt|;
name|tableDescriptor
operator|.
name|setCoprocessor
argument_list|(
name|CoprocessorDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|EmptyRegionObsever
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|setJarPath
argument_list|(
literal|null
argument_list|)
operator|.
name|setPriority
argument_list|(
name|Coprocessor
operator|.
name|PRIORITY_USER
argument_list|)
operator|.
name|setProperties
argument_list|(
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|tableDescriptor
operator|.
name|setCoprocessor
argument_list|(
name|CoprocessorDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|NoDataFromCompaction
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|setJarPath
argument_list|(
literal|null
argument_list|)
operator|.
name|setPriority
argument_list|(
name|Coprocessor
operator|.
name|PRIORITY_HIGHEST
argument_list|)
operator|.
name|setProperties
argument_list|(
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|Admin
name|admin
init|=
name|UTIL
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|tableDescriptor
argument_list|)
expr_stmt|;
name|Table
name|table
init|=
name|UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|tableDescriptor
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
comment|// put a row and flush it to disk
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
name|addColumn
argument_list|(
name|A
argument_list|,
name|A
argument_list|,
name|A
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|HRegionServer
name|rs
init|=
name|UTIL
operator|.
name|getRSForFirstRegionInTable
argument_list|(
name|tableDescriptor
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|HRegion
argument_list|>
name|regions
init|=
name|rs
operator|.
name|getRegions
argument_list|(
name|tableDescriptor
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"More than 1 region serving test table with 1 row"
argument_list|,
literal|1
argument_list|,
name|regions
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Region
name|region
init|=
name|regions
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|admin
operator|.
name|flushRegion
argument_list|(
name|region
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionName
argument_list|()
argument_list|)
expr_stmt|;
name|CountDownLatch
name|latch
init|=
operator|(
operator|(
name|CompactionCompletionNotifyingRegion
operator|)
name|region
operator|)
operator|.
name|getCompactionStateChangeLatch
argument_list|()
decl_stmt|;
comment|// put another row and flush that too
name|put
operator|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"anotherrow"
argument_list|)
argument_list|)
expr_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|A
argument_list|,
name|A
argument_list|,
name|A
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|admin
operator|.
name|flushRegion
argument_list|(
name|region
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionName
argument_list|()
argument_list|)
expr_stmt|;
comment|// run a compaction, which normally would should get rid of the data
comment|// wait for the compaction checker to complete
name|latch
operator|.
name|await
argument_list|()
expr_stmt|;
comment|// check both rows to ensure that they aren't there
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
name|ROW
argument_list|)
decl_stmt|;
name|Result
name|r
init|=
name|table
operator|.
name|get
argument_list|(
name|get
argument_list|)
decl_stmt|;
name|assertNull
argument_list|(
literal|"Got an unexpected number of rows - no data should be returned with the NoDataFromScan coprocessor. Found: "
operator|+
name|r
argument_list|,
name|r
operator|.
name|listCells
argument_list|()
argument_list|)
expr_stmt|;
name|get
operator|=
operator|new
name|Get
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"anotherrow"
argument_list|)
argument_list|)
expr_stmt|;
name|r
operator|=
name|table
operator|.
name|get
argument_list|(
name|get
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
literal|"Got an unexpected number of rows - no data should be returned with the NoDataFromScan coprocessor Found: "
operator|+
name|r
argument_list|,
name|r
operator|.
name|listCells
argument_list|()
argument_list|)
expr_stmt|;
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
name|UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

