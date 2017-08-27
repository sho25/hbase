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
name|ArrayUtils
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|ExtendedCellBuilderImpl
implements|implements
name|ExtendedCellBuilder
block|{
specifier|protected
name|byte
index|[]
name|row
init|=
literal|null
decl_stmt|;
specifier|protected
name|int
name|rOffset
init|=
literal|0
decl_stmt|;
specifier|protected
name|int
name|rLength
init|=
literal|0
decl_stmt|;
specifier|protected
name|byte
index|[]
name|family
init|=
literal|null
decl_stmt|;
specifier|protected
name|int
name|fOffset
init|=
literal|0
decl_stmt|;
specifier|protected
name|int
name|fLength
init|=
literal|0
decl_stmt|;
specifier|protected
name|byte
index|[]
name|qualifier
init|=
literal|null
decl_stmt|;
specifier|protected
name|int
name|qOffset
init|=
literal|0
decl_stmt|;
specifier|protected
name|int
name|qLength
init|=
literal|0
decl_stmt|;
specifier|protected
name|long
name|timestamp
init|=
name|HConstants
operator|.
name|LATEST_TIMESTAMP
decl_stmt|;
specifier|protected
name|Byte
name|type
init|=
literal|null
decl_stmt|;
specifier|protected
name|byte
index|[]
name|value
init|=
literal|null
decl_stmt|;
specifier|protected
name|int
name|vOffset
init|=
literal|0
decl_stmt|;
specifier|protected
name|int
name|vLength
init|=
literal|0
decl_stmt|;
specifier|protected
name|long
name|seqId
init|=
literal|0
decl_stmt|;
specifier|protected
name|byte
index|[]
name|tags
init|=
literal|null
decl_stmt|;
specifier|protected
name|int
name|tagsOffset
init|=
literal|0
decl_stmt|;
specifier|protected
name|int
name|tagsLength
init|=
literal|0
decl_stmt|;
annotation|@
name|Override
specifier|public
name|ExtendedCellBuilder
name|setRow
parameter_list|(
specifier|final
name|byte
index|[]
name|row
parameter_list|)
block|{
return|return
name|setRow
argument_list|(
name|row
argument_list|,
literal|0
argument_list|,
name|ArrayUtils
operator|.
name|length
argument_list|(
name|row
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|ExtendedCellBuilder
name|setRow
parameter_list|(
specifier|final
name|byte
index|[]
name|row
parameter_list|,
name|int
name|rOffset
parameter_list|,
name|int
name|rLength
parameter_list|)
block|{
name|this
operator|.
name|row
operator|=
name|row
expr_stmt|;
name|this
operator|.
name|rOffset
operator|=
name|rOffset
expr_stmt|;
name|this
operator|.
name|rLength
operator|=
name|rLength
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|ExtendedCellBuilder
name|setFamily
parameter_list|(
specifier|final
name|byte
index|[]
name|family
parameter_list|)
block|{
return|return
name|setFamily
argument_list|(
name|family
argument_list|,
literal|0
argument_list|,
name|ArrayUtils
operator|.
name|length
argument_list|(
name|family
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|ExtendedCellBuilder
name|setFamily
parameter_list|(
specifier|final
name|byte
index|[]
name|family
parameter_list|,
name|int
name|fOffset
parameter_list|,
name|int
name|fLength
parameter_list|)
block|{
name|this
operator|.
name|family
operator|=
name|family
expr_stmt|;
name|this
operator|.
name|fOffset
operator|=
name|fOffset
expr_stmt|;
name|this
operator|.
name|fLength
operator|=
name|fLength
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|ExtendedCellBuilder
name|setQualifier
parameter_list|(
specifier|final
name|byte
index|[]
name|qualifier
parameter_list|)
block|{
return|return
name|setQualifier
argument_list|(
name|qualifier
argument_list|,
literal|0
argument_list|,
name|ArrayUtils
operator|.
name|length
argument_list|(
name|qualifier
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|ExtendedCellBuilder
name|setQualifier
parameter_list|(
specifier|final
name|byte
index|[]
name|qualifier
parameter_list|,
name|int
name|qOffset
parameter_list|,
name|int
name|qLength
parameter_list|)
block|{
name|this
operator|.
name|qualifier
operator|=
name|qualifier
expr_stmt|;
name|this
operator|.
name|qOffset
operator|=
name|qOffset
expr_stmt|;
name|this
operator|.
name|qLength
operator|=
name|qLength
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|ExtendedCellBuilder
name|setTimestamp
parameter_list|(
specifier|final
name|long
name|timestamp
parameter_list|)
block|{
name|this
operator|.
name|timestamp
operator|=
name|timestamp
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|ExtendedCellBuilder
name|setType
parameter_list|(
specifier|final
name|byte
name|type
parameter_list|)
block|{
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|ExtendedCellBuilder
name|setValue
parameter_list|(
specifier|final
name|byte
index|[]
name|value
parameter_list|)
block|{
return|return
name|setValue
argument_list|(
name|value
argument_list|,
literal|0
argument_list|,
name|ArrayUtils
operator|.
name|length
argument_list|(
name|value
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|ExtendedCellBuilder
name|setValue
parameter_list|(
specifier|final
name|byte
index|[]
name|value
parameter_list|,
name|int
name|vOffset
parameter_list|,
name|int
name|vLength
parameter_list|)
block|{
name|this
operator|.
name|value
operator|=
name|value
expr_stmt|;
name|this
operator|.
name|vOffset
operator|=
name|vOffset
expr_stmt|;
name|this
operator|.
name|vLength
operator|=
name|vLength
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|ExtendedCellBuilder
name|setTags
parameter_list|(
specifier|final
name|byte
index|[]
name|tags
parameter_list|)
block|{
return|return
name|setTags
argument_list|(
name|tags
argument_list|,
literal|0
argument_list|,
name|ArrayUtils
operator|.
name|length
argument_list|(
name|tags
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|ExtendedCellBuilder
name|setTags
parameter_list|(
specifier|final
name|byte
index|[]
name|tags
parameter_list|,
name|int
name|tagsOffset
parameter_list|,
name|int
name|tagsLength
parameter_list|)
block|{
name|this
operator|.
name|tags
operator|=
name|tags
expr_stmt|;
name|this
operator|.
name|tagsOffset
operator|=
name|tagsOffset
expr_stmt|;
name|this
operator|.
name|tagsLength
operator|=
name|tagsLength
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|ExtendedCellBuilder
name|setSequenceId
parameter_list|(
specifier|final
name|long
name|seqId
parameter_list|)
block|{
name|this
operator|.
name|seqId
operator|=
name|seqId
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|private
name|void
name|checkBeforeBuild
parameter_list|()
block|{
if|if
condition|(
name|type
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"The type can't be NULL"
argument_list|)
throw|;
block|}
block|}
specifier|protected
specifier|abstract
name|ExtendedCell
name|innerBuild
parameter_list|()
function_decl|;
annotation|@
name|Override
specifier|public
name|ExtendedCell
name|build
parameter_list|()
block|{
name|checkBeforeBuild
argument_list|()
expr_stmt|;
return|return
name|innerBuild
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|ExtendedCellBuilder
name|clear
parameter_list|()
block|{
name|row
operator|=
literal|null
expr_stmt|;
name|rOffset
operator|=
literal|0
expr_stmt|;
name|rLength
operator|=
literal|0
expr_stmt|;
name|family
operator|=
literal|null
expr_stmt|;
name|fOffset
operator|=
literal|0
expr_stmt|;
name|fLength
operator|=
literal|0
expr_stmt|;
name|qualifier
operator|=
literal|null
expr_stmt|;
name|qOffset
operator|=
literal|0
expr_stmt|;
name|qLength
operator|=
literal|0
expr_stmt|;
name|timestamp
operator|=
name|HConstants
operator|.
name|LATEST_TIMESTAMP
expr_stmt|;
name|type
operator|=
literal|null
expr_stmt|;
name|value
operator|=
literal|null
expr_stmt|;
name|vOffset
operator|=
literal|0
expr_stmt|;
name|vLength
operator|=
literal|0
expr_stmt|;
name|seqId
operator|=
literal|0
expr_stmt|;
name|tags
operator|=
literal|null
expr_stmt|;
name|tagsOffset
operator|=
literal|0
expr_stmt|;
name|tagsLength
operator|=
literal|0
expr_stmt|;
return|return
name|this
return|;
block|}
block|}
end_class

end_unit

