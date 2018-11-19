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
name|procedure2
operator|.
name|store
operator|.
name|wal
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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Comparator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|NoSuchElementException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|lang3
operator|.
name|mutable
operator|.
name|MutableInt
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
name|hadoop
operator|.
name|hbase
operator|.
name|procedure2
operator|.
name|ProcedureUtil
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
name|store
operator|.
name|ProcedureStore
operator|.
name|ProcedureIterator
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
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|ProcedureProtos
import|;
end_import

begin_comment
comment|/**  * Used to build the tree for procedures.  *<p/>  * We will group the procedures with the root procedure, and then validate each group. For each  * group of procedures(with the same root procedure), we will collect all the stack ids, if the max  * stack id is n, then all the stack ids should be from 0 to n, non-repetition and non-omission. If  * not, we will consider all the procedures in this group as corrupted. Please see the code in  * {@link #checkReady(Entry, Map)} method.  *<p/>  * For the procedures not in any group, i.e, can not find the root procedure for these procedures,  * we will also consider them as corrupted. Please see the code in {@link #checkOrphan(Map)} method.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|WALProcedureTree
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|WALProcedureTree
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
class|class
name|Entry
block|{
specifier|private
specifier|final
name|ProcedureProtos
operator|.
name|Procedure
name|proc
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|Entry
argument_list|>
name|subProcs
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|public
name|Entry
parameter_list|(
name|ProcedureProtos
operator|.
name|Procedure
name|proc
parameter_list|)
block|{
name|this
operator|.
name|proc
operator|=
name|proc
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
literal|"Procedure(pid="
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|proc
operator|.
name|getProcId
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", ppid="
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|proc
operator|.
name|hasParentId
argument_list|()
condition|?
name|proc
operator|.
name|getParentId
argument_list|()
else|:
name|Procedure
operator|.
name|NO_PROC_ID
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", class="
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|proc
operator|.
name|getClassName
argument_list|()
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
block|}
comment|// when loading we will iterator the procedures twice, so use this class to cache the deserialized
comment|// result to prevent deserializing multiple times.
specifier|private
specifier|static
specifier|final
class|class
name|ProtoAndProc
block|{
specifier|private
specifier|final
name|ProcedureProtos
operator|.
name|Procedure
name|proto
decl_stmt|;
specifier|private
name|Procedure
argument_list|<
name|?
argument_list|>
name|proc
decl_stmt|;
specifier|public
name|ProtoAndProc
parameter_list|(
name|ProcedureProtos
operator|.
name|Procedure
name|proto
parameter_list|)
block|{
name|this
operator|.
name|proto
operator|=
name|proto
expr_stmt|;
block|}
specifier|public
name|Procedure
argument_list|<
name|?
argument_list|>
name|getProc
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|proc
operator|==
literal|null
condition|)
block|{
name|proc
operator|=
name|ProcedureUtil
operator|.
name|convertToProcedure
argument_list|(
name|proto
argument_list|)
expr_stmt|;
block|}
return|return
name|proc
return|;
block|}
block|}
specifier|private
specifier|final
name|List
argument_list|<
name|ProtoAndProc
argument_list|>
name|validProcs
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|ProtoAndProc
argument_list|>
name|corruptedProcs
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|boolean
name|isFinished
parameter_list|(
name|ProcedureProtos
operator|.
name|Procedure
name|proc
parameter_list|)
block|{
if|if
condition|(
operator|!
name|proc
operator|.
name|hasParentId
argument_list|()
condition|)
block|{
switch|switch
condition|(
name|proc
operator|.
name|getState
argument_list|()
condition|)
block|{
case|case
name|ROLLEDBACK
case|:
case|case
name|SUCCESS
case|:
return|return
literal|true
return|;
default|default:
break|break;
block|}
block|}
return|return
literal|false
return|;
block|}
specifier|private
name|WALProcedureTree
parameter_list|(
name|Map
argument_list|<
name|Long
argument_list|,
name|Entry
argument_list|>
name|procMap
parameter_list|)
block|{
name|List
argument_list|<
name|Entry
argument_list|>
name|rootEntries
init|=
name|buildTree
argument_list|(
name|procMap
argument_list|)
decl_stmt|;
for|for
control|(
name|Entry
name|rootEntry
range|:
name|rootEntries
control|)
block|{
name|checkReady
argument_list|(
name|rootEntry
argument_list|,
name|procMap
argument_list|)
expr_stmt|;
block|}
name|checkOrphan
argument_list|(
name|procMap
argument_list|)
expr_stmt|;
name|Comparator
argument_list|<
name|ProtoAndProc
argument_list|>
name|cmp
init|=
parameter_list|(
name|p1
parameter_list|,
name|p2
parameter_list|)
lambda|->
name|Long
operator|.
name|compare
argument_list|(
name|p1
operator|.
name|proto
operator|.
name|getProcId
argument_list|()
argument_list|,
name|p2
operator|.
name|proto
operator|.
name|getProcId
argument_list|()
argument_list|)
decl_stmt|;
name|Collections
operator|.
name|sort
argument_list|(
name|validProcs
argument_list|,
name|cmp
argument_list|)
expr_stmt|;
name|Collections
operator|.
name|sort
argument_list|(
name|corruptedProcs
argument_list|,
name|cmp
argument_list|)
expr_stmt|;
block|}
specifier|private
name|List
argument_list|<
name|Entry
argument_list|>
name|buildTree
parameter_list|(
name|Map
argument_list|<
name|Long
argument_list|,
name|Entry
argument_list|>
name|procMap
parameter_list|)
block|{
name|List
argument_list|<
name|Entry
argument_list|>
name|rootEntries
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|procMap
operator|.
name|values
argument_list|()
operator|.
name|forEach
argument_list|(
name|entry
lambda|->
block|{
if|if
condition|(
operator|!
name|entry
operator|.
name|proc
operator|.
name|hasParentId
argument_list|()
condition|)
block|{
name|rootEntries
operator|.
name|add
argument_list|(
name|entry
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|Entry
name|parentEntry
init|=
name|procMap
operator|.
name|get
argument_list|(
name|entry
operator|.
name|proc
operator|.
name|getParentId
argument_list|()
argument_list|)
decl_stmt|;
comment|// For a valid procedure this should not be null. We will log the error later if it is null,
comment|// as it will not be referenced by any root procedures.
if|if
condition|(
name|parentEntry
operator|!=
literal|null
condition|)
block|{
name|parentEntry
operator|.
name|subProcs
operator|.
name|add
argument_list|(
name|entry
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
return|return
name|rootEntries
return|;
block|}
specifier|private
name|void
name|collectStackId
parameter_list|(
name|Entry
name|entry
parameter_list|,
name|Map
argument_list|<
name|Integer
argument_list|,
name|List
argument_list|<
name|Entry
argument_list|>
argument_list|>
name|stackId2Proc
parameter_list|,
name|MutableInt
name|maxStackId
parameter_list|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Procedure {} stack ids={}"
argument_list|,
name|entry
argument_list|,
name|entry
operator|.
name|proc
operator|.
name|getStackIdList
argument_list|()
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|,
name|n
init|=
name|entry
operator|.
name|proc
operator|.
name|getStackIdCount
argument_list|()
init|;
name|i
operator|<
name|n
condition|;
name|i
operator|++
control|)
block|{
name|int
name|stackId
init|=
name|entry
operator|.
name|proc
operator|.
name|getStackId
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|stackId
operator|>
name|maxStackId
operator|.
name|intValue
argument_list|()
condition|)
block|{
name|maxStackId
operator|.
name|setValue
argument_list|(
name|stackId
argument_list|)
expr_stmt|;
block|}
name|stackId2Proc
operator|.
name|computeIfAbsent
argument_list|(
name|stackId
argument_list|,
name|k
lambda|->
operator|new
name|ArrayList
argument_list|<>
argument_list|()
argument_list|)
operator|.
name|add
argument_list|(
name|entry
argument_list|)
expr_stmt|;
block|}
name|entry
operator|.
name|subProcs
operator|.
name|forEach
argument_list|(
name|e
lambda|->
name|collectStackId
argument_list|(
name|e
argument_list|,
name|stackId2Proc
argument_list|,
name|maxStackId
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|addAllToCorruptedAndRemoveFromProcMap
parameter_list|(
name|Entry
name|entry
parameter_list|,
name|Map
argument_list|<
name|Long
argument_list|,
name|Entry
argument_list|>
name|remainingProcMap
parameter_list|)
block|{
name|corruptedProcs
operator|.
name|add
argument_list|(
operator|new
name|ProtoAndProc
argument_list|(
name|entry
operator|.
name|proc
argument_list|)
argument_list|)
expr_stmt|;
name|remainingProcMap
operator|.
name|remove
argument_list|(
name|entry
operator|.
name|proc
operator|.
name|getProcId
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Entry
name|e
range|:
name|entry
operator|.
name|subProcs
control|)
block|{
name|addAllToCorruptedAndRemoveFromProcMap
argument_list|(
name|e
argument_list|,
name|remainingProcMap
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|addAllToValidAndRemoveFromProcMap
parameter_list|(
name|Entry
name|entry
parameter_list|,
name|Map
argument_list|<
name|Long
argument_list|,
name|Entry
argument_list|>
name|remainingProcMap
parameter_list|)
block|{
name|validProcs
operator|.
name|add
argument_list|(
operator|new
name|ProtoAndProc
argument_list|(
name|entry
operator|.
name|proc
argument_list|)
argument_list|)
expr_stmt|;
name|remainingProcMap
operator|.
name|remove
argument_list|(
name|entry
operator|.
name|proc
operator|.
name|getProcId
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Entry
name|e
range|:
name|entry
operator|.
name|subProcs
control|)
block|{
name|addAllToValidAndRemoveFromProcMap
argument_list|(
name|e
argument_list|,
name|remainingProcMap
argument_list|)
expr_stmt|;
block|}
block|}
comment|// In this method first we will check whether the given root procedure and all its sub procedures
comment|// are valid, through the procedure stack. And we will also remove all these procedures from the
comment|// remainingProcMap, so at last, if there are still procedures in the map, we know that there are
comment|// orphan procedures.
specifier|private
name|void
name|checkReady
parameter_list|(
name|Entry
name|rootEntry
parameter_list|,
name|Map
argument_list|<
name|Long
argument_list|,
name|Entry
argument_list|>
name|remainingProcMap
parameter_list|)
block|{
if|if
condition|(
name|isFinished
argument_list|(
name|rootEntry
operator|.
name|proc
argument_list|)
condition|)
block|{
if|if
condition|(
operator|!
name|rootEntry
operator|.
name|subProcs
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"unexpected active children for root-procedure: {}"
argument_list|,
name|rootEntry
argument_list|)
expr_stmt|;
name|rootEntry
operator|.
name|subProcs
operator|.
name|forEach
argument_list|(
name|e
lambda|->
name|LOG
operator|.
name|error
argument_list|(
literal|"unexpected active children: {}"
argument_list|,
name|e
argument_list|)
argument_list|)
expr_stmt|;
name|addAllToCorruptedAndRemoveFromProcMap
argument_list|(
name|rootEntry
argument_list|,
name|remainingProcMap
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|addAllToValidAndRemoveFromProcMap
argument_list|(
name|rootEntry
argument_list|,
name|remainingProcMap
argument_list|)
expr_stmt|;
block|}
return|return;
block|}
name|Map
argument_list|<
name|Integer
argument_list|,
name|List
argument_list|<
name|Entry
argument_list|>
argument_list|>
name|stackId2Proc
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|MutableInt
name|maxStackId
init|=
operator|new
name|MutableInt
argument_list|(
name|Integer
operator|.
name|MIN_VALUE
argument_list|)
decl_stmt|;
name|collectStackId
argument_list|(
name|rootEntry
argument_list|,
name|stackId2Proc
argument_list|,
name|maxStackId
argument_list|)
expr_stmt|;
comment|// the stack ids should start from 0 and increase by one every time
name|boolean
name|valid
init|=
literal|true
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<=
name|maxStackId
operator|.
name|intValue
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|List
argument_list|<
name|Entry
argument_list|>
name|entries
init|=
name|stackId2Proc
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|entries
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Missing stack id {}, max stack id is {}, root procedure is {}"
argument_list|,
name|i
argument_list|,
name|maxStackId
argument_list|,
name|rootEntry
argument_list|)
expr_stmt|;
name|valid
operator|=
literal|false
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|entries
operator|.
name|size
argument_list|()
operator|>
literal|1
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Multiple procedures {} have the same stack id {}, max stack id is {},"
operator|+
literal|" root procedure is {}"
argument_list|,
name|entries
argument_list|,
name|i
argument_list|,
name|maxStackId
argument_list|,
name|rootEntry
argument_list|)
expr_stmt|;
name|valid
operator|=
literal|false
expr_stmt|;
block|}
block|}
if|if
condition|(
name|valid
condition|)
block|{
name|addAllToValidAndRemoveFromProcMap
argument_list|(
name|rootEntry
argument_list|,
name|remainingProcMap
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|addAllToCorruptedAndRemoveFromProcMap
argument_list|(
name|rootEntry
argument_list|,
name|remainingProcMap
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|checkOrphan
parameter_list|(
name|Map
argument_list|<
name|Long
argument_list|,
name|Entry
argument_list|>
name|procMap
parameter_list|)
block|{
name|procMap
operator|.
name|values
argument_list|()
operator|.
name|forEach
argument_list|(
name|entry
lambda|->
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Orphan procedure: {}"
argument_list|,
name|entry
argument_list|)
expr_stmt|;
name|corruptedProcs
operator|.
name|add
argument_list|(
operator|new
name|ProtoAndProc
argument_list|(
name|entry
operator|.
name|proc
argument_list|)
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
specifier|final
class|class
name|Iter
implements|implements
name|ProcedureIterator
block|{
specifier|private
specifier|final
name|List
argument_list|<
name|ProtoAndProc
argument_list|>
name|procs
decl_stmt|;
specifier|private
name|Iterator
argument_list|<
name|ProtoAndProc
argument_list|>
name|iter
decl_stmt|;
specifier|private
name|ProtoAndProc
name|current
decl_stmt|;
specifier|public
name|Iter
parameter_list|(
name|List
argument_list|<
name|ProtoAndProc
argument_list|>
name|procs
parameter_list|)
block|{
name|this
operator|.
name|procs
operator|=
name|procs
expr_stmt|;
name|reset
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|reset
parameter_list|()
block|{
name|iter
operator|=
name|procs
operator|.
name|iterator
argument_list|()
expr_stmt|;
if|if
condition|(
name|iter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|current
operator|=
name|iter
operator|.
name|next
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|current
operator|=
literal|null
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|hasNext
parameter_list|()
block|{
return|return
name|current
operator|!=
literal|null
return|;
block|}
specifier|private
name|void
name|checkNext
parameter_list|()
block|{
if|if
condition|(
operator|!
name|hasNext
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|NoSuchElementException
argument_list|()
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isNextFinished
parameter_list|()
block|{
name|checkNext
argument_list|()
expr_stmt|;
return|return
name|isFinished
argument_list|(
name|current
operator|.
name|proto
argument_list|)
return|;
block|}
specifier|private
name|void
name|moveToNext
parameter_list|()
block|{
if|if
condition|(
name|iter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|current
operator|=
name|iter
operator|.
name|next
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|current
operator|=
literal|null
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|skipNext
parameter_list|()
block|{
name|checkNext
argument_list|()
expr_stmt|;
name|moveToNext
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Procedure
argument_list|<
name|?
argument_list|>
name|next
parameter_list|()
throws|throws
name|IOException
block|{
name|checkNext
argument_list|()
expr_stmt|;
name|Procedure
argument_list|<
name|?
argument_list|>
name|proc
init|=
name|current
operator|.
name|getProc
argument_list|()
decl_stmt|;
name|moveToNext
argument_list|()
expr_stmt|;
return|return
name|proc
return|;
block|}
block|}
specifier|public
name|ProcedureIterator
name|getValidProcs
parameter_list|()
block|{
return|return
operator|new
name|Iter
argument_list|(
name|validProcs
argument_list|)
return|;
block|}
specifier|public
name|ProcedureIterator
name|getCorruptedProcs
parameter_list|()
block|{
return|return
operator|new
name|Iter
argument_list|(
name|corruptedProcs
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|WALProcedureTree
name|build
parameter_list|(
name|Collection
argument_list|<
name|ProcedureProtos
operator|.
name|Procedure
argument_list|>
name|procedures
parameter_list|)
block|{
name|Map
argument_list|<
name|Long
argument_list|,
name|Entry
argument_list|>
name|procMap
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|ProcedureProtos
operator|.
name|Procedure
name|proc
range|:
name|procedures
control|)
block|{
name|procMap
operator|.
name|put
argument_list|(
name|proc
operator|.
name|getProcId
argument_list|()
argument_list|,
operator|new
name|Entry
argument_list|(
name|proc
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|WALProcedureTree
argument_list|(
name|procMap
argument_list|)
return|;
block|}
block|}
end_class

end_unit

