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
name|util
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
name|Random
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
name|CellScanner
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
name|Tag
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
name|client
operator|.
name|Put
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
name|LoadTestDataGeneratorWithTags
extends|extends
name|DefaultDataGenerator
block|{
specifier|private
name|int
name|minNumTags
decl_stmt|,
name|maxNumTags
decl_stmt|;
specifier|private
name|int
name|minTagLength
decl_stmt|,
name|maxTagLength
decl_stmt|;
specifier|private
name|Random
name|random
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
specifier|public
name|LoadTestDataGeneratorWithTags
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
if|if
condition|(
name|args
operator|.
name|length
operator|!=
literal|4
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"LoadTestDataGeneratorWithTags must have "
operator|+
literal|"4 initialization arguments. ie. minNumTags:maxNumTags:minTagLength:maxTagLength"
argument_list|)
throw|;
block|}
comment|// 1st arg in args is the min number of tags to be used with every cell
name|this
operator|.
name|minNumTags
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|args
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
comment|// 2nd arg in args is the max number of tags to be used with every cell
name|this
operator|.
name|maxNumTags
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|args
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
comment|// 3rd arg in args is the min tag length
name|this
operator|.
name|minTagLength
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
comment|// 4th arg in args is the max tag length
name|this
operator|.
name|maxTagLength
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|args
index|[
literal|3
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
name|m
operator|instanceof
name|Put
condition|)
block|{
name|List
argument_list|<
name|Cell
argument_list|>
name|updatedCells
init|=
operator|new
name|ArrayList
argument_list|<
name|Cell
argument_list|>
argument_list|()
decl_stmt|;
name|int
name|numTags
decl_stmt|;
if|if
condition|(
name|minNumTags
operator|==
name|maxNumTags
condition|)
block|{
name|numTags
operator|=
name|minNumTags
expr_stmt|;
block|}
else|else
block|{
name|numTags
operator|=
name|minNumTags
operator|+
name|random
operator|.
name|nextInt
argument_list|(
name|maxNumTags
operator|-
name|minNumTags
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|Tag
argument_list|>
name|tags
decl_stmt|;
for|for
control|(
name|CellScanner
name|cellScanner
init|=
name|m
operator|.
name|cellScanner
argument_list|()
init|;
name|cellScanner
operator|.
name|advance
argument_list|()
condition|;
control|)
block|{
name|Cell
name|cell
init|=
name|cellScanner
operator|.
name|current
argument_list|()
decl_stmt|;
name|byte
index|[]
name|tag
init|=
name|LoadTestTool
operator|.
name|generateData
argument_list|(
name|random
argument_list|,
name|minTagLength
operator|+
name|random
operator|.
name|nextInt
argument_list|(
name|maxTagLength
operator|-
name|minTagLength
argument_list|)
argument_list|)
decl_stmt|;
name|tags
operator|=
operator|new
name|ArrayList
argument_list|<
name|Tag
argument_list|>
argument_list|()
expr_stmt|;
for|for
control|(
name|int
name|n
init|=
literal|0
init|;
name|n
operator|<
name|numTags
condition|;
name|n
operator|++
control|)
block|{
name|tags
operator|.
name|add
argument_list|(
operator|new
name|Tag
argument_list|(
operator|(
name|byte
operator|)
literal|127
argument_list|,
name|tag
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|Cell
name|updatedCell
init|=
operator|new
name|KeyValue
argument_list|(
name|cell
operator|.
name|getRowArray
argument_list|()
argument_list|,
name|cell
operator|.
name|getRowOffset
argument_list|()
argument_list|,
name|cell
operator|.
name|getRowLength
argument_list|()
argument_list|,
name|cell
operator|.
name|getFamilyArray
argument_list|()
argument_list|,
name|cell
operator|.
name|getFamilyOffset
argument_list|()
argument_list|,
name|cell
operator|.
name|getFamilyLength
argument_list|()
argument_list|,
name|cell
operator|.
name|getQualifierArray
argument_list|()
argument_list|,
name|cell
operator|.
name|getQualifierOffset
argument_list|()
argument_list|,
name|cell
operator|.
name|getQualifierLength
argument_list|()
argument_list|,
name|cell
operator|.
name|getTimestamp
argument_list|()
argument_list|,
name|Type
operator|.
name|codeToType
argument_list|(
name|cell
operator|.
name|getTypeByte
argument_list|()
argument_list|)
argument_list|,
name|cell
operator|.
name|getValueArray
argument_list|()
argument_list|,
name|cell
operator|.
name|getValueOffset
argument_list|()
argument_list|,
name|cell
operator|.
name|getValueLength
argument_list|()
argument_list|,
name|tags
argument_list|)
decl_stmt|;
name|updatedCells
operator|.
name|add
argument_list|(
name|updatedCell
argument_list|)
expr_stmt|;
block|}
name|m
operator|.
name|getFamilyCellMap
argument_list|()
operator|.
name|clear
argument_list|()
expr_stmt|;
comment|// Clear and add new Cells to the Mutation.
for|for
control|(
name|Cell
name|cell
range|:
name|updatedCells
control|)
block|{
operator|(
operator|(
name|Put
operator|)
name|m
operator|)
operator|.
name|add
argument_list|(
name|cell
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|m
return|;
block|}
block|}
end_class

end_unit

