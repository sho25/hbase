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
name|ByteArrayInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInputStream
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
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicLong
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
name|CellUtil
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
name|PrivateCellUtil
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
name|TimeRange
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
name|annotations
operator|.
name|VisibleForTesting
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
name|base
operator|.
name|Preconditions
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
name|shaded
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|HBaseProtos
import|;
end_import

begin_comment
comment|/**  * Stores minimum and maximum timestamp values, it is [minimumTimestamp, maximumTimestamp] in  * interval notation.  * Use this class at write-time ONLY. Too much synchronization to use at read time  * Use {@link TimeRange} at read time instead of this. See toTimeRange() to make TimeRange to use.  * MemStores use this class to track minimum and maximum timestamps. The TimeRangeTracker made by  * the MemStore is passed to the StoreFile for it to write out as part a flush in the the file  * metadata. If no memstore involved -- i.e. a compaction -- then the StoreFile will calculate its  * own TimeRangeTracker as it appends. The StoreFile serialized TimeRangeTracker is used  * at read time via an instance of {@link TimeRange} to test if Cells fit the StoreFile TimeRange.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|TimeRangeTracker
block|{
specifier|public
enum|enum
name|Type
block|{
comment|// thread-unsafe
name|NON_SYNC
block|,
comment|// thread-safe
name|SYNC
block|}
specifier|static
specifier|final
name|long
name|INITIAL_MIN_TIMESTAMP
init|=
name|Long
operator|.
name|MAX_VALUE
decl_stmt|;
specifier|static
specifier|final
name|long
name|INITIAL_MAX_TIMESTAMP
init|=
operator|-
literal|1L
decl_stmt|;
specifier|public
specifier|static
name|TimeRangeTracker
name|create
parameter_list|(
name|Type
name|type
parameter_list|)
block|{
switch|switch
condition|(
name|type
condition|)
block|{
case|case
name|NON_SYNC
case|:
return|return
operator|new
name|NonSyncTimeRangeTracker
argument_list|()
return|;
case|case
name|SYNC
case|:
return|return
operator|new
name|SyncTimeRangeTracker
argument_list|()
return|;
default|default:
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"The type:"
operator|+
name|type
operator|+
literal|" is unsupported"
argument_list|)
throw|;
block|}
block|}
specifier|public
specifier|static
name|TimeRangeTracker
name|create
parameter_list|(
name|Type
name|type
parameter_list|,
name|TimeRangeTracker
name|trt
parameter_list|)
block|{
switch|switch
condition|(
name|type
condition|)
block|{
case|case
name|NON_SYNC
case|:
return|return
operator|new
name|NonSyncTimeRangeTracker
argument_list|(
name|trt
argument_list|)
return|;
case|case
name|SYNC
case|:
return|return
operator|new
name|SyncTimeRangeTracker
argument_list|(
name|trt
argument_list|)
return|;
default|default:
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"The type:"
operator|+
name|type
operator|+
literal|" is unsupported"
argument_list|)
throw|;
block|}
block|}
specifier|public
specifier|static
name|TimeRangeTracker
name|create
parameter_list|(
name|Type
name|type
parameter_list|,
name|long
name|minimumTimestamp
parameter_list|,
name|long
name|maximumTimestamp
parameter_list|)
block|{
switch|switch
condition|(
name|type
condition|)
block|{
case|case
name|NON_SYNC
case|:
return|return
operator|new
name|NonSyncTimeRangeTracker
argument_list|(
name|minimumTimestamp
argument_list|,
name|maximumTimestamp
argument_list|)
return|;
case|case
name|SYNC
case|:
return|return
operator|new
name|SyncTimeRangeTracker
argument_list|(
name|minimumTimestamp
argument_list|,
name|maximumTimestamp
argument_list|)
return|;
default|default:
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"The type:"
operator|+
name|type
operator|+
literal|" is unsupported"
argument_list|)
throw|;
block|}
block|}
specifier|protected
specifier|abstract
name|void
name|setMax
parameter_list|(
name|long
name|ts
parameter_list|)
function_decl|;
specifier|protected
specifier|abstract
name|void
name|setMin
parameter_list|(
name|long
name|ts
parameter_list|)
function_decl|;
specifier|protected
specifier|abstract
name|boolean
name|compareAndSetMin
parameter_list|(
name|long
name|expect
parameter_list|,
name|long
name|update
parameter_list|)
function_decl|;
specifier|protected
specifier|abstract
name|boolean
name|compareAndSetMax
parameter_list|(
name|long
name|expect
parameter_list|,
name|long
name|update
parameter_list|)
function_decl|;
comment|/**    * Update the current TimestampRange to include the timestamp from<code>cell</code>.    * If the Key is of type DeleteColumn or DeleteFamily, it includes the    * entire time range from 0 to timestamp of the key.    * @param cell the Cell to include    */
specifier|public
name|void
name|includeTimestamp
parameter_list|(
specifier|final
name|Cell
name|cell
parameter_list|)
block|{
name|includeTimestamp
argument_list|(
name|cell
operator|.
name|getTimestamp
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|PrivateCellUtil
operator|.
name|isDeleteColumnOrFamily
argument_list|(
name|cell
argument_list|)
condition|)
block|{
name|includeTimestamp
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * If required, update the current TimestampRange to include timestamp    * @param timestamp the timestamp value to include    */
annotation|@
name|edu
operator|.
name|umd
operator|.
name|cs
operator|.
name|findbugs
operator|.
name|annotations
operator|.
name|SuppressWarnings
argument_list|(
name|value
operator|=
literal|"MT_CORRECTNESS"
argument_list|,
name|justification
operator|=
literal|"Intentional"
argument_list|)
name|void
name|includeTimestamp
parameter_list|(
specifier|final
name|long
name|timestamp
parameter_list|)
block|{
name|long
name|initialMinTimestamp
init|=
name|getMin
argument_list|()
decl_stmt|;
if|if
condition|(
name|timestamp
operator|<
name|initialMinTimestamp
condition|)
block|{
name|long
name|curMinTimestamp
init|=
name|initialMinTimestamp
decl_stmt|;
while|while
condition|(
name|timestamp
operator|<
name|curMinTimestamp
condition|)
block|{
if|if
condition|(
operator|!
name|compareAndSetMin
argument_list|(
name|curMinTimestamp
argument_list|,
name|timestamp
argument_list|)
condition|)
block|{
name|curMinTimestamp
operator|=
name|getMin
argument_list|()
expr_stmt|;
block|}
else|else
block|{
comment|// successfully set minimumTimestamp, break.
break|break;
block|}
block|}
comment|// When it reaches here, there are two possibilities:
comment|//  1). timestamp>= curMinTimestamp, someone already sets the minimumTimestamp. In this case,
comment|//      it still needs to check if initialMinTimestamp == INITIAL_MIN_TIMESTAMP to see
comment|//      if it needs to update minimumTimestamp. Someone may already set both
comment|//      minimumTimestamp/minimumTimestamp to the same value(curMinTimestamp),
comment|//      need to check if maximumTimestamp needs to be updated.
comment|//  2). timestamp< curMinTimestamp, it sets the minimumTimestamp successfully.
comment|//      In this case,it still needs to check if initialMinTimestamp == INITIAL_MIN_TIMESTAMP
comment|//      to see if it needs to set maximumTimestamp.
if|if
condition|(
name|initialMinTimestamp
operator|!=
name|INITIAL_MIN_TIMESTAMP
condition|)
block|{
comment|// Someone already sets minimumTimestamp and timestamp is less than minimumTimestamp.
comment|// In this case, no need to set maximumTimestamp as it will be set to at least
comment|// initialMinTimestamp.
return|return;
block|}
block|}
name|long
name|curMaxTimestamp
init|=
name|getMax
argument_list|()
decl_stmt|;
if|if
condition|(
name|timestamp
operator|>
name|curMaxTimestamp
condition|)
block|{
while|while
condition|(
name|timestamp
operator|>
name|curMaxTimestamp
condition|)
block|{
if|if
condition|(
operator|!
name|compareAndSetMax
argument_list|(
name|curMaxTimestamp
argument_list|,
name|timestamp
argument_list|)
condition|)
block|{
name|curMaxTimestamp
operator|=
name|getMax
argument_list|()
expr_stmt|;
block|}
else|else
block|{
comment|// successfully set maximumTimestamp, break
break|break;
block|}
block|}
block|}
block|}
comment|/**    * Check if the range has ANY overlap with TimeRange    * @param tr TimeRange, it expects [minStamp, maxStamp)    * @return True if there is overlap, false otherwise    */
specifier|public
name|boolean
name|includesTimeRange
parameter_list|(
specifier|final
name|TimeRange
name|tr
parameter_list|)
block|{
return|return
operator|(
name|getMin
argument_list|()
operator|<
name|tr
operator|.
name|getMax
argument_list|()
operator|&&
name|getMax
argument_list|()
operator|>=
name|tr
operator|.
name|getMin
argument_list|()
operator|)
return|;
block|}
comment|/**    * @return the minimumTimestamp    */
specifier|public
specifier|abstract
name|long
name|getMin
parameter_list|()
function_decl|;
comment|/**    * @return the maximumTimestamp    */
specifier|public
specifier|abstract
name|long
name|getMax
parameter_list|()
function_decl|;
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"["
operator|+
name|getMin
argument_list|()
operator|+
literal|","
operator|+
name|getMax
argument_list|()
operator|+
literal|"]"
return|;
block|}
comment|/**    * @param data the serialization data. It can't be null!    * @return An instance of NonSyncTimeRangeTracker filled w/ the content of serialized    * NonSyncTimeRangeTracker in<code>timeRangeTrackerBytes</code>.    * @throws IOException    */
specifier|public
specifier|static
name|TimeRangeTracker
name|parseFrom
parameter_list|(
specifier|final
name|byte
index|[]
name|data
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|parseFrom
argument_list|(
name|data
argument_list|,
name|Type
operator|.
name|NON_SYNC
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|TimeRangeTracker
name|parseFrom
parameter_list|(
specifier|final
name|byte
index|[]
name|data
parameter_list|,
name|Type
name|type
parameter_list|)
throws|throws
name|IOException
block|{
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|data
argument_list|,
literal|"input data is null!"
argument_list|)
expr_stmt|;
if|if
condition|(
name|ProtobufUtil
operator|.
name|isPBMagicPrefix
argument_list|(
name|data
argument_list|)
condition|)
block|{
name|int
name|pblen
init|=
name|ProtobufUtil
operator|.
name|lengthOfPBMagic
argument_list|()
decl_stmt|;
name|HBaseProtos
operator|.
name|TimeRangeTracker
operator|.
name|Builder
name|builder
init|=
name|HBaseProtos
operator|.
name|TimeRangeTracker
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|ProtobufUtil
operator|.
name|mergeFrom
argument_list|(
name|builder
argument_list|,
name|data
argument_list|,
name|pblen
argument_list|,
name|data
operator|.
name|length
operator|-
name|pblen
argument_list|)
expr_stmt|;
return|return
name|TimeRangeTracker
operator|.
name|create
argument_list|(
name|type
argument_list|,
name|builder
operator|.
name|getFrom
argument_list|()
argument_list|,
name|builder
operator|.
name|getTo
argument_list|()
argument_list|)
return|;
block|}
else|else
block|{
try|try
init|(
name|DataInputStream
name|in
init|=
operator|new
name|DataInputStream
argument_list|(
operator|new
name|ByteArrayInputStream
argument_list|(
name|data
argument_list|)
argument_list|)
init|)
block|{
return|return
name|TimeRangeTracker
operator|.
name|create
argument_list|(
name|type
argument_list|,
name|in
operator|.
name|readLong
argument_list|()
argument_list|,
name|in
operator|.
name|readLong
argument_list|()
argument_list|)
return|;
block|}
block|}
block|}
specifier|public
specifier|static
name|byte
index|[]
name|toByteArray
parameter_list|(
name|TimeRangeTracker
name|tracker
parameter_list|)
block|{
return|return
name|ProtobufUtil
operator|.
name|prependPBMagic
argument_list|(
name|HBaseProtos
operator|.
name|TimeRangeTracker
operator|.
name|newBuilder
argument_list|()
operator|.
name|setFrom
argument_list|(
name|tracker
operator|.
name|getMin
argument_list|()
argument_list|)
operator|.
name|setTo
argument_list|(
name|tracker
operator|.
name|getMax
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
operator|.
name|toByteArray
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * @return Make a TimeRange from current state of<code>this</code>.    */
name|TimeRange
name|toTimeRange
parameter_list|()
block|{
name|long
name|min
init|=
name|getMin
argument_list|()
decl_stmt|;
name|long
name|max
init|=
name|getMax
argument_list|()
decl_stmt|;
comment|// Initial TimeRangeTracker timestamps are the opposite of what you want for a TimeRange. Fix!
if|if
condition|(
name|min
operator|==
name|INITIAL_MIN_TIMESTAMP
condition|)
block|{
name|min
operator|=
name|TimeRange
operator|.
name|INITIAL_MIN_TIMESTAMP
expr_stmt|;
block|}
if|if
condition|(
name|max
operator|==
name|INITIAL_MAX_TIMESTAMP
condition|)
block|{
name|max
operator|=
name|TimeRange
operator|.
name|INITIAL_MAX_TIMESTAMP
expr_stmt|;
block|}
return|return
operator|new
name|TimeRange
argument_list|(
name|min
argument_list|,
name|max
argument_list|)
return|;
block|}
annotation|@
name|VisibleForTesting
comment|//In order to estimate the heap size, this inner class need to be accessible to TestHeapSize.
specifier|public
specifier|static
class|class
name|NonSyncTimeRangeTracker
extends|extends
name|TimeRangeTracker
block|{
specifier|private
name|long
name|minimumTimestamp
init|=
name|INITIAL_MIN_TIMESTAMP
decl_stmt|;
specifier|private
name|long
name|maximumTimestamp
init|=
name|INITIAL_MAX_TIMESTAMP
decl_stmt|;
name|NonSyncTimeRangeTracker
parameter_list|()
block|{     }
name|NonSyncTimeRangeTracker
parameter_list|(
specifier|final
name|TimeRangeTracker
name|trt
parameter_list|)
block|{
name|this
operator|.
name|minimumTimestamp
operator|=
name|trt
operator|.
name|getMin
argument_list|()
expr_stmt|;
name|this
operator|.
name|maximumTimestamp
operator|=
name|trt
operator|.
name|getMax
argument_list|()
expr_stmt|;
block|}
name|NonSyncTimeRangeTracker
parameter_list|(
name|long
name|minimumTimestamp
parameter_list|,
name|long
name|maximumTimestamp
parameter_list|)
block|{
name|this
operator|.
name|minimumTimestamp
operator|=
name|minimumTimestamp
expr_stmt|;
name|this
operator|.
name|maximumTimestamp
operator|=
name|maximumTimestamp
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|setMax
parameter_list|(
name|long
name|ts
parameter_list|)
block|{
name|maximumTimestamp
operator|=
name|ts
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|setMin
parameter_list|(
name|long
name|ts
parameter_list|)
block|{
name|minimumTimestamp
operator|=
name|ts
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|compareAndSetMin
parameter_list|(
name|long
name|expect
parameter_list|,
name|long
name|update
parameter_list|)
block|{
if|if
condition|(
name|minimumTimestamp
operator|!=
name|expect
condition|)
block|{
return|return
literal|false
return|;
block|}
name|minimumTimestamp
operator|=
name|update
expr_stmt|;
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|compareAndSetMax
parameter_list|(
name|long
name|expect
parameter_list|,
name|long
name|update
parameter_list|)
block|{
if|if
condition|(
name|maximumTimestamp
operator|!=
name|expect
condition|)
block|{
return|return
literal|false
return|;
block|}
name|maximumTimestamp
operator|=
name|update
expr_stmt|;
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getMin
parameter_list|()
block|{
return|return
name|minimumTimestamp
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getMax
parameter_list|()
block|{
return|return
name|maximumTimestamp
return|;
block|}
block|}
annotation|@
name|VisibleForTesting
comment|//In order to estimate the heap size, this inner class need to be accessible to TestHeapSize.
specifier|public
specifier|static
class|class
name|SyncTimeRangeTracker
extends|extends
name|TimeRangeTracker
block|{
specifier|private
specifier|final
name|AtomicLong
name|minimumTimestamp
init|=
operator|new
name|AtomicLong
argument_list|(
name|INITIAL_MIN_TIMESTAMP
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|AtomicLong
name|maximumTimestamp
init|=
operator|new
name|AtomicLong
argument_list|(
name|INITIAL_MAX_TIMESTAMP
argument_list|)
decl_stmt|;
specifier|private
name|SyncTimeRangeTracker
parameter_list|()
block|{     }
name|SyncTimeRangeTracker
parameter_list|(
specifier|final
name|TimeRangeTracker
name|trt
parameter_list|)
block|{
name|this
operator|.
name|minimumTimestamp
operator|.
name|set
argument_list|(
name|trt
operator|.
name|getMin
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|maximumTimestamp
operator|.
name|set
argument_list|(
name|trt
operator|.
name|getMax
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|SyncTimeRangeTracker
parameter_list|(
name|long
name|minimumTimestamp
parameter_list|,
name|long
name|maximumTimestamp
parameter_list|)
block|{
name|this
operator|.
name|minimumTimestamp
operator|.
name|set
argument_list|(
name|minimumTimestamp
argument_list|)
expr_stmt|;
name|this
operator|.
name|maximumTimestamp
operator|.
name|set
argument_list|(
name|maximumTimestamp
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|setMax
parameter_list|(
name|long
name|ts
parameter_list|)
block|{
name|maximumTimestamp
operator|.
name|set
argument_list|(
name|ts
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|setMin
parameter_list|(
name|long
name|ts
parameter_list|)
block|{
name|minimumTimestamp
operator|.
name|set
argument_list|(
name|ts
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|compareAndSetMin
parameter_list|(
name|long
name|expect
parameter_list|,
name|long
name|update
parameter_list|)
block|{
return|return
name|minimumTimestamp
operator|.
name|compareAndSet
argument_list|(
name|expect
argument_list|,
name|update
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|compareAndSetMax
parameter_list|(
name|long
name|expect
parameter_list|,
name|long
name|update
parameter_list|)
block|{
return|return
name|maximumTimestamp
operator|.
name|compareAndSet
argument_list|(
name|expect
argument_list|,
name|update
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getMin
parameter_list|()
block|{
return|return
name|minimumTimestamp
operator|.
name|get
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getMax
parameter_list|()
block|{
return|return
name|maximumTimestamp
operator|.
name|get
argument_list|()
return|;
block|}
block|}
block|}
end_class

end_unit

