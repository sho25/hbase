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
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
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
name|HRegionInfo
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
name|HTableDescriptor
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
name|regionserver
operator|.
name|MultiVersionConcurrencyControl
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
name|CollectionUtils
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Sets
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
name|wal
operator|.
name|WAL
operator|.
name|Entry
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
name|wal
operator|.
name|WALKey
import|;
end_import

begin_comment
comment|/**  * A WAL Entry for {@link FSHLog} implementation.  Immutable.  * A subclass of {@link Entry} that carries extra info across the ring buffer such as  * region sequence id (we want to use this later, just before we write the WAL to ensure region  * edits maintain order).  The extra info added here is not 'serialized' as part of the WALEdit  * hence marked 'transient' to underline this fact.  It also adds mechanism so we can wait on  * the assign of the region sequence id.  See #stampRegionSequenceId().  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|FSWALEntry
extends|extends
name|Entry
block|{
comment|// The below data members are denoted 'transient' just to highlight these are not persisted;
comment|// they are only in memory and held here while passing over the ring buffer.
specifier|private
specifier|final
specifier|transient
name|long
name|sequence
decl_stmt|;
specifier|private
specifier|final
specifier|transient
name|boolean
name|inMemstore
decl_stmt|;
specifier|private
specifier|final
specifier|transient
name|HTableDescriptor
name|htd
decl_stmt|;
specifier|private
specifier|final
specifier|transient
name|HRegionInfo
name|hri
decl_stmt|;
specifier|private
specifier|final
name|Set
argument_list|<
name|byte
index|[]
argument_list|>
name|familyNames
decl_stmt|;
name|FSWALEntry
parameter_list|(
specifier|final
name|long
name|sequence
parameter_list|,
specifier|final
name|WALKey
name|key
parameter_list|,
specifier|final
name|WALEdit
name|edit
parameter_list|,
specifier|final
name|HTableDescriptor
name|htd
parameter_list|,
specifier|final
name|HRegionInfo
name|hri
parameter_list|,
specifier|final
name|boolean
name|inMemstore
parameter_list|)
block|{
name|super
argument_list|(
name|key
argument_list|,
name|edit
argument_list|)
expr_stmt|;
name|this
operator|.
name|inMemstore
operator|=
name|inMemstore
expr_stmt|;
name|this
operator|.
name|htd
operator|=
name|htd
expr_stmt|;
name|this
operator|.
name|hri
operator|=
name|hri
expr_stmt|;
name|this
operator|.
name|sequence
operator|=
name|sequence
expr_stmt|;
if|if
condition|(
name|inMemstore
condition|)
block|{
comment|// construct familyNames here to reduce the work of log sinker.
name|ArrayList
argument_list|<
name|Cell
argument_list|>
name|cells
init|=
name|this
operator|.
name|getEdit
argument_list|()
operator|.
name|getCells
argument_list|()
decl_stmt|;
if|if
condition|(
name|CollectionUtils
operator|.
name|isEmpty
argument_list|(
name|cells
argument_list|)
condition|)
block|{
name|this
operator|.
name|familyNames
operator|=
name|Collections
operator|.
expr|<
name|byte
index|[]
operator|>
name|emptySet
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|Set
argument_list|<
name|byte
index|[]
argument_list|>
name|familySet
init|=
name|Sets
operator|.
name|newTreeSet
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
decl_stmt|;
for|for
control|(
name|Cell
name|cell
range|:
name|cells
control|)
block|{
if|if
condition|(
operator|!
name|CellUtil
operator|.
name|matchingFamily
argument_list|(
name|cell
argument_list|,
name|WALEdit
operator|.
name|METAFAMILY
argument_list|)
condition|)
block|{
name|familySet
operator|.
name|add
argument_list|(
name|CellUtil
operator|.
name|cloneFamily
argument_list|(
name|cell
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|this
operator|.
name|familyNames
operator|=
name|Collections
operator|.
name|unmodifiableSet
argument_list|(
name|familySet
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|this
operator|.
name|familyNames
operator|=
name|Collections
operator|.
expr|<
name|byte
index|[]
operator|>
name|emptySet
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"sequence="
operator|+
name|this
operator|.
name|sequence
operator|+
literal|", "
operator|+
name|super
operator|.
name|toString
argument_list|()
return|;
block|}
empty_stmt|;
name|boolean
name|isInMemstore
parameter_list|()
block|{
return|return
name|this
operator|.
name|inMemstore
return|;
block|}
name|HTableDescriptor
name|getHTableDescriptor
parameter_list|()
block|{
return|return
name|this
operator|.
name|htd
return|;
block|}
name|HRegionInfo
name|getHRegionInfo
parameter_list|()
block|{
return|return
name|this
operator|.
name|hri
return|;
block|}
comment|/**    * @return The sequence on the ring buffer when this edit was added.    */
name|long
name|getSequence
parameter_list|()
block|{
return|return
name|this
operator|.
name|sequence
return|;
block|}
comment|/**    * Here is where a WAL edit gets its sequenceid.    * SIDE-EFFECT is our stamping the sequenceid into every Cell AND setting the sequenceid into the    * MVCC WriteEntry!!!!    * @return The sequenceid we stamped on this edit.    * @throws IOException    */
name|long
name|stampRegionSequenceId
parameter_list|()
throws|throws
name|IOException
block|{
name|long
name|regionSequenceId
init|=
name|WALKey
operator|.
name|NO_SEQUENCE_ID
decl_stmt|;
name|MultiVersionConcurrencyControl
name|mvcc
init|=
name|getKey
argument_list|()
operator|.
name|getMvcc
argument_list|()
decl_stmt|;
name|MultiVersionConcurrencyControl
operator|.
name|WriteEntry
name|we
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|mvcc
operator|!=
literal|null
condition|)
block|{
name|we
operator|=
name|mvcc
operator|.
name|begin
argument_list|()
expr_stmt|;
name|regionSequenceId
operator|=
name|we
operator|.
name|getWriteNumber
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|this
operator|.
name|getEdit
argument_list|()
operator|.
name|isReplay
argument_list|()
operator|&&
name|inMemstore
condition|)
block|{
for|for
control|(
name|Cell
name|c
range|:
name|getEdit
argument_list|()
operator|.
name|getCells
argument_list|()
control|)
block|{
name|CellUtil
operator|.
name|setSequenceId
argument_list|(
name|c
argument_list|,
name|regionSequenceId
argument_list|)
expr_stmt|;
block|}
block|}
comment|// This has to stay in this order
name|WALKey
name|key
init|=
name|getKey
argument_list|()
decl_stmt|;
name|key
operator|.
name|setLogSeqNum
argument_list|(
name|regionSequenceId
argument_list|)
expr_stmt|;
name|key
operator|.
name|setWriteEntry
argument_list|(
name|we
argument_list|)
expr_stmt|;
return|return
name|regionSequenceId
return|;
block|}
comment|/**    * @return the family names which are effected by this edit.    */
name|Set
argument_list|<
name|byte
index|[]
argument_list|>
name|getFamilyNames
parameter_list|()
block|{
return|return
name|familyNames
return|;
block|}
block|}
end_class

end_unit

