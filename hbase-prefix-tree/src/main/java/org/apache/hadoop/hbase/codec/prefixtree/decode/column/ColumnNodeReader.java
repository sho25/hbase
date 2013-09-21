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
name|util
operator|.
name|vint
operator|.
name|UFIntTool
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
name|vint
operator|.
name|UVIntTool
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ColumnNodeReader
block|{
comment|/**************** fields ************************/
specifier|protected
name|PrefixTreeBlockMeta
name|blockMeta
decl_stmt|;
specifier|protected
name|byte
index|[]
name|block
decl_stmt|;
specifier|protected
name|ColumnNodeType
name|nodeType
decl_stmt|;
specifier|protected
name|byte
index|[]
name|columnBuffer
decl_stmt|;
specifier|protected
name|int
name|offsetIntoBlock
decl_stmt|;
specifier|protected
name|int
name|tokenOffsetIntoBlock
decl_stmt|;
specifier|protected
name|int
name|tokenLength
decl_stmt|;
specifier|protected
name|int
name|parentStartPosition
decl_stmt|;
comment|/************** construct *************************/
specifier|public
name|ColumnNodeReader
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
block|}
specifier|public
name|void
name|initOnBlock
parameter_list|(
name|PrefixTreeBlockMeta
name|blockMeta
parameter_list|,
name|byte
index|[]
name|block
parameter_list|)
block|{
name|this
operator|.
name|blockMeta
operator|=
name|blockMeta
expr_stmt|;
name|this
operator|.
name|block
operator|=
name|block
expr_stmt|;
block|}
comment|/************* methods *****************************/
specifier|public
name|void
name|positionAt
parameter_list|(
name|int
name|offsetIntoBlock
parameter_list|)
block|{
name|this
operator|.
name|offsetIntoBlock
operator|=
name|offsetIntoBlock
expr_stmt|;
name|tokenLength
operator|=
name|UVIntTool
operator|.
name|getInt
argument_list|(
name|block
argument_list|,
name|offsetIntoBlock
argument_list|)
expr_stmt|;
name|tokenOffsetIntoBlock
operator|=
name|offsetIntoBlock
operator|+
name|UVIntTool
operator|.
name|numBytes
argument_list|(
name|tokenLength
argument_list|)
expr_stmt|;
name|int
name|parentStartPositionIndex
init|=
name|tokenOffsetIntoBlock
operator|+
name|tokenLength
decl_stmt|;
name|int
name|offsetWidth
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
name|offsetWidth
operator|=
name|blockMeta
operator|.
name|getFamilyOffsetWidth
argument_list|()
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
name|offsetWidth
operator|=
name|blockMeta
operator|.
name|getQualifierOffsetWidth
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|offsetWidth
operator|=
name|blockMeta
operator|.
name|getTagsOffsetWidth
argument_list|()
expr_stmt|;
block|}
name|parentStartPosition
operator|=
operator|(
name|int
operator|)
name|UFIntTool
operator|.
name|fromBytes
argument_list|(
name|block
argument_list|,
name|parentStartPositionIndex
argument_list|,
name|offsetWidth
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|prependTokenToBuffer
parameter_list|(
name|int
name|bufferStartIndex
parameter_list|)
block|{
name|System
operator|.
name|arraycopy
argument_list|(
name|block
argument_list|,
name|tokenOffsetIntoBlock
argument_list|,
name|columnBuffer
argument_list|,
name|bufferStartIndex
argument_list|,
name|tokenLength
argument_list|)
expr_stmt|;
block|}
specifier|public
name|boolean
name|isRoot
parameter_list|()
block|{
if|if
condition|(
name|nodeType
operator|==
name|ColumnNodeType
operator|.
name|FAMILY
condition|)
block|{
return|return
name|offsetIntoBlock
operator|==
name|blockMeta
operator|.
name|getAbsoluteFamilyOffset
argument_list|()
return|;
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
return|return
name|offsetIntoBlock
operator|==
name|blockMeta
operator|.
name|getAbsoluteQualifierOffset
argument_list|()
return|;
block|}
else|else
block|{
return|return
name|offsetIntoBlock
operator|==
name|blockMeta
operator|.
name|getAbsoluteTagsOffset
argument_list|()
return|;
block|}
block|}
comment|/************** standard methods *********************/
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|super
operator|.
name|toString
argument_list|()
operator|+
literal|"["
operator|+
name|offsetIntoBlock
operator|+
literal|"]"
return|;
block|}
comment|/****************** get/set ****************************/
specifier|public
name|int
name|getTokenLength
parameter_list|()
block|{
return|return
name|tokenLength
return|;
block|}
specifier|public
name|int
name|getParentStartPosition
parameter_list|()
block|{
return|return
name|parentStartPosition
return|;
block|}
block|}
end_class

end_unit

