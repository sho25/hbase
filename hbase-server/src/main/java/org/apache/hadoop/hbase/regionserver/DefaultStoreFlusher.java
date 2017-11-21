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
name|regionserver
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
name|yetus
operator|.
name|audience
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
name|monitoring
operator|.
name|MonitoredTask
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
name|util
operator|.
name|StringUtils
import|;
end_import

begin_comment
comment|/**  * Default implementation of StoreFlusher.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|DefaultStoreFlusher
extends|extends
name|StoreFlusher
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
name|DefaultStoreFlusher
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|Object
name|flushLock
init|=
operator|new
name|Object
argument_list|()
decl_stmt|;
specifier|public
name|DefaultStoreFlusher
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|HStore
name|store
parameter_list|)
block|{
name|super
argument_list|(
name|conf
argument_list|,
name|store
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|Path
argument_list|>
name|flushSnapshot
parameter_list|(
name|MemStoreSnapshot
name|snapshot
parameter_list|,
name|long
name|cacheFlushId
parameter_list|,
name|MonitoredTask
name|status
parameter_list|,
name|ThroughputController
name|throughputController
parameter_list|,
name|FlushLifeCycleTracker
name|tracker
parameter_list|)
throws|throws
name|IOException
block|{
name|ArrayList
argument_list|<
name|Path
argument_list|>
name|result
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|int
name|cellsCount
init|=
name|snapshot
operator|.
name|getCellsCount
argument_list|()
decl_stmt|;
if|if
condition|(
name|cellsCount
operator|==
literal|0
condition|)
return|return
name|result
return|;
comment|// don't flush if there are no entries
comment|// Use a store scanner to find which rows to flush.
name|long
name|smallestReadPoint
init|=
name|store
operator|.
name|getSmallestReadPoint
argument_list|()
decl_stmt|;
name|InternalScanner
name|scanner
init|=
name|createScanner
argument_list|(
name|snapshot
operator|.
name|getScanners
argument_list|()
argument_list|,
name|smallestReadPoint
argument_list|,
name|tracker
argument_list|)
decl_stmt|;
name|StoreFileWriter
name|writer
decl_stmt|;
try|try
block|{
comment|// TODO:  We can fail in the below block before we complete adding this flush to
comment|//        list of store files.  Add cleanup of anything put on filesystem if we fail.
synchronized|synchronized
init|(
name|flushLock
init|)
block|{
name|status
operator|.
name|setStatus
argument_list|(
literal|"Flushing "
operator|+
name|store
operator|+
literal|": creating writer"
argument_list|)
expr_stmt|;
comment|// Write the map out to the disk
name|writer
operator|=
name|store
operator|.
name|createWriterInTmp
argument_list|(
name|cellsCount
argument_list|,
name|store
operator|.
name|getColumnFamilyDescriptor
argument_list|()
operator|.
name|getCompressionType
argument_list|()
argument_list|,
comment|/* isCompaction = */
literal|false
argument_list|,
comment|/* includeMVCCReadpoint = */
literal|true
argument_list|,
comment|/* includesTags = */
name|snapshot
operator|.
name|isTagsPresent
argument_list|()
argument_list|,
comment|/* shouldDropBehind = */
literal|false
argument_list|)
expr_stmt|;
name|IOException
name|e
init|=
literal|null
decl_stmt|;
try|try
block|{
name|performFlush
argument_list|(
name|scanner
argument_list|,
name|writer
argument_list|,
name|smallestReadPoint
argument_list|,
name|throughputController
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
name|e
operator|=
name|ioe
expr_stmt|;
comment|// throw the exception out
throw|throw
name|ioe
throw|;
block|}
finally|finally
block|{
if|if
condition|(
name|e
operator|!=
literal|null
condition|)
block|{
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|finalizeWriter
argument_list|(
name|writer
argument_list|,
name|cacheFlushId
argument_list|,
name|status
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
finally|finally
block|{
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Flushed, sequenceid="
operator|+
name|cacheFlushId
operator|+
literal|", memsize="
operator|+
name|StringUtils
operator|.
name|TraditionalBinaryPrefix
operator|.
name|long2String
argument_list|(
name|snapshot
operator|.
name|getDataSize
argument_list|()
argument_list|,
literal|""
argument_list|,
literal|1
argument_list|)
operator|+
literal|", hasBloomFilter="
operator|+
name|writer
operator|.
name|hasGeneralBloom
argument_list|()
operator|+
literal|", into tmp file "
operator|+
name|writer
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
name|result
operator|.
name|add
argument_list|(
name|writer
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
block|}
end_class

end_unit

