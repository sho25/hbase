begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2007 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|mapreduce
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
name|classification
operator|.
name|InterfaceStability
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
name|io
operator|.
name|Writable
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
name|mapreduce
operator|.
name|InputSplit
import|;
end_import

begin_comment
comment|/**  * A table split corresponds to a key range (low, high). All references to row  * below refer to the key of the row.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Stable
specifier|public
class|class
name|TableSplit
extends|extends
name|InputSplit
implements|implements
name|Writable
implements|,
name|Comparable
argument_list|<
name|TableSplit
argument_list|>
block|{
specifier|private
name|byte
index|[]
name|tableName
decl_stmt|;
specifier|private
name|byte
index|[]
name|startRow
decl_stmt|;
specifier|private
name|byte
index|[]
name|endRow
decl_stmt|;
specifier|private
name|String
name|regionLocation
decl_stmt|;
comment|/** Default constructor. */
specifier|public
name|TableSplit
parameter_list|()
block|{
name|this
argument_list|(
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
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
comment|/**    * Creates a new instance while assigning all variables.    *    * @param tableName  The name of the current table.    * @param startRow  The start row of the split.    * @param endRow  The end row of the split.    * @param location  The location of the region.    */
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
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
name|this
operator|.
name|startRow
operator|=
name|startRow
expr_stmt|;
name|this
operator|.
name|endRow
operator|=
name|endRow
expr_stmt|;
name|this
operator|.
name|regionLocation
operator|=
name|location
expr_stmt|;
block|}
comment|/**    * Returns the table name.    *    * @return The table name.    */
specifier|public
name|byte
index|[]
name|getTableName
parameter_list|()
block|{
return|return
name|tableName
return|;
block|}
comment|/**    * Returns the start row.    *    * @return The start row.    */
specifier|public
name|byte
index|[]
name|getStartRow
parameter_list|()
block|{
return|return
name|startRow
return|;
block|}
comment|/**    * Returns the end row.    *    * @return The end row.    */
specifier|public
name|byte
index|[]
name|getEndRow
parameter_list|()
block|{
return|return
name|endRow
return|;
block|}
comment|/**    * Returns the region location.    *    * @return The region's location.    */
specifier|public
name|String
name|getRegionLocation
parameter_list|()
block|{
return|return
name|regionLocation
return|;
block|}
comment|/**    * Returns the region's location as an array.    *    * @return The array containing the region location.    * @see org.apache.hadoop.mapreduce.InputSplit#getLocations()    */
annotation|@
name|Override
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
name|regionLocation
block|}
return|;
block|}
comment|/**    * Returns the length of the split.    *    * @return The length of the split.    * @see org.apache.hadoop.mapreduce.InputSplit#getLength()    */
annotation|@
name|Override
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
comment|/**    * Reads the values of each field.    *    * @param in  The input to read from.    * @throws IOException When reading the input fails.    */
annotation|@
name|Override
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
name|tableName
operator|=
name|Bytes
operator|.
name|readByteArray
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|startRow
operator|=
name|Bytes
operator|.
name|readByteArray
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|endRow
operator|=
name|Bytes
operator|.
name|readByteArray
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|regionLocation
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
comment|/**    * Writes the field values to the output.    *    * @param out  The output to write to.    * @throws IOException When writing the values to the output fails.    */
annotation|@
name|Override
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
name|tableName
argument_list|)
expr_stmt|;
name|Bytes
operator|.
name|writeByteArray
argument_list|(
name|out
argument_list|,
name|startRow
argument_list|)
expr_stmt|;
name|Bytes
operator|.
name|writeByteArray
argument_list|(
name|out
argument_list|,
name|endRow
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
name|regionLocation
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Returns the details about this instance as a string.    *    * @return The values of this instance as a string.    * @see java.lang.Object#toString()    */
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|regionLocation
operator|+
literal|":"
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|startRow
argument_list|)
operator|+
literal|","
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|endRow
argument_list|)
return|;
block|}
comment|/**    * Compares this split against the given one.    *    * @param split  The split to compare to.    * @return The result of the comparison.    * @see java.lang.Comparable#compareTo(java.lang.Object)    */
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
name|TableSplit
name|split
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
name|split
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
return|return
name|Bytes
operator|.
name|equals
argument_list|(
name|tableName
argument_list|,
operator|(
operator|(
name|TableSplit
operator|)
name|o
operator|)
operator|.
name|tableName
argument_list|)
operator|&&
name|Bytes
operator|.
name|equals
argument_list|(
name|startRow
argument_list|,
operator|(
operator|(
name|TableSplit
operator|)
name|o
operator|)
operator|.
name|startRow
argument_list|)
operator|&&
name|Bytes
operator|.
name|equals
argument_list|(
name|endRow
argument_list|,
operator|(
operator|(
name|TableSplit
operator|)
name|o
operator|)
operator|.
name|endRow
argument_list|)
operator|&&
name|regionLocation
operator|.
name|equals
argument_list|(
operator|(
operator|(
name|TableSplit
operator|)
name|o
operator|)
operator|.
name|regionLocation
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
name|tableName
operator|!=
literal|null
condition|?
name|Arrays
operator|.
name|hashCode
argument_list|(
name|tableName
argument_list|)
else|:
literal|0
decl_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
operator|(
name|startRow
operator|!=
literal|null
condition|?
name|Arrays
operator|.
name|hashCode
argument_list|(
name|startRow
argument_list|)
else|:
literal|0
operator|)
expr_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
operator|(
name|endRow
operator|!=
literal|null
condition|?
name|Arrays
operator|.
name|hashCode
argument_list|(
name|endRow
argument_list|)
else|:
literal|0
operator|)
expr_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
operator|(
name|regionLocation
operator|!=
literal|null
condition|?
name|regionLocation
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

