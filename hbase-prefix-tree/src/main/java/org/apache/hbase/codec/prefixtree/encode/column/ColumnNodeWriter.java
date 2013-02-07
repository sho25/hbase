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
name|hbase
operator|.
name|codec
operator|.
name|prefixtree
operator|.
name|encode
operator|.
name|column
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
name|io
operator|.
name|OutputStream
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
name|util
operator|.
name|ByteRange
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
name|Strings
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
name|hbase
operator|.
name|codec
operator|.
name|prefixtree
operator|.
name|encode
operator|.
name|tokenize
operator|.
name|TokenizerNode
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
name|hbase
operator|.
name|util
operator|.
name|vint
operator|.
name|UVIntTool
import|;
end_import

begin_comment
comment|/**  * Column nodes can be either family nodes or qualifier nodes, as both sections encode similarly.  * The family and qualifier sections of the data block are made of 1 or more of these nodes.  *<p/>  * Each node is composed of 3 sections:<br/>  *<li>tokenLength: UVInt (normally 1 byte) indicating the number of token bytes  *<li>token[]: the actual token bytes  *<li>parentStartPosition: the offset of the next node from the start of the family or qualifier  * section  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ColumnNodeWriter
block|{
comment|/************* fields ****************************/
specifier|protected
name|TokenizerNode
name|builderNode
decl_stmt|;
specifier|protected
name|PrefixTreeBlockMeta
name|blockMeta
decl_stmt|;
specifier|protected
name|boolean
name|familyVsQualifier
decl_stmt|;
specifier|protected
name|int
name|tokenLength
decl_stmt|;
specifier|protected
name|byte
index|[]
name|token
decl_stmt|;
specifier|protected
name|int
name|parentStartPosition
decl_stmt|;
comment|/*************** construct **************************/
specifier|public
name|ColumnNodeWriter
parameter_list|(
name|PrefixTreeBlockMeta
name|blockMeta
parameter_list|,
name|TokenizerNode
name|builderNode
parameter_list|,
name|boolean
name|familyVsQualifier
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
name|builderNode
operator|=
name|builderNode
expr_stmt|;
name|this
operator|.
name|familyVsQualifier
operator|=
name|familyVsQualifier
expr_stmt|;
name|calculateTokenLength
argument_list|()
expr_stmt|;
block|}
comment|/************* methods *******************************/
specifier|public
name|boolean
name|isRoot
parameter_list|()
block|{
return|return
name|parentStartPosition
operator|==
literal|0
return|;
block|}
specifier|private
name|void
name|calculateTokenLength
parameter_list|()
block|{
name|tokenLength
operator|=
name|builderNode
operator|.
name|getTokenLength
argument_list|()
expr_stmt|;
name|token
operator|=
operator|new
name|byte
index|[
name|tokenLength
index|]
expr_stmt|;
block|}
comment|/**    * This method is called before blockMeta.qualifierOffsetWidth is known, so we pass in a    * placeholder.    * @param offsetWidthPlaceholder the placeholder    * @return node width    */
specifier|public
name|int
name|getWidthUsingPlaceholderForOffsetWidth
parameter_list|(
name|int
name|offsetWidthPlaceholder
parameter_list|)
block|{
name|int
name|width
init|=
literal|0
decl_stmt|;
name|width
operator|+=
name|UVIntTool
operator|.
name|numBytes
argument_list|(
name|tokenLength
argument_list|)
expr_stmt|;
name|width
operator|+=
name|token
operator|.
name|length
expr_stmt|;
name|width
operator|+=
name|offsetWidthPlaceholder
expr_stmt|;
return|return
name|width
return|;
block|}
specifier|public
name|void
name|writeBytes
parameter_list|(
name|OutputStream
name|os
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|parentOffsetWidth
decl_stmt|;
if|if
condition|(
name|familyVsQualifier
condition|)
block|{
name|parentOffsetWidth
operator|=
name|blockMeta
operator|.
name|getFamilyOffsetWidth
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|parentOffsetWidth
operator|=
name|blockMeta
operator|.
name|getQualifierOffsetWidth
argument_list|()
expr_stmt|;
block|}
name|UVIntTool
operator|.
name|writeBytes
argument_list|(
name|tokenLength
argument_list|,
name|os
argument_list|)
expr_stmt|;
name|os
operator|.
name|write
argument_list|(
name|token
argument_list|)
expr_stmt|;
name|UFIntTool
operator|.
name|writeBytes
argument_list|(
name|parentOffsetWidth
argument_list|,
name|parentStartPosition
argument_list|,
name|os
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setTokenBytes
parameter_list|(
name|ByteRange
name|source
parameter_list|)
block|{
name|source
operator|.
name|deepCopySubRangeTo
argument_list|(
literal|0
argument_list|,
name|tokenLength
argument_list|,
name|token
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
comment|/****************** standard methods ************************/
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
name|Strings
operator|.
name|padFront
argument_list|(
name|builderNode
operator|.
name|getOutputArrayOffset
argument_list|()
operator|+
literal|""
argument_list|,
literal|' '
argument_list|,
literal|3
argument_list|)
operator|+
literal|","
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"["
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|token
argument_list|)
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"]->"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|parentStartPosition
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/************************** get/set ***********************/
specifier|public
name|void
name|setParentStartPosition
parameter_list|(
name|int
name|parentStartPosition
parameter_list|)
block|{
name|this
operator|.
name|parentStartPosition
operator|=
name|parentStartPosition
expr_stmt|;
block|}
block|}
end_class

end_unit

