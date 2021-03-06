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
name|io
package|;
end_package

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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * Represents an interval of version timestamps. Presumes timestamps between  * {@link #INITIAL_MIN_TIMESTAMP} and {@link #INITIAL_MAX_TIMESTAMP} only. Gets freaked out if  * passed a timestamp that is< {@link #INITIAL_MIN_TIMESTAMP},  *<p>  * Evaluated according to minStamp&lt;= timestamp&lt; maxStamp or [minStamp,maxStamp) in interval  * notation.  *<p>  * Can be returned and read by clients. Should not be directly created by clients. Thus, all  * constructors are purposely @InterfaceAudience.Private.  *<p>  * Immutable. Thread-safe.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
class|class
name|TimeRange
block|{
specifier|public
specifier|static
specifier|final
name|long
name|INITIAL_MIN_TIMESTAMP
init|=
literal|0L
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|long
name|INITIAL_MAX_TIMESTAMP
init|=
name|Long
operator|.
name|MAX_VALUE
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|TimeRange
name|ALL_TIME
init|=
operator|new
name|TimeRange
argument_list|(
name|INITIAL_MIN_TIMESTAMP
argument_list|,
name|INITIAL_MAX_TIMESTAMP
argument_list|)
decl_stmt|;
specifier|public
specifier|static
name|TimeRange
name|allTime
parameter_list|()
block|{
return|return
name|ALL_TIME
return|;
block|}
specifier|public
specifier|static
name|TimeRange
name|at
parameter_list|(
name|long
name|ts
parameter_list|)
block|{
if|if
condition|(
name|ts
operator|<
literal|0
operator|||
name|ts
operator|==
name|Long
operator|.
name|MAX_VALUE
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"invalid ts:"
operator|+
name|ts
argument_list|)
throw|;
block|}
return|return
operator|new
name|TimeRange
argument_list|(
name|ts
argument_list|,
name|ts
operator|+
literal|1
argument_list|)
return|;
block|}
comment|/**    * Represents the time interval [minStamp, Long.MAX_VALUE)    * @param minStamp the minimum timestamp value, inclusive    */
specifier|public
specifier|static
name|TimeRange
name|from
parameter_list|(
name|long
name|minStamp
parameter_list|)
block|{
name|check
argument_list|(
name|minStamp
argument_list|,
name|INITIAL_MAX_TIMESTAMP
argument_list|)
expr_stmt|;
return|return
operator|new
name|TimeRange
argument_list|(
name|minStamp
argument_list|,
name|INITIAL_MAX_TIMESTAMP
argument_list|)
return|;
block|}
comment|/**    * Represents the time interval [0, maxStamp)    * @param maxStamp the minimum timestamp value, exclusive    */
specifier|public
specifier|static
name|TimeRange
name|until
parameter_list|(
name|long
name|maxStamp
parameter_list|)
block|{
name|check
argument_list|(
name|INITIAL_MIN_TIMESTAMP
argument_list|,
name|maxStamp
argument_list|)
expr_stmt|;
return|return
operator|new
name|TimeRange
argument_list|(
name|INITIAL_MIN_TIMESTAMP
argument_list|,
name|maxStamp
argument_list|)
return|;
block|}
comment|/**    * Represents the time interval [minStamp, maxStamp)    * @param minStamp the minimum timestamp, inclusive    * @param maxStamp the maximum timestamp, exclusive    */
specifier|public
specifier|static
name|TimeRange
name|between
parameter_list|(
name|long
name|minStamp
parameter_list|,
name|long
name|maxStamp
parameter_list|)
block|{
name|check
argument_list|(
name|minStamp
argument_list|,
name|maxStamp
argument_list|)
expr_stmt|;
return|return
operator|new
name|TimeRange
argument_list|(
name|minStamp
argument_list|,
name|maxStamp
argument_list|)
return|;
block|}
specifier|private
specifier|final
name|long
name|minStamp
decl_stmt|;
specifier|private
specifier|final
name|long
name|maxStamp
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|allTime
decl_stmt|;
comment|/**    * Default constructor.    * Represents interval [0, Long.MAX_VALUE) (allTime)    * @deprecated This is made @InterfaceAudience.Private in the 2.0 line and above and may be    * changed to private or removed in 3.0.    */
annotation|@
name|Deprecated
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
name|TimeRange
parameter_list|()
block|{
name|this
argument_list|(
name|INITIAL_MIN_TIMESTAMP
argument_list|,
name|INITIAL_MAX_TIMESTAMP
argument_list|)
expr_stmt|;
block|}
comment|/**    * Represents interval [minStamp, Long.MAX_VALUE)    * @param minStamp the minimum timestamp value, inclusive    * @deprecated This is made @InterfaceAudience.Private in the 2.0 line and above and may be    * changed to private or removed in 3.0.    */
annotation|@
name|Deprecated
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
name|TimeRange
parameter_list|(
name|long
name|minStamp
parameter_list|)
block|{
name|this
argument_list|(
name|minStamp
argument_list|,
name|INITIAL_MAX_TIMESTAMP
argument_list|)
expr_stmt|;
block|}
comment|/**    * Represents interval [minStamp, Long.MAX_VALUE)    * @param minStamp the minimum timestamp value, inclusive    * @deprecated This is made @InterfaceAudience.Private in the 2.0 line and above and may be    * changed to private or removed in 3.0.    */
annotation|@
name|Deprecated
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
name|TimeRange
parameter_list|(
name|byte
index|[]
name|minStamp
parameter_list|)
block|{
name|this
argument_list|(
name|Bytes
operator|.
name|toLong
argument_list|(
name|minStamp
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Represents interval [minStamp, maxStamp)    * @param minStamp the minimum timestamp, inclusive    * @param maxStamp the maximum timestamp, exclusive    * @deprecated This is made @InterfaceAudience.Private in the 2.0 line and above and may be    * changed to private or removed in 3.0.    */
annotation|@
name|Deprecated
annotation|@
name|InterfaceAudience
operator|.
name|Private
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
comment|/**    * Represents interval [minStamp, maxStamp)    * @param minStamp the minimum timestamp, inclusive    * @param maxStamp the maximum timestamp, exclusive    * @throws IllegalArgumentException if either<0,    * @deprecated This is made @InterfaceAudience.Private in the 2.0 line and above and may be    * changed to private or removed in 3.0.    */
annotation|@
name|Deprecated
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
name|TimeRange
parameter_list|(
name|long
name|minStamp
parameter_list|,
name|long
name|maxStamp
parameter_list|)
block|{
name|check
argument_list|(
name|minStamp
argument_list|,
name|maxStamp
argument_list|)
expr_stmt|;
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
name|this
operator|.
name|allTime
operator|=
name|isAllTime
argument_list|(
name|minStamp
argument_list|,
name|maxStamp
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|boolean
name|isAllTime
parameter_list|(
name|long
name|minStamp
parameter_list|,
name|long
name|maxStamp
parameter_list|)
block|{
return|return
name|minStamp
operator|==
name|INITIAL_MIN_TIMESTAMP
operator|&&
name|maxStamp
operator|==
name|INITIAL_MAX_TIMESTAMP
return|;
block|}
specifier|private
specifier|static
name|void
name|check
parameter_list|(
name|long
name|minStamp
parameter_list|,
name|long
name|maxStamp
parameter_list|)
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
literal|", maxStamp:"
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
name|IllegalArgumentException
argument_list|(
literal|"maxStamp is smaller than minStamp"
argument_list|)
throw|;
block|}
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
comment|/**    * Check if the specified timestamp is within this TimeRange.    *<p>    * Returns true if within interval [minStamp, maxStamp), false if not.    * @param bytes timestamp to check    * @param offset offset into the bytes    * @return true if within TimeRange, false if not    * @deprecated This is made @InterfaceAudience.Private in the 2.0 line and above and may be    *   changed to private or removed in 3.0. Use {@link #withinTimeRange(long)} instead    */
annotation|@
name|Deprecated
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
block|{
return|return
literal|true
return|;
block|}
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
assert|assert
name|timestamp
operator|>=
literal|0
assert|;
if|if
condition|(
name|this
operator|.
name|allTime
condition|)
block|{
return|return
literal|true
return|;
block|}
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
comment|/**    * Check if the range has any overlap with TimeRange    * @param tr TimeRange    * @return True if there is overlap, false otherwise    */
comment|// This method came from TimeRangeTracker. We used to go there for this function but better
comment|// to come here to the immutable, unsynchronized datastructure at read time.
specifier|public
name|boolean
name|includesTimeRange
parameter_list|(
specifier|final
name|TimeRange
name|tr
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|allTime
condition|)
block|{
return|return
literal|true
return|;
block|}
assert|assert
name|tr
operator|.
name|getMin
argument_list|()
operator|>=
literal|0
assert|;
return|return
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
return|;
block|}
comment|/**    * Check if the specified timestamp is within or after this TimeRange.    *<p>    * Returns true if greater than minStamp, false if not.    * @param timestamp timestamp to check    * @return true if within or after TimeRange, false if not    */
specifier|public
name|boolean
name|withinOrAfterTimeRange
parameter_list|(
name|long
name|timestamp
parameter_list|)
block|{
assert|assert
name|timestamp
operator|>=
literal|0
assert|;
if|if
condition|(
name|allTime
condition|)
block|{
return|return
literal|true
return|;
block|}
comment|// check if>= minStamp
return|return
name|timestamp
operator|>=
name|minStamp
return|;
block|}
comment|/**    * Compare the timestamp to timerange.    * @return -1 if timestamp is less than timerange,    * 0 if timestamp is within timerange,    * 1 if timestamp is greater than timerange    */
specifier|public
name|int
name|compare
parameter_list|(
name|long
name|timestamp
parameter_list|)
block|{
assert|assert
name|timestamp
operator|>=
literal|0
assert|;
if|if
condition|(
name|this
operator|.
name|allTime
condition|)
block|{
return|return
literal|0
return|;
block|}
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
return|return
name|timestamp
operator|>=
name|maxStamp
condition|?
literal|1
else|:
literal|0
return|;
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

