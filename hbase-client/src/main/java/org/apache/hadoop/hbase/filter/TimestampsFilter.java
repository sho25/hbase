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
name|filter
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
name|java
operator|.
name|util
operator|.
name|TreeSet
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
name|PrivateCellUtil
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|FilterProtos
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
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|InvalidProtocolBufferException
import|;
end_import

begin_comment
comment|/**  * Filter that returns only cells whose timestamp (version) is  * in the specified list of timestamps (versions).  *<p>  * Note: Use of this filter overrides any time range/time stamp  * options specified using {@link org.apache.hadoop.hbase.client.Get#setTimeRange(long, long)},  * {@link org.apache.hadoop.hbase.client.Scan#setTimeRange(long, long)}, {@link org.apache.hadoop.hbase.client.Get#setTimeStamp(long)},  * or {@link org.apache.hadoop.hbase.client.Scan#setTimeStamp(long)}.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
class|class
name|TimestampsFilter
extends|extends
name|FilterBase
block|{
specifier|private
specifier|final
name|boolean
name|canHint
decl_stmt|;
name|TreeSet
argument_list|<
name|Long
argument_list|>
name|timestamps
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|MAX_LOG_TIMESTAMPS
init|=
literal|5
decl_stmt|;
comment|// Used during scans to hint the scan to stop early
comment|// once the timestamps fall below the minTimeStamp.
name|long
name|minTimeStamp
init|=
name|Long
operator|.
name|MAX_VALUE
decl_stmt|;
comment|/**    * Constructor for filter that retains only the specified timestamps in the list.    * @param timestamps    */
specifier|public
name|TimestampsFilter
parameter_list|(
name|List
argument_list|<
name|Long
argument_list|>
name|timestamps
parameter_list|)
block|{
name|this
argument_list|(
name|timestamps
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructor for filter that retains only those    * cells whose timestamp (version) is in the specified    * list of timestamps.    *    * @param timestamps list of timestamps that are wanted.    * @param canHint should the filter provide a seek hint? This can skip    *                past delete tombstones, so it should only be used when that    *                is not an issue ( no deletes, or don't care if data    *                becomes visible)    */
specifier|public
name|TimestampsFilter
parameter_list|(
name|List
argument_list|<
name|Long
argument_list|>
name|timestamps
parameter_list|,
name|boolean
name|canHint
parameter_list|)
block|{
for|for
control|(
name|Long
name|timestamp
range|:
name|timestamps
control|)
block|{
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|timestamp
operator|>=
literal|0
argument_list|,
literal|"must be positive %s"
argument_list|,
name|timestamp
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|canHint
operator|=
name|canHint
expr_stmt|;
name|this
operator|.
name|timestamps
operator|=
operator|new
name|TreeSet
argument_list|<>
argument_list|(
name|timestamps
argument_list|)
expr_stmt|;
name|init
argument_list|()
expr_stmt|;
block|}
comment|/**    * @return the list of timestamps    */
specifier|public
name|List
argument_list|<
name|Long
argument_list|>
name|getTimestamps
parameter_list|()
block|{
name|List
argument_list|<
name|Long
argument_list|>
name|list
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|timestamps
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|list
operator|.
name|addAll
argument_list|(
name|timestamps
argument_list|)
expr_stmt|;
return|return
name|list
return|;
block|}
specifier|private
name|void
name|init
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|timestamps
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|minTimeStamp
operator|=
name|this
operator|.
name|timestamps
operator|.
name|first
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Gets the minimum timestamp requested by filter.    * @return  minimum timestamp requested by filter.    */
specifier|public
name|long
name|getMin
parameter_list|()
block|{
return|return
name|minTimeStamp
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|filterRowKey
parameter_list|(
name|Cell
name|cell
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Impl in FilterBase might do unnecessary copy for Off heap backed Cells.
return|return
literal|false
return|;
block|}
annotation|@
name|Deprecated
annotation|@
name|Override
specifier|public
name|ReturnCode
name|filterKeyValue
parameter_list|(
specifier|final
name|Cell
name|c
parameter_list|)
block|{
return|return
name|filterCell
argument_list|(
name|c
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|ReturnCode
name|filterCell
parameter_list|(
specifier|final
name|Cell
name|c
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|timestamps
operator|.
name|contains
argument_list|(
name|c
operator|.
name|getTimestamp
argument_list|()
argument_list|)
condition|)
block|{
return|return
name|ReturnCode
operator|.
name|INCLUDE
return|;
block|}
elseif|else
if|if
condition|(
name|c
operator|.
name|getTimestamp
argument_list|()
operator|<
name|minTimeStamp
condition|)
block|{
comment|// The remaining versions of this column are guaranteed
comment|// to be lesser than all of the other values.
return|return
name|ReturnCode
operator|.
name|NEXT_COL
return|;
block|}
return|return
name|canHint
condition|?
name|ReturnCode
operator|.
name|SEEK_NEXT_USING_HINT
else|:
name|ReturnCode
operator|.
name|SKIP
return|;
block|}
comment|/**    * Pick the next cell that the scanner should seek to. Since this can skip any number of cells    * any of which can be a delete this can resurect old data.    *    * The method will only be used if canHint was set to true while creating the filter.    *    * @throws IOException This will never happen.    */
annotation|@
name|Override
specifier|public
name|Cell
name|getNextCellHint
parameter_list|(
name|Cell
name|currentCell
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|canHint
condition|)
block|{
return|return
literal|null
return|;
block|}
name|Long
name|nextTimestampObject
init|=
name|timestamps
operator|.
name|lower
argument_list|(
name|currentCell
operator|.
name|getTimestamp
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|nextTimestampObject
operator|==
literal|null
condition|)
block|{
comment|// This should only happen if the current column's
comment|// timestamp is below the last one in the list.
comment|//
comment|// It should never happen as the filterCell should return NEXT_COL
comment|// but it's always better to be extra safe and protect against future
comment|// behavioral changes.
return|return
name|PrivateCellUtil
operator|.
name|createLastOnRowCol
argument_list|(
name|currentCell
argument_list|)
return|;
block|}
comment|// Since we know the nextTimestampObject isn't null here there must still be
comment|// timestamps that can be included. Cast the Long to a long and return the
comment|// a cell with the current row/cf/col and the next found timestamp.
name|long
name|nextTimestamp
init|=
name|nextTimestampObject
decl_stmt|;
return|return
name|PrivateCellUtil
operator|.
name|createFirstOnRowColTS
argument_list|(
name|currentCell
argument_list|,
name|nextTimestamp
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|Filter
name|createFilterFromArguments
parameter_list|(
name|ArrayList
argument_list|<
name|byte
index|[]
argument_list|>
name|filterArguments
parameter_list|)
block|{
name|ArrayList
argument_list|<
name|Long
argument_list|>
name|timestamps
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|filterArguments
operator|.
name|size
argument_list|()
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
name|filterArguments
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|long
name|timestamp
init|=
name|ParseFilter
operator|.
name|convertByteArrayToLong
argument_list|(
name|filterArguments
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
decl_stmt|;
name|timestamps
operator|.
name|add
argument_list|(
name|timestamp
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|TimestampsFilter
argument_list|(
name|timestamps
argument_list|)
return|;
block|}
comment|/**    * @return The filter serialized using pb    */
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|toByteArray
parameter_list|()
block|{
name|FilterProtos
operator|.
name|TimestampsFilter
operator|.
name|Builder
name|builder
init|=
name|FilterProtos
operator|.
name|TimestampsFilter
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|addAllTimestamps
argument_list|(
name|this
operator|.
name|timestamps
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setCanHint
argument_list|(
name|canHint
argument_list|)
expr_stmt|;
return|return
name|builder
operator|.
name|build
argument_list|()
operator|.
name|toByteArray
argument_list|()
return|;
block|}
comment|/**    * @param pbBytes A pb serialized {@link TimestampsFilter} instance    *    * @return An instance of {@link TimestampsFilter} made from<code>bytes</code>    * @see #toByteArray    */
specifier|public
specifier|static
name|TimestampsFilter
name|parseFrom
parameter_list|(
specifier|final
name|byte
index|[]
name|pbBytes
parameter_list|)
throws|throws
name|DeserializationException
block|{
name|FilterProtos
operator|.
name|TimestampsFilter
name|proto
decl_stmt|;
try|try
block|{
name|proto
operator|=
name|FilterProtos
operator|.
name|TimestampsFilter
operator|.
name|parseFrom
argument_list|(
name|pbBytes
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InvalidProtocolBufferException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|DeserializationException
argument_list|(
name|e
argument_list|)
throw|;
block|}
return|return
operator|new
name|TimestampsFilter
argument_list|(
name|proto
operator|.
name|getTimestampsList
argument_list|()
argument_list|,
name|proto
operator|.
name|hasCanHint
argument_list|()
operator|&&
name|proto
operator|.
name|getCanHint
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * @param o the other filter to compare with    * @return true if and only if the fields of the filter that are serialized    * are equal to the corresponding fields in other.  Used for testing.    */
annotation|@
name|Override
name|boolean
name|areSerializedFieldsEqual
parameter_list|(
name|Filter
name|o
parameter_list|)
block|{
if|if
condition|(
name|o
operator|==
name|this
condition|)
return|return
literal|true
return|;
if|if
condition|(
operator|!
operator|(
name|o
operator|instanceof
name|TimestampsFilter
operator|)
condition|)
return|return
literal|false
return|;
name|TimestampsFilter
name|other
init|=
operator|(
name|TimestampsFilter
operator|)
name|o
decl_stmt|;
return|return
name|this
operator|.
name|getTimestamps
argument_list|()
operator|.
name|equals
argument_list|(
name|other
operator|.
name|getTimestamps
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|toString
argument_list|(
name|MAX_LOG_TIMESTAMPS
argument_list|)
return|;
block|}
specifier|protected
name|String
name|toString
parameter_list|(
name|int
name|maxTimestamps
parameter_list|)
block|{
name|StringBuilder
name|tsList
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|int
name|count
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Long
name|ts
range|:
name|this
operator|.
name|timestamps
control|)
block|{
if|if
condition|(
name|count
operator|>=
name|maxTimestamps
condition|)
block|{
break|break;
block|}
operator|++
name|count
expr_stmt|;
name|tsList
operator|.
name|append
argument_list|(
name|ts
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|count
operator|<
name|this
operator|.
name|timestamps
operator|.
name|size
argument_list|()
operator|&&
name|count
operator|<
name|maxTimestamps
condition|)
block|{
name|tsList
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|String
operator|.
name|format
argument_list|(
literal|"%s (%d/%d): [%s] canHint: [%b]"
argument_list|,
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|,
name|count
argument_list|,
name|this
operator|.
name|timestamps
operator|.
name|size
argument_list|()
argument_list|,
name|tsList
operator|.
name|toString
argument_list|()
argument_list|,
name|canHint
argument_list|)
return|;
block|}
block|}
end_class

end_unit

