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
name|procedure2
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceStability
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
specifier|final
class|class
name|StringUtils
block|{
specifier|private
name|StringUtils
parameter_list|()
block|{}
specifier|public
specifier|static
name|String
name|humanTimeDiff
parameter_list|(
name|long
name|timeDiff
parameter_list|)
block|{
if|if
condition|(
name|timeDiff
operator|<
literal|1000
condition|)
block|{
return|return
name|String
operator|.
name|format
argument_list|(
literal|"%d msec"
argument_list|,
name|timeDiff
argument_list|)
return|;
block|}
name|StringBuilder
name|buf
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|long
name|hours
init|=
name|timeDiff
operator|/
operator|(
literal|60
operator|*
literal|60
operator|*
literal|1000
operator|)
decl_stmt|;
name|long
name|rem
init|=
operator|(
name|timeDiff
operator|%
operator|(
literal|60
operator|*
literal|60
operator|*
literal|1000
operator|)
operator|)
decl_stmt|;
name|long
name|minutes
init|=
name|rem
operator|/
operator|(
literal|60
operator|*
literal|1000
operator|)
decl_stmt|;
name|rem
operator|=
name|rem
operator|%
operator|(
literal|60
operator|*
literal|1000
operator|)
expr_stmt|;
name|float
name|seconds
init|=
name|rem
operator|/
literal|1000.0f
decl_stmt|;
if|if
condition|(
name|hours
operator|!=
literal|0
condition|)
block|{
name|buf
operator|.
name|append
argument_list|(
name|hours
argument_list|)
expr_stmt|;
name|buf
operator|.
name|append
argument_list|(
literal|" hrs, "
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|minutes
operator|!=
literal|0
condition|)
block|{
name|buf
operator|.
name|append
argument_list|(
name|minutes
argument_list|)
expr_stmt|;
name|buf
operator|.
name|append
argument_list|(
literal|" mins, "
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|hours
operator|>
literal|0
operator|||
name|minutes
operator|>
literal|0
condition|)
block|{
name|buf
operator|.
name|append
argument_list|(
name|seconds
argument_list|)
expr_stmt|;
name|buf
operator|.
name|append
argument_list|(
literal|" sec"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|buf
operator|.
name|append
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%.4f sec"
argument_list|,
name|seconds
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|buf
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|String
name|humanSize
parameter_list|(
name|double
name|size
parameter_list|)
block|{
if|if
condition|(
name|size
operator|>=
operator|(
literal|1L
operator|<<
literal|40
operator|)
condition|)
block|{
return|return
name|String
operator|.
name|format
argument_list|(
literal|"%.1f T"
argument_list|,
name|size
operator|/
operator|(
literal|1L
operator|<<
literal|40
operator|)
argument_list|)
return|;
block|}
if|if
condition|(
name|size
operator|>=
operator|(
literal|1L
operator|<<
literal|30
operator|)
condition|)
block|{
return|return
name|String
operator|.
name|format
argument_list|(
literal|"%.1f G"
argument_list|,
name|size
operator|/
operator|(
literal|1L
operator|<<
literal|30
operator|)
argument_list|)
return|;
block|}
if|if
condition|(
name|size
operator|>=
operator|(
literal|1L
operator|<<
literal|20
operator|)
condition|)
block|{
return|return
name|String
operator|.
name|format
argument_list|(
literal|"%.1f M"
argument_list|,
name|size
operator|/
operator|(
literal|1L
operator|<<
literal|20
operator|)
argument_list|)
return|;
block|}
if|if
condition|(
name|size
operator|>=
operator|(
literal|1L
operator|<<
literal|10
operator|)
condition|)
block|{
return|return
name|String
operator|.
name|format
argument_list|(
literal|"%.1f K"
argument_list|,
name|size
operator|/
operator|(
literal|1L
operator|<<
literal|10
operator|)
argument_list|)
return|;
block|}
return|return
name|String
operator|.
name|format
argument_list|(
literal|"%.0f"
argument_list|,
name|size
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|boolean
name|isEmpty
parameter_list|(
specifier|final
name|String
name|input
parameter_list|)
block|{
return|return
name|input
operator|==
literal|null
operator|||
name|input
operator|.
name|length
argument_list|()
operator|==
literal|0
return|;
block|}
specifier|public
specifier|static
name|String
name|buildString
parameter_list|(
specifier|final
name|String
modifier|...
name|parts
parameter_list|)
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
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
name|parts
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|parts
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|StringBuilder
name|appendStrings
parameter_list|(
specifier|final
name|StringBuilder
name|sb
parameter_list|,
specifier|final
name|String
modifier|...
name|parts
parameter_list|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|parts
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|parts
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
return|return
name|sb
return|;
block|}
block|}
end_class

end_unit

