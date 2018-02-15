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
name|master
operator|.
name|procedure
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
name|procedure2
operator|.
name|LockStatus
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
name|procedure2
operator|.
name|Procedure
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

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|TableQueue
extends|extends
name|Queue
argument_list|<
name|TableName
argument_list|>
block|{
specifier|private
specifier|final
name|LockStatus
name|namespaceLockStatus
decl_stmt|;
specifier|public
name|TableQueue
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|int
name|priority
parameter_list|,
name|LockStatus
name|tableLock
parameter_list|,
name|LockStatus
name|namespaceLockStatus
parameter_list|)
block|{
name|super
argument_list|(
name|tableName
argument_list|,
name|priority
argument_list|,
name|tableLock
argument_list|)
expr_stmt|;
name|this
operator|.
name|namespaceLockStatus
operator|=
name|namespaceLockStatus
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isAvailable
parameter_list|()
block|{
comment|// if there are no items in the queue, or the namespace is locked.
comment|// we can't execute operation on this table
if|if
condition|(
name|isEmpty
argument_list|()
operator|||
name|namespaceLockStatus
operator|.
name|hasExclusiveLock
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|getLockStatus
argument_list|()
operator|.
name|hasExclusiveLock
argument_list|()
condition|)
block|{
comment|// if we have an exclusive lock already taken
comment|// only child of the lock owner can be executed
specifier|final
name|Procedure
argument_list|<
name|?
argument_list|>
name|nextProc
init|=
name|peek
argument_list|()
decl_stmt|;
return|return
name|nextProc
operator|!=
literal|null
operator|&&
name|getLockStatus
argument_list|()
operator|.
name|hasLockAccess
argument_list|(
name|nextProc
argument_list|)
return|;
block|}
comment|// no xlock
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|requireExclusiveLock
parameter_list|(
name|Procedure
argument_list|<
name|?
argument_list|>
name|proc
parameter_list|)
block|{
return|return
name|requireTableExclusiveLock
argument_list|(
operator|(
name|TableProcedureInterface
operator|)
name|proc
argument_list|)
return|;
block|}
comment|/**    * @param proc must not be null    */
specifier|private
specifier|static
name|boolean
name|requireTableExclusiveLock
parameter_list|(
name|TableProcedureInterface
name|proc
parameter_list|)
block|{
switch|switch
condition|(
name|proc
operator|.
name|getTableOperationType
argument_list|()
condition|)
block|{
case|case
name|CREATE
case|:
case|case
name|DELETE
case|:
case|case
name|DISABLE
case|:
case|case
name|ENABLE
case|:
return|return
literal|true
return|;
case|case
name|EDIT
case|:
comment|// we allow concurrent edit on the NS table
return|return
operator|!
name|proc
operator|.
name|getTableName
argument_list|()
operator|.
name|equals
argument_list|(
name|TableName
operator|.
name|NAMESPACE_TABLE_NAME
argument_list|)
return|;
case|case
name|READ
case|:
return|return
literal|false
return|;
comment|// region operations are using the shared-lock on the table
comment|// and then they will grab an xlock on the region.
case|case
name|REGION_SPLIT
case|:
case|case
name|REGION_MERGE
case|:
case|case
name|REGION_ASSIGN
case|:
case|case
name|REGION_UNASSIGN
case|:
case|case
name|REGION_EDIT
case|:
case|case
name|REGION_GC
case|:
case|case
name|MERGED_REGIONS_GC
case|:
return|return
literal|false
return|;
default|default:
break|break;
block|}
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"unexpected type "
operator|+
name|proc
operator|.
name|getTableOperationType
argument_list|()
argument_list|)
throw|;
block|}
block|}
end_class

end_unit

