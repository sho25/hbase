begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements. See the NOTICE file distributed with this  * work for additional information regarding copyright ownership. The ASF  * licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the  * License for the specific language governing permissions and limitations  * under the License.  */
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
name|security
operator|.
name|visibility
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
name|client
operator|.
name|Get
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
name|client
operator|.
name|Mutation
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
name|MultiThreadedAction
operator|.
name|DefaultDataGenerator
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|LoadTestDataGeneratorWithVisibilityLabels
extends|extends
name|DefaultDataGenerator
block|{
specifier|private
specifier|static
specifier|final
name|String
name|COMMA
init|=
literal|","
decl_stmt|;
specifier|private
name|String
index|[]
name|visibilityExps
init|=
literal|null
decl_stmt|;
specifier|private
name|String
index|[]
index|[]
name|authorizations
init|=
literal|null
decl_stmt|;
specifier|public
name|LoadTestDataGeneratorWithVisibilityLabels
parameter_list|(
name|int
name|minValueSize
parameter_list|,
name|int
name|maxValueSize
parameter_list|,
name|int
name|minColumnsPerKey
parameter_list|,
name|int
name|maxColumnsPerKey
parameter_list|,
name|byte
index|[]
modifier|...
name|columnFamilies
parameter_list|)
block|{
name|super
argument_list|(
name|minValueSize
argument_list|,
name|maxValueSize
argument_list|,
name|minColumnsPerKey
argument_list|,
name|maxColumnsPerKey
argument_list|,
name|columnFamilies
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|initialize
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
block|{
name|super
operator|.
name|initialize
argument_list|(
name|args
argument_list|)
expr_stmt|;
if|if
condition|(
name|args
operator|.
name|length
argument_list|<
literal|1
operator|||
name|args
operator|.
name|length
argument_list|>
literal|2
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"LoadTestDataGeneratorWithVisibilityLabels can have "
operator|+
literal|"1 or 2 initialization arguments"
argument_list|)
throw|;
block|}
comment|// 1st arg in args is supposed to be the visibilityExps to be used with Mutations.
name|String
name|temp
init|=
name|args
index|[
literal|0
index|]
decl_stmt|;
comment|// This will be comma separated list of expressions.
name|this
operator|.
name|visibilityExps
operator|=
name|temp
operator|.
name|split
argument_list|(
name|COMMA
argument_list|)
expr_stmt|;
comment|// 2nd arg in args,if present, is supposed to be comma separated set of authorizations to be
comment|// used with Gets. Each of the set will be comma separated within square brackets.
comment|// Eg: [secret,private],[confidential,private],[public]
if|if
condition|(
name|args
operator|.
name|length
operator|==
literal|2
condition|)
block|{
name|this
operator|.
name|authorizations
operator|=
name|toAuthorizationsSet
argument_list|(
name|args
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|String
index|[]
index|[]
name|toAuthorizationsSet
parameter_list|(
name|String
name|authorizationsStr
parameter_list|)
block|{
comment|// Eg: [secret,private],[confidential,private],[public]
name|String
index|[]
name|split
init|=
name|authorizationsStr
operator|.
name|split
argument_list|(
literal|"],"
argument_list|)
decl_stmt|;
name|String
index|[]
index|[]
name|result
init|=
operator|new
name|String
index|[
name|split
operator|.
name|length
index|]
index|[]
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
name|split
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|String
name|s
init|=
name|split
index|[
name|i
index|]
operator|.
name|trim
argument_list|()
decl_stmt|;
assert|assert
name|s
operator|.
name|charAt
argument_list|(
literal|0
argument_list|)
operator|==
literal|'['
assert|;
name|s
operator|=
name|s
operator|.
name|substring
argument_list|(
literal|1
argument_list|)
expr_stmt|;
if|if
condition|(
name|i
operator|==
name|split
operator|.
name|length
operator|-
literal|1
condition|)
block|{
assert|assert
name|s
operator|.
name|charAt
argument_list|(
name|s
operator|.
name|length
argument_list|()
operator|-
literal|1
argument_list|)
operator|==
literal|']'
assert|;
name|s
operator|=
name|s
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|s
operator|.
name|length
argument_list|()
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
name|String
index|[]
name|tmp
init|=
name|s
operator|.
name|split
argument_list|(
name|COMMA
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|tmp
operator|.
name|length
condition|;
name|j
operator|++
control|)
block|{
name|tmp
index|[
name|j
index|]
operator|=
name|tmp
index|[
name|j
index|]
operator|.
name|trim
argument_list|()
expr_stmt|;
block|}
name|result
index|[
name|i
index|]
operator|=
name|tmp
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
annotation|@
name|Override
specifier|public
name|Mutation
name|beforeMutate
parameter_list|(
name|long
name|rowkeyBase
parameter_list|,
name|Mutation
name|m
parameter_list|)
throws|throws
name|IOException
block|{
name|m
operator|.
name|setCellVisibility
argument_list|(
operator|new
name|CellVisibility
argument_list|(
name|this
operator|.
name|visibilityExps
index|[
operator|(
name|int
operator|)
name|rowkeyBase
operator|%
name|this
operator|.
name|visibilityExps
operator|.
name|length
index|]
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|m
return|;
block|}
annotation|@
name|Override
specifier|public
name|Get
name|beforeGet
parameter_list|(
name|long
name|rowkeyBase
parameter_list|,
name|Get
name|get
parameter_list|)
block|{
name|get
operator|.
name|setAuthorizations
argument_list|(
operator|new
name|Authorizations
argument_list|(
name|authorizations
index|[
call|(
name|int
call|)
argument_list|(
name|rowkeyBase
operator|%
name|authorizations
operator|.
name|length
argument_list|)
index|]
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|get
return|;
block|}
block|}
end_class

end_unit

