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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|io
operator|.
name|Text
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
name|WritableComparable
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
name|JenkinsHash
import|;
end_import

begin_comment
comment|/**  * HRegion information.  * Contains HRegion id, start and end keys, a reference to this  * HRegions' table descriptor, etc.  */
end_comment

begin_class
specifier|public
class|class
name|HRegionInfo
implements|implements
name|WritableComparable
block|{
comment|/**    * @param regionName    * @return the encodedName    */
specifier|public
specifier|static
name|String
name|encodeRegionName
parameter_list|(
specifier|final
name|Text
name|regionName
parameter_list|)
block|{
return|return
name|String
operator|.
name|valueOf
argument_list|(
name|JenkinsHash
operator|.
name|hash
argument_list|(
name|regionName
operator|.
name|getBytes
argument_list|()
argument_list|,
name|regionName
operator|.
name|getLength
argument_list|()
argument_list|,
literal|0
argument_list|)
argument_list|)
return|;
block|}
comment|/** delimiter used between portions of a region name */
specifier|private
specifier|static
specifier|final
name|String
name|DELIMITER
init|=
literal|","
decl_stmt|;
comment|/** HRegionInfo for root region */
specifier|public
specifier|static
specifier|final
name|HRegionInfo
name|rootRegionInfo
init|=
operator|new
name|HRegionInfo
argument_list|(
literal|0L
argument_list|,
name|HTableDescriptor
operator|.
name|rootTableDesc
argument_list|)
decl_stmt|;
comment|/** HRegionInfo for first meta region */
specifier|public
specifier|static
specifier|final
name|HRegionInfo
name|firstMetaRegionInfo
init|=
operator|new
name|HRegionInfo
argument_list|(
literal|1L
argument_list|,
name|HTableDescriptor
operator|.
name|metaTableDesc
argument_list|)
decl_stmt|;
comment|/**    * Extracts table name prefix from a region name.    * Presumes region names are ASCII characters only.    * @param regionName A region name.    * @return The table prefix of a region name.    */
specifier|public
specifier|static
name|Text
name|getTableNameFromRegionName
parameter_list|(
specifier|final
name|Text
name|regionName
parameter_list|)
block|{
name|int
name|offset
init|=
name|regionName
operator|.
name|find
argument_list|(
name|DELIMITER
argument_list|)
decl_stmt|;
if|if
condition|(
name|offset
operator|==
operator|-
literal|1
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|regionName
operator|.
name|toString
argument_list|()
operator|+
literal|" does not "
operator|+
literal|"contain '"
operator|+
name|DELIMITER
operator|+
literal|"' character"
argument_list|)
throw|;
block|}
name|byte
index|[]
name|tableName
init|=
operator|new
name|byte
index|[
name|offset
index|]
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|regionName
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|tableName
argument_list|,
literal|0
argument_list|,
name|offset
argument_list|)
expr_stmt|;
return|return
operator|new
name|Text
argument_list|(
name|tableName
argument_list|)
return|;
block|}
specifier|private
name|Text
name|endKey
decl_stmt|;
specifier|private
name|boolean
name|offLine
decl_stmt|;
specifier|private
name|long
name|regionId
decl_stmt|;
specifier|private
name|Text
name|regionName
decl_stmt|;
specifier|private
name|boolean
name|split
decl_stmt|;
specifier|private
name|Text
name|startKey
decl_stmt|;
specifier|private
name|HTableDescriptor
name|tableDesc
decl_stmt|;
comment|/** Used to construct the HRegionInfo for the root and first meta regions */
specifier|private
name|HRegionInfo
parameter_list|(
name|long
name|regionId
parameter_list|,
name|HTableDescriptor
name|tableDesc
parameter_list|)
block|{
name|this
operator|.
name|regionId
operator|=
name|regionId
expr_stmt|;
name|this
operator|.
name|tableDesc
operator|=
name|tableDesc
expr_stmt|;
name|this
operator|.
name|endKey
operator|=
operator|new
name|Text
argument_list|()
expr_stmt|;
name|this
operator|.
name|offLine
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|regionName
operator|=
operator|new
name|Text
argument_list|(
name|tableDesc
operator|.
name|getName
argument_list|()
operator|.
name|toString
argument_list|()
operator|+
name|DELIMITER
operator|+
name|DELIMITER
operator|+
name|regionId
argument_list|)
expr_stmt|;
name|this
operator|.
name|split
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|startKey
operator|=
operator|new
name|Text
argument_list|()
expr_stmt|;
block|}
comment|/** Default constructor - creates empty object */
specifier|public
name|HRegionInfo
parameter_list|()
block|{
name|this
operator|.
name|endKey
operator|=
operator|new
name|Text
argument_list|()
expr_stmt|;
name|this
operator|.
name|offLine
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|regionId
operator|=
literal|0
expr_stmt|;
name|this
operator|.
name|regionName
operator|=
operator|new
name|Text
argument_list|()
expr_stmt|;
name|this
operator|.
name|split
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|startKey
operator|=
operator|new
name|Text
argument_list|()
expr_stmt|;
name|this
operator|.
name|tableDesc
operator|=
operator|new
name|HTableDescriptor
argument_list|()
expr_stmt|;
block|}
comment|/**    * Construct HRegionInfo with explicit parameters    *     * @param tableDesc the table descriptor    * @param startKey first key in region    * @param endKey end of key range    * @throws IllegalArgumentException    */
specifier|public
name|HRegionInfo
parameter_list|(
name|HTableDescriptor
name|tableDesc
parameter_list|,
name|Text
name|startKey
parameter_list|,
name|Text
name|endKey
parameter_list|)
throws|throws
name|IllegalArgumentException
block|{
name|this
argument_list|(
name|tableDesc
argument_list|,
name|startKey
argument_list|,
name|endKey
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
comment|/**    * Construct HRegionInfo with explicit parameters    *     * @param tableDesc the table descriptor    * @param startKey first key in region    * @param endKey end of key range    * @param split true if this region has split and we have daughter regions    * regions that may or may not hold references to this region.    * @throws IllegalArgumentException    */
specifier|public
name|HRegionInfo
parameter_list|(
name|HTableDescriptor
name|tableDesc
parameter_list|,
name|Text
name|startKey
parameter_list|,
name|Text
name|endKey
parameter_list|,
specifier|final
name|boolean
name|split
parameter_list|)
throws|throws
name|IllegalArgumentException
block|{
if|if
condition|(
name|tableDesc
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"tableDesc cannot be null"
argument_list|)
throw|;
block|}
name|this
operator|.
name|endKey
operator|=
operator|new
name|Text
argument_list|()
expr_stmt|;
if|if
condition|(
name|endKey
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|endKey
operator|.
name|set
argument_list|(
name|endKey
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|offLine
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|regionId
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
name|this
operator|.
name|regionName
operator|=
operator|new
name|Text
argument_list|(
name|tableDesc
operator|.
name|getName
argument_list|()
operator|.
name|toString
argument_list|()
operator|+
name|DELIMITER
operator|+
operator|(
name|startKey
operator|==
literal|null
condition|?
literal|""
else|:
name|startKey
operator|.
name|toString
argument_list|()
operator|)
operator|+
name|DELIMITER
operator|+
name|regionId
argument_list|)
expr_stmt|;
name|this
operator|.
name|split
operator|=
name|split
expr_stmt|;
name|this
operator|.
name|startKey
operator|=
operator|new
name|Text
argument_list|()
expr_stmt|;
if|if
condition|(
name|startKey
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|startKey
operator|.
name|set
argument_list|(
name|startKey
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|tableDesc
operator|=
name|tableDesc
expr_stmt|;
block|}
comment|/** @return the endKey */
specifier|public
name|Text
name|getEndKey
parameter_list|()
block|{
return|return
name|endKey
return|;
block|}
comment|/** @return the regionId */
specifier|public
name|long
name|getRegionId
parameter_list|()
block|{
return|return
name|regionId
return|;
block|}
comment|/** @return the regionName */
specifier|public
name|Text
name|getRegionName
parameter_list|()
block|{
return|return
name|regionName
return|;
block|}
comment|/** @return the startKey */
specifier|public
name|Text
name|getStartKey
parameter_list|()
block|{
return|return
name|startKey
return|;
block|}
comment|/** @return the tableDesc */
specifier|public
name|HTableDescriptor
name|getTableDesc
parameter_list|()
block|{
return|return
name|tableDesc
return|;
block|}
comment|/**    * @return True if has been split and has daughters.    */
specifier|public
name|boolean
name|isSplit
parameter_list|()
block|{
return|return
name|this
operator|.
name|split
return|;
block|}
comment|/**    * @param split set split status    */
specifier|public
name|void
name|setSplit
parameter_list|(
name|boolean
name|split
parameter_list|)
block|{
name|this
operator|.
name|split
operator|=
name|split
expr_stmt|;
block|}
comment|/**    * @return True if this region is offline.    */
specifier|public
name|boolean
name|isOffline
parameter_list|()
block|{
return|return
name|this
operator|.
name|offLine
return|;
block|}
comment|/**    * @param offLine set online - offline status    */
specifier|public
name|void
name|setOffline
parameter_list|(
name|boolean
name|offLine
parameter_list|)
block|{
name|this
operator|.
name|offLine
operator|=
name|offLine
expr_stmt|;
block|}
comment|/**    * {@inheritDoc}    */
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"regionname: "
operator|+
name|this
operator|.
name|regionName
operator|.
name|toString
argument_list|()
operator|+
literal|", startKey:<"
operator|+
name|this
operator|.
name|startKey
operator|.
name|toString
argument_list|()
operator|+
literal|">,"
operator|+
operator|(
name|isOffline
argument_list|()
condition|?
literal|" offline: true,"
else|:
literal|""
operator|)
operator|+
operator|(
name|isSplit
argument_list|()
condition|?
literal|" split: true,"
else|:
literal|""
operator|)
operator|+
literal|" tableDesc: {"
operator|+
name|this
operator|.
name|tableDesc
operator|.
name|toString
argument_list|()
operator|+
literal|"}"
return|;
block|}
comment|/**    * {@inheritDoc}    */
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
return|return
name|this
operator|.
name|compareTo
argument_list|(
name|o
argument_list|)
operator|==
literal|0
return|;
block|}
comment|/**    * {@inheritDoc}    */
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
name|this
operator|.
name|regionName
operator|.
name|hashCode
argument_list|()
decl_stmt|;
name|result
operator|^=
name|Long
operator|.
name|valueOf
argument_list|(
name|this
operator|.
name|regionId
argument_list|)
operator|.
name|hashCode
argument_list|()
expr_stmt|;
name|result
operator|^=
name|this
operator|.
name|startKey
operator|.
name|hashCode
argument_list|()
expr_stmt|;
name|result
operator|^=
name|this
operator|.
name|endKey
operator|.
name|hashCode
argument_list|()
expr_stmt|;
name|result
operator|^=
name|Boolean
operator|.
name|valueOf
argument_list|(
name|this
operator|.
name|offLine
argument_list|)
operator|.
name|hashCode
argument_list|()
expr_stmt|;
name|result
operator|^=
name|this
operator|.
name|tableDesc
operator|.
name|hashCode
argument_list|()
expr_stmt|;
return|return
name|result
return|;
block|}
comment|//
comment|// Writable
comment|//
comment|/**    * {@inheritDoc}    */
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
name|endKey
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|offLine
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeLong
argument_list|(
name|regionId
argument_list|)
expr_stmt|;
name|regionName
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|split
argument_list|)
expr_stmt|;
name|startKey
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|tableDesc
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
comment|/**    * {@inheritDoc}    */
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
name|endKey
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|this
operator|.
name|offLine
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
name|this
operator|.
name|regionId
operator|=
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
name|this
operator|.
name|regionName
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|this
operator|.
name|split
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
name|this
operator|.
name|startKey
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|this
operator|.
name|tableDesc
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
comment|//
comment|// Comparable
comment|//
comment|/**    * {@inheritDoc}    */
specifier|public
name|int
name|compareTo
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
name|HRegionInfo
name|other
init|=
operator|(
name|HRegionInfo
operator|)
name|o
decl_stmt|;
comment|// Are regions of same table?
name|int
name|result
init|=
name|this
operator|.
name|tableDesc
operator|.
name|compareTo
argument_list|(
name|other
operator|.
name|tableDesc
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
operator|!=
literal|0
condition|)
block|{
return|return
name|result
return|;
block|}
comment|// Compare start keys.
name|result
operator|=
name|this
operator|.
name|startKey
operator|.
name|compareTo
argument_list|(
name|other
operator|.
name|startKey
argument_list|)
expr_stmt|;
if|if
condition|(
name|result
operator|!=
literal|0
condition|)
block|{
return|return
name|result
return|;
block|}
comment|// Compare end keys.
return|return
name|this
operator|.
name|endKey
operator|.
name|compareTo
argument_list|(
name|other
operator|.
name|endKey
argument_list|)
return|;
block|}
block|}
end_class

end_unit

