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
name|CellComparator
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
name|CellUtil
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
name|KeyValueUtil
import|;
end_import

begin_comment
comment|/**  * As the PrefixTreeArrayScanner moves through the tree bytes, it changes the values in the fields  * of this class so that Cell logic can be applied, but without allocating new memory for every Cell  * iterated through.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|PrefixTreeCell
implements|implements
name|Cell
implements|,
name|Comparable
argument_list|<
name|Cell
argument_list|>
block|{
comment|/********************** static **********************/
specifier|public
specifier|static
specifier|final
name|KeyValue
operator|.
name|Type
index|[]
name|TYPES
init|=
operator|new
name|KeyValue
operator|.
name|Type
index|[
literal|256
index|]
decl_stmt|;
static|static
block|{
for|for
control|(
name|KeyValue
operator|.
name|Type
name|type
range|:
name|KeyValue
operator|.
name|Type
operator|.
name|values
argument_list|()
control|)
block|{
name|TYPES
index|[
name|type
operator|.
name|getCode
argument_list|()
operator|&
literal|0xff
index|]
operator|=
name|type
expr_stmt|;
block|}
block|}
comment|//Same as KeyValue constructor.  Only used to avoid NPE's when full cell hasn't been initialized.
specifier|public
specifier|static
specifier|final
name|KeyValue
operator|.
name|Type
name|DEFAULT_TYPE
init|=
name|KeyValue
operator|.
name|Type
operator|.
name|Put
decl_stmt|;
comment|/******************** fields ************************/
specifier|protected
name|byte
index|[]
name|block
decl_stmt|;
comment|//we could also avoid setting the mvccVersion in the scanner/searcher, but this is simpler
specifier|protected
name|boolean
name|includeMvccVersion
decl_stmt|;
specifier|protected
name|byte
index|[]
name|rowBuffer
decl_stmt|;
specifier|protected
name|int
name|rowLength
decl_stmt|;
specifier|protected
name|byte
index|[]
name|familyBuffer
decl_stmt|;
specifier|protected
name|int
name|familyOffset
decl_stmt|;
specifier|protected
name|int
name|familyLength
decl_stmt|;
specifier|protected
name|byte
index|[]
name|qualifierBuffer
decl_stmt|;
comment|// aligned to the end of the array
specifier|protected
name|int
name|qualifierOffset
decl_stmt|;
specifier|protected
name|int
name|qualifierLength
decl_stmt|;
specifier|protected
name|Long
name|timestamp
decl_stmt|;
specifier|protected
name|Long
name|mvccVersion
decl_stmt|;
specifier|protected
name|KeyValue
operator|.
name|Type
name|type
decl_stmt|;
specifier|protected
name|int
name|absoluteValueOffset
decl_stmt|;
specifier|protected
name|int
name|valueLength
decl_stmt|;
specifier|protected
name|byte
index|[]
name|tagsBuffer
decl_stmt|;
specifier|protected
name|int
name|tagsOffset
decl_stmt|;
specifier|protected
name|int
name|tagsLength
decl_stmt|;
comment|/********************** Cell methods ******************/
comment|/**    * For debugging.  Currently creates new KeyValue to utilize its toString() method.    */
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|getKeyValueString
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
if|if
condition|(
operator|!
operator|(
name|obj
operator|instanceof
name|Cell
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
comment|//Temporary hack to maintain backwards compatibility with KeyValue.equals
return|return
name|CellComparator
operator|.
name|equalsIgnoreMvccVersion
argument_list|(
name|this
argument_list|,
operator|(
name|Cell
operator|)
name|obj
argument_list|)
return|;
comment|//TODO return CellComparator.equals(this, (Cell)obj);//see HBASE-6907
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
comment|//Temporary hack to maintain backwards compatibility with KeyValue.hashCode
comment|//I don't think this is used in any hot code paths
return|return
name|KeyValueUtil
operator|.
name|copyToNewKeyValue
argument_list|(
name|this
argument_list|)
operator|.
name|hashCode
argument_list|()
return|;
comment|//TODO return CellComparator.hashCode(this);//see HBASE-6907
block|}
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
name|Cell
name|other
parameter_list|)
block|{
return|return
name|CellComparator
operator|.
name|compareStatic
argument_list|(
name|this
argument_list|,
name|other
argument_list|,
literal|false
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getTimestamp
parameter_list|()
block|{
return|return
name|timestamp
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getMvccVersion
parameter_list|()
block|{
if|if
condition|(
operator|!
name|includeMvccVersion
condition|)
block|{
return|return
literal|0L
return|;
block|}
return|return
name|mvccVersion
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getSequenceId
parameter_list|()
block|{
return|return
name|getMvccVersion
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getValueLength
parameter_list|()
block|{
return|return
name|valueLength
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|getRowArray
parameter_list|()
block|{
return|return
name|rowBuffer
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getRowOffset
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|short
name|getRowLength
parameter_list|()
block|{
return|return
operator|(
name|short
operator|)
name|rowLength
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|getFamilyArray
parameter_list|()
block|{
return|return
name|familyBuffer
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getFamilyOffset
parameter_list|()
block|{
return|return
name|familyOffset
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
name|getFamilyLength
parameter_list|()
block|{
return|return
operator|(
name|byte
operator|)
name|familyLength
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|getQualifierArray
parameter_list|()
block|{
return|return
name|qualifierBuffer
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getQualifierOffset
parameter_list|()
block|{
return|return
name|qualifierOffset
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getQualifierLength
parameter_list|()
block|{
return|return
name|qualifierLength
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|getValueArray
parameter_list|()
block|{
return|return
name|block
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getValueOffset
parameter_list|()
block|{
return|return
name|absoluteValueOffset
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
name|getTypeByte
parameter_list|()
block|{
return|return
name|type
operator|.
name|getCode
argument_list|()
return|;
block|}
comment|/* Deprecated methods pushed into the Cell interface */
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|getValue
parameter_list|()
block|{
return|return
name|CellUtil
operator|.
name|cloneValue
argument_list|(
name|this
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|getFamily
parameter_list|()
block|{
return|return
name|CellUtil
operator|.
name|cloneFamily
argument_list|(
name|this
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|getQualifier
parameter_list|()
block|{
return|return
name|CellUtil
operator|.
name|cloneQualifier
argument_list|(
name|this
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|getRow
parameter_list|()
block|{
return|return
name|CellUtil
operator|.
name|cloneRow
argument_list|(
name|this
argument_list|)
return|;
block|}
comment|/************************* helper methods *************************/
comment|/**    * Need this separate method so we can call it from subclasses' toString() methods    */
specifier|protected
name|String
name|getKeyValueString
parameter_list|()
block|{
name|KeyValue
name|kv
init|=
name|KeyValueUtil
operator|.
name|copyToNewKeyValue
argument_list|(
name|this
argument_list|)
decl_stmt|;
return|return
name|kv
operator|.
name|toString
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getTagsOffset
parameter_list|()
block|{
return|return
name|tagsOffset
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getTagsLength
parameter_list|()
block|{
return|return
name|tagsLength
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|getTagsArray
parameter_list|()
block|{
return|return
name|this
operator|.
name|tagsBuffer
return|;
block|}
block|}
end_class

end_unit

