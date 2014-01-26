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
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicLong
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

begin_comment
comment|/**  * A WAL Entry for {@link FSHLog} implementation.  Immutable.  * It is a subclass of {@link HLog.Entry} that carries extra info across the ring buffer such as  * region sequence id (we want to use this later, just before we write the WAL to ensure region  * edits maintain order).  The extra info added here is not 'serialized' as part of the WALEdit  * hence marked 'transient' to underline this fact.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|FSWALEntry
extends|extends
name|HLog
operator|.
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
name|AtomicLong
name|regionSequenceIdReference
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
name|FSWALEntry
parameter_list|(
specifier|final
name|long
name|sequence
parameter_list|,
specifier|final
name|HLogKey
name|key
parameter_list|,
specifier|final
name|WALEdit
name|edit
parameter_list|,
specifier|final
name|AtomicLong
name|referenceToRegionSequenceId
parameter_list|,
specifier|final
name|boolean
name|inMemstore
parameter_list|,
specifier|final
name|HTableDescriptor
name|htd
parameter_list|,
specifier|final
name|HRegionInfo
name|hri
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
name|regionSequenceIdReference
operator|=
name|referenceToRegionSequenceId
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
name|AtomicLong
name|getRegionSequenceIdReference
parameter_list|()
block|{
return|return
name|this
operator|.
name|regionSequenceIdReference
return|;
block|}
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
block|}
end_class

end_unit

