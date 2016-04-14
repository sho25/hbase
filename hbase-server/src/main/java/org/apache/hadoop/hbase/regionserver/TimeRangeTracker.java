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
name|DataInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutput
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
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|Writables
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
name|io
operator|.
name|Writable
import|;
end_import

begin_comment
comment|/**  * Stores minimum and maximum timestamp values. Both timestamps are inclusive.  * Use this class at write-time ONLY. Too much synchronization to use at read time  * (TODO: there are two scenarios writing, once when lots of concurrency as part of memstore  * updates but then later we can make one as part of a compaction when there is only one thread  * involved -- consider making different version, the synchronized and the unsynchronized).  * Use {@link TimeRange} at read time instead of this. See toTimeRange() to make TimeRange to use.  * MemStores use this class to track minimum and maximum timestamps. The TimeRangeTracker made by  * the MemStore is passed to the StoreFile for it to write out as part a flush in the the file  * metadata. If no memstore involved -- i.e. a compaction -- then the StoreFile will calculate its  * own TimeRangeTracker as it appends. The StoreFile serialized TimeRangeTracker is used  * at read time via an instance of {@link TimeRange} to test if Cells fit the StoreFile TimeRange.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|TimeRangeTracker
implements|implements
name|Writable
block|{
specifier|static
specifier|final
name|long
name|INITIAL_MIN_TIMESTAMP
init|=
name|Long
operator|.
name|MAX_VALUE
decl_stmt|;
name|long
name|minimumTimestamp
init|=
name|INITIAL_MIN_TIMESTAMP
decl_stmt|;
specifier|static
specifier|final
name|long
name|INITIAL_MAX_TIMESTAMP
init|=
operator|-
literal|1
decl_stmt|;
name|long
name|maximumTimestamp
init|=
name|INITIAL_MAX_TIMESTAMP
decl_stmt|;
comment|/**    * Default constructor.    * Initializes TimeRange to be null    */
specifier|public
name|TimeRangeTracker
parameter_list|()
block|{}
comment|/**    * Copy Constructor    * @param trt source TimeRangeTracker    */
specifier|public
name|TimeRangeTracker
parameter_list|(
specifier|final
name|TimeRangeTracker
name|trt
parameter_list|)
block|{
name|set
argument_list|(
name|trt
operator|.
name|getMin
argument_list|()
argument_list|,
name|trt
operator|.
name|getMax
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|TimeRangeTracker
parameter_list|(
name|long
name|minimumTimestamp
parameter_list|,
name|long
name|maximumTimestamp
parameter_list|)
block|{
name|set
argument_list|(
name|minimumTimestamp
argument_list|,
name|maximumTimestamp
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|set
parameter_list|(
specifier|final
name|long
name|min
parameter_list|,
specifier|final
name|long
name|max
parameter_list|)
block|{
name|this
operator|.
name|minimumTimestamp
operator|=
name|min
expr_stmt|;
name|this
operator|.
name|maximumTimestamp
operator|=
name|max
expr_stmt|;
block|}
comment|/**    * @param l    * @return True if we initialized values    */
specifier|private
name|boolean
name|init
parameter_list|(
specifier|final
name|long
name|l
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|minimumTimestamp
operator|!=
name|INITIAL_MIN_TIMESTAMP
condition|)
return|return
literal|false
return|;
name|set
argument_list|(
name|l
argument_list|,
name|l
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
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
name|CellUtil
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
comment|// Do test outside of synchronization block.  Synchronization in here can be problematic
comment|// when many threads writing one Store -- they can all pile up trying to add in here.
comment|// Happens when doing big write upload where we are hammering on one region.
if|if
condition|(
name|timestamp
operator|<
name|this
operator|.
name|minimumTimestamp
condition|)
block|{
synchronized|synchronized
init|(
name|this
init|)
block|{
if|if
condition|(
operator|!
name|init
argument_list|(
name|timestamp
argument_list|)
condition|)
block|{
if|if
condition|(
name|timestamp
operator|<
name|this
operator|.
name|minimumTimestamp
condition|)
block|{
name|this
operator|.
name|minimumTimestamp
operator|=
name|timestamp
expr_stmt|;
block|}
block|}
block|}
block|}
elseif|else
if|if
condition|(
name|timestamp
operator|>
name|this
operator|.
name|maximumTimestamp
condition|)
block|{
synchronized|synchronized
init|(
name|this
init|)
block|{
if|if
condition|(
operator|!
name|init
argument_list|(
name|timestamp
argument_list|)
condition|)
block|{
if|if
condition|(
name|this
operator|.
name|maximumTimestamp
operator|<
name|timestamp
condition|)
block|{
name|this
operator|.
name|maximumTimestamp
operator|=
name|timestamp
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
comment|/**    * Check if the range has any overlap with TimeRange    * @param tr TimeRange    * @return True if there is overlap, false otherwise    */
specifier|public
specifier|synchronized
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
name|this
operator|.
name|minimumTimestamp
operator|<
name|tr
operator|.
name|getMax
argument_list|()
operator|&&
name|this
operator|.
name|maximumTimestamp
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
specifier|synchronized
name|long
name|getMin
parameter_list|()
block|{
return|return
name|minimumTimestamp
return|;
block|}
comment|/**    * @return the maximumTimestamp    */
specifier|public
specifier|synchronized
name|long
name|getMax
parameter_list|()
block|{
return|return
name|maximumTimestamp
return|;
block|}
specifier|public
specifier|synchronized
name|void
name|write
parameter_list|(
specifier|final
name|DataOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeLong
argument_list|(
name|minimumTimestamp
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeLong
argument_list|(
name|maximumTimestamp
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|synchronized
name|void
name|readFields
parameter_list|(
specifier|final
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|minimumTimestamp
operator|=
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
name|this
operator|.
name|maximumTimestamp
operator|=
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"["
operator|+
name|minimumTimestamp
operator|+
literal|","
operator|+
name|maximumTimestamp
operator|+
literal|"]"
return|;
block|}
comment|/**    * @return An instance of TimeRangeTracker filled w/ the content of serialized    * TimeRangeTracker in<code>timeRangeTrackerBytes</code>.    * @throws IOException    */
specifier|public
specifier|static
name|TimeRangeTracker
name|getTimeRangeTracker
parameter_list|(
specifier|final
name|byte
index|[]
name|timeRangeTrackerBytes
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|timeRangeTrackerBytes
operator|==
literal|null
condition|)
return|return
literal|null
return|;
name|TimeRangeTracker
name|trt
init|=
operator|new
name|TimeRangeTracker
argument_list|()
decl_stmt|;
name|Writables
operator|.
name|copyWritable
argument_list|(
name|timeRangeTrackerBytes
argument_list|,
name|trt
argument_list|)
expr_stmt|;
return|return
name|trt
return|;
block|}
comment|/**    * @return An instance of a TimeRange made from the serialized TimeRangeTracker passed in    *<code>timeRangeTrackerBytes</code>.    * @throws IOException    */
specifier|static
name|TimeRange
name|getTimeRange
parameter_list|(
specifier|final
name|byte
index|[]
name|timeRangeTrackerBytes
parameter_list|)
throws|throws
name|IOException
block|{
name|TimeRangeTracker
name|trt
init|=
name|getTimeRangeTracker
argument_list|(
name|timeRangeTrackerBytes
argument_list|)
decl_stmt|;
return|return
name|trt
operator|==
literal|null
condition|?
literal|null
else|:
name|trt
operator|.
name|toTimeRange
argument_list|()
return|;
block|}
specifier|private
name|boolean
name|isFreshInstance
parameter_list|()
block|{
return|return
name|getMin
argument_list|()
operator|==
name|INITIAL_MIN_TIMESTAMP
operator|&&
name|getMax
argument_list|()
operator|==
name|INITIAL_MAX_TIMESTAMP
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
comment|// Check for the case where the TimeRangeTracker is fresh. In that case it has
comment|// initial values that are antithetical to a TimeRange... Return an uninitialized TimeRange
comment|// if passed an uninitialized TimeRangeTracker.
if|if
condition|(
name|isFreshInstance
argument_list|()
condition|)
block|{
return|return
operator|new
name|TimeRange
argument_list|()
return|;
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
block|}
end_class

end_unit

