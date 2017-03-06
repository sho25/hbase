begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|regionserver
operator|.
name|querymatcher
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
name|SortedSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeSet
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

begin_comment
comment|/**  * This class is responsible for the tracking and enforcement of Deletes during the course of a Scan  * operation. It only has to enforce Delete and DeleteColumn, since the DeleteFamily is handled at a  * higher level.  *<p>  * This class is utilized through three methods:  *<ul>  *<li>{@link #add} when encountering a Delete or DeleteColumn</li>  *<li>{@link #isDeleted} when checking if a Put KeyValue has been deleted</li>  *<li>{@link #update} when reaching the end of a StoreFile or row for scans</li>  *</ul>  *<p>  * This class is NOT thread-safe as queries are never multi-threaded  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ScanDeleteTracker
implements|implements
name|DeleteTracker
block|{
specifier|protected
name|boolean
name|hasFamilyStamp
init|=
literal|false
decl_stmt|;
specifier|protected
name|long
name|familyStamp
init|=
literal|0L
decl_stmt|;
specifier|protected
name|SortedSet
argument_list|<
name|Long
argument_list|>
name|familyVersionStamps
init|=
operator|new
name|TreeSet
argument_list|<
name|Long
argument_list|>
argument_list|()
decl_stmt|;
specifier|protected
name|Cell
name|deleteCell
init|=
literal|null
decl_stmt|;
specifier|protected
name|byte
name|deleteType
init|=
literal|0
decl_stmt|;
specifier|protected
name|long
name|deleteTimestamp
init|=
literal|0L
decl_stmt|;
comment|/**    * Add the specified KeyValue to the list of deletes to check against for this row operation.    *<p>    * This is called when a Delete is encountered.    * @param cell - the delete cell    */
annotation|@
name|Override
specifier|public
name|void
name|add
parameter_list|(
name|Cell
name|cell
parameter_list|)
block|{
name|long
name|timestamp
init|=
name|cell
operator|.
name|getTimestamp
argument_list|()
decl_stmt|;
name|byte
name|type
init|=
name|cell
operator|.
name|getTypeByte
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|hasFamilyStamp
operator|||
name|timestamp
operator|>
name|familyStamp
condition|)
block|{
if|if
condition|(
name|type
operator|==
name|KeyValue
operator|.
name|Type
operator|.
name|DeleteFamily
operator|.
name|getCode
argument_list|()
condition|)
block|{
name|hasFamilyStamp
operator|=
literal|true
expr_stmt|;
name|familyStamp
operator|=
name|timestamp
expr_stmt|;
return|return;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
name|KeyValue
operator|.
name|Type
operator|.
name|DeleteFamilyVersion
operator|.
name|getCode
argument_list|()
condition|)
block|{
name|familyVersionStamps
operator|.
name|add
argument_list|(
name|timestamp
argument_list|)
expr_stmt|;
return|return;
block|}
if|if
condition|(
name|deleteCell
operator|!=
literal|null
operator|&&
name|type
operator|<
name|deleteType
condition|)
block|{
comment|// same column, so ignore less specific delete
if|if
condition|(
name|CellUtil
operator|.
name|matchingQualifier
argument_list|(
name|cell
argument_list|,
name|deleteCell
argument_list|)
condition|)
block|{
return|return;
block|}
block|}
comment|// new column, or more general delete type
name|deleteCell
operator|=
name|cell
expr_stmt|;
name|deleteType
operator|=
name|type
expr_stmt|;
name|deleteTimestamp
operator|=
name|timestamp
expr_stmt|;
block|}
comment|// missing else is never called.
block|}
comment|/**    * Check if the specified KeyValue buffer has been deleted by a previously seen delete.    * @param cell - current cell to check if deleted by a previously seen delete    * @return deleteResult    */
annotation|@
name|Override
specifier|public
name|DeleteResult
name|isDeleted
parameter_list|(
name|Cell
name|cell
parameter_list|)
block|{
name|long
name|timestamp
init|=
name|cell
operator|.
name|getTimestamp
argument_list|()
decl_stmt|;
if|if
condition|(
name|hasFamilyStamp
operator|&&
name|timestamp
operator|<=
name|familyStamp
condition|)
block|{
return|return
name|DeleteResult
operator|.
name|FAMILY_DELETED
return|;
block|}
if|if
condition|(
name|familyVersionStamps
operator|.
name|contains
argument_list|(
name|Long
operator|.
name|valueOf
argument_list|(
name|timestamp
argument_list|)
argument_list|)
condition|)
block|{
return|return
name|DeleteResult
operator|.
name|FAMILY_VERSION_DELETED
return|;
block|}
if|if
condition|(
name|deleteCell
operator|!=
literal|null
condition|)
block|{
name|int
name|ret
init|=
operator|-
operator|(
name|CellComparator
operator|.
name|compareQualifiers
argument_list|(
name|cell
argument_list|,
name|deleteCell
argument_list|)
operator|)
decl_stmt|;
if|if
condition|(
name|ret
operator|==
literal|0
condition|)
block|{
if|if
condition|(
name|deleteType
operator|==
name|KeyValue
operator|.
name|Type
operator|.
name|DeleteColumn
operator|.
name|getCode
argument_list|()
condition|)
block|{
return|return
name|DeleteResult
operator|.
name|COLUMN_DELETED
return|;
block|}
comment|// Delete (aka DeleteVersion)
comment|// If the timestamp is the same, keep this one
if|if
condition|(
name|timestamp
operator|==
name|deleteTimestamp
condition|)
block|{
return|return
name|DeleteResult
operator|.
name|VERSION_DELETED
return|;
block|}
comment|// use assert or not?
assert|assert
name|timestamp
operator|<
name|deleteTimestamp
assert|;
comment|// different timestamp, let's clear the buffer.
name|deleteCell
operator|=
literal|null
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|ret
operator|<
literal|0
condition|)
block|{
comment|// Next column case.
name|deleteCell
operator|=
literal|null
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"isDelete failed: deleteBuffer="
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|deleteCell
operator|.
name|getQualifierArray
argument_list|()
argument_list|,
name|deleteCell
operator|.
name|getQualifierOffset
argument_list|()
argument_list|,
name|deleteCell
operator|.
name|getQualifierLength
argument_list|()
argument_list|)
operator|+
literal|", qualifier="
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
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
argument_list|)
operator|+
literal|", timestamp="
operator|+
name|timestamp
operator|+
literal|", comparison result: "
operator|+
name|ret
argument_list|)
throw|;
block|}
block|}
return|return
name|DeleteResult
operator|.
name|NOT_DELETED
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isEmpty
parameter_list|()
block|{
return|return
name|deleteCell
operator|==
literal|null
operator|&&
operator|!
name|hasFamilyStamp
operator|&&
name|familyVersionStamps
operator|.
name|isEmpty
argument_list|()
return|;
block|}
annotation|@
name|Override
comment|// called between every row.
specifier|public
name|void
name|reset
parameter_list|()
block|{
name|hasFamilyStamp
operator|=
literal|false
expr_stmt|;
name|familyStamp
operator|=
literal|0L
expr_stmt|;
name|familyVersionStamps
operator|.
name|clear
argument_list|()
expr_stmt|;
name|deleteCell
operator|=
literal|null
expr_stmt|;
block|}
annotation|@
name|Override
comment|// should not be called at all even (!)
specifier|public
name|void
name|update
parameter_list|()
block|{
name|this
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|beforeShipped
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|deleteCell
operator|!=
literal|null
condition|)
block|{
name|deleteCell
operator|=
name|KeyValueUtil
operator|.
name|toNewKeyCell
argument_list|(
name|deleteCell
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

