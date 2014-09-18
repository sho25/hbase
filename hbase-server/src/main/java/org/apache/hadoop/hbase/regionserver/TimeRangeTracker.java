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
name|KeyValue
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
name|KeyValue
operator|.
name|Type
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
name|io
operator|.
name|Writable
import|;
end_import

begin_comment
comment|/**  * Stores the minimum and maximum timestamp values (both are inclusive).  * Can be used to find if any given time range overlaps with its time range  * MemStores use this class to track its minimum and maximum timestamps.  * When writing StoreFiles, this information is stored in meta blocks and used  * at read time to match against the required TimeRange.  */
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
name|long
name|minimumTimestamp
init|=
operator|-
literal|1
decl_stmt|;
name|long
name|maximumTimestamp
init|=
operator|-
literal|1
decl_stmt|;
comment|/**    * Default constructor.    * Initializes TimeRange to be null    */
specifier|public
name|TimeRangeTracker
parameter_list|()
block|{    }
comment|/**    * Copy Constructor    * @param trt source TimeRangeTracker    */
specifier|public
name|TimeRangeTracker
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
name|getMinimumTimestamp
argument_list|()
expr_stmt|;
name|this
operator|.
name|maximumTimestamp
operator|=
name|trt
operator|.
name|getMaximumTimestamp
argument_list|()
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
comment|/**    * Update the current TimestampRange to include the timestamp from Cell    * If the Key is of type DeleteColumn or DeleteFamily, it includes the    * entire time range from 0 to timestamp of the key.    * @param cell the Cell to include    */
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
comment|/**    * Update the current TimestampRange to include the timestamp from Key.    * If the Key is of type DeleteColumn or DeleteFamily, it includes the    * entire time range from 0 to timestamp of the key.    * @param key    */
specifier|public
name|void
name|includeTimestamp
parameter_list|(
specifier|final
name|byte
index|[]
name|key
parameter_list|)
block|{
name|includeTimestamp
argument_list|(
name|Bytes
operator|.
name|toLong
argument_list|(
name|key
argument_list|,
name|key
operator|.
name|length
operator|-
name|KeyValue
operator|.
name|TIMESTAMP_TYPE_SIZE
argument_list|)
argument_list|)
expr_stmt|;
name|int
name|type
init|=
name|key
index|[
name|key
operator|.
name|length
operator|-
literal|1
index|]
decl_stmt|;
if|if
condition|(
name|type
operator|==
name|Type
operator|.
name|DeleteColumn
operator|.
name|getCode
argument_list|()
operator|||
name|type
operator|==
name|Type
operator|.
name|DeleteFamily
operator|.
name|getCode
argument_list|()
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
specifier|private
specifier|synchronized
name|void
name|includeTimestamp
parameter_list|(
specifier|final
name|long
name|timestamp
parameter_list|)
block|{
if|if
condition|(
name|maximumTimestamp
operator|==
operator|-
literal|1
condition|)
block|{
name|minimumTimestamp
operator|=
name|timestamp
expr_stmt|;
name|maximumTimestamp
operator|=
name|timestamp
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|minimumTimestamp
operator|>
name|timestamp
condition|)
block|{
name|minimumTimestamp
operator|=
name|timestamp
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|maximumTimestamp
operator|<
name|timestamp
condition|)
block|{
name|maximumTimestamp
operator|=
name|timestamp
expr_stmt|;
block|}
return|return;
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
name|getMinimumTimestamp
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
name|getMaximumTimestamp
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
block|}
end_class

end_unit

