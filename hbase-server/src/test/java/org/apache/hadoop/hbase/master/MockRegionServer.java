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
name|Random
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeMap
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
name|ConcurrentSkipListMap
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
name|ServerName
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
name|exceptions
operator|.
name|ZooKeeperConnectionException
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
name|catalog
operator|.
name|CatalogTracker
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
name|AdminProtocol
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
name|ClientProtocol
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
name|executor
operator|.
name|ExecutorService
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
name|ipc
operator|.
name|RpcServer
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
name|TableLockManager
operator|.
name|NullTableLockManager
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
operator|.
name|CloseRegionRequest
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
operator|.
name|CloseRegionResponse
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
operator|.
name|CompactRegionRequest
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
operator|.
name|CompactRegionResponse
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
operator|.
name|FlushRegionRequest
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
operator|.
name|FlushRegionResponse
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
operator|.
name|GetOnlineRegionRequest
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
operator|.
name|GetOnlineRegionResponse
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
operator|.
name|GetRegionInfoRequest
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
operator|.
name|GetRegionInfoResponse
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
operator|.
name|GetServerInfoRequest
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
operator|.
name|GetServerInfoResponse
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
operator|.
name|GetStoreFileRequest
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
operator|.
name|GetStoreFileResponse
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
operator|.
name|OpenRegionRequest
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
operator|.
name|OpenRegionResponse
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
operator|.
name|ReplicateWALEntryRequest
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
operator|.
name|ReplicateWALEntryResponse
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
operator|.
name|RollWALWriterRequest
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
operator|.
name|RollWALWriterResponse
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
operator|.
name|SplitRegionRequest
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
operator|.
name|SplitRegionResponse
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
operator|.
name|StopServerRequest
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
operator|.
name|StopServerResponse
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
name|protobuf
operator|.
name|generated
operator|.
name|ClientProtos
operator|.
name|BulkLoadHFileRequest
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
operator|.
name|BulkLoadHFileResponse
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
operator|.
name|GetRequest
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
operator|.
name|GetResponse
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
operator|.
name|MultiGetRequest
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
operator|.
name|MultiGetResponse
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
operator|.
name|MultiRequest
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
operator|.
name|MutateRequest
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
operator|.
name|MutateResponse
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
operator|.
name|ScanRequest
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
operator|.
name|ScanResponse
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
name|CompactionRequestor
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
name|FlushRequester
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
name|Leases
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
name|RegionServerAccounting
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
name|wal
operator|.
name|HLog
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
name|zookeeper
operator|.
name|ZooKeeperWatcher
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|KeeperException
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
name|RpcController
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
comment|/**  * A mock RegionServer implementation.  * Use this when you can't bend Mockito to your liking (e.g. return null result  * when 'scanning' until master timesout and then return a coherent meta row  * result thereafter.  Have some facility for faking gets and scans.  See  * {@link #setGetResult(byte[], byte[], Result)} for how to fill the backing data  * store that the get pulls from.  */
end_comment

begin_class
class|class
name|MockRegionServer
implements|implements
name|AdminProtocol
implements|,
name|ClientProtocol
implements|,
name|RegionServerServices
block|{
specifier|private
specifier|final
name|ServerName
name|sn
decl_stmt|;
specifier|private
specifier|final
name|ZooKeeperWatcher
name|zkw
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|final
name|Random
name|random
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
comment|/**    * Map of regions to map of rows and {@link Results}.  Used as data source when    * {@link MockRegionServer#get(byte[], Get)} is called. Because we have a byte    * key, need to use TreeMap and provide a Comparator.  Use    * {@link #setGetResult(byte[], byte[], Result)} filling this map.    */
specifier|private
specifier|final
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|Result
argument_list|>
argument_list|>
name|gets
init|=
operator|new
name|TreeMap
argument_list|<
name|byte
index|[]
argument_list|,
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|Result
argument_list|>
argument_list|>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
decl_stmt|;
comment|/**    * Map of regions to results to return when scanning.    */
specifier|private
specifier|final
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|Result
index|[]
argument_list|>
name|nexts
init|=
operator|new
name|TreeMap
argument_list|<
name|byte
index|[]
argument_list|,
name|Result
index|[]
argument_list|>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
decl_stmt|;
comment|/**    * Data structure that holds regionname and index used scanning.    */
class|class
name|RegionNameAndIndex
block|{
specifier|private
specifier|final
name|byte
index|[]
name|regionName
decl_stmt|;
specifier|private
name|int
name|index
init|=
literal|0
decl_stmt|;
name|RegionNameAndIndex
parameter_list|(
specifier|final
name|byte
index|[]
name|regionName
parameter_list|)
block|{
name|this
operator|.
name|regionName
operator|=
name|regionName
expr_stmt|;
block|}
name|byte
index|[]
name|getRegionName
parameter_list|()
block|{
return|return
name|this
operator|.
name|regionName
return|;
block|}
name|int
name|getThenIncrement
parameter_list|()
block|{
name|int
name|currentIndex
init|=
name|this
operator|.
name|index
decl_stmt|;
name|this
operator|.
name|index
operator|++
expr_stmt|;
return|return
name|currentIndex
return|;
block|}
block|}
comment|/**    * Outstanding scanners and their offset into<code>nexts</code>    */
specifier|private
specifier|final
name|Map
argument_list|<
name|Long
argument_list|,
name|RegionNameAndIndex
argument_list|>
name|scannersAndOffsets
init|=
operator|new
name|HashMap
argument_list|<
name|Long
argument_list|,
name|RegionNameAndIndex
argument_list|>
argument_list|()
decl_stmt|;
comment|/**    * @param sn Name of this mock regionserver    * @throws IOException    * @throws org.apache.hadoop.hbase.exceptions.ZooKeeperConnectionException    */
name|MockRegionServer
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|ServerName
name|sn
parameter_list|)
throws|throws
name|ZooKeeperConnectionException
throws|,
name|IOException
block|{
name|this
operator|.
name|sn
operator|=
name|sn
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|zkw
operator|=
operator|new
name|ZooKeeperWatcher
argument_list|(
name|conf
argument_list|,
name|sn
operator|.
name|toString
argument_list|()
argument_list|,
name|this
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
comment|/**    * Use this method filling the backing data source used by {@link #get(byte[], Get)}    * @param regionName    * @param row    * @param r    */
name|void
name|setGetResult
parameter_list|(
specifier|final
name|byte
index|[]
name|regionName
parameter_list|,
specifier|final
name|byte
index|[]
name|row
parameter_list|,
specifier|final
name|Result
name|r
parameter_list|)
block|{
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|Result
argument_list|>
name|value
init|=
name|this
operator|.
name|gets
operator|.
name|get
argument_list|(
name|regionName
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
comment|// If no value already, create one.  Needs to be treemap because we are
comment|// using byte array as key.   Not thread safe.
name|value
operator|=
operator|new
name|TreeMap
argument_list|<
name|byte
index|[]
argument_list|,
name|Result
argument_list|>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
expr_stmt|;
name|this
operator|.
name|gets
operator|.
name|put
argument_list|(
name|regionName
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
name|value
operator|.
name|put
argument_list|(
name|row
argument_list|,
name|r
argument_list|)
expr_stmt|;
block|}
comment|/**    * Use this method to set what a scanner will reply as we next through    * @param regionName    * @param rs    */
name|void
name|setNextResults
parameter_list|(
specifier|final
name|byte
index|[]
name|regionName
parameter_list|,
specifier|final
name|Result
index|[]
name|rs
parameter_list|)
block|{
name|this
operator|.
name|nexts
operator|.
name|put
argument_list|(
name|regionName
argument_list|,
name|rs
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isStopped
parameter_list|()
block|{
comment|// TODO Auto-generated method stub
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|abort
parameter_list|(
name|String
name|why
parameter_list|,
name|Throwable
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|this
operator|.
name|sn
operator|+
literal|": "
operator|+
name|why
argument_list|,
name|e
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isAborted
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
specifier|public
name|long
name|openScanner
parameter_list|(
name|byte
index|[]
name|regionName
parameter_list|,
name|Scan
name|scan
parameter_list|)
throws|throws
name|IOException
block|{
name|long
name|scannerId
init|=
name|this
operator|.
name|random
operator|.
name|nextLong
argument_list|()
decl_stmt|;
name|this
operator|.
name|scannersAndOffsets
operator|.
name|put
argument_list|(
name|scannerId
argument_list|,
operator|new
name|RegionNameAndIndex
argument_list|(
name|regionName
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|scannerId
return|;
block|}
specifier|public
name|Result
name|next
parameter_list|(
name|long
name|scannerId
parameter_list|)
throws|throws
name|IOException
block|{
name|RegionNameAndIndex
name|rnai
init|=
name|this
operator|.
name|scannersAndOffsets
operator|.
name|get
argument_list|(
name|scannerId
argument_list|)
decl_stmt|;
name|int
name|index
init|=
name|rnai
operator|.
name|getThenIncrement
argument_list|()
decl_stmt|;
name|Result
index|[]
name|results
init|=
name|this
operator|.
name|nexts
operator|.
name|get
argument_list|(
name|rnai
operator|.
name|getRegionName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|results
operator|==
literal|null
condition|)
return|return
literal|null
return|;
return|return
name|index
operator|<
name|results
operator|.
name|length
condition|?
name|results
index|[
name|index
index|]
else|:
literal|null
return|;
block|}
specifier|public
name|Result
index|[]
name|next
parameter_list|(
name|long
name|scannerId
parameter_list|,
name|int
name|numberOfRows
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Just return one result whatever they ask for.
name|Result
name|r
init|=
name|next
argument_list|(
name|scannerId
argument_list|)
decl_stmt|;
return|return
name|r
operator|==
literal|null
condition|?
literal|null
else|:
operator|new
name|Result
index|[]
block|{
name|r
block|}
return|;
block|}
specifier|public
name|void
name|close
parameter_list|(
specifier|final
name|long
name|scannerId
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|scannersAndOffsets
operator|.
name|remove
argument_list|(
name|scannerId
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|(
name|String
name|why
parameter_list|)
block|{
name|this
operator|.
name|zkw
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|addToOnlineRegions
parameter_list|(
name|HRegion
name|r
parameter_list|)
block|{
comment|// TODO Auto-generated method stub
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|removeFromOnlineRegions
parameter_list|(
name|HRegion
name|r
parameter_list|,
name|ServerName
name|destination
parameter_list|)
block|{
comment|// TODO Auto-generated method stub
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|HRegion
name|getFromOnlineRegions
parameter_list|(
name|String
name|encodedRegionName
parameter_list|)
block|{
comment|// TODO Auto-generated method stub
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|Configuration
name|getConfiguration
parameter_list|()
block|{
return|return
name|this
operator|.
name|conf
return|;
block|}
annotation|@
name|Override
specifier|public
name|ZooKeeperWatcher
name|getZooKeeper
parameter_list|()
block|{
return|return
name|this
operator|.
name|zkw
return|;
block|}
annotation|@
name|Override
specifier|public
name|CatalogTracker
name|getCatalogTracker
parameter_list|()
block|{
comment|// TODO Auto-generated method stub
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|ServerName
name|getServerName
parameter_list|()
block|{
return|return
name|this
operator|.
name|sn
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isStopping
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|CompactionRequestor
name|getCompactionRequester
parameter_list|()
block|{
comment|// TODO Auto-generated method stub
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|FlushRequester
name|getFlushRequester
parameter_list|()
block|{
comment|// TODO Auto-generated method stub
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|RegionServerAccounting
name|getRegionServerAccounting
parameter_list|()
block|{
comment|// TODO Auto-generated method stub
return|return
literal|null
return|;
block|}
specifier|public
name|TableLockManager
name|getTableLockManager
parameter_list|()
block|{
return|return
operator|new
name|NullTableLockManager
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|postOpenDeployTasks
parameter_list|(
name|HRegion
name|r
parameter_list|,
name|CatalogTracker
name|ct
parameter_list|)
throws|throws
name|KeeperException
throws|,
name|IOException
block|{
comment|// TODO Auto-generated method stub
block|}
annotation|@
name|Override
specifier|public
name|RpcServer
name|getRpcServer
parameter_list|()
block|{
comment|// TODO Auto-generated method stub
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|ConcurrentSkipListMap
argument_list|<
name|byte
index|[]
argument_list|,
name|Boolean
argument_list|>
name|getRegionsInTransitionInRS
parameter_list|()
block|{
comment|// TODO Auto-generated method stub
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|FileSystem
name|getFileSystem
parameter_list|()
block|{
comment|// TODO Auto-generated method stub
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|GetResponse
name|get
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|GetRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
name|byte
index|[]
name|regionName
init|=
name|request
operator|.
name|getRegion
argument_list|()
operator|.
name|getValue
argument_list|()
operator|.
name|toByteArray
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|Result
argument_list|>
name|m
init|=
name|this
operator|.
name|gets
operator|.
name|get
argument_list|(
name|regionName
argument_list|)
decl_stmt|;
name|GetResponse
operator|.
name|Builder
name|builder
init|=
name|GetResponse
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
name|m
operator|!=
literal|null
condition|)
block|{
name|byte
index|[]
name|row
init|=
name|request
operator|.
name|getGet
argument_list|()
operator|.
name|getRow
argument_list|()
operator|.
name|toByteArray
argument_list|()
decl_stmt|;
name|builder
operator|.
name|setResult
argument_list|(
name|ProtobufUtil
operator|.
name|toResult
argument_list|(
name|m
operator|.
name|get
argument_list|(
name|row
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|builder
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|MultiGetResponse
name|multiGet
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|MultiGetRequest
name|requests
parameter_list|)
throws|throws
name|ServiceException
block|{
name|byte
index|[]
name|regionName
init|=
name|requests
operator|.
name|getRegion
argument_list|()
operator|.
name|getValue
argument_list|()
operator|.
name|toByteArray
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|Result
argument_list|>
name|m
init|=
name|this
operator|.
name|gets
operator|.
name|get
argument_list|(
name|regionName
argument_list|)
decl_stmt|;
name|MultiGetResponse
operator|.
name|Builder
name|builder
init|=
name|MultiGetResponse
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
name|m
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|ClientProtos
operator|.
name|Get
name|get
range|:
name|requests
operator|.
name|getGetList
argument_list|()
control|)
block|{
name|byte
index|[]
name|row
init|=
name|get
operator|.
name|getRow
argument_list|()
operator|.
name|toByteArray
argument_list|()
decl_stmt|;
name|builder
operator|.
name|addResult
argument_list|(
name|ProtobufUtil
operator|.
name|toResult
argument_list|(
name|m
operator|.
name|get
argument_list|(
name|row
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|builder
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|MutateResponse
name|mutate
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|MutateRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
comment|// TODO Auto-generated method stub
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|ScanResponse
name|scan
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|ScanRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
name|ScanResponse
operator|.
name|Builder
name|builder
init|=
name|ScanResponse
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
try|try
block|{
if|if
condition|(
name|request
operator|.
name|hasScan
argument_list|()
condition|)
block|{
name|byte
index|[]
name|regionName
init|=
name|request
operator|.
name|getRegion
argument_list|()
operator|.
name|getValue
argument_list|()
operator|.
name|toByteArray
argument_list|()
decl_stmt|;
name|builder
operator|.
name|setScannerId
argument_list|(
name|openScanner
argument_list|(
name|regionName
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setMoreResults
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|long
name|scannerId
init|=
name|request
operator|.
name|getScannerId
argument_list|()
decl_stmt|;
name|Result
name|result
init|=
name|next
argument_list|(
name|scannerId
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|addResult
argument_list|(
name|ProtobufUtil
operator|.
name|toResult
argument_list|(
name|result
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setMoreResults
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|builder
operator|.
name|setMoreResults
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|close
argument_list|(
name|scannerId
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|ie
parameter_list|)
block|{
throw|throw
operator|new
name|ServiceException
argument_list|(
name|ie
argument_list|)
throw|;
block|}
return|return
name|builder
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|BulkLoadHFileResponse
name|bulkLoadHFile
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|BulkLoadHFileRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
comment|// TODO Auto-generated method stub
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|ClientProtos
operator|.
name|CoprocessorServiceResponse
name|execService
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|ClientProtos
operator|.
name|CoprocessorServiceRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
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
operator|.
name|MultiResponse
name|multi
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|MultiRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
comment|// TODO Auto-generated method stub
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|GetRegionInfoResponse
name|getRegionInfo
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|GetRegionInfoRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
name|GetRegionInfoResponse
operator|.
name|Builder
name|builder
init|=
name|GetRegionInfoResponse
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|setRegionInfo
argument_list|(
name|HRegionInfo
operator|.
name|convert
argument_list|(
name|HRegionInfo
operator|.
name|FIRST_META_REGIONINFO
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|builder
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|GetStoreFileResponse
name|getStoreFile
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|GetStoreFileRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
comment|// TODO Auto-generated method stub
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|GetOnlineRegionResponse
name|getOnlineRegion
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|GetOnlineRegionRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
comment|// TODO Auto-generated method stub
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|OpenRegionResponse
name|openRegion
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|OpenRegionRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
comment|// TODO Auto-generated method stub
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|CloseRegionResponse
name|closeRegion
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|CloseRegionRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
comment|// TODO Auto-generated method stub
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|FlushRegionResponse
name|flushRegion
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|FlushRegionRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
comment|// TODO Auto-generated method stub
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|SplitRegionResponse
name|splitRegion
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|SplitRegionRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
comment|// TODO Auto-generated method stub
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|CompactRegionResponse
name|compactRegion
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|CompactRegionRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
comment|// TODO Auto-generated method stub
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|ReplicateWALEntryResponse
name|replicateWALEntry
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|ReplicateWALEntryRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
comment|// TODO Auto-generated method stub
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|RollWALWriterResponse
name|rollWALWriter
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|RollWALWriterRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
comment|// TODO Auto-generated method stub
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|GetServerInfoResponse
name|getServerInfo
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|GetServerInfoRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
comment|// TODO Auto-generated method stub
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|StopServerResponse
name|stopServer
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|StopServerRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
comment|// TODO Auto-generated method stub
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|HRegion
argument_list|>
name|getOnlineRegions
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
comment|// TODO Auto-generated method stub
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|Leases
name|getLeases
parameter_list|()
block|{
comment|// TODO Auto-generated method stub
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|HLog
name|getWAL
parameter_list|(
name|HRegionInfo
name|regionInfo
parameter_list|)
throws|throws
name|IOException
block|{
comment|// TODO Auto-generated method stub
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|ExecutorService
name|getExecutorService
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit

