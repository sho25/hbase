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
name|mapred
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutput
import|;
end_import

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
name|Arrays
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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|TableName
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
name|mapred
operator|.
name|InputSplit
import|;
end_import

begin_comment
comment|/**  * A table split corresponds to a key range [low, high)  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
class|class
name|TableSplit
implements|implements
name|InputSplit
implements|,
name|Comparable
argument_list|<
name|TableSplit
argument_list|>
block|{
specifier|private
name|TableName
name|m_tableName
decl_stmt|;
specifier|private
name|byte
index|[]
name|m_startRow
decl_stmt|;
specifier|private
name|byte
index|[]
name|m_endRow
decl_stmt|;
specifier|private
name|String
name|m_regionLocation
decl_stmt|;
comment|/** default constructor */
specifier|public
name|TableSplit
parameter_list|()
block|{
name|this
argument_list|(
operator|(
name|TableName
operator|)
literal|null
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|,
literal|""
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructor    * @param tableName    * @param startRow    * @param endRow    * @param location    */
specifier|public
name|TableSplit
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|byte
index|[]
name|startRow
parameter_list|,
name|byte
index|[]
name|endRow
parameter_list|,
specifier|final
name|String
name|location
parameter_list|)
block|{
name|this
operator|.
name|m_tableName
operator|=
name|tableName
expr_stmt|;
name|this
operator|.
name|m_startRow
operator|=
name|startRow
expr_stmt|;
name|this
operator|.
name|m_endRow
operator|=
name|endRow
expr_stmt|;
name|this
operator|.
name|m_regionLocation
operator|=
name|location
expr_stmt|;
block|}
specifier|public
name|TableSplit
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|,
name|byte
index|[]
name|startRow
parameter_list|,
name|byte
index|[]
name|endRow
parameter_list|,
specifier|final
name|String
name|location
parameter_list|)
block|{
name|this
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableName
argument_list|)
argument_list|,
name|startRow
argument_list|,
name|endRow
argument_list|,
name|location
argument_list|)
expr_stmt|;
block|}
comment|/** @return table name */
specifier|public
name|TableName
name|getTable
parameter_list|()
block|{
return|return
name|this
operator|.
name|m_tableName
return|;
block|}
comment|/** @return table name */
specifier|public
name|byte
index|[]
name|getTableName
parameter_list|()
block|{
return|return
name|this
operator|.
name|m_tableName
operator|.
name|getName
argument_list|()
return|;
block|}
comment|/** @return starting row key */
specifier|public
name|byte
index|[]
name|getStartRow
parameter_list|()
block|{
return|return
name|this
operator|.
name|m_startRow
return|;
block|}
comment|/** @return end row key */
specifier|public
name|byte
index|[]
name|getEndRow
parameter_list|()
block|{
return|return
name|this
operator|.
name|m_endRow
return|;
block|}
comment|/** @return the region's hostname */
specifier|public
name|String
name|getRegionLocation
parameter_list|()
block|{
return|return
name|this
operator|.
name|m_regionLocation
return|;
block|}
specifier|public
name|String
index|[]
name|getLocations
parameter_list|()
block|{
return|return
operator|new
name|String
index|[]
block|{
name|this
operator|.
name|m_regionLocation
block|}
return|;
block|}
specifier|public
name|long
name|getLength
parameter_list|()
block|{
comment|// Not clear how to obtain this... seems to be used only for sorting splits
return|return
literal|0
return|;
block|}
specifier|public
name|void
name|readFields
parameter_list|(
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|m_tableName
operator|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|Bytes
operator|.
name|readByteArray
argument_list|(
name|in
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|m_startRow
operator|=
name|Bytes
operator|.
name|readByteArray
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|this
operator|.
name|m_endRow
operator|=
name|Bytes
operator|.
name|readByteArray
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|this
operator|.
name|m_regionLocation
operator|=
name|Bytes
operator|.
name|toString
argument_list|(
name|Bytes
operator|.
name|readByteArray
argument_list|(
name|in
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|write
parameter_list|(
name|DataOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|Bytes
operator|.
name|writeByteArray
argument_list|(
name|out
argument_list|,
name|this
operator|.
name|m_tableName
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|Bytes
operator|.
name|writeByteArray
argument_list|(
name|out
argument_list|,
name|this
operator|.
name|m_startRow
argument_list|)
expr_stmt|;
name|Bytes
operator|.
name|writeByteArray
argument_list|(
name|out
argument_list|,
name|this
operator|.
name|m_endRow
argument_list|)
expr_stmt|;
name|Bytes
operator|.
name|writeByteArray
argument_list|(
name|out
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|this
operator|.
name|m_regionLocation
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
literal|"HBase table split("
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"table name: "
argument_list|)
operator|.
name|append
argument_list|(
name|m_tableName
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", start row: "
argument_list|)
operator|.
name|append
argument_list|(
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|m_startRow
argument_list|)
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", end row: "
argument_list|)
operator|.
name|append
argument_list|(
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|m_endRow
argument_list|)
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", region location: "
argument_list|)
operator|.
name|append
argument_list|(
name|m_regionLocation
argument_list|)
expr_stmt|;
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
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
name|TableSplit
name|o
parameter_list|)
block|{
return|return
name|Bytes
operator|.
name|compareTo
argument_list|(
name|getStartRow
argument_list|()
argument_list|,
name|o
operator|.
name|getStartRow
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|o
operator|==
literal|null
operator|||
operator|!
operator|(
name|o
operator|instanceof
name|TableSplit
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|TableSplit
name|other
init|=
operator|(
name|TableSplit
operator|)
name|o
decl_stmt|;
return|return
name|m_tableName
operator|.
name|equals
argument_list|(
name|other
operator|.
name|m_tableName
argument_list|)
operator|&&
name|Bytes
operator|.
name|equals
argument_list|(
name|m_startRow
argument_list|,
name|other
operator|.
name|m_startRow
argument_list|)
operator|&&
name|Bytes
operator|.
name|equals
argument_list|(
name|m_endRow
argument_list|,
name|other
operator|.
name|m_endRow
argument_list|)
operator|&&
name|m_regionLocation
operator|.
name|equals
argument_list|(
name|other
operator|.
name|m_regionLocation
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
name|int
name|result
init|=
name|m_tableName
operator|!=
literal|null
condition|?
name|m_tableName
operator|.
name|hashCode
argument_list|()
else|:
literal|0
decl_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
name|Arrays
operator|.
name|hashCode
argument_list|(
name|m_startRow
argument_list|)
expr_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
name|Arrays
operator|.
name|hashCode
argument_list|(
name|m_endRow
argument_list|)
expr_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
operator|(
name|m_regionLocation
operator|!=
literal|null
condition|?
name|m_regionLocation
operator|.
name|hashCode
argument_list|()
else|:
literal|0
operator|)
expr_stmt|;
return|return
name|result
return|;
block|}
block|}
end_class

end_unit

