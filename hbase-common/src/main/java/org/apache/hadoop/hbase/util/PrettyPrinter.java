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
name|util
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
name|classification
operator|.
name|InterfaceAudience
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|PrettyPrinter
block|{
specifier|public
enum|enum
name|Unit
block|{
name|TIME_INTERVAL
block|,
name|NONE
block|}
specifier|public
specifier|static
name|String
name|format
parameter_list|(
specifier|final
name|String
name|value
parameter_list|,
specifier|final
name|Unit
name|unit
parameter_list|)
block|{
name|StringBuilder
name|human
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
switch|switch
condition|(
name|unit
condition|)
block|{
case|case
name|TIME_INTERVAL
case|:
name|human
operator|.
name|append
argument_list|(
name|humanReadableTTL
argument_list|(
name|Long
operator|.
name|valueOf
argument_list|(
name|value
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
break|break;
default|default:
name|human
operator|.
name|append
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
return|return
name|human
operator|.
name|toString
argument_list|()
return|;
block|}
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
literal|"ICAST_INTEGER_MULTIPLY_CAST_TO_LONG"
argument_list|,
name|justification
operator|=
literal|"Will not overflow"
argument_list|)
specifier|private
specifier|static
name|String
name|humanReadableTTL
parameter_list|(
specifier|final
name|long
name|interval
parameter_list|)
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|int
name|days
decl_stmt|,
name|hours
decl_stmt|,
name|minutes
decl_stmt|,
name|seconds
decl_stmt|;
comment|// edge cases first
if|if
condition|(
name|interval
operator|==
name|Integer
operator|.
name|MAX_VALUE
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"FOREVER"
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
if|if
condition|(
name|interval
operator|<
name|HConstants
operator|.
name|MINUTE_IN_SECONDS
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|interval
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" SECOND"
argument_list|)
operator|.
name|append
argument_list|(
name|interval
operator|==
literal|1
condition|?
literal|""
else|:
literal|"S"
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
name|days
operator|=
call|(
name|int
call|)
argument_list|(
name|interval
operator|/
name|HConstants
operator|.
name|DAY_IN_SECONDS
argument_list|)
expr_stmt|;
name|hours
operator|=
call|(
name|int
call|)
argument_list|(
name|interval
operator|-
name|HConstants
operator|.
name|DAY_IN_SECONDS
operator|*
name|days
argument_list|)
operator|/
name|HConstants
operator|.
name|HOUR_IN_SECONDS
expr_stmt|;
name|minutes
operator|=
call|(
name|int
call|)
argument_list|(
name|interval
operator|-
name|HConstants
operator|.
name|DAY_IN_SECONDS
operator|*
name|days
operator|-
name|HConstants
operator|.
name|HOUR_IN_SECONDS
operator|*
name|hours
argument_list|)
operator|/
name|HConstants
operator|.
name|MINUTE_IN_SECONDS
expr_stmt|;
name|seconds
operator|=
call|(
name|int
call|)
argument_list|(
name|interval
operator|-
name|HConstants
operator|.
name|DAY_IN_SECONDS
operator|*
name|days
operator|-
name|HConstants
operator|.
name|HOUR_IN_SECONDS
operator|*
name|hours
operator|-
name|HConstants
operator|.
name|MINUTE_IN_SECONDS
operator|*
name|minutes
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|interval
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" SECONDS ("
argument_list|)
expr_stmt|;
if|if
condition|(
name|days
operator|>
literal|0
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|days
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" DAY"
argument_list|)
operator|.
name|append
argument_list|(
name|days
operator|==
literal|1
condition|?
literal|""
else|:
literal|"S"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|hours
operator|>
literal|0
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|days
operator|>
literal|0
condition|?
literal|" "
else|:
literal|""
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|hours
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" HOUR"
argument_list|)
operator|.
name|append
argument_list|(
name|hours
operator|==
literal|1
condition|?
literal|""
else|:
literal|"S"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|minutes
operator|>
literal|0
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|days
operator|+
name|hours
operator|>
literal|0
condition|?
literal|" "
else|:
literal|""
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|minutes
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" MINUTE"
argument_list|)
operator|.
name|append
argument_list|(
name|minutes
operator|==
literal|1
condition|?
literal|""
else|:
literal|"S"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|seconds
operator|>
literal|0
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|days
operator|+
name|hours
operator|+
name|minutes
operator|>
literal|0
condition|?
literal|" "
else|:
literal|""
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|seconds
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" SECOND"
argument_list|)
operator|.
name|append
argument_list|(
name|minutes
operator|==
literal|1
condition|?
literal|""
else|:
literal|"S"
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|")"
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

