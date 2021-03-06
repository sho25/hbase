begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements. See the NOTICE file distributed with this  * work for additional information regarding copyright ownership. The ASF  * licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the  * License for the specific language governing permissions and limitations  * under the License.  */
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
operator|.
name|test
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
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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
name|Delete
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
name|security
operator|.
name|access
operator|.
name|Permission
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
name|LoadTestDataGeneratorWithACL
extends|extends
name|DefaultDataGenerator
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|LoadTestDataGeneratorWithACL
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|String
index|[]
name|userNames
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|COMMA
init|=
literal|","
decl_stmt|;
specifier|private
name|int
name|specialPermCellInsertionFactor
init|=
literal|100
decl_stmt|;
specifier|public
name|LoadTestDataGeneratorWithACL
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
operator|!=
literal|3
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"LoadTestDataGeneratorWithACL can have "
operator|+
literal|"1st arguement which would be super user, the 2nd argument "
operator|+
literal|"would be the user list and the 3rd argument should be the factor representing "
operator|+
literal|"the row keys for which only write ACLs will be added."
argument_list|)
throw|;
block|}
name|String
name|temp
init|=
name|args
index|[
literal|1
index|]
decl_stmt|;
comment|// This will be comma separated list of expressions.
name|this
operator|.
name|userNames
operator|=
name|temp
operator|.
name|split
argument_list|(
name|COMMA
argument_list|)
expr_stmt|;
name|this
operator|.
name|specialPermCellInsertionFactor
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|args
index|[
literal|2
index|]
argument_list|)
expr_stmt|;
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
if|if
condition|(
operator|!
operator|(
name|m
operator|instanceof
name|Delete
operator|)
condition|)
block|{
if|if
condition|(
name|userNames
operator|!=
literal|null
operator|&&
name|userNames
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|int
name|mod
init|=
operator|(
operator|(
name|int
operator|)
name|rowkeyBase
operator|%
name|this
operator|.
name|userNames
operator|.
name|length
operator|)
decl_stmt|;
if|if
condition|(
operator|(
operator|(
name|int
operator|)
name|rowkeyBase
operator|%
name|specialPermCellInsertionFactor
operator|)
operator|==
literal|0
condition|)
block|{
comment|// These cells cannot be read back when running as user userName[mod]
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Adding special perm "
operator|+
name|rowkeyBase
argument_list|)
expr_stmt|;
block|}
name|m
operator|.
name|setACL
argument_list|(
name|userNames
index|[
name|mod
index|]
argument_list|,
operator|new
name|Permission
argument_list|(
name|Permission
operator|.
name|Action
operator|.
name|WRITE
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|m
operator|.
name|setACL
argument_list|(
name|userNames
index|[
name|mod
index|]
argument_list|,
operator|new
name|Permission
argument_list|(
name|Permission
operator|.
name|Action
operator|.
name|READ
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|m
return|;
block|}
block|}
end_class

end_unit

