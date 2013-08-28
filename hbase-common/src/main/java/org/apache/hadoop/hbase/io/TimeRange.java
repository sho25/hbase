begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|io
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
name|classification
operator|.
name|InterfaceStability
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

begin_comment
comment|/**  * Represents an interval of version timestamps.  *<p>  * Evaluated according to minStamp<= timestamp< maxStamp  * or [minStamp,maxStamp) in interval notation.  *<p>  * Only used internally; should not be accessed directly by clients.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Stable
specifier|public
class|class
name|TimeRange
block|{
specifier|private
name|long
name|minStamp
init|=
literal|0L
decl_stmt|;
specifier|private
name|long
name|maxStamp
init|=
name|Long
operator|.
name|MAX_VALUE
decl_stmt|;
specifier|private
name|boolean
name|allTime
init|=
literal|false
decl_stmt|;
comment|/**    * Default constructor.    * Represents interval [0, Long.MAX_VALUE) (allTime)    */
specifier|public
name|TimeRange
parameter_list|()
block|{
name|allTime
operator|=
literal|true
expr_stmt|;
block|}
comment|/**    * Represents interval [minStamp, Long.MAX_VALUE)    * @param minStamp the minimum timestamp value, inclusive    */
specifier|public
name|TimeRange
parameter_list|(
name|long
name|minStamp
parameter_list|)
block|{
name|this
operator|.
name|minStamp
operator|=
name|minStamp
expr_stmt|;
block|}
comment|/**    * Represents interval [minStamp, Long.MAX_VALUE)    * @param minStamp the minimum timestamp value, inclusive    */
specifier|public
name|TimeRange
parameter_list|(
name|byte
index|[]
name|minStamp
parameter_list|)
block|{
name|this
operator|.
name|minStamp
operator|=
name|Bytes
operator|.
name|toLong
argument_list|(
name|minStamp
argument_list|)
expr_stmt|;
block|}
comment|/**    * Represents interval [minStamp, maxStamp)    * @param minStamp the minimum timestamp, inclusive    * @param maxStamp the maximum timestamp, exclusive    * @throws IOException    */
specifier|public
name|TimeRange
parameter_list|(
name|long
name|minStamp
parameter_list|,
name|long
name|maxStamp
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|minStamp
operator|<
literal|0
operator|||
name|maxStamp
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Timestamp cannot be negative. minStamp:"
operator|+
name|minStamp
operator|+
literal|", maxStamp"
operator|+
name|maxStamp
argument_list|)
throw|;
block|}
if|if
condition|(
name|maxStamp
operator|<
name|minStamp
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"maxStamp is smaller than minStamp"
argument_list|)
throw|;
block|}
name|this
operator|.
name|minStamp
operator|=
name|minStamp
expr_stmt|;
name|this
operator|.
name|maxStamp
operator|=
name|maxStamp
expr_stmt|;
block|}
comment|/**    * Represents interval [minStamp, maxStamp)    * @param minStamp the minimum timestamp, inclusive    * @param maxStamp the maximum timestamp, exclusive    * @throws IOException    */
specifier|public
name|TimeRange
parameter_list|(
name|byte
index|[]
name|minStamp
parameter_list|,
name|byte
index|[]
name|maxStamp
parameter_list|)
throws|throws
name|IOException
block|{
name|this
argument_list|(
name|Bytes
operator|.
name|toLong
argument_list|(
name|minStamp
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toLong
argument_list|(
name|maxStamp
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * @return the smallest timestamp that should be considered    */
specifier|public
name|long
name|getMin
parameter_list|()
block|{
return|return
name|minStamp
return|;
block|}
comment|/**    * @return the biggest timestamp that should be considered    */
specifier|public
name|long
name|getMax
parameter_list|()
block|{
return|return
name|maxStamp
return|;
block|}
comment|/**    * Check if it is for all time    * @return true if it is for all time    */
specifier|public
name|boolean
name|isAllTime
parameter_list|()
block|{
return|return
name|allTime
return|;
block|}
comment|/**    * Check if the specified timestamp is within this TimeRange.    *<p>    * Returns true if within interval [minStamp, maxStamp), false    * if not.    * @param bytes timestamp to check    * @param offset offset into the bytes    * @return true if within TimeRange, false if not    */
specifier|public
name|boolean
name|withinTimeRange
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|,
name|int
name|offset
parameter_list|)
block|{
if|if
condition|(
name|allTime
condition|)
return|return
literal|true
return|;
return|return
name|withinTimeRange
argument_list|(
name|Bytes
operator|.
name|toLong
argument_list|(
name|bytes
argument_list|,
name|offset
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Check if the specified timestamp is within this TimeRange.    *<p>    * Returns true if within interval [minStamp, maxStamp), false    * if not.    * @param timestamp timestamp to check    * @return true if within TimeRange, false if not    */
specifier|public
name|boolean
name|withinTimeRange
parameter_list|(
name|long
name|timestamp
parameter_list|)
block|{
if|if
condition|(
name|allTime
condition|)
return|return
literal|true
return|;
comment|// check if>= minStamp
return|return
operator|(
name|minStamp
operator|<=
name|timestamp
operator|&&
name|timestamp
operator|<
name|maxStamp
operator|)
return|;
block|}
comment|/**    * Check if the specified timestamp is within this TimeRange.    *<p>    * Returns true if within interval [minStamp, maxStamp), false    * if not.    * @param timestamp timestamp to check    * @return true if within TimeRange, false if not    */
specifier|public
name|boolean
name|withinOrAfterTimeRange
parameter_list|(
name|long
name|timestamp
parameter_list|)
block|{
if|if
condition|(
name|allTime
condition|)
return|return
literal|true
return|;
comment|// check if>= minStamp
return|return
operator|(
name|timestamp
operator|>=
name|minStamp
operator|)
return|;
block|}
comment|/**    * Compare the timestamp to timerange    * @param timestamp    * @return -1 if timestamp is less than timerange,    * 0 if timestamp is within timerange,    * 1 if timestamp is greater than timerange    */
specifier|public
name|int
name|compare
parameter_list|(
name|long
name|timestamp
parameter_list|)
block|{
if|if
condition|(
name|timestamp
operator|<
name|minStamp
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
elseif|else
if|if
condition|(
name|timestamp
operator|>=
name|maxStamp
condition|)
block|{
return|return
literal|1
return|;
block|}
else|else
block|{
return|return
literal|0
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"maxStamp="
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|this
operator|.
name|maxStamp
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", minStamp="
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|this
operator|.
name|minStamp
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

