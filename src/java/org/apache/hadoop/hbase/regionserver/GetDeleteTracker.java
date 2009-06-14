begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2009 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
package|;
end_package

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
name|Iterator
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
name|util
operator|.
name|Bytes
import|;
end_import

begin_comment
comment|/**  * This class is responsible for the tracking and enforcement of Deletes  * during the course of a Get operation.  *<p>  * This class is utilized through three methods:  *<ul><li>{@link #add} when encountering a Delete  *<li>{@link #isDeleted} when checking if a Put KeyValue has been deleted  *<li>{@link #update} when reaching the end of a StoreFile  *<p>  * This class is NOT thread-safe as queries are never multi-threaded   */
end_comment

begin_class
specifier|public
class|class
name|GetDeleteTracker
implements|implements
name|DeleteTracker
block|{
specifier|private
name|long
name|familyStamp
init|=
operator|-
literal|1L
decl_stmt|;
specifier|protected
name|List
argument_list|<
name|Delete
argument_list|>
name|deletes
init|=
literal|null
decl_stmt|;
specifier|private
name|List
argument_list|<
name|Delete
argument_list|>
name|newDeletes
init|=
operator|new
name|ArrayList
argument_list|<
name|Delete
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|Iterator
argument_list|<
name|Delete
argument_list|>
name|iterator
decl_stmt|;
specifier|private
name|Delete
name|delete
init|=
literal|null
decl_stmt|;
comment|/**    * Constructor    * @param comparator    */
specifier|public
name|GetDeleteTracker
parameter_list|()
block|{}
comment|/**    * Add the specified KeyValue to the list of deletes to check against for    * this row operation.    *<p>    * This is called when a Delete is encountered in a StoreFile.    * @param buffer    * @param qualifierOffset    * @param qualifierLength    * @param timestamp    * @param type    */
annotation|@
name|Override
specifier|public
name|void
name|add
parameter_list|(
name|byte
index|[]
name|buffer
parameter_list|,
name|int
name|qualifierOffset
parameter_list|,
name|int
name|qualifierLength
parameter_list|,
name|long
name|timestamp
parameter_list|,
name|byte
name|type
parameter_list|)
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
if|if
condition|(
name|timestamp
operator|>
name|familyStamp
condition|)
block|{
name|familyStamp
operator|=
name|timestamp
expr_stmt|;
block|}
return|return;
block|}
if|if
condition|(
name|timestamp
operator|>
name|familyStamp
condition|)
block|{
name|this
operator|.
name|newDeletes
operator|.
name|add
argument_list|(
operator|new
name|Delete
argument_list|(
name|buffer
argument_list|,
name|qualifierOffset
argument_list|,
name|qualifierLength
argument_list|,
name|type
argument_list|,
name|timestamp
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**     * Check if the specified KeyValue buffer has been deleted by a previously    * seen delete.    * @param buffer KeyValue buffer    * @param qualifierOffset column qualifier offset    * @param qualifierLength column qualifier length    * @param timestamp timestamp    * @return true is the specified KeyValue is deleted, false if not    */
annotation|@
name|Override
specifier|public
name|boolean
name|isDeleted
parameter_list|(
name|byte
index|[]
name|buffer
parameter_list|,
name|int
name|qualifierOffset
parameter_list|,
name|int
name|qualifierLength
parameter_list|,
name|long
name|timestamp
parameter_list|)
block|{
comment|// Check against DeleteFamily
if|if
condition|(
name|timestamp
operator|<=
name|familyStamp
condition|)
block|{
return|return
literal|true
return|;
block|}
comment|// Check if there are other deletes
if|if
condition|(
name|this
operator|.
name|delete
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
comment|// Check column
name|int
name|ret
init|=
name|Bytes
operator|.
name|compareTo
argument_list|(
name|buffer
argument_list|,
name|qualifierOffset
argument_list|,
name|qualifierLength
argument_list|,
name|this
operator|.
name|delete
operator|.
name|buffer
argument_list|,
name|this
operator|.
name|delete
operator|.
name|qualifierOffset
argument_list|,
name|this
operator|.
name|delete
operator|.
name|qualifierLength
argument_list|)
decl_stmt|;
if|if
condition|(
name|ret
operator|<=
operator|-
literal|1
condition|)
block|{
comment|// Have not reached the next delete yet
return|return
literal|false
return|;
block|}
elseif|else
if|if
condition|(
name|ret
operator|>=
literal|1
condition|)
block|{
comment|// Deletes an earlier column, need to move down deletes
if|if
condition|(
name|this
operator|.
name|iterator
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|this
operator|.
name|delete
operator|=
name|this
operator|.
name|iterator
operator|.
name|next
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|delete
operator|=
literal|null
expr_stmt|;
return|return
literal|false
return|;
block|}
return|return
name|isDeleted
argument_list|(
name|buffer
argument_list|,
name|qualifierOffset
argument_list|,
name|qualifierLength
argument_list|,
name|timestamp
argument_list|)
return|;
block|}
comment|// Check Timestamp
if|if
condition|(
name|timestamp
operator|>
name|this
operator|.
name|delete
operator|.
name|timestamp
condition|)
block|{
return|return
literal|false
return|;
block|}
comment|// Check Type
switch|switch
condition|(
name|KeyValue
operator|.
name|Type
operator|.
name|codeToType
argument_list|(
name|this
operator|.
name|delete
operator|.
name|type
argument_list|)
condition|)
block|{
case|case
name|Delete
case|:
name|boolean
name|equal
init|=
name|timestamp
operator|==
name|this
operator|.
name|delete
operator|.
name|timestamp
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|iterator
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|this
operator|.
name|delete
operator|=
name|this
operator|.
name|iterator
operator|.
name|next
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|delete
operator|=
literal|null
expr_stmt|;
block|}
if|if
condition|(
name|equal
condition|)
block|{
return|return
literal|true
return|;
block|}
comment|// timestamp< this.delete.timestamp
comment|// Delete of an explicit column newer than current
return|return
name|isDeleted
argument_list|(
name|buffer
argument_list|,
name|qualifierOffset
argument_list|,
name|qualifierLength
argument_list|,
name|timestamp
argument_list|)
return|;
case|case
name|DeleteColumn
case|:
return|return
literal|true
return|;
block|}
comment|// should never reach this
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isEmpty
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|familyStamp
operator|==
literal|0L
operator|&&
name|this
operator|.
name|delete
operator|==
literal|null
condition|)
block|{
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|reset
parameter_list|()
block|{
name|this
operator|.
name|deletes
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|delete
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|newDeletes
operator|=
operator|new
name|ArrayList
argument_list|<
name|Delete
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|familyStamp
operator|=
literal|0L
expr_stmt|;
name|this
operator|.
name|iterator
operator|=
literal|null
expr_stmt|;
block|}
comment|/**    * Called at the end of every StoreFile.    *<p>    * Many optimized implementations of Trackers will require an update at    * when the end of each StoreFile is reached.    */
annotation|@
name|Override
specifier|public
name|void
name|update
parameter_list|()
block|{
comment|// If no previous deletes, use new deletes and return
if|if
condition|(
name|this
operator|.
name|deletes
operator|==
literal|null
operator|||
name|this
operator|.
name|deletes
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
name|finalize
argument_list|(
name|this
operator|.
name|newDeletes
argument_list|)
expr_stmt|;
return|return;
block|}
comment|// If no new delete, retain previous deletes and return
if|if
condition|(
name|this
operator|.
name|newDeletes
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return;
block|}
comment|// Merge previous deletes with new deletes
name|List
argument_list|<
name|Delete
argument_list|>
name|mergeDeletes
init|=
operator|new
name|ArrayList
argument_list|<
name|Delete
argument_list|>
argument_list|(
name|this
operator|.
name|newDeletes
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|oldIndex
init|=
literal|0
decl_stmt|;
name|int
name|newIndex
init|=
literal|0
decl_stmt|;
name|Delete
name|newDelete
init|=
name|newDeletes
operator|.
name|get
argument_list|(
name|oldIndex
argument_list|)
decl_stmt|;
name|Delete
name|oldDelete
init|=
name|deletes
operator|.
name|get
argument_list|(
name|oldIndex
argument_list|)
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
switch|switch
condition|(
name|compareDeletes
argument_list|(
name|oldDelete
argument_list|,
name|newDelete
argument_list|)
condition|)
block|{
case|case
name|NEXT_NEW
case|:
block|{
if|if
condition|(
operator|++
name|newIndex
operator|==
name|newDeletes
operator|.
name|size
argument_list|()
condition|)
block|{
comment|// Done with new, add the rest of old to merged and return
name|mergeDown
argument_list|(
name|mergeDeletes
argument_list|,
name|deletes
argument_list|,
name|oldIndex
argument_list|)
expr_stmt|;
name|finalize
argument_list|(
name|mergeDeletes
argument_list|)
expr_stmt|;
return|return;
block|}
name|newDelete
operator|=
name|this
operator|.
name|newDeletes
operator|.
name|get
argument_list|(
name|newIndex
argument_list|)
expr_stmt|;
break|break;
block|}
case|case
name|INCLUDE_NEW_NEXT_NEW
case|:
block|{
name|mergeDeletes
operator|.
name|add
argument_list|(
name|newDelete
argument_list|)
expr_stmt|;
if|if
condition|(
operator|++
name|newIndex
operator|==
name|newDeletes
operator|.
name|size
argument_list|()
condition|)
block|{
comment|// Done with new, add the rest of old to merged and return
name|mergeDown
argument_list|(
name|mergeDeletes
argument_list|,
name|deletes
argument_list|,
name|oldIndex
argument_list|)
expr_stmt|;
name|finalize
argument_list|(
name|mergeDeletes
argument_list|)
expr_stmt|;
return|return;
block|}
name|newDelete
operator|=
name|this
operator|.
name|newDeletes
operator|.
name|get
argument_list|(
name|newIndex
argument_list|)
expr_stmt|;
break|break;
block|}
case|case
name|INCLUDE_NEW_NEXT_BOTH
case|:
block|{
name|mergeDeletes
operator|.
name|add
argument_list|(
name|newDelete
argument_list|)
expr_stmt|;
operator|++
name|oldIndex
expr_stmt|;
operator|++
name|newIndex
expr_stmt|;
if|if
condition|(
name|oldIndex
operator|==
name|deletes
operator|.
name|size
argument_list|()
condition|)
block|{
if|if
condition|(
name|newIndex
operator|==
name|newDeletes
operator|.
name|size
argument_list|()
condition|)
block|{
name|finalize
argument_list|(
name|mergeDeletes
argument_list|)
expr_stmt|;
return|return;
block|}
name|mergeDown
argument_list|(
name|mergeDeletes
argument_list|,
name|newDeletes
argument_list|,
name|newIndex
argument_list|)
expr_stmt|;
name|finalize
argument_list|(
name|mergeDeletes
argument_list|)
expr_stmt|;
return|return;
block|}
elseif|else
if|if
condition|(
name|newIndex
operator|==
name|newDeletes
operator|.
name|size
argument_list|()
condition|)
block|{
name|mergeDown
argument_list|(
name|mergeDeletes
argument_list|,
name|deletes
argument_list|,
name|oldIndex
argument_list|)
expr_stmt|;
name|finalize
argument_list|(
name|mergeDeletes
argument_list|)
expr_stmt|;
return|return;
block|}
name|oldDelete
operator|=
name|this
operator|.
name|deletes
operator|.
name|get
argument_list|(
name|oldIndex
argument_list|)
expr_stmt|;
name|newDelete
operator|=
name|this
operator|.
name|newDeletes
operator|.
name|get
argument_list|(
name|newIndex
argument_list|)
expr_stmt|;
break|break;
block|}
case|case
name|INCLUDE_OLD_NEXT_BOTH
case|:
block|{
name|mergeDeletes
operator|.
name|add
argument_list|(
name|oldDelete
argument_list|)
expr_stmt|;
operator|++
name|oldIndex
expr_stmt|;
operator|++
name|newIndex
expr_stmt|;
if|if
condition|(
name|oldIndex
operator|==
name|deletes
operator|.
name|size
argument_list|()
condition|)
block|{
if|if
condition|(
name|newIndex
operator|==
name|newDeletes
operator|.
name|size
argument_list|()
condition|)
block|{
name|finalize
argument_list|(
name|mergeDeletes
argument_list|)
expr_stmt|;
return|return;
block|}
name|mergeDown
argument_list|(
name|mergeDeletes
argument_list|,
name|newDeletes
argument_list|,
name|newIndex
argument_list|)
expr_stmt|;
name|finalize
argument_list|(
name|mergeDeletes
argument_list|)
expr_stmt|;
return|return;
block|}
elseif|else
if|if
condition|(
name|newIndex
operator|==
name|newDeletes
operator|.
name|size
argument_list|()
condition|)
block|{
name|mergeDown
argument_list|(
name|mergeDeletes
argument_list|,
name|deletes
argument_list|,
name|oldIndex
argument_list|)
expr_stmt|;
name|finalize
argument_list|(
name|mergeDeletes
argument_list|)
expr_stmt|;
return|return;
block|}
name|oldDelete
operator|=
name|this
operator|.
name|deletes
operator|.
name|get
argument_list|(
name|oldIndex
argument_list|)
expr_stmt|;
name|newDelete
operator|=
name|this
operator|.
name|newDeletes
operator|.
name|get
argument_list|(
name|newIndex
argument_list|)
expr_stmt|;
break|break;
block|}
case|case
name|INCLUDE_OLD_NEXT_OLD
case|:
block|{
name|mergeDeletes
operator|.
name|add
argument_list|(
name|oldDelete
argument_list|)
expr_stmt|;
if|if
condition|(
operator|++
name|oldIndex
operator|==
name|deletes
operator|.
name|size
argument_list|()
condition|)
block|{
name|mergeDown
argument_list|(
name|mergeDeletes
argument_list|,
name|newDeletes
argument_list|,
name|newIndex
argument_list|)
expr_stmt|;
name|finalize
argument_list|(
name|mergeDeletes
argument_list|)
expr_stmt|;
return|return;
block|}
name|oldDelete
operator|=
name|this
operator|.
name|deletes
operator|.
name|get
argument_list|(
name|oldIndex
argument_list|)
expr_stmt|;
break|break;
block|}
case|case
name|NEXT_OLD
case|:
block|{
if|if
condition|(
operator|++
name|oldIndex
operator|==
name|deletes
operator|.
name|size
argument_list|()
condition|)
block|{
comment|// Done with old, add the rest of new to merged and return
name|mergeDown
argument_list|(
name|mergeDeletes
argument_list|,
name|newDeletes
argument_list|,
name|newIndex
argument_list|)
expr_stmt|;
name|finalize
argument_list|(
name|mergeDeletes
argument_list|)
expr_stmt|;
return|return;
block|}
name|oldDelete
operator|=
name|this
operator|.
name|deletes
operator|.
name|get
argument_list|(
name|oldIndex
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
specifier|private
name|void
name|finalize
parameter_list|(
name|List
argument_list|<
name|Delete
argument_list|>
name|mergeDeletes
parameter_list|)
block|{
name|this
operator|.
name|deletes
operator|=
name|mergeDeletes
expr_stmt|;
name|this
operator|.
name|newDeletes
operator|=
operator|new
name|ArrayList
argument_list|<
name|Delete
argument_list|>
argument_list|()
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|deletes
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|this
operator|.
name|iterator
operator|=
name|deletes
operator|.
name|iterator
argument_list|()
expr_stmt|;
name|this
operator|.
name|delete
operator|=
name|iterator
operator|.
name|next
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|mergeDown
parameter_list|(
name|List
argument_list|<
name|Delete
argument_list|>
name|mergeDeletes
parameter_list|,
name|List
argument_list|<
name|Delete
argument_list|>
name|srcDeletes
parameter_list|,
name|int
name|srcIndex
parameter_list|)
block|{
name|int
name|index
init|=
name|srcIndex
decl_stmt|;
while|while
condition|(
name|index
operator|<
name|srcDeletes
operator|.
name|size
argument_list|()
condition|)
block|{
name|mergeDeletes
operator|.
name|add
argument_list|(
name|srcDeletes
operator|.
name|get
argument_list|(
name|index
operator|++
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|protected
name|DeleteCompare
name|compareDeletes
parameter_list|(
name|Delete
name|oldDelete
parameter_list|,
name|Delete
name|newDelete
parameter_list|)
block|{
comment|// Compare columns
comment|// Just compairing qualifier portion, can keep on using Bytes.compareTo().
name|int
name|ret
init|=
name|Bytes
operator|.
name|compareTo
argument_list|(
name|oldDelete
operator|.
name|buffer
argument_list|,
name|oldDelete
operator|.
name|qualifierOffset
argument_list|,
name|oldDelete
operator|.
name|qualifierLength
argument_list|,
name|newDelete
operator|.
name|buffer
argument_list|,
name|newDelete
operator|.
name|qualifierOffset
argument_list|,
name|newDelete
operator|.
name|qualifierLength
argument_list|)
decl_stmt|;
if|if
condition|(
name|ret
operator|<=
operator|-
literal|1
condition|)
block|{
return|return
name|DeleteCompare
operator|.
name|INCLUDE_OLD_NEXT_OLD
return|;
block|}
elseif|else
if|if
condition|(
name|ret
operator|>=
literal|1
condition|)
block|{
return|return
name|DeleteCompare
operator|.
name|INCLUDE_NEW_NEXT_NEW
return|;
block|}
comment|// Same column
comment|// Branches below can be optimized.  Keeping like this until testing
comment|// is complete.
if|if
condition|(
name|oldDelete
operator|.
name|type
operator|==
name|newDelete
operator|.
name|type
condition|)
block|{
comment|// the one case where we can merge 2 deletes -> 1 delete.
if|if
condition|(
name|oldDelete
operator|.
name|type
operator|==
name|KeyValue
operator|.
name|Type
operator|.
name|Delete
operator|.
name|getCode
argument_list|()
condition|)
block|{
if|if
condition|(
name|oldDelete
operator|.
name|timestamp
operator|>
name|newDelete
operator|.
name|timestamp
condition|)
block|{
return|return
name|DeleteCompare
operator|.
name|INCLUDE_OLD_NEXT_OLD
return|;
block|}
elseif|else
if|if
condition|(
name|oldDelete
operator|.
name|timestamp
operator|<
name|newDelete
operator|.
name|timestamp
condition|)
block|{
return|return
name|DeleteCompare
operator|.
name|INCLUDE_NEW_NEXT_NEW
return|;
block|}
else|else
block|{
return|return
name|DeleteCompare
operator|.
name|INCLUDE_OLD_NEXT_BOTH
return|;
block|}
block|}
if|if
condition|(
name|oldDelete
operator|.
name|timestamp
operator|<
name|newDelete
operator|.
name|timestamp
condition|)
block|{
return|return
name|DeleteCompare
operator|.
name|INCLUDE_NEW_NEXT_BOTH
return|;
block|}
return|return
name|DeleteCompare
operator|.
name|INCLUDE_OLD_NEXT_BOTH
return|;
block|}
comment|// old delete is more specific than the new delete.
comment|// if the olddelete is newer then the newdelete, we have to
comment|//  keep it
if|if
condition|(
name|oldDelete
operator|.
name|type
operator|<
name|newDelete
operator|.
name|type
condition|)
block|{
if|if
condition|(
name|oldDelete
operator|.
name|timestamp
operator|>
name|newDelete
operator|.
name|timestamp
condition|)
block|{
return|return
name|DeleteCompare
operator|.
name|INCLUDE_OLD_NEXT_OLD
return|;
block|}
elseif|else
if|if
condition|(
name|oldDelete
operator|.
name|timestamp
operator|<
name|newDelete
operator|.
name|timestamp
condition|)
block|{
return|return
name|DeleteCompare
operator|.
name|NEXT_OLD
return|;
block|}
else|else
block|{
return|return
name|DeleteCompare
operator|.
name|NEXT_OLD
return|;
block|}
block|}
comment|// new delete is more specific than the old delete.
if|if
condition|(
name|oldDelete
operator|.
name|type
operator|>
name|newDelete
operator|.
name|type
condition|)
block|{
if|if
condition|(
name|oldDelete
operator|.
name|timestamp
operator|>
name|newDelete
operator|.
name|timestamp
condition|)
block|{
return|return
name|DeleteCompare
operator|.
name|NEXT_NEW
return|;
block|}
elseif|else
if|if
condition|(
name|oldDelete
operator|.
name|timestamp
operator|<
name|newDelete
operator|.
name|timestamp
condition|)
block|{
return|return
name|DeleteCompare
operator|.
name|INCLUDE_NEW_NEXT_NEW
return|;
block|}
else|else
block|{
return|return
name|DeleteCompare
operator|.
name|NEXT_NEW
return|;
block|}
block|}
comment|// Should never reach,
comment|// throw exception for assertion?
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"GetDeleteTracker:compareDelete reached terminal state"
argument_list|)
throw|;
block|}
comment|/**    * Internal class used to store the necessary information for a Delete.    *<p>    * Rather than reparsing the KeyValue, or copying fields, this class points    * to the underlying KeyValue buffer with pointers to the qualifier and fields    * for type and timestamp.  No parsing work is done in DeleteTracker now.    *<p>    * Fields are public because they are accessed often, directly, and only    * within this class.    */
specifier|protected
class|class
name|Delete
block|{
name|byte
index|[]
name|buffer
decl_stmt|;
name|int
name|qualifierOffset
decl_stmt|;
name|int
name|qualifierLength
decl_stmt|;
name|byte
name|type
decl_stmt|;
name|long
name|timestamp
decl_stmt|;
comment|/**      * Constructor      * @param buffer      * @param qualifierOffset      * @param qualifierLength      * @param type      * @param timestamp      */
specifier|public
name|Delete
parameter_list|(
name|byte
index|[]
name|buffer
parameter_list|,
name|int
name|qualifierOffset
parameter_list|,
name|int
name|qualifierLength
parameter_list|,
name|byte
name|type
parameter_list|,
name|long
name|timestamp
parameter_list|)
block|{
name|this
operator|.
name|buffer
operator|=
name|buffer
expr_stmt|;
name|this
operator|.
name|qualifierOffset
operator|=
name|qualifierOffset
expr_stmt|;
name|this
operator|.
name|qualifierLength
operator|=
name|qualifierLength
expr_stmt|;
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
name|this
operator|.
name|timestamp
operator|=
name|timestamp
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

