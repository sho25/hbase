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
name|codec
operator|.
name|prefixtree
operator|.
name|decode
operator|.
name|column
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
name|hadoop
operator|.
name|hbase
operator|.
name|codec
operator|.
name|prefixtree
operator|.
name|PrefixTreeBlockMeta
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
name|codec
operator|.
name|prefixtree
operator|.
name|encode
operator|.
name|other
operator|.
name|ColumnNodeType
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
name|nio
operator|.
name|ByteBuff
import|;
end_import

begin_comment
comment|/**  * Position one of these appropriately in the data block and you can call its methods to retrieve  * the family or qualifier at the current position.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ColumnReader
block|{
comment|/****************** fields *************************/
specifier|protected
name|PrefixTreeBlockMeta
name|blockMeta
decl_stmt|;
specifier|protected
name|byte
index|[]
name|columnBuffer
decl_stmt|;
specifier|protected
name|int
name|columnOffset
decl_stmt|;
specifier|protected
name|int
name|columnLength
decl_stmt|;
specifier|protected
name|ColumnNodeType
name|nodeType
decl_stmt|;
specifier|protected
name|ColumnNodeReader
name|columnNodeReader
decl_stmt|;
comment|/******************** construct *******************/
specifier|public
name|ColumnReader
parameter_list|(
name|byte
index|[]
name|columnBuffer
parameter_list|,
name|ColumnNodeType
name|nodeType
parameter_list|)
block|{
name|this
operator|.
name|columnBuffer
operator|=
name|columnBuffer
expr_stmt|;
name|this
operator|.
name|nodeType
operator|=
name|nodeType
expr_stmt|;
name|this
operator|.
name|columnNodeReader
operator|=
operator|new
name|ColumnNodeReader
argument_list|(
name|columnBuffer
argument_list|,
name|nodeType
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|initOnBlock
parameter_list|(
name|PrefixTreeBlockMeta
name|blockMeta
parameter_list|,
name|ByteBuff
name|block
parameter_list|)
block|{
name|this
operator|.
name|blockMeta
operator|=
name|blockMeta
expr_stmt|;
name|clearColumnBuffer
argument_list|()
expr_stmt|;
name|columnNodeReader
operator|.
name|initOnBlock
argument_list|(
name|blockMeta
argument_list|,
name|block
argument_list|)
expr_stmt|;
block|}
comment|/********************* methods *******************/
specifier|public
name|ColumnReader
name|populateBuffer
parameter_list|(
name|int
name|offsetIntoColumnData
parameter_list|)
block|{
name|clearColumnBuffer
argument_list|()
expr_stmt|;
name|int
name|nextRelativeOffset
init|=
name|offsetIntoColumnData
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|int
name|absoluteOffset
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|nodeType
operator|==
name|ColumnNodeType
operator|.
name|FAMILY
condition|)
block|{
name|absoluteOffset
operator|=
name|blockMeta
operator|.
name|getAbsoluteFamilyOffset
argument_list|()
operator|+
name|nextRelativeOffset
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|nodeType
operator|==
name|ColumnNodeType
operator|.
name|QUALIFIER
condition|)
block|{
name|absoluteOffset
operator|=
name|blockMeta
operator|.
name|getAbsoluteQualifierOffset
argument_list|()
operator|+
name|nextRelativeOffset
expr_stmt|;
block|}
else|else
block|{
name|absoluteOffset
operator|=
name|blockMeta
operator|.
name|getAbsoluteTagsOffset
argument_list|()
operator|+
name|nextRelativeOffset
expr_stmt|;
block|}
name|columnNodeReader
operator|.
name|positionAt
argument_list|(
name|absoluteOffset
argument_list|)
expr_stmt|;
name|columnOffset
operator|-=
name|columnNodeReader
operator|.
name|getTokenLength
argument_list|()
expr_stmt|;
name|columnLength
operator|+=
name|columnNodeReader
operator|.
name|getTokenLength
argument_list|()
expr_stmt|;
name|columnNodeReader
operator|.
name|prependTokenToBuffer
argument_list|(
name|columnOffset
argument_list|)
expr_stmt|;
if|if
condition|(
name|columnNodeReader
operator|.
name|isRoot
argument_list|()
condition|)
block|{
return|return
name|this
return|;
block|}
name|nextRelativeOffset
operator|=
name|columnNodeReader
operator|.
name|getParentStartPosition
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|byte
index|[]
name|copyBufferToNewArray
parameter_list|()
block|{
comment|// for testing
name|byte
index|[]
name|out
init|=
operator|new
name|byte
index|[
name|columnLength
index|]
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|columnBuffer
argument_list|,
name|columnOffset
argument_list|,
name|out
argument_list|,
literal|0
argument_list|,
name|out
operator|.
name|length
argument_list|)
expr_stmt|;
return|return
name|out
return|;
block|}
specifier|public
name|int
name|getColumnLength
parameter_list|()
block|{
return|return
name|columnLength
return|;
block|}
specifier|public
name|void
name|clearColumnBuffer
parameter_list|()
block|{
name|columnOffset
operator|=
name|columnBuffer
operator|.
name|length
expr_stmt|;
name|columnLength
operator|=
literal|0
expr_stmt|;
block|}
comment|/****************************** get/set *************************************/
specifier|public
name|int
name|getColumnOffset
parameter_list|()
block|{
return|return
name|columnOffset
return|;
block|}
block|}
end_class

end_unit

