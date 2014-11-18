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
name|coprocessor
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
name|List
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
name|commons
operator|.
name|logging
operator|.
name|Log
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
name|logging
operator|.
name|LogFactory
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
name|regionserver
operator|.
name|wal
operator|.
name|HLogKey
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
name|wal
operator|.
name|WALEdit
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
comment|/**  * Class for testing WALObserver coprocessor.  *  * It will monitor WAL writing and restoring, and modify passed-in WALEdit, i.e,  * ignore specified columns when writing, or add a KeyValue. On the other  * side, it checks whether the ignored column is still in WAL when Restoreed  * at region reconstruct.  */
end_comment

begin_class
specifier|public
class|class
name|SampleRegionWALObserver
extends|extends
name|BaseRegionObserver
implements|implements
name|WALObserver
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|SampleRegionWALObserver
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|byte
index|[]
name|tableName
decl_stmt|;
specifier|private
name|byte
index|[]
name|row
decl_stmt|;
specifier|private
name|byte
index|[]
name|ignoredFamily
decl_stmt|;
specifier|private
name|byte
index|[]
name|ignoredQualifier
decl_stmt|;
specifier|private
name|byte
index|[]
name|addedFamily
decl_stmt|;
specifier|private
name|byte
index|[]
name|addedQualifier
decl_stmt|;
specifier|private
name|byte
index|[]
name|changedFamily
decl_stmt|;
specifier|private
name|byte
index|[]
name|changedQualifier
decl_stmt|;
specifier|private
name|boolean
name|preWALWriteCalled
init|=
literal|false
decl_stmt|;
specifier|private
name|boolean
name|postWALWriteCalled
init|=
literal|false
decl_stmt|;
specifier|private
name|boolean
name|preWALRestoreCalled
init|=
literal|false
decl_stmt|;
specifier|private
name|boolean
name|postWALRestoreCalled
init|=
literal|false
decl_stmt|;
comment|// Deprecated versions
specifier|private
name|boolean
name|preWALWriteDeprecatedCalled
init|=
literal|false
decl_stmt|;
specifier|private
name|boolean
name|postWALWriteDeprecatedCalled
init|=
literal|false
decl_stmt|;
specifier|private
name|boolean
name|preWALRestoreDeprecatedCalled
init|=
literal|false
decl_stmt|;
specifier|private
name|boolean
name|postWALRestoreDeprecatedCalled
init|=
literal|false
decl_stmt|;
comment|/**    * Set values: with a table name, a column name which will be ignored, and    * a column name which will be added to WAL.    */
specifier|public
name|void
name|setTestValues
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|,
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|igf
parameter_list|,
name|byte
index|[]
name|igq
parameter_list|,
name|byte
index|[]
name|chf
parameter_list|,
name|byte
index|[]
name|chq
parameter_list|,
name|byte
index|[]
name|addf
parameter_list|,
name|byte
index|[]
name|addq
parameter_list|)
block|{
name|this
operator|.
name|row
operator|=
name|row
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
name|this
operator|.
name|ignoredFamily
operator|=
name|igf
expr_stmt|;
name|this
operator|.
name|ignoredQualifier
operator|=
name|igq
expr_stmt|;
name|this
operator|.
name|addedFamily
operator|=
name|addf
expr_stmt|;
name|this
operator|.
name|addedQualifier
operator|=
name|addq
expr_stmt|;
name|this
operator|.
name|changedFamily
operator|=
name|chf
expr_stmt|;
name|this
operator|.
name|changedQualifier
operator|=
name|chq
expr_stmt|;
name|preWALWriteCalled
operator|=
literal|false
expr_stmt|;
name|postWALWriteCalled
operator|=
literal|false
expr_stmt|;
name|preWALRestoreCalled
operator|=
literal|false
expr_stmt|;
name|postWALRestoreCalled
operator|=
literal|false
expr_stmt|;
name|preWALWriteDeprecatedCalled
operator|=
literal|false
expr_stmt|;
name|postWALWriteDeprecatedCalled
operator|=
literal|false
expr_stmt|;
name|preWALRestoreDeprecatedCalled
operator|=
literal|false
expr_stmt|;
name|postWALRestoreDeprecatedCalled
operator|=
literal|false
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|postWALWrite
parameter_list|(
name|ObserverContext
argument_list|<
name|?
extends|extends
name|WALCoprocessorEnvironment
argument_list|>
name|env
parameter_list|,
name|HRegionInfo
name|info
parameter_list|,
name|WALKey
name|logKey
parameter_list|,
name|WALEdit
name|logEdit
parameter_list|)
throws|throws
name|IOException
block|{
name|postWALWriteCalled
operator|=
literal|true
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|postWALWrite
parameter_list|(
name|ObserverContext
argument_list|<
name|WALCoprocessorEnvironment
argument_list|>
name|env
parameter_list|,
name|HRegionInfo
name|info
parameter_list|,
name|HLogKey
name|logKey
parameter_list|,
name|WALEdit
name|logEdit
parameter_list|)
throws|throws
name|IOException
block|{
name|postWALWriteDeprecatedCalled
operator|=
literal|true
expr_stmt|;
name|postWALWrite
argument_list|(
name|env
argument_list|,
name|info
argument_list|,
operator|(
name|WALKey
operator|)
name|logKey
argument_list|,
name|logEdit
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|preWALWrite
parameter_list|(
name|ObserverContext
argument_list|<
name|?
extends|extends
name|WALCoprocessorEnvironment
argument_list|>
name|env
parameter_list|,
name|HRegionInfo
name|info
parameter_list|,
name|WALKey
name|logKey
parameter_list|,
name|WALEdit
name|logEdit
parameter_list|)
throws|throws
name|IOException
block|{
name|boolean
name|bypass
init|=
literal|false
decl_stmt|;
comment|// check table name matches or not.
if|if
condition|(
operator|!
name|Bytes
operator|.
name|equals
argument_list|(
name|info
operator|.
name|getTableName
argument_list|()
argument_list|,
name|this
operator|.
name|tableName
argument_list|)
condition|)
block|{
return|return
name|bypass
return|;
block|}
name|preWALWriteCalled
operator|=
literal|true
expr_stmt|;
comment|// here we're going to remove one keyvalue from the WALEdit, and add
comment|// another one to it.
name|List
argument_list|<
name|Cell
argument_list|>
name|cells
init|=
name|logEdit
operator|.
name|getCells
argument_list|()
decl_stmt|;
name|Cell
name|deletedCell
init|=
literal|null
decl_stmt|;
for|for
control|(
name|Cell
name|cell
range|:
name|cells
control|)
block|{
comment|// assume only one kv from the WALEdit matches.
name|byte
index|[]
name|family
init|=
name|cell
operator|.
name|getFamily
argument_list|()
decl_stmt|;
name|byte
index|[]
name|qulifier
init|=
name|cell
operator|.
name|getQualifier
argument_list|()
decl_stmt|;
if|if
condition|(
name|Arrays
operator|.
name|equals
argument_list|(
name|family
argument_list|,
name|ignoredFamily
argument_list|)
operator|&&
name|Arrays
operator|.
name|equals
argument_list|(
name|qulifier
argument_list|,
name|ignoredQualifier
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Found the KeyValue from WALEdit which should be ignored."
argument_list|)
expr_stmt|;
name|deletedCell
operator|=
name|cell
expr_stmt|;
block|}
if|if
condition|(
name|Arrays
operator|.
name|equals
argument_list|(
name|family
argument_list|,
name|changedFamily
argument_list|)
operator|&&
name|Arrays
operator|.
name|equals
argument_list|(
name|qulifier
argument_list|,
name|changedQualifier
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Found the KeyValue from WALEdit which should be changed."
argument_list|)
expr_stmt|;
name|cell
operator|.
name|getValueArray
argument_list|()
index|[
name|cell
operator|.
name|getValueOffset
argument_list|()
index|]
operator|+=
literal|1
expr_stmt|;
block|}
block|}
if|if
condition|(
literal|null
operator|!=
name|row
condition|)
block|{
name|cells
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row
argument_list|,
name|addedFamily
argument_list|,
name|addedQualifier
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|deletedCell
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"About to delete a KeyValue from WALEdit."
argument_list|)
expr_stmt|;
name|cells
operator|.
name|remove
argument_list|(
name|deletedCell
argument_list|)
expr_stmt|;
block|}
return|return
name|bypass
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|preWALWrite
parameter_list|(
name|ObserverContext
argument_list|<
name|WALCoprocessorEnvironment
argument_list|>
name|env
parameter_list|,
name|HRegionInfo
name|info
parameter_list|,
name|HLogKey
name|logKey
parameter_list|,
name|WALEdit
name|logEdit
parameter_list|)
throws|throws
name|IOException
block|{
name|preWALWriteDeprecatedCalled
operator|=
literal|true
expr_stmt|;
return|return
name|preWALWrite
argument_list|(
name|env
argument_list|,
name|info
argument_list|,
operator|(
name|WALKey
operator|)
name|logKey
argument_list|,
name|logEdit
argument_list|)
return|;
block|}
comment|/**    * Triggered before  {@link org.apache.hadoop.hbase.regionserver.HRegion} when WAL is    * Restoreed.    */
annotation|@
name|Override
specifier|public
name|void
name|preWALRestore
parameter_list|(
name|ObserverContext
argument_list|<
name|?
extends|extends
name|RegionCoprocessorEnvironment
argument_list|>
name|env
parameter_list|,
name|HRegionInfo
name|info
parameter_list|,
name|WALKey
name|logKey
parameter_list|,
name|WALEdit
name|logEdit
parameter_list|)
throws|throws
name|IOException
block|{
name|preWALRestoreCalled
operator|=
literal|true
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|preWALRestore
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|env
parameter_list|,
name|HRegionInfo
name|info
parameter_list|,
name|HLogKey
name|logKey
parameter_list|,
name|WALEdit
name|logEdit
parameter_list|)
throws|throws
name|IOException
block|{
name|preWALRestoreDeprecatedCalled
operator|=
literal|true
expr_stmt|;
name|preWALRestore
argument_list|(
name|env
argument_list|,
name|info
argument_list|,
operator|(
name|WALKey
operator|)
name|logKey
argument_list|,
name|logEdit
argument_list|)
expr_stmt|;
block|}
comment|/**    * Triggered after {@link org.apache.hadoop.hbase.regionserver.HRegion} when WAL is    * Restoreed.    */
annotation|@
name|Override
specifier|public
name|void
name|postWALRestore
parameter_list|(
name|ObserverContext
argument_list|<
name|?
extends|extends
name|RegionCoprocessorEnvironment
argument_list|>
name|env
parameter_list|,
name|HRegionInfo
name|info
parameter_list|,
name|WALKey
name|logKey
parameter_list|,
name|WALEdit
name|logEdit
parameter_list|)
throws|throws
name|IOException
block|{
name|postWALRestoreCalled
operator|=
literal|true
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|postWALRestore
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|env
parameter_list|,
name|HRegionInfo
name|info
parameter_list|,
name|HLogKey
name|logKey
parameter_list|,
name|WALEdit
name|logEdit
parameter_list|)
throws|throws
name|IOException
block|{
name|postWALRestoreDeprecatedCalled
operator|=
literal|true
expr_stmt|;
name|postWALRestore
argument_list|(
name|env
argument_list|,
name|info
argument_list|,
operator|(
name|WALKey
operator|)
name|logKey
argument_list|,
name|logEdit
argument_list|)
expr_stmt|;
block|}
specifier|public
name|boolean
name|isPreWALWriteCalled
parameter_list|()
block|{
return|return
name|preWALWriteCalled
return|;
block|}
specifier|public
name|boolean
name|isPostWALWriteCalled
parameter_list|()
block|{
return|return
name|postWALWriteCalled
return|;
block|}
specifier|public
name|boolean
name|isPreWALRestoreCalled
parameter_list|()
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|SampleRegionWALObserver
operator|.
name|class
operator|.
name|getName
argument_list|()
operator|+
literal|".isPreWALRestoreCalled is called."
argument_list|)
expr_stmt|;
return|return
name|preWALRestoreCalled
return|;
block|}
specifier|public
name|boolean
name|isPostWALRestoreCalled
parameter_list|()
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|SampleRegionWALObserver
operator|.
name|class
operator|.
name|getName
argument_list|()
operator|+
literal|".isPostWALRestoreCalled is called."
argument_list|)
expr_stmt|;
return|return
name|postWALRestoreCalled
return|;
block|}
specifier|public
name|boolean
name|isPreWALWriteDeprecatedCalled
parameter_list|()
block|{
return|return
name|preWALWriteDeprecatedCalled
return|;
block|}
specifier|public
name|boolean
name|isPostWALWriteDeprecatedCalled
parameter_list|()
block|{
return|return
name|postWALWriteDeprecatedCalled
return|;
block|}
specifier|public
name|boolean
name|isPreWALRestoreDeprecatedCalled
parameter_list|()
block|{
return|return
name|preWALRestoreDeprecatedCalled
return|;
block|}
specifier|public
name|boolean
name|isPostWALRestoreDeprecatedCalled
parameter_list|()
block|{
return|return
name|postWALRestoreDeprecatedCalled
return|;
block|}
comment|/**    * This class should trigger our legacy support since it does not directly implement the    * newer API methods.    */
specifier|static
class|class
name|Legacy
extends|extends
name|SampleRegionWALObserver
block|{   }
block|}
end_class

end_unit

