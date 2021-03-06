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
name|replication
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
name|CellBuilderType
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
name|ExtendedCellBuilder
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
name|ExtendedCellBuilderFactory
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
name|WALEdit
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
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Predicate
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
name|WALProtos
operator|.
name|BulkLoadDescriptor
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
name|WALProtos
operator|.
name|StoreDescriptor
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|BulkLoadCellFilter
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
name|BulkLoadCellFilter
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|ExtendedCellBuilder
name|cellBuilder
init|=
name|ExtendedCellBuilderFactory
operator|.
name|create
argument_list|(
name|CellBuilderType
operator|.
name|SHALLOW_COPY
argument_list|)
decl_stmt|;
comment|/**    * Filters the bulk load cell using the supplied predicate.    * @param cell The WAL cell to filter.    * @param famPredicate Returns true of given family should be removed.    * @return The filtered cell.    */
specifier|public
name|Cell
name|filterCell
parameter_list|(
name|Cell
name|cell
parameter_list|,
name|Predicate
argument_list|<
name|byte
index|[]
argument_list|>
name|famPredicate
parameter_list|)
block|{
name|byte
index|[]
name|fam
decl_stmt|;
name|BulkLoadDescriptor
name|bld
init|=
literal|null
decl_stmt|;
try|try
block|{
name|bld
operator|=
name|WALEdit
operator|.
name|getBulkLoadDescriptor
argument_list|(
name|cell
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to get bulk load events information from the WAL file."
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
name|cell
return|;
block|}
name|List
argument_list|<
name|StoreDescriptor
argument_list|>
name|storesList
init|=
name|bld
operator|.
name|getStoresList
argument_list|()
decl_stmt|;
comment|// Copy the StoreDescriptor list and update it as storesList is a unmodifiableList
name|List
argument_list|<
name|StoreDescriptor
argument_list|>
name|copiedStoresList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|storesList
argument_list|)
decl_stmt|;
name|Iterator
argument_list|<
name|StoreDescriptor
argument_list|>
name|copiedStoresListIterator
init|=
name|copiedStoresList
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|boolean
name|anyStoreRemoved
init|=
literal|false
decl_stmt|;
while|while
condition|(
name|copiedStoresListIterator
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|StoreDescriptor
name|sd
init|=
name|copiedStoresListIterator
operator|.
name|next
argument_list|()
decl_stmt|;
name|fam
operator|=
name|sd
operator|.
name|getFamilyName
argument_list|()
operator|.
name|toByteArray
argument_list|()
expr_stmt|;
if|if
condition|(
name|famPredicate
operator|.
name|apply
argument_list|(
name|fam
argument_list|)
condition|)
block|{
name|copiedStoresListIterator
operator|.
name|remove
argument_list|()
expr_stmt|;
name|anyStoreRemoved
operator|=
literal|true
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|!
name|anyStoreRemoved
condition|)
block|{
return|return
name|cell
return|;
block|}
elseif|else
if|if
condition|(
name|copiedStoresList
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
name|BulkLoadDescriptor
operator|.
name|Builder
name|newDesc
init|=
name|BulkLoadDescriptor
operator|.
name|newBuilder
argument_list|()
operator|.
name|setTableName
argument_list|(
name|bld
operator|.
name|getTableName
argument_list|()
argument_list|)
operator|.
name|setEncodedRegionName
argument_list|(
name|bld
operator|.
name|getEncodedRegionName
argument_list|()
argument_list|)
operator|.
name|setBulkloadSeqNum
argument_list|(
name|bld
operator|.
name|getBulkloadSeqNum
argument_list|()
argument_list|)
decl_stmt|;
name|newDesc
operator|.
name|addAllStores
argument_list|(
name|copiedStoresList
argument_list|)
expr_stmt|;
name|BulkLoadDescriptor
name|newBulkLoadDescriptor
init|=
name|newDesc
operator|.
name|build
argument_list|()
decl_stmt|;
return|return
name|cellBuilder
operator|.
name|clear
argument_list|()
operator|.
name|setRow
argument_list|(
name|CellUtil
operator|.
name|cloneRow
argument_list|(
name|cell
argument_list|)
argument_list|)
operator|.
name|setFamily
argument_list|(
name|WALEdit
operator|.
name|METAFAMILY
argument_list|)
operator|.
name|setQualifier
argument_list|(
name|WALEdit
operator|.
name|BULK_LOAD
argument_list|)
operator|.
name|setTimestamp
argument_list|(
name|cell
operator|.
name|getTimestamp
argument_list|()
argument_list|)
operator|.
name|setType
argument_list|(
name|cell
operator|.
name|getTypeByte
argument_list|()
argument_list|)
operator|.
name|setValue
argument_list|(
name|newBulkLoadDescriptor
operator|.
name|toByteArray
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
block|}
end_class

end_unit

