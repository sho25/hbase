begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**   * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|zookeeper
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
name|io
operator|.
name|InterruptedIOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|UnsupportedEncodingException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URLDecoder
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URLEncoder
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
name|hbase
operator|.
name|classification
operator|.
name|InterfaceAudience
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
name|exceptions
operator|.
name|DeserializationException
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
name|ZooKeeperProtos
operator|.
name|RegionStoreSequenceIds
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

begin_comment
comment|/**  * Common methods and attributes used by {@link org.apache.hadoop.hbase.master.SplitLogManager}   * and {@link org.apache.hadoop.hbase.regionserver.SplitLogWorker}  * running distributed splitting of WAL logs.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ZKSplitLog
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
name|ZKSplitLog
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Gets the full path node name for the log file being split.    * This method will url encode the filename.    * @param zkw zk reference    * @param filename log file name (only the basename)    */
specifier|public
specifier|static
name|String
name|getEncodedNodeName
parameter_list|(
name|ZooKeeperWatcher
name|zkw
parameter_list|,
name|String
name|filename
parameter_list|)
block|{
return|return
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|zkw
operator|.
name|splitLogZNode
argument_list|,
name|encode
argument_list|(
name|filename
argument_list|)
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|String
name|getFileName
parameter_list|(
name|String
name|node
parameter_list|)
block|{
name|String
name|basename
init|=
name|node
operator|.
name|substring
argument_list|(
name|node
operator|.
name|lastIndexOf
argument_list|(
literal|'/'
argument_list|)
operator|+
literal|1
argument_list|)
decl_stmt|;
return|return
name|decode
argument_list|(
name|basename
argument_list|)
return|;
block|}
specifier|static
name|String
name|encode
parameter_list|(
name|String
name|s
parameter_list|)
block|{
try|try
block|{
return|return
name|URLEncoder
operator|.
name|encode
argument_list|(
name|s
argument_list|,
literal|"UTF-8"
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|UnsupportedEncodingException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"URLENCODER doesn't support UTF-8"
argument_list|)
throw|;
block|}
block|}
specifier|static
name|String
name|decode
parameter_list|(
name|String
name|s
parameter_list|)
block|{
try|try
block|{
return|return
name|URLDecoder
operator|.
name|decode
argument_list|(
name|s
argument_list|,
literal|"UTF-8"
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|UnsupportedEncodingException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"URLDecoder doesn't support UTF-8"
argument_list|)
throw|;
block|}
block|}
specifier|public
specifier|static
name|String
name|getRescanNode
parameter_list|(
name|ZooKeeperWatcher
name|zkw
parameter_list|)
block|{
return|return
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|zkw
operator|.
name|splitLogZNode
argument_list|,
literal|"RESCAN"
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|boolean
name|isRescanNode
parameter_list|(
name|ZooKeeperWatcher
name|zkw
parameter_list|,
name|String
name|path
parameter_list|)
block|{
name|String
name|prefix
init|=
name|getRescanNode
argument_list|(
name|zkw
argument_list|)
decl_stmt|;
if|if
condition|(
name|path
operator|.
name|length
argument_list|()
operator|<=
name|prefix
operator|.
name|length
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|prefix
operator|.
name|length
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|prefix
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
operator|!=
name|path
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
return|return
literal|true
return|;
block|}
specifier|public
specifier|static
name|boolean
name|isTaskPath
parameter_list|(
name|ZooKeeperWatcher
name|zkw
parameter_list|,
name|String
name|path
parameter_list|)
block|{
name|String
name|dirname
init|=
name|path
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|path
operator|.
name|lastIndexOf
argument_list|(
literal|'/'
argument_list|)
argument_list|)
decl_stmt|;
return|return
name|dirname
operator|.
name|equals
argument_list|(
name|zkw
operator|.
name|splitLogZNode
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|Path
name|getSplitLogDir
parameter_list|(
name|Path
name|rootdir
parameter_list|,
name|String
name|tmpname
parameter_list|)
block|{
return|return
operator|new
name|Path
argument_list|(
operator|new
name|Path
argument_list|(
name|rootdir
argument_list|,
name|HConstants
operator|.
name|SPLIT_LOGDIR_NAME
argument_list|)
argument_list|,
name|tmpname
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|String
name|getSplitLogDirTmpComponent
parameter_list|(
specifier|final
name|String
name|worker
parameter_list|,
name|String
name|file
parameter_list|)
block|{
return|return
name|worker
operator|+
literal|"_"
operator|+
name|ZKSplitLog
operator|.
name|encode
argument_list|(
name|file
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|void
name|markCorrupted
parameter_list|(
name|Path
name|rootdir
parameter_list|,
name|String
name|logFileName
parameter_list|,
name|FileSystem
name|fs
parameter_list|)
block|{
name|Path
name|file
init|=
operator|new
name|Path
argument_list|(
name|getSplitLogDir
argument_list|(
name|rootdir
argument_list|,
name|logFileName
argument_list|)
argument_list|,
literal|"corrupt"
argument_list|)
decl_stmt|;
try|try
block|{
name|fs
operator|.
name|createNewFile
argument_list|(
name|file
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Could not flag a log file as corrupted. Failed to create "
operator|+
name|file
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
name|boolean
name|isCorrupted
parameter_list|(
name|Path
name|rootdir
parameter_list|,
name|String
name|logFileName
parameter_list|,
name|FileSystem
name|fs
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|file
init|=
operator|new
name|Path
argument_list|(
name|getSplitLogDir
argument_list|(
name|rootdir
argument_list|,
name|logFileName
argument_list|)
argument_list|,
literal|"corrupt"
argument_list|)
decl_stmt|;
name|boolean
name|isCorrupt
decl_stmt|;
name|isCorrupt
operator|=
name|fs
operator|.
name|exists
argument_list|(
name|file
argument_list|)
expr_stmt|;
return|return
name|isCorrupt
return|;
block|}
comment|/*    * Following methods come from SplitLogManager    */
comment|/**    * check if /hbase/recovering-regions/<current region encoded name> exists. Returns true if exists    * and set watcher as well.    * @param zkw    * @param regionEncodedName region encode name    * @return true when /hbase/recovering-regions/<current region encoded name> exists    * @throws KeeperException    */
specifier|public
specifier|static
name|boolean
name|isRegionMarkedRecoveringInZK
parameter_list|(
name|ZooKeeperWatcher
name|zkw
parameter_list|,
name|String
name|regionEncodedName
parameter_list|)
throws|throws
name|KeeperException
block|{
name|boolean
name|result
init|=
literal|false
decl_stmt|;
name|String
name|nodePath
init|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|zkw
operator|.
name|recoveringRegionsZNode
argument_list|,
name|regionEncodedName
argument_list|)
decl_stmt|;
name|byte
index|[]
name|node
init|=
name|ZKUtil
operator|.
name|getDataAndWatch
argument_list|(
name|zkw
argument_list|,
name|nodePath
argument_list|)
decl_stmt|;
if|if
condition|(
name|node
operator|!=
literal|null
condition|)
block|{
name|result
operator|=
literal|true
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
comment|/**    * @param bytes - Content of a failed region server or recovering region znode.    * @return long - The last flushed sequence Id for the region server    */
specifier|public
specifier|static
name|long
name|parseLastFlushedSequenceIdFrom
parameter_list|(
specifier|final
name|byte
index|[]
name|bytes
parameter_list|)
block|{
name|long
name|lastRecordedFlushedSequenceId
init|=
operator|-
literal|1l
decl_stmt|;
try|try
block|{
name|lastRecordedFlushedSequenceId
operator|=
name|ZKUtil
operator|.
name|parseWALPositionFrom
argument_list|(
name|bytes
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|DeserializationException
name|e
parameter_list|)
block|{
name|lastRecordedFlushedSequenceId
operator|=
operator|-
literal|1l
expr_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
literal|"Can't parse last flushed sequence Id"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
return|return
name|lastRecordedFlushedSequenceId
return|;
block|}
specifier|public
specifier|static
name|void
name|deleteRecoveringRegionZNodes
parameter_list|(
name|ZooKeeperWatcher
name|watcher
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|regions
parameter_list|)
block|{
try|try
block|{
if|if
condition|(
name|regions
operator|==
literal|null
condition|)
block|{
comment|// remove all children under /home/recovering-regions
name|LOG
operator|.
name|debug
argument_list|(
literal|"Garbage collecting all recovering region znodes"
argument_list|)
expr_stmt|;
name|ZKUtil
operator|.
name|deleteChildrenRecursively
argument_list|(
name|watcher
argument_list|,
name|watcher
operator|.
name|recoveringRegionsZNode
argument_list|)
expr_stmt|;
block|}
else|else
block|{
for|for
control|(
name|String
name|curRegion
range|:
name|regions
control|)
block|{
name|String
name|nodePath
init|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|watcher
operator|.
name|recoveringRegionsZNode
argument_list|,
name|curRegion
argument_list|)
decl_stmt|;
name|ZKUtil
operator|.
name|deleteNodeRecursively
argument_list|(
name|watcher
argument_list|,
name|nodePath
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Cannot remove recovering regions from ZooKeeper"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * This function is used in distributedLogReplay to fetch last flushed sequence id from ZK    * @param zkw    * @param serverName    * @param encodedRegionName    * @return the last flushed sequence ids recorded in ZK of the region for<code>serverName<code>    * @throws IOException    */
specifier|public
specifier|static
name|RegionStoreSequenceIds
name|getRegionFlushedSequenceId
parameter_list|(
name|ZooKeeperWatcher
name|zkw
parameter_list|,
name|String
name|serverName
parameter_list|,
name|String
name|encodedRegionName
parameter_list|)
throws|throws
name|IOException
block|{
comment|// when SplitLogWorker recovers a region by directly replaying unflushed WAL edits,
comment|// last flushed sequence Id changes when newly assigned RS flushes writes to the region.
comment|// If the newly assigned RS fails again(a chained RS failures scenario), the last flushed
comment|// sequence Id name space (sequence Id only valid for a particular RS instance), changes
comment|// when different newly assigned RS flushes the region.
comment|// Therefore, in this mode we need to fetch last sequence Ids from ZK where we keep history of
comment|// last flushed sequence Id for each failed RS instance.
name|RegionStoreSequenceIds
name|result
init|=
literal|null
decl_stmt|;
name|String
name|nodePath
init|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|zkw
operator|.
name|recoveringRegionsZNode
argument_list|,
name|encodedRegionName
argument_list|)
decl_stmt|;
name|nodePath
operator|=
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|nodePath
argument_list|,
name|serverName
argument_list|)
expr_stmt|;
try|try
block|{
name|byte
index|[]
name|data
decl_stmt|;
try|try
block|{
name|data
operator|=
name|ZKUtil
operator|.
name|getData
argument_list|(
name|zkw
argument_list|,
name|nodePath
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|InterruptedIOException
argument_list|()
throw|;
block|}
if|if
condition|(
name|data
operator|!=
literal|null
condition|)
block|{
name|result
operator|=
name|ZKUtil
operator|.
name|parseRegionStoreSequenceIds
argument_list|(
name|data
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Cannot get lastFlushedSequenceId from ZooKeeper for server="
operator|+
name|serverName
operator|+
literal|"; region="
operator|+
name|encodedRegionName
argument_list|,
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|DeserializationException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Can't parse last flushed sequence Id from znode:"
operator|+
name|nodePath
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
block|}
end_class

end_unit

